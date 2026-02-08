package com.portfolio.api.service;

import com.portfolio.api.dto.PortfolioRequest;
import com.portfolio.api.dto.PortfolioResponse;
import com.portfolio.api.dto.HoldingResponse;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.model.User;
import com.portfolio.api.repository.PortfolioRepository;
import com.portfolio.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
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
