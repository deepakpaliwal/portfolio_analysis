# Session Log

## Session 1 — 2026-02-10

### What was done
- Explored the full project structure and codebase
- Created this `memory/` folder to persist context across sessions
- Documented the project overview, architecture, and planned next steps

### Current state of the project
- All four modules exist: `portfolio-api`, `portfolio-ui`, `portfolio-batch`, `portfolio-db`
- The project builds and is configured for local (H2), dev, cat, and prod (PostgreSQL) profiles
- Docker Compose orchestration is in place
- JWT authentication flow is implemented and working
- Sample data (users, portfolios, strategies, feature toggles) is seeded via Liquibase
- Recent commits fixed BCrypt hashes for sample users and added JWT auth flow

### Branch info
- Working branch: `claude/add-memory-folder-AQYYQ`
- Base branch: `main`

### Key decisions made
- Memory folder lives at project root (`/memory/`) for easy access by future sessions
- Files are written in Markdown for readability
- Each session should append to this log so context accumulates
