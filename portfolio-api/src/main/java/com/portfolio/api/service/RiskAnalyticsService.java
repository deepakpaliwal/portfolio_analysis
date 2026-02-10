package com.portfolio.api.service;

import com.portfolio.api.dto.RiskAnalyticsResponse;
import com.portfolio.api.dto.RiskAnalyticsResponse.*;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.repository.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Risk analytics computation engine (FR-RA-001 through FR-RA-012).
 *
 * Fetches historical prices via Finnhub candle data, then computes:
 * VaR (Historical Simulation, Parametric, Monte Carlo), CVaR, volatility,
 * beta, alpha, Sharpe/Sortino/Treynor ratios, max drawdown, and stress tests.
 */
@Service
public class RiskAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(RiskAnalyticsService.class);
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final int SCALE = 8;
    private static final String BENCHMARK = "SPY";
    private static final double RISK_FREE_RATE_ANNUAL = 0.05; // ~5% risk-free rate
    private static final int MONTE_CARLO_SIMULATIONS = 10_000;

    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;

    public RiskAnalyticsService(PortfolioRepository portfolioRepository, MarketDataService marketDataService) {
        this.portfolioRepository = portfolioRepository;
        this.marketDataService = marketDataService;
    }

    /**
     * Compute full risk analytics for a portfolio.
     *
     * @param portfolioId     portfolio to analyze
     * @param username        owner username for authorization
     * @param confidenceLevel e.g. 0.95
     * @param timeHorizonDays e.g. 1, 10, 30
     * @param lookbackDays    e.g. 252 (1 year)
     */
    public RiskAnalyticsResponse computeRiskAnalytics(Long portfolioId, String username,
                                                       double confidenceLevel, int timeHorizonDays, int lookbackDays) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new SecurityException("Access denied");
        }

        List<Holding> holdings = portfolio.getHoldings();
        if (holdings == null || holdings.isEmpty()) {
            throw new IllegalStateException("Portfolio has no holdings");
        }

        // Filter to STOCK holdings with valid tickers
        List<Holding> stockHoldings = holdings.stream()
                .filter(h -> h.getTicker() != null && !h.getTicker().isBlank())
                .toList();

        if (stockHoldings.isEmpty()) {
            throw new IllegalStateException("Portfolio has no stock holdings for risk analysis");
        }

        // Time range for historical data
        long now = Instant.now().getEpochSecond();
        long from = now - ((long) lookbackDays * 24 * 60 * 60);

        // Fetch closing prices for each holding and benchmark
        Map<String, double[]> closingPrices = new LinkedHashMap<>();
        for (Holding h : stockHoldings) {
            double[] prices = fetchClosingPrices(h.getTicker(), from, now);
            if (prices != null && prices.length > 1) {
                closingPrices.put(h.getTicker(), prices);
            }
        }

        double[] benchmarkPrices = fetchClosingPrices(BENCHMARK, from, now);

        if (closingPrices.isEmpty()) {
            throw new IllegalStateException("Could not fetch historical prices for any holding");
        }

        // Compute current market values and weights
        Map<String, BigDecimal> marketValues = new LinkedHashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Holding h : stockHoldings) {
            if (!closingPrices.containsKey(h.getTicker())) continue;
            BigDecimal price = marketDataService.getCurrentPrice(h.getTicker());
            if (price == null) {
                double[] prices = closingPrices.get(h.getTicker());
                price = BigDecimal.valueOf(prices[prices.length - 1]);
            }
            BigDecimal mv = price.multiply(h.getQuantity(), MC);
            marketValues.put(h.getTicker(), mv);
            totalValue = totalValue.add(mv);
        }

        Map<String, Double> weights = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> e : marketValues.entrySet()) {
            weights.put(e.getKey(), totalValue.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : e.getValue().divide(totalValue, SCALE, RoundingMode.HALF_UP).doubleValue());
        }

        // Align all return series to the same minimum length
        int minLen = closingPrices.values().stream().mapToInt(a -> a.length).min().orElse(0);
        if (benchmarkPrices != null) {
            minLen = Math.min(minLen, benchmarkPrices.length);
        }

        // Compute daily returns for each holding
        Map<String, double[]> holdingReturns = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> entry : closingPrices.entrySet()) {
            holdingReturns.put(entry.getKey(), computeReturns(entry.getValue(), minLen));
        }

        double[] benchmarkReturns = benchmarkPrices != null ? computeReturns(benchmarkPrices, minLen) : null;

        // Compute portfolio daily returns (weighted sum)
        int numReturns = holdingReturns.values().stream().mapToInt(a -> a.length).min().orElse(0);
        double[] portfolioReturns = new double[numReturns];
        for (Map.Entry<String, double[]> entry : holdingReturns.entrySet()) {
            double w = weights.getOrDefault(entry.getKey(), 0.0);
            double[] rets = entry.getValue();
            for (int i = 0; i < numReturns; i++) {
                portfolioReturns[i] += w * rets[i];
            }
        }

        // Trim benchmark returns to match
        if (benchmarkReturns != null && benchmarkReturns.length > numReturns) {
            benchmarkReturns = Arrays.copyOfRange(benchmarkReturns,
                    benchmarkReturns.length - numReturns, benchmarkReturns.length);
        }

        // ── Build response ──
        RiskAnalyticsResponse resp = new RiskAnalyticsResponse();
        resp.setPortfolioId(portfolioId);
        resp.setPortfolioName(portfolio.getName());
        resp.setPortfolioValue(totalValue.setScale(2, RoundingMode.HALF_UP));
        resp.setBaseCurrency(portfolio.getBaseCurrency());
        resp.setConfidenceLevel(confidenceLevel);
        resp.setTimeHorizonDays(timeHorizonDays);
        resp.setLookbackDays(lookbackDays);

        // Volatility (FR-RA-004)
        double dailyVol = standardDeviation(portfolioReturns);
        double annualVol = dailyVol * Math.sqrt(252);
        resp.setDailyVolatility(bd(dailyVol));
        resp.setAnnualizedVolatility(bd(annualVol));

        // VaR (FR-RA-001, FR-RA-002)
        double sqrtT = Math.sqrt(timeHorizonDays);
        VaRMetrics var = new VaRMetrics();
        var.setHistoricalSimulation(bd(historicalVaR(portfolioReturns, confidenceLevel, timeHorizonDays, totalValue.doubleValue())));
        var.setParametric(bd(parametricVaR(dailyVol, confidenceLevel, timeHorizonDays, totalValue.doubleValue())));
        var.setMonteCarlo(bd(monteCarloVaR(portfolioReturns, confidenceLevel, timeHorizonDays, totalValue.doubleValue())));
        resp.setVar(var);

        // CVaR (FR-RA-003)
        resp.setCvar95(bd(computeCVaR(portfolioReturns, 0.95, timeHorizonDays, totalValue.doubleValue())));
        resp.setCvar99(bd(computeCVaR(portfolioReturns, 0.99, timeHorizonDays, totalValue.doubleValue())));

        // Beta (FR-RA-005)
        if (benchmarkReturns != null && benchmarkReturns.length == numReturns) {
            double portBeta = computeBeta(portfolioReturns, benchmarkReturns);
            resp.setPortfolioBeta(bd(portBeta));

            List<HoldingBeta> holdingBetaList = new ArrayList<>();
            for (Map.Entry<String, double[]> entry : holdingReturns.entrySet()) {
                double[] rets = entry.getValue();
                double[] alignedBench = benchmarkReturns;
                if (rets.length != alignedBench.length) {
                    int len = Math.min(rets.length, alignedBench.length);
                    rets = Arrays.copyOfRange(rets, rets.length - len, rets.length);
                    alignedBench = Arrays.copyOfRange(alignedBench, alignedBench.length - len, alignedBench.length);
                }
                HoldingBeta hb = new HoldingBeta();
                hb.setTicker(entry.getKey());
                Holding holding = stockHoldings.stream()
                        .filter(h -> h.getTicker().equals(entry.getKey())).findFirst().orElse(null);
                hb.setName(holding != null ? holding.getName() : entry.getKey());
                hb.setBeta(bd(computeBeta(rets, alignedBench)));
                hb.setWeight(bd(weights.getOrDefault(entry.getKey(), 0.0)));
                holdingBetaList.add(hb);
            }
            resp.setHoldingBetas(holdingBetaList);

            // Alpha (FR-RA-006) — Jensen's alpha = Rp - [Rf + Beta * (Rm - Rf)]
            double dailyRf = RISK_FREE_RATE_ANNUAL / 252;
            double avgPortReturn = mean(portfolioReturns) * 252; // annualized
            double avgBenchReturn = mean(benchmarkReturns) * 252;
            double alpha = avgPortReturn - (RISK_FREE_RATE_ANNUAL + portBeta * (avgBenchReturn - RISK_FREE_RATE_ANNUAL));
            resp.setPortfolioAlpha(bd(alpha));

            // Treynor ratio (FR-RA-007) = (Rp - Rf) / Beta
            if (Math.abs(portBeta) > 1e-10) {
                resp.setTreynorRatio(bd((avgPortReturn - RISK_FREE_RATE_ANNUAL) / portBeta));
            }
        }

        // Sharpe ratio (FR-RA-007) = (Rp - Rf) / sigma_p
        double avgPortReturnAnn = mean(portfolioReturns) * 252;
        if (annualVol > 1e-10) {
            resp.setSharpeRatio(bd((avgPortReturnAnn - RISK_FREE_RATE_ANNUAL) / annualVol));
        }

        // Sortino ratio (FR-RA-007) = (Rp - Rf) / downside_deviation
        double downsideDev = downsideDeviation(portfolioReturns) * Math.sqrt(252);
        if (downsideDev > 1e-10) {
            resp.setSortinoRatio(bd((avgPortReturnAnn - RISK_FREE_RATE_ANNUAL) / downsideDev));
        }

        // Max drawdown (FR-RA-008)
        computeMaxDrawdown(portfolioReturns, closingPrices, from, resp);

        // Stress testing (FR-RA-009)
        resp.setStressTests(buildStressScenarios(totalValue.doubleValue(), resp.getPortfolioBeta()));

        // Monte Carlo distribution (part of FR-RA-001)
        MonteCarloResult mcResult = runMonteCarloDistribution(portfolioReturns, timeHorizonDays);
        resp.setMonteCarlo(mcResult);

        return resp;
    }

    // ── Historical prices ──

    private double[] fetchClosingPrices(String ticker, long from, long to) {
        try {
            Map<String, Object> candles = marketDataService.getStockCandles(ticker, "D", from, to);
            if (candles == null || candles.get("c") == null) return null;

            @SuppressWarnings("unchecked")
            List<Number> closes = (List<Number>) candles.get("c");
            return closes.stream().mapToDouble(Number::doubleValue).toArray();
        } catch (Exception e) {
            log.warn("Failed to fetch closing prices for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    private double[] computeReturns(double[] prices, int maxLen) {
        // Trim prices to maxLen from the end
        int start = Math.max(0, prices.length - maxLen);
        int n = prices.length - start;
        double[] returns = new double[n - 1];
        for (int i = 1; i < n; i++) {
            double prev = prices[start + i - 1];
            returns[i - 1] = prev == 0 ? 0 : (prices[start + i] - prev) / prev;
        }
        return returns;
    }

    // ── VaR Methods (FR-RA-001) ──

    private double historicalVaR(double[] returns, double confidence, int horizon, double portfolioValue) {
        double[] sorted = returns.clone();
        Arrays.sort(sorted);
        int index = (int) Math.floor((1 - confidence) * sorted.length);
        index = Math.max(0, Math.min(index, sorted.length - 1));
        double dailyVaR = -sorted[index];
        return dailyVaR * Math.sqrt(horizon) * portfolioValue;
    }

    private double parametricVaR(double dailyVol, double confidence, int horizon, double portfolioValue) {
        double zScore = getZScore(confidence);
        return zScore * dailyVol * Math.sqrt(horizon) * portfolioValue;
    }

    private double monteCarloVaR(double[] returns, double confidence, int horizon, double portfolioValue) {
        double mu = mean(returns);
        double sigma = standardDeviation(returns);
        Random rng = new Random(42); // fixed seed for reproducibility

        double[] simReturns = new double[MONTE_CARLO_SIMULATIONS];
        for (int i = 0; i < MONTE_CARLO_SIMULATIONS; i++) {
            double cumReturn = 0;
            for (int d = 0; d < horizon; d++) {
                cumReturn += mu + sigma * rng.nextGaussian();
            }
            simReturns[i] = cumReturn;
        }

        Arrays.sort(simReturns);
        int index = (int) Math.floor((1 - confidence) * MONTE_CARLO_SIMULATIONS);
        index = Math.max(0, Math.min(index, MONTE_CARLO_SIMULATIONS - 1));
        return -simReturns[index] * portfolioValue;
    }

    // ── CVaR / Expected Shortfall (FR-RA-003) ──

    private double computeCVaR(double[] returns, double confidence, int horizon, double portfolioValue) {
        double[] sorted = returns.clone();
        Arrays.sort(sorted);
        int cutoff = (int) Math.floor((1 - confidence) * sorted.length);
        cutoff = Math.max(1, cutoff);
        double sum = 0;
        for (int i = 0; i < cutoff; i++) {
            sum += sorted[i];
        }
        double avgTailLoss = -sum / cutoff;
        return avgTailLoss * Math.sqrt(horizon) * portfolioValue;
    }

    // ── Beta (FR-RA-005) ──

    private double computeBeta(double[] assetReturns, double[] benchmarkReturns) {
        int n = Math.min(assetReturns.length, benchmarkReturns.length);
        if (n < 2) return 1.0;

        double meanA = mean(assetReturns, n);
        double meanB = mean(benchmarkReturns, n);
        double cov = 0, varB = 0;
        for (int i = 0; i < n; i++) {
            double diffA = assetReturns[i] - meanA;
            double diffB = benchmarkReturns[i] - meanB;
            cov += diffA * diffB;
            varB += diffB * diffB;
        }
        return varB == 0 ? 1.0 : cov / varB;
    }

    // ── Max Drawdown (FR-RA-008) ──

    @SuppressWarnings("unchecked")
    private void computeMaxDrawdown(double[] portfolioReturns, Map<String, double[]> closingPrices,
                                     long fromEpoch, RiskAnalyticsResponse resp) {
        // Reconstruct portfolio value series from returns
        double[] valueSeries = new double[portfolioReturns.length + 1];
        valueSeries[0] = 1.0;
        for (int i = 0; i < portfolioReturns.length; i++) {
            valueSeries[i + 1] = valueSeries[i] * (1 + portfolioReturns[i]);
        }

        double maxDD = 0;
        int peakIdx = 0, troughIdx = 0;
        double peak = valueSeries[0];
        int currentPeakIdx = 0;

        for (int i = 1; i < valueSeries.length; i++) {
            if (valueSeries[i] > peak) {
                peak = valueSeries[i];
                currentPeakIdx = i;
            }
            double dd = (peak - valueSeries[i]) / peak;
            if (dd > maxDD) {
                maxDD = dd;
                peakIdx = currentPeakIdx;
                troughIdx = i;
            }
        }

        resp.setMaxDrawdown(bd(maxDD));

        // Estimate dates from indices
        LocalDate startDate = LocalDate.ofInstant(Instant.ofEpochSecond(fromEpoch), ZoneOffset.UTC);
        resp.setMaxDrawdownPeakDate(startDate.plusDays(peakIdx).format(DateTimeFormatter.ISO_LOCAL_DATE));
        resp.setMaxDrawdownTroughDate(startDate.plusDays(troughIdx).format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    // ── Stress Testing (FR-RA-009) ──

    private List<StressScenario> buildStressScenarios(double portfolioValue, BigDecimal portfolioBeta) {
        double beta = portfolioBeta != null ? portfolioBeta.doubleValue() : 1.0;
        List<StressScenario> scenarios = new ArrayList<>();

        // Historical scenarios with their approximate S&P 500 peak-to-trough declines
        scenarios.add(buildScenario("2008 Financial Crisis",
                "Simulates the 2007-2009 subprime mortgage crisis and bank failures",
                -56.8, beta, portfolioValue));
        scenarios.add(buildScenario("COVID-19 Crash (2020)",
                "Simulates the rapid market selloff in Feb-Mar 2020",
                -33.9, beta, portfolioValue));
        scenarios.add(buildScenario("Dot-com Bubble (2000-2002)",
                "Simulates the technology bubble burst of early 2000s",
                -49.1, beta, portfolioValue));
        scenarios.add(buildScenario("Black Monday (1987)",
                "Simulates the Oct 19, 1987 single-day market crash",
                -22.6, beta, portfolioValue));
        scenarios.add(buildScenario("Interest Rate Shock (+300bps)",
                "Simulates a sudden 300 basis point increase in interest rates",
                -20.0, beta, portfolioValue));

        return scenarios;
    }

    private StressScenario buildScenario(String name, String description, double marketShock,
                                          double beta, double portfolioValue) {
        double estimatedLossPct = marketShock * beta / 100.0;
        double estimatedLoss = Math.abs(estimatedLossPct) * portfolioValue;

        StressScenario s = new StressScenario();
        s.setName(name);
        s.setDescription(description);
        s.setMarketShockPercent(bd(marketShock));
        s.setEstimatedLoss(bd(estimatedLoss).setScale(2, RoundingMode.HALF_UP));
        s.setEstimatedLossPercent(bd(estimatedLossPct * 100).setScale(2, RoundingMode.HALF_UP));
        return s;
    }

    // ── Monte Carlo Distribution (FR-RA-001) ──

    private MonteCarloResult runMonteCarloDistribution(double[] returns, int horizon) {
        double mu = mean(returns);
        double sigma = standardDeviation(returns);
        Random rng = new Random(42);

        double[] endReturns = new double[MONTE_CARLO_SIMULATIONS];
        for (int i = 0; i < MONTE_CARLO_SIMULATIONS; i++) {
            double cumReturn = 0;
            for (int d = 0; d < horizon; d++) {
                cumReturn += mu + sigma * rng.nextGaussian();
            }
            endReturns[i] = cumReturn;
        }
        Arrays.sort(endReturns);

        MonteCarloResult mc = new MonteCarloResult();
        mc.setSimulations(MONTE_CARLO_SIMULATIONS);
        mc.setMeanReturn(bd(mean(endReturns)));
        mc.setPercentile5(bd(percentile(endReturns, 5)));
        mc.setPercentile25(bd(percentile(endReturns, 25)));
        mc.setMedian(bd(percentile(endReturns, 50)));
        mc.setPercentile75(bd(percentile(endReturns, 75)));
        mc.setPercentile95(bd(percentile(endReturns, 95)));
        return mc;
    }

    // ── Statistical helpers ──

    private double mean(double[] data) {
        return mean(data, data.length);
    }

    private double mean(double[] data, int n) {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += data[i];
        return n == 0 ? 0 : sum / n;
    }

    private double standardDeviation(double[] data) {
        if (data.length < 2) return 0;
        double m = mean(data);
        double sumSq = 0;
        for (double d : data) {
            sumSq += (d - m) * (d - m);
        }
        return Math.sqrt(sumSq / (data.length - 1));
    }

    private double downsideDeviation(double[] returns) {
        double dailyRf = RISK_FREE_RATE_ANNUAL / 252;
        double sumSq = 0;
        int count = 0;
        for (double r : returns) {
            double diff = r - dailyRf;
            if (diff < 0) {
                sumSq += diff * diff;
                count++;
            }
        }
        return count < 2 ? 0 : Math.sqrt(sumSq / (count - 1));
    }

    private double percentile(double[] sortedData, double pct) {
        double idx = (pct / 100.0) * (sortedData.length - 1);
        int lower = (int) Math.floor(idx);
        int upper = Math.min(lower + 1, sortedData.length - 1);
        double frac = idx - lower;
        return sortedData[lower] + frac * (sortedData[upper] - sortedData[lower]);
    }

    private double getZScore(double confidence) {
        if (confidence >= 0.99) return 2.326;
        if (confidence >= 0.95) return 1.645;
        if (confidence >= 0.90) return 1.282;
        return 1.645; // default to 95%
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }
}
