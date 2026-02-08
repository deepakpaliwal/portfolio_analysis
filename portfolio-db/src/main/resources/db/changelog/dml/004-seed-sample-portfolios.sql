--liquibase formatted sql

--changeset portfolio:dml-004-seed-sample-portfolios context:local,dev
--comment: Seed sample portfolios and holdings for development and testing

-- Portfolio for trader user (id=2)
INSERT INTO portfolios (user_id, name, description, base_currency) VALUES
(2, 'Tech Growth Portfolio', 'High-growth technology stocks focused on AI and cloud computing', 'USD'),
(2, 'Dividend Income Portfolio', 'Conservative dividend-paying blue chips for steady income', 'USD'),
(2, 'Crypto Portfolio', 'Diversified cryptocurrency holdings', 'USD');

-- Portfolio for free-tier viewer user (id=3)
INSERT INTO portfolios (user_id, name, description, base_currency) VALUES
(3, 'Starter Portfolio', 'Beginner portfolio with diversified ETFs', 'USD');

-- Holdings for Tech Growth Portfolio (portfolio_id=1)
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) VALUES
(1, 'STOCK', 'AAPL', 'Apple Inc.', 50.00000000, 175.5000, '2024-01-15', 'USD', 'Technology'),
(1, 'STOCK', 'MSFT', 'Microsoft Corporation', 30.00000000, 380.2500, '2024-02-01', 'USD', 'Technology'),
(1, 'STOCK', 'NVDA', 'NVIDIA Corporation', 25.00000000, 620.0000, '2024-03-10', 'USD', 'Technology'),
(1, 'STOCK', 'GOOGL', 'Alphabet Inc.', 40.00000000, 142.5000, '2024-01-20', 'USD', 'Technology'),
(1, 'STOCK', 'AMZN', 'Amazon.com Inc.', 20.00000000, 178.2500, '2024-04-05', 'USD', 'Consumer Discretionary');

-- Holdings for Dividend Income Portfolio (portfolio_id=2)
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) VALUES
(2, 'STOCK', 'JNJ', 'Johnson & Johnson', 100.00000000, 155.0000, '2023-06-15', 'USD', 'Healthcare'),
(2, 'STOCK', 'PG', 'Procter & Gamble', 80.00000000, 148.5000, '2023-07-01', 'USD', 'Consumer Staples'),
(2, 'STOCK', 'KO', 'Coca-Cola Company', 150.00000000, 58.7500, '2023-05-20', 'USD', 'Consumer Staples'),
(2, 'BOND', 'US10Y', 'US Treasury 10-Year Note', 10.00000000, 950.0000, '2023-09-01', 'USD', 'Fixed Income'),
(2, 'CASH', 'USD', 'US Dollar Cash', 15000.00000000, 1.0000, '2024-01-01', 'USD', 'Cash');

-- Holdings for Crypto Portfolio (portfolio_id=3)
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) VALUES
(3, 'CRYPTOCURRENCY', 'BTC', 'Bitcoin', 0.50000000, 42000.0000, '2024-01-10', 'USD', 'Cryptocurrency'),
(3, 'CRYPTOCURRENCY', 'ETH', 'Ethereum', 5.00000000, 2300.0000, '2024-02-15', 'USD', 'Cryptocurrency'),
(3, 'CRYPTOCURRENCY', 'SOL', 'Solana', 100.00000000, 95.0000, '2024-03-01', 'USD', 'Cryptocurrency');

-- Holdings for Starter Portfolio (portfolio_id=4)
INSERT INTO holdings (portfolio_id, asset_type, ticker, name, quantity, purchase_price, purchase_date, currency, sector) VALUES
(4, 'ETF', 'SPY', 'SPDR S&P 500 ETF', 10.00000000, 475.0000, '2024-01-05', 'USD', 'Broad Market'),
(4, 'ETF', 'QQQ', 'Invesco QQQ Trust', 8.00000000, 405.0000, '2024-02-10', 'USD', 'Technology'),
(4, 'ETF', 'BND', 'Vanguard Total Bond Market', 50.00000000, 72.5000, '2024-01-15', 'USD', 'Fixed Income');

-- Sample transactions
INSERT INTO transactions (holding_id, transaction_type, quantity, price, fees, executed_at, notes) VALUES
(1, 'BUY', 50.00000000, 175.5000, 4.99, '2024-01-15 10:30:00', 'Initial purchase of AAPL'),
(2, 'BUY', 30.00000000, 380.2500, 4.99, '2024-02-01 11:15:00', 'Initial purchase of MSFT'),
(3, 'BUY', 25.00000000, 620.0000, 4.99, '2024-03-10 09:45:00', 'Initial purchase of NVDA'),
(6, 'BUY', 100.00000000, 155.0000, 0.00, '2023-06-15 14:00:00', 'Initial purchase of JNJ'),
(6, 'DIVIDEND', 0.00000000, 1.19, 0.00, '2023-09-15 09:00:00', 'JNJ quarterly dividend Q3 2023'),
(6, 'DIVIDEND', 0.00000000, 1.19, 0.00, '2023-12-15 09:00:00', 'JNJ quarterly dividend Q4 2023'),
(12, 'BUY', 0.50000000, 42000.0000, 15.00, '2024-01-10 16:30:00', 'Initial BTC purchase');

--rollback DELETE FROM transactions; DELETE FROM holdings; DELETE FROM portfolios WHERE user_id IN (2,3);
