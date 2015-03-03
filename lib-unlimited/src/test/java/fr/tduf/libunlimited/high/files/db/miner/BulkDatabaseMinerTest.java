package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

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
    public void getAllResourcesFromTopic_whenTopicNotFound_shouldReturnEmpty() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<List<DbResourceDto>> actualResult = BulkDatabaseMiner.load(topicObjects).getAllResourcesFromTopic(DbDto.Topic.ACHIEVEMENTS);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getAllResourcesFromTopic_shouldReturnAllLocales() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        List<DbResourceDto> actualResources = BulkDatabaseMiner.load(topicObjects).getAllResourcesFromTopic(DbDto.Topic.BOTS).get();

        // THEN
        assertThat(actualResources).hasSize(2);
    }

    @Test
    public void getResourceFromTopicAndLocale_whenLocaleNotFound_shouldReturnEmpty() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.BOTS, DbResourceDto.Locale.UNITED_STATES);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceFromTopicAndLocale_whenTopicNotFound_shouldReturnEmpty() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.ACHIEVEMENTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceFromTopicAndLocale_whenExists_shouldReturnIt() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceFromTopicAndLocale(DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isPresent();
    }

    @Test(expected = NoSuchElementException.class)
    public void getDatabaseTopic_whenNotFound_shouldThrowException() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(DbDto.Topic.ACHIEVEMENTS);

        // THEN: exception
    }

    @Test
    public void getDatabaseTopic_whenExists_shouldReturnIt() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(DbDto.Topic.BOTS);

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test(expected = NoSuchElementException.class)
    public void getDatabaseTopicFromReference_whenNotFound_shouldThrowException() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getDatabaseTopicFromReference("000");

        // THEN: exception
    }

    @Test
    public void getDatabaseTopicFromReference_whenExists_shouldReturnIt() {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjects();

        // WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjects).getDatabaseTopicFromReference("111");

        // THEN
        assertThat(actualTopicObject).isNotNull();
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithInternalIdentifier_whenTopicNotFound_shouldThrowException() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(1, DbDto.Topic.ACHIEVEMENTS);

        // THEN: exception
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldThrowException() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(10, DbDto.Topic.BOTS);

        // THEN: exception
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        DbDataDto.Entry actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(0, DbDto.Topic.BOTS);

        // THEN
        assertThat(actualEntry).isNotNull();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenTopicNotFound_shouldReturnEmpty() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.ACHIEVEMENTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenLocaleNotFound_shouldReturnEmpty() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.BOTS, DbResourceDto.Locale.KOREA);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenEntryNotFound_shouldReturnEmpty() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000002", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndLocaleWithReference_whenEntryFound_shouldReturnIt() {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjects();

        // WHEN
        Optional<DbResourceDto.Entry> actualResult = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndLocaleWithReference("00000001", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);

        // THEN
        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getValue()).isEqualTo("FR");
    }

    private static ArrayList<DbDto> createTopicObjects() {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(DbDto.Topic.BOTS)
                        .forReference("111")
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(DbDataDto.Entry.builder().build())
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(DbResourceDto.Locale.FRANCE)
                        .addEntry(DbResourceDto.Entry.builder()
                                .forReference("00000001")
                                .withValue("FR")
                                .build())
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(DbResourceDto.Locale.GERMANY)
                        .addEntry(DbResourceDto.Entry.builder()
                                .forReference("00000001")
                                .withValue("GE")
                                .build())
                        .build())
                .build());

        return dbDtos;
    }
}