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

---

## Session 2 — 2026-02-10

### What was done
- Implemented **FR-UM-001**: User self-registration with email and password

### Changes made

#### Backend (`portfolio-api`)
| File | Action | Details |
|---|---|---|
| `dto/RegisterRequest.java` | **Created** | DTO with Bean Validation: email, password (min 12 chars, mixed case, digit, symbol), firstName, lastName |
| `controller/AuthController.java` | **Modified** | Added `POST /api/v1/auth/register` endpoint; injected `PasswordEncoder`; checks email uniqueness, hashes password with BCrypt(12), saves user, returns JWT + user DTO (HTTP 201) |

#### Frontend (`portfolio-ui`)
| File | Action | Details |
|---|---|---|
| `pages/Register.tsx` | **Created** | Registration form with client-side validation (password complexity, confirm match), server-side error display, auto-login on success |
| `pages/Login.tsx` | **Modified** | Added "Don't have an account? Sign up" link to `/register` |
| `App.tsx` | **Modified** | Added `/register` public route |

### API contract

**POST /api/v1/auth/register**
```json
Request:  { "email", "password", "firstName", "lastName" }
Success:  201 { "token", "tokenType", "user": { id, email, firstName, lastName, role } }
Conflict: 409 { "error": "An account with this email already exists" }
Invalid:  400 { "error": "Validation Failed", "fieldErrors": { ... } }
```

### Defaults for new users
- Role: `VIEWER`
- Subscription: `FREE`
- Email verified: `false`
- Auth provider: `LOCAL`

### Next requirement to implement
- FR-UM-002: OAuth 2.0 login via Google
- FR-UM-003: Email verification before full access

---

## Session 3 — 2026-02-10

### What was done
- Implemented **FR-PM-001**: Users can create one or more named portfolios

### Changes made

#### Backend (`portfolio-api`)
| File | Action | Details |
|---|---|---|
| `controller/PortfolioController.java` | **Modified** | Replaced `X-User-Id` header with JWT `Authentication` principal; added `UserRepository` to resolve email to user ID |

#### Frontend (`portfolio-ui`)
| File | Action | Details |
|---|---|---|
| `store/slices/portfolioSlice.ts` | **Modified** | Added `addPortfolio`, `removePortfolio` reducers; changed `selectPortfolio` to accept `null`; exported `Portfolio` type |
| `pages/Portfolio.tsx` | **Rewritten** | Full API integration: fetches portfolios on mount, create form (name, description, currency), portfolio selector dropdown, delete with confirm, holdings table for selected portfolio |

### Key fixes
- **PortfolioController auth**: Was using `@RequestHeader("X-User-Id")` which required the frontend to know and send the user's DB id. Changed to use Spring Security `Authentication` object so user identity comes from JWT token automatically.

### How it works (end-to-end)
1. User navigates to `/portfolio` (protected route)
2. `GET /api/v1/portfolios` fetches all portfolios for the JWT-authenticated user
3. User clicks "+ New Portfolio" → inline form appears (name required, description optional, currency selector)
4. `POST /api/v1/portfolios` creates the portfolio → added to Redux store → auto-selected
5. Selected portfolio shows details header + holdings table
6. Delete button calls `DELETE /api/v1/portfolios/{id}` with confirmation

---

## Session 4 — 2026-02-10

### What was done
Implemented all High + Medium requirements from **4.2 Portfolio Management**:
- **FR-PM-002**: Add/update/remove holdings (HoldingController + HoldingService)
- **FR-PM-003**: All asset classes supported (AssetType enum already had them)
- **FR-PM-004**: Holdings record all required fields (entity already had them)
- **FR-PM-007**: Custom category tagging (category field on Holding)
- **FR-PM-009**: CSV import for holdings
- **FR-PM-010**: Transaction history (TransactionController + TransactionService)
- **FR-PM-012**: Allocation breakdown by asset type, sector, currency

