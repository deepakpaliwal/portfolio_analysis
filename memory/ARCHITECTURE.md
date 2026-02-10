# Architecture & Technical Decisions

## Build System
- **Maven** multi-module project (parent POM at root)
- `portfolio-db` is a dependency of both `portfolio-api` and `portfolio-batch`
- Frontend uses **npm + Vite** (separate from Maven)

## Key File Locations

### API
- Config: `portfolio-api/src/main/resources/application.yml`
- Controllers: `portfolio-api/src/main/java/com/portfolio/api/controller/`
- Services: `portfolio-api/src/main/java/com/portfolio/api/service/`
- Security: `portfolio-api/src/main/java/com/portfolio/api/config/SecurityConfig.java`
- JWT: `portfolio-api/src/main/java/com/portfolio/api/security/JwtTokenProvider.java`

### UI
- Entry: `portfolio-ui/src/main.tsx`
- Pages: `portfolio-ui/src/pages/` (Dashboard, Portfolio, Screener, Risk, Trading, Admin, Login)
- Store: `portfolio-ui/src/store/` (Redux slices for auth + portfolio)
- API client: `portfolio-ui/src/services/api.ts`
- Routes: `portfolio-ui/src/App.tsx`

### Batch
- Config: `portfolio-batch/src/main/java/com/portfolio/batch/config/`
- Scheduler: runs daily at 2 AM (`0 0 2 * * *`)

### Database
- Changelogs: `portfolio-db/src/main/resources/db/changelog/`
- DDL: `changes/ddl/` (14 table creation scripts)
- DML: `changes/dml/` (5 seed data scripts)
- Master: `db.changelog-master.yaml`

## Environment Profiles
| Profile | DB | Pool Max | Notes |
|---|---|---|---|
| local | H2 file | N/A | Dev on laptop, H2 console enabled |
| dev | PostgreSQL | 10 | Raspberry Pi / team dev |
| cat | PostgreSQL | 15 | Pre-production UAT |
| prod | PostgreSQL | 30 | AWS, minimal logging |

## Security Architecture
- Stateless JWT auth — no server-side sessions
- `SecurityConfig` defines public endpoints (`/auth/**`, `/actuator/health`, `/swagger-ui/**`)
- All other endpoints require authentication
- `JwtAuthenticationFilter` extracts and validates tokens per request
- CORS allows `localhost:3000` and `localhost:5173`

## Docker
- `docker-compose.yml` at root orchestrates all services
- Multi-stage Dockerfiles for API and UI
- PostgreSQL 16 container for dev/test

## Observability
- Spring Actuator: `/actuator/health`, `/actuator/info`
- Micrometer + Prometheus: `/actuator/prometheus`
