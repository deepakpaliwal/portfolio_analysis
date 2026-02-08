# Portfolio Analysis Application — Requirements Document

**Version:** 1.0
**Date:** 2026-02-07
**License:** Apache 2.0

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [System Overview](#2-system-overview)
3. [Technology Stack](#3-technology-stack)
4. [Functional Requirements](#4-functional-requirements)
   - 4.1 [User Management](#41-user-management)
   - 4.2 [Portfolio Management](#42-portfolio-management)
   - 4.3 [Asset Management](#43-asset-management)
   - 4.4 [Stock & Sector Screener](#44-stock--sector-screener)
   - 4.5 [Risk Analytics](#45-risk-analytics)
   - 4.6 [Correlation & Hedging Analysis](#46-correlation--hedging-analysis)
   - 4.7 [Strategy Engine](#47-strategy-engine)
   - 4.8 [Automated Trading](#48-automated-trading)
   - 4.9 [Reporting & Dashboards](#49-reporting--dashboards)
   - 4.10 [Admin Panel](#410-admin-panel)
   - 4.11 [Freemium & Subscription Model](#411-freemium--subscription-model)
   - 4.12 [Alerting & Notifications](#412-alerting--notifications)
   - 4.13 [Log Search & Management](#413-log-search--management)
   - 4.14 [Batch Processing & Deep Analysis](#414-batch-processing--deep-analysis)
5. [Non-Functional Requirements](#5-non-functional-requirements)
   - 5.1 [Performance](#51-performance)
   - 5.2 [Scalability](#52-scalability)
   - 5.3 [Security](#53-security)
   - 5.4 [Availability & Reliability](#54-availability--reliability)
   - 5.5 [Data Storage & Persistence](#55-data-storage--persistence)
   - 5.6 [Usability](#56-usability)
   - 5.7 [Maintainability](#57-maintainability)
   - 5.8 [Compliance & Regulatory](#58-compliance--regulatory)
   - 5.9 [Observability](#59-observability)
   - 5.10 [Testing & Quality Assurance](#510-testing--quality-assurance)
   - 5.11 [Documentation](#511-documentation)
6. [Data Model (High-Level)](#6-data-model-high-level)
7. [External Integrations](#7-external-integrations)
8. [Glossary](#8-glossary)

---

## 1. Introduction

### 1.1 Purpose

This document defines the functional and non-functional requirements for a **Financial Portfolio Analysis Application**. The application enables investors to construct multi-asset portfolios, perform risk analytics, screen stocks and sectors, and execute automated trading strategies across traditional and cryptocurrency markets.

### 1.2 Scope

The system covers portfolio construction, real-time and historical market data analysis, quantitative risk measurement, strategy recommendation, and broker-integrated automated trading. It supports equities, fixed income, options, cash, real estate holdings, retirement funds, and cryptocurrencies.

### 1.3 Audience

- Software engineers and architects implementing the system
- Product owners and stakeholders defining acceptance criteria
- QA engineers designing test plans
- DevOps engineers planning infrastructure

### 1.4 Definitions & Conventions

- **SHALL** — mandatory requirement
- **SHOULD** — recommended but not mandatory
- **MAY** — optional / nice-to-have
- Requirement IDs follow the pattern `[CATEGORY]-[NUMBER]` (e.g., `FR-PM-001`)

---

## 2. System Overview

The Portfolio Analysis Application is a **microservices-based** full-stack web application that provides:

- **Portfolio Construction** — Create and manage portfolios containing stocks, bonds, options, cash, real estate, retirement funds, and cryptocurrencies.
- **Market Intelligence** — Screen individual tickers and entire sectors by aggregating market data, financial statements, SEC filings, and analyst recommendations.
- **Risk Analytics** — Calculate Value at Risk (VaR), portfolio volatility, beta against S&P 500, and other risk metrics.
- **Hedging & Correlation** — Identify correlated and inversely-correlated assets to suggest hedging opportunities.
- **Strategy & Automation** — Suggest money-making strategies and execute trades automatically on behalf of the user based on a selected strategy.
- **Admin Panel** — Centralized administration interface for system configuration, user management, feature toggles, and subscription plan management.
- **Batch Analytics** — Scheduled batch processing for deep data analysis, recommendation generation, and portfolio optimization.
- **Freemium Model** — Tiered subscription model with free basic features and paid premium capabilities.

```
┌──────────────────────────────────────────────────────────────────────┐
│                          React Frontend                              │
│   (Dashboard, Screener, Portfolio, Trading, Admin Panel)             │
└──────────────────────┬───────────────────────────────────────────────┘
                       │  REST / WebSocket / API Gateway
┌──────────────────────▼───────────────────────────────────────────────┐
│                       API Gateway / BFF                               │
│         (Routing, Auth, Rate Limiting, Load Balancing)                │
└──┬──────────┬──────────┬──────────┬──────────┬──────────┬────────────┘
   │          │          │          │          │          │
┌──▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼───┐  ┌──▼────────────┐
│User &│  │Port- │  │Screen│  │Risk &│  │Strat-│  │ Batch         │
│Auth  │  │folio │  │er    │  │Analy-│  │egy & │  │ Processing    │
│Micro │  │Micro │  │Micro │  │tics  │  │Trade │  │ Service       │
│svc   │  │svc   │  │svc   │  │Micro │  │Micro │  │ (Deep Analy-  │
│      │  │      │  │      │  │svc   │  │svc   │  │  sis & Reco)  │
└──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘  └──┬────────────┘
   │         │         │         │         │          │
┌──▼─────────▼─────────▼─────────▼─────────▼──────────▼────────────┐
│                    Message Broker (Kafka / RabbitMQ)               │
└──────┬──────────────┬──────────────────────────┬─────────────────┘
       │              │                          │
 ┌─────▼─────┐  ┌────▼─────┐  ┌─────────────────▼──────────────┐
 │ PostgreSQL │  │    H2    │  │  ELK / OpenSearch               │
 │ (Production│  │  (Local/ │  │  (Centralized Log Search &      │
 │  Store)    │  │   Dev)   │  │   Management)                   │
 └───────────┘  └──────────┘  └────────────────────────────────┘
```

---

## 3. Technology Stack

| Layer              | Technology                                         | Purpose                                                |
|--------------------|----------------------------------------------------|--------------------------------------------------------|
| **IDE**            | **IntelliJ IDEA**                                  | Primary development IDE for backend and frontend       |
| Frontend           | React (TypeScript)                                 | Single-page application UI                             |
| State Management   | Redux Toolkit / React Query                        | Client-side state and server-state caching             |
| Charting           | Recharts / D3.js                                   | Portfolio charts, risk visualizations                  |
| Backend            | Spring Boot 3.x (Java 21+)                        | REST API, business logic, scheduling                   |
| Architecture       | Spring Cloud / Microservices                       | Service discovery, config server, circuit breakers     |
| API Gateway        | Spring Cloud Gateway                               | Request routing, rate limiting, load balancing          |
| ORM                | Spring Data JPA / Hibernate                        | Database access and entity mapping                     |
| Production DB      | PostgreSQL 16+                                     | Primary persistent data store                          |
| Local / Dev DB     | H2 (in-memory or file-based)                       | Local development and integration testing              |
| Messaging          | Apache Kafka / RabbitMQ                            | Async inter-service communication and event streaming  |
| WebSocket          | Spring WebSocket / STOMP                           | Real-time price updates and notifications              |
| Scheduling         | Spring Scheduler / Quartz                          | Periodic data ingestion and strategy runs              |
| Batch Processing   | Spring Batch                                       | Deep data analysis, recommendation, and batch jobs     |
| Security           | Spring Security + OAuth 2.0 / JWT                  | Authentication and authorization                       |
| Social Login       | Google OAuth 2.0, Meta (Facebook) Login            | Third-party social authentication providers            |
| **Build**          | **Apache Maven**                                   | Backend build and dependency management                |
| Frontend Build     | Vite                                               | Frontend bundling and dev server                       |
| Containerization   | Docker / Docker Compose                            | Local and production deployment                        |
| Service Mesh       | Kubernetes / Docker Swarm (optional)               | Microservices orchestration and scaling                |
| Log Search         | ELK Stack (Elasticsearch, Logstash, Kibana) / OpenSearch | Centralized log aggregation, search, and visualization |
| Alerting           | Spring Mail, Twilio SMS API                        | Email and SMS alerting and notifications               |
| Payments           | Stripe / PayPal                                    | Freemium subscription billing and payment processing   |

---

## 4. Functional Requirements

### 4.1 User Management

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-UM-001   | The system SHALL allow users to register with their own email and password (self-registration).       | High     |
| FR-UM-002   | The system SHALL support OAuth 2.0 login via **Google** identity provider.                           | High     |
| FR-UM-002a  | The system SHALL support OAuth 2.0 login via **Meta (Facebook)** identity provider.                  | High     |
| FR-UM-002b  | The system SHALL allow users to choose between social login (Google/Meta) or registering with their own password during sign-up. | High |
| FR-UM-003   | The system SHALL enforce email verification before granting full access.                              | High     |
| FR-UM-004   | The system SHALL support role-based access control (VIEWER, TRADER, ADMIN).                          | High     |
| FR-UM-005   | The system SHALL allow users to configure notification preferences (email, in-app, SMS).             | Medium   |
| FR-UM-006   | The system SHALL provide a user profile page to manage personal information and linked brokerage accounts. | Medium |
| FR-UM-007   | The system SHALL enforce password complexity rules (minimum 12 characters, mixed case, digits, symbols). | High  |
| FR-UM-008   | The system SHALL support multi-factor authentication (TOTP-based).                                   | High     |
| FR-UM-009   | The system SHALL allow users who registered via social login to optionally set a local password for backup access. | Medium |
| FR-UM-010   | The system SHALL support account linking — a user who registered with email/password can later link their Google or Meta account, and vice versa. | Medium |

### 4.2 Portfolio Management

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-PM-001   | The system SHALL allow users to create one or more named portfolios.                                 | High     |
| FR-PM-002   | The system SHALL allow users to add, update, and remove holdings within a portfolio.                 | High     |
| FR-PM-003   | The system SHALL support the following asset classes within a portfolio: **Stocks, Bonds, Options, Cash, Real Estate, Retirement Funds, and Cryptocurrencies**. | High |
| FR-PM-004   | Each holding SHALL record: asset type, ticker/identifier, quantity, purchase price, purchase date, and currency. | High |
| FR-PM-005   | The system SHALL compute the current market value of each holding and the portfolio total in real time. | High |
| FR-PM-006   | The system SHALL display unrealized gain/loss (absolute and percentage) per holding and for the portfolio. | High |
| FR-PM-007   | The system SHALL allow users to tag or group holdings by custom categories (e.g., "tech", "income", "speculative"). | Medium |
| FR-PM-008   | The system SHALL support multiple currencies and perform automatic FX conversion for portfolio valuation. | Medium |
| FR-PM-009   | The system SHALL allow importing holdings via CSV or brokerage account sync.                         | Medium   |
| FR-PM-010   | The system SHALL maintain a complete transaction history for every portfolio (buys, sells, dividends, splits). | High |
| FR-PM-011   | The system SHALL allow users to clone an existing portfolio for what-if scenario analysis.            | Low      |
| FR-PM-012   | The system SHALL calculate and display portfolio allocation breakdown by asset class, sector, and geography. | High |

### 4.3 Asset Management

#### 4.3.1 Stocks (Equities)

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-AS-001   | The system SHALL support adding equities by ticker symbol (e.g., AAPL, MSFT).                       | High     |
| FR-AS-002   | The system SHALL fetch and display real-time and historical stock prices.                             | High     |
| FR-AS-003   | The system SHALL track dividend payments and factor them into total return calculations.              | Medium   |
| FR-AS-004   | The system SHALL support stock splits and adjust historical data accordingly.                         | Medium   |

#### 4.3.2 Bonds (Fixed Income)

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-BD-001   | The system SHALL support adding bonds by CUSIP or ISIN.                                              | High     |
| FR-BD-002   | The system SHALL capture bond-specific attributes: coupon rate, maturity date, face value, yield-to-maturity, credit rating. | High |
| FR-BD-003   | The system SHALL calculate accrued interest and current bond valuation.                               | Medium   |
| FR-BD-004   | The system SHALL support bond types: Treasury, Corporate, Municipal, and Agency.                     | Medium   |

#### 4.3.3 Options

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-OP-001   | The system SHALL support adding call and put options by OCC symbol or underlying + strike + expiry.  | High     |
| FR-OP-002   | The system SHALL display Greeks (Delta, Gamma, Theta, Vega, Rho) for each option position.          | High     |
| FR-OP-003   | The system SHALL compute theoretical option value using Black-Scholes or Binomial models.            | Medium   |
| FR-OP-004   | The system SHALL alert users when options are approaching expiration (configurable threshold).        | Medium   |
| FR-OP-005   | The system SHALL support multi-leg option strategies (spreads, straddles, iron condors, etc.).       | Medium   |

#### 4.3.4 Cash

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-CA-001   | The system SHALL allow recording cash positions in multiple currencies.                               | High     |
| FR-CA-002   | The system SHALL automatically convert cash balances to the portfolio's base currency for valuation.  | High     |

#### 4.3.5 Real Estate

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-RE-001   | The system SHALL allow users to add real estate holdings with: property address, purchase price, current estimated value, and property type (residential, commercial, REIT). | High |
| FR-RE-002   | The system SHALL support manual or automated (Zillow/Redfin API) valuation updates for properties.   | Medium   |
| FR-RE-003   | The system SHALL track rental income and expenses for income-producing properties.                    | Low      |
| FR-RE-004   | The system SHALL support REITs as tradeable securities with real-time pricing.                        | Medium   |

#### 4.3.6 Retirement Funds

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-RT-001   | The system SHALL support retirement account types: 401(k), IRA, Roth IRA, 403(b), SEP IRA.          | High     |
| FR-RT-002   | The system SHALL track contribution limits and warn users when approaching annual limits.             | Medium   |
| FR-RT-003   | The system SHALL calculate projected retirement value based on current balance, contribution rate, and expected return. | Medium |
| FR-RT-004   | The system SHALL account for tax implications (pre-tax vs. post-tax) in return calculations.         | Low      |

#### 4.3.7 Cryptocurrencies

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-CR-001   | The system SHALL support major cryptocurrencies (BTC, ETH, SOL, ADA, DOT, etc.) and ERC-20 tokens.  | High     |
| FR-CR-002   | The system SHALL fetch real-time and historical cryptocurrency prices from exchanges or aggregators.  | High     |
| FR-CR-003   | The system SHALL support wallet address tracking to auto-import balances from on-chain data.          | Medium   |
| FR-CR-004   | The system SHALL support DeFi positions (staking, liquidity pools) as portfolio line items.           | Low      |
| FR-CR-005   | The system SHALL display crypto-specific metrics: market cap, circulating supply, 24h volume, and dominance percentage. | Medium |
| FR-CR-006   | The system SHALL track gas fees and transaction costs for crypto trades.                              | Low      |

### 4.4 Stock & Sector Screener

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-SC-001   | The system SHALL provide a **Ticker Screener** that generates a comprehensive report for any given stock ticker. | High |
| FR-SC-002   | The ticker report SHALL include: current price, 52-week high/low, market cap, P/E ratio, EPS, dividend yield, and revenue/earnings trends. | High |
| FR-SC-003   | The system SHALL ingest and display **financial statements** (income statement, balance sheet, cash flow) for the last 5 years (annual) and 8 quarters (quarterly). | High |
| FR-SC-004   | The system SHALL retrieve and summarize **SEC filings** (10-K, 10-Q, 8-K, proxy statements) for screened companies. | High |
| FR-SC-005   | The system SHALL aggregate and display **analyst recommendations** (buy/hold/sell consensus, price targets, earnings estimates). | High |
| FR-SC-006   | The system SHALL provide a **Sector Screener** that generates performance reports for market sectors (e.g., Technology, Healthcare, Energy). | High |
| FR-SC-007   | The sector report SHALL include: sector performance vs. S&P 500, top/bottom performing stocks, average P/E, sector rotation signals, and fund flows. | High |
| FR-SC-008   | The system SHALL allow users to define custom screening criteria (e.g., P/E < 15, dividend yield > 3%, market cap > $10B). | Medium |
| FR-SC-009   | The system SHALL support technical indicator overlays on screened tickers: SMA, EMA, RSI, MACD, Bollinger Bands. | Medium |
| FR-SC-010   | The system SHALL cache screener results and allow users to save and revisit past screens.             | Medium   |
| FR-SC-011   | The system SHALL allow exporting screener reports as PDF or CSV.                                      | Low      |
| FR-SC-012   | The system SHALL provide sentiment analysis by aggregating news headlines and social media mentions for a ticker or sector. | Low |

### 4.5 Risk Analytics

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-RA-001   | The system SHALL calculate **Value at Risk (VaR)** for each portfolio using: Historical Simulation, Variance-Covariance (Parametric), and Monte Carlo Simulation methods. | High |
| FR-RA-002   | The system SHALL allow configurable VaR parameters: confidence level (90%, 95%, 99%) and time horizon (1-day, 10-day, 30-day). | High |
| FR-RA-003   | The system SHALL calculate and display **Conditional VaR (CVaR / Expected Shortfall)**.              | Medium   |
| FR-RA-004   | The system SHALL calculate **portfolio volatility** (annualized standard deviation of returns).       | High     |
| FR-RA-005   | The system SHALL calculate **beta** of the portfolio and each individual holding against the S&P 500 (SPX). | High |
| FR-RA-006   | The system SHALL calculate **alpha** (Jensen's alpha) measuring risk-adjusted excess return.          | Medium   |
| FR-RA-007   | The system SHALL calculate and display **Sharpe Ratio**, **Sortino Ratio**, and **Treynor Ratio**.   | High     |
| FR-RA-008   | The system SHALL compute **maximum drawdown** over configurable lookback periods.                     | Medium   |
| FR-RA-009   | The system SHALL perform **stress testing** by simulating portfolio impact of historical scenarios (e.g., 2008 Financial Crisis, COVID-19 crash, Dot-com bubble). | Medium |
| FR-RA-010   | The system SHALL allow users to define custom stress scenarios with user-specified market shocks.      | Low      |
| FR-RA-011   | The system SHALL provide a risk dashboard summarizing all risk metrics in one consolidated view.       | High     |
| FR-RA-012   | The system SHALL recalculate risk metrics at least once per trading day and on-demand when requested.  | High     |

### 4.6 Correlation & Hedging Analysis

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-CH-001   | The system SHALL compute a **correlation matrix** across all holdings in a portfolio.                 | High     |
| FR-CH-002   | The system SHALL visualize the correlation matrix as a heatmap.                                       | High     |
| FR-CH-003   | The system SHALL identify **highly correlated asset pairs** (correlation > 0.7) and flag concentration risk. | High |
| FR-CH-004   | The system SHALL identify **negatively correlated assets** suitable as hedges for existing positions.  | High     |
| FR-CH-005   | The system SHALL suggest specific hedge instruments (e.g., put options, inverse ETFs, short positions, uncorrelated assets) for portfolio protection. | High |
| FR-CH-006   | The system SHALL compute **rolling correlations** over configurable windows (30-day, 90-day, 1-year) to detect regime changes. | Medium |
| FR-CH-007   | The system SHALL provide diversification scoring for the portfolio based on correlation analysis.      | Medium   |
| FR-CH-008   | The system SHALL support cross-asset-class correlation (e.g., equity vs. crypto, bonds vs. stocks).   | Medium   |

### 4.7 Strategy Engine

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-SE-001   | The system SHALL suggest **investment strategies** based on user risk profile, goals, and current portfolio composition. | High |
| FR-SE-002   | The system SHALL support the following predefined strategies: <ul><li>Value Investing (screen for undervalued stocks)</li><li>Growth Investing (high revenue/earnings growth)</li><li>Dividend Income (high-yield, consistent payers)</li><li>Momentum Trading (trend-following based on technical signals)</li><li>Mean Reversion (buy oversold, sell overbought)</li><li>Covered Call Writing (income from owned equities)</li><li>Pairs Trading (long/short correlated pairs on divergence)</li><li>Risk Parity (equal risk contribution allocation)</li><li>Dollar-Cost Averaging (systematic periodic investment)</li><li>Crypto Yield Farming (DeFi staking and LP strategies)</li></ul> | High |
| FR-SE-003   | The system SHALL backtest any strategy against historical data and present performance metrics (CAGR, max drawdown, Sharpe, win rate). | High |
| FR-SE-004   | The system SHALL allow users to customize strategy parameters (e.g., lookback periods, thresholds, allocation weights). | Medium |
| FR-SE-005   | The system SHALL provide a strategy comparison view showing side-by-side performance of multiple strategies. | Medium |
| FR-SE-006   | The system SHALL generate trade signals (BUY / SELL / HOLD) for each strategy with rationale.        | High     |
| FR-SE-007   | The system SHALL assess portfolio tax efficiency and suggest tax-loss harvesting opportunities.        | Low      |

### 4.8 Automated Trading

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-AT-001   | The system SHALL allow users to link brokerage accounts via API (e.g., Alpaca, Interactive Brokers, TD Ameritrade). | High |
| FR-AT-002   | The system SHALL allow users to link cryptocurrency exchange accounts (e.g., Coinbase, Binance, Kraken) via API keys. | High |
| FR-AT-003   | The system SHALL allow users to select an active strategy and enable automated trading for a portfolio. | High |
| FR-AT-004   | The system SHALL execute trades automatically based on the selected strategy's signals.               | High     |
| FR-AT-005   | The system SHALL enforce configurable **risk limits** before executing any trade: max position size, max daily loss, max total exposure. | High |
| FR-AT-006   | The system SHALL require explicit user confirmation for trades exceeding a configurable dollar threshold. | High |
| FR-AT-007   | The system SHALL log every automated trade with: timestamp, asset, action, quantity, price, strategy, and rationale. | High |
| FR-AT-008   | The system SHALL support **paper trading mode** to simulate strategy execution without real money.     | High     |
| FR-AT-009   | The system SHALL provide a kill switch to immediately halt all automated trading.                      | High     |
| FR-AT-010   | The system SHALL notify users (email + in-app) of every executed trade and daily P&L summary.         | Medium   |
| FR-AT-011   | The system SHALL support order types: market, limit, stop-loss, and trailing stop.                    | High     |
| FR-AT-012   | The system SHALL handle partial fills and order rejections gracefully.                                 | Medium   |

### 4.9 Reporting & Dashboards

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-RD-001   | The system SHALL provide a **main dashboard** showing portfolio summary, performance chart, allocation pie chart, and top movers. | High |
| FR-RD-002   | The system SHALL provide customizable date range selection for all charts and reports.                 | High     |
| FR-RD-003   | The system SHALL benchmark portfolio performance against configurable indices (S&P 500, NASDAQ, Dow Jones, Russell 2000). | High |
| FR-RD-004   | The system SHALL generate periodic performance reports (daily, weekly, monthly, quarterly, annual).    | Medium   |
| FR-RD-005   | The system SHALL provide a **trade journal** view showing all executed trades with filtering and sorting. | Medium  |
| FR-RD-006   | The system SHALL export reports in PDF, CSV, and Excel formats.                                       | Medium   |
| FR-RD-007   | The system SHALL provide real-time portfolio value updates via WebSocket push.                         | High     |

### 4.10 Admin Panel

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-AP-001   | The system SHALL provide a web-based **Admin Panel** accessible only to users with the ADMIN role.   | High     |
| FR-AP-002   | The Admin Panel SHALL allow administrators to manage users: view, activate, deactivate, delete, and change user roles. | High |
| FR-AP-003   | The Admin Panel SHALL provide a **system configuration** interface for modifying application-wide settings (e.g., rate limits, trading limits, maintenance mode) without redeployment. | High |
| FR-AP-004   | The Admin Panel SHALL allow administrators to manage **feature toggles** to enable or disable features globally or per subscription tier. | High |
| FR-AP-005   | The Admin Panel SHALL provide a **subscription plan management** interface to create, modify, and retire freemium/premium plans. | High |
| FR-AP-006   | The Admin Panel SHALL display a **system health dashboard** showing service status, uptime, resource utilization, and error rates across all microservices. | Medium |
| FR-AP-007   | The Admin Panel SHALL allow administrators to broadcast **system-wide announcements** and maintenance notifications to all users. | Medium |
| FR-AP-008   | The Admin Panel SHALL provide an **audit log viewer** with filtering and search capabilities for reviewing sensitive operations. | High |
| FR-AP-009   | The Admin Panel SHALL allow administrators to manage **API rate limits** and override limits for specific users or tiers. | Medium |
| FR-AP-010   | The Admin Panel SHALL provide tools to manage **batch jobs**: trigger, pause, resume, and view execution history of batch processing tasks. | Medium |
| FR-AP-011   | The Admin Panel SHALL allow administrators to view and manage **alerting rules** for email and SMS notifications. | Medium |
| FR-AP-012   | All changes made via the Admin Panel SHALL be recorded in the audit log with the administrator's identity, timestamp, and details of the change. | High |

### 4.11 Freemium & Subscription Model

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-FM-001   | The system SHALL implement a **Freemium subscription model** with at least two tiers: **Free** and **Premium**. | High |
| FR-FM-002   | The **Free tier** SHALL include: single portfolio (up to 20 holdings), basic screener (limited to 5 screens/day), basic risk metrics (portfolio volatility, beta), and delayed market data (15-minute delay). | High |
| FR-FM-003   | The **Premium tier** SHALL include: unlimited portfolios and holdings, full screener with unlimited screens, advanced risk analytics (VaR, CVaR, Monte Carlo, stress testing), real-time market data, automated trading, strategy backtesting, and batch deep analysis reports. | High |
| FR-FM-004   | The system SHOULD support an optional **Pro tier** (between Free and Premium) with intermediate feature access. | Low |
| FR-FM-005   | The system SHALL clearly indicate to users which features are locked behind a paid tier with upgrade prompts. | High |
| FR-FM-006   | The system SHALL integrate with a **payment processor** (Stripe and/or PayPal) for subscription billing. | High |
| FR-FM-007   | The system SHALL support **monthly and annual** billing cycles with a discount for annual subscriptions. | Medium |
| FR-FM-008   | The system SHALL allow users to upgrade, downgrade, or cancel their subscription at any time.         | High     |
| FR-FM-009   | The system SHALL provide a **free trial period** (configurable, default: 14 days) for Premium features. | Medium |
| FR-FM-010   | The system SHALL handle subscription expiration gracefully by downgrading the user to the Free tier without data loss. | High |
| FR-FM-011   | The system SHALL track feature usage per user for analytics and plan optimization.                     | Medium   |
| FR-FM-012   | The system SHALL support **promotional codes** and discount coupons for subscription plans.            | Low      |

### 4.12 Alerting & Notifications

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-AN-001   | The system SHALL support alerting users via **email** for critical events and notifications.           | High     |
| FR-AN-002   | The system SHALL support alerting users via **SMS** for time-sensitive and critical alerts.            | High     |
| FR-AN-003   | The system SHALL allow users to configure which alert types they receive via email, SMS, or both.     | High     |
| FR-AN-004   | The system SHALL send alerts for the following events: trade executions, risk limit breaches, portfolio value thresholds (user-defined), strategy signal generation, option expiration warnings, and subscription/billing events. | High |
| FR-AN-005   | The system SHALL support **price alerts** — users can set alerts when a specific asset reaches a target price. | High |
| FR-AN-006   | The system SHALL support **portfolio performance alerts** — notify when daily P&L exceeds a user-defined threshold (gain or loss). | Medium |
| FR-AN-007   | The system SHALL implement an **alert management dashboard** where users can view, create, edit, and delete their alert rules. | Medium |
| FR-AN-008   | The system SHALL throttle alerts to prevent alert fatigue (configurable maximum alerts per hour per channel). | Medium |
| FR-AN-009   | The system SHALL maintain an alert history log showing all sent alerts with delivery status.           | Medium   |
| FR-AN-010   | The system SHALL support **in-app notifications** (push to frontend via WebSocket) in addition to email and SMS. | Medium |
| FR-AN-011   | Email alerts SHALL be sent via a reliable email service provider (e.g., SendGrid, AWS SES).           | High     |
| FR-AN-012   | SMS alerts SHALL be sent via a reliable SMS gateway (e.g., Twilio).                                   | High     |

### 4.13 Log Search & Management

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-LS-001   | The system SHALL provide a **centralized log search** interface accessible to ADMIN users.            | High     |
| FR-LS-002   | The log search SHALL support full-text search across all application logs from all microservices.     | High     |
| FR-LS-003   | The log search SHALL support filtering by: service name, log level (DEBUG, INFO, WARN, ERROR), timestamp range, correlation ID, and user ID. | High |
| FR-LS-004   | The system SHALL aggregate logs from all microservices into a centralized log store (ELK Stack / OpenSearch). | High |
| FR-LS-005   | The system SHALL support structured log queries using Kibana Query Language (KQL) or equivalent.      | Medium   |
| FR-LS-006   | The system SHALL provide **log dashboards** with visualizations for error rate trends, request volume, and response time distributions. | Medium |
| FR-LS-007   | The system SHALL support **log-based alerting** — trigger alerts when error rates exceed thresholds or specific error patterns are detected. | Medium |
| FR-LS-008   | The system SHALL retain logs for a configurable retention period (default: 90 days).                  | High     |
| FR-LS-009   | The system SHALL ensure all logs include **correlation IDs** to enable end-to-end request tracing across microservices. | High |
| FR-LS-010   | The system SHALL ensure that sensitive data (passwords, API keys, PII) is **never logged** in plain text. | High |

### 4.14 Batch Processing & Deep Analysis

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| FR-BP-001   | The system SHALL include a **Batch Processing Service** built on Spring Batch for scheduled and on-demand data analysis jobs. | High |
| FR-BP-002   | The batch service SHALL perform **deep data analysis** on portfolio holdings, including multi-factor performance attribution, trend detection, and anomaly identification. | High |
| FR-BP-003   | The batch service SHALL generate **recommendation reports** based on analysis of portfolio composition, market conditions, and user risk profile. | High |
| FR-BP-004   | Recommendations SHALL include: asset rebalancing suggestions, underperforming holding alerts, diversification opportunities, and tax-loss harvesting candidates. | High |
| FR-BP-005   | The batch service SHALL perform **sector and market trend analysis** by processing large volumes of historical and real-time market data. | Medium |
| FR-BP-006   | The batch service SHALL support **configurable scheduling** — jobs can run daily, weekly, monthly, or on-demand via the Admin Panel. | High |
| FR-BP-007   | The batch service SHALL implement **job restart and recovery** — failed jobs can be restarted from the last successful checkpoint. | High |
| FR-BP-008   | The batch service SHALL support **parallel chunk processing** to efficiently process large datasets (millions of market data records). | Medium |
| FR-BP-009   | The batch service SHALL generate **user-facing analysis reports** delivered via email notification and available in the user dashboard. | Medium |
| FR-BP-010   | The batch service SHALL compute **correlation and regression analysis** across asset classes for portfolio optimization recommendations. | Medium |
| FR-BP-011   | The batch service SHALL maintain a **job execution log** with status, duration, records processed, and errors for each run. | High |
| FR-BP-012   | The batch service SHALL be a **separate microservice** that can scale independently from the real-time API services. | High |

---

## 5. Non-Functional Requirements

### 5.1 Performance

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-PF-001  | API responses for portfolio queries SHALL return within **200ms** at the 95th percentile under normal load. | High |
| NFR-PF-002  | Real-time price updates SHALL be delivered to the frontend within **500ms** of receipt from market data providers. | High |
| NFR-PF-003  | VaR calculation (Monte Carlo with 10,000 simulations) SHALL complete within **5 seconds** for portfolios with up to 100 holdings. | High |
| NFR-PF-004  | The screener report generation SHALL complete within **10 seconds** for a single ticker.              | Medium   |
| NFR-PF-005  | The frontend initial page load (Time to Interactive) SHALL be under **3 seconds** on a standard broadband connection. | High |
| NFR-PF-006  | Backtesting a strategy over 10 years of daily data SHALL complete within **30 seconds**.              | Medium   |
| NFR-PF-007  | Database queries SHALL be optimized with appropriate indexing; no single query SHALL exceed **100ms** execution time. | High |

### 5.2 Scalability

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-SC-001  | The system SHALL support at least **10,000 concurrent users** without degradation.                    | Medium   |
| NFR-SC-002  | The backend SHALL be stateless to allow horizontal scaling behind a load balancer.                     | High     |
| NFR-SC-003  | The database layer SHALL support read replicas for scaling read-heavy analytics workloads.             | Medium   |
| NFR-SC-004  | Market data ingestion SHALL scale independently from the API layer.                                    | Medium   |
| NFR-SC-005  | The system SHOULD support sharding or partitioning of historical price data by date range.             | Low      |

### 5.3 Security

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-SE-001  | All API endpoints SHALL require authentication via JWT tokens with a maximum lifetime of 1 hour.      | High     |
| NFR-SE-002  | All communication between client and server SHALL use TLS 1.2+.                                       | High     |
| NFR-SE-003  | Brokerage API keys and credentials SHALL be encrypted at rest using AES-256.                          | High     |
| NFR-SE-004  | The system SHALL enforce rate limiting on all public-facing APIs (configurable, default: 100 req/min per user). | High |
| NFR-SE-005  | The system SHALL sanitize all user inputs to prevent injection attacks (SQL, XSS, CSRF).              | High     |
| NFR-SE-006  | The system SHALL maintain an audit log of all sensitive operations (login, trade execution, API key management). | High |
| NFR-SE-007  | The system SHALL implement account lockout after 5 consecutive failed login attempts (30-minute lockout). | High |
| NFR-SE-008  | Passwords SHALL be hashed using bcrypt with a minimum cost factor of 12.                              | High     |
| NFR-SE-009  | The system SHALL undergo annual penetration testing and vulnerability assessments.                     | Medium   |
| NFR-SE-010  | Brokerage and exchange API keys SHALL never be exposed in API responses, logs, or error messages.      | High     |

### 5.4 Availability & Reliability

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-AR-001  | The system SHALL target **99.9% uptime** (approximately 8.7 hours downtime per year) during market hours. | High |
| NFR-AR-002  | The system SHALL implement graceful degradation: if market data feeds are unavailable, last-known prices SHALL be displayed with a staleness indicator. | High |
| NFR-AR-003  | The trading execution service SHALL implement circuit breakers to prevent cascading failures.          | High     |
| NFR-AR-004  | The system SHALL perform daily automated backups of the PostgreSQL database with a 30-day retention period. | High |
| NFR-AR-005  | Database backups SHALL support point-in-time recovery (PITR) with a recovery point objective (RPO) of **1 hour**. | Medium |
| NFR-AR-006  | The system SHALL recover from a full outage within a recovery time objective (RTO) of **4 hours**.     | Medium   |
| NFR-AR-007  | All trade operations SHALL be idempotent to prevent duplicate execution on retry.                      | High     |

### 5.5 Data Storage & Persistence

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-DS-001  | **PostgreSQL** SHALL be the primary data store for all production environments.                        | High     |
| NFR-DS-002  | **H2** SHALL be used as the local data store for development and integration testing.                  | High     |
| NFR-DS-003  | The application SHALL use Spring Profiles to switch between PostgreSQL (prod, staging) and H2 (local, test) without code changes. | High |
| NFR-DS-004  | Database schema migrations SHALL be managed via Flyway or Liquibase with version-controlled migration scripts. | High |
| NFR-DS-005  | Historical price data SHALL be retained for a minimum of **20 years** to support long-term backtesting. | Medium  |
| NFR-DS-006  | The H2 local store SHALL support both in-memory mode (for tests) and file-based mode (for local development persistence). | Medium |
| NFR-DS-007  | The system SHALL use connection pooling (HikariCP) with configurable pool sizes per environment.       | High     |
| NFR-DS-008  | Large binary data (SEC filings, PDF reports) SHOULD be stored in object storage (S3-compatible) with metadata references in PostgreSQL. | Medium |

### 5.6 Usability

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-US-001  | The frontend SHALL be responsive and functional on desktop (1024px+) and tablet (768px+) viewports.   | High     |
| NFR-US-002  | The application SHALL support light and dark themes.                                                   | Low      |
| NFR-US-003  | All interactive charts SHALL support zoom, pan, and tooltip on hover.                                  | Medium   |
| NFR-US-004  | The system SHALL provide contextual help tooltips explaining financial terms and metrics.               | Medium   |
| NFR-US-005  | The system SHALL follow WCAG 2.1 Level AA accessibility guidelines.                                    | Medium   |
| NFR-US-006  | Error messages SHALL be user-friendly and provide actionable guidance.                                  | High     |

### 5.7 Maintainability

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-MT-001  | The backend SHALL follow a layered architecture: Controller → Service → Repository.                    | High     |
| NFR-MT-002  | The codebase SHALL maintain a minimum of **90% unit test code coverage** for business logic, measured by JaCoCo or equivalent coverage tool. | High     |
| NFR-MT-003  | Integration tests SHALL cover all API endpoints and database operations.                               | High     |
| NFR-MT-004  | The project SHALL use a CI/CD pipeline with automated build, test, lint, and deploy stages.            | High     |
| NFR-MT-005  | API endpoints SHALL be documented using OpenAPI 3.0 (Swagger) with auto-generated documentation.       | High     |
| NFR-MT-006  | The system SHALL use structured logging (JSON format) with correlation IDs for request tracing.         | Medium   |
| NFR-MT-007  | All configuration SHALL be externalized via environment variables or Spring configuration files.        | High     |

### 5.8 Compliance & Regulatory

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-CR-001  | The system SHALL display appropriate disclaimers that automated trading involves risk of financial loss. | High   |
| NFR-CR-002  | The system SHALL log all trading activity in a format suitable for regulatory audit.                    | High     |
| NFR-CR-003  | The system SHOULD comply with SEC and FINRA regulations regarding automated trading systems.            | Medium   |
| NFR-CR-004  | The system SHALL comply with GDPR and CCPA for user data handling, including right to deletion.         | High     |
| NFR-CR-005  | The system SHALL provide users a mechanism to export all their personal data (data portability).        | Medium   |
| NFR-CR-006  | Crypto trading features SHALL comply with applicable FinCEN and local jurisdiction regulations.         | Medium   |

### 5.9 Observability

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-OB-001  | The system SHALL expose health check endpoints (`/actuator/health`) for monitoring.                    | High     |
| NFR-OB-002  | The system SHALL export metrics (request latency, error rates, trade execution times) via Micrometer to Prometheus-compatible endpoints. | Medium |
| NFR-OB-003  | The system SHALL support distributed tracing (OpenTelemetry) for end-to-end request tracking.          | Medium   |
| NFR-OB-004  | The system SHALL generate alerts for: failed trades, market data feed outages, abnormal error rates, and risk limit breaches. | High |
| NFR-OB-005  | Application logs SHALL be aggregated in a centralized logging system (ELK stack or equivalent).         | Medium   |

### 5.10 Testing & Quality Assurance

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-TQ-001  | The codebase SHALL achieve a minimum of **90% code coverage** across all microservices, measured by JaCoCo and enforced in the Maven build via the `jacoco-maven-plugin`. | High |
| NFR-TQ-002  | Unit tests SHALL be written using **JUnit 5** and **Mockito** for all service, repository, and utility classes. | High |
| NFR-TQ-003  | Integration tests SHALL be written using **Spring Boot Test** with `@SpringBootTest` and test containers for database and messaging dependencies. | High |
| NFR-TQ-004  | The project SHALL include a comprehensive **regression test suite** that is executed on every build to ensure that new changes do not break existing functionality. | High |
| NFR-TQ-005  | The regression test suite SHALL cover all critical business flows: user registration/login, portfolio CRUD, trade execution, risk calculation, and billing/subscription management. | High |
| NFR-TQ-006  | The project SHALL include **behavioral test cases** (BDD-style) written using Cucumber or equivalent, with **sample data** fixtures for each scenario. | High |
| NFR-TQ-007  | Sample data for behavioral tests SHALL include: sample user profiles (free and premium tiers), sample portfolios with diverse asset classes, sample market data (historical prices for at least 5 tickers over 2 years), sample trade orders, and sample alert configurations. | High |
| NFR-TQ-008  | Sample behavioral test data SHALL be maintained in version-controlled fixture files (JSON/CSV) under `src/test/resources/fixtures/`. | High |
| NFR-TQ-009  | The project SHALL include **API contract tests** to validate request/response schemas for all REST endpoints. | Medium |
| NFR-TQ-010  | The project SHALL include **performance/load tests** using JMeter or Gatling for critical API endpoints. | Medium |
| NFR-TQ-011  | All test suites (unit, integration, regression, behavioral) SHALL be executable via Maven: `mvn test` for unit tests, `mvn verify` for integration and behavioral tests. | High |
| NFR-TQ-012  | The CI/CD pipeline SHALL fail the build if code coverage drops below the 90% threshold.               | High     |
| NFR-TQ-013  | The project SHALL include **end-to-end (E2E) tests** for critical user journeys using Selenium or Playwright. | Medium |
| NFR-TQ-014  | Test reports SHALL be generated in HTML and XML formats and published as CI/CD build artifacts.         | Medium   |

### 5.11 Documentation

| ID          | Requirement                                                                                          | Priority |
|-------------|------------------------------------------------------------------------------------------------------|----------|
| NFR-DC-001  | All Java classes SHALL include **Javadoc comments** describing the class purpose, author, and version. | High     |
| NFR-DC-002  | All public methods SHALL include **Javadoc comments** documenting parameters (`@param`), return values (`@return`), exceptions (`@throws`), and a brief description of behavior. | High |
| NFR-DC-003  | Complex business logic (risk calculations, strategy algorithms, batch processing logic) SHALL include **inline comments** explaining the reasoning and algorithmic approach. | High |
| NFR-DC-004  | Each microservice SHALL include a `README.md` documenting: service purpose, API endpoints, configuration properties, build instructions, and dependencies. | High |
| NFR-DC-005  | API documentation SHALL be auto-generated from code annotations using **SpringDoc OpenAPI** (Swagger UI) and kept in sync with the codebase. | High |
| NFR-DC-006  | Database schema changes SHALL be documented in migration scripts with descriptive comments explaining each change. | Medium |
| NFR-DC-007  | The Maven `pom.xml` SHALL be configured to generate Javadoc as part of the `site` lifecycle phase.     | Medium   |
| NFR-DC-008  | Configuration properties SHALL be documented using `@ConfigurationProperties` metadata with descriptions for each property. | Medium |
| NFR-DC-009  | The project SHALL maintain an **Architecture Decision Record (ADR)** log for significant design decisions. | Medium  |
| NFR-DC-010  | Code documentation quality SHALL be enforced via Checkstyle rules that flag missing Javadoc on public classes and methods. | High |

---

## 6. Data Model (High-Level)

```
┌──────────────┐       ┌──────────────────┐       ┌────────────────┐
│    User       │1    N │   Portfolio       │1    N │   Holding       │
├──────────────┤───────├──────────────────┤───────├────────────────┤
│ id            │       │ id                │       │ id              │
│ email         │       │ user_id (FK)      │       │ portfolio_id(FK)│
│ password_hash │       │ name              │       │ asset_type      │
│ role          │       │ base_currency     │       │ ticker/ident    │
│ mfa_secret    │       │ created_at        │       │ quantity        │
│ created_at    │       │ updated_at        │       │ purchase_price  │
└──────────────┘       └──────────────────┘       │ purchase_date   │
                                                   │ currency        │
                                                   │ metadata (JSON) │
                                                   └────────────────┘
        ┌──────────────────┐       ┌──────────────────┐
        │  Transaction      │       │  MarketData       │
        ├──────────────────┤       ├──────────────────┤
        │ id                │       │ id                │
        │ holding_id (FK)   │       │ ticker            │
        │ type (BUY/SELL/   │       │ asset_type        │
        │   DIVIDEND/SPLIT) │       │ price             │
        │ quantity          │       │ timestamp         │
        │ price             │       │ open/high/low/    │
        │ fees              │       │   close/volume    │
        │ executed_at       │       │ source            │
        └──────────────────┘       └──────────────────┘

        ┌──────────────────┐       ┌──────────────────┐
        │  TradeOrder        │       │  Strategy         │
        ├──────────────────┤       ├──────────────────┤
        │ id                │       │ id                │
        │ portfolio_id (FK) │       │ name              │
        │ strategy_id (FK)  │       │ type              │
        │ asset_type        │       │ parameters (JSON) │
        │ ticker            │       │ description       │
        │ action (BUY/SELL) │       │ is_active         │
        │ order_type        │       └──────────────────┘
        │ quantity          │
        │ limit_price       │       ┌──────────────────┐
        │ status            │       │  ScreenerReport   │
        │ broker_order_id   │       ├──────────────────┤
        │ executed_at       │       │ id                │
        │ rationale         │       │ user_id (FK)      │
        └──────────────────┘       │ type (TICKER/     │
                                    │        SECTOR)    │
        ┌──────────────────┐       │ target            │
        │  BrokerAccount    │       │ report_data(JSON) │
        ├──────────────────┤       │ created_at        │
        │ id                │       └──────────────────┘
        │ user_id (FK)      │
        │ broker_name       │
        │ api_key_encrypted │
        │ api_secret_enc    │
        │ account_type      │
        │ is_paper_trading  │
        └──────────────────┘

        ┌──────────────────┐       ┌──────────────────┐
        │  Subscription     │       │  AlertRule        │
        ├──────────────────┤       ├──────────────────┤
        │ id                │       │ id                │
        │ user_id (FK)      │       │ user_id (FK)      │
        │ plan (FREE/       │       │ alert_type        │
        │   PREMIUM/PRO)    │       │ channel (EMAIL/   │
        │ status (ACTIVE/   │       │   SMS/IN_APP)     │
        │   CANCELLED/      │       │ condition (JSON)  │
        │   EXPIRED)        │       │ is_active         │
        │ billing_cycle     │       │ last_triggered_at │
        │ start_date        │       │ created_at        │
        │ end_date          │       └──────────────────┘
        │ payment_provider  │
        │ external_sub_id   │       ┌──────────────────┐
        │ created_at        │       │  BatchJob         │
        └──────────────────┘       ├──────────────────┤
                                    │ id                │
        ┌──────────────────┐       │ job_name          │
        │  AdminAuditLog    │       │ job_type          │
        ├──────────────────┤       │ status (PENDING/  │
        │ id                │       │   RUNNING/DONE/   │
        │ admin_user_id(FK) │       │   FAILED)         │
        │ action            │       │ parameters (JSON) │
        │ target_entity     │       │ started_at        │
        │ target_id         │       │ completed_at      │
        │ details (JSON)    │       │ records_processed │
        │ timestamp         │       │ error_message     │
        └──────────────────┘       └──────────────────┘

        ┌──────────────────┐
        │  FeatureToggle    │
        ├──────────────────┤
        │ id                │
        │ feature_key       │
        │ is_enabled        │
        │ allowed_tiers     │
        │ updated_by (FK)   │
        │ updated_at        │
        └──────────────────┘
```

---

## 7. External Integrations

| Integration              | Purpose                                                    | Examples                                    |
|--------------------------|------------------------------------------------------------|---------------------------------------------|
| Market Data Provider     | Real-time and historical prices for equities, options      | Alpha Vantage, Polygon.io, Yahoo Finance    |
| SEC EDGAR API            | SEC filings (10-K, 10-Q, 8-K) retrieval                   | SEC EDGAR Full-Text Search API              |
| Financial Data API       | Financial statements, ratios, analyst estimates            | Financial Modeling Prep, IEX Cloud          |
| Crypto Data Provider     | Real-time and historical crypto prices, on-chain data      | CoinGecko, CoinMarketCap, Alchemy          |
| Brokerage API            | Trade execution for equities and options                   | Alpaca, Interactive Brokers, TD Ameritrade  |
| Crypto Exchange API      | Trade execution for cryptocurrencies                       | Coinbase Pro, Binance, Kraken               |
| Real Estate Valuation    | Property value estimates                                   | Zillow API, Redfin, ATTOM Data              |
| News & Sentiment         | News aggregation and sentiment scoring                     | NewsAPI, Finnhub, StockTwits                |
| FX Rates                 | Currency conversion rates                                  | Open Exchange Rates, Fixer.io               |
| Email Notification       | User notifications and alerts via email                    | SendGrid, AWS SES                           |
| SMS Notification         | User notifications and alerts via SMS                      | Twilio, AWS SNS                             |
| Google OAuth 2.0         | Social login via Google identity provider                  | Google Identity Services API                |
| Meta (Facebook) Login    | Social login via Meta identity provider                    | Facebook Login SDK / Graph API              |
| Payment Processing       | Subscription billing and payment management                | Stripe, PayPal                              |
| Log Aggregation          | Centralized log collection, indexing, and search           | Elasticsearch, Logstash, Kibana, OpenSearch |
| Service Discovery        | Microservice registration and discovery                    | Eureka, Consul, Kubernetes DNS              |

---

## 8. Glossary

| Term                     | Definition                                                                                          |
|--------------------------|-----------------------------------------------------------------------------------------------------|
| **Alpha**                | Excess return of an investment relative to a benchmark index, adjusted for risk.                    |
| **Beta**                 | Measure of an asset's volatility relative to the overall market (S&P 500). Beta of 1.0 means the asset moves with the market. |
| **Black-Scholes Model**  | Mathematical model for pricing European-style options contracts.                                    |
| **CAGR**                 | Compound Annual Growth Rate — the annualized rate of return over a period.                          |
| **Correlation Matrix**   | Table showing pairwise correlation coefficients between assets in a portfolio.                       |
| **CUSIP**                | Committee on Uniform Securities Identification Procedures — 9-character identifier for securities.  |
| **CVaR**                 | Conditional Value at Risk (Expected Shortfall) — the expected loss given that the loss exceeds VaR. |
| **DeFi**                 | Decentralized Finance — financial services built on blockchain protocols without intermediaries.     |
| **Greeks**               | Measures of sensitivity of an option's price to various factors (Delta, Gamma, Theta, Vega, Rho).  |
| **ISIN**                 | International Securities Identification Number — 12-character alphanumeric code for securities.     |
| **Maximum Drawdown**     | Largest peak-to-trough decline in portfolio value over a specified period.                           |
| **Monte Carlo Simulation**| Statistical technique using random sampling to model the probability of different outcomes.         |
| **OCC Symbol**           | Options Clearing Corporation standardized symbol for options contracts.                              |
| **REIT**                 | Real Estate Investment Trust — a company that owns income-producing real estate.                     |
| **Sharpe Ratio**         | Risk-adjusted return metric: (Portfolio Return − Risk-Free Rate) / Portfolio Std Dev.               |
| **Sortino Ratio**        | Similar to Sharpe but only penalizes downside volatility.                                           |
| **Treynor Ratio**        | Risk-adjusted return metric using beta: (Portfolio Return − Risk-Free Rate) / Beta.                 |
| **Value at Risk (VaR)**  | Maximum expected loss at a given confidence level over a specified time horizon.                     |
| **Volatility**           | Statistical measure of the dispersion of returns, typically annualized standard deviation.           |
| **API Gateway**          | Entry point for client requests in a microservices architecture that handles routing, authentication, and rate limiting. |
| **BDD**                  | Behavior-Driven Development — a testing methodology where tests are written in a human-readable format describing system behavior. |
| **Circuit Breaker**      | A design pattern that prevents cascading failures in distributed systems by stopping calls to a failing service. |
| **ELK Stack**            | Elasticsearch, Logstash, and Kibana — a popular stack for centralized log aggregation, search, and visualization. |
| **Feature Toggle**       | A technique to enable or disable features at runtime without deploying new code.                     |
| **Freemium**             | A business model offering basic features for free while charging for premium features.               |
| **JaCoCo**               | Java Code Coverage — a code coverage library for Java used to measure test coverage.                 |
| **Microservice**         | An architectural style where an application is composed of small, independently deployable services. |
| **Regression Test**      | A test suite that verifies previously working functionality still works after code changes.          |
| **Service Discovery**    | The mechanism by which microservices locate and communicate with each other in a distributed system. |
| **Spring Batch**         | A framework for building robust batch processing applications in Java/Spring.                        |

---

*End of Requirements Document*
