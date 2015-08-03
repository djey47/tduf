package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static java.util.Arrays.asList;
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

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryNotFound_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(10, DbDto.Topic.BOTS);

        // THEN
        assertThat(actualEntry).isEmpty();
    }

    @Test
    public void getContentEntryFromTopicWithInternalIdentifier_whenEntryFound_shouldReturnIt() throws IOException, URISyntaxException {
        // GIVEN
        List<DbDto> topicObjects = createTopicObjectsFromResources();

        // WHEN
        Optional<DbDataDto.Entry> actualEntry = BulkDatabaseMiner.load(topicObjects).getContentEntryFromTopicWithInternalIdentifier(0, DbDto.Topic.BOTS);

        // THEN
        assertThat(actualEntry).isPresent();
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
    public void getAllResourceValuesForReference_whenAllValuesPresent_shouldReturnThem() {
        // GIVEN
        DbResourceDto resourceObject1 = createResourceObjectWithSingleEntry("111111", "A");
        DbResourceDto resourceObject2 = createResourceObjectWithSingleEntry("111111", "B");
        DbResourceDto resourceObject3 = createResourceObjectWithSingleEntry("111111", "C");
        DbResourceDto resourceObject4 = createResourceObjectWithSingleEntry("111111", "A");
        List<DbResourceDto> topicResourceObjects = asList(resourceObject1, resourceObject2, resourceObject3, resourceObject4);

        // WHEN
        Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference("111111", topicResourceObjects);

        // THEN
        assertThat(actualValues)
                .hasSize(3)
                .contains("A", "B", "C");
    }

    @Test
    public void getAllResourceValuesForReference_whenValueMissingForLocale_shouldReturnOnlyExistingValues() {
        // GIVEN
        DbResourceDto resourceObject1 = createResourceObjectWithSingleEntry("111111", "A");
        DbResourceDto resourceObject2 = createResourceObjectWithSingleEntry("000000", "Z");
        DbResourceDto resourceObject3 = createResourceObjectWithSingleEntry("111111", "B");
        DbResourceDto resourceObject4 = createResourceObjectWithSingleEntry("111111", "C");
        List<DbResourceDto> topicResourceObjects = asList(resourceObject1, resourceObject2, resourceObject3, resourceObject4);

        // WHEN
        Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference("111111", topicResourceObjects);

        // THEN
        assertThat(actualValues)
                .hasSize(3)
                .contains("A", "B", "C");
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
        DbDataDto.Entry entry = createContentEntryWithTwoItems(expectedItem, otherItem);

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
        DbDataDto.Entry entry = createContentEntryWithTwoItems(item, otherItem);

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
        DbDataDto.Entry entry = createContentEntryWithTwoItems(uidItem, otherItem);

        // WHEN
        String actualEntryReference = BulkDatabaseMiner.getContentEntryReference(entry, 1);

        // THEN
        assertThat(actualEntryReference).isEqualTo("123456789");
    }

    private static ArrayList<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"));

        return dbDtos;
    }

    private static DbResourceDto createResourceObjectWithSingleEntry(String reference, String value) {
        return DbResourceDto.builder()
                .addEntry(DbResourceDto.Entry.builder()
                                .forReference(reference)
                                .withValue(value)
                                .build()
                )
                .build();
    }

    private static DbDataDto.Entry createContentEntryWithTwoItems(DbDataDto.Item item1, DbDataDto.Item item2) {
        return DbDataDto.Entry.builder()
                .addItem(item1, item2)
                .build();
    }

    private static DbDataDto.Item createContentItemWithRank(int fieldRank) {
        return DbDataDto.Item.builder()
                .ofFieldRank(fieldRank)
                .build();
    }
}