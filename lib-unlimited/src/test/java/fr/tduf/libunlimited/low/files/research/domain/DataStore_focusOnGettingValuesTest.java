package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.research.domain.Type.UNKNOWN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class DataStore_focusOnGettingValuesTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastore-map.json"));
    }

    @Test
    void getRawValue_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getRawValue("f1")).isEmpty();
    }

    @Test
    void getRawValue_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        byte[] expectedBytes = { 0, 1, 2, 3};
        DataStoreFixture.putRawValueInStore("f1", expectedBytes, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f1")).contains(expectedBytes);
    }

    @Test
    void getRawValue_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        byte[] bytes = { 0 };
        DataStoreFixture.putRawValueInStore("f1", bytes, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f2")).isEmpty();
    }

    @Test
    void getText_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getText("f1")).isEmpty();
    }

    @Test
    void getText_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putStringInStore("f1", "v1", dataStore);

        // WHEN-THEN
        assertThat(dataStore.getText("f1")).contains("v1");
    }

    @Test
    void getText_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        DataStoreFixture.putStringInStore("f1", "v1", dataStore);

        // WHEN-THEN
        assertThat(dataStore.getText("f2")).isEmpty();
    }

    @Test
    void getInteger_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getInteger("f1")).isEmpty();
    }

    @Test
    void getInteger_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putLongInStore("f1", 100L, false, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f1")).contains(100L);
    }

    @Test
    void getInteger_whenSignedValue_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putLongInStore("f1", -100L, true, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f1")).contains(-100L);
    }

    @Test
    void getInteger_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        DataStoreFixture.putLongInStore("f1", 100L, false, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f2")).isEmpty();
    }

    @Test
    void getFloatingPoint_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1")).isEmpty();
    }

    @Test
    void getFloatingPoint_whenOne32BitItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putFloatInStore("f1", 691.44006f, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1")).contains(691.44006f);
    }

    @Test
    void getFloatingPoint_whenOne16BitItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putHalfFloatInStore("f1", 691.5f, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1")).contains(691.5f);
    }

    @Test
    void getIntegerListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<Long> values = dataStore.getIntegerListOf("my_field");

        // THEN
        assertThat(values)
                .isNotNull()
                .hasSize(3)
                .containsAll(asList(10L, 20L, 30L));
    }

    @Test
    void getFloatingPointListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<Float> values = dataStore.getFloatingPointListOf("my_fp_field");

        // THEN
        assertThat(values)
                .isNotNull()
                .hasSize(3)
                .containsAll(asList(235.666667f, 335.666667f, 435.666667f));
    }

    @Test
    void getRepeatedValues_whenProvidedStore_shouldReturnCorrespondingValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<DataStore> actualValues = dataStore.getRepeatedValues("entry_list");

        // THEN
        assertThat(actualValues)
                .isNotNull()
                .hasSize(3);
        final DataStore subStore0 = actualValues.get(0);
        assertThat(subStore0.getStore()).hasSize(4);
        final DataStore subStore1 = actualValues.get(1);
        assertThat(subStore1.getStore()).hasSize(4);
        final DataStore subStore2 = actualValues.get(2);
        assertThat(subStore2.getStore()).hasSize(4);

        Map<String, Entry> subStore = subStore0.getStore();
        assertThat(subStore.get("my_field")).isEqualTo(new Entry(Type.INTEGER, TypeHelper.integerToRaw(10L)));
        assertThat(subStore.get("my_fp_field")).isEqualTo(new Entry(Type.FPOINT, TypeHelper.floatingPoint32ToRaw(235.666667f)));
        assertThat(subStore.get("a_field")).isEqualTo(new Entry(Type.TEXT, TypeHelper.textToRaw("az", 2)));
        assertThat(subStore.get("another_field")).isEqualTo(new Entry(UNKNOWN, new byte[] {0x1, 0x2, 0x3, 0x4}));
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
}