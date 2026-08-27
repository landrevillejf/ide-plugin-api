package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginCacheService.EvictionPolicy;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Mutation-killing tests for {@link DefaultPluginCacheService} and its
 * private inner classes {@code PluginCache} / {@code CacheEntry}.
 */
@DisplayName("DefaultPluginCacheService mutation tests")
class DefaultPluginCacheServiceMutationTest {

    private static final String P = "cache-plugin";

    private DefaultPluginCacheService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPluginCacheService();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Object innerCache(String pluginId) throws Exception {
        Field field = DefaultPluginCacheService.class.getDeclaredField("pluginCaches");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> caches = (Map<String, Object>) field.get(service);
        return caches.get(pluginId);
    }

    private Object newEntry(long expiryTime) throws Exception {
        Class<?> cls = Class.forName(
                "com.protonmail.landrevillejf.ide.plugin.service.impl.DefaultPluginCacheService$CacheEntry");
        Constructor<?> ctor = cls.getDeclaredConstructor(Object.class, long.class);
        ctor.setAccessible(true);
        return ctor.newInstance("value", expiryTime);
    }

    private boolean isExpiredAt(Object cacheEntry, long currentTime) throws Exception {
        Method method = cacheEntry.getClass().getDeclaredMethod("isExpired", long.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(cacheEntry, currentTime);
    }

    private boolean isExpiredNow(Object cacheEntry) throws Exception {
        Method method = cacheEntry.getClass().getDeclaredMethod("isExpired");
        method.setAccessible(true);
        return (Boolean) method.invoke(cacheEntry);
    }

    @Nested
    @DisplayName("basic operations")
    class BasicOperationTests {

        @Test
        @DisplayName("constructor logs initialization")
        void constructorLogs() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                new DefaultPluginCacheService();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("DefaultPluginCacheService initialized"));
            }
        }

        @Test
        @DisplayName("put and get round-trip with hit/miss logging")
        void putAndGet() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.put(P, "key", "value");
                assertThat(service.get(P, "key")).isEqualTo("value");
                assertThat(service.get(P, "missing")).isNull();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cache put: plugin=cache-plugin, key=key, ttl=-1ms"))
                        .anyMatch(m -> m.contains("Cache hit: plugin=cache-plugin, key=key"))
                        .anyMatch(m -> m.contains("Cache miss: plugin=cache-plugin, key=missing"));
            }
        }

        @Test
        @DisplayName("get of unknown plugin returns null")
        void getUnknownPlugin() {
            assertThat(service.get("ghost", "key")).isNull();
        }

        @Test
        @DisplayName("typed get filters by value class")
        void typedGet() {
            service.put(P, "number", 42);
            assertThat(service.get(P, "number", Integer.class)).isEqualTo(42);
            assertThat(service.get(P, "number", String.class)).isNull();
            assertThat(service.get(P, "missing", String.class)).isNull();
        }

        @Test
        @DisplayName("containsKey covers present, absent, expired and unknown plugin")
        void containsKey() {
            service.put(P, "live", "v");
            service.put(P, "gone", "v", 5);
            sleep(30);

            assertThat(service.containsKey(P, "live")).isTrue();
            assertThat(service.containsKey(P, "absent")).isFalse();
            assertThat(service.containsKey(P, "gone")).isFalse();
            assertThat(service.containsKey("ghost", "live")).isFalse();
        }

        @Test
        @DisplayName("remove deletes the entry and logs")
        void remove() {
            service.put(P, "key", "value");
            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.remove(P, "key");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cache remove"));
            }
            assertThat(service.containsKey(P, "key")).isFalse();
            assertThat(service.size(P)).isZero();

            // removing from an unknown plugin is a silent no-op
            service.remove("ghost", "key");
        }

        @Test
        @DisplayName("clear empties the cache and resets statistics")
        void clear() {
            service.put(P, "a", 1);
            service.put(P, "b", 2);
            service.get(P, "a"); // hit
            service.get(P, "zz"); // miss

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.clear(P);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cache cleared: plugin=cache-plugin"));
            }
            assertThat(service.size(P)).isZero();
            assertThat(service.getStatistics(P))
                    .contains(entry("hits", 0L), entry("misses", 0L));

            // clearing an unknown plugin is a silent no-op
            service.clear("ghost");
        }

        @Test
        @DisplayName("clearAll wipes every plugin cache")
        void clearAll() {
            service.put("p1", "k", 1);
            service.put("p2", "k", 2);

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.clearAll();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("All caches cleared"));
            }
            assertThat(service.size("p1")).isZero();
            assertThat(service.size("p2")).isZero();
        }

        @Test
        @DisplayName("size of unknown plugin is zero")
        void sizeUnknown() {
            assertThat(service.size("ghost")).isZero();
        }

        @Test
        @DisplayName("getKeys returns exact keys or empty for unknown plugin")
        void keys() {
            service.put(P, "alpha", 1);
            service.put(P, "beta", 2);
            assertThat(service.getKeys(P)).containsExactlyInAnyOrder("alpha", "beta");
            assertThat(service.getKeys("ghost")).isEmpty();
        }
    }

    @Nested
    @DisplayName("TTL handling")
    class TtlTests {

        @Test
        @DisplayName("expired entry behaves as a miss and is dropped")
        void expiredEntry() {
            service.put(P, "gone", "value", 5);
            sleep(30);

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                assertThat(service.get(P, "gone")).isNull();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cache miss"));
            }
            assertThat(service.containsKey(P, "gone")).isFalse();
            assertThat(service.size(P)).isZero();
            assertThat(service.getStatistics(P)).containsEntry("misses", 1L);
        }

        @Test
        @DisplayName("zero and negative TTL never expire")
        void noTtlNeverExpires() {
            service.put(P, "zero", "v0", 0);
            service.put(P, "negative", "v1", -1);
            sleep(30);

            assertThat(service.get(P, "zero")).isEqualTo("v0");
            assertThat(service.get(P, "negative")).isEqualTo("v1");
        }
    }

    @Nested
    @DisplayName("eviction")
    class EvictionTests {

        @Test
        @DisplayName("LRU evicts the least recently used entry")
        void lruRecency() {
            service.setMaxSize(P, 2);
            service.put(P, "a", 1);
            service.put(P, "b", 2);
            service.put(P, "a", 11); // refresh recency of "a"
            service.put(P, "c", 3);  // must evict "b"

            assertThat(service.get(P, "b")).isNull();
            assertThat(service.get(P, "a")).isEqualTo(11);
            assertThat(service.get(P, "c")).isEqualTo(3);
            assertThat(service.getStatistics(P)).containsEntry("evictions", 1L);
        }

        @Test
        @DisplayName("FIFO evicts by insertion order regardless of access")
        void fifoInsertionOrder() {
            service.setEvictionPolicy(P, EvictionPolicy.FIFO);
            service.setMaxSize(P, 2);
            service.put(P, "a", 1);
            service.put(P, "b", 2);
            service.get(P, "a");     // access must NOT reorder FIFO
            service.put(P, "c", 3);  // must evict "a"

            assertThat(service.get(P, "a")).isNull();
            assertThat(service.get(P, "b")).isEqualTo(2);
            assertThat(service.get(P, "c")).isEqualTo(3);
        }

        @Test
        @DisplayName("LFU eviction path evicts an entry")
        void lfuEvicts() {
            service.setEvictionPolicy(P, EvictionPolicy.LFU);
            service.setMaxSize(P, 2);
            service.put(P, "a", 1);
            service.put(P, "b", 2);

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.put(P, "c", 3);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Evicted entry") && m.contains("LFU"));
            }
            assertThat(service.size(P)).isEqualTo(2);
            assertThat(service.getStatistics(P)).containsEntry("evictions", 1L);
        }

        @Test
        @DisplayName("cache at exactly maxSize is not evicted")
        void evictionBoundary() {
            service.setMaxSize(P, 2);
            service.put(P, "a", 1);
            service.put(P, "b", 2);
            assertThat(service.size(P)).isEqualTo(2);
        }

        @Test
        @DisplayName("setMaxSize shrinks cache down to the new limit")
        void shrinkOnMaxSize() {
            service.put(P, "a", 1);
            service.put(P, "b", 2);
            service.put(P, "c", 3);

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.setMaxSize(P, 1);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Max size set"));
            }
            assertThat(service.size(P)).isEqualTo(1);
            assertThat(service.getStatistics(P)).containsEntry("evictions", 2L);
        }

        @Test
        @DisplayName("evict on an empty cache is a no-op")
        void evictEmptyCache() throws Exception {
            service.setMaxSize("empty", 5);
            Object cache = innerCache("empty");
            Method evict = cache.getClass().getDeclaredMethod("evict");
            evict.setAccessible(true);
            evict.invoke(cache);

            assertThat(service.getStatistics("empty"))
                    .contains(entry("size", 0), entry("evictions", 0L));
        }
    }

    @Nested
    @DisplayName("statistics")
    class StatisticsTests {

        @Test
        @DisplayName("statistics map is exact")
        void exactStatistics() {
            service.put(P, "key", "value");
            service.get(P, "key");      // hit
            service.get(P, "missing");  // miss

            Map<String, Object> stats = service.getStatistics(P);
            assertThat(stats).containsExactly(
                    entry("size", 1),
                    entry("maxSize", 1000),
                    entry("hits", 1L),
                    entry("misses", 1L),
                    entry("evictions", 0L),
                    entry("hitRatio", 0.5),
                    entry("evictionPolicy", "LRU"));
        }

        @Test
        @DisplayName("hit ratio is zero without any accesses")
        void zeroHitRatio() {
            service.put(P, "key", "value");
            assertThat(service.getStatistics(P)).containsEntry("hitRatio", 0.0);
        }

        @Test
        @DisplayName("resetStatistics zeroes all counters")
        void resetStatistics() {
            service.setMaxSize(P, 1);
            service.put(P, "a", 1);
            service.put(P, "b", 2); // eviction
            service.get(P, "b");    // hit
            service.get(P, "zz");   // miss

            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.resetStatistics(P);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Statistics reset"));
            }
            assertThat(service.getStatistics(P)).contains(
                    entry("hits", 0L), entry("misses", 0L), entry("evictions", 0L));

            // resetting an unknown plugin is a silent no-op
            service.resetStatistics("ghost");
        }

        @Test
        @DisplayName("statistics of unknown plugin is an empty map")
        void statisticsUnknown() {
            assertThat(service.getStatistics("ghost")).isEmpty();
        }

        @Test
        @DisplayName("eviction policy is reported in statistics")
        void policyInStatistics() {
            service.setEvictionPolicy(P, EvictionPolicy.FIFO);
            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                service.setEvictionPolicy(P, EvictionPolicy.LFU);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Eviction policy set") && m.contains("LFU"));
            }
            assertThat(service.getStatistics(P)).containsEntry("evictionPolicy", "LFU");
        }
    }

    @Nested
    @DisplayName("periodic cleanup and CacheEntry boundaries")
    class CleanupTests {

        @Test
        @DisplayName("cleanupExpiredEntries removes only expired entries")
        void cleanupExpiredEntries() throws Exception {
            service.put(P, "gone", "v", 5);
            service.put(P, "stay", "v");
            sleep(30);

            Method cleanup = DefaultPluginCacheService.class
                    .getDeclaredMethod("cleanupExpiredEntries");
            cleanup.setAccessible(true);
            try (LogCapture capture = LogCapture.attach(DefaultPluginCacheService.class)) {
                cleanup.invoke(service);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Removed expired entry") && m.contains("key=gone"))
                        .anyMatch(m -> m.contains("Expired entries cleanup completed"));
            }
            assertThat(service.size(P)).isEqualTo(1);
            assertThat(service.containsKey(P, "stay")).isTrue();
            assertThat(service.containsKey(P, "gone")).isFalse();
        }

        @Test
        @DisplayName("CacheEntry expiry boundary is strict")
        void cacheEntryBoundaries() throws Exception {
            assertThat(isExpiredAt(newEntry(100), 100)).isFalse();
            assertThat(isExpiredAt(newEntry(100), 101)).isTrue();
            assertThat(isExpiredAt(newEntry(100), 99)).isFalse();
            assertThat(isExpiredAt(newEntry(0), Long.MAX_VALUE)).isFalse();
            assertThat(isExpiredAt(newEntry(-1), Long.MAX_VALUE)).isFalse();
        }

        @Test
        @DisplayName("CacheEntry isExpired uses the wall clock")
        void cacheEntryWallClock() throws Exception {
            assertThat(isExpiredNow(newEntry(100))).isTrue();
            assertThat(isExpiredNow(newEntry(Long.MAX_VALUE))).isFalse();
        }
    }
}
