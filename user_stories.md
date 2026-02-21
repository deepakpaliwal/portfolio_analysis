# User Stories (derived from `feature_list.md` and `REQUIREMENTS.md`)

Status legend:
- ✅ **Implemented** = available in current codebase/UI/API
- 🟡 **Partially Implemented** = basic support exists, but requirement scope is incomplete
- ⬜ **Planned** = not yet implemented

---

## EP-01 — User Identity, Security & Access Control

### US-01-001 — Email/password registration and login (FR-UM-001)
**Status:** ✅ Implemented  
**Story:** As a new user, I want to register with email/password and log in so that I can access my portfolio workspace.

**Acceptance Criteria**
- Given a valid registration payload, when I submit `/auth/register`, then a user account is created and auth token is returned.
- Given valid credentials, when I submit `/auth/login`, then I receive a JWT and user profile details.

### US-01-002 — Role-based access (FR-UM-004)
**Status:** ✅ Implemented  
**Story:** As a platform operator, I want role-based permissions (VIEWER/TRADER/ADMIN) so that sensitive actions are restricted.

**Acceptance Criteria**
- Given users with different roles, when authorization rules are evaluated, then role restrictions are enforced by backend security config.
- Given a protected UI route, when an unauthenticated user accesses it, then they are redirected to login.

### US-01-003 — Social login + account linking (FR-UM-002/002a/002b/009/010)
**Status:** ⬜ Planned  
**Story:** As a user, I want Google/Meta login and optional account linking so that I can use federated identity.

**Acceptance Criteria**
- User can authenticate with Google and Meta OAuth providers.
- Existing local accounts can be linked/unlinked with social identities.

### US-01-004 — MFA and email verification (FR-UM-003/008)
**Status:** ⬜ Planned  
**Story:** As a security-conscious user, I want email verification and TOTP MFA so that account takeover risk is reduced.

**Acceptance Criteria**
- Unverified users have limited access until email verification is completed.
- MFA challenge is enforced at login for users who enable TOTP.

---

## EP-02 — Portfolio Lifecycle Management

### US-02-001 — Portfolio CRUD (FR-PM-001)
**Status:** ✅ Implemented  
**Story:** As an investor, I want to create, view, update, and delete named portfolios so I can organize my investments.

**Acceptance Criteria**
- API supports create/list/get/update/delete operations for portfolios.
- UI allows creating and selecting portfolios from a portfolio list.

### US-02-002 — Holdings CRUD across portfolios (FR-PM-002/004)
**Status:** ✅ Implemented  
**Story:** As an investor, I want to add, edit, and remove holdings with core metadata so that portfolio positions stay accurate.

**Acceptance Criteria**
- Holding create/update/delete endpoints exist under portfolio scope.
- Holding records capture asset type, ticker, quantity, purchase price/date, currency, sector/category.

### US-02-003 — Allocation and valuation views (FR-PM-005/006/012)
**Status:** ✅ Implemented  
**Story:** As an investor, I want allocation and valuation insights so I can track portfolio performance and concentration risk.

**Acceptance Criteria**
- System computes market value, unrealized P/L, and portfolio totals.
- Allocation breakdowns are available by asset type, sector, and currency.

### US-02-004 — CSV import and transaction history (FR-PM-009/010)
**Status:** 🟡 Partially Implemented  
**Story:** As an investor, I want to import holdings and review transactions so portfolio records are easy to maintain.

**Acceptance Criteria**
- Holdings CSV import endpoint and UI flow are available.
- Portfolio/holding transaction history APIs exist.
- Brokerage sync import is not yet available.

### US-02-005 — Portfolio cloning and what-if analysis (FR-PM-011)
**Status:** ⬜ Planned  
**Story:** As an investor, I want to clone a portfolio for scenario testing so I can compare strategies safely.

**Acceptance Criteria**
- User can duplicate an existing portfolio with holdings.
- Cloned portfolio has independent lifecycle and audit trail.

---

## EP-03 — Multi-Asset Coverage

### US-03-001 — Core multi-asset support (FR-PM-003, FR-AS-001, FR-CR-001)
**Status:** ✅ Implemented  
**Story:** As an investor, I want to manage multiple asset classes (stocks, bonds, options, cash, real estate, retirement funds, crypto) in one portfolio.

**Acceptance Criteria**
- Holding model and UI support enumerated asset classes.
- User can create holdings for supported asset types.

### US-03-002 — Real-time/historical market pricing (FR-AS-002, FR-CR-002)
**Status:** 🟡 Partially Implemented  
**Story:** As an investor, I want market prices to update valuation so that P/L reflects current data.

