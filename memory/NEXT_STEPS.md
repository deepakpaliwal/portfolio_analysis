# Next Steps & Planned Work

## Requirements Implementation Progress (from REQUIREMENTS.md)
- [x] FR-UM-001 — Self-registration with email/password (Session 2)
- [ ] FR-UM-002 — OAuth 2.0 login via Google
- [ ] FR-UM-002a — OAuth 2.0 login via Meta
- [ ] FR-UM-003 — Email verification before full access
- [ ] FR-UM-007 — Password complexity (enforced in RegisterRequest DTO)
- [ ] FR-UM-008 — Multi-factor authentication (TOTP)

## Potential areas of work
These are observed opportunities — not commitments. Future sessions should confirm with the user before starting any of these.

### Testing
- [ ] Add unit tests for `PortfolioService` and `PortfolioController`
- [ ] Add integration tests using Testcontainers (dependency already in POM)
- [ ] Add frontend tests (React Testing Library / Vitest)

### API Enhancements
- [ ] Implement remaining CRUD for holdings, transactions, trade orders
- [ ] Add market data fetching from an external API
- [ ] Implement strategy backtesting endpoints
- [ ] Add screener endpoint with filtering logic
- [ ] WebSocket implementation for real-time price updates

### UI Enhancements
- [ ] Build out Dashboard page with real portfolio metrics and charts
- [ ] Implement Screener page filtering and results
- [ ] Build Risk analytics page (VaR calculation display, Monte Carlo charts)
- [ ] Build Trading page (order entry form, order book)
- [ ] Admin panel — user management, feature toggle controls

### DevOps
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Automated database backup strategy
- [ ] Monitoring and alerting setup

### Batch Processing
- [ ] Implement Spark analytics jobs (correlation matrix, risk metrics)
- [ ] Connect batch results back to API for user consumption

## Known Issues
- None documented yet. Future sessions should add issues here as they are discovered.
