# Aegis API Platform

A production-grade, multi-tenant API gateway built with Spring Boot. Enforces per-API per-plan rate limits via Redis atomic counters, processes usage events asynchronously through RabbitMQ with DLQ/retry, and proxies validated requests to tenant backend services — deployed on AWS EC2 behind Nginx with Docker Compose.

---

## Architecture
```
                     ┌──────────────────────────────────────────────┐
                     │               Client (External)              │
                     └─────────────────────┬────────────────────────┘
                                           │ HTTPS
                                           ▼
                     ┌──────────────────────────────────────────────┐
                     │          Nginx (TLS Termination)             │
                     │    HTTP → HTTPS redirect, proxy_pass         │
                     └─────────────────────┬────────────────────────┘
                                           │ HTTP (internal)
                                           ▼
         ┌─────────────────────────────────────────────────────────────────┐
         │                   Spring Boot Application                       │
         │                                                                 │
         │  ┌──────────────────────┐     ┌───────────────────────────────┐ │
         │  │   /api/**  (JWT)     │     │   /gateway/**  (API Key)      │ │
         │  │                      │     │                               │ │
         │  │  Auth, Tenant CRUD,  │     │  ApiKeyAuthFilter             │ │
         │  │  Plan Management,    │     │         │                     │ │
         │  │  API Definition,     │     │         ▼                     │ │
         │  │  API Key CRUD,       │     │  PolicyResolver               │ │
         │  │  Analytics           │     │  (plan + per-API overrides)   │ │
         │  └──────────────────────┘     │         │                     │ │
         │                               │         ▼                     │ │
         │                         ┌─────┤  RateLimitService             │ │
         │                         │Redis│  (INCR per tenant+api+min)    │ │
         │                         └─────┤         │                     │ │
         │                         ┌─────┤  QuotaService                 │ │
         │                         │Redis│  (INCR per tenant per month)  │ │
         │                         └─────┤         │                     │ │
         │                               │         ▼                     │ │
         │                               │  WebClient → Target URL       │ │
         │                               │  (Circuit Breaker + Retry)    │ │
         │                               │         │                     │ │
         │                         ┌─────┤  UsageEventPublisher          │ │
         │                         │ MQ  │  (async fire-and-forget)      │ │
         │                         └─────└───────────────────────────────┘ │
         └─────────────────────────────────────────────────────────────────┘
                   │ RabbitMQ                         │ MySQL
                   ▼                                  ▼
         ┌──────────────────┐             ┌─────────────────────┐
         │  usage.queue     │             │  usage_log table    │
         │  (DLQ + retry)   │             │  (indexed queries)  │
         │       │          │             └─────────────────────┘
         │       ▼          │             ┌─────────────────────┐
         │  UsageConsumer   │             │ Prometheus + Grafana│
         │  (idempotent)    │             │ (Micrometer metrics)│
         └──────────────────┘             └─────────────────────┘
```

---

## What's Actually Built

### Multi-Tenant Management
- Tenants are isolated entities, each assigned a subscription plan at creation
- Tenant status lifecycle: `ACTIVE → SUSPENDED → DELETED`
- All gateway and management operations are tenant-scoped; one tenant cannot access another's APIs or data

### Subscription Plans & Policy Override System
- Plans define a global `rateLimitPerMinute` and `monthlyQuota`
- `PlanApiConfig` allows per-API overrides on top of plan defaults — a FREE plan can impose tighter limits on `/payments` than on `/products` without creating a separate plan
- `PolicyResolverService` merges plan defaults with API-level overrides at request time

### API Key Lifecycle
- Raw key generated as `ak_live_<UUID>`, returned to caller **once only**
- SHA-256 hash stored in DB; raw key is never persisted
- Keys are tenant-scoped, optionally expiry-bounded, and revocable
- Validated in `ApiKeyAuthenticationFilter` before any rate/quota check runs