**Acceptance Criteria**
- System provides market data fetch and stock price history sync endpoints.
- Portfolio valuation consumes latest market prices.
- Full crypto exchange aggregation remains pending.

### US-03-003 — Asset-specific depth (bonds/options/real estate/retirement/cash) (FR-BD-*, FR-OP-*, FR-RE-*, FR-RT-*, FR-CA-*)
**Status:** ⬜ Planned  
**Story:** As an investor, I want each asset class to include domain-specific fields and calculations.

**Acceptance Criteria**
- Bonds capture coupon, maturity, YTM, ratings.
- Options include Greeks, expiries, and multi-leg strategy support.
- Real estate and retirement modules include dedicated calculations.

---

## EP-04 — Screening, Research & Technical Analysis

### US-04-001 — Ticker screener report (FR-SC-001/002)
**Status:** ✅ Implemented  
**Story:** As a trader, I want a ticker screener report with key fundamentals and market stats so I can evaluate opportunities quickly.

**Acceptance Criteria**
- API returns ticker report for requested symbol.
- UI shows key metrics (price, 52W range, P/E, EPS, dividend yield, etc.).

### US-04-002 — Sector screener analytics (FR-SC-006/007)
**Status:** ✅ Implemented  
**Story:** As a trader, I want sector-level performance insights so I can identify rotation and relative strength.

**Acceptance Criteria**
- API exposes sector listing/report endpoints.
- UI presents sector performance summary and leaders/laggards.

### US-04-003 — Custom screen criteria and saved reports (FR-SC-008+, screener report management)
**Status:** ✅ Implemented  
**Story:** As a trader, I want to run custom screens and save reports so I can track recurring opportunities.

**Acceptance Criteria**
- API supports criteria-based screening and screener report CRUD.
- UI allows saving and loading ticker/sector/custom reports.

### US-04-004 — Deep research: statements, SEC filings, analyst consensus (FR-SC-003/004/005)
**Status:** 🟡 Partially Implemented  
**Story:** As a researcher, I want financial statements, filing summaries, and analyst views in one place.

**Acceptance Criteria**
- Ticker response supports sections for financials/filings/analyst data in UI.
- Multi-year completeness and broad provider depth are still pending.

---

## EP-05 — Risk, Correlation & Portfolio Diagnostics

### US-05-001 — Portfolio risk analytics (FR-RA-*)
**Status:** ✅ Implemented  
**Story:** As a risk-aware investor, I want VaR, volatility, drawdown, and Sharpe-like risk outputs so I can monitor downside exposure.

**Acceptance Criteria**
- Risk endpoint returns portfolio risk metrics.
- Risk analytics UI renders key indicators and trends.

### US-05-002 — Correlation and hedging analysis (FR-CH-*)
**Status:** ✅ Implemented  
**Story:** As a portfolio manager, I want cross-holding correlation analysis so I can improve diversification.

**Acceptance Criteria**
- Correlation endpoint computes portfolio-level matrix/summary.
- Correlation UI page displays actionable diversification insights.

---

## EP-06 — Strategy Engine & Automated Trading

### US-06-001 — Strategy recommendations and backtesting (FR-SE-001..007)
**Status:** ⬜ Planned  
**Story:** As an active investor, I want strategy recommendations and backtesting so I can choose evidence-based tactics.

**Acceptance Criteria**
- Predefined and customizable strategies are configurable.
- Historical backtest outputs include CAGR, max drawdown, Sharpe, win rate.

### US-06-002 — Automated execution with risk guardrails (FR-AT-001..012)
**Status:** ⬜ Planned  
**Story:** As a trader, I want automated order execution with strict risk limits and kill switch so I can scale systematically.

**Acceptance Criteria**
- Brokerage/exchange integrations support live and paper trading.
- OMS supports market/limit/stop/trailing stop and trade audit logs.
- Risk limit enforcement blocks non-compliant orders.

### US-06-003 — Advanced OMS from feature spec (trailing stop, OCO, iceberg, VWAP)
**Status:** ⬜ Planned  
**Story:** As a professional trader, I want advanced order types so execution quality and control improve.

**Acceptance Criteria**
- OMS supports OCO, iceberg, and VWAP execution workflows.
- Execution reports include slippage and fill-quality diagnostics.

---

## EP-07 — Dashboards, Reports & Real-Time Experience

### US-07-001 — Main dashboard and portfolio overview (FR-RD-001/002/003)
**Status:** ✅ Implemented  
**Story:** As a user, I want a dashboard with summary KPIs and benchmark-aware performance views so I can understand account health at a glance.

**Acceptance Criteria**
- Dashboard route exists in UI and loads core portfolio KPIs.
- Date range and benchmark comparison controls are available for core charts.

