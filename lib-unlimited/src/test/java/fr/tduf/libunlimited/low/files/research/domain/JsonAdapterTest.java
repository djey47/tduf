package fr.tduf.libunlimited.low.files.research.domain;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import fr.tduf.libunlimited.low.files.research.rw.JsonAdapter;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture.createEmptyStore;
import static fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture.getStoreContentsAsJson;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonAdapterTest {
    private static final String THIS_CLASS_NAME = JsonAdapterTest.class.getSimpleName();

    private JsonAdapter jsonAdapter;

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
    }

    @Test
    void toJsonString_whenProvidedStore_shouldReturnJsonRepresentation() throws IOException, JSONException {
        // GIVEN
        dataStore = createEmptyStore();
        String expectedJson = getStoreContentsAsJson("/files/json/store.json");
        DataStoreFixture.createStoreEntries(dataStore);
        jsonAdapter = new JsonAdapter(dataStore);

        // WHEN
        String actualJson = jsonAdapter.toJsonString();
        Log.debug(THIS_CLASS_NAME, actualJson);

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
        jsonAdapter = new JsonAdapter(specialDataStore);

        // WHEN
        String actualJson = jsonAdapter.toJsonString();

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
        jsonAdapter = new JsonAdapter(specialDataStore);

        // WHEN
        String actualJson = jsonAdapter.toJsonString();

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
        jsonAdapter = new JsonAdapter(specialDataStore);

        // WHEN
        String actualJson = jsonAdapter.toJsonString();

        // THEN
        assertThat(actualJson).isNotNull();
        assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    @Test
    void fromJsonString_whenProvidedJson_shouldSetStore() throws IOException {
        // GIVEN
        dataStore = createEmptyStore();
        jsonAdapter = new JsonAdapter(dataStore);
        String jsonInput = getStoreContentsAsJson("/files/json/store.json");

        // WHEN
        jsonAdapter.fromJsonString(jsonInput);

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
        jsonAdapter = new JsonAdapter(dataStore);
        String jsonInput = getStoreContentsAsJson("/files/json/store_longInteger.json");

        // WHEN
        jsonAdapter.fromJsonString(jsonInput);

        // THEN
        assertThat(dataStore.getStore()).hasSize(2);
        assertThat(dataStore.getInteger("my_int_field")).contains(10L);
        assertThat(dataStore.getInteger("my_long_field")).contains(4286700000L);
    }

    @Test
    void fromJsonString_whenProvidedJson_andLinks_shouldSetStore() throws IOException {
        // GIVEN
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-links-map.json"));
        jsonAdapter = new JsonAdapter(dataStore);
        String jsonInput = getStoreContentsAsJson("/files/json/store_links.json");

        // WHEN
        jsonAdapter.fromJsonString(jsonInput);

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
}
