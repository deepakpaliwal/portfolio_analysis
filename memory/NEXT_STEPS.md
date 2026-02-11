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
- [x] FR-PM-005 — Real-time market value via Finnhub API (Session 5)
- [x] FR-PM-006 — Unrealized gain/loss display (Session 5)
- [x] FR-PM-008 — Multi-currency FX conversion via Finnhub forex rates (Session 5)
- [x] FR-SC-001 through FR-SC-010 — Stock & Sector Screener (Session 6)
- [x] FR-RA-001 through FR-RA-009 — Risk Analytics (Session 7)
- [x] FR-RA-011 — Risk dashboard (Session 7)
- [x] FR-RA-012 — On-demand recalculation (Session 7)
- [x] Batch Price Module — CSV storage, watermarking, scheduler, management UI (Session 8)
- [ ] FR-UM-002 — OAuth 2.0 login via Google
- [ ] FR-UM-002a — OAuth 2.0 login via Meta
- [ ] FR-UM-003 — Email verification before full access
- [ ] FR-UM-008 — Multi-factor authentication (TOTP)

## Pending Sections (by priority)

### High Priority — Next to implement
1. **4.6 Correlation & Hedging Analysis** — Correlation matrix, heatmap, hedge suggestions (8 requirements)
2. **4.7 Strategy Engine** — Strategy suggestions, backtesting, trade signals (7 requirements)
3. **4.9 Reporting & Dashboards** — Main dashboard, performance charts, WebSocket updates (7 requirements)
4. **4.10 Admin Panel** — User management, system config, feature toggles (12 requirements)

### Medium Priority
5. **4.8 Automated Trading** — Broker integration, auto-execution, paper trading (12 requirements)
6. **4.11 Freemium & Subscription** — Stripe/PayPal, tiers, billing (12 requirements)
7. **4.12 Alerting & Notifications** — Email, SMS, in-app alerts (12 requirements)
8. **4.3 Asset Management** — Bonds, options, crypto, real estate (remaining sub-sections)

### Lower Priority
9. **4.13 Log Search & Management** — ELK/OpenSearch (10 requirements)
10. **4.14 Batch Processing & Deep Analysis** — Spring Batch, recommendation reports (12 requirements)

## Potential areas of work
These are observed opportunities — not commitments. Future sessions should confirm with the user before starting any of these.

### Testing
- [ ] Add unit tests for `PortfolioService` and `PortfolioController`
- [ ] Add integration tests using Testcontainers (dependency already in POM)
- [ ] Add frontend tests (React Testing Library / Vitest)

### API Enhancements
- [x] Implement remaining CRUD for holdings, transactions, trade orders
- [x] Add market data fetching from Finnhub API (Session 5)
- [ ] Implement strategy backtesting endpoints
- [x] Add screener endpoint with filtering logic (Session 6 — full section 4.4)
- [ ] WebSocket implementation for real-time price updates

### UI Enhancements
- [ ] Build out Dashboard page with real portfolio metrics and charts
- [x] Implement Screener page filtering and results (Session 6)
- [x] Build Risk analytics page (Session 7)
- [x] Build Batch Management page (Session 8)
- [ ] Build Trading page (order entry form, order book)
- [ ] Admin panel — user management, feature toggle controls

### DevOps
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Automated database backup strategy
- [ ] Monitoring and alerting setup

## Known Issues
- Finnhub free tier: `/stock/candle` returns 403 (paid only)
- Yahoo Finance: Returns 429 without proper User-Agent header (solved by batch module)
- Entity lazy loading: Always use `@Transactional` when accessing Portfolio.user or Portfolio.holdings
- User entity uses `getEmail()` NOT `getUsername()`
