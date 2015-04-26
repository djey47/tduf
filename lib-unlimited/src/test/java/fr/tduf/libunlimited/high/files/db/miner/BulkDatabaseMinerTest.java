package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkDatabaseMinerTest {

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
    public void getAllResourcesFromTopic_whenTopicNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<List<DbResourceDto>> actualResult = BulkDatabaseMiner.load(topicObjects).getAllResourcesFromTopic(DbDto.Topic.ACHIEVEMENTS);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getAllResourcesFromTopic_shouldReturnAllLocales() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        List<DbResourceDto> actualResources = BulkDatabaseMiner.load(topicObjects).getAllResourcesFromTopic(DbDto.Topic.BOTS).get();

        // THEN
        assertThat(actualResources).hasSize(2);
    }

    @Test
    public void getResourceFromTopicAndLocale_whenLocaleNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.BOTS, DbResourceDto.Locale.UNITED_STATES);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceFromTopicAndLocale_whenTopicNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.ACHIEVEMENTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceFromTopicAndLocale_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isPresent();
    }

    @Test
    public void getDatabaseTopic_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDto> actualResult = BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(DbDto.Topic.ACHIEVEMENTS);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getDatabaseTopic_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(DbDto.Topic.BOTS).get();

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test(expected = NoSuchElementException.class)
    public void getDatabaseTopicFromReference_whenNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getDatabaseTopicFromReference("000");

        // THEN: exception
    }

    @Test
    public void getDatabaseTopicFromReference_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjects).getDatabaseTopicFromReference("111");

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithInternalIdentifier_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(1, DbDto.Topic.ACHIEVEMENTS);

        // THEN: exception
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(10, DbDto.Topic.BOTS);

        // THEN: exception
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        DbDataDto.Entry actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(0, DbDto.Topic.BOTS);

        // THEN
        assertThat(actualEntry).isNotNull();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenTopicNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.ACHIEVEMENTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenLocaleNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.BOTS, DbResourceDto.Locale.KOREA);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenEntryNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000002", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getValue()).isEqualTo("FR");
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenTopicNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithReference("", DbDto.Topic.RIMS)).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenRefNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithReference("1500", DbDto.Topic.BOTS)).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenRefFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithReference("606298799", DbDto.Topic.BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
        assertThat(actualEntry.get().getId()).isEqualTo(0);
        assertThat(actualEntry.get().getItems().get(0).getRawValue()).isEqualTo("606298799");
    }

    @Test
    public void getUidFieldRank_whenNoIdentifierField_shouldReturnAbsent() {
        // GIVEN
        List<DbStructureDto.Field> structureFields = new ArrayList<>();

        // WHEN-THEN
        assertThat(BulkDatabaseMiner.getUidFieldRank(structureFields).isPresent()).isFalse();
    }

    @Test
    public void getUidFieldRank_whenIdentifierFieldPresent_shouldReturnRank() throws IOException, URISyntaxException {
        // GIVEN
        List<DbStructureDto.Field> structureFields = createTopicObjectsFromResources().get(0).getStructure().getFields();

        // WHEN-THEN
        assertThat(BulkDatabaseMiner.getUidFieldRank(structureFields)).contains(1);
    }

    private static ArrayList<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"));

        return dbDtos;
    }
}