# Multi-Tenant API Management Platform

A backend-only Spring Boot application that simulates a production-style **API Management and Traffic Control Platform**.
The system models how modern SaaS infrastructure platforms manage API keys, enforce subscription-based access, apply rate limiting policies, and track usage analytics across multiple tenants.

---

## 📌 Project Overview

This project implements a **multi-tenant API access management platform** where SaaS companies can register their APIs, configure subscription plans, and enforce usage policies.

The focus is on backend architecture, policy-driven enforcement, infrastructure-level traffic control, and scalable system design — without frontend dependencies.

### Key Goals

- Support multiple SaaS tenants
- Define APIs per tenant
- Configure subscription plans per tenant
- Enforce per-API per-tier rate limits
- Enforce tenant-level monthly quota
- Implement Redis-based rate limiting
- Process usage logs asynchronously using RabbitMQ
- Provide analytics endpoints
- Deploy using Docker and AWS
- Automate build and deployment using CI/CD
- Extend into a lightweight reverse proxy (Phase 2)

---

## 🧠 Core Concepts Implemented

- Multi-Tenant Backend Architecture
- API Key Authentication & Lifecycle Management
- Subscription Plan Enforcement
- Per-API Per-Tier Rate Limiting
- Tenant-Level Monthly Quota Tracking
- Redis Atomic Counters
- Event-Driven Async Logging
- Infrastructure-Oriented Backend Design
- Reverse Proxy Simulation (Phase 2)

---

## 🏢 Multi-Tenant Support

The platform supports multiple independent SaaS tenants.

Each tenant can:

- Register in the system
- Define and manage their APIs
- Create subscription plans
- Generate API keys
- Monitor usage analytics independently

All configurations and policies are tenant-scoped to ensure isolation.

---

## 🔌 API Definitions Per Tenant

Each tenant can define APIs they want to protect.

An API definition includes:

- Logical identifier
- HTTP method (GET / POST / PUT / DELETE)
- Path
- Active status
- (Phase 2) Target URL for reverse proxy forwarding

This allows fine-grained access control per API.

---

## 📦 Subscription Plans Per Tenant

Tenants can configure multiple subscription tiers such as:

| Plan       | Description                             |
| ---------- | --------------------------------------- |
| FREE       | Limited access with strict usage limits |
| PRO        | Higher limits and expanded access       |
| ENTERPRISE | Maximum limits and full access          |

Each subscription plan defines:

- Monthly request quota
- Per-API rate limits
- Access permissions

Plans are configurable per tenant.

---

## ⏱ Per-API Per-Tier Rate Limiting

Rate limiting is enforced based on:

- API Key
- API Definition
- Subscription Plan

Example:

- FREE → 10 requests/minute on `/orders`
- PRO → 100 requests/minute on `/orders`

Each API can have different limits for different subscription tiers.

---

## 📊 Tenant-Level Monthly Quota

In addition to short-term rate limiting, each tenant has a monthly usage quota.

- Tracked per tenant
- Enforced per billing cycle
- Requests are blocked when quota is exceeded
- Simulates subscription-based billing enforcement

---

## 🔐 API Key Lifecycle

API keys:

- Are generated per tenant
- Are linked to a specific subscription plan
- Are securely stored (hashed)
- Must be provided via `X-API-KEY` header
- Can be activated or revoked

The API key represents the identity and subscription tier of the consuming client.

---

# ⚡ Infrastructure & Performance

## 🔴 Redis-Based Rate Limiting

Redis is used for:

- Per-API per-key request counters
- Tenant-level monthly usage counters
- Atomic increment operations
- High-performance traffic enforcement

Redis ensures concurrency safety and real-time limit enforcement.

---

## 🟠 RabbitMQ Async Logging

Every successful API request publishes a usage event.

RabbitMQ is used to:

- Process usage logs asynchronously
- Prevent blocking request threads
- Simulate event-driven backend systems

A consumer service persists usage logs for analytics processing.

---

# 📈 Analytics Endpoints

The platform provides analytics APIs for tenants, including:

- Total requests per tenant
- Requests per API
- Daily usage breakdown
- Remaining monthly quota
- Top APIs by usage

These endpoints simulate real-world SaaS usage dashboards.

---

# 🐳 Docker & AWS Deployment

## Dockerization

The full stack is containerized using Docker:

- Spring Boot Application
- MySQL
- Redis
- RabbitMQ

Docker Compose enables complete local execution.

---

## AWS Deployment

The application is deployed on AWS EC2.

Deployment includes:

- Docker-based service hosting
- Network and security configuration
- Production-style infrastructure setup

---

# 🔄 CI/CD Pipeline

GitHub Actions is used to implement CI/CD.

Pipeline includes:

- Automated build on push
- Test execution
- Docker image creation
- Optional image publishing
- Deployment automation

This simulates real-world DevOps workflow.

---

# 🚀 Phase 2 – Reverse Proxy Layer

After completing the core Level 2 platform, the system is extended into a lightweight reverse proxy layer.

In Phase 2:

- API requests are validated and rate-limited
- Requests are forwarded to the tenant’s backend service
- Downstream responses are returned transparently
- Usage logging remains asynchronous

This transforms the system into a minimal API Gateway simulation.

---

## 🛠 Tech Stack

- Java 17
- Spring Boot
- Spring Security
- MySQL
- Redis
- RabbitMQ
- Docker
- AWS EC2
- GitHub Actions

---

## 🎯 Architectural Focus

This project emphasizes:

- Clean layered backend architecture
- Multi-tenant system modeling
- Subscription-driven traffic governance
- Infrastructure-level API enforcement
- Event-driven design patterns
- Cloud deployment practices

---

## 📌 Note

This project is intentionally backend-focused.
No frontend or UI components are included.

---
