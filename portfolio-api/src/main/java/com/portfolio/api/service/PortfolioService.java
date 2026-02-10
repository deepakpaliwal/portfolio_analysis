package com.portfolio.api.service;

import com.portfolio.api.dto.AllocationResponse;
import com.portfolio.api.dto.HoldingResponse;
import com.portfolio.api.dto.PortfolioRequest;
import com.portfolio.api.dto.PortfolioResponse;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.AssetType;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.model.User;
import com.portfolio.api.repository.HoldingRepository;
import com.portfolio.api.repository.PortfolioRepository;
import com.portfolio.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for portfolio management operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Service
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            UserRepository userRepository,
                            HoldingRepository holdingRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
    }

    /**
     * Creates a new portfolio for the given user.
     *
     * @param userId  the owner user ID
     * @param request the portfolio creation request
     * @return the created portfolio response
     * @throws ResourceNotFoundException if the user is not found
     */
    public PortfolioResponse createPortfolio(Long userId, PortfolioRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setBaseCurrency(request.getBaseCurrency());

        Portfolio saved = portfolioRepository.save(portfolio);
        return toResponse(saved);
    }

    /**
     * Retrieves all portfolios for a given user.
     *
     * @param userId the user ID
     * @return list of portfolio responses
     */
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPortfoliosByUser(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific portfolio by ID.
     *
     * @param portfolioId the portfolio ID
     * @return the portfolio response
     * @throws ResourceNotFoundException if the portfolio is not found
     */
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));
        return toResponse(portfolio);
    }

    /**
     * Updates an existing portfolio.
     *
     * @param portfolioId the portfolio ID to update
     * @param request     the update request
     * @return the updated portfolio response
     * @throws ResourceNotFoundException if the portfolio is not found
     */
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setBaseCurrency(request.getBaseCurrency());

        Portfolio saved = portfolioRepository.save(portfolio);
        return toResponse(saved);
    }

    /**
     * Deletes a portfolio by ID.
     *
     * @param portfolioId the portfolio ID to delete
     * @throws ResourceNotFoundException if the portfolio is not found
     */
    public void deletePortfolio(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio", "id", portfolioId);
        }
        portfolioRepository.deleteById(portfolioId);
    }

    @Transactional(readOnly = true)
    public AllocationResponse getAllocation(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        List<Holding> holdings = portfolio.getHoldings();

        BigDecimal totalCost = holdings.stream()
                .map(h -> h.getQuantity().multiply(h.getPurchasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AllocationResponse response = new AllocationResponse();
        response.setTotalCostBasis(totalCost);
        response.setByAssetType(buildAllocation(holdings, totalCost, h -> h.getAssetType().name()));
        response.setBySector(buildAllocation(holdings, totalCost, h -> h.getSector() != null ? h.getSector() : "Unknown"));
        response.setByCurrency(buildAllocation(holdings, totalCost, Holding::getCurrency));
        return response;
    }

    private List<AllocationResponse.AllocationEntry> buildAllocation(
            List<Holding> holdings, BigDecimal total,
            java.util.function.Function<Holding, String> classifier) {

        Map<String, BigDecimal> groups = new LinkedHashMap<>();
        for (Holding h : holdings) {
            String key = classifier.apply(h);
            BigDecimal cost = h.getQuantity().multiply(h.getPurchasePrice());
            groups.merge(key, cost, BigDecimal::add);
        }

        return groups.entrySet().stream()
                .map(e -> new AllocationResponse.AllocationEntry(
                        e.getKey(),
                        e.getValue(),
                        total.compareTo(BigDecimal.ZERO) > 0
                                ? e.getValue().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    public List<HoldingResponse> importHoldingsFromCsv(Long portfolioId, InputStream csvStream) throws IOException {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        List<Holding> imported = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");
            int assetTypeIdx = findColumn(headers, "asset_type");
            int tickerIdx = findColumn(headers, "ticker");
            int nameIdx = findColumnOptional(headers, "name");
            int quantityIdx = findColumn(headers, "quantity");
            int priceIdx = findColumn(headers, "purchase_price");
            int dateIdx = findColumn(headers, "purchase_date");
            int currencyIdx = findColumnOptional(headers, "currency");
            int sectorIdx = findColumnOptional(headers, "sector");
            int categoryIdx = findColumnOptional(headers, "category");

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(",", -1);
                try {
                    Holding h = new Holding();
                    h.setPortfolio(portfolio);
                    h.setAssetType(AssetType.valueOf(cols[assetTypeIdx].trim().toUpperCase()));
                    h.setTicker(cols[tickerIdx].trim());
                    h.setName(nameIdx >= 0 ? cols[nameIdx].trim() : null);
                    h.setQuantity(new BigDecimal(cols[quantityIdx].trim()));
                    h.setPurchasePrice(new BigDecimal(cols[priceIdx].trim()));
                    h.setPurchaseDate(LocalDate.parse(cols[dateIdx].trim()));
                    h.setCurrency(currencyIdx >= 0 && !cols[currencyIdx].trim().isEmpty()
                            ? cols[currencyIdx].trim() : "USD");
                    h.setSector(sectorIdx >= 0 ? cols[sectorIdx].trim() : null);
                    h.setCategory(categoryIdx >= 0 ? cols[categoryIdx].trim() : null);
                    imported.add(h);
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    throw new IllegalArgumentException("Error on CSV line " + lineNum + ": " + e.getMessage());
                }
            }
        }

        List<Holding> saved = holdingRepository.saveAll(imported);
        return saved.stream().map(h -> {
            HoldingResponse r = new HoldingResponse();
            r.setId(h.getId());
            r.setAssetType(h.getAssetType());
            r.setTicker(h.getTicker());
            r.setName(h.getName());
            r.setQuantity(h.getQuantity());
            r.setPurchasePrice(h.getPurchasePrice());
            r.setPurchaseDate(h.getPurchaseDate());
            r.setCurrency(h.getCurrency());
            r.setSector(h.getSector());
            r.setCategory(h.getCategory());
            r.setCreatedAt(h.getCreatedAt());
            r.setUpdatedAt(h.getUpdatedAt());
            return r;
        }).collect(Collectors.toList());
    }

    private int findColumn(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        throw new IllegalArgumentException("Required CSV column missing: " + name);
    }

    private int findColumnOptional(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.setId(portfolio.getId());
        response.setName(portfolio.getName());
        response.setDescription(portfolio.getDescription());
        response.setBaseCurrency(portfolio.getBaseCurrency());
        response.setHoldingCount(portfolio.getHoldings().size());
        response.setHoldings(portfolio.getHoldings().stream().map(h -> {
            HoldingResponse hr = new HoldingResponse();
            hr.setId(h.getId());
            hr.setAssetType(h.getAssetType());
            hr.setTicker(h.getTicker());
            hr.setName(h.getName());
            hr.setQuantity(h.getQuantity());
            hr.setPurchasePrice(h.getPurchasePrice());
            hr.setPurchaseDate(h.getPurchaseDate());
            hr.setCurrency(h.getCurrency());
            hr.setSector(h.getSector());
            hr.setCategory(h.getCategory());
            hr.setCreatedAt(h.getCreatedAt());
            hr.setUpdatedAt(h.getUpdatedAt());
            return hr;
        }).collect(Collectors.toList()));
        response.setCreatedAt(portfolio.getCreatedAt());
        response.setUpdatedAt(portfolio.getUpdatedAt());
        return response;
    }
}
