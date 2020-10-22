package fr.tduf.libunlimited.common.cache;

import com.esotericsoftware.minlog.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.esotericsoftware.minlog.Log.LEVEL_INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CacheManagerTest {

    private CacheManager.CacheManagerInstance cacheManagerInstance;

    @BeforeEach
    public void setUp() {
        Log.set(LEVEL_INFO);

        cacheManagerInstance = CacheManager.it.self();
    }

    @AfterEach
    public void tearDown() {
        cacheManagerInstance.clearAllStores();
    }

    @Test
    public void getValueFromKey_whenNullStore_shouldCallRealMethod(){
        // GIVEN-WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey(null, "key", () -> Optional.of("result"));

        // THEN
        assertThat(actualValue).contains("result");
    }

    @Test
    public void getValueFromKey_whenNullStore_andNullKey_shouldCallRealMethod(){
        // GIVEN-WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey(null, null, () -> Optional.of("result"));

        // THEN
        assertThat(actualValue).contains("result");
    }

    @Test
    public void getValueFromKey_whenNullCallback_shouldThrowException(){
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> cacheManagerInstance.getValueFromKey(null, null, null));
    }

    @Test
    public void getValueFromKey_whenStoreDoesNotExist_shouldCallRealMethod(){
        // GIVEN-WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("storeNotFound", "key", () -> Optional.of("result"));

        // THEN
        assertThat(actualValue).contains("result");
    }

    @Test
    public void getValueFromKey_whenStoreExists_butKeyDoesNot_shouldCallRealMethod(){
        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result"));

        // WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "newKey", Optional::empty);

        // THEN
        assertThat(actualValue).isEmpty();
    }

    @Test
    public void getValueFromKey_whenStoreExists_andKeyAsWell_shouldRetrieveValueFromCache_andNotCallRealMethod(){
        if (cacheManagerInstance.isDisabled()) {
            return;
        }

        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result"));

        // WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("uncached result"));

        // THEN
        assertThat(actualValue).contains("result");
    }

    @Test
    public void clearAllStores_shouldForceCallRealMethod() {
        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result to be cached"));

        // WHEN
        cacheManagerInstance.clearAllStores();
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("real result"));

        // THEN
        assertThat(actualValue).contains("real result");
    }

    @Test
    public void clearStoreByName_whenStoreExists_shouldForceCallRealMethod() {
        if (cacheManagerInstance.isDisabled()) {
            return;
        }

        // GIVEN
        cacheManagerInstance.getValueFromKey("store1", "key", () -> Optional.of("result to be cached in store 1"));
        cacheManagerInstance.getValueFromKey("store2", "key", () -> Optional.of("result to be cached in store 2"));

        // WHEN
        cacheManagerInstance.clearStoreByName("store1");
        Optional<String> actualValueFromStore1 = cacheManagerInstance.getValueFromKey("store1", "key", () -> Optional.of("real result"));
        Optional<String> actualValueFromStore2 = cacheManagerInstance.getValueFromKey("store2", "key", () -> Optional.of("real result"));

        // THEN
        assertThat(actualValueFromStore1).contains("real result");
        assertThat(actualValueFromStore2).contains("result to be cached in store 2");
    }

    @Test
    public void clearStoreByName_whenStoreDoesNotExist_shouldRetrieveValueFromCache() {
        if (cacheManagerInstance.isDisabled()) {
            return;
        }

        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result to be cached"));

        // WHEN
        cacheManagerInstance.clearStoreByName("newStore");
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("real result"));

        // THEN
        assertThat(actualValue).contains("result to be cached");
    }

    @Test
    public void clearStoreByName_whenStoreExists_shouldNotAffectOtherStore() {
        if (cacheManagerInstance.isDisabled()) {
            return;
        }

        // GIVEN
        cacheManagerInstance.getValueFromKey("store1", "key1", () -> Optional.of("result1 to be cached"));
        cacheManagerInstance.getValueFromKey("store2", "key2", () -> Optional.of("result2 to be cached"));

        // WHEN
        cacheManagerInstance.clearStoreByName("store1");
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store2", "key2", Optional::empty);

        // THEN
        assertThat(actualValue).contains("result2 to be cached");
    }
}
