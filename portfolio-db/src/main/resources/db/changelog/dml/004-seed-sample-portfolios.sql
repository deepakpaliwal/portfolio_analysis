--liquibase formatted sql

--changeset portfolio:dml-004-seed-sample-portfolios context:local,dev
--comment: Seed sample portfolios and holdings for development and testing without relying on hard-coded IDs

-- Portfolios for trader user
INSERT INTO portfolios (user_id, name, description, base_currency)
SELECT u.id, v.name, v.description, v.base_currency
FROM users u
JOIN (
    VALUES
        ('Tech Growth Portfolio', 'High-growth technology stocks focused on AI and cloud computing', 'USD'),
        ('Dividend Income Portfolio', 'Conservative dividend-paying blue chips for steady income', 'USD'),
        ('Crypto Portfolio', 'Diversified cryptocurrency holdings', 'USD')
) AS v(name, description, base_currency) ON TRUE
WHERE u.email = 'trader@example.com';

-- Portfolio for free-tier viewer user
INSERT INTO portfolios (user_id, name, description, base_currency)
SELECT u.id, 'Starter Portfolio', 'Beginner portfolio with diversified ETFs', 'USD'
FROM users u
WHERE u.email = 'viewer@example.com';

-- Holdings for Tech Growth Portfolio
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector)
SELECT p.id, v.asset_type, v.ticker, v.name, v.quantity, v.purchase_price, v.purchase_date, v.currency, v.sector
FROM portfolios p
JOIN users u ON u.id = p.user_id
JOIN (
    VALUES
        ('STOCK', 'AAPL', 'Apple Inc.', 50.00000000, 175.5000, DATE '2024-01-15', 'USD', 'Technology'),
        ('STOCK', 'MSFT', 'Microsoft Corporation', 30.00000000, 380.2500, DATE '2024-02-01', 'USD', 'Technology'),
        ('STOCK', 'NVDA', 'NVIDIA Corporation', 25.00000000, 620.0000, DATE '2024-03-10', 'USD', 'Technology'),
        ('STOCK', 'GOOGL', 'Alphabet Inc.', 40.00000000, 142.5000, DATE '2024-01-20', 'USD', 'Technology'),
        ('STOCK', 'AMZN', 'Amazon.com Inc.', 20.00000000, 178.2500, DATE '2024-04-05', 'USD', 'Consumer Discretionary')
) AS v(asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) ON TRUE
WHERE u.email = 'trader@example.com' AND p.name = 'Tech Growth Portfolio';

-- Holdings for Dividend Income Portfolio
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector)
SELECT p.id, v.asset_type, v.ticker, v.name, v.quantity, v.purchase_price, v.purchase_date, v.currency, v.sector
FROM portfolios p
JOIN users u ON u.id = p.user_id
JOIN (
    VALUES
        ('STOCK', 'JNJ', 'Johnson & Johnson', 100.00000000, 155.0000, DATE '2023-06-15', 'USD', 'Healthcare'),
        ('STOCK', 'PG', 'Procter & Gamble', 80.00000000, 148.5000, DATE '2023-07-01', 'USD', 'Consumer Staples'),
        ('STOCK', 'KO', 'Coca-Cola Company', 150.00000000, 58.7500, DATE '2023-05-20', 'USD', 'Consumer Staples'),
        ('BOND', 'US10Y', 'US Treasury 10-Year Note', 10.00000000, 950.0000, DATE '2023-09-01', 'USD', 'Fixed Income'),
        ('CASH', 'USD', 'US Dollar Cash', 15000.00000000, 1.0000, DATE '2024-01-01', 'USD', 'Cash')
) AS v(asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) ON TRUE
WHERE u.email = 'trader@example.com' AND p.name = 'Dividend Income Portfolio';

