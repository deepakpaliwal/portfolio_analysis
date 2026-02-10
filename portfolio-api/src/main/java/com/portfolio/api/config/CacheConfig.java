package com.portfolio.api.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache("quotes"),
                new ConcurrentMapCache("fxRates")
        ));
        return cacheManager;
    }

    /**
     * Evict all caches every 60 seconds so prices stay reasonably fresh.
     */
    @Scheduled(fixedRate = 60000)
    public void evictCaches() {
        CacheManager cm = cacheManager();
        cm.getCacheNames().forEach(name -> {
            var cache = cm.getCache(name);
            if (cache != null) cache.clear();
        });
    }
}
