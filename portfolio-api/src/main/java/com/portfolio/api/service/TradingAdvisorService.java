package com.portfolio.api.service;

import com.portfolio.api.dto.TradingAdvisorResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class TradingAdvisorService {
    private final StockPriceHistoryService priceHistoryService;
    private final MarketDataService marketDataService;

    public TradingAdvisorService(StockPriceHistoryService priceHistoryService, MarketDataService marketDataService) {
        this.priceHistoryService = priceHistoryService;
        this.marketDataService = marketDataService;
    }

    public TradingAdvisorResponse analyze(String symbol, BigDecimal positionValue, int lookbackDays) {
        return analyzeInternal(symbol, positionValue, lookbackDays, false);
    }

    public TradingAdvisorResponse analyzeCrypto(String symbol, BigDecimal positionValue, int lookbackDays) {
        return analyzeInternal(symbol, positionValue, lookbackDays, true);
    }

    private TradingAdvisorResponse analyzeInternal(String symbol, BigDecimal positionValue, int lookbackDays, boolean crypto) {
        String ticker = symbol == null ? "" : symbol.toUpperCase().trim();
        if (ticker.isBlank()) throw new IllegalArgumentException("Ticker is required");
        int synced = priceHistoryService.syncTickerHistory(ticker);

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(Math.max(lookbackDays, 60));
        double[] closes = priceHistoryService.getClosingPrices(ticker, from, to);
        List<LocalDate> dates = priceHistoryService.getTradeDates(ticker, from, to);

        if (closes.length < 30) throw new IllegalArgumentException("Not enough historical data for " + ticker);

        double last = closes[closes.length - 1];

        TradingAdvisorResponse resp = new TradingAdvisorResponse();
        resp.setTicker(ticker);
        resp.setRecordsSynced(synced);
        resp.setStoredRecords(priceHistoryService.getRecordCount(ticker));

        Map<String, Object> quote = marketDataService.getQuote(ticker);
        BigDecimal quotedPrice = num(quote, "c");
        BigDecimal quotedChangePercent = num(quote, "dp");

        if (crypto) {
            resp.setName(ticker);
            resp.setIndustry("Cryptocurrency");
        } else {
            Map<String, Object> profile = marketDataService.getCompanyProfile(ticker);
            resp.setName(str(profile, "name"));
            resp.setIndustry(str(profile, "finnhubIndustry"));
        }
        resp.setCurrentPrice(quotedPrice != null ? quotedPrice : bd(last));
        if (quotedChangePercent != null) {
            resp.setChangePercent(quotedChangePercent);
        } else if (closes.length > 1 && closes[closes.length - 2] != 0) {
            double fallbackChangePercent = ((last / closes[closes.length - 2]) - 1) * 100;
            resp.setChangePercent(bd(fallbackChangePercent));
        }

        TradingAdvisorResponse.Indicators ind = new TradingAdvisorResponse.Indicators();
        double sma20 = sma(closes, 20);
        double ema20 = ema(closes, 20);
        double rsi14 = rsi(closes, 14);
        double ema12 = ema(closes, 12);
        double ema26 = ema(closes, 26);
        double macd = ema12 - ema26;
        double signal9 = macd; // approximation for lightweight advisor
        double annualVol = annualizedVol(returns(closes));

        ind.setSma20(bd(sma20));
        ind.setEma20(bd(ema20));
        ind.setRsi14(bd(rsi14));
        ind.setMacd(bd(macd));
        ind.setSignal9(bd(signal9));
        ind.setAnnualizedVolatility(bd(annualVol));
        resp.setIndicators(ind);

        double[] rets = returns(closes);
        double var95 = historicalVar(rets, 0.95) * positionValue.doubleValue();
        double var99 = historicalVar(rets, 0.99) * positionValue.doubleValue();

        TradingAdvisorResponse.Risk risk = new TradingAdvisorResponse.Risk();
        risk.setVar95(bd(Math.abs(var95)));
        risk.setVar99(bd(Math.abs(var99)));
        resp.setRisk(risk);

        String reco;
        String rationale;
        if (last > sma20 && macd > 0 && rsi14 < 70) {
            reco = "BUY";
            rationale = "Price above trend (SMA20), positive momentum (MACD), RSI not overbought.";
        } else if (last < sma20 && macd < 0 && rsi14 > 30) {
            reco = "SELL";
            rationale = "Price below trend with negative momentum.";
        } else {
            reco = "HOLD";
            rationale = "Signals are mixed; wait for clearer setup.";
        }
        resp.setRecommendation(reco);
        resp.setRationale(rationale);

        List<TradingAdvisorResponse.ChartPoint> chart = new ArrayList<>();
        int size = Math.min(closes.length, dates.size());
        for (int i = 0; i < size; i++) chart.add(new TradingAdvisorResponse.ChartPoint(dates.get(i).toString(), bd(closes[i])));
        resp.setChart(chart);

        return resp;
    }

    private BigDecimal num(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return null;
        Object v = map.get(key);
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return null;
    }
    private String str(Map<String, Object> map, String key) { return map == null || map.get(key) == null ? null : String.valueOf(map.get(key)); }
    private BigDecimal bd(double x) { return BigDecimal.valueOf(x).setScale(4, RoundingMode.HALF_UP); }

    private double sma(double[] v, int n) {
        int s = Math.max(0, v.length - n); double sum = 0; for (int i=s;i<v.length;i++) sum += v[i]; return sum / (v.length - s);
    }
    private double ema(double[] v, int n) {
        double k = 2.0 / (n + 1); double e = v[0];
        for (int i=1;i<v.length;i++) e = v[i] * k + e * (1 - k);
        return e;
    }
    private double[] returns(double[] v) {
        double[] r = new double[Math.max(0, v.length - 1)];
        for (int i=1;i<v.length;i++) r[i-1] = (v[i]/v[i-1]) - 1.0;
        return r;
    }
    private double rsi(double[] v, int n) {
        int start = Math.max(1, v.length - n);
        double gain = 0, loss = 0;
        for (int i=start;i<v.length;i++) {
            double d = v[i] - v[i-1];
            if (d >= 0) gain += d; else loss -= d;
        }
        if (loss == 0) return 100;
        double rs = (gain / n) / (loss / n);
        return 100 - (100 / (1 + rs));
    }
    private double annualizedVol(double[] r) {
        if (r.length < 2) return 0;
        double m = Arrays.stream(r).average().orElse(0);
        double var = 0; for (double x : r) var += (x-m)*(x-m);
        double sd = Math.sqrt(var / (r.length - 1));
        return sd * Math.sqrt(252);
    }
    private double historicalVar(double[] r, double confidence) {
        if (r.length == 0) return 0;
        double[] c = Arrays.copyOf(r, r.length);
        Arrays.sort(c);
        int idx = Math.max(0, (int)Math.floor((1.0 - confidence) * c.length) - 1);
        return Math.min(c[idx], 0);
    }
}
