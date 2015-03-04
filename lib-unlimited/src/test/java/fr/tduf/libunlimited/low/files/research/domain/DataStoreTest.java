package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.*;
import static java.util.Arrays.asList;
import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

//FIXME https://github.com/joel-costigliola/assertj-core/issues/293
public class DataStoreTest {

    private final DataStore dataStore = new DataStore();

    @Test
    public void clearAll_shouldRemoveAllEntries() throws Exception {
        // GIVEN
        putStringInStore("k1", "v1");
        putStringInStore("k2", "v2");

        // WHEN
        dataStore.clearAll();

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    public void addValue_shouldSetEntryRankSequentially() throws Exception {
        // GIVEN
        byte[] rawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addValue("f1", UNKNOWN, rawValue);
        dataStore.addValue("f2", UNKNOWN, rawValue);
        dataStore.addValue("f3", UNKNOWN, rawValue);

        // THEN
        assertThat(dataStore.getStore().get("f1").getRank()).isEqualTo(0);
        assertThat(dataStore.getStore().get("f2").getRank()).isEqualTo(1);
        assertThat(dataStore.getStore().get("f3").getRank()).isEqualTo(2);
    }

    @Test
    public void addValue_whenUnknowType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addValue("f1", UNKNOWN, expectedRawValue);

        // THEN
        DataStore.Entry actualEntry = dataStore.getStore().get("f1");
        assertThat(actualEntry.getRawValue()).isEqualTo(expectedRawValue);
        assertThat(actualEntry.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    public void addValue_whenTextType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", TEXT, "v1".getBytes());

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenIntegerType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", INTEGER, TypeHelper.integerToRaw(500L));

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenFloatingPointType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addValue_whenNonStoredType_shouldNotCreateEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", GAP, new byte[] {0xA});

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    public void addText_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addText("f1", "v1");

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addInteger_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addInteger("f1", 500L);

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addFloatingPoint("f1", 83.666667f);

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addHalfFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addHalfFloatingPoint("f1", 83.67f);

        // THEN
        DataStore.Entry expectedEntry = new DataStore.Entry("f1", FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint16ToRaw(83.67f));
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    public void addRepeatedRawValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addRepeatedRawValue("repeater", "f1", 0, expectedRawValue);

        // THEN
        DataStore.Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).isEqualTo(expectedRawValue);
        assertThat(actualEntry.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    public void addRepeatedTextValue_shouldCreateNewEntryInStore() {
        // GIVEN - WHEN
        dataStore.addRepeatedTextValue("repeater", "f1", 0, "v1");

        // THEN
        DataStore.Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).isEqualTo("v1".getBytes());
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.TEXT);
    }

