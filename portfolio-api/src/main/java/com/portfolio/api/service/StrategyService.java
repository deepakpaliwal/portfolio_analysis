package com.portfolio.api.service;

import com.portfolio.api.dto.StrategyResponse.*;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Strategy engine (FR-SE-001 through FR-SE-007).
 *
 * Provides predefined investment strategies, backtesting using historical
 * price data from local DB/CSV, trade signal generation, and tax-loss
 * harvesting suggestions.
 */
@Service
public class StrategyService {

    private static final Logger log = LoggerFactory.getLogger(StrategyService.class);
    private static final double RISK_FREE_RATE = 0.05;
    private static final String BENCHMARK = "SPY";

    private final PortfolioRepository portfolioRepository;
    private final StockPriceHistoryService priceHistoryService;
    private final MarketDataService marketDataService;

    public StrategyService(PortfolioRepository portfolioRepository,
                            StockPriceHistoryService priceHistoryService,
                            MarketDataService marketDataService) {
        this.portfolioRepository = portfolioRepository;
        this.priceHistoryService = priceHistoryService;
        this.marketDataService = marketDataService;
    }

    // ── FR-SE-001/002: Predefined Strategies ──

    public List<StrategyDefinition> getAvailableStrategies() {
        List<StrategyDefinition> strategies = new ArrayList<>();

        strategies.add(buildStrategy("sma_crossover", "SMA Crossover", "Trend Following",
                "Buy when short-term SMA crosses above long-term SMA, sell on reverse cross.",
                "Medium", List.of("Growth-oriented", "Trend traders"),
                List.of(
                        new StrategyParameter("shortPeriod", "Short SMA Period", "int", "20", "Short moving average window"),
                        new StrategyParameter("longPeriod", "Long SMA Period", "int", "50", "Long moving average window")
                )));

        strategies.add(buildStrategy("mean_reversion", "Mean Reversion", "Mean Reversion",
                "Buy when price drops below lower Bollinger Band, sell when above upper band.",
                "Medium", List.of("Value investors", "Range-bound markets"),
                List.of(
                        new StrategyParameter("period", "Bollinger Period", "int", "20", "Lookback window"),
                        new StrategyParameter("stdDev", "Std Dev Multiplier", "double", "2.0", "Band width")
                )));

        strategies.add(buildStrategy("momentum", "Momentum", "Momentum",
                "Buy assets with strong recent performance (high RSI trend), sell weak performers.",
                "High", List.of("Aggressive traders", "Bull markets"),
                List.of(
                        new StrategyParameter("rsiPeriod", "RSI Period", "int", "14", "RSI lookback"),
                        new StrategyParameter("buyThreshold", "Buy RSI Threshold", "int", "30", "Buy when RSI below this"),
                        new StrategyParameter("sellThreshold", "Sell RSI Threshold", "int", "70", "Sell when RSI above this")
                )));

        strategies.add(buildStrategy("dividend_yield", "Dividend Yield", "Income",
                "Focus on high-dividend stocks, buy when yield exceeds threshold.",
                "Low", List.of("Income seekers", "Conservative investors"),
                List.of(
                        new StrategyParameter("minYield", "Min Dividend Yield %", "double", "3.0", "Minimum annual yield")
                )));

        strategies.add(buildStrategy("dca", "Dollar Cost Averaging", "Passive",
                "Invest fixed amount at regular intervals regardless of price.",
                "Low", List.of("Long-term investors", "Beginners"),
                List.of(
                        new StrategyParameter("intervalDays", "Investment Interval (days)", "int", "30", "Days between investments")
                )));

        strategies.add(buildStrategy("risk_parity", "Risk Parity", "Portfolio Optimization",
                "Allocate capital inversely proportional to asset volatility for equal risk contribution.",
                "Low", List.of("Institutional investors", "Balanced portfolios"),
                List.of(
                        new StrategyParameter("volWindow", "Volatility Window", "int", "60", "Days for volatility calculation")
                )));

        return strategies;
    }

    // ── FR-SE-003: Backtesting ──

    public BacktestResult backtest(String strategyId, String ticker, int lookbackDays,
                                    Map<String, String> params) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(lookbackDays);

        double[] prices = priceHistoryService.getClosingPrices(ticker, startDate, endDate);
        if (prices.length < 50) {
            throw new IllegalStateException(
                    "Insufficient price data for " + ticker + " (" + prices.length +
                    " records). Sync prices via Batch Prices page first.");
        }

