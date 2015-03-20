package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.UNKNOWN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DataStore_focusOnGettingValuesTest {

    private DataStore dataStore;

    @Before
    public void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure());
    }

    @Test
    public void getRawValue_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getRawValue("f1").isPresent()).isEqualTo(false);
    }

    @Test
    public void getRawValue_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        byte[] expectedBytes = { 0, 1, 2, 3};
        DataStoreFixture.putRawValueInStore("f1", expectedBytes, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f1").get()).isEqualTo(expectedBytes);
    }

    @Test
    public void getRawValue_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        byte[] bytes = { 0 };
        DataStoreFixture.putRawValueInStore("f1", bytes, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f2").isPresent()).isEqualTo(false);
    }

    @Test
    public void getText_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getText("f1").isPresent()).isFalse();
    }

    @Test
    public void getText_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putStringInStore("f1", "v1", dataStore);

        // WHEN-THEN
        assertThat(dataStore.getText("f1").get()).isEqualTo("v1");
    }

    @Test
    public void getText_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        DataStoreFixture.putStringInStore("f1", "v1", dataStore);

        // WHEN-THEN
        assertThat(dataStore.getText("f2").isPresent()).isFalse();
    }

    @Test
    public void getInteger_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getInteger("f1").isPresent()).isFalse();
    }

    @Test
    public void getInteger_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putLongInStore("f1", 100L, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f1").get()).isEqualTo(100L);
    }

    @Test
    public void getInteger_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        DataStoreFixture.putLongInStore("f1", 100L, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f2").isPresent()).isFalse();
    }

    @Test
    public void getFloatingPoint_whenNoItem_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1").isPresent()).isFalse();
    }

    @Test
    public void getFloatingPoint_whenOne32BitItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putFloatInStore("f1", 691.44006f, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1").get()).isEqualTo(691.44006f);
    }

    @Test
    public void getFloatingPoint_whenOne16BitItem_andSuccess_shouldReturnValue() {
        // GIVEN
        DataStoreFixture.putHalfFloatInStore("f1", 691.5f, dataStore);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1").get()).isEqualTo(691.5f);
    }

    @Test
    public void getIntegerListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<Long> values = dataStore.getIntegerListOf("my_field");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values).containsAll(asList(10L, 20L, 30L));
    }

    @Test
    public void getFloatingPointListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<Float> values = dataStore.getFloatingPointListOf("my_fp_field");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values).containsAll(asList(235.666667f, 335.666667f, 435.666667f));
    }

    @Test
    public void getRepeatedValues_whenProvidedStore_shouldReturnCorrespondingValues() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        List<DataStore> actualValues = dataStore.getRepeatedValues("entry_list");

        // THEN
        assertThat(actualValues).isNotNull();
        assertThat(actualValues).hasSize(3);
        assertThat(actualValues.get(0).getStore()).hasSize(4);
        assertThat(actualValues.get(1).getStore()).hasSize(4);
        assertThat(actualValues.get(2).getStore()).hasSize(4);

        Map<String, DataStore.Entry> subStore = actualValues.get(0).getStore();
        assertThat(subStore.get("my_field")).isEqualTo(new DataStore.Entry("entry_list[0].my_field", FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(10L)));
        assertThat(subStore.get("my_fp_field")).isEqualTo(new DataStore.Entry("entry_list[0].my_fp_field", FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(235.666667f)));
        assertThat(subStore.get("a_field")).isEqualTo(new DataStore.Entry("entry_list[0].a_field", FileStructureDto.Type.TEXT, TypeHelper.textToRaw("az")));
        assertThat(subStore.get("another_field")).isEqualTo(new DataStore.Entry("entry_list[0].another_field", UNKNOWN, new byte[] {0x1, 0x2, 0x3, 0x4}));
    }


}