### New backend files created
| File | Purpose |
|---|---|
| `service/HoldingService.java` | CRUD for holdings within a portfolio |
| `controller/HoldingController.java` | REST endpoints: `POST/GET/PUT/DELETE /api/v1/portfolios/{id}/holdings` |
| `dto/TransactionRequest.java` | Transaction creation DTO with validation |
| `dto/TransactionResponse.java` | Transaction response DTO (includes holding ticker) |
| `service/TransactionService.java` | Record + query transactions by holding or portfolio |
| `controller/TransactionController.java` | REST endpoints for transactions |
| `dto/AllocationResponse.java` | Allocation breakdown response DTO |

### Modified backend files
| File | Changes |
|---|---|
| `service/PortfolioService.java` | Added `getAllocation()` and `importHoldingsFromCsv()` methods |
| `controller/PortfolioController.java` | Added `GET /{id}/allocation` and `POST /{id}/holdings/import` endpoints |

### Frontend changes
| File | Changes |
|---|---|
| `pages/Portfolio.tsx` | Full rewrite with 3 tabs (Holdings, Transactions, Allocation), add/edit/delete holding form, CSV import button, allocation bar charts |

### API endpoints added
```
Holdings:
  POST   /api/v1/portfolios/{id}/holdings          — Add holding
  GET    /api/v1/portfolios/{id}/holdings           — List holdings
  PUT    /api/v1/portfolios/{id}/holdings/{hid}     — Update holding
  DELETE /api/v1/portfolios/{id}/holdings/{hid}     — Delete holding
  POST   /api/v1/portfolios/{id}/holdings/import    — CSV import (multipart)

Transactions:
  POST   /api/v1/holdings/{hid}/transactions        — Record transaction
  GET    /api/v1/holdings/{hid}/transactions         — By holding
  GET    /api/v1/portfolios/{id}/transactions        — By portfolio

Allocation:
  GET    /api/v1/portfolios/{id}/allocation          — Breakdown by type/sector/currency
```

### CSV import format
Required columns: `asset_type`, `ticker`, `quantity`, `purchase_price`, `purchase_date`
Optional columns: `name`, `currency`, `sector`, `category`

---

## Session 5 — 2026-02-10

### What was done
Implemented **FR-PM-005** (real-time market value), **FR-PM-006** (unrealized gain/loss), and **FR-PM-008** (multi-currency FX conversion) using Finnhub API.

### New backend files created
| File | Purpose |
|---|---|
| `config/FinnhubConfig.java` | `@ConfigurationProperties` for Finnhub API key + base URL; defines `RestTemplate` bean |
| `config/CacheConfig.java` | `SimpleCacheManager` with `quotes` and `fxRates` caches; 60s scheduled eviction |
| `service/MarketDataService.java` | `getCurrentPrice(ticker)` via Finnhub `/quote`; `getExchangeRate(from, to)` via Finnhub `/forex/rates`; both `@Cacheable` |
| `service/ValuationService.java` | Combines holdings + live prices + FX rates; computes cost basis, market value, gain/loss per holding and totals |
| `dto/ValuationResponse.java` | Response DTO with nested `HoldingValuation` (current price, FX rate, cost basis, market value, gain/loss, return %) |

### New configuration files created
| File | Details |
|---|---|
| `application-local.yml` | Finnhub key hardcoded for local development |
| `application-dev.yml` | Finnhub key hardcoded for dev environment |
| `application-cat.yml` | Finnhub key with env var fallback |
| `application-prod.yml` | Finnhub key from env var only (`${FINNHUB_API_KEY}`) |

### Modified backend files
| File | Changes |
|---|---|
| `controller/PortfolioController.java` | Injected `ValuationService`; added `GET /{id}/valuation` endpoint |

### Frontend changes
| File | Changes |
|---|---|
| `pages/Portfolio.tsx` | Added 4th "Valuation" tab with: summary cards (cost basis, market value, gain/loss, return %), detailed table per holding (purchase price, current price, FX rate, cost basis, market value, gain/loss, return %), loading state |

### API endpoint added
```
Valuation:
  GET /api/v1/portfolios/{id}/valuation — Live portfolio valuation with market prices + FX
```

