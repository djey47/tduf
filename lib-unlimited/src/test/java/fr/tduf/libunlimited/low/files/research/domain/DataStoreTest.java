package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.UNKNOWN;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DataStoreTest {

    private DataStore dataStore;

    @Before
    public void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure());
    }

    @Test
    public void clearAll_shouldRemoveAllEntries() throws Exception {
        // GIVEN
        DataStoreFixture.putStringInStore("k1", "v1", dataStore);
        DataStoreFixture.putStringInStore("k2", "v2", dataStore);

        // WHEN
        dataStore.clearAll();

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    public void size_whenNewStore_shouldReturnZero() {
        // GIVEN - WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isZero();
    }

    @Test
    public void size_whenTwoItemsInStore_shouldReturnTwo() {
        // GIVEN
        DataStoreFixture.putStringInStore("k1", "v1", dataStore);
        DataStoreFixture.putStringInStore("k2", "v2", dataStore);

        // WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void generateKeyPrefixForRepeatedField() {
        // GIVEN-WHEN
        String actualKeyPrefix = DataStore.generateKeyPrefixForRepeatedField("entry_list", 1);

        // THEN
        assertThat(actualKeyPrefix).isEqualTo("entry_list[1].");
    }

    @Test
    public void toJsonString_whenProvidedStore_shouldReturnJsonRepresentation() throws IOException, URISyntaxException, JSONException {
        // GIVEN
        String expectedJson = getStoreContentsAsJson();
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        String actualJson = dataStore.toJsonString();
        System.out.println(actualJson);

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    public void fromJsonString_whenProvidedJson_shouldSetStore() throws IOException, URISyntaxException {
        // GIVEN
        String jsonInput = getStoreContentsAsJson();

        // WHEN
        dataStore.fromJsonString(jsonInput);

        // THEN
        assertThat(dataStore.getStore()).hasSize(12);
        assertThat(dataStore.getIntegerListOf("my_field")).containsAll(asList(10L, 20L, 30L));
        assertThat(dataStore.getFloatingPointListOf("my_fp_field")).containsAll(asList(235.666667f, 335.666667f, 435.666667f));
        assertThat(dataStore.getText("entry_list[0].a_field").get()).isEqualTo("az");
        assertThat(dataStore.getText("entry_list[1].a_field").get()).isEqualTo("bz");
        assertThat(dataStore.getText("entry_list[2].a_field").get()).isEqualTo("cz");
        assertThat(dataStore.getRawValue("entry_list[0].another_field").get()).isEqualTo(new byte[]{0x1, 0x2, 0x3, 0x4});
        assertThat(dataStore.getRawValue("entry_list[1].another_field").get()).isEqualTo(new byte[]{0x5, 0x6, 0x7, 0x8});
        assertThat(dataStore.getRawValue("entry_list[2].another_field").get()).isEqualTo(new byte[]{0x9, 0xA, 0xB, 0xC});
    }

    @Test
    public void copy_shouldMakeDataStoreCopy() {
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
    public void mergeAll_whenNullSourceStore_shouldDoNothing() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);

        // WHEN
        dataStore.mergeAll(null);

        // THEN
        assertThat(dataStore.size()).isEqualTo(12);
    }

    @Test
    public void mergeAll_shouldAddNewEntries() throws IOException {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore sourceStore = createDataStoreToMerge();

        // WHEN
        dataStore.mergeAll(sourceStore);

        // THEN
        assertThat(dataStore.size()).isEqualTo(16);
    }

    @Test (expected = IllegalArgumentException.class)
    public void mergeAll_whenDifferentStructures_shouldThrowIllegalArgumentException() throws IOException {
        // GIVEN
        DataStore sourceStore = new DataStore(FileStructureDto.builder().build());

        // WHEN
        dataStore.mergeAll(sourceStore);

        // THEN: IAE
    }

    private static String getStoreContentsAsJson() throws URISyntaxException, IOException {
        return FilesHelper.readTextFromResourceFile("/files/json/store.json");
    }

    private static DataStore createDataStoreToMerge() throws IOException {
        DataStore sourceStore = new DataStore(DataStoreFixture.getFileStructure());
        // Already existing entries
        sourceStore.addInteger("entry_list[2].my_field", 30L);
        sourceStore.addFloatingPoint("entry_list[2].my_fp_field", 435.666667f);
        sourceStore.addText("entry_list[2].a_field", "cz");
        sourceStore.addValue("entry_list[2].another_field", UNKNOWN, new byte [] {0x9, 0xA, 0xB, 0xC});
        // New entries
        sourceStore.addInteger("entry_list[3].my_field", 30L);
        sourceStore.addFloatingPoint("entry_list[3].my_fp_field", 435.666667f);
        sourceStore.addText("entry_list[3].a_field", "cz");
        sourceStore.addValue("entry_list[3].another_field", UNKNOWN, new byte[]{0x9, 0xA, 0xB, 0xC});
        return sourceStore;
    }
}