### US-07-002 — Scheduled/exportable reporting and trade journal (FR-RD-004/005/006)
**Status:** ⬜ Planned  
**Story:** As a user, I want periodic exports and trade journal tooling so I can perform audit and tax workflows.

**Acceptance Criteria**
- Reports can be generated on schedule and exported (PDF/CSV/Excel).
- Trade journal includes filters and annotations.

### US-07-003 — Real-time updates via WebSocket (FR-RD-007)
**Status:** 🟡 Partially Implemented  
**Story:** As a user, I want push updates so portfolio values refresh without manual reload.

**Acceptance Criteria**
- Backend WebSocket configuration is present.
- End-to-end UI subscription for live valuation updates is not fully wired.

---

## EP-08 — Admin Operations, Feature Flags & Batch Control

### US-08-001 — Admin panel foundation (FR-AP-001)
**Status:** 🟡 Partially Implemented  
**Story:** As an admin, I want a dedicated administration console so I can manage platform operations.

**Acceptance Criteria**
- Admin route/page exists in UI for privileged operations.
- Current page is mostly placeholder; deeper workflows pending.

### US-08-002 — Feature toggle and system configuration management (FR-AP-003/004)
**Status:** ⬜ Planned  
**Story:** As an admin, I want feature toggle controls and system configuration management.

**Acceptance Criteria**
- Admin UI can view/edit toggle states by tier/environment.
- All changes are audited with actor/timestamp.

### US-08-003 — Batch ticker/schedule controls (FR-AP-010, FR-BP-006/011)
**Status:** ✅ Implemented  
**Story:** As an admin, I want to configure batch ticker jobs and schedules so deep analysis runs automatically.

**Acceptance Criteria**
- API supports ticker config CRUD and schedule get/update.
- API supports triggering batch runs and tracking execution metadata.

### US-08-004 — Audit log and system health dashboard (FR-AP-006/008/012)
**Status:** 🟡 Partially Implemented  
**Story:** As an admin, I want health and audit visibility so I can operate reliably and compliantly.

**Acceptance Criteria**
- Health endpoint exists.
- Full admin-facing audit log UI/search workflow remains pending.

---

## EP-09 — Subscription, Monetization & Entitlements

### US-09-001 — Tiered subscriptions and entitlements (FR-FM-001..012)
**Status:** 🟡 Partially Implemented  
**Story:** As a business owner, I want freemium/premium (and optional pro) entitlements so monetization is enforceable.

**Acceptance Criteria**
- Subscription tier is modeled on users and feature-toggle seed data exists.
- Payment integration, billing lifecycle, coupons, and upgrade UX are pending.

---

## EP-10 — Alerting, Logging, Compliance & Resilience

### US-10-001 — Alerts and notification center (FR-AN-001..012)
**Status:** ⬜ Planned  
**Story:** As a user, I want configurable alerts (email/SMS/in-app) for market and portfolio events.

**Acceptance Criteria**
- Alert rules support create/update/delete and per-channel preferences.
- Delivery history, throttling, and provider integrations are implemented.

### US-10-002 — Centralized log search (FR-LS-001..010)
**Status:** ⬜ Planned  
**Story:** As an admin, I want centralized queryable logs so I can troubleshoot and audit production behavior.

**Acceptance Criteria**
- Logs are aggregated with correlation IDs and retention controls.
- ADMIN UI provides full-text and faceted log search.

### US-10-003 — Security/compliance hardening from feature spec (MFA, KYC/AML, encryption, HA)
**Status:** ⬜ Planned  
**Story:** As a regulated platform operator, I want enterprise-grade security and compliance controls.

**Acceptance Criteria**
- MFA, KYC/AML workflows, encryption controls, and SAR hooks are available.
- Availability targets and multi-region failover are measured and documented.

---

## EP-11 — Batch Deep Analysis Microservice

### US-11-001 — Independent batch analysis service (FR-BP-001/002/003/004/012)
**Status:** ✅ Implemented  
**Story:** As a platform architect, I want batch analytics to run in a separate microservice so intensive workloads scale independently.

**Acceptance Criteria**
- Dedicated `portfolio-batch` module exists with Spring Batch/Spark setup.
- Batch jobs can be scheduled and invoked independently of real-time API traffic.

### US-11-002 — Advanced analytics outputs to user channels (FR-BP-005/009/010)
**Status:** 🟡 Partially Implemented  
**Story:** As a user, I want deep-analysis recommendations and correlation/regression outputs delivered to dashboard/notifications.

**Acceptance Criteria**
- Batch engine computes analytics artifacts.
- Delivery into user-facing dashboard/email channels is incomplete.