### Redis Rate Limiting
- Key pattern: `rate:{tenantId}:{apiId}:{yyyyMMddHHmm}`
- `INCR` on key; TTL of 60s set on first increment (count == 1)
- Per-API per-plan limit enforced; throws `RateLimitExceededException` → HTTP 429

### Redis Monthly Quota
- Key pattern: `quota:{tenantId}:{yyyyMM}`
- On cache miss, seeds counter from `usage_log` DB count for the current month, then increments
- TTL set to 31 days; throws `MonthlyQuotaExceededException` → HTTP 402

### Async Usage Logging (RabbitMQ)
- Every successful proxied request publishes a `UsageEvent` to `usage.exchange` (Direct exchange) with routing key `usage.event`
- Consumer persists to `usage_log` with a unique `event_id` (UUID) for idempotency — duplicates detected via DB unique constraint and silently dropped
- On consumer failure: 3 retries with exponential backoff (1s base, 2× multiplier, 10s cap); exhausted messages routed to `usage.dlq.queue` via dead-letter exchange
- `setDefaultRequeueRejected(false)` prevents infinite retry loops

### Reverse Proxy (Gateway)
- `GatewayController` matches `/gateway/**` requests to a registered `ApiDefinition` by `tenantId + path + HTTP method`
- Proxies via `WebClient` to `targetUrl`; selected headers forwarded, sensitive headers (`Authorization`, `Cookie`, `Host`) stripped
- `BackendCallerService` wraps the call with Resilience4j Circuit Breaker + Reactor retry (2 attempts, 200ms backoff) for transient errors and 5xx responses
- Fallback returns HTTP 503 on circuit open

### Analytics
- `SYSTEM_ADMIN`: query usage for any tenant
- `TENANT_ADMIN`: query own usage with optional `?from=&to=` date range (max 90-day window, defaults to last 30 days)
- Endpoints: total requests, per-API breakdown (paginated), daily usage, remaining monthly quota

### Observability
- Structured JSON logging via `logstash-logback-encoder` with `correlationId` propagated through MDC in every log line
- Custom Micrometer counters: `gateway_request_total`, `rate_limit_exceeded_total`, `monthly_quota_exceeded_total`
- Prometheus scrape at `/actuator/prometheus`; Grafana wired in Docker Compose

---

## Key Technical Decisions

**Redis atomic counters for rate limiting, not DB writes**
A relational DB cannot handle sub-second counter increments at gateway throughput without becoming the bottleneck. Redis `INCR` is O(1), atomic, and single-threaded internally — no locking needed. In-memory counters per JVM instance break across multiple replicas.

**RabbitMQ for usage logging, not synchronous DB writes**
Writing to `usage_log` on every gateway request adds a DB round-trip to the critical path. Decoupling via RabbitMQ means gateway response time is unaffected by DB write latency spikes. The tradeoff is eventual consistency in analytics — acceptable since analytics are not real-time requirements.

**Idempotent consumer with event UUID**
RabbitMQ's at-least-once delivery guarantee means a message can be redelivered after a consumer crash mid-processing. Without idempotency, retries produce duplicate usage records that corrupt analytics. The `event_id` unique constraint is the simplest durable guard.

**DLQ over infinite retry**
Infinite retry on a poison message blocks the consumer indefinitely. Routing to a DLQ after N attempts preserves the message for inspection and replay without blocking the main queue.

**Per-API plan config overrides**
A flat plan rate limit applied uniformly across all APIs is too coarse for real SaaS products. `PlanApiConfig` encodes per-API limits without requiring plan proliferation.

**Flyway over `ddl-auto: create`**
`ddl-auto: create` destroys data on restart and is untraceable in production. Flyway provides versioned, auditable schema migrations that behave identically in local Docker, CI, and EC2.