-- Holdings for Crypto Portfolio
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector)
SELECT p.id, v.asset_type, v.ticker, v.name, v.quantity, v.purchase_price, v.purchase_date, v.currency, v.sector
FROM portfolios p
JOIN users u ON u.id = p.user_id
JOIN (
    VALUES
        ('CRYPTOCURRENCY', 'BTC', 'Bitcoin', 0.50000000, 42000.0000, DATE '2024-01-10', 'USD', 'Cryptocurrency'),
        ('CRYPTOCURRENCY', 'ETH', 'Ethereum', 5.00000000, 2300.0000, DATE '2024-02-15', 'USD', 'Cryptocurrency'),
        ('CRYPTOCURRENCY', 'SOL', 'Solana', 100.00000000, 95.0000, DATE '2024-03-01', 'USD', 'Cryptocurrency')
) AS v(asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) ON TRUE
WHERE u.email = 'trader@example.com' AND p.name = 'Crypto Portfolio';

-- Holdings for Starter Portfolio
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector)
SELECT p.id, v.asset_type, v.ticker, v.name, v.quantity, v.purchase_price, v.purchase_date, v.currency, v.sector
FROM portfolios p
JOIN users u ON u.id = p.user_id
JOIN (
    VALUES
        ('ETF', 'SPY', 'SPDR S&P 500 ETF', 10.00000000, 475.0000, DATE '2024-01-05', 'USD', 'Broad Market'),
        ('ETF', 'QQQ', 'Invesco QQQ Trust', 8.00000000, 405.0000, DATE '2024-02-10', 'USD', 'Technology'),
        ('ETF', 'BND', 'Vanguard Total Bond Market', 50.00000000, 72.5000, DATE '2024-01-15', 'USD', 'Fixed Income')
) AS v(asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) ON TRUE
WHERE u.email = 'viewer@example.com' AND p.name = 'Starter Portfolio';

-- Sample transactions
INSERT INTO transactions (holding_id, transaction_type, quantity, price, fees, executed_at, notes)
SELECT h.id, v.transaction_type, v.quantity, v.price, v.fees, v.executed_at, v.notes
FROM holdings h
JOIN portfolios p ON p.id = h.portfolio_id
JOIN users u ON u.id = p.user_id
JOIN (
    VALUES
        ('trader@example.com', 'Tech Growth Portfolio', 'AAPL', 'BUY', 50.00000000, 175.5000, 4.99, TIMESTAMP '2024-01-15 10:30:00', 'Initial purchase of AAPL'),
        ('trader@example.com', 'Tech Growth Portfolio', 'MSFT', 'BUY', 30.00000000, 380.2500, 4.99, TIMESTAMP '2024-02-01 11:15:00', 'Initial purchase of MSFT'),
        ('trader@example.com', 'Tech Growth Portfolio', 'NVDA', 'BUY', 25.00000000, 620.0000, 4.99, TIMESTAMP '2024-03-10 09:45:00', 'Initial purchase of NVDA'),
        ('trader@example.com', 'Dividend Income Portfolio', 'JNJ', 'BUY', 100.00000000, 155.0000, 0.00, TIMESTAMP '2023-06-15 14:00:00', 'Initial purchase of JNJ'),
        ('trader@example.com', 'Dividend Income Portfolio', 'JNJ', 'DIVIDEND', 0.00000000, 1.19, 0.00, TIMESTAMP '2023-09-15 09:00:00', 'JNJ quarterly dividend Q3 2023'),
        ('trader@example.com', 'Dividend Income Portfolio', 'JNJ', 'DIVIDEND', 0.00000000, 1.19, 0.00, TIMESTAMP '2023-12-15 09:00:00', 'JNJ quarterly dividend Q4 2023'),
        ('trader@example.com', 'Crypto Portfolio', 'BTC', 'BUY', 0.50000000, 42000.0000, 15.00, TIMESTAMP '2024-01-10 16:30:00', 'Initial BTC purchase')
) AS v(email, portfolio_name, ticker, transaction_type, quantity, price, fees, executed_at, notes)
    ON v.email = u.email AND v.portfolio_name = p.name AND v.ticker = h.ticker;

--rollback DELETE FROM transactions; DELETE FROM holdings; DELETE FROM portfolios WHERE user_id IN (SELECT id FROM users WHERE email IN ('trader@example.com', 'viewer@example.com'));
