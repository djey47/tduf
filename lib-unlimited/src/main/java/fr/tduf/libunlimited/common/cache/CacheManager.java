package fr.tduf.libunlimited.common.cache;

import com.esotericsoftware.minlog.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.esotericsoftware.minlog.Log.DEBUG;

/**
 * Generic manager to simplify caching of items
 */
public enum CacheManager {

    /**
     * Describes unique instance
     */
    it(new CacheManagerInstance(false));

    private static final String LOG_CATEGORY = CacheManager.class.getSimpleName();

    private final CacheManagerInstance cacheManagerInstance;

    CacheManager(CacheManagerInstance cacheManagerInstance) {
        this.cacheManagerInstance = cacheManagerInstance;
    }

    /**
     * @return unique instance of manager
     */
    public CacheManagerInstance self() {
        return cacheManagerInstance;
    }

    public static class CacheManagerInstance {

        private final boolean disabled;
        private final Map<String, Map<String, ?>> stores;

        private CacheManagerInstance(boolean enabled) {
            this.disabled = !enabled;
            this.stores = new ConcurrentHashMap<>(16);
        }

        /**
         * Updates store with the result.
         * @param storeName : store in which key should be located
         * @param key       : key to the value to be retrieved
         * @param supplier  : describes processing to get a brand new value
         * @param <R>       : type of value to be Teturned
         * @return value from cache in store if existing, otherwise value returned by call to supplier.
         */
        public <R> R getValueFromKey(String storeName, String key, Supplier<R> supplier) {
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier instance must not be null");
            }

            if (!disabled) {
                if (storeName == null || key == null) {
                    return supplier.get();
                }

                stores.putIfAbsent(storeName, new ConcurrentHashMap<>(16));
                //noinspection unchecked
                Map<String, R> storeMap = (Map<String, R>) stores.get(storeName);

                R previousValue = storeMap.putIfAbsent(key, supplier.get());
                if (DEBUG) {
                    String storeKeyLabel = "{" + storeName + ", " + key + "}";
                    if (previousValue == null) {
                        Log.debug(LOG_CATEGORY, "Cache miss! " + storeKeyLabel);
                    } else {
                        Log.debug(LOG_CATEGORY, "Cache hit! " + storeKeyLabel);
                    }
                }
                return storeMap.get(key);
            }

            return supplier.get();
        }

        /**
         * Removes all cached information.
         */
        public void clearAllStores() {
            if (disabled) {
                return;
            }

            stores.clear();

            Log.debug(LOG_CATEGORY, "All stores were cleared!");
        }

        /**
         * @param storeName : name of store in which all keys will be removed.
         */
        public void clearStoreByName(String storeName) {
            if (disabled) {
                return;
            }

            stores.getOrDefault(storeName, new HashMap<>()).clear();

            Log.debug(LOG_CATEGORY, "Cleared store! {" + storeName + "}");
        }

        /**
         * @return true if caching feature is enabled.
         */
        public boolean isDisabled() {
            return disabled;
        }
    }
}
