package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


public class DatabasePatcherTest {

    private static final Class<DatabasePatcherTest> thisClass = DatabasePatcherTest.class;

    @Before
    public void setUp() {
        BulkDatabaseMiner.clearAllCaches();
    }

    @Test(expected = NullPointerException.class)
    public void apply_whenNullPatchObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        createPatcher(createDefaultDatabaseObjects()).apply(null);

        // THEN: NPE
    }

    @Test
    public void apply_whenUpdateResourcesPatch_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333", "Cindy");
    }

    @Test
    public void apply_whenUpdateResourcesPatch_forAllLocales_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources-all.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333", "Cindy");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "33333333", "Cindy");
    }

    @Test
    public void apply_whenDeleteResourcesPatch_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "60367256");
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333");
    }

    @Test
    public void apply_whenDeleteResourcesPatch_forAllLocales_shouldRemoveExistingEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteResources-all.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "60367256");
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333");
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "60367256");
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "33333333");
    }

    @Test(expected = IllegalArgumentException.class)
    public void apply_whenUpdateContentsPatch_forAllFields_andIncorrectValueCount_shouldThrowException() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateContents-addAll-badCount.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN: IAE
    }

    @Test
    public void apply_whenUpdateContentsPatch_forAllFields_shouldAddNewEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(DbDto.Topic.BOTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 1;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 1);

        DbDataDto.Entry actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(8);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("57167257");
    }
    @Test
    public void apply_whenUpdateContentsPatch_forAllFields_withRefSupport_shouldAddNewEntryAndUpdateExisting() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateContents-addAll-ref.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 1;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 1);

        DbDataDto.Entry actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(103);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("1221657049");

        DbDataDto.Entry actualUpdatedEntry = databaseMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA).get();
        assertThat(actualUpdatedEntry.getId()).isEqualTo(0);

        assertThat(actualUpdatedEntry.getItems()).hasSize(103);
        assertThat(actualUpdatedEntry.getItems().get(1).getRawValue()).isEqualTo("864426");
    }

    @Test
    public void apply_whenUpdateContentsPatch_withAssociationEntries_shouldCreateThem() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateContents-addAll-assoc.mini.json");
        DbDto databaseObject1 = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");
        DbDto databaseObject2 = readObjectFromResource(DbDto.class, "/db/json/TDU_CarRims.json");
        DbDto databaseObject3 = readObjectFromResource(DbDto.class, "/db/json/TDU_CarColors.json");
        DbDto databaseObject4 = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPacks.json");

        List<DbDto> databaseObjects = asList(databaseObject1, databaseObject2, databaseObject3, databaseObject4);
        DatabasePatcher patcher = createPatcher(databaseObjects);

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(databaseObjects);
        List<DbDataDto.Entry> carRimsTopicEntries = databaseMiner.getDatabaseTopic(CAR_RIMS).get().getData().getEntries();
        int carRimsPreviousEntryCount = carRimsTopicEntries.size();
        List<DbDataDto.Entry> carColorsTopicEntries = databaseMiner.getDatabaseTopic(CAR_COLORS).get().getData().getEntries();
        int carColorsPreviousEntryCount = carColorsTopicEntries.size();
        List<DbDataDto.Entry> carPacksTopicEntries = databaseMiner.getDatabaseTopic(CAR_PACKS).get().getData().getEntries();
        int carPacksPreviousEntryCount = carPacksTopicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        assertThat(carColorsTopicEntries).hasSize(carColorsPreviousEntryCount + 4);
        assertThat(carRimsTopicEntries).hasSize(carRimsPreviousEntryCount + 1);
        assertThat(carPacksTopicEntries).hasSize(carPacksPreviousEntryCount + 1);
    }

    @Test
    public void apply_whenUpdateContentsPatch_andBitfield_shouldUpdateBitfield() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateContents-mixed-bitfield.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarShops.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        DbDataDto.Entry actualModifiedEntry = databaseMiner.getContentEntryFromTopicWithReference("589356824", CAR_SHOPS).get();
        assertThat(actualModifiedEntry.getItems().get(18).getSwitchValues())
                .isNotNull()
                .isNotEmpty();

        DbDataDto.Entry actualCreatedEntry = databaseMiner.getContentEntryFromTopicWithReference("00000000", CAR_SHOPS).get();
        assertThat(actualCreatedEntry.getItems().get(18).getSwitchValues())
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    public void apply_whenUpdateContentsPatch_forOneItem_andREFDoesNotExist_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updatePartialContents-newRef.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);
    }

    @Test
    public void apply_whenUpdateContentsPatch_forOneItem_andREFExists_butInvalidRank_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updatePartialContents-badRank-existingRef.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();
        int previousHashCode = getEntryHashCode(databaseMiner, "1139121456", CAR_PHYSICS_DATA);


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        int actualHashCode = getEntryHashCode(databaseMiner, "1139121456", CAR_PHYSICS_DATA);
        assertThat(actualHashCode).isEqualTo(previousHashCode);
    }

    @Test
    public void apply_whenUpdateContentsPatch_forNoItem_andREFExists_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updatePartialContents-noValues-existingRef.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();
        int previousHashCode = getEntryHashCode(databaseMiner, "1139121456", CAR_PHYSICS_DATA);


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        int actualHashCode = getEntryHashCode(databaseMiner, "1139121456", CAR_PHYSICS_DATA);
        assertThat(actualHashCode).isEqualTo(previousHashCode);
    }

    @Test
    public void apply_whenUpdateContentsPatch_forOneItem_andREFExists_shouldChangeIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updatePartialContents-existingRef.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();
        List<String> previousValues = extractAllItemsFromCarPhysicsEntryExceptedLastOne(databaseMiner, "1139121456");


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        List<String> actualValues = extractAllItemsFromCarPhysicsEntryExceptedLastOne(databaseMiner, "1139121456");
        assertThat(actualValues).isEqualTo(previousValues);

        String actualItemValue = databaseMiner
                .getContentEntryFromTopicWithReference("1139121456", CAR_PHYSICS_DATA).get()
                .getItems()
                .get(102)
                .getRawValue();
        assertThat(actualItemValue).isEqualTo("7954");
    }

    @Test
    public void apply_whenDeleteContentsPatch_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteContentsPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteContents-ref.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteContentsPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
    }

    private static DatabasePatcher createPatcher(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabasePatcher.class, databaseObjects);
    }

    private static List<DbDto> createDefaultDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }

    private static <T> T readObjectFromResource(Class<T> objectClass, String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), objectClass);
    }

    private static int getEntryHashCode(BulkDatabaseMiner databaseMiner, String ref, DbDto.Topic topic) {
        return databaseMiner
                .getContentEntryFromTopicWithReference(ref, topic)
                .get().hashCode();
    }

    private static List<String> extractAllItemsFromCarPhysicsEntryExceptedLastOne(BulkDatabaseMiner databaseMiner, String ref) {
        return databaseMiner
                .getContentEntryFromTopicWithReference(ref, CAR_PHYSICS_DATA).get()
                .getItems().stream()
                .filter((item) -> item.getFieldRank() != 103)
                .map(DbDataDto.Item::getRawValue)
                .collect(toList());
    }

    private static void assertResourceEntryPresentAndMatch(BulkDatabaseMiner databaseMiner, DbDto.Topic topic, DbResourceDto.Locale locale, String ref, String value) {
        Optional<DbResourceDto.Entry> actualUpdatedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);
        assertThat(actualUpdatedEntry).isPresent();
        assertThat(actualUpdatedEntry.get().getValue()).isEqualTo(value);
    }

    private static void assertResourceEntryMissing(BulkDatabaseMiner databaseMiner, DbDto.Topic topic, DbResourceDto.Locale locale, String ref) {
        Optional<DbResourceDto.Entry> actualUpdatedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);
        assertThat(actualUpdatedEntry).isEmpty();
    }
}