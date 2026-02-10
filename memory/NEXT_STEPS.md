# Next Steps & Planned Work

## Requirements Implementation Progress (from REQUIREMENTS.md)
- [x] FR-UM-001 — Self-registration with email/password (Session 2)
- [x] FR-PM-001 — Create one or more named portfolios (Session 3)
- [x] FR-PM-002 — Add/update/remove holdings (Session 4)
- [x] FR-PM-003 — Support all asset classes (Session 4, enum already existed)
- [x] FR-PM-004 — Holdings record type/ticker/qty/price/date/currency (Session 4)
- [x] FR-PM-007 — Tag/group by custom categories (Session 4)
- [x] FR-PM-009 — CSV import for holdings (Session 4)
- [x] FR-PM-010 — Transaction history: buys, sells, dividends, splits (Session 4)
- [x] FR-PM-012 — Allocation breakdown by asset class, sector, currency (Session 4)
- [ ] FR-PM-005 — Real-time market value computation (needs external API)
- [ ] FR-PM-006 — Unrealized gain/loss display (depends on FR-PM-005)
- [ ] FR-PM-008 — Multi-currency FX conversion (needs FX rate source)
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
- [x] Implement remaining CRUD for holdings, transactions, trade orders
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
