package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BOTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.TUTORIALS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BulkDatabaseMinerTest {

    private static List<DbDto> topicObjectsFromResources;

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        topicObjectsFromResources = createTopicObjectsFromResources();
    }

    @Test
    void load_whenNullDatabaseObjects_shouldThrowNullPointerException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class, () -> BulkDatabaseMiner.load(null));
    }

    @Test
    void load_whenProvidedDatabaseObjects_shouldAssignObjectsAndReturnInstance() {
        // GIVEN
        ArrayList<DbDto> topicObjects = new ArrayList<>();

        // WHEN
        BulkDatabaseMiner bulkDatabaseMiner = BulkDatabaseMiner.load(topicObjects);

        // THEN
        assertThat(bulkDatabaseMiner).isNotNull();
        assertThat(bulkDatabaseMiner.getTopicObjects()).isEqualTo(topicObjects);
    }

    @Test
    void load_whenProvidedDatabaseObjects_shouldGenerateUniqueMinerIdentifiers() {
        // GIVEN
        ArrayList<DbDto> topicObjects = new ArrayList<>();

        // WHEN
        BulkDatabaseMiner bulkDatabaseMiner1 = BulkDatabaseMiner.load(topicObjects);
        BulkDatabaseMiner bulkDatabaseMiner2 = BulkDatabaseMiner.load(topicObjects);

        // THEN
        assertThat(bulkDatabaseMiner1.getId()).isNotEqualTo(bulkDatabaseMiner2.getId());
    }

    @Test
    void getDatabaseTopic_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<DbDto> actualResult = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopic(TUTORIALS);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    void getDatabaseTopic_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopic(BOTS).get();

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test
    void getDatabaseTopicFromReference_whenNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopicFromReference("000"));
    }

    @Test
    void getDatabaseTopicFromReference_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopicFromReference("111");

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    static List<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        return asList(
                FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Achievements_FAKE.json"),
                FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"),
                FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_PNJ_FAKE.json"));
    }
}
