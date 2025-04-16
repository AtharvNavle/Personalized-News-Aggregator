package com.newsaggregator.util;

import com.newsaggregator.model.Article;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Caching service for news articles to minimize API calls.
 * Implements the Singleton pattern.
 */
public class NewsCache {
    private static final Logger LOGGER = Logger.getLogger(NewsCache.class.getName());
    private static final long CACHE_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(15); // 15 minutes cache expiry
    
    private static NewsCache instance;
    private final Map<String, CacheEntry> cache;
    
    /**
     * Private constructor for singleton pattern.
     */
    private NewsCache() {
        this.cache = new HashMap<>();
    }
    
    /**
     * Gets the singleton instance of NewsCache.
     *
     * @return the NewsCache instance
     */
    public static synchronized NewsCache getInstance() {
        if (instance == null) {
            instance = new NewsCache();
        }
        return instance;
    }
    
    /**
     * Caches a list of articles with the given key.
     *
     * @param key      the cache key
     * @param articles the articles to cache
     */
    public void cacheArticles(String key, List<Article> articles) {
        cache.put(key, new CacheEntry(articles, System.currentTimeMillis()));
        LOGGER.info("Cached articles for key: " + key);
    }
    
    /**
     * Gets articles from cache if available and not expired.
     *
     * @param key the cache key
     * @return the list of articles or null if not in cache or expired
     */
    public List<Article> getArticles(String key) {
        CacheEntry entry = cache.get(key);
        
        // If entry doesn't exist or is expired, return null
        if (entry == null || System.currentTimeMillis() - entry.getTimestamp() > CACHE_EXPIRY_TIME) {
            if (entry != null) {
                // Remove expired entry
                cache.remove(key);
                LOGGER.info("Cache expired for key: " + key);
            }
            return null;
        }
        
        LOGGER.info("Cache hit for key: " + key);
        return entry.getArticles();
    }
    
    /**
     * Clears the entire cache.
     */
    public void clearCache() {
        cache.clear();
        LOGGER.info("Cache cleared");
    }
    
    /**
     * Clears a specific entry from the cache.
     *
     * @param key the cache key to clear
     */
    public void clearCacheEntry(String key) {
        cache.remove(key);
        LOGGER.info("Cache entry cleared for key: " + key);
    }
    
    /**
     * Inner class representing a cache entry with timestamp.
     */
    private static class CacheEntry {
        private final List<Article> articles;
        private final long timestamp;
        
        /**
         * Constructor for CacheEntry.
         *
         * @param articles  the articles to cache
         * @param timestamp the time when the articles were cached
         */
        public CacheEntry(List<Article> articles, long timestamp) {
            this.articles = articles;
            this.timestamp = timestamp;
        }
        
        /**
         * Gets the articles in this cache entry.
         *
         * @return the articles
         */
        public List<Article> getArticles() {
            return articles;
        }
        
        /**
         * Gets the timestamp of this cache entry.
         *
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