**Non-root Docker user**
Running as root in a container means a container escape equals root on the host. `aegisuser` with no elevated permissions limits blast radius. Combined with a JRE-only Alpine runtime image, the attack surface is minimal.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8 + Flyway migrations |
| Cache / Rate Limiting | Redis 7 (String counters) |
| Messaging | RabbitMQ 3 (Direct exchange, DLQ, retry) |
| Proxy Client | Spring WebFlux `WebClient` |
| Resilience | Resilience4j Circuit Breaker |
| Observability | Micrometer, Prometheus, Grafana |
| Auth | JWT (HS256) + API Key (SHA-256 hashed) |
| Containerization | Docker (multi-stage, non-root) + Docker Compose |
| Reverse Proxy | Nginx (TLS termination, HTTP→HTTPS redirect) |
| Infra | AWS EC2 |

---

## How to Run

### Prerequisites
- Docker and Docker Compose installed
- Ports 80, 443, 8080, 3000, 9090 available

### 1. Configure environment
```bash
cp .env.example .env
```

Edit `.env`:
```env
SPRING_PROFILE=prod
DB_ROOT_PASSWORD=<secure_password>
DB_NAME=aegis_api_platform
DB_USERNAME=aegis_user
DB_PASSWORD=<secure_password>
RABBIT_USER=aegis_rabbit
RABBIT_PASS=<secure_password>
JWT_SECRET=<base64_encoded_256bit_secret>
```

Generate a JWT secret:
```bash
openssl rand -base64 32
```

### 2. Start the stack

**Production:**
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

**Local development (exposes all service ports):**
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

Startup order is enforced via healthchecks: MySQL → Redis → RabbitMQ → App → Nginx.

### 3. Verify
```bash
curl http://localhost/actuator/health
# Swagger UI (dev only): http://localhost/swagger-ui/index.html
```

### 4. Example bootstrap flow
```bash
# 1. Login as SYSTEM_ADMIN
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@aegis.io","password":"<password>"}'

# 2. Create a subscription plan
curl -X POST http://localhost/api/admin/plans \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"planCode":"PRO","name":"Pro Plan","monthlyQuota":100000,"rateLimitPerMinute":100,"price":49.99,"currency":"USD"}'

# 3. Create a tenant
curl -X POST http://localhost/api/admin/tenants \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp","subscriptionPlanId":1}'

# 4. Create a TENANT_ADMIN user
curl -X POST http://localhost/api/admin/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@acme.io","password":"acme1234","role":"TENANT_ADMIN","tenantId":1}'

# 5. Login as tenant, register an API, generate an API key, then call gateway:
curl -H "X-API-KEY: ak_live_..." http://localhost/gateway/your-path
```

### Monitoring

| Service | URL |
|---|---|
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| RabbitMQ Management | http://localhost:15672 (dev only) |

---

## Project Structure
```
.
├── aegis-api-platform/
│   ├── src/main/java/com/aegis/api_platform/
│   │   ├── analytics/        # Usage analytics (controller, service, projections)
│   │   ├── common/           # CorrelationIdFilter
│   │   ├── config/           # Security, RabbitMQ, WebClient config
│   │   ├── controller/       # REST controllers (admin + tenant management)
│   │   ├── dto/              # Request/response records
│   │   ├── exception/        # Custom exceptions + GlobalExceptionHandler
│   │   ├── gateway/          # API key filter, proxy controller, circuit breaker
│   │   ├── messaging/        # RabbitMQ publisher, consumer, events
│   │   ├── metrics/          # Micrometer custom counters
│   │   ├── model/            # JPA entities
│   │   ├── policy/           # ApiPolicy + PolicyResolverService
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # JWT filter, JwtService, SecurityUtils
│   │   └── service/          # Service interfaces + implementations
│   └── src/main/resources/
│       ├── db/migration/     # Flyway SQL migrations V1–V12
│       └── application*.yaml # Base + dev + prod profiles
├── docker-compose.yml        # Base compose (all services)
├── docker-compose.dev.yml    # Dev overrides (port exposure)
├── docker-compose.prod.yml   # Prod overrides
├── nginx/default.conf        # Nginx TLS + reverse proxy config
└── monitoring/prometheus.yml # Prometheus scrape config
```