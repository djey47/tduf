package fr.tduf.libunlimited.low.files.research.domain;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture.createEmptyStore;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DataStoreTest {
    private DataStore dataStore;

    @BeforeEach
    void setUp() throws IOException {
        Log.set(Log.LEVEL_INFO);

        dataStore = createEmptyStore();
    }

    @Test
    void clearAll_shouldRemoveAllEntries() {
        // GIVEN
        DataStoreFixture.putStringInStore("k1", "v1", dataStore);
        DataStoreFixture.putStringInStore("k2", "v2", dataStore);

        // WHEN
        dataStore.clearAll();

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    void size_whenNewStore_shouldReturnZero() {
        // GIVEN - WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isZero();
    }

    @Test
    void size_whenTwoItemsInStore_shouldReturnTwo() {
        // GIVEN
        DataStoreFixture.putStringInStore("k1", "v1", dataStore);
        DataStoreFixture.putStringInStore("k2", "v2", dataStore);

        // WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isEqualTo(2);
    }

    @Test
    void generateKeyPrefixForRepeatedField() {
        // GIVEN-WHEN
        String actualKeyPrefix = DataStore.generateKeyPrefixForRepeatedField("entry_list", 1);

        // THEN
        assertThat(actualKeyPrefix).isEqualTo("entry_list[1].");
    }

    @Test
    void generateKeyPrefixForRepeatedFieldUnderRepeater() {
        // GIVEN-WHEN
        String actualKeyPrefix = DataStore.generateKeyPrefixForRepeatedField("entry_list", 1, "myEntries[5].");

        // THEN
        assertThat(actualKeyPrefix).isEqualTo("myEntries[5].entry_list[1].");
    }

    @Test
    void copy_shouldMakeDataStoreCopy() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);


        // WHEN
        DataStore actualCopy = dataStore.copy();


        // THEN
        assertThat(actualCopy).isNotSameAs(dataStore);

        assertThat(actualCopy.size()).isEqualTo(dataStore.size());

        assertThat(actualCopy.getFileStructure()).isEqualTo(dataStore.getFileStructure());

        assertThat(actualCopy.getStore()).isNotSameAs(dataStore.getStore());

        Entry pickedOneSourceEntry = dataStore.getStore().get("entry_list[0].my_field");
        Entry pickedOneActualEntry = actualCopy.getStore().get("entry_list[0].my_field");
        assertThat(pickedOneActualEntry)
                .isEqualTo(pickedOneSourceEntry)
                .isNotSameAs(pickedOneSourceEntry);
    }

    @Test
    void copyFields_toSameStore_withoutRepeater_shouldNotChangeValue() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);
        Set<String> fieldNames = new HashSet<>(singletonList("my_field"));

        // WHEN
        subStore.copyFields(fieldNames, null, null, null);

        // THEN
        Entry pickedActualEntry = subStore.getStore().get("my_field");
        Entry pickedOriginalEntry = dataStore.getStore().get("entry_list[0].my_field");
        assertThat(pickedActualEntry)
                .isEqualTo(pickedOriginalEntry)
                .isNotSameAs(pickedOriginalEntry);
    }

    @Test
    void copyFields_toSameStore_withRepeater_shouldAddValue() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);
        Set<String> fieldNames = new HashSet<>(singletonList("my_field"));

        // WHEN
        subStore.copyFields(fieldNames, null, "entry_list", 3L);

        // THEN
        Entry pickedActualEntry = subStore.getStore().get("entry_list[3].my_field");
        Entry pickedOriginalEntry = dataStore.getStore().get("entry_list[0].my_field");
        assertThat(pickedActualEntry)
                .isEqualTo(pickedOriginalEntry)
                .isNotSameAs(pickedOriginalEntry);
    }

    @Test
    void copyFields_toAnotherStore_withoutRepeater_shouldAddValue() throws IOException {
        // GIVEN
        DataStore targetStore = createEmptyStore();
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);
        Set<String> fieldNames = new HashSet<>(singletonList("my_field"));

        // WHEN
        subStore.copyFields(fieldNames, targetStore, null, null);

        // THEN
        Entry pickedActualEntry = targetStore.getStore().get("my_field");
        Entry pickedOriginalEntry = subStore.getStore().get("my_field");
        assertThat(pickedActualEntry)
                .isEqualTo(pickedOriginalEntry)
                .isNotSameAs(pickedOriginalEntry);
    }

    @Test
    void copyFields_toAnotherStore_withRepeater_shouldAddValue() throws IOException {
        // GIVEN
        DataStore targetStore = createEmptyStore();
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);
        Set<String> fieldNames = new HashSet<>(singletonList("my_field"));

        // WHEN
        subStore.copyFields(fieldNames, targetStore, "entry_list", 0L);

        // THEN
        Entry pickedActualEntry = targetStore.getStore().get("entry_list[0].my_field");
        Entry pickedOriginalEntry = subStore.getStore().get("my_field");
        assertThat(pickedActualEntry)
                .isEqualTo(pickedOriginalEntry)
                .isNotSameAs(pickedOriginalEntry);
    }
}
