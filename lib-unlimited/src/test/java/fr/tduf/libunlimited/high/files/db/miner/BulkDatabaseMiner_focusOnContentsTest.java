package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMinerTest.createTopicObjectsFromResources;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class BulkDatabaseMiner_focusOnContentsTest {

    private List<DbDto> topicObjectsFromResources;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        topicObjectsFromResources = createTopicObjectsFromResources();

        // Set level to TRACE to get performance information
//        Log.set(LEVEL_DEBUG);
//        Log.setLogger(new PerformanceLogger(Paths.get("perfs").toAbsolutePath()));
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithInternalIdentifier_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(1, ACHIEVEMENTS);

        // THEN: exception
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(10, BOTS);

        // THEN
        assertThat(actualEntry).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithInternalIdentifier(0, BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
    }

    @Test
    public void getContentEntriesMatchingCriteria_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        DbFieldValueDto criteria = DbFieldValueDto.fromCouple(1, "606298799");

        // WHEN
        List<ContentEntryDto> actualEntries = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntriesMatchingSimpleCondition(criteria, BOTS);

        // THEN
        assertThat(actualEntries)
                .hasSize(1)
                .extracting("id").containsExactly(0L);
    }

    @Test
    public void getContentEntriesMatchingCriteria_whenEntryNotFound_shouldReturnEmptyList() throws IOException, URISyntaxException {
        // GIVEN
        DbFieldValueDto criteria = DbFieldValueDto.fromCouple(1, "0000000");

        // WHEN
        List<ContentEntryDto> actualEntries = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntriesMatchingSimpleCondition(criteria, BOTS);

        // THEN
        assertThat(actualEntries).isEmpty();
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithRef_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        assertThat(BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("", DbDto.Topic.RIMS)).isEmpty();

        // THEN: NSE
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenRefNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("1500", BOTS)).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenRefFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentEntryDto> actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithReference("606298799", BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
        assertThat(actualEntry.get().getId()).isEqualTo(0);
        assertThat(actualEntry.get().getItemAtRank(1).get().getRawValue()).isEqualTo("606298799");
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryFromTopicWithItemValues_whenTopicNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                null,
                TUTORIALS);

        // THEN: NSE
    }

    @Test
    public void getContentEntryFromTopicWithItemValues_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThat(BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                asList("540091906" ,"824778956"),
                PNJ)
        ).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithItemValues_whenFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        final ContentEntryDto actualEntry = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryFromTopicWithItemValues(
                asList("540091906", "82477895"),
                PNJ)
                .get();

        // THEN
        assertThat(actualEntry.getId()).isEqualTo(0);
        assertThat(actualEntry.getItems()).hasSize(2);
    }

    @Test(expected = NullPointerException.class)
    public void getContentEntryReference_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN
        BulkDatabaseMiner.getContentEntryReference(null, -1);

        // THEN: NPE
    }

    @Test(expected = IllegalStateException.class)
    public void getContentEntryReference_whenUidFieldNotAvailable_shouldThrowException() {
        // GIVEN
        ContentItemDto item  = createContentItemWithRank(1);
        ContentItemDto otherItem = createContentItemWithRank(2);
        ContentEntryDto entry = createContentEntryWithItems(asList(item, otherItem));

        // WHEN
        BulkDatabaseMiner.getContentEntryReference(entry, 3);

        // THEN: NSE
    }

    @Test
    public void getContentEntryReference_whenUidFieldAvailable_shouldReturnRef() {
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

    @Test(expected = IllegalStateException.class)
    public void getRemoteContentEntryWithInternalIdentifier_whenEntryInternalIdentifierDoesNotExist_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();
        DbDto.Topic sourceTopic = DbDto.Topic.PNJ;
        DbDto.Topic targetTopic = DbDto.Topic.CLOTHES;

        // THEN
        BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 2, 10, targetTopic);

        // WHEN:NSE
    }

    @Test
    public void getRemoteContentEntryWithInternalIdentifier_whenRemoteEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
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
    public void getRemoteContentEntryWithInternalIdentifier_whenRemoteEntryExists_shouldReturnEntry() throws IOException, URISyntaxException {
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
    public void getContentEntryReferenceWithInternalIdentifier_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(10, DbDto.Topic.PNJ);

        // THEN
        assertThat(potentialEntryReference).isEmpty();
    }

    @Test
    public void getContentEntryReferenceWithInternalIdentifier_whenUidFieldAbsent_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithoutUidFieldFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(0, ACHIEVEMENTS);

        // THEN
        assertThat(potentialEntryReference).isEmpty();
    }

    @Test
    public void getContentEntryReferenceWithInternalIdentifier_whenEntryExists_andUidFieldPresent_shouldReturnRawValue() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();

        // WHEN
        Optional<String> potentialEntryReference = BulkDatabaseMiner.load(topicObjects).getContentEntryReferenceWithInternalIdentifier(0, DbDto.Topic.PNJ);

        // THEN
        assertThat(potentialEntryReference).contains("540091906");
    }

    @Test
    public void getContentItemWithEntryIdentifierAndFieldRank_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 1, 10);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    public void getContentItemWithEntryIdentifierAndFieldRank_whenItemDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 10, 0);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    public void getContentItemWithEntryIdentifierAndFieldRank_whenItemExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ContentItemDto expectedItem = topicObjectsFromResources.get(0).getData().getEntries().get(0).getItems().get(0);

        // WHEN
        Optional<ContentItemDto> potentialItem = BulkDatabaseMiner.load(topicObjectsFromResources).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 1, 0);

        // THEN
        assertThat(potentialItem).contains(expectedItem);
    }

    @Test
    public void getContentEntryInternalIdentifierWithReference_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        OptionalLong potentialInternalId = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryInternalIdentifierWithReference("REF", BOTS);

        // THEN
        assertThat(potentialInternalId).isEmpty();
    }

    @Test
    public void getContentEntryInternalIdentifierWithReference_whenEntryExists_shouldReturnInternalId() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        OptionalLong potentialInternalId = BulkDatabaseMiner.load(topicObjectsFromResources).getContentEntryInternalIdentifierWithReference("606298799", BOTS);

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
                .build();
    }
}