### Architecture decisions
- **Caching**: `@Cacheable` on `MarketDataService` methods with scheduled 60s eviction prevents excessive Finnhub API calls
- **FX conversion**: Holdings in non-base currencies are converted using Finnhub forex rates at the portfolio's base currency
- **Graceful degradation**: If Finnhub returns no price, cost basis is used as market value with zero gain/loss
- **Profile-based config**: API keys separated into profile YAML files, not in main `application.yml`

---

## Session 6 — 2026-02-10

### What was done
- Fixed **403 auth error** — Spring Security was using `Http403ForbiddenEntryPoint` as default. Added `HttpStatusEntryPoint(UNAUTHORIZED)` to return 401. Updated frontend client to handle both 401 and 403.
- Implemented **full section 4.4 Stock & Sector Screener** (FR-SC-001 through FR-SC-010)

### New backend files created
| File | Purpose |
|---|---|
| `model/ScreenerReport.java` | JPA Entity mapping to `screener_reports` table |
| `repository/ScreenerReportRepository.java` | Repository for saved screener reports |
| `dto/TickerReportResponse.java` | Comprehensive ticker report DTO (profile, metrics, financials, filings, analysts) |
| `dto/SectorReportResponse.java` | Sector performance report DTO (top/bottom performers, rotation signals) |
| `dto/ScreenCriteriaRequest.java` | Custom screening criteria request DTO |
| `dto/ScreenResultResponse.java` | Custom screen results DTO |
| `dto/TechnicalIndicatorResponse.java` | Technical indicator time-series DTO |
| `service/ScreenerService.java` | Core screener orchestration (ticker reports, sector reports, custom screens, saved reports) |
| `controller/ScreenerController.java` | REST endpoints for all screener features |

### Modified backend files
| File | Changes |
|---|---|
| `service/MarketDataService.java` | Added 10 new Finnhub endpoints: profile, metrics, financials, filings, recommendations, price targets, earnings, peers, stock symbols, technical indicators |
| `config/CacheConfig.java` | Added 10 new cache names for screener data |
| `config/SecurityConfig.java` | Added `HttpStatusEntryPoint(UNAUTHORIZED)` |

### Frontend changes
| File | Changes |
|---|---|
| `pages/Screener.tsx` | Full rewrite: 4 tabs (Ticker, Sector, Custom Screen, Saved Reports) with complete Finnhub integration |
| `api/client.ts` | Handle both 401 and 403 in response interceptor |

### API endpoints added
```
Screener:
  GET    /api/v1/screener/ticker/{symbol}       — Comprehensive ticker report
  GET    /api/v1/screener/sectors               — List available sectors
  GET    /api/v1/screener/sector/{sector}       — Sector performance report
  POST   /api/v1/screener/screen                — Run custom stock screen
  GET    /api/v1/screener/indicators/{symbol}   — Technical indicator data
  GET    /api/v1/screener/reports               — List saved reports
  POST   /api/v1/screener/reports               — Save a report
  GET    /api/v1/screener/reports/{id}          — Get saved report data
  DELETE /api/v1/screener/reports/{id}          — Delete saved report
```

### Requirements covered
- **FR-SC-001**: Ticker Screener with comprehensive reports
- **FR-SC-002**: Current price, 52-week range, market cap, P/E, EPS, dividend yield, revenue/earnings trends
- **FR-SC-003**: Financial statements (annual + quarterly) from Finnhub financials-reported API
- **FR-SC-004**: SEC filings (10-K, 10-Q, 8-K, proxy) from Finnhub filings API
- **FR-SC-005**: Analyst recommendations (buy/hold/sell), price targets, earnings estimates
- **FR-SC-006**: Sector Screener with 11 sectors (10 representative tickers each)
- **FR-SC-007**: Sector vs S&P 500, top/bottom performers, avg P/E, rotation signals
- **FR-SC-008**: Custom screening criteria (P/E, dividend yield, market cap, beta, price)
- **FR-SC-009**: Technical indicators via Finnhub indicator API (SMA, EMA, RSI, MACD, Bollinger)
- **FR-SC-010**: Save/revisit past screens (persisted in screener_reports table)
