package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DataStore_focusOnAddingValuesTest {

    private DataStore dataStore;

    @Before
    public void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastore-map.json"));
    }

    @Test
    public void addValue_whenUnknowType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addValue("f1", UNKNOWN, expectedRawValue);

        // THEN
        Entry actualEntry = dataStore.getStore().get("f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedRawValue);
        assertThat(actualEntry.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    public void addValue_whenTextType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", TEXT, "v1".getBytes());

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenIntegerType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", INTEGER, TypeHelper.integerToRaw(500L));

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenFloatingPointType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenNonStoredType_shouldNotCreateEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", GAP, new byte[]{0xA});

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    public void addText_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addText("f1", "v1");

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addInteger_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addInteger("f1", 500L);

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addFloatingPoint("f1", 83.666667f);

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addHalfFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addHalfFloatingPoint("f1", 83.67f);

        // THEN
        Entry expectedEntry = new Entry(FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint16ToRaw(83.67f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addRepeatedRawValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addRepeatedRawValue("repeater", "f1", 0, expectedRawValue);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedRawValue);
        assertThat(actualEntry.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    public void addRepeatedTextValue_shouldCreateNewEntryInStore() {
        // GIVEN - WHEN
        dataStore.addRepeatedTextValue("repeater", "f1", 0, "v1");

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly("v1".getBytes());
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.TEXT);
    }

    @Test
    public void addRepeatedIntegerValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte)0xFF, (byte)0xFF};

        // WHEN
        dataStore.addRepeatedIntegerValue("repeater", "f1", 0, 0xFFFFL);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.INTEGER);
    }

    @Test
    public void addRepeatedFloatingPointValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x44, 0x21, (byte)0x9D, (byte)0xD6 };

        // WHEN
        dataStore.addRepeatedFloatingPointValue("repeater", "f1", 0, 646.46619f);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.FPOINT);
    }

    @Test(expected = NullPointerException.class)
    public void mergeRepeatedValues_whenNullSubStore_shouldThrowException() {
        // GIVEN-WHEN
        dataStore.mergeRepeatedValues("", 0, null);

        // THEN: NPE
    }

    @Test
    public void mergeRepeatedValues_shouldAddEntriesAtRightIndex() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);

        // WHEN
        dataStore.mergeRepeatedValues("entry_list", 3, subStore);

        // THEN
        assertThat(dataStore.getStore()).hasSize(16);

        DataStore actualSubDataStore = dataStore.getRepeatedValues("entry_list").get(3);
        assertThat(actualSubDataStore.size()).isEqualTo(4);
        assertThat(actualSubDataStore.getInteger("my_field").get()).isEqualTo(10L);
        assertThat(actualSubDataStore.getFloatingPoint("my_fp_field").get()).isEqualTo(235.666667f);
        assertThat(actualSubDataStore.getText("a_field").get()).isEqualTo("az");
        assertThat(actualSubDataStore.getRawValue("another_field").get()).isEqualTo(new byte[]{0x1, 0x2, 0x3, 0x4});
    }

    @Test
    public void replaceRepeatedValues_shouldChangeAllKeysAtRightIndex() {
        // GIVEN
        DataStoreFixture.createStoreEntries(dataStore);
        DataStore subStore = dataStore.getRepeatedValues("entry_list").get(0);

        // WHEN
        dataStore.replaceRepeatedValues("entry_list", 2, subStore);

        // THEN
        assertThat(dataStore.getRepeatedValues("entry_list").get(2).getStore()).isEqualTo(subStore.getStore());
    }
}
