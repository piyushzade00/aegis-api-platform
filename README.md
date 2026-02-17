Multi-Tenant API Management Platform

A backend-only Spring Boot application that demonstrates multi-tenant API management, subscription-based access control, per-API rate limiting, and usage analytics.

The system models how modern SaaS infrastructure platforms manage API keys, enforce traffic control policies, and monitor usage across multiple tenant applications.

📌 Project Overview

This project implements a multi-tenant API access management platform where SaaS companies can define and protect their APIs using configurable subscription plans and rate limiting policies.

It focuses on backend system design, policy enforcement, traffic control, and infrastructure-level architecture rather than frontend functionality.

Key goals:

Support multiple SaaS tenants

Allow tenants to define and protect their APIs

Enforce per-API per-subscription rate limits

Track usage and enforce tenant-level quotas

Implement Redis-based rate limiting

Process usage logs asynchronously using RabbitMQ

Deploy using Docker and AWS

Automate build and deployment using CI/CD

🧠 Core Concepts Implemented

Multi-Tenant System Design

API Key Authentication & Lifecycle Management

Subscription Plan Configuration per Tenant

Per-API Per-Tier Rate Limiting

Tenant-Level Monthly Quota Enforcement

Redis-Based Atomic Counters

Event-Driven Usage Logging

Backend-First Infrastructure Design

Reverse Proxy Simulation (Phase 2)

🏢 Multi-Tenant Support

The platform supports multiple SaaS tenants.

Each tenant can:

Register in the system

Define their own APIs

Create multiple subscription plans

Generate API keys for their customers

Monitor usage and analytics independently

All data and policies are scoped per tenant to ensure isolation.

🔌 API Definitions Per Tenant

Each tenant can register API definitions they want to protect.

An API definition includes:

Logical identifier

HTTP method

Path

Active status

(Phase 2) Target URL for request forwarding

This enables fine-grained policy enforcement per API.

📦 Subscription Plans Per Tenant

Tenants can define subscription tiers such as:

FREE

PRO

ENTERPRISE

Each subscription plan specifies:

Monthly request quota

Per-API rate limits

Access permissions

Plans are fully configurable per tenant.

⏱ Per-API Per-Tier Rate Limiting

Rate limiting is enforced based on:

API Key

API Definition

Subscription Plan

Example:

FREE plan → 10 requests/minute for /orders

PRO plan → 100 requests/minute for /orders

Each API can have different limits for different subscription tiers.

📊 Tenant-Level Monthly Quota

In addition to short-term rate limits, each tenant has a monthly usage quota.

Tracked per tenant

Enforced per billing cycle

Requests are blocked when quota is exceeded

Used to simulate subscription enforcement

🔐 API Key Lifecycle

API keys are:

Generated per tenant

Associated with a specific subscription plan

Securely stored (hashed)

Required in the X-API-KEY header

Can be activated or revoked

The API key represents the identity and plan of the consuming client.

⚡ Infrastructure & Performance
🔴 Redis-Based Rate Limiting

Redis is used for:

Per-API per-key request counters

Tenant-level monthly usage counters

High-performance atomic increment operations

Temporary window-based rate limiting

Redis ensures:

Concurrency safety

High throughput

Accurate real-time enforcement

🟠 RabbitMQ Async Logging

Every successful API request publishes a usage event.

RabbitMQ is used to:

Process usage logs asynchronously

Avoid blocking request threads

Simulate event-driven backend architecture

A consumer stores usage records for analytics and reporting.

📈 Analytics Endpoints

The platform exposes analytics APIs for tenants, including:

Total requests per tenant

Requests per API

Daily usage breakdown

Remaining monthly quota

Top APIs by traffic

These endpoints simulate real-world SaaS usage dashboards.

🐳 Docker & AWS Deployment
Dockerization

The entire system is containerized:

Spring Boot Application

MySQL

Redis

RabbitMQ

Docker Compose enables full-stack local execution.

AWS Deployment

The application is deployed on AWS EC2.

Deployment includes:

Docker-based service hosting

Network and security configuration

Production-like environment setup

🔄 CI/CD Pipeline

GitHub Actions is used to implement CI/CD.

Pipeline includes:

Automated build on push

Test execution

Docker image creation

Optional image publishing

Deployment automation

This simulates production-grade DevOps practices.

🚀 Phase 2 – Reverse Proxy Layer

After completing the core Level 2 platform, the system is extended with a lightweight reverse proxy layer.

In Phase 2:

Incoming API requests are validated

Rate limits and quotas are enforced

Requests are forwarded to the tenant’s backend service

Downstream responses are returned transparently

Usage logging remains asynchronous

This transforms the system into a minimal API Gateway simulation.

🛠 Tech Stack

Java 17

Spring Boot

Spring Security

MySQL

Redis

RabbitMQ

Docker

AWS EC2

GitHub Actions

🎯 Architectural Focus

This project emphasizes:

Multi-tenant backend architecture

Policy-driven API enforcement

Subscription-based traffic governance

Infrastructure-oriented backend design

Event-driven processing

Cloud deployment practices

No frontend components are included.
The platform is designed purely as a backend API management layer.
