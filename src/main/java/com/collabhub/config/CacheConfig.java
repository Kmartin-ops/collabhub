package com.collabhub.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching  // activates Spring's caching infrastructure
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Default spec — applies to all caches unless overridden
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)               // max 500 entries per cache
                .expireAfterWrite(5, TimeUnit.MINUTES)  // expire 5 mins after write
                .recordStats());                // enable hit/miss metrics

        return manager;
    }
}