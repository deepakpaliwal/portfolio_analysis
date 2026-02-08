package com.portfolio.api.repository;

import com.portfolio.api.model.AssetType;
import com.portfolio.api.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Holding entity operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    List<Holding> findByPortfolioIdAndAssetType(Long portfolioId, AssetType assetType);

    long countByPortfolioId(Long portfolioId);
}
