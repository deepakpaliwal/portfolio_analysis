package com.portfolio.api.service;

import com.portfolio.api.dto.CorrelationAnalysisResponse;
import com.portfolio.api.dto.CorrelationAnalysisResponse.*;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.repository.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Correlation and hedging analysis engine (FR-CH-001 through FR-CH-008).
 *
 * Computes pairwise correlation matrices, identifies concentration risk,
 * suggests hedge instruments, calculates rolling correlations, and
 * scores portfolio diversification.
 */
@Service
public class CorrelationService {

    private static final Logger log = LoggerFactory.getLogger(CorrelationService.class);
    private static final double HIGH_CORR_THRESHOLD = 0.7;
    private static final double NEGATIVE_CORR_THRESHOLD = -0.3;

    private final PortfolioRepository portfolioRepository;
    private final StockPriceHistoryService priceHistoryService;

    public CorrelationService(PortfolioRepository portfolioRepository,
                               StockPriceHistoryService priceHistoryService) {
        this.portfolioRepository = portfolioRepository;
        this.priceHistoryService = priceHistoryService;
    }

    /**
     * Compute full correlation analysis for a portfolio.
     */
    @Transactional(readOnly = true)
    public CorrelationAnalysisResponse analyzeCorrelation(Long portfolioId, String username, int lookbackDays) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (!portfolio.getUser().getEmail().equals(username)) {
            throw new SecurityException("Access denied");
        }

        // Filter to stock/ETF holdings with tickers
        List<Holding> stockHoldings = portfolio.getHoldings().stream()
                .filter(h -> h.getTicker() != null && !h.getTicker().isBlank())
                .filter(h -> {
                    String type = h.getAssetType() != null ? h.getAssetType().name() : "";
                    return "STOCK".equals(type) || "ETF".equals(type);
                })
                .toList();

