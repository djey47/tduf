package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DataStore_focusOnDeletingValuesTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() throws IOException {
        dataStore = new DataStore(DataStoreFixture.getFileStructure("/files/structures/TEST-datastore-map.json"));
    }

    @Test
    void deleteRepeatedIntegerValue_whenValueExists_shouldRemoveIt() {
        // given
        dataStore.addRepeatedIntegerValue("repeater", "field", 0L, 150L);

        // when
        dataStore.deleteRepeatedValue("repeater", "field", 0L);

        // then
        assertThat(dataStore.getRepeatedValues("repeater")).isEmpty();
    }

    @Test
    void deleteRepeatedIntegerValue_whenValueDoesNotExist_shouldDoNothing() {
        // given
        dataStore.addRepeatedIntegerValue("repeater", "field1", 0L, 150L);

        // when
        dataStore.deleteRepeatedValue("repeater", "field2", 0L);

        // then
        assertThat(dataStore.getRepeatedValues("repeater")).hasSize(1);
    }
}
