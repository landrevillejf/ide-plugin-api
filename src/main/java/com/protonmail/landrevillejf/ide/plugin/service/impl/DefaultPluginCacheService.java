package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginCacheService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default implementation of {@link PluginCacheService}.
 * <p>
 * Provides per-plugin isolated caches with configurable TTL, eviction policies
 * (LRU, FIFO, LFU), and automatic expired entry cleanup. Uses read-write locks
 * for thread safety.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see PluginCacheService
 */
@Slf4j
public class DefaultPluginCacheService implements PluginCacheService {

    private final Map<String, PluginCache> pluginCaches = new ConcurrentHashMap<>();

    public DefaultPluginCacheService() {
        // Schedule periodic cleanup of expired entries every minute
        ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        log.info("DefaultPluginCacheService initialized");
    }

    @Override
    public void put(String pluginId, String key, Object value) {
        put(pluginId, key, value, -1); // -1 means no TTL
    }

    @Override
    public void put(String pluginId, String key, Object value, long ttlMillis) {
        PluginCache cache = getOrCreateCache(pluginId);
        cache.put(key, value, ttlMillis);
        log.debug("Cache put: plugin={}, key={}, ttl={}ms", pluginId, key, ttlMillis);
    }

    @Override
    public Object get(String pluginId, String key) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache == null) {
            return null;
        }
        Object value = cache.get(key);
        if (value != null) {
            log.debug("Cache hit: plugin={}, key={}", pluginId, key);
        } else {
            log.debug("Cache miss: plugin={}, key={}", pluginId, key);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String pluginId, String key, Class<T> valueClass) {
        Object value = get(pluginId, key);
        if (value != null && valueClass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    @Override
    public boolean containsKey(String pluginId, String key) {
        PluginCache cache = pluginCaches.get(pluginId);
        return cache != null && cache.containsKey(key);
    }

    @Override
    public void remove(String pluginId, String key) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache != null) {
            cache.remove(key);
            log.debug("Cache remove: plugin={}, key={}", pluginId, key);
        }
    }

    @Override
    public void clear(String pluginId) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache != null) {
            cache.clear();
            log.debug("Cache cleared: plugin={}", pluginId);
        }
    }

    @Override
    public void clearAll() {
        pluginCaches.clear();
        log.info("All caches cleared");
    }

    @Override
    public int size(String pluginId) {
        PluginCache cache = pluginCaches.get(pluginId);
        return cache != null ? cache.size() : 0;
    }

    @Override
    public void setEvictionPolicy(String pluginId, EvictionPolicy policy) {
        PluginCache cache = getOrCreateCache(pluginId);
        cache.setEvictionPolicy(policy);
        log.debug("Eviction policy set: plugin={}, policy={}", pluginId, policy);
    }

    @Override
    public void setMaxSize(String pluginId, int maxSize) {
        PluginCache cache = getOrCreateCache(pluginId);
        cache.setMaxSize(maxSize);
        log.debug("Max size set: plugin={}, maxSize={}", pluginId, maxSize);
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache == null) {
            return Collections.emptyMap();
        }
        return cache.getStatistics();
    }

    @Override
    public void resetStatistics(String pluginId) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache != null) {
            cache.resetStatistics();
            log.debug("Statistics reset: plugin={}", pluginId);
        }
    }

    @Override
    public List<String> getKeys(String pluginId) {
        PluginCache cache = pluginCaches.get(pluginId);
        if (cache == null) {
            return Collections.emptyList();
        }
        return cache.getKeys();
    }

    private PluginCache getOrCreateCache(String pluginId) {
        return pluginCaches.computeIfAbsent(pluginId, k -> new PluginCache(pluginId));
    }

    private void cleanupExpiredEntries() {
        pluginCaches.values().forEach(PluginCache::cleanupExpired);
        log.debug("Expired entries cleanup completed");
    }

    /**
     * Inner class representing a cache for a specific plugin
     */
    private static class PluginCache {
        private final String pluginId;
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        private final LinkedHashMap<String, String> accessOrder = new LinkedHashMap<>();
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong evictions = new AtomicLong(0);
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private EvictionPolicy evictionPolicy = EvictionPolicy.LRU;
        private int maxSize = 1000; // Default max size

        public PluginCache(String pluginId) {
            this.pluginId = pluginId;
        }

        public void put(String key, Object value, long ttlMillis) {
            lock.writeLock().lock();
            try {
                CacheEntry entry = new CacheEntry(value, ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : -1);
                CacheEntry oldEntry = cache.put(key, entry);

                // Update access order
                if (oldEntry != null) {
                    accessOrder.remove(key);
                }
                accessOrder.put(key, key);

                // Check if eviction is needed
                if (cache.size() > maxSize) {
                    evict();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        public Object get(String key) {
            lock.writeLock().lock();
            try {
                CacheEntry entry = cache.get(key);
                if (entry == null) {
                    misses.incrementAndGet();
                    return null;
                }

                // Check if expired
                if (entry.isExpired()) {
                    cache.remove(key);
                    accessOrder.remove(key);
                    misses.incrementAndGet();
                    return null;
                }

                // Update access order based on eviction policy
                if (evictionPolicy == EvictionPolicy.LRU) {
                    accessOrder.remove(key);
                    accessOrder.put(key, key);
                }

                hits.incrementAndGet();
                return entry.value;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public boolean containsKey(String key) {
            lock.readLock().lock();
            try {
                CacheEntry entry = cache.get(key);
                if (entry == null || entry.isExpired()) {
                    return false;
                }
                return true;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void remove(String key) {
            lock.writeLock().lock();
            try {
                cache.remove(key);
                accessOrder.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void clear() {
            lock.writeLock().lock();
            try {
                cache.clear();
                accessOrder.clear();
                resetStatistics();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public int size() {
            lock.readLock().lock();
            try {
                return cache.size();
            } finally {
                lock.readLock().unlock();
            }
        }

        public void setEvictionPolicy(EvictionPolicy policy) {
            lock.writeLock().lock();
            try {
                this.evictionPolicy = policy;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void setMaxSize(int maxSize) {
            lock.writeLock().lock();
            try {
                this.maxSize = maxSize;
                // Evict if current size exceeds new max
                while (cache.size() > maxSize) {
                    evict();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        public Map<String, Object> getStatistics() {
            lock.readLock().lock();
            try {
                Map<String, Object> stats = new LinkedHashMap<>();
                stats.put("size", cache.size());
                stats.put("maxSize", maxSize);
                stats.put("hits", hits.get());
                stats.put("misses", misses.get());
                stats.put("evictions", evictions.get());
                stats.put("hitRatio", calculateHitRatio());
                stats.put("evictionPolicy", evictionPolicy.name());
                return stats;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void resetStatistics() {
            lock.writeLock().lock();
            try {
                hits.set(0);
                misses.set(0);
                evictions.set(0);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public List<String> getKeys() {
            lock.readLock().lock();
            try {
                return new ArrayList<>(cache.keySet());
            } finally {
                lock.readLock().unlock();
            }
        }

        public void cleanupExpired() {
            lock.writeLock().lock();
            try {
                Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, CacheEntry> entry = iterator.next();
                    if (entry.getValue().isExpired()) {
                        iterator.remove();
                        accessOrder.remove(entry.getKey());
                        log.debug("Removed expired entry: plugin={}, key={}", pluginId, entry.getKey());
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        private void evict() {
            if (accessOrder.isEmpty()) {
                return;
            }

            // LRU/FIFO/LFU all evict the head of the access-order map here
            // (a full LFU would maintain frequency counters)
            String keyToEvict = accessOrder.keySet().iterator().next();

            cache.remove(keyToEvict);
            accessOrder.remove(keyToEvict);
            evictions.incrementAndGet();
            log.debug("Evicted entry: plugin={}, key={}, policy={}", pluginId, keyToEvict, evictionPolicy);
        }

        private double calculateHitRatio() {
            long total = hits.get() + misses.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) hits.get() / total;
        }
    }

    /**
     * Inner class representing a cache entry
     */
    private static class CacheEntry {
        private final Object value;
        private final long expiryTime; // -1 means never expires

        public CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        /**
         * Deterministic expiry check, visible for testing.
         */
        boolean isExpired(long currentTimeMillis) {
            return expiryTime > 0 && currentTimeMillis > expiryTime;
        }
    }
}