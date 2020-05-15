package fr.tduf.libunlimited.low.files.research.domain;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class DataStoreTest {

    private static final Class<DataStoreTest> thisClass = DataStoreTest.class;

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
    void toJsonString_whenProvidedStore_shouldReturnJsonRepresentation() throws IOException, JSONException {
        // GIVEN
        String expectedJson = getStoreContentsAsJson("/files/json/store.json");
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        String actualJson = dataStore.toJsonString();
        Log.debug(thisClass.getSimpleName(), actualJson);

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    void toJsonString_whenProvidedStore_andSizeAsReference_shouldReturnJsonRepresentation() throws IOException, JSONException {
        // GIVEN
        DataStore specialDataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastoreAndFormula-map.json"));
        String expectedJson = getStoreContentsAsJson("/files/json/store_formula.json");
        DataStoreFixture.createStoreEntriesForSizeFormula(specialDataStore);

        // WHEN
        String actualJson = specialDataStore.toJsonString();

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    void toJsonString_whenProvidedStore_andLevel2Repeater_shouldReturnJsonRepresentation() throws IOException, JSONException {
        // GIVEN
        DataStore specialDataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastoreAndLevel2Repeater-map.json"));
        String expectedJson = getStoreContentsAsJson("/files/json/store_lvl2Repeater.json");
        DataStoreFixture.createStoreEntriesForLevel2Repeater(specialDataStore);

        // WHEN
        String actualJson = specialDataStore.toJsonString();

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    void toJsonString_whenProvidedStore_andLinkSources_shouldReturnJsonRepresentation() throws IOException, JSONException {
        // GIVEN
        DataStore specialDataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-links-map.json"));
        String expectedJson = getStoreContentsAsJson("/files/json/store_links.json");
        DataStoreFixture.createStoreEntriesForLinkSources(specialDataStore);

        // WHEN
        String actualJson = specialDataStore.toJsonString();

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    void fromJsonString_whenProvidedJson_shouldSetStore() throws IOException {
        // GIVEN
        String jsonInput = getStoreContentsAsJson("/files/json/store.json");

        // WHEN
        dataStore.fromJsonString(jsonInput);

        // THEN
        assertThat(dataStore.getStore()).hasSize(12);
        assertThat(dataStore.getIntegerListOf("my_field")).containsAll(asList(10L, 20L, 30L));
        assertThat(dataStore.getFloatingPointListOf("my_fp_field")).containsAll(asList(235.666667f, 335.666667f, 435.666667f));
        assertThat(dataStore.getText("entry_list[0].a_field")).contains("az");
        assertThat(dataStore.getText("entry_list[1].a_field")).contains("bz");
        assertThat(dataStore.getText("entry_list[2].a_field")).contains("cz");
        assertThat(dataStore.getRawValue("entry_list[0].another_field")).contains(new byte[]{0x1, 0x2, 0x3, 0x4});
        assertThat(dataStore.getRawValue("entry_list[1].another_field")).contains(new byte[]{0x5, 0x6, 0x7, 0x8});
        assertThat(dataStore.getRawValue("entry_list[2].another_field")).contains(new byte[]{0x9, 0xA, 0xB, 0xC});
    }

    @Test
    void fromJsonString_whenProvidedJson_andLongIntegerValues_shouldSetStore() throws IOException {
        // GIVEN
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-unsignedLong-map.json"));
        String jsonInput = getStoreContentsAsJson("/files/json/store_longInteger.json");

        // WHEN
        dataStore.fromJsonString(jsonInput);

        // THEN
        assertThat(dataStore.getStore()).hasSize(2);
        assertThat(dataStore.getInteger("my_int_field")).contains(10L);
        assertThat(dataStore.getInteger("my_long_field")).contains(4286700000L);
    }

    @Test
    void fromJsonString_whenProvidedJson_andLinks_shouldSetStore() throws IOException {
        // GIVEN
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-links-map.json"));
        String jsonInput = getStoreContentsAsJson("/files/json/store_links.json");

        // WHEN
        dataStore.fromJsonString(jsonInput);

        // THEN
        assertThat(dataStore.getStore()).hasSize(4);
        Map<Integer, String> actualSources = dataStore.getLinksContainer().getSources();
        Map<Integer, String> actualTargets = dataStore.getLinksContainer().getTargets();
        assertThat(actualSources).hasSize(2);
        assertThat(actualSources).containsKeys(10, 14);
        assertThat(actualSources.get(10)).isEqualTo("linkSource1");
        assertThat(actualSources.get(14)).isEqualTo("linkSource2");
        assertThat(actualTargets).hasSize(2);
        assertThat(actualTargets).containsKeys(10, 14);
        assertThat(actualTargets.get(10)).isEqualTo("linkedEntries[0].");
        assertThat(actualTargets.get(14)).isEqualTo("linkedEntries[1].");
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

    @Test
    void getRepeatedValues_withLevel2Repeater_shouldExtractValuesCorrectly() {
        // given
        DataStoreFixture.createStoreEntriesForLevel2Repeater(dataStore);

        // when
        List<DataStore> actualStoresLvl1 = dataStore.getRepeatedValues("repeaterLvl1");
        List<DataStore> actualStoresLvl2 = dataStore.getRepeatedValues("repeaterLvl2", "repeaterLvl1[0].");

        // then
        assertThat(actualStoresLvl1).hasSize(2);
        DataStore subStoreAtIndex0 = actualStoresLvl1.get(0);
        assertThat(subStoreAtIndex0.size()).isEqualTo(2);
        assertThat(subStoreAtIndex0.getInteger("repeaterLvl2[0].number")).contains(500L);
        assertThat(subStoreAtIndex0.getInteger("repeaterLvl2[1].number")).contains(501L);
        DataStore subStoreAtIndex1 = actualStoresLvl1.get(1);
        assertThat(subStoreAtIndex1.size()).isEqualTo(2);
        assertThat(subStoreAtIndex1.getInteger("repeaterLvl2[0].number")).contains(502L);
        assertThat(subStoreAtIndex1.getInteger("repeaterLvl2[1].number")).contains(503L);

        assertThat(actualStoresLvl2).hasSize(2);
        DataStore subStoreLvl2AtIndex0 = actualStoresLvl2.get(0);
        assertThat(subStoreLvl2AtIndex0.size()).isEqualTo(1);
        assertThat(subStoreLvl2AtIndex0.getInteger("number")).contains(500L);
        DataStore subStoreLvl2AtIndex1 = actualStoresLvl2.get(1);
        assertThat(subStoreLvl2AtIndex1.size()).isEqualTo(1);
        assertThat(subStoreLvl2AtIndex1.getInteger("number")).contains(501L);
    }

    private static String getStoreContentsAsJson(String resourcePath) throws IOException {
        return FilesHelper.readTextFromResourceFile(resourcePath);
    }

    private static DataStore createEmptyStore() throws IOException {
        return new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastore-map.json"));
    }
}