    @Test
    public void addRepeatedIntegerValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte)0xFF, (byte)0xFF};

        // WHEN
        dataStore.addRepeatedIntegerValue("repeater", "f1", 0, 0xFFFFL);

        // THEN
        DataStore.Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).isEqualTo(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.INTEGER);
    }

    @Test
    public void addRepeatedFloatingPointValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x44, 0x21, (byte)0x9D, (byte)0xD6 };

        // WHEN
        dataStore.addRepeatedFloatingPointValue("repeater", "f1", 0, 646.46619f);

        // THEN
        DataStore.Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).isEqualTo(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(FileStructureDto.Type.FPOINT);
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
        putStringInStore("k1", "v1");
        putStringInStore("k2", "v2");

        // WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isEqualTo(2);
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
        putRawValueInStore("f1", expectedBytes);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f1").get()).isEqualTo(expectedBytes);
    }

    @Test
    public void getRawValue_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        byte[] bytes = { 0 };
        putRawValueInStore("f1", bytes);

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
        putStringInStore("f1", "v1");

        // WHEN-THEN
        assertThat(dataStore.getText("f1").get()).isEqualTo("v1");
    }

    @Test
    public void getText_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        putStringInStore("f1", "v1");

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
        putLongInStore("f1", 100L);

        // WHEN-THEN
        assertThat(dataStore.getInteger("f1").get()).isEqualTo(100L);
    }

    @Test
    public void getInteger_whenOneItem_andNoSuccess_shouldReturnAbsent() {
        // GIVEN
        putLongInStore("f1", 100L);

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
        putFloatInStore("f1", 691.44006f);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1").get()).isEqualTo(691.44006f);
    }

    @Test
    public void getFloatingPoint_whenOne16BitItem_andSuccess_shouldReturnValue() {
        // GIVEN
        putHalfFloatInStore("f1", 691.5f);

        // WHEN-THEN
        assertThat(dataStore.getFloatingPoint("f1").get()).isEqualTo(691.5f);
    }

    @Test
    public void getIntegerListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        createStoreEntries();

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
        createStoreEntries();

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
        createStoreEntries();

        // WHEN
        List<DataStore> actualValues = dataStore.getRepeatedValues("entry_list");

        // THEN
        assertThat(actualValues).isNotNull();
        assertThat(actualValues).hasSize(3);
        assertThat(actualValues.get(0).getStore()).hasSize(4);
        assertThat(actualValues.get(1).getStore()).hasSize(4);
        assertThat(actualValues.get(2).getStore()).hasSize(4);

        Map<String, DataStore.Entry> subStore = actualValues.get(0).getStore();
        assertThat(subStore.get("my_field")).isEqualTo(new DataStore.Entry("entry_list[0].my_field", FileStructureDto.Type.INTEGER, TypeHelper.integerToRaw(10L), 0));
        assertThat(subStore.get("my_fp_field")).isEqualTo(new DataStore.Entry("entry_list[0].my_fp_field", FileStructureDto.Type.FPOINT, TypeHelper.floatingPoint32ToRaw(235.666667f), 1));
        assertThat(subStore.get("a_field")).isEqualTo(new DataStore.Entry("entry_list[0].a_field", FileStructureDto.Type.TEXT, TypeHelper.textToRaw("az"), 2));
        assertThat(subStore.get("another_field")).isEqualTo(new DataStore.Entry("entry_list[0].another_field", UNKNOWN, new byte[] {0x1, 0x2, 0x3, 0x4}, 3));
    }

    @Test
    public void generateKeyPrefixForRepeatedField() {
        // GIVEN-WHEN
        String actualKeyPrefix = DataStore.generateKeyPrefixForRepeatedField("entry_list", 1);

        // THEN
        assertThat(actualKeyPrefix).isEqualTo("entry_list[1].");
    }

    @Test
    public void toJsonString_whenProvidedStore_shouldReturnJsonRepresentation() throws IOException, URISyntaxException {
        // GIVEN
        String expectedJson = getStoreContentsAsJson();
        createStoreEntries();

        // WHEN
        String actualJson = dataStore.toJsonString();
        System.out.println(actualJson);

        // THEN
        assertThat(actualJson).isNotNull();
        assertJsonEquals(expectedJson, actualJson);
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
    public void isBase64Encoded_whenNullString_shouldReturnFalse() {
        // GIVEN-WHEN-THEN
        assertThat(DataStore.isBase64Encoded(null)).isFalse();
    }

    @Test
    public void isBase64Encoded_whenBase64EncodedString_shouldReturnTrue() {
        // GIVEN
        String value = java.util.Base64.getEncoder().encodeToString(new byte[]{0xA, 0xB, 0xC});

        // WHEN
        boolean base64Encoded = DataStore.isBase64Encoded(value);

        // THEN
        assertThat(base64Encoded).isTrue();
    }

    @Test
    public void isBase64Encoded_whenRegularString_shouldReturnFalse() {
        // GIVEN
        String value = "ABC";

        // WHEN
        boolean base64Encoded = DataStore.isBase64Encoded(value);

        // THEN
        assertThat(base64Encoded).isFalse();
    }

    private void putRawValueInStore(String key, byte[] bytes) {
        dataStore.addValue(key, UNKNOWN, bytes);
    }

    private void putLongInStore(String key, long value) {
        byte[] bytes = ByteBuffer
                .allocate(8)
                .putLong(value)
                .array();
        dataStore.addValue(key, INTEGER, bytes);
    }

    private void putFloatInStore(String key, float value) {
        byte[] bytes = ByteBuffer
                .allocate(4)
                .putFloat(value)
                .array();
        dataStore.addValue(key, FPOINT, bytes);
    }

    private void putHalfFloatInStore(String key, float value) {
        byte[] bytes = TypeHelper.floatingPoint16ToRaw(value);
        dataStore.addValue(key, FPOINT, bytes);
    }

    private void putStringInStore(String key, String value) {
        dataStore.addValue(key, TEXT, value.getBytes());
    }

    private void createStoreEntries() {
        dataStore.getStore().clear();

        putLongInStore("entry_list[0].my_field", 10L);
        putFloatInStore("entry_list[0].my_fp_field", 235.666667f);
        putStringInStore("entry_list[0].a_field", "az");
        putRawValueInStore("entry_list[0].another_field", new byte[]{0x1, 0x2, 0x3, 0x4});
        putLongInStore("entry_list[1].my_field", 20L);
        putFloatInStore("entry_list[1].my_fp_field", 335.666667f);
        putStringInStore("entry_list[1].a_field", "bz");
        putRawValueInStore("entry_list[1].another_field", new byte[]{0x5, 0x6, 0x7, 0x8});
        putLongInStore("entry_list[2].my_field", 30L);
        putFloatInStore("entry_list[2].my_fp_field", 435.666667f);
        putStringInStore("entry_list[2].a_field", "cz");
        putRawValueInStore("entry_list[2].another_field", new byte [] {0x9, 0xA, 0xB, 0xC});
    }

    private static String getStoreContentsAsJson() throws URISyntaxException, IOException {
        return FilesHelper.readTextFromResourceFile("/files/json/store.json");
    }
}