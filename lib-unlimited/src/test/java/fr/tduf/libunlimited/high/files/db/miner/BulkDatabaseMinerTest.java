package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.assertj.core.api.StrictAssertions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BOTS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BulkDatabaseMinerTest {

    private List<DbDto> topicObjectsFromResources;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        topicObjectsFromResources = createTopicObjectsFromResources();
    }

    @Test(expected = NullPointerException.class)
    public void load_whenNullDatabaseObjects_shouldThrowNullPointerException() {
        // GIVEN-WHEN-THEN
        BulkDatabaseMiner.load(null);
    }

    @Test
    public void load_whenProvidedDatabaseObjects_shouldAssignObjectsAndReturnInstance() {
        // GIVEN
        ArrayList<DbDto> topicObjects = new ArrayList<>();

        // WHEN
        BulkDatabaseMiner bulkDatabaseMiner = BulkDatabaseMiner.load(topicObjects);

        // THEN
        assertThat(bulkDatabaseMiner).isNotNull();
        assertThat(bulkDatabaseMiner.getTopicObjects()).isEqualTo(topicObjects);
    }

    @Test
    public void load_whenProvidedDatabaseObjects_shouldGenerateUniqueMinerIdentifiers() {
        // GIVEN
        ArrayList<DbDto> topicObjects = new ArrayList<>();

        // WHEN
        BulkDatabaseMiner bulkDatabaseMiner1 = BulkDatabaseMiner.load(topicObjects);
        BulkDatabaseMiner bulkDatabaseMiner2 = BulkDatabaseMiner.load(topicObjects);

        // THEN
        assertThat(bulkDatabaseMiner1.getId()).isNotEqualTo(bulkDatabaseMiner2.getId());
    }

    @Test
    public void getDatabaseTopic_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<DbDto> actualResult = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopic(ACHIEVEMENTS);

        // THEN
        StrictAssertions.assertThat(actualResult).isEmpty();
    }

    @Test
    public void getDatabaseTopic_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopic(BOTS).get();

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void getDatabaseTopicFromReference_whenNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopicFromReference("000");

        // THEN: exception
    }

    @Test
    public void getDatabaseTopicFromReference_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjectsFromResources).getDatabaseTopicFromReference("111");

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    static List<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        return asList(
                FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"),
                FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_PNJ_FAKE.json"));
    }
}
