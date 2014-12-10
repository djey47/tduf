package fr.tduf.libunlimited.low.files.research.domain;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class DataStoreTest {

    private final DataStore dataStore = new DataStore();

    @Test
    public void clearAll_shouldRemoveAllEntries() throws Exception {
        // GIVEN
        dataStore.getStore().put("k1", "v1");
        dataStore.getStore().put("k2", "v2");

        // WHEN
        dataStore.clearAll();

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    public void add_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.add("f1", "v1");

        // THEN
        assertThat(dataStore.getStore()).contains(MapEntry.entry("f1", "v1"));
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
        dataStore.getStore().put("k1", "v1");
        dataStore.getStore().put("k2", "v2");

        // WHEN
        int size = dataStore.size();

        //THEN
        assertThat(size).isEqualTo(2);
    }

    @Test
    public void get_whenNoItem_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(dataStore.get("f1")).isNull();
    }

    @Test
    public void get_whenOneItem_andSuccess_shouldReturnValue() {
        // GIVEN
        dataStore.getStore().put("f1", "v1");

        // WHEN-THEN
        assertThat(dataStore.get("f1")).isEqualTo("v1");
    }

    @Test
    public void get_whenOneItem_andNoSuccess_shouldReturnNull() {
        // GIVEN
        dataStore.getStore().put("f1", "v1");

        // WHEN-THEN
        assertThat(dataStore.get("f2")).isNull();
    }

    @Test
    public void getNumericListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        DataStore dataStore = createStoreWithEntries();

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
        DataStore dataStore = createStoreWithEntries();

        // WHEN
        List<Map<String, String>> values = dataStore.getRepeatedValuesOf("entry_list");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get(0)).hasSize(2);
        assertThat(values.get(1)).hasSize(2);
        assertThat(values.get(2)).hasSize(2);
    }

    private DataStore createStoreWithEntries() {
        DataStore dataStore = new DataStore();

        dataStore.getStore().put("entry_list[0].my_field", "10");
        dataStore.getStore().put("entry_list[0].a_field", "az");
        dataStore.getStore().put("entry_list[1].my_field", "20");
        dataStore.getStore().put("entry_list[1].a_field", "bz");
        dataStore.getStore().put("entry_list[2].my_field", "30");
        dataStore.getStore().put("entry_list[2].a_field", "cz");

        return dataStore;
    }

}