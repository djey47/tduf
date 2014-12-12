package fr.tduf.libunlimited.low.files.research.domain;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


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
    public void addRawValue_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addRawValue("f1", expectedRawValue);

        // THEN
        //FIXME https://github.com/joel-costigliola/assertj-core/issues/293
        assertThat(dataStore.getStore().get("f1")).isEqualTo(expectedRawValue);
    }

    @Test
    public void addText_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addText("f1", "v1");

        // THEN
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", "v1".getBytes()));
    }

    @Test
    public void addRepeatedRawValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addRepeatedRawValue("repeater", "f1", 0, expectedRawValue);
        //FIXME https://github.com/joel-costigliola/assertj-core/issues/293
        assertThat(dataStore.getStore().get("repeater[0].f1")).isEqualTo(expectedRawValue);

    }

    @Test
    public void addRepeatedTextValue_shouldCreateNewEntryInStore() {
        // GIVEN - WHEN
        dataStore.addRepeatedTextValue("repeater", "f1", 0, "v1");

        // THEN
        //FIXME https://github.com/joel-costigliola/assertj-core/issues/293
        assertThat(dataStore.getStore().get("repeater[0].f1")).isEqualTo("v1".getBytes());
    }

    @Test
    public void addRepeatedNumericValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte)0xFF, (byte)0xFF};

        // WHEN
        dataStore.addRepeatedNumericValue("repeater", "f1", 0, 0xFFFFL);

        // THEN
        //FIXME https://github.com/joel-costigliola/assertj-core/issues/293
        assertThat(dataStore.getStore().get("repeater[0].f1")).isEqualTo(expectedBytes);
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
    public void getRawValue_whenNoItem_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getRawValue("f1")).isNull();
    }

    @Test
    public void getRawValue_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        byte[] expectedBytes = { 0, 1, 2, 3};
        dataStore.getStore().put("f1", expectedBytes);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f1")).isEqualTo(expectedBytes);
    }

    @Test
    public void getRawValue_whenOneItem_andNoSuccess_shouldReturnNull() {
        // GIVEN
        byte[] bytes = { 0 };
        dataStore.getStore().put("f1", bytes);

        // WHEN-THEN
        assertThat(dataStore.getRawValue("f2")).isNull();
    }

    @Test
    public void getText_whenNoItem_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getText("f1")).isNull();
    }

    @Test
    public void getText_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        putStringInStore("f1", "v1");

        // WHEN-THEN
        assertThat(dataStore.getText("f1")).isEqualTo("v1");
    }

    @Test
    public void getText_whenOneItem_andNoSuccess_shouldReturnNull() {
        // GIVEN
        putStringInStore("f1", "v1");

        // WHEN-THEN
        assertThat(dataStore.getText("f2")).isNull();
    }

    @Test
    public void getNumeric_whenNoItem_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.getNumeric("f1")).isNull();
    }

    @Test
    public void getNumeric_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        putLongInStore("f1", 100L);

        // WHEN-THEN
        assertThat(dataStore.getNumeric("f1")).isEqualTo(100L);
    }

    @Test
    public void getNumeric_whenOneItem_andNoSuccess_shouldReturnNull() {
        // GIVEN
        putLongInStore("f1", 100L);

        // WHEN-THEN
        assertThat(dataStore.getText("f2")).isNull();
    }

    @Test
    public void getNumericListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        createStoreEntries();

        // WHEN
        List<Long> values = dataStore.getNumericListOf("my_field");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values).containsAll(asList(10L, 20L, 30L));
    }

    @Test
    public void getRepeatedValuesOf_whenProvidedStore_shouldReturnCorrespondingValues() {
        // GIVEN
        createStoreEntries();

        // WHEN
        List<Map<String, byte[]>> values = dataStore.getRepeatedValuesOf("entry_list");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get(0)).hasSize(2);
        assertThat(values.get(1)).hasSize(2);
        assertThat(values.get(2)).hasSize(2);
    }

    @Test
    public void generateKeyPrefixForRepeatedField() {
        // GIVEN-WHEN
        String actualKeyPrefix = DataStore.generateKeyPrefixForRepeatedField("entry_list", 1);

        // THEN
        assertThat(actualKeyPrefix).isEqualTo("entry_list[1].");
    }

    private void putLongInStore(String key, long value) {
        byte[] bytes = ByteBuffer
                .allocate(8)
                .putLong(value)
                .array();
        dataStore.getStore().put(key, bytes);
    }

    private void putStringInStore(String key, String value) {
        dataStore.getStore().put(key, value.getBytes());
    }

    private void createStoreEntries() {
        dataStore.getStore().clear();

        putLongInStore("entry_list[0].my_field", 10L);
        putStringInStore("entry_list[0].a_field", "az");
        putLongInStore("entry_list[1].my_field", 20L);
        putStringInStore("entry_list[1].a_field", "bz");
        putLongInStore("entry_list[2].my_field", 30L);
        putStringInStore("entry_list[2].a_field", "cz");
   }
}