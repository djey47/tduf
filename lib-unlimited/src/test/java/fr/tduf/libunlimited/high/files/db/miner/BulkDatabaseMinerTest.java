package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BOTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class BulkDatabaseMinerTest {

    @Before
    public void setUp() {
        // Set level to TRACE to get performance information
//        Log.set(LEVEL_DEBUG);
//        Log.setLogger(new PerformanceLogger(Paths.get("perfs").toAbsolutePath()));

        BulkDatabaseMiner.clearAllCaches();
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
    public void getCacheKey_whenThreeItems_shouldReturnFormattedKey() {
        // GIVEN-WHEN
        String actualKey = BulkDatabaseMiner.getCacheKey("a", "b", "c");

        // THEN
        assertThat(actualKey).isEqualTo("a:b:c");
    }

    // TODO move yo dedicated test class

    @Test
    public void getDatabaseTopic_whenNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDto> actualResult = BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(ACHIEVEMENTS);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    public void getDatabaseTopic_whenExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        ArrayList<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        DbDto actualTopicObject = BulkDatabaseMiner.load(topicObjects).getDatabaseTopic(BOTS).get();

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
        BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(1, ACHIEVEMENTS);

        // THEN: exception
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(10, BOTS);

        // THEN
        assertThat(actualEntry).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(0, BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
    }

    @Test
    public void getContentEntriesMatchingCriteria_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();
        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(1, "606298799"));

        // WHEN
        List<DbDataDto.Entry> actualEntries = BulkDatabaseMiner.load(topicObjects).getContentEntriesMatchingCriteria(criteria, BOTS);

        // THEN
        assertThat(actualEntries)
                .hasSize(1)
                .extracting("id").containsExactly(0L);
    }

    @Test
    public void getContentEntriesMatchingCriteria_whenEntryNotFound_shouldReturnEmptyList() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();
        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(1, "0000000"));

        // WHEN
        List<DbDataDto.Entry> actualEntries = BulkDatabaseMiner.load(topicObjects).getContentEntriesMatchingCriteria(criteria, BOTS);

        // THEN
        assertThat(actualEntries).isEmpty();
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
        assertThat(BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithReference("1500", BOTS)).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithRef_whenRefFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithReference("606298799", BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
        assertThat(actualEntry.get().getId()).isEqualTo(0);
        assertThat(actualEntry.get().getItemAtRank(1).get().getRawValue()).isEqualTo("606298799");
    }

    @Test
    public void getContentItemFromEntryAtFieldRank_whenNoItem_shouldReturnAbsent() {
        // GIVEN
        DbDataDto.Entry entry = DbDataDto.Entry.builder().build();

        // WHEN
        Optional<DbDataDto.Item> potentialItem = BulkDatabaseMiner.getContentItemFromEntryAtFieldRank(entry, 1);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    public void getContentItemFromEntryAtFieldRank_whenItemAtSameRank_shouldReturnIt() {
        // GIVEN
        DbDataDto.Item expectedItem = createContentItemWithRank(1);
        DbDataDto.Item otherItem = createContentItemWithRank(2);
        DbDataDto.Entry entry = createContentEntryWithItems(asList(expectedItem, otherItem));

        // WHEN
        Optional<DbDataDto.Item> potentialItem = BulkDatabaseMiner.getContentItemFromEntryAtFieldRank(entry, 1);

        // THEN
        assertThat(potentialItem).contains(expectedItem);
    }

    @Test(expected = NullPointerException.class)
    public void getContentEntryReference_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN
        BulkDatabaseMiner.getContentEntryReference(null, -1);

        // THEN: NPE
    }

    @Test(expected = NoSuchElementException.class)
    public void getContentEntryReference_whenUidFieldNotAvailable_shouldThrowException() {
        // GIVEN
        DbDataDto.Item item  = createContentItemWithRank(1);
        DbDataDto.Item otherItem = createContentItemWithRank(2);
        DbDataDto.Entry entry = createContentEntryWithItems(asList(item, otherItem));

        // WHEN
        BulkDatabaseMiner.getContentEntryReference(entry, 3);

        // THEN: NSE
    }

    @Test
    public void getContentEntryReference_whenUidFieldAvailable_shouldReturnRef() {
        // GIVEN
        DbDataDto.Item uidItem  = DbDataDto.Item.builder()
                .ofFieldRank(1)
                .withRawValue("123456789")
                .build();
        DbDataDto.Item otherItem = createContentItemWithRank(2);
        DbDataDto.Entry entry = createContentEntryWithItems(asList(uidItem, otherItem));

        // WHEN
        String actualEntryReference = BulkDatabaseMiner.getContentEntryReference(entry, 1);

        // THEN
        assertThat(actualEntryReference).isEqualTo("123456789");
    }

    @Test(expected = NoSuchElementException.class)
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
        Optional<DbDataDto.Entry> potentialRemoteEntry = BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 1, 0, targetTopic);

        // WHEN
        assertThat(potentialRemoteEntry).isEmpty();
    }

    @Test
    public void getRemoteContentEntryWithInternalIdentifier_whenRemoteEntryExists_shouldReturnEntry() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsWithRemoteReferencesFromResources();
        DbDataDto.Entry expectedEntry = topicObjects.get(1).getData().getEntries().get(0);
        DbDto.Topic sourceTopic = DbDto.Topic.PNJ;
        DbDto.Topic targetTopic = DbDto.Topic.CLOTHES;

        // THEN
        Optional<DbDataDto.Entry> potentialRemoteEntry = BulkDatabaseMiner.load(topicObjects).getRemoteContentEntryWithInternalIdentifier(sourceTopic, 2, 0, targetTopic);

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
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Item> potentialItem = BulkDatabaseMiner.load(topicObjects).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 1, 10);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    public void getContentItemWithEntryIdentifierAndFieldRank_whenItemDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Item> potentialItem = BulkDatabaseMiner.load(topicObjects).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 10, 0);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    public void getContentItemWithEntryIdentifierAndFieldRank_whenItemExists_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();
        DbDataDto.Item expectedItem = topicObjects.get(0).getData().getEntries().get(0).getItems().get(0);

        // WHEN
        Optional<DbDataDto.Item> potentialItem = BulkDatabaseMiner.load(topicObjects).getContentItemWithEntryIdentifierAndFieldRank(BOTS, 1, 0);

        // THEN
        assertThat(potentialItem).contains(expectedItem);
    }

    @Test
    public void getContentEntryInternalIdentifierWithReference_whenEntryDoesNotExist_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        OptionalLong potentialInternalId = BulkDatabaseMiner.load(topicObjects).getContentEntryInternalIdentifierWithReference("REF", BOTS);

        // THEN
        assertThat(potentialInternalId).isEmpty();
    }

    @Test
    public void getContentEntryInternalIdentifierWithReference_whenEntryExists_shouldReturnInternalId() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        OptionalLong potentialInternalId = BulkDatabaseMiner.load(topicObjects).getContentEntryInternalIdentifierWithReference("606298799", BOTS);

        // THEN
        assertThat(potentialInternalId).hasValue(0);
    }

    private static ArrayList<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"));

        return dbDtos;
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

    private DbDataDto.Entry createContentEntryWithItems(List<DbDataDto.Item> items) {
        return DbDataDto.Entry.builder()
                .addItems(items)
                .build();
    }

    private static DbDataDto.Item createContentItemWithRank(int fieldRank) {
        return DbDataDto.Item.builder()
                .ofFieldRank(fieldRank)
                .build();
    }
}
