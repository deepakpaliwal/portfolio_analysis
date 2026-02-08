--liquibase formatted sql

--changeset portfolio:dml-001-seed-default-strategies
--comment: Seed default investment and trading strategies
INSERT INTO strategies (name, type, parameters, description, is_active) VALUES
('Value Investing', 'VALUE', '{"peRatioMax": 15, "pbRatioMax": 1.5, "dividendYieldMin": 2.0}', 'Screen for undervalued stocks based on fundamental metrics like P/E ratio, P/B ratio, and dividend yield.', TRUE),
('Growth Investing', 'GROWTH', '{"revenueGrowthMin": 15, "earningsGrowthMin": 20, "lookbackYears": 3}', 'Identify high-growth companies with strong revenue and earnings growth trajectories.', TRUE),
('Dividend Income', 'DIVIDEND', '{"dividendYieldMin": 3.0, "payoutRatioMax": 80, "consecutiveYearsMin": 5}', 'Focus on high-yield, consistent dividend payers for income generation.', TRUE),
('Momentum Trading', 'MOMENTUM', '{"lookbackDays": 90, "rsiOverbought": 70, "rsiOversold": 30}', 'Trend-following strategy based on technical indicators and price momentum.', TRUE),
('Mean Reversion', 'MEAN_REVERSION', '{"lookbackDays": 20, "stdDevThreshold": 2.0, "rsiThreshold": 30}', 'Buy oversold assets and sell overbought assets based on statistical deviation from the mean.', TRUE),
('Covered Call Writing', 'COVERED_CALL', '{"daysToExpiry": 30, "deltaTarget": 0.3, "premiumMinPct": 1.0}', 'Generate income by writing covered calls on existing equity positions.', TRUE),
('Pairs Trading', 'PAIRS', '{"correlationMin": 0.8, "divergenceThreshold": 2.0, "lookbackDays": 60}', 'Long/short correlated pairs when they diverge from their historical relationship.', TRUE),
('Risk Parity', 'RISK_PARITY', '{"rebalanceFrequency": "MONTHLY", "riskBudget": "EQUAL"}', 'Allocate portfolio weights to achieve equal risk contribution from each asset.', TRUE),
('Dollar-Cost Averaging', 'DCA', '{"frequency": "WEEKLY", "allocationPct": 100}', 'Systematic periodic investment to reduce timing risk and average purchase cost.', TRUE),
('Crypto Yield Farming', 'YIELD_FARM', '{"minAPY": 5.0, "maxTVLRank": 50, "protocols": "AAVE,COMPOUND,UNISWAP"}', 'DeFi staking and liquidity pool strategies for generating yield on crypto assets.', TRUE);

--rollback DELETE FROM strategies WHERE type IN ('VALUE','GROWTH','DIVIDEND','MOMENTUM','MEAN_REVERSION','COVERED_CALL','PAIRS','RISK_PARITY','DCA','YIELD_FARM');
