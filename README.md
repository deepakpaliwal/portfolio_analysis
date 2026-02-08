# Portfolio Analysis Application

A full-stack financial portfolio analysis platform built with **Spring Boot**, **React**, and **Apache Spark**. Manage multi-asset portfolios, run risk analytics, screen stocks, execute automated trading strategies, and deploy anywhere from your laptop to AWS.

## Table of Contents

- [Architecture](#architecture)
- [Modules](#modules)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Clone the Repository](#1-clone-the-repository)
  - [Build the Project](#2-build-the-project)
  - [Run Locally with IntelliJ](#3-run-locally-with-intellij)
  - [Run with Scripts](#4-run-with-scripts-maclinux)
  - [Run with Docker](#5-run-with-docker)
- [Default Sample Users](#default-sample-users)
- [Sample Data](#sample-data)
  - [Sample Portfolios](#sample-portfolios)
  - [Sample Holdings](#sample-holdings)
  - [Sample Market Data](#sample-market-data)
  - [Pre-loaded Strategies](#pre-loaded-strategies)
  - [Feature Toggles](#feature-toggles)
- [Application Profiles](#application-profiles)
- [API Documentation](#api-documentation)
- [Scripts Reference](#scripts-reference)
- [Deployment](#deployment)
  - [Deploy to a Remote Server](#deploy-to-a-remote-server)
  - [Deploy to Raspberry Pi Cluster](#deploy-to-raspberry-pi-cluster)
  - [Deploy to AWS](#deploy-to-aws)
- [Database Migrations (Liquibase)](#database-migrations-liquibase)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [License](#license)

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                       portfolio-ui (React / Vite)                        │
│  Dashboard | Portfolio | Screener | Risk Analytics | Trading | Admin     │
└──────────────────────────┬───────────────────────────────────────────────┘
                           │  REST / WebSocket
┌──────────────────────────▼───────────────────────────────────────────────┐
│                  portfolio-api (Spring Boot REST API)                     │
│  Security | JPA | WebSocket | OpenAPI | JWT | Actuator | Prometheus      │
└──────────────┬───────────────────────────────────────────────────────────┘
               │
┌──────────────▼───────────────────────────────────────────────────────────┐
│           portfolio-db (Liquibase DDL/DML Migrations)                     │
│  14 DDL tables | 5 DML seed scripts | Versioned changelogs               │
└──────────────┬───────────────────────────────────────────────────────────┘
               │
       ┌───────▼───────┐
       │  H2 (local)   │     ┌──────────────────────────────────────────┐
       │  PostgreSQL    │     │  portfolio-batch (Spring Batch + Spark)  │
       │  (dev/cat/prod)│     │  Deep analysis | Recommendations | Jobs │
       └───────────────┘     └──────────────────────────────────────────┘
```

---

## Modules

| Module | Description | Port |
|---|---|---|
| **portfolio-api** | Spring Boot REST API backend with Security, JPA, WebSocket, OpenAPI | `8080` |
| **portfolio-ui** | React/TypeScript SPA with Redux, React Query, Recharts | `5173` (dev) / `80` (prod) |
| **portfolio-batch** | Spring Batch + Apache Spark batch processing for deep analytics | `8082` |
| **portfolio-db** | Liquibase DDL/DML migration module shared by API and Batch | N/A (library) |

---

## Prerequisites

| Tool | Version | Required For |
|---|---|---|
| **Java JDK** | 21+ | API, Batch, DB modules |
| **Apache Maven** | 3.9+ | Building Java modules |
| **Node.js** | 20+ | UI module |
| **npm** | 10+ | UI module |
| **Docker** (optional) | 24+ | Container-based deployment |
| **Docker Compose** (optional) | 2.20+ | Multi-container orchestration |
| **PostgreSQL** (optional) | 16+ | Dev/CAT/Prod environments (not needed for local) |

> **Note**: For local development, no external database is needed. The `local` profile uses an embedded H2 database.

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/deepakpaliwal/portfolio_analysis.git
cd portfolio_analysis
```

### 2. Build the Project

**Mac / Linux:**
```bash
./scripts/build.sh
```

**Windows:**
```cmd
scripts\build.bat
```

**Or build manually:**
```bash
# Build Java modules (API, Batch, DB)
mvn clean package -DskipTests

# Build React UI
cd portfolio-ui
npm install
npm run build
cd ..
```

### 3. Run Locally with IntelliJ

1. **Import the project** as a Maven project in IntelliJ IDEA
2. IntelliJ will detect the multi-module structure automatically
3. **Run portfolio-api**:
   - Open `portfolio-api/src/main/java/com/portfolio/api/PortfolioApiApplication.java`
   - Click the green play button or right-click and select "Run"
   - The API starts on `http://localhost:8080` with the `local` profile (H2 database)
4. **Run portfolio-ui**:
   - Open the Terminal in IntelliJ
   - Run: `cd portfolio-ui && npm install && npm run dev`
   - The UI starts on `http://localhost:5173`
5. **Run portfolio-batch** (optional):
   - Open `portfolio-batch/src/main/java/com/portfolio/batch/PortfolioBatchApplication.java`
   - Click the green play button
   - The batch service starts on `http://localhost:8082`
6. **Access H2 Console** (local profile only):
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/portfolio;MODE=PostgreSQL;AUTO_SERVER=TRUE`
   - Username: `sa` / Password: *(empty)*

### 4. Run with Scripts (Mac/Linux)

```bash
# Start all services in background
./scripts/start.sh all

# Start a single service
./scripts/start.sh api
./scripts/start.sh ui
./scripts/start.sh batch

# Stop all services
./scripts/stop.sh all

# Restart all services
./scripts/restart.sh all

# Use a specific profile
PROFILE=dev ./scripts/start.sh all
```

**Windows:**
```cmd
scripts\start.bat all
scripts\stop.bat all
scripts\restart.bat all

set PROFILE=dev
scripts\start.bat all
```

### 5. Run with Docker

```bash
# Local mode (H2 database, no PostgreSQL needed)
docker compose up --build

# Dev mode (with PostgreSQL)
docker compose --profile dev up --build

# Production mode
PROFILE=prod DB_PASSWORD=securepassword docker compose --profile prod up --build -d
```

Access the application:
- **UI**: http://localhost (port 80)
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

---

## Default Sample Users

The following users are pre-loaded in `local` and `dev` profiles via Liquibase seed data. All users share the same password.

> **Default Password for all sample users: `Password123!`**

| Email | First Name | Last Name | Role | Subscription | Description |
|---|---|---|---|---|---|
| `admin@portfolio-analysis.com` | System | Admin | **ADMIN** | Premium | Full admin access, manage users, feature toggles, batch jobs |
| `trader@example.com` | Jane | Trader | **TRADER** | Premium | Full trading access, 3 sample portfolios pre-loaded |
| `viewer@example.com` | John | Viewer | **VIEWER** | Free | Read-only access, 1 starter portfolio pre-loaded |
| `pro.user@example.com` | Alice | ProUser | **TRADER** | Pro | Intermediate tier access for testing Pro features |

### Roles

| Role | Permissions |
|---|---|
| **VIEWER** | View portfolios, basic screener, basic risk metrics |
| **TRADER** | Everything in VIEWER + create/edit portfolios, execute trades, use strategies |
| **ADMIN** | Everything in TRADER + Admin Panel, user management, feature toggles, batch job control |

---

## Sample Data

### Sample Portfolios

4 portfolios are pre-loaded for the sample users:

| Portfolio | Owner | Description | Holdings |
|---|---|---|---|
| **Tech Growth Portfolio** | Jane Trader | High-growth technology stocks (AI, cloud) | 5 stocks |
| **Dividend Income Portfolio** | Jane Trader | Conservative blue chips for income | 3 stocks + 1 bond + cash |
| **Crypto Portfolio** | Jane Trader | Diversified cryptocurrency holdings | 3 cryptos |
| **Starter Portfolio** | John Viewer | Beginner diversified ETFs | 3 ETFs |

### Sample Holdings

#### Tech Growth Portfolio
| Ticker | Name | Type | Qty | Purchase Price | Date | Sector |
|---|---|---|---|---|---|---|
| AAPL | Apple Inc. | Stock | 50 | $175.50 | 2024-01-15 | Technology |
| MSFT | Microsoft Corporation | Stock | 30 | $380.25 | 2024-02-01 | Technology |
| NVDA | NVIDIA Corporation | Stock | 25 | $620.00 | 2024-03-10 | Technology |
| GOOGL | Alphabet Inc. | Stock | 40 | $142.50 | 2024-01-20 | Technology |
| AMZN | Amazon.com Inc. | Stock | 20 | $178.25 | 2024-04-05 | Consumer Discretionary |

#### Dividend Income Portfolio
| Ticker | Name | Type | Qty | Purchase Price | Date | Sector |
|---|---|---|---|---|---|---|
| JNJ | Johnson & Johnson | Stock | 100 | $155.00 | 2023-06-15 | Healthcare |
| PG | Procter & Gamble | Stock | 80 | $148.50 | 2023-07-01 | Consumer Staples |
| KO | Coca-Cola Company | Stock | 150 | $58.75 | 2023-05-20 | Consumer Staples |
| US10Y | US Treasury 10-Year Note | Bond | 10 | $950.00 | 2023-09-01 | Fixed Income |
| USD | US Dollar Cash | Cash | 15,000 | $1.00 | 2024-01-01 | Cash |

#### Crypto Portfolio
| Ticker | Name | Type | Qty | Purchase Price | Date |
|---|---|---|---|---|---|
| BTC | Bitcoin | Crypto | 0.5 | $42,000.00 | 2024-01-10 |
| ETH | Ethereum | Crypto | 5.0 | $2,300.00 | 2024-02-15 |
| SOL | Solana | Crypto | 100 | $95.00 | 2024-03-01 |

#### Starter Portfolio (Free Tier)
| Ticker | Name | Type | Qty | Purchase Price | Date | Sector |
|---|---|---|---|---|---|---|
| SPY | SPDR S&P 500 ETF | ETF | 10 | $475.00 | 2024-01-05 | Broad Market |
| QQQ | Invesco QQQ Trust | ETF | 8 | $405.00 | 2024-02-10 | Technology |
| BND | Vanguard Total Bond Market | ETF | 50 | $72.50 | 2024-01-15 | Fixed Income |

### Sample Market Data

Latest price snapshots are pre-loaded for all holdings:

| Ticker | Type | Latest Price | Source |
|---|---|---|---|
| AAPL | Stock | $192.53 | Sample |
| MSFT | Stock | $425.18 | Sample |
| NVDA | Stock | $875.00 | Sample |
| GOOGL | Stock | $175.25 | Sample |
| AMZN | Stock | $197.50 | Sample |
| JNJ | Stock | $158.75 | Sample |
| PG | Stock | $162.30 | Sample |
| KO | Stock | $62.15 | Sample |
| SPY | ETF | $595.00 | Sample |
| QQQ | ETF | $510.25 | Sample |
| BND | ETF | $73.50 | Sample |
| BTC | Crypto | $97,500.00 | Sample |
| ETH | Crypto | $3,450.00 | Sample |
| SOL | Crypto | $185.00 | Sample |

Additionally, 8 days of historical AAPL data is included for testing time-series features.

### Pre-loaded Strategies

10 investment/trading strategies are seeded and ready to use:

| Strategy | Type | Description |
|---|---|---|
| Value Investing | VALUE | Screen undervalued stocks (P/E, P/B, dividend yield) |
| Growth Investing | GROWTH | High revenue/earnings growth companies |
| Dividend Income | DIVIDEND | High-yield, consistent dividend payers |
| Momentum Trading | MOMENTUM | Trend-following based on RSI and technical signals |
| Mean Reversion | MEAN_REVERSION | Buy oversold / sell overbought based on standard deviation |
| Covered Call Writing | COVERED_CALL | Income generation from covered call options |
| Pairs Trading | PAIRS | Long/short correlated pairs on divergence |
| Risk Parity | RISK_PARITY | Equal risk contribution allocation |
| Dollar-Cost Averaging | DCA | Systematic periodic investment |
| Crypto Yield Farming | YIELD_FARM | DeFi staking and liquidity pool strategies |

### Feature Toggles

24 feature toggles are pre-configured, controlling access by subscription tier:

| Feature | Free | Pro | Premium |
|---|---|---|---|
| Portfolio Management | Yes | Yes | Yes |
| Basic Screener | Yes | Yes | Yes |
| Advanced Screener | - | Yes | Yes |
| Basic Risk Metrics | Yes | Yes | Yes |
| Advanced Risk Analytics | - | - | Yes |
| VaR Calculation | - | - | Yes |
| Monte Carlo Simulation | - | - | Yes |
| Correlation Matrix | - | Yes | Yes |
| Strategy Engine | - | Yes | Yes |
| Strategy Backtesting | - | - | Yes |
| Automated Trading | - | - | Yes |
| Paper Trading | - | Yes | Yes |
| Real-Time Data | - | - | Yes |
| CSV Export | - | Yes | Yes |
| PDF Export | - | - | Yes |
| Email Alerts | Yes | Yes | Yes |
| SMS Alerts | - | Yes | Yes |
| Price Alerts | - | Yes | Yes |
| Crypto Trading | - | Yes | Yes |
| Options Greeks | - | Yes | Yes |
| Tax-Loss Harvesting | - | - | Yes |
| Admin Panel | - | - | Yes |

---

## Application Profiles

| Profile | Database | Use Case | Activate |
|---|---|---|---|
| **local** | H2 (file-based, PostgreSQL mode) | IntelliJ development on your laptop | `SPRING_PROFILES_ACTIVE=local` (default) |
| **dev** | PostgreSQL | Raspberry Pi cluster / team dev server | `SPRING_PROFILES_ACTIVE=dev` |
| **cat** | PostgreSQL | Pre-production / UAT testing | `SPRING_PROFILES_ACTIVE=cat` |
| **prod** | PostgreSQL | AWS production deployment | `SPRING_PROFILES_ACTIVE=prod` |

Switch profiles via:
```bash
# Environment variable
export SPRING_PROFILES_ACTIVE=dev

# Maven
mvn spring-boot:run -Pdev

# Docker
PROFILE=dev docker compose --profile dev up

# IntelliJ: Edit Run Configuration -> Environment variables -> SPRING_PROFILES_ACTIVE=dev
```

---

## API Documentation

When the API is running, interactive documentation is available at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Key API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Authenticate and receive JWT token |
| `POST` | `/api/v1/auth/register` | Register a new user |
| `GET` | `/api/v1/portfolios` | List all portfolios for the authenticated user |
| `POST` | `/api/v1/portfolios` | Create a new portfolio |
| `GET` | `/api/v1/portfolios/{id}` | Get portfolio details with holdings |
| `PUT` | `/api/v1/portfolios/{id}` | Update a portfolio |
| `DELETE` | `/api/v1/portfolios/{id}` | Delete a portfolio |
| `GET` | `/api/v1/health` | Application health check |
| `GET` | `/actuator/health` | Spring Actuator health |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

---

## Scripts Reference

All scripts are located in the `scripts/` directory:

| Script | Platform | Description |
|---|---|---|
| `start.sh` | Mac / Linux | Start services: `./start.sh [api\|ui\|batch\|all]` |
| `stop.sh` | Mac / Linux | Stop services: `./stop.sh [api\|ui\|batch\|all]` |
| `restart.sh` | Mac / Linux | Restart services: `./restart.sh [api\|ui\|batch\|all]` |
| `build.sh` | Mac / Linux | Build all modules: `./build.sh [profile]` |
| `deploy-server.sh` | Mac / Linux | Deploy to remote server via SSH |
| `start.bat` | Windows | Start services: `start.bat [api\|ui\|batch\|all]` |
| `stop.bat` | Windows | Stop services: `stop.bat [api\|ui\|batch\|all]` |
| `restart.bat` | Windows | Restart services: `restart.bat [api\|ui\|batch\|all]` |
| `build.bat` | Windows | Build all modules: `build.bat [profile]` |

---

## Deployment

### Deploy to a Remote Server

Each module can be deployed to a **different physical server**:

```bash
# Deploy all modules to one server
./scripts/deploy-server.sh --host 192.168.1.100 --module all --profile dev

# Deploy API to a dedicated server
./scripts/deploy-server.sh --host api-server.local --module api --profile prod

# Deploy Batch to a different server
./scripts/deploy-server.sh --host batch-server.local --module batch --profile prod

# Deploy UI to yet another server
./scripts/deploy-server.sh --host web-server.local --module ui --profile prod
```

The deploy script handles: building, copying artifacts via SCP/rsync, creating systemd service files, and configuring nginx for the UI.

### Deploy to Raspberry Pi Cluster

Optimized for Raspberry Pi 4B (4GB+ RAM):

```bash
# Docker on the Pi
docker compose -f docker-compose.yml -f deploy/raspberry-pi/docker-compose.rpi.yml --profile dev up -d

# Or deploy via SSH
./scripts/deploy-server.sh --host pi-1.local --module all --profile dev
```

See [`deploy/raspberry-pi/README.md`](deploy/raspberry-pi/README.md) for detailed cluster setup.

### Deploy to AWS

ECS Fargate + RDS PostgreSQL + ALB:

```bash
# Deploy via CloudFormation
aws cloudformation deploy \
    --template-file deploy/aws/cloudformation.yml \
    --stack-name portfolio-analysis \
    --parameter-overrides Environment=prod DBPassword=<password> \
    --capabilities CAPABILITY_IAM
```

See [`deploy/aws/README.md`](deploy/aws/README.md) for full AWS setup including ECR image push, Secrets Manager configuration, and cost estimates.

---

## Database Migrations (Liquibase)

All schema and data changes are managed through Liquibase in the `portfolio-db` module:

```
portfolio-db/src/main/resources/db/changelog/
├── db.changelog-master.xml          # Master changelog (includes all below)
├── ddl/                             # Schema changes (14 tables)
│   ├── 001-create-users-table.sql
│   ├── 002-create-portfolios-table.sql
│   ├── 003-create-holdings-table.sql
│   ├── 004-create-transactions-table.sql
│   ├── 005-create-market-data-table.sql
│   ├── 006-create-trade-orders-table.sql
│   ├── 007-create-strategies-table.sql
│   ├── 008-create-broker-accounts-table.sql
│   ├── 009-create-subscriptions-table.sql
│   ├── 010-create-alert-rules-table.sql
│   ├── 011-create-batch-jobs-table.sql
│   ├── 012-create-screener-reports-table.sql
│   ├── 013-create-admin-audit-log-table.sql
│   └── 014-create-feature-toggles-table.sql
└── dml/                             # Seed data
    ├── 001-seed-default-strategies.sql
    ├── 002-seed-feature-toggles.sql
    ├── 003-seed-sample-users.sql        (local, dev only)
    ├── 004-seed-sample-portfolios.sql   (local, dev only)
    └── 005-seed-sample-market-data.sql  (local, dev only)
```

Migrations run automatically on application startup. To run manually:

```bash
cd portfolio-db
mvn liquibase:update -Plocal
```

---

## Project Structure

```
portfolio-analysis/
├── pom.xml                          # Parent POM (Spring Boot 3.2.3, Java 21)
├── portfolio-api/                   # Spring Boot REST API
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/portfolio/api/
│       │   ├── config/              # Security, WebSocket, OpenAPI
│       │   ├── controller/          # REST controllers
│       │   ├── dto/                 # Request/Response DTOs
│       │   ├── exception/           # Global exception handler
│       │   ├── model/               # JPA entities and enums
│       │   ├── repository/          # Spring Data JPA repositories
│       │   └── service/             # Business logic services
│       └── resources/
│           └── application.yml      # Multi-profile config
├── portfolio-ui/                    # React/TypeScript SPA
│   ├── package.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── api/                     # Axios HTTP client
│       ├── components/              # Shared UI components
│       ├── pages/                   # Route pages
│       └── store/                   # Redux store and slices
├── portfolio-batch/                 # Spring Batch + Apache Spark
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/portfolio/batch/
│       ├── config/                  # Spark and Batch configuration
│       └── job/                     # Batch job schedulers
├── portfolio-db/                    # Liquibase migrations
│   ├── pom.xml
│   └── src/main/resources/db/changelog/
│       ├── ddl/                     # 14 DDL table creation scripts
│       └── dml/                     # 5 DML seed data scripts
├── scripts/                         # Start/stop/build/deploy scripts
│   ├── start.sh / start.bat
│   ├── stop.sh / stop.bat
│   ├── restart.sh / restart.bat
│   ├── build.sh / build.bat
│   └── deploy-server.sh
├── deploy/
│   ├── aws/                         # CloudFormation + env config
│   └── raspberry-pi/                # Docker Compose override + env
├── docker-compose.yml               # Full-stack Docker orchestration
├── .env.example                     # Environment variable template
└── REQUIREMENTS.md                  # Full requirements document
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.2.3, Java 21, Spring Security, Spring Data JPA |
| **Frontend** | React 18, TypeScript, Redux Toolkit, React Query, Recharts, Vite |
| **Batch** | Spring Batch, Apache Spark 3.5 (Scala 2.13) |
| **Database** | PostgreSQL 16 (prod), H2 (local) |
| **Migrations** | Liquibase 4.25 |
| **API Docs** | SpringDoc OpenAPI 2.3 (Swagger UI) |
| **Auth** | JWT (jjwt 0.12.5), OAuth 2.0, BCrypt (cost 12) |
| **Observability** | Spring Actuator, Micrometer, Prometheus |
| **Containerization** | Docker (multi-stage, multi-arch), Docker Compose |
| **Cloud** | AWS (ECS Fargate, RDS, ALB, CloudFormation) |
| **Testing** | JUnit 5, Mockito, Spring Boot Test, Testcontainers |
| **Build** | Maven 3.9, npm, Vite |

---

## License

Apache 2.0 - See [REQUIREMENTS.md](REQUIREMENTS.md) for details.
