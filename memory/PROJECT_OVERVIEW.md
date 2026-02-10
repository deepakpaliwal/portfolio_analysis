# Project Overview

## What is this?
A full-stack financial portfolio analysis platform with subscription-based feature access.

## Modules

| Module | Tech | Port | Purpose |
|---|---|---|---|
| `portfolio-api` | Spring Boot 3.2.3, Java 21 | 8080 | REST API backend |
| `portfolio-ui` | React 18, TypeScript 5, Vite 5 | 5173 (dev) / 80 (prod) | Frontend SPA |
| `portfolio-batch` | Spring Batch + Apache Spark 3.5 | 8082 | Async analytics processing |
| `portfolio-db` | Liquibase 4.25 | N/A | Shared DB migration library |

## Core Features
- Multi-asset portfolio management (stocks, bonds, crypto, ETFs)
- Risk analytics: VaR, Monte Carlo, correlation matrices, stress tests
- Automated trading strategies and backtesting (10 built-in strategies)
- Stock/asset screener with customizable filters
- Real-time price monitoring and alerts (email, SMS, price-based)
- Role-based access: VIEWER, TRADER, ADMIN
- Subscription tiers: Free, Pro, Premium (24 feature toggles)

## Database
- **Local:** H2 embedded (file-based)
- **Dev/CAT/Prod:** PostgreSQL 16
- **Migrations:** Liquibase changelogs in `portfolio-db`
- **14 tables**, 5 seed data scripts

## Authentication
- JWT tokens (jjwt 0.12.5), 3600s expiry
- BCrypt password hashing (cost factor 12)
- OAuth 2.0 support in architecture

## Sample Users (password: `Password123!`)
- `admin@portfolio-analysis.com` — ADMIN, Premium
- `trader@example.com` — TRADER, Premium
- `viewer@example.com` — VIEWER, Free
- `pro.user@example.com` — TRADER, Pro

## Key API Endpoints
- `POST /api/v1/auth/login` — Login
- `POST /api/v1/auth/register` — Register
- `GET/POST /api/v1/portfolios` — List / Create portfolios
- `GET/PUT/DELETE /api/v1/portfolios/{id}` — Portfolio CRUD
- `GET /actuator/health` — Health check
- `/swagger-ui.html` — API docs

## Deployment Options
1. **Local** — IntelliJ + H2
2. **Remote server** — SSH/SCP + systemd + nginx
3. **Raspberry Pi cluster** — Spark standalone, optimized memory
4. **AWS** — ECS Fargate + RDS + ALB via CloudFormation