        if (stockHoldings.size() < 2) {
            throw new IllegalStateException("Need at least 2 stock/ETF holdings for correlation analysis");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(lookbackDays);

        // Collect return series for each holding
        List<String> tickers = new ArrayList<>();
        List<String> tickerNames = new ArrayList<>();
        Map<String, double[]> returnSeries = new LinkedHashMap<>();

        for (Holding h : stockHoldings) {
            double[] prices = priceHistoryService.getClosingPrices(h.getTicker(), startDate, endDate);
            if (prices.length > 1) {
                double[] returns = computeReturns(prices);
                tickers.add(h.getTicker());
                tickerNames.add(h.getName() != null ? h.getName() : h.getTicker());
                returnSeries.put(h.getTicker(), returns);
            } else {
                log.warn("Insufficient price data for {} — skipping", h.getTicker());
            }
        }

        if (returnSeries.size() < 2) {
            throw new IllegalStateException(
                    "Need price data for at least 2 holdings. Sync prices via Batch Prices page first.");
        }

        // Align all return series to the same length (min)
        int minLen = returnSeries.values().stream().mapToInt(a -> a.length).min().orElse(0);
        for (Map.Entry<String, double[]> e : returnSeries.entrySet()) {
            double[] r = e.getValue();
            if (r.length > minLen) {
                e.setValue(Arrays.copyOfRange(r, r.length - minLen, r.length));
            }
        }

        int n = tickers.size();
        double[][] allReturns = new double[n][];
        for (int i = 0; i < n; i++) {
            allReturns[i] = returnSeries.get(tickers.get(i));
        }

        // FR-CH-001: Compute correlation matrix
        double[][] matrix = computeCorrelationMatrix(allReturns);

        // FR-CH-003: Identify highly correlated pairs
        List<CorrelatedPair> highlyCorrelated = new ArrayList<>();
        List<CorrelatedPair> negativelyCorrelated = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double corr = matrix[i][j];

                if (corr >= HIGH_CORR_THRESHOLD) {
                    CorrelatedPair pair = new CorrelatedPair();
                    pair.setTicker1(tickers.get(i));
                    pair.setTicker2(tickers.get(j));
                    pair.setName1(tickerNames.get(i));
                    pair.setName2(tickerNames.get(j));
                    pair.setCorrelation(bd(corr));
                    pair.setRiskLevel(corr >= 0.9 ? "Very High" : "High");
                    highlyCorrelated.add(pair);
                }

                // FR-CH-004: Negatively correlated pairs
                if (corr <= NEGATIVE_CORR_THRESHOLD) {
                    CorrelatedPair pair = new CorrelatedPair();
                    pair.setTicker1(tickers.get(i));
                    pair.setTicker2(tickers.get(j));
                    pair.setName1(tickerNames.get(i));
                    pair.setName2(tickerNames.get(j));
                    pair.setCorrelation(bd(corr));
                    pair.setRiskLevel(corr <= -0.7 ? "Strong Hedge" : "Moderate Hedge");
                    negativelyCorrelated.add(pair);
                }
            }
        }

        // Sort by correlation strength
        highlyCorrelated.sort((a, b) -> b.getCorrelation().compareTo(a.getCorrelation()));
        negativelyCorrelated.sort((a, b) -> a.getCorrelation().compareTo(b.getCorrelation()));

        // FR-CH-005: Hedge suggestions
        List<HedgeSuggestion> hedgeSuggestions = generateHedgeSuggestions(tickers, tickerNames, stockHoldings);

        // FR-CH-006: Rolling correlations for top pairs
        Map<String, RollingCorrelation> rollingCorrelations = new LinkedHashMap<>();
        List<int[]> topPairs = getTopPairs(matrix, n, 5);
        for (int[] pair : topPairs) {
            String key = tickers.get(pair[0]) + "/" + tickers.get(pair[1]);
            RollingCorrelation rc = computeRollingCorrelation(
                    tickers.get(pair[0]), tickers.get(pair[1]),
                    returnSeries.get(tickers.get(pair[0])),
                    returnSeries.get(tickers.get(pair[1])),
                    endDate, startDate);
            rollingCorrelations.put(key, rc);
        }

        // FR-CH-007: Diversification score
        double avgOffDiagonal = computeAverageAbsCorrelation(matrix, n);
        double diversScore = Math.max(0, Math.min(100, (1.0 - avgOffDiagonal) * 100));
        String diversRating = diversScore >= 80 ? "Excellent" :
                diversScore >= 60 ? "Good" :
                diversScore >= 40 ? "Moderate" :
                diversScore >= 20 ? "Poor" : "Very Poor";

        // Build response
        CorrelationAnalysisResponse resp = new CorrelationAnalysisResponse();
        resp.setPortfolioId(portfolioId);
        resp.setPortfolioName(portfolio.getName());
        resp.setHoldingCount(n);
        resp.setLookbackDays(lookbackDays);
        resp.setTickers(tickers);
        resp.setTickerNames(tickerNames);
        resp.setCorrelationMatrix(roundMatrix(matrix));
        resp.setHighlyCorrelatedPairs(highlyCorrelated);
        resp.setNegativelyCorrelatedPairs(negativelyCorrelated);
        resp.setHedgeSuggestions(hedgeSuggestions);
        resp.setRollingCorrelations(rollingCorrelations);
        resp.setDiversificationScore(bd(diversScore));
        resp.setDiversificationRating(diversRating);

        return resp;
    }

    // ── Correlation Matrix (FR-CH-001) ──

    private double[][] computeCorrelationMatrix(double[][] allReturns) {
        int n = allReturns.length;
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0;
            for (int j = i + 1; j < n; j++) {
                double corr = pearsonCorrelation(allReturns[i], allReturns[j]);
                matrix[i][j] = corr;
                matrix[j][i] = corr;
            }
        }

        return matrix;
    }

    private double pearsonCorrelation(double[] x, double[] y) {
        int n = Math.min(x.length, y.length);
        if (n < 2) return 0;

        double meanX = mean(x, n);
        double meanY = mean(y, n);

        double sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            sumXY += dx * dy;
            sumX2 += dx * dx;
            sumY2 += dy * dy;
        }

        double denom = Math.sqrt(sumX2 * sumY2);
        return denom == 0 ? 0 : sumXY / denom;
    }

    // ── Rolling Correlations (FR-CH-006) ──

    private RollingCorrelation computeRollingCorrelation(String ticker1, String ticker2,
                                                          double[] returns1, double[] returns2,
                                                          LocalDate endDate, LocalDate startDate) {
        RollingCorrelation rc = new RollingCorrelation();
        rc.setTicker1(ticker1);
        rc.setTicker2(ticker2);

        int len = Math.min(returns1.length, returns2.length);

        // 30-day rolling
        if (len >= 30) {
            double[] r1 = Arrays.copyOfRange(returns1, len - 30, len);
            double[] r2 = Arrays.copyOfRange(returns2, len - 30, len);
            rc.setCorrelation30d(bd(pearsonCorrelation(r1, r2)));
        }

        // 90-day rolling
        if (len >= 90) {
            double[] r1 = Arrays.copyOfRange(returns1, len - 90, len);
            double[] r2 = Arrays.copyOfRange(returns2, len - 90, len);
            rc.setCorrelation90d(bd(pearsonCorrelation(r1, r2)));
        }

        // 1-year (252 trading days) or full period
        int yearLen = Math.min(252, len);
        double[] r1y = Arrays.copyOfRange(returns1, len - yearLen, len);
        double[] r2y = Arrays.copyOfRange(returns2, len - yearLen, len);
        rc.setCorrelation1y(bd(pearsonCorrelation(r1y, r2y)));

        // Trend: compare 30d vs 1y
        if (rc.getCorrelation30d() != null && rc.getCorrelation1y() != null) {
            double diff = rc.getCorrelation30d().doubleValue() - rc.getCorrelation1y().doubleValue();
            if (diff > 0.1) rc.setTrend("Increasing");
            else if (diff < -0.1) rc.setTrend("Decreasing");
            else rc.setTrend("Stable");
        } else {
            rc.setTrend("N/A");
        }

        return rc;
    }

    // ── Hedge Suggestions (FR-CH-005) ──

    private List<HedgeSuggestion> generateHedgeSuggestions(List<String> tickers,
                                                            List<String> tickerNames,
                                                            List<Holding> holdings) {
        List<HedgeSuggestion> suggestions = new ArrayList<>();

        // Sector-based hedge mapping: sector → inverse ETF / hedge instruments
        Map<String, String[]> sectorHedges = Map.of(
                "Technology", new String[]{"SH", "Short S&P 500 ETF — broad market hedge", "-0.95"},
                "Healthcare", new String[]{"RWM", "Short Russell 2000 ETF — small-cap hedge", "-0.85"},
                "Financials", new String[]{"SKF", "UltraShort Financials — sector inverse ETF", "-0.90"},
                "Energy", new String[]{"ERY", "Direxion Daily Energy Bear — energy inverse ETF", "-0.90"},
                "Consumer Discretionary", new String[]{"SH", "Short S&P 500 ETF — broad consumer hedge", "-0.80"},
                "Industrials", new String[]{"SH", "Short S&P 500 ETF — broad industrial hedge", "-0.80"},
                "Materials", new String[]{"SH", "Short S&P 500 ETF — broad materials hedge", "-0.75"},
                "Utilities", new String[]{"TBT", "Short 20+ Year Treasury — rates inverse", "-0.60"},
                "Real Estate", new String[]{"SRS", "UltraShort Real Estate — REIT inverse ETF", "-0.85"}
        );

        Set<String> suggestedInstruments = new HashSet<>();

        for (int i = 0; i < tickers.size(); i++) {
            Holding h = holdings.stream()
                    .filter(ho -> ho.getTicker().equals(tickers.get(i)))
                    .findFirst().orElse(null);
            if (h == null) continue;

            String sector = h.getSector() != null ? h.getSector() : "Unknown";

            // Sector-specific inverse ETF
            if (sectorHedges.containsKey(sector)) {
                String[] hedge = sectorHedges.get(sector);
                if (suggestedInstruments.add(hedge[0] + "-" + tickers.get(i))) {
                    HedgeSuggestion s = new HedgeSuggestion();
                    s.setHoldingTicker(tickers.get(i));
                    s.setHoldingName(tickerNames.get(i));
                    s.setHedgeType("Inverse ETF");
                    s.setHedgeInstrument(hedge[0]);
                    s.setDescription(hedge[1]);
                    s.setExpectedCorrelation(bd(Double.parseDouble(hedge[2])));
                    suggestions.add(s);
                }
            }

            // General put option suggestion
            HedgeSuggestion putSuggestion = new HedgeSuggestion();
            putSuggestion.setHoldingTicker(tickers.get(i));
            putSuggestion.setHoldingName(tickerNames.get(i));
            putSuggestion.setHedgeType("Put Option");
            putSuggestion.setHedgeInstrument(tickers.get(i) + " PUT");
            putSuggestion.setDescription("Protective put on " + tickers.get(i) + " — limits downside while preserving upside");
            putSuggestion.setExpectedCorrelation(bd(-1.0));
            suggestions.add(putSuggestion);
        }

        // General portfolio hedges
        if (!tickers.isEmpty()) {
            HedgeSuggestion vixHedge = new HedgeSuggestion();
            vixHedge.setHoldingTicker("PORTFOLIO");
            vixHedge.setHoldingName("Entire Portfolio");
            vixHedge.setHedgeType("Volatility");
            vixHedge.setHedgeInstrument("VXX");
            vixHedge.setDescription("iPath VIX Short-Term Futures — rises when market fear increases");
            vixHedge.setExpectedCorrelation(bd(-0.80));
            suggestions.add(vixHedge);

            HedgeSuggestion goldHedge = new HedgeSuggestion();
            goldHedge.setHoldingTicker("PORTFOLIO");
            goldHedge.setHoldingName("Entire Portfolio");
            goldHedge.setHedgeType("Uncorrelated Asset");
            goldHedge.setHedgeInstrument("GLD");
            goldHedge.setDescription("SPDR Gold Trust — historically low correlation with equities");
            goldHedge.setExpectedCorrelation(bd(-0.10));
            suggestions.add(goldHedge);

            HedgeSuggestion bondHedge = new HedgeSuggestion();
            bondHedge.setHoldingTicker("PORTFOLIO");
            bondHedge.setHoldingName("Entire Portfolio");
            bondHedge.setHedgeType("Uncorrelated Asset");
            bondHedge.setHedgeInstrument("TLT");
            bondHedge.setDescription("iShares 20+ Year Treasury Bond — traditional equity/bond diversification");
            bondHedge.setExpectedCorrelation(bd(-0.30));
            suggestions.add(bondHedge);
        }

        return suggestions;
    }

    // ── Diversification Score (FR-CH-007) ──

    private double computeAverageAbsCorrelation(double[][] matrix, int n) {
        if (n < 2) return 0;
        double sum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sum += Math.abs(matrix[i][j]);
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    // ── Helpers ──

    private double[] computeReturns(double[] prices) {
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            returns[i - 1] = prices[i - 1] == 0 ? 0 : (prices[i] - prices[i - 1]) / prices[i - 1];
        }
        return returns;
    }

    private double mean(double[] data, int n) {
        double sum = 0;
        for (int i = 0; i < n; i++) sum += data[i];
        return n == 0 ? 0 : sum / n;
    }

    private List<int[]> getTopPairs(double[][] matrix, int n, int maxPairs) {
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                pairs.add(new int[]{i, j});
            }
        }
        // Sort by absolute correlation descending
        pairs.sort((a, b) -> Double.compare(
                Math.abs(matrix[b[0]][b[1]]),
                Math.abs(matrix[a[0]][a[1]])));
        return pairs.subList(0, Math.min(maxPairs, pairs.size()));
    }

    private double[][] roundMatrix(double[][] matrix) {
        double[][] rounded = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            rounded[i] = new double[matrix[i].length];
            for (int j = 0; j < matrix[i].length; j++) {
                rounded[i][j] = Math.round(matrix[i][j] * 10000.0) / 10000.0;
            }
        }
        return rounded;
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
