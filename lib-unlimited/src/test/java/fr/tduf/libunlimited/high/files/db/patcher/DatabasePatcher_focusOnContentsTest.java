package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static fr.tduf.libunlimited.common.helper.FilesHelper.readObjectFromJsonResourceFile;
import static fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher_commonTest.createPatcher;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class DatabasePatcher_focusOnContentsTest {

    @Test
    void apply_whenUpdateContentsPatch_forAllFields_andIncorrectValueCount_shouldThrowException() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-badCount.mini.json");
        DatabasePatcher patcher = createPatcher(singletonList(DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BOTS)));

        // WHEN-THEN
        assertThrows( IllegalArgumentException.class,
                () -> patcher.apply(updateContentsPatch));
    }

    @Test
    void apply_whenUpdateContentsPatch_forAllFields_shouldAddNewEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(DbDto.Topic.BOTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 1;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 1);

        ContentEntryDto actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(8);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("57167257");
    }

    @Test
    void batchApply_whenTwoUpdateContentsPatches_shouldAddEntries() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN
        DbPatchDto updateContentsPatch1 = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef.mini.json");
        DbPatchDto updateContentsPatch2 = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef-2.mini.json");
        List<DbPatchDto> updateContentsPatches = asList(updateContentsPatch1, updateContentsPatch2);
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(DbDto.Topic.BOTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.batchApply(updateContentsPatches);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 2;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 2);

        ContentEntryDto actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(8);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("57167257");

        actualEntryIndex++;
        actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(8);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("57167258");
    }

    @Test
    void apply_whenUpdateContentsPatch_forAllFields_andSameEntryExists_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef-existing.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(DbDto.Topic.BOTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);
        assertThat(topicEntries.get(0).getItems()).extracting("rawValue").containsExactly("55467256", "54373256", "540091912", "1", "0", "1178042409", "0", "0.5");
    }

    @Test
    void apply_whenUpdateContentsPatch_forAllFields_withRefSupport_shouldAddNewEntryAndUpdateExisting() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-ref.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 1;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 1);

        ContentEntryDto actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(103);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("1221657049");

        ContentEntryDto actualUpdatedEntry = databaseMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA).get();
        assertThat(actualUpdatedEntry.getId()).isEqualTo(0);

        assertThat(actualUpdatedEntry.getItems()).hasSize(103);
        assertThat(actualUpdatedEntry.getItems().get(1).getRawValue()).isEqualTo("864426");
    }

    @Test
    void apply_whenUpdateContentsPatch_forAllFields_withRefSupport_andStrictMode_shouldOnlyAddNewEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-ref-strict.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        int actualEntryIndex = actualEntryCount - 1;
        assertThat(actualEntryCount).isEqualTo(previousEntryCount + 1);

        ContentEntryDto actualCreatedEntry = topicEntries.get(actualEntryIndex);
        assertThat(actualCreatedEntry.getId()).isEqualTo(actualEntryIndex);

        assertThat(actualCreatedEntry.getItems()).hasSize(103);
        assertThat(actualCreatedEntry.getItems().get(0).getRawValue()).isEqualTo("1221657049");

        ContentEntryDto actualUpdatedEntry = databaseMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA).get();
        assertThat(actualUpdatedEntry.getId()).isEqualTo(0);

        assertThat(actualUpdatedEntry.getItems()).hasSize(103);
        assertThat(actualUpdatedEntry.getItems().get(1).getRawValue()).isEqualTo("735");
    }

    @Test
    void apply_whenUpdateContentsPatch_withAssociationEntries_shouldCreateThem() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-assoc.mini.json");

        List<DbDto> databaseObjects = DatabaseHelper.createDatabase();
        DatabasePatcher patcher = createPatcher(databaseObjects);

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(databaseObjects);
        List<ContentEntryDto> carRimsTopicEntries = databaseMiner.getDatabaseTopic(CAR_RIMS).get().getData().getEntries();
        int carRimsPreviousEntryCount = carRimsTopicEntries.size();
        List<ContentEntryDto> carColorsTopicEntries = databaseMiner.getDatabaseTopic(CAR_COLORS).get().getData().getEntries();
        int carColorsPreviousEntryCount = carColorsTopicEntries.size();
        List<ContentEntryDto> carPacksTopicEntries = databaseMiner.getDatabaseTopic(CAR_PACKS).get().getData().getEntries();
        int carPacksPreviousEntryCount = carPacksTopicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        assertThat(carColorsTopicEntries).hasSize(carColorsPreviousEntryCount + 4);
        assertThat(carRimsTopicEntries).hasSize(carRimsPreviousEntryCount + 1);
        assertThat(carPacksTopicEntries).hasSize(carPacksPreviousEntryCount + 1);
    }

    @Test
    void apply_whenUpdateContentsPatch_andBitfield_shouldUpdateBitfield() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-mixed-bitfield.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_SHOPS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        ContentEntryDto actualModifiedEntry = databaseMiner.getContentEntryFromTopicWithReference("589356824", CAR_SHOPS).get();
        assertThat(actualModifiedEntry.getItems().get(18).getSwitchValues())
                .isNotNull()
                .isNotEmpty();

        ContentEntryDto actualCreatedEntry = databaseMiner.getContentEntryFromTopicWithReference("00000000", CAR_SHOPS).get();
        assertThat(actualCreatedEntry.getItems().get(18).getSwitchValues())
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void apply_whenUpdateContentsPatch_forOneItem_andREFDoesNotExist_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-newRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);
    }

    @Test
    void apply_whenUpdateContentsPatch_forOneItem_andREFExists_butInvalidRank_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-badRank-existingRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
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
    void apply_whenUpdateContentsPatch_forNoItem_andREFExists_shouldIgnoreIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-noValues-existingRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
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
    void apply_whenUpdateContentsPatch_forOneItem_andREFExists_shouldChangeIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-existingRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();
        List<String> previousValues = extractAllItemsFromEntryExcepted(databaseMiner, "1139121456", CAR_PHYSICS_DATA, 103);


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        List<String> actualValues = extractAllItemsFromEntryExcepted(databaseMiner, "1139121456", CAR_PHYSICS_DATA, 103);
        assertThat(actualValues).isEqualTo(previousValues);

        String actualItemValue = databaseMiner
                .getContentEntryFromTopicWithReference("1139121456", CAR_PHYSICS_DATA).get()
                .getItems()
                .get(102)
                .getRawValue();
        assertThat(actualItemValue).isEqualTo("7954");
    }

    @Test
    void apply_whenUpdateContentsPatch_forOneItem_andPseudoREFExists_shouldChangeIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-existingPseudoRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_COLORS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(CAR_COLORS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();

        String pseudoReference = "632098801|57376127";
        List<String> previousValues = extractAllItemsFromEntryExcepted(databaseMiner, pseudoReference, CAR_COLORS, 6);


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        List<String> actualValues = extractAllItemsFromEntryExcepted(databaseMiner, pseudoReference, CAR_COLORS, 6);
        assertThat(actualValues).isEqualTo(previousValues);

        String actualItemValue = databaseMiner
                .getContentEntryFromTopicWithReference(pseudoReference, CAR_COLORS).get()
                .getItems()
                .get(5)
                .getRawValue();
        assertThat(actualItemValue).isEqualTo("5000");
    }

    @Test
    void apply_whenUpdateContentsPatch_forOneItem_andFilterWithOneCondition_shouldChangeThem() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-filter.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.ACHIEVEMENTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(ACHIEVEMENTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        assertThat(databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "55736935"), ACHIEVEMENTS)

                .filter((entry) -> "5".equals(entry.getItemAtRank(2).get().getRawValue()))

                .count()).isEqualTo(5);
    }

    @Test
    void apply_whenUpdateContentsPatch_forOneItem_andFilterWithTwoConditions_shouldChangeIt() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updatePartialContents-filter2.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.ACHIEVEMENTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> topicEntries = databaseMiner.getDatabaseTopic(ACHIEVEMENTS).get().getData().getEntries();
        int previousEntryCount = topicEntries.size();


        // WHEN
        patcher.apply(updateContentsPatch);


        // THEN
        int actualEntryCount = topicEntries.size();
        assertThat(actualEntryCount).isEqualTo(previousEntryCount);

        assertThat(databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "55736935"), ACHIEVEMENTS)

                .filter((entry) -> "5".equals(entry.getItemAtRank(2).get().getRawValue()))

                .count()).isEqualTo(2);
    }

    @Test
    void apply_whenDeleteContentsPatch_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteContents-ref.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteContentsPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getContentEntryFromTopicWithReference("606298799", CAR_PHYSICS_DATA)).isEmpty();
    }

    @Test
    void apply_whenDeleteContentsPatch_andPseudoREF_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteContents-pseudoRef.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_COLORS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteContentsPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getContentEntryFromTopicWithReference("632098801|57376127", CAR_COLORS)).isEmpty();
    }

    @Test
    void apply_whenDeleteContentsPatch_andFilterWithOneCondition_shouldRemoveExistingEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteContents-filter.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.ACHIEVEMENTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteContentsPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "55736935"), ACHIEVEMENTS).findAny().isPresent()).isFalse();
    }

    @Test
    void apply_whenDeleteContentsPatch_andFilterWithTwoConditions_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteContentsPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteContents-filter2.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.ACHIEVEMENTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteContentsPatch);

        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> actualEntries = databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "55736935"), ACHIEVEMENTS)
                .collect(toList());
        assertThat(actualEntries)
                .hasSize(4);
        assertThat(actualEntries.stream()

                .filter((entry) -> "5".equals(entry.getItems().get(1).getRawValue()))

                .findAny()).isEmpty();
    }

    @Test
    void apply_whenMovePatch_andUpDirection_shouldMoveExistingEntryOnePosition() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto movePatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/moveContents-up-default.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_COLORS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(movePatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> actualEntries = databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "632098801"), CAR_COLORS)
                .collect(toList());
        assertThat(actualEntries)
                .hasSize(8);

        assertAllEntriesHaveFirstItem(actualEntries, "632098801");

        assertEntriesHaveSecondItems(actualEntries,
                "63166127", "57376127", "55556127", "58456127", "54776127", "54966127", "55466127", "61076127");
    }

    @Test
    void apply_whenMovePatch_andUpDirection_andTwoSteps_shouldMoveExistingEntryTwoPositions() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto movePatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/moveContents-up-2steps.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_COLORS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(movePatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> actualEntries = databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "632098801"), CAR_COLORS)
                .collect(toList());
        assertThat(actualEntries)
                .hasSize(8);

        assertAllEntriesHaveFirstItem(actualEntries, "632098801");

        assertEntriesHaveSecondItems(actualEntries,
                "55556127", "57376127", "63166127", "58456127", "54776127", "54966127", "55466127", "61076127");
    }

    @Test
    void apply_whenMovePatch_andDownDirection_andOneStep_shouldMoveExistingEntryOnePosition() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto movePatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/moveContents-down-1step.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_COLORS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(movePatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));

        List<ContentEntryDto> actualEntries = databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, "632098801"), CAR_COLORS)
                .collect(toList());
        assertThat(actualEntries)
                .hasSize(8);

        assertAllEntriesHaveFirstItem(actualEntries, "632098801");

        assertEntriesHaveSecondItems(actualEntries,
                "63166127", "57376127", "55556127", "58456127", "54776127", "54966127", "55466127", "61076127");
    }

    private static int getEntryHashCode(BulkDatabaseMiner databaseMiner, String ref, DbDto.Topic topic) {
        return databaseMiner
                .getContentEntryFromTopicWithReference(ref, topic)
                .get().hashCode();
    }

    private static List<String> extractAllItemsFromEntryExcepted(BulkDatabaseMiner databaseMiner, String ref, DbDto.Topic topic, int rejectedFieldRank) {
        return databaseMiner
                .getContentEntryFromTopicWithReference(ref, topic).get()
                .getItems().stream()
                .filter((item) -> item.getFieldRank() != rejectedFieldRank)
                .map(ContentItemDto::getRawValue)
                .collect(toList());
    }

    private static void assertAllEntriesHaveFirstItem(List<ContentEntryDto> actualEntries, String firstItemValue) {
        assertThat(actualEntries.stream()
                .map ( (entry) -> entry.getItems().get(0).getRawValue() )
                .collect(toList()))
                .containsOnly(firstItemValue);
    }

    private static void assertEntriesHaveSecondItems(List<ContentEntryDto> actualEntries, String... secondItemValues) {
        assertThat(actualEntries.stream()
                .sorted( (entry1, entry2) -> Integer.valueOf(entry1.getId()).compareTo(entry2.getId()))
                .map ( (entry) -> entry.getItems().get(1).getRawValue() )
                .collect(toList()))
                .containsExactly(secondItemValues);
    }
}
