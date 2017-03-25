package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.research.domain.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

class DataStore_focusOnAddingValuesTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastore-map.json"));
    }

    @Test
    void addValue_whenUnknowType_shouldCreateNewEntryInStore() throws Exception {
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
    void addValue_whenTextType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", TEXT, "v1".getBytes());

        // THEN
        Entry expectedEntry = new Entry(Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addValue_whenIntegerType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", INTEGER, TypeHelper.integerToRaw(500L));

        // THEN
        Entry expectedEntry = new Entry(Type.INTEGER, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addValue_whenFloatingPointType_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));

        // THEN
        Entry expectedEntry = new Entry(Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addValue_whenNonStoredType_shouldNotCreateEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addValue("f1", GAP, new byte[]{0xA});

        // THEN
        assertThat(dataStore.getStore()).isEmpty();
    }

    @Test
    void addText_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addText("f1", "v1");

        // THEN
        Entry expectedEntry = new Entry(Type.TEXT, "v1".getBytes());
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addInteger_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addInteger32("f1", 500L);

        // THEN
        Entry expectedEntry = new Entry(Type.INTEGER, false, 4, TypeHelper.integerToRaw(500L));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addFloatingPoint("f1", 83.666667f);

        // THEN
        Entry expectedEntry = new Entry(Type.FPOINT, TypeHelper.floatingPoint32ToRaw(83.666667f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addHalfFloatingPoint_shouldCreateNewEntryInStore() throws Exception {
        // GIVEN - WHEN
        dataStore.addHalfFloatingPoint("f1", 83.67f);

        // THEN
        Entry expectedEntry = new Entry(Type.FPOINT, false, 2, TypeHelper.floatingPoint16ToRaw(83.67f));
        assertThat(dataStore.getStore()).containsOnly(MapEntry.entry("f1", expectedEntry));
    }

    @Test
    void addRepeatedRawValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedRawValue = { 0x0, 0x1, 0x2, 0x3 };

        // WHEN
        dataStore.addRepeatedValue("repeater", "f1", 0, expectedRawValue);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedRawValue);
        assertThat(actualEntry.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    void addRepeatedTextValue_shouldCreateNewEntryInStore() {
        // GIVEN - WHEN
        dataStore.addRepeatedText("repeater", "f1", 0, "v1");

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly("v1".getBytes());
        assertThat(actualEntry.getType()).isEqualTo(Type.TEXT);
    }

    @Test
    void addRepeatedIntegerValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte)0xFF, (byte)0xFF};

        // WHEN
        dataStore.addRepeatedInteger32("repeater", "f1", 0, 0xFFFFL);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(Type.INTEGER);
    }

    @Test
    void addRepeatedFloatingPointValue_shouldCreateNewEntryInStore() {
        // GIVEN
        byte[] expectedBytes = { 0x44, 0x21, (byte)0x9D, (byte)0xD6 };

        // WHEN
        dataStore.addRepeatedFloatingPoint("repeater", "f1", 0, 646.46619f);

        // THEN
        Entry actualEntry = dataStore.getStore().get("repeater[0].f1");
        assertThat(actualEntry.getRawValue()).containsExactly(expectedBytes);
        assertThat(actualEntry.getType()).isEqualTo(Type.FPOINT);
    }
}
