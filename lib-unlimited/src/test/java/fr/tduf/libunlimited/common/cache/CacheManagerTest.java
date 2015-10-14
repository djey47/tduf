package fr.tduf.libunlimited.common.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class CacheManagerTest {

    private CacheManager.CacheManagerInstance cacheManagerInstance;

    @Before
    public void setUp() {
        cacheManagerInstance = CacheManager.it.self();
    }

    @After
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

    @Test(expected = NullPointerException.class)
    public void getValueFromKey_whenNullCallback_shouldThrowException(){
        // GIVEN-WHEN
        cacheManagerInstance.getValueFromKey(null, null, null);

        // THEN: NPE
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
        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result"));

        // WHEN
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "key", Optional::empty);

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
        // GIVEN
        cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("result to be cached"));

        // WHEN
        cacheManagerInstance.clearStoreByName("store");
        Optional<String> actualValue = cacheManagerInstance.getValueFromKey("store", "key", () -> Optional.of("real result"));

        // THEN
        assertThat(actualValue).contains("real result");
    }

    @Test
    public void clearStoreByName_whenStoreDoesNotExist_shouldRetrieveValueFromCache() {
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
