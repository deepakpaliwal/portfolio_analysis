package com.portfolio.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.api.dto.*;
import com.portfolio.api.model.ScreenerReport;
import com.portfolio.api.model.User;
import com.portfolio.api.repository.ScreenerReportRepository;
import com.portfolio.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScreenerService {

    private static final Logger log = LoggerFactory.getLogger(ScreenerService.class);

    private final MarketDataService marketDataService;
    private final ScreenerReportRepository screenerReportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Representative tickers per sector for sector screener
    private static final Map<String, List<String>> SECTOR_TICKERS = Map.ofEntries(
            Map.entry("Technology", List.of("AAPL", "MSFT", "NVDA", "GOOGL", "META", "AVGO", "ORCL", "CRM", "ADBE", "AMD")),
            Map.entry("Healthcare", List.of("JNJ", "UNH", "PFE", "ABBV", "MRK", "LLY", "TMO", "ABT", "DHR", "BMY")),
            Map.entry("Financials", List.of("JPM", "BAC", "WFC", "GS", "MS", "BLK", "SCHW", "AXP", "C", "USB")),
            Map.entry("Consumer Discretionary", List.of("AMZN", "TSLA", "HD", "MCD", "NKE", "SBUX", "LOW", "TJX", "BKNG", "CMG")),
            Map.entry("Consumer Staples", List.of("PG", "KO", "PEP", "WMT", "COST", "PM", "MDLZ", "CL", "EL", "KHC")),
            Map.entry("Energy", List.of("XOM", "CVX", "COP", "SLB", "EOG", "MPC", "PSX", "VLO", "OXY", "WMB")),
            Map.entry("Industrials", List.of("CAT", "GE", "UNP", "BA", "HON", "RTX", "DE", "LMT", "MMM", "UPS")),
            Map.entry("Utilities", List.of("NEE", "DUK", "SO", "D", "AEP", "SRE", "EXC", "XEL", "ED", "WEC")),
            Map.entry("Real Estate", List.of("PLD", "AMT", "CCI", "EQIX", "PSA", "SPG", "O", "DLR", "WELL", "AVB")),
            Map.entry("Materials", List.of("LIN", "APD", "SHW", "ECL", "FCX", "NEM", "NUE", "VMC", "MLM", "DD")),
            Map.entry("Communication Services", List.of("GOOGL", "META", "DIS", "NFLX", "CMCSA", "VZ", "T", "TMUS", "CHTR", "EA"))
    );

    public ScreenerService(MarketDataService marketDataService,
                           ScreenerReportRepository screenerReportRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.marketDataService = marketDataService;
        this.screenerReportRepository = screenerReportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    // ───────── Ticker Report (FR-SC-001 through FR-SC-005) ─────────

    public TickerReportResponse generateTickerReport(String symbol) {
        String ticker = symbol.toUpperCase().trim();
        TickerReportResponse report = new TickerReportResponse();
        report.setTicker(ticker);

        // Company profile
        Map<String, Object> profile = marketDataService.getCompanyProfile(ticker);
        if (profile != null && !profile.isEmpty()) {
            report.setName(strVal(profile, "name"));
            report.setIndustry(strVal(profile, "finnhubIndustry"));
            report.setSector(strVal(profile, "finnhubIndustry"));
            report.setCountry(strVal(profile, "country"));
            report.setCurrency(strVal(profile, "currency"));
            report.setExchange(strVal(profile, "exchange"));
            report.setLogo(strVal(profile, "logo"));
            report.setWeburl(strVal(profile, "weburl"));
            report.setMarketCap(numVal(profile, "marketCapitalization"));
        }

        // Quote data
        Map<String, Object> quote = marketDataService.getQuote(ticker);
        if (quote != null) {
            report.setCurrentPrice(numVal(quote, "c"));
            report.setPreviousClose(numVal(quote, "pc"));
            report.setChange(numVal(quote, "d"));
            report.setChangePercent(numVal(quote, "dp"));
        }

        // Basic financials / metrics
        Map<String, Object> financials = marketDataService.getBasicFinancials(ticker);
        if (financials != null && financials.get("metric") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metric = (Map<String, Object>) financials.get("metric");
            report.setWeekHigh52(numVal(metric, "52WeekHigh"));
            report.setWeekLow52(numVal(metric, "52WeekLow"));
            report.setPeRatio(numVal(metric, "peNormalizedAnnual"));
            report.setEps(numVal(metric, "epsNormalizedAnnual"));
            report.setDividendYield(numVal(metric, "dividendYieldIndicatedAnnual"));
            report.setBeta(numVal(metric, "beta"));
            report.setRevenueGrowthTTM(numVal(metric, "revenueGrowthTTMYoy"));
            report.setEarningsGrowthTTM(numVal(metric, "epsGrowthTTMYoy"));
        }

        // Financial statements (FR-SC-003)
        report.setAnnualFinancials(parseFinancialStatements(
                marketDataService.getFinancialStatements(ticker, "annual"), 5));
        report.setQuarterlyFinancials(parseFinancialStatements(
                marketDataService.getFinancialStatements(ticker, "quarterly"), 8));

        // SEC filings (FR-SC-004)
        report.setSecFilings(parseSecFilings(marketDataService.getSecFilings(ticker)));

        // Analyst recommendations (FR-SC-005)
        report.setRecommendations(parseRecommendations(marketDataService.getRecommendations(ticker)));
        report.setPriceTarget(parsePriceTarget(marketDataService.getPriceTarget(ticker)));
        report.setEarnings(parseEarnings(marketDataService.getEarnings(ticker)));

        return report;
    }

    // ───────── Sector Report (FR-SC-006, FR-SC-007) ─────────

    public SectorReportResponse generateSectorReport(String sector) {
        SectorReportResponse report = new SectorReportResponse();
        report.setSector(sector);

        List<String> tickers = SECTOR_TICKERS.getOrDefault(sector, Collections.emptyList());
        if (tickers.isEmpty()) {
            report.setStockCount(0);
            report.setTopPerformers(Collections.emptyList());
            report.setBottomPerformers(Collections.emptyList());
            report.setRotationSignals(Collections.emptyList());
            return report;
        }

        List<SectorReportResponse.StockPerformance> performances = new ArrayList<>();
        BigDecimal peSum = BigDecimal.ZERO;
        BigDecimal divYieldSum = BigDecimal.ZERO;
        int peCount = 0;
        int divCount = 0;

        for (String ticker : tickers) {
            SectorReportResponse.StockPerformance sp = new SectorReportResponse.StockPerformance();
            sp.setTicker(ticker);

            Map<String, Object> profile = marketDataService.getCompanyProfile(ticker);
            if (profile != null) {
                sp.setName(strVal(profile, "name"));
                sp.setMarketCap(numVal(profile, "marketCapitalization"));
            }

            Map<String, Object> quote = marketDataService.getQuote(ticker);
            if (quote != null) {
                sp.setCurrentPrice(numVal(quote, "c"));
                sp.setChangePercent(numVal(quote, "dp"));
            }

            Map<String, Object> financials = marketDataService.getBasicFinancials(ticker);
            if (financials != null && financials.get("metric") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metric = (Map<String, Object>) financials.get("metric");
                BigDecimal pe = numVal(metric, "peNormalizedAnnual");
                if (pe != null && pe.compareTo(BigDecimal.ZERO) > 0) {
                    sp.setPeRatio(pe);
                    peSum = peSum.add(pe);
                    peCount++;
                }
                BigDecimal dy = numVal(metric, "dividendYieldIndicatedAnnual");
                if (dy != null) {
                    divYieldSum = divYieldSum.add(dy);
                    divCount++;
                }
            }

            performances.add(sp);
        }

        // Sort by changePercent for top/bottom performers
        performances.sort((a, b) -> {
            BigDecimal aVal = a.getChangePercent() != null ? a.getChangePercent() : BigDecimal.ZERO;
            BigDecimal bVal = b.getChangePercent() != null ? b.getChangePercent() : BigDecimal.ZERO;
            return bVal.compareTo(aVal);
        });

        report.setStockCount(performances.size());
        report.setTopPerformers(performances.stream().limit(5).collect(Collectors.toList()));
        report.setBottomPerformers(performances.stream()
                .sorted((a, b) -> {
                    BigDecimal aVal = a.getChangePercent() != null ? a.getChangePercent() : BigDecimal.ZERO;
                    BigDecimal bVal = b.getChangePercent() != null ? b.getChangePercent() : BigDecimal.ZERO;
                    return aVal.compareTo(bVal);
                }).limit(5).collect(Collectors.toList()));

        // Average P/E and dividend yield
        if (peCount > 0) {
            report.setAveragePE(peSum.divide(BigDecimal.valueOf(peCount), 2, RoundingMode.HALF_UP));
        }
        if (divCount > 0) {
            report.setAverageDividendYield(divYieldSum.divide(BigDecimal.valueOf(divCount), 2, RoundingMode.HALF_UP));
        }

        // Sector performance = average of all stock change percentages
        BigDecimal sectorPerfSum = performances.stream()
                .map(p -> p.getChangePercent() != null ? p.getChangePercent() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setSectorPerformancePercent(sectorPerfSum.divide(
                BigDecimal.valueOf(performances.size()), 2, RoundingMode.HALF_UP));

        // S&P 500 performance (SPY as proxy)
        Map<String, Object> spyQuote = marketDataService.getQuote("SPY");
        BigDecimal spPerf = BigDecimal.ZERO;
        if (spyQuote != null && spyQuote.get("dp") != null) {
            spPerf = numVal(spyQuote, "dp");
        }
        report.setSpPerformancePercent(spPerf);
        report.setRelativePerformancePercent(
                report.getSectorPerformancePercent().subtract(spPerf != null ? spPerf : BigDecimal.ZERO));

        // Rotation signals
        report.setRotationSignals(generateRotationSignals(report));

        return report;
    }

    // ───────── Custom Screen (FR-SC-008) ─────────

    public ScreenResultResponse runCustomScreen(ScreenCriteriaRequest criteria) {
        ScreenResultResponse result = new ScreenResultResponse();
        String exchange = criteria.getExchange() != null ? criteria.getExchange() : "US";
        List<Map<String, Object>> symbols = marketDataService.getStockSymbols(exchange);

        // Filter to common stocks only
        List<String> tickers = symbols.stream()
                .filter(s -> "Common Stock".equals(s.get("type")))
                .map(s -> (String) s.get("symbol"))
                .filter(Objects::nonNull)
                .limit(100) // Limit to avoid API rate limits
                .collect(Collectors.toList());

        List<ScreenResultResponse.ScreenedStock> matched = new ArrayList<>();

        for (String ticker : tickers) {
            try {
                Map<String, Object> profile = marketDataService.getCompanyProfile(ticker);
                Map<String, Object> quote = marketDataService.getQuote(ticker);
                Map<String, Object> financials = marketDataService.getBasicFinancials(ticker);

                if (profile == null || quote == null) continue;

                // Sector filter
                if (criteria.getSector() != null && !criteria.getSector().isEmpty()) {
                    String industry = strVal(profile, "finnhubIndustry");
                    if (industry == null || !industry.toLowerCase().contains(criteria.getSector().toLowerCase())) {
                        continue;
                    }
                }

                BigDecimal currentPrice = numVal(quote, "c");
                BigDecimal changePercent = numVal(quote, "dp");

                @SuppressWarnings("unchecked")
                Map<String, Object> metric = financials != null && financials.get("metric") != null
                        ? (Map<String, Object>) financials.get("metric") : Collections.emptyMap();

                BigDecimal pe = numVal(metric, "peNormalizedAnnual");
                BigDecimal eps = numVal(metric, "epsNormalizedAnnual");
                BigDecimal divYield = numVal(metric, "dividendYieldIndicatedAnnual");
                BigDecimal beta = numVal(metric, "beta");
                BigDecimal marketCap = numVal(profile, "marketCapitalization");
                BigDecimal high52 = numVal(metric, "52WeekHigh");
                BigDecimal low52 = numVal(metric, "52WeekLow");

                // Apply filters
                if (!passesFilter(pe, criteria.getPeRatioMin(), criteria.getPeRatioMax())) continue;
                if (!passesFilter(divYield, criteria.getDividendYieldMin(), criteria.getDividendYieldMax())) continue;
                if (!passesFilter(marketCap, criteria.getMarketCapMin(), criteria.getMarketCapMax())) continue;
                if (!passesFilter(beta, criteria.getBetaMin(), criteria.getBetaMax())) continue;
                if (!passesFilter(currentPrice, criteria.getPriceMin(), criteria.getPriceMax())) continue;
                if (criteria.getEpsMin() != null && (eps == null || eps.compareTo(criteria.getEpsMin()) < 0)) continue;

                ScreenResultResponse.ScreenedStock stock = new ScreenResultResponse.ScreenedStock();
                stock.setTicker(ticker);
                stock.setName(strVal(profile, "name"));
                stock.setSector(strVal(profile, "finnhubIndustry"));
                stock.setIndustry(strVal(profile, "finnhubIndustry"));
                stock.setCurrentPrice(currentPrice);
                stock.setChangePercent(changePercent);
                stock.setMarketCap(marketCap);
                stock.setPeRatio(pe);
                stock.setEps(eps);
                stock.setDividendYield(divYield);
                stock.setBeta(beta);
                stock.setWeekHigh52(high52);
                stock.setWeekLow52(low52);
                matched.add(stock);
            } catch (Exception e) {
                log.debug("Skipping ticker {} in screen: {}", ticker, e.getMessage());
            }
        }

        result.setTotalMatches(matched.size());
        result.setStocks(matched);
        return result;
    }

    // ───────── Technical Indicators (FR-SC-009) ─────────

    public TechnicalIndicatorResponse getTechnicalIndicator(String symbol, String indicator,
                                                             String resolution, int timeperiod) {
        String ticker = symbol.toUpperCase().trim();
        long to = Instant.now().getEpochSecond();
        long from = to - (365L * 24 * 60 * 60); // 1 year of data

        Map<String, Object> data = marketDataService.getTechnicalIndicator(
                ticker, indicator, resolution, from, to, timeperiod);

        TechnicalIndicatorResponse response = new TechnicalIndicatorResponse();
        response.setTicker(ticker);
        response.setIndicator(indicator.toUpperCase());
        response.setResolution(resolution);

        if (data != null && data.get("t") != null && data.get("indicator") != null) {
            @SuppressWarnings("unchecked")
            List<Number> timestamps = (List<Number>) data.get("t");
            @SuppressWarnings("unchecked")
            Map<String, List<Number>> indicatorMap = (Map<String, List<Number>>) data.get("indicator");

            // Most indicators return a single key
            List<Number> values = null;
            for (List<Number> v : indicatorMap.values()) {
                values = v;
                break;
            }

            if (timestamps != null && values != null) {
                List<TechnicalIndicatorResponse.DataPoint> points = new ArrayList<>();
                int size = Math.min(timestamps.size(), values.size());
                for (int i = 0; i < size; i++) {
                    if (values.get(i) != null) {
                        points.add(new TechnicalIndicatorResponse.DataPoint(
                                timestamps.get(i).longValue(),
                                BigDecimal.valueOf(values.get(i).doubleValue())));
                    }
                }
                response.setValues(points);
            }
        }

        if (response.getValues() == null) {
            response.setValues(Collections.emptyList());
        }
        return response;
    }

    // ───────── Saved Reports (FR-SC-010) ─────────

    @Transactional
    public ScreenerReport saveReport(String email, String reportType, String target, Object reportData) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScreenerReport report = new ScreenerReport();
        report.setUser(user);
        report.setReportType(reportType);
        report.setTarget(target);
        try {
            report.setReportData(objectMapper.writeValueAsString(reportData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize report data", e);
        }
        return screenerReportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSavedReports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return screenerReportRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", r.getId());
                    map.put("reportType", r.getReportType());
                    map.put("target", r.getTarget());
                    map.put("createdAt", r.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getSavedReportData(String email, Long reportId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScreenerReport report = screenerReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return report.getReportData();
    }

    @Transactional
    public void deleteReport(String email, Long reportId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScreenerReport report = screenerReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        screenerReportRepository.delete(report);
    }

    public List<String> getAvailableSectors() {
        return new ArrayList<>(SECTOR_TICKERS.keySet()).stream().sorted().collect(Collectors.toList());
    }

    // ───────── Helpers ─────────

    private boolean passesFilter(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (min != null && (value == null || value.compareTo(min) < 0)) return false;
        if (max != null && (value == null || value.compareTo(max) > 0)) return false;
        return true;
    }

    private String strVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    private BigDecimal numVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            return d != 0 ? BigDecimal.valueOf(d) : null;
        }
        return null;
    }

    private List<TickerReportResponse.FinancialStatement> parseFinancialStatements(
            List<Map<String, Object>> raw, int limit) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        return raw.stream().limit(limit).map(item -> {
            TickerReportResponse.FinancialStatement fs = new TickerReportResponse.FinancialStatement();
            fs.setYear(item.get("year") != null ? ((Number) item.get("year")).intValue() : 0);
            fs.setQuarter(item.get("quarter") != null ? item.get("quarter").toString() : null);
            fs.setPeriod(item.get("startDate") != null ? item.get("startDate").toString() : null);

            // Extract report data from the nested structure
            @SuppressWarnings("unchecked")
            Map<String, Object> report = item.get("report") != null
                    ? (Map<String, Object>) item.get("report") : Collections.emptyMap();

            // Attempt to extract key financial figures from IC/BS/CF sections
            fs.setRevenue(extractFinancialValue(report, "ic", "us-gaap_Revenues", "us-gaap_RevenueFromContractWithCustomerExcludingAssessedTax"));
            fs.setNetIncome(extractFinancialValue(report, "ic", "us-gaap_NetIncomeLoss"));
            fs.setTotalAssets(extractFinancialValue(report, "bs", "us-gaap_Assets"));
            fs.setTotalLiabilities(extractFinancialValue(report, "bs", "us-gaap_Liabilities"));
            fs.setTotalEquity(extractFinancialValue(report, "bs", "us-gaap_StockholdersEquity"));
            fs.setOperatingCashFlow(extractFinancialValue(report, "cf", "us-gaap_NetCashProvidedByUsedInOperatingActivities"));

            return fs;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private BigDecimal extractFinancialValue(Map<String, Object> report, String section, String... conceptKeys) {
        if (report == null) return null;
        Object sectionObj = report.get(section);
        if (!(sectionObj instanceof List)) return null;
        List<Map<String, Object>> items = (List<Map<String, Object>>) sectionObj;
        for (String concept : conceptKeys) {
            for (Map<String, Object> item : items) {
                if (concept.equals(item.get("concept"))) {
                    Object val = item.get("value");
                    if (val instanceof Number) {
                        return BigDecimal.valueOf(((Number) val).doubleValue());
                    }
                }
            }
        }
        return null;
    }

    private List<TickerReportResponse.SecFiling> parseSecFilings(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        return raw.stream()
                .filter(f -> {
                    String form = strVal(f, "form");
                    return form != null && (form.equals("10-K") || form.equals("10-Q") ||
                            form.equals("8-K") || form.startsWith("DEF"));
                })
                .limit(20)
                .map(f -> {
                    TickerReportResponse.SecFiling sf = new TickerReportResponse.SecFiling();
                    sf.setForm(strVal(f, "form"));
                    sf.setFiledDate(strVal(f, "filedDate"));
                    sf.setAcceptedDate(strVal(f, "acceptedDate"));
                    sf.setReportUrl(strVal(f, "reportUrl"));
                    return sf;
                })
                .collect(Collectors.toList());
    }

    private List<TickerReportResponse.AnalystRecommendation> parseRecommendations(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        return raw.stream().limit(12).map(r -> {
            TickerReportResponse.AnalystRecommendation rec = new TickerReportResponse.AnalystRecommendation();
            rec.setPeriod(strVal(r, "period"));
            rec.setStrongBuy(r.get("strongBuy") != null ? ((Number) r.get("strongBuy")).intValue() : 0);
            rec.setBuy(r.get("buy") != null ? ((Number) r.get("buy")).intValue() : 0);
            rec.setHold(r.get("hold") != null ? ((Number) r.get("hold")).intValue() : 0);
            rec.setSell(r.get("sell") != null ? ((Number) r.get("sell")).intValue() : 0);
            rec.setStrongSell(r.get("strongSell") != null ? ((Number) r.get("strongSell")).intValue() : 0);
            return rec;
        }).collect(Collectors.toList());
    }

    private TickerReportResponse.PriceTarget parsePriceTarget(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) return null;
        TickerReportResponse.PriceTarget pt = new TickerReportResponse.PriceTarget();
        pt.setTargetHigh(numVal(raw, "targetHigh"));
        pt.setTargetLow(numVal(raw, "targetLow"));
        pt.setTargetMean(numVal(raw, "targetMean"));
        pt.setTargetMedian(numVal(raw, "targetMedian"));
        return pt;
    }

    private List<TickerReportResponse.EarningsData> parseEarnings(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyList();
        return raw.stream().limit(8).map(e -> {
            TickerReportResponse.EarningsData ed = new TickerReportResponse.EarningsData();
            ed.setPeriod(strVal(e, "period"));
            ed.setActual(numVal(e, "actual"));
            ed.setEstimate(numVal(e, "estimate"));
            ed.setSurprise(numVal(e, "surprise"));
            ed.setSurprisePercent(numVal(e, "surprisePercent"));
            return ed;
        }).collect(Collectors.toList());
    }

    private List<SectorReportResponse.SectorRotationSignal> generateRotationSignals(SectorReportResponse report) {
        List<SectorReportResponse.SectorRotationSignal> signals = new ArrayList<>();

        if (report.getRelativePerformancePercent() != null) {
            SectorReportResponse.SectorRotationSignal signal = new SectorReportResponse.SectorRotationSignal();
            BigDecimal rel = report.getRelativePerformancePercent();
            if (rel.compareTo(BigDecimal.valueOf(1)) > 0) {
                signal.setSignal("OUTPERFORMING");
                signal.setDescription(report.getSector() + " is outperforming the S&P 500 by " +
                        rel.setScale(2, RoundingMode.HALF_UP) + "%");
            } else if (rel.compareTo(BigDecimal.valueOf(-1)) < 0) {
                signal.setSignal("UNDERPERFORMING");
                signal.setDescription(report.getSector() + " is underperforming the S&P 500 by " +
                        rel.abs().setScale(2, RoundingMode.HALF_UP) + "%");
            } else {
                signal.setSignal("IN-LINE");
                signal.setDescription(report.getSector() + " is performing in line with the S&P 500");
            }
            signals.add(signal);
        }

        if (report.getAveragePE() != null) {
            SectorReportResponse.SectorRotationSignal peSignal = new SectorReportResponse.SectorRotationSignal();
            if (report.getAveragePE().compareTo(BigDecimal.valueOf(25)) > 0) {
                peSignal.setSignal("HIGH_VALUATION");
                peSignal.setDescription("Average P/E of " + report.getAveragePE() + " suggests elevated valuations");
            } else if (report.getAveragePE().compareTo(BigDecimal.valueOf(15)) < 0) {
                peSignal.setSignal("LOW_VALUATION");
                peSignal.setDescription("Average P/E of " + report.getAveragePE() + " suggests attractive valuations");
            } else {
                peSignal.setSignal("FAIR_VALUATION");
                peSignal.setDescription("Average P/E of " + report.getAveragePE() + " suggests fair valuations");
            }
            signals.add(peSignal);
        }

        return signals;
    }
}
