package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMinerTest.createTopicObjectsFromResources;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BulkDatabaseMiner_focusOnContentsTest {

    private static List<DbDto> topicObjectsFromResources;
    static {
        try {
            topicObjectsFromResources = createTopicObjectsFromResources();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        // Set level to TRACE to get performance information
//        Log.set(LEVEL_DEBUG);
//        Log.setLogger(new PerformanceLogger(Paths.get("perfs").toAbsolutePath()));
    }

    @Test
    void getContentEntryFromTopicWithInternalIdentifier_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(1, INTERIOR));
    }

    @Test
    void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(10, BOTS);

        // THEN
        assertThat(actualEntry).isEmpty();
    }

    @Test
    void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(0, BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
    }

    @Test
    void getContentEntriesMatchingCriteria_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        DbFieldValueDto criteria = DbFieldValueDto.fromCouple(1, "606298799");

        // WHEN
        List<ContentEntryDto> actualEntries = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntriesMatchingSimpleCondition(criteria, BOTS);

        // THEN
        assertThat(actualEntries)
                .hasSize(1)
                .extracting("id").containsExactly(0);
    }

    @Test
    void getContentEntriesMatchingCriteria_whenEntryNotFound_shouldReturnEmptyList() throws IOException, URISyntaxException {
        // GIVEN
        DbFieldValueDto criteria = DbFieldValueDto.fromCouple(1, "0000000");

        // WHEN
        List<ContentEntryDto> actualEntries = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntriesMatchingSimpleCondition(criteria, BOTS);

        // THEN
        assertThat(actualEntries).isEmpty();
    }

    @Test
    void getContentEntryFromTopicWithRef_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("", DbDto.Topic.RIMS));
    }

    @Test
    void getContentEntryFromTopicWithRef_whenRefNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("1500", BOTS)).isEmpty();
    }

    @Test
    void getContentEntryFromTopicWithRef_whenRefFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("606298799", BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
        assertThat(actualEntry.get().getId()).isEqualTo(0);
        assertThat(actualEntry.get().getItemAtRank(1).get().getRawValue()).isEqualTo("606298799");
    }

    @Test
    void getContentEntryFromTopicWithRef_whenPseudoRefFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("55736935|5", ACHIEVEMENTS);

        // THEN
        assertThat(actualEntry).isPresent();
        assertThat(actualEntry.get().getId()).isEqualTo(0);
        assertThat(actualEntry.get().getItemAtRank(1).get().getRawValue()).isEqualTo("55736935");
        assertThat(actualEntry.get().getItemAtRank(2).get().getRawValue()).isEqualTo("5");
    }

    @Test
    void getContentEntryFromTopicWithItemValues_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                        null,
                        TUTORIALS));
    }

    @Test
    void getContentEntryFromTopicWithItemValues_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                asList("540091906" ,"824778956"),
                PNJ)
        ).isEmpty();
    }

    @Test
    void getContentEntryFromTopicWithItemValues_whenFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        final ContentEntryDto actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                asList("540091906", "82477895"),
                PNJ)
                .get();

        // THEN
        assertThat(actualEntry.getId()).isEqualTo(0);
        assertThat(actualEntry.getItems()).hasSize(2);
    }

    @Test
    void getContentEntryReference_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> BulkDatabaseMiner.getContentEntryReference(null, -1));
    }

    @Test
    void getContentEntryReference_whenUidFieldNotAvailable_shouldThrowException() {
        // GIVEN
        ContentItemDto item  = createContentItemWithRank(1);
        ContentItemDto otherItem = createContentItemWithRank(2);
        ContentEntryDto entry = createContentEntryWithItems(asList(item, otherItem));

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> BulkDatabaseMiner.getContentEntryReference(entry, 3));
    }

    @Test
    void getContentEntryReference_whenUidFieldAvailable_shouldReturnRef() {
        // GIVEN
        ContentItemDto uidItem  = ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue("123456789")
                .build();
        ContentItemDto otherItem = createContentItemWithRank(2);
        ContentEntryDto entry = createContentEntryWithItems(asList(uidItem, otherItem));

        // WHEN
        String actualEntryReference = BulkDatabaseMiner.getContentEntryReference(entry, 1);

        // THEN
        assertThat(actualEntryReference).isEqualTo("123456789");
    }

    @Test
    void getRemoteContentEntryWithInternalIdentifier_whenEntryInternalIdentifierDoesNotExist_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();
        DbDto.Topic sourceTopic = DbDto.Topic.PNJ;
        DbDto.Topic targetTopic = DbDto.Topic.CLOTHES;

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 2, 10, targetTopic));
    }

    @Test
    void getRemoteContentEntryWithInternalIdentifier_whenRemoteEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();
        DbDto.Topic sourceTopic = DbDto.Topic.PNJ;
        DbDto.Topic targetTopic = DbDto.Topic.CLOTHES;

        // THEN
        Optional<ContentEntryDto> potentialRemoteEntry = BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 1, 0, targetTopic);

        // WHEN
        assertThat(potentialRemoteEntry).isEmpty();
    }

    @Test
    void getRemoteContentEntryWithInternalIdentifier_whenRemoteEntryExists_shouldReturnEntry() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();
        ContentEntryDto expectedEntry = topicObjects.get(1).getData().getEntries().get(0);
        DbDto.Topic sourceTopic = DbDto.Topic.PNJ;
        DbDto.Topic targetTopic = DbDto.Topic.CLOTHES;

        // THEN
        Optional<ContentEntryDto> potentialRemoteEntry = BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 2, 0, targetTopic);

        // WHEN
        assertThat(potentialRemoteEntry).contains(expectedEntry);
    }

    @Test
    void getContentEntryReferenceWithInternalIdentifier_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(10, DbDto.Topic.PNJ);

        // THEN
        assertThat(potentialEntryReference).isEmpty();
    }

    @Test
    void getContentEntryReferenceWithInternalIdentifier_whenUidFieldAbsent_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithoutUidFieldFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(0, ACHIEVEMENTS);

        // THEN
        assertThat(potentialEntryReference).isEmpty();
    }

    @Test
    void getContentEntryReferenceWithInternalIdentifier_whenEntryExists_andUidFieldPresent_shouldReturnRawValue() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(0, DbDto.Topic.PNJ);

        // THEN
        assertThat(potentialEntryReference).contains("540091906");
    }

    @Test
    void getContentItemWithEntryIdentifierAndFieldRank_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 1, 10);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    void getContentItemWithEntryIdentifierAndFieldRank_whenItemDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 10, 0);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    void getContentItemWithEntryIdentifierAndFieldRank_whenItemExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ContentItemDto expectedItem = topicObjectsFromResources.get(0).getData().getEntries().get(0).getItems().get(0);

        // WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(ACHIEVEMENTS, 1, 0);

        // THEN
        assertThat(potentialItem).contains(expectedItem);
    }

    @Test
    void getContentEntryInternalIdentifierWithReference_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        OptionalInt potentialInternalId = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryInternalIdentifierWithReference("REF", BOTS);

        // THEN
        assertThat(potentialInternalId).isEmpty();
    }

    @Test
    void getContentEntryInternalIdentifierWithReference_whenEntryExists_shouldReturnInternalId() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        OptionalInt potentialInternalId = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryInternalIdentifierWithReference("606298799", BOTS);

        // THEN
        assertThat(potentialInternalId).hasValue(0);
    }

    private static ArrayList<DbDto> createTopicObjectsWithoutUidFieldFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Achievements_FAKE.json"));

        return dbDtos;
    }

    private static ArrayList<DbDto> createTopicObjectsWithRemoteReferencesFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_PNJ_FAKE.json"));
        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Clothes_FAKE.json"));

        return dbDtos;
    }

    private ContentEntryDto createContentEntryWithItems(List<ContentItemDto> items) {
        return ContentEntryDto.builder()
                .addItems(items)
                .build();
    }

    private static ContentItemDto createContentItemWithRank(int fieldRank) {
        return ContentItemDto.builder()
                .ofFieldRank(fieldRank)
                .withRawValue("123")
                .build();
    }
}