        double[] benchPrices = priceHistoryService.getClosingPrices(BENCHMARK, startDate, endDate);

        List<TradeRecord> trades;
        switch (strategyId) {
            case "sma_crossover":
                int shortP = parseInt(params, "shortPeriod", 20);
                int longP = parseInt(params, "longPeriod", 50);
                trades = backtestSmaCrossover(prices, startDate, shortP, longP);
                break;
            case "mean_reversion":
                int period = parseInt(params, "period", 20);
                double stdDev = parseDouble(params, "stdDev", 2.0);
                trades = backtestMeanReversion(prices, startDate, period, stdDev);
                break;
            case "momentum":
                int rsiPeriod = parseInt(params, "rsiPeriod", 14);
                int buyThresh = parseInt(params, "buyThreshold", 30);
                int sellThresh = parseInt(params, "sellThreshold", 70);
                trades = backtestMomentum(prices, startDate, rsiPeriod, buyThresh, sellThresh);
                break;
            case "dca":
                int interval = parseInt(params, "intervalDays", 30);
                trades = backtestDca(prices, startDate, interval);
                break;
            default:
                // For strategies without specific backtest logic, use SMA crossover as default
                trades = backtestSmaCrossover(prices, startDate, 20, 50);
                break;
        }

        return buildBacktestResult(strategyId, ticker, lookbackDays, prices, benchPrices, trades);
    }

    // ── FR-SE-006: Trade Signals ──

    @Transactional(readOnly = true)
    public PortfolioSignals generateSignals(Long portfolioId, String username) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (!portfolio.getUser().getEmail().equals(username)) {
            throw new SecurityException("Access denied");
        }

        List<Holding> stockHoldings = portfolio.getHoldings().stream()
                .filter(h -> h.getTicker() != null && !h.getTicker().isBlank())
                .filter(h -> {
                    String type = h.getAssetType() != null ? h.getAssetType().name() : "";
                    return "STOCK".equals(type) || "ETF".equals(type);
                })
                .toList();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(100);

        List<TradeSignal> signals = new ArrayList<>();
        List<TaxLossCandidate> taxLossCandidates = new ArrayList<>();

        for (Holding h : stockHoldings) {
            double[] prices = priceHistoryService.getClosingPrices(h.getTicker(), startDate, endDate);
            if (prices.length < 30) continue;

            double currentPrice = prices[prices.length - 1];

            // SMA signal
            TradeSignal smaSignal = generateSmaSignal(h, prices, currentPrice);
            if (smaSignal != null) signals.add(smaSignal);

            // RSI signal
            TradeSignal rsiSignal = generateRsiSignal(h, prices, currentPrice);
            if (rsiSignal != null) signals.add(rsiSignal);

            // FR-SE-007: Tax-loss harvesting
            if (h.getPurchasePrice() != null) {
                double purchasePrice = h.getPurchasePrice().doubleValue();
                if (currentPrice < purchasePrice) {
                    double loss = (currentPrice - purchasePrice) * h.getQuantity().doubleValue();
                    double lossPct = (currentPrice - purchasePrice) / purchasePrice;

                    if (lossPct <= -0.05) { // At least 5% loss
                        TaxLossCandidate tlc = new TaxLossCandidate();
                        tlc.setTicker(h.getTicker());
                        tlc.setName(h.getName());
                        tlc.setPurchasePrice(h.getPurchasePrice());
                        tlc.setCurrentPrice(bd(currentPrice));
                        tlc.setUnrealizedLoss(bd(loss));
                        tlc.setUnrealizedLossPct(bd(lossPct * 100));
                        tlc.setSuggestion(lossPct <= -0.20
                                ? "Strong candidate: sell to harvest loss and buy similar ETF after 30-day wash sale period"
                                : "Consider selling to offset capital gains; replace with sector ETF to maintain exposure");
                        taxLossCandidates.add(tlc);
                    }
                }
            }
        }

        // Sort signals: BUY first, then SELL, then HOLD
        signals.sort((a, b) -> {
            int order = signalOrder(a.getSignal()) - signalOrder(b.getSignal());
            return order != 0 ? order : b.getConfidence().compareTo(a.getConfidence());
        });

        taxLossCandidates.sort((a, b) -> a.getUnrealizedLossPct().compareTo(b.getUnrealizedLossPct()));

        PortfolioSignals result = new PortfolioSignals();
        result.setPortfolioId(portfolioId);
        result.setPortfolioName(portfolio.getName());
        result.setSignals(signals);
        result.setTaxLossCandidates(taxLossCandidates);
        return result;
    }

    // ── Backtest Implementations ──

    private List<TradeRecord> backtestSmaCrossover(double[] prices, LocalDate startDate,
                                                     int shortPeriod, int longPeriod) {
        List<TradeRecord> trades = new ArrayList<>();
        int maxPeriod = Math.max(shortPeriod, longPeriod);
        if (prices.length <= maxPeriod) return trades;

        boolean inPosition = false;
        double entryPrice = 0;
        LocalDate entryDate = null;

        for (int i = maxPeriod; i < prices.length; i++) {
            double shortSma = sma(prices, i, shortPeriod);
            double longSma = sma(prices, i, longPeriod);
            double prevShortSma = sma(prices, i - 1, shortPeriod);
            double prevLongSma = sma(prices, i - 1, longPeriod);

            LocalDate date = startDate.plusDays(i);

            if (!inPosition && prevShortSma <= prevLongSma && shortSma > longSma) {
                // Golden cross — buy
                inPosition = true;
                entryPrice = prices[i];
                entryDate = date;
            } else if (inPosition && prevShortSma >= prevLongSma && shortSma < longSma) {
                // Death cross — sell
                TradeRecord t = new TradeRecord();
                t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setExitDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setSignal("BUY");
                t.setEntryPrice(bd(entryPrice));
                t.setExitPrice(bd(prices[i]));
                t.setReturnPct(bd((prices[i] - entryPrice) / entryPrice * 100));
                trades.add(t);
                inPosition = false;
            }
        }

        // Close open position at end
        if (inPosition) {
            TradeRecord t = new TradeRecord();
            t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setExitDate(startDate.plusDays(prices.length - 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setSignal("BUY");
            t.setEntryPrice(bd(entryPrice));
            t.setExitPrice(bd(prices[prices.length - 1]));
            t.setReturnPct(bd((prices[prices.length - 1] - entryPrice) / entryPrice * 100));
            trades.add(t);
        }

        return trades;
    }

    private List<TradeRecord> backtestMeanReversion(double[] prices, LocalDate startDate,
                                                      int period, double stdDevMult) {
        List<TradeRecord> trades = new ArrayList<>();
        if (prices.length <= period) return trades;

        boolean inPosition = false;
        double entryPrice = 0;
        LocalDate entryDate = null;

        for (int i = period; i < prices.length; i++) {
            double mean = sma(prices, i, period);
            double std = stdDev(prices, i, period);
            double lowerBand = mean - stdDevMult * std;
            double upperBand = mean + stdDevMult * std;

            LocalDate date = startDate.plusDays(i);

            if (!inPosition && prices[i] <= lowerBand) {
                inPosition = true;
                entryPrice = prices[i];
                entryDate = date;
            } else if (inPosition && prices[i] >= upperBand) {
                TradeRecord t = new TradeRecord();
                t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setExitDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setSignal("BUY");
                t.setEntryPrice(bd(entryPrice));
                t.setExitPrice(bd(prices[i]));
                t.setReturnPct(bd((prices[i] - entryPrice) / entryPrice * 100));
                trades.add(t);
                inPosition = false;
            }
        }

        if (inPosition) {
            TradeRecord t = new TradeRecord();
            t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setExitDate(startDate.plusDays(prices.length - 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setSignal("BUY");
            t.setEntryPrice(bd(entryPrice));
            t.setExitPrice(bd(prices[prices.length - 1]));
            t.setReturnPct(bd((prices[prices.length - 1] - entryPrice) / entryPrice * 100));
            trades.add(t);
        }

        return trades;
    }

    private List<TradeRecord> backtestMomentum(double[] prices, LocalDate startDate,
                                                 int rsiPeriod, int buyThresh, int sellThresh) {
        List<TradeRecord> trades = new ArrayList<>();
        if (prices.length <= rsiPeriod + 1) return trades;

        double[] rsiValues = computeRsi(prices, rsiPeriod);

        boolean inPosition = false;
        double entryPrice = 0;
        LocalDate entryDate = null;

        for (int i = 0; i < rsiValues.length; i++) {
            int priceIdx = rsiPeriod + i;
            LocalDate date = startDate.plusDays(priceIdx);

            if (!inPosition && rsiValues[i] < buyThresh) {
                inPosition = true;
                entryPrice = prices[priceIdx];
                entryDate = date;
            } else if (inPosition && rsiValues[i] > sellThresh) {
                TradeRecord t = new TradeRecord();
                t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setExitDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                t.setSignal("BUY");
                t.setEntryPrice(bd(entryPrice));
                t.setExitPrice(bd(prices[priceIdx]));
                t.setReturnPct(bd((prices[priceIdx] - entryPrice) / entryPrice * 100));
                trades.add(t);
                inPosition = false;
            }
        }

        if (inPosition) {
            TradeRecord t = new TradeRecord();
            t.setEntryDate(entryDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setExitDate(startDate.plusDays(prices.length - 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setSignal("BUY");
            t.setEntryPrice(bd(entryPrice));
            t.setExitPrice(bd(prices[prices.length - 1]));
            t.setReturnPct(bd((prices[prices.length - 1] - entryPrice) / entryPrice * 100));
            trades.add(t);
        }

        return trades;
    }

    private List<TradeRecord> backtestDca(double[] prices, LocalDate startDate, int intervalDays) {
        List<TradeRecord> trades = new ArrayList<>();
        double totalShares = 0;
        double totalInvested = 0;
        double investmentPerPeriod = 1000.0;

        for (int i = 0; i < prices.length; i += intervalDays) {
            double shares = investmentPerPeriod / prices[i];
            totalShares += shares;
            totalInvested += investmentPerPeriod;

            TradeRecord t = new TradeRecord();
            t.setEntryDate(startDate.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setExitDate(startDate.plusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            t.setSignal("BUY");
            t.setEntryPrice(bd(prices[i]));
            t.setExitPrice(bd(prices[i]));
            t.setReturnPct(BigDecimal.ZERO);
            trades.add(t);
        }

        // Mark last trade with overall return
        if (!trades.isEmpty() && totalInvested > 0) {
            double endValue = totalShares * prices[prices.length - 1];
            double totalReturn = (endValue - totalInvested) / totalInvested * 100;
            TradeRecord last = trades.get(trades.size() - 1);
            last.setExitPrice(bd(prices[prices.length - 1]));
            last.setReturnPct(bd(totalReturn));
        }

        return trades;
    }

    // ── Signal Generation ──

    private TradeSignal generateSmaSignal(Holding h, double[] prices, double currentPrice) {
        if (prices.length < 50) return null;

        double sma20 = sma(prices, prices.length - 1, 20);
        double sma50 = sma(prices, prices.length - 1, 50);
        double prevSma20 = sma(prices, prices.length - 2, 20);
        double prevSma50 = sma(prices, prices.length - 2, 50);

        TradeSignal signal = new TradeSignal();
        signal.setTicker(h.getTicker());
        signal.setHoldingName(h.getName() != null ? h.getName() : h.getTicker());
        signal.setCurrentPrice(bd(currentPrice));
        signal.setStrategySource("SMA Crossover (20/50)");

        if (prevSma20 <= prevSma50 && sma20 > sma50) {
            signal.setSignal("BUY");
            signal.setRationale("Golden cross: 20-day SMA crossed above 50-day SMA, indicating bullish momentum");
            signal.setConfidence(bd(0.75));
            signal.setTargetPrice(bd(currentPrice * 1.10));
            signal.setStopLoss(bd(currentPrice * 0.95));
            return signal;
        } else if (prevSma20 >= prevSma50 && sma20 < sma50) {
            signal.setSignal("SELL");
            signal.setRationale("Death cross: 20-day SMA crossed below 50-day SMA, indicating bearish momentum");
            signal.setConfidence(bd(0.70));
            signal.setTargetPrice(bd(currentPrice * 0.90));
            signal.setStopLoss(bd(currentPrice * 1.03));
            return signal;
        } else if (sma20 > sma50 && currentPrice > sma20) {
            signal.setSignal("HOLD");
            signal.setRationale("Price above both SMAs with positive trend — maintain position");
            signal.setConfidence(bd(0.60));
            return signal;
        }

        return null;
    }

    private TradeSignal generateRsiSignal(Holding h, double[] prices, double currentPrice) {
        if (prices.length < 20) return null;

        double[] rsi = computeRsi(prices, 14);
        if (rsi.length == 0) return null;

        double currentRsi = rsi[rsi.length - 1];

        TradeSignal signal = new TradeSignal();
        signal.setTicker(h.getTicker());
        signal.setHoldingName(h.getName() != null ? h.getName() : h.getTicker());
        signal.setCurrentPrice(bd(currentPrice));
        signal.setStrategySource("RSI (14-period)");

        if (currentRsi < 30) {
            signal.setSignal("BUY");
            signal.setRationale(String.format("RSI at %.1f (oversold < 30) — potential bounce opportunity", currentRsi));
            signal.setConfidence(bd(0.70));
            signal.setTargetPrice(bd(currentPrice * 1.08));
            signal.setStopLoss(bd(currentPrice * 0.95));
            return signal;
        } else if (currentRsi > 70) {
            signal.setSignal("SELL");
            signal.setRationale(String.format("RSI at %.1f (overbought > 70) — consider taking profits", currentRsi));
            signal.setConfidence(bd(0.65));
            signal.setTargetPrice(bd(currentPrice * 0.92));
            signal.setStopLoss(bd(currentPrice * 1.03));
            return signal;
        }

        return null;
    }

    // ── Result Builder ──

    private BacktestResult buildBacktestResult(String strategyId, String ticker, int lookbackDays,
                                                 double[] prices, double[] benchPrices,
                                                 List<TradeRecord> trades) {
        BacktestResult result = new BacktestResult();
        result.setStrategyId(strategyId);
        result.setStrategyName(getStrategyName(strategyId));
        result.setTicker(ticker);
        result.setLookbackDays(lookbackDays);
        result.setTrades(trades);
        result.setTotalTrades(trades.size());

        // Compute trade statistics
        int wins = 0, losses = 0;
        double totalWin = 0, totalLoss = 0;
        for (TradeRecord t : trades) {
            double ret = t.getReturnPct().doubleValue();
            if (ret > 0) { wins++; totalWin += ret; }
            else { losses++; totalLoss += Math.abs(ret); }
        }

        result.setWinningTrades(wins);
        result.setLosingTrades(losses);
        result.setWinRate(trades.isEmpty() ? BigDecimal.ZERO : bd((double) wins / trades.size() * 100));
        result.setAvgWin(wins == 0 ? BigDecimal.ZERO : bd(totalWin / wins));
        result.setAvgLoss(losses == 0 ? BigDecimal.ZERO : bd(totalLoss / losses));
        result.setProfitFactor(totalLoss == 0 ? bd(totalWin > 0 ? 999 : 0) : bd(totalWin / totalLoss));

        // Buy-and-hold return
        double totalReturn = prices.length > 1
                ? (prices[prices.length - 1] - prices[0]) / prices[0] * 100 : 0;
        result.setTotalReturn(bd(totalReturn));

        // CAGR
        double years = lookbackDays / 252.0;
        double cagr = years > 0 ? (Math.pow(1 + totalReturn / 100, 1.0 / years) - 1) * 100 : 0;
        result.setCagr(bd(cagr));

        // Max drawdown
        result.setMaxDrawdown(bd(computeMaxDrawdown(prices)));

        // Sharpe & Sortino
        double[] returns = computeDailyReturns(prices);
        double avgReturn = mean(returns) * 252;
        double vol = stdDevArray(returns) * Math.sqrt(252);
        result.setSharpeRatio(vol > 0 ? bd((avgReturn - RISK_FREE_RATE) / vol) : BigDecimal.ZERO);

        double downDev = downsideDeviation(returns) * Math.sqrt(252);
        result.setSortinoRatio(downDev > 0 ? bd((avgReturn - RISK_FREE_RATE) / downDev) : BigDecimal.ZERO);

        // Benchmark return
        if (benchPrices != null && benchPrices.length > 1) {
            double benchReturn = (benchPrices[benchPrices.length - 1] - benchPrices[0]) / benchPrices[0] * 100;
            result.setBenchmarkReturn(bd(benchReturn));
            result.setAlpha(bd(totalReturn - benchReturn));
        }

        return result;
    }

    // ── Technical Indicators ──

    private double sma(double[] prices, int endIdx, int period) {
        double sum = 0;
        int start = Math.max(0, endIdx - period + 1);
        for (int i = start; i <= endIdx; i++) sum += prices[i];
        return sum / (endIdx - start + 1);
    }

    private double stdDev(double[] prices, int endIdx, int period) {
        int start = Math.max(0, endIdx - period + 1);
        double mean = sma(prices, endIdx, period);
        double sumSq = 0;
        int count = 0;
        for (int i = start; i <= endIdx; i++) {
            sumSq += (prices[i] - mean) * (prices[i] - mean);
            count++;
        }
        return count < 2 ? 0 : Math.sqrt(sumSq / (count - 1));
    }

    private double[] computeRsi(double[] prices, int period) {
        if (prices.length <= period) return new double[0];

        double[] gains = new double[prices.length - 1];
        double[] losses = new double[prices.length - 1];
        for (int i = 0; i < prices.length - 1; i++) {
            double change = prices[i + 1] - prices[i];
            gains[i] = Math.max(change, 0);
            losses[i] = Math.max(-change, 0);
        }

        double[] rsi = new double[prices.length - period];
        double avgGain = 0, avgLoss = 0;
        for (int i = 0; i < period; i++) {
            avgGain += gains[i];
            avgLoss += losses[i];
        }
        avgGain /= period;
        avgLoss /= period;

        for (int i = 0; i < rsi.length; i++) {
            if (i > 0) {
                int idx = period - 1 + i;
                avgGain = (avgGain * (period - 1) + gains[idx]) / period;
                avgLoss = (avgLoss * (period - 1) + losses[idx]) / period;
            }
            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            rsi[i] = 100 - (100 / (1 + rs));
        }

        return rsi;
    }

    // ── Statistical Helpers ──

    private double computeMaxDrawdown(double[] prices) {
        double peak = prices[0];
        double maxDD = 0;
        for (double p : prices) {
            if (p > peak) peak = p;
            double dd = (peak - p) / peak;
            if (dd > maxDD) maxDD = dd;
        }
        return maxDD * 100;
    }

    private double[] computeDailyReturns(double[] prices) {
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            returns[i - 1] = prices[i - 1] == 0 ? 0 : (prices[i] - prices[i - 1]) / prices[i - 1];
        }
        return returns;
    }

    private double mean(double[] data) {
        double sum = 0;
        for (double d : data) sum += d;
        return data.length == 0 ? 0 : sum / data.length;
    }

    private double stdDevArray(double[] data) {
        if (data.length < 2) return 0;
        double m = mean(data);
        double sumSq = 0;
        for (double d : data) sumSq += (d - m) * (d - m);
        return Math.sqrt(sumSq / (data.length - 1));
    }

    private double downsideDeviation(double[] returns) {
        double dailyRf = RISK_FREE_RATE / 252;
        double sumSq = 0;
        int count = 0;
        for (double r : returns) {
            double diff = r - dailyRf;
            if (diff < 0) { sumSq += diff * diff; count++; }
        }
        return count < 2 ? 0 : Math.sqrt(sumSq / (count - 1));
    }

    // ── Helpers ──

    private StrategyDefinition buildStrategy(String id, String name, String category,
                                              String description, String riskLevel,
                                              List<String> suitableFor,
                                              List<StrategyParameter> params) {
        StrategyDefinition s = new StrategyDefinition();
        s.setId(id);
        s.setName(name);
        s.setCategory(category);
        s.setDescription(description);
        s.setRiskLevel(riskLevel);
        s.setSuitableFor(suitableFor);
        s.setParameters(params);
        return s;
    }

    private String getStrategyName(String id) {
        return getAvailableStrategies().stream()
                .filter(s -> s.getId().equals(id))
                .map(StrategyDefinition::getName)
                .findFirst().orElse(id);
    }

    private int signalOrder(String signal) {
        return switch (signal) { case "BUY" -> 0; case "SELL" -> 1; default -> 2; };
    }

    private int parseInt(Map<String, String> params, String key, int defaultVal) {
        try { return params != null && params.containsKey(key) ? Integer.parseInt(params.get(key)) : defaultVal; }
        catch (NumberFormatException e) { return defaultVal; }
    }

    private double parseDouble(Map<String, String> params, String key, double defaultVal) {
        try { return params != null && params.containsKey(key) ? Double.parseDouble(params.get(key)) : defaultVal; }
        catch (NumberFormatException e) { return defaultVal; }
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
