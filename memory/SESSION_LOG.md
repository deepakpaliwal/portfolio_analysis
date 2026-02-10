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
