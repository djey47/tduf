package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.esotericsoftware.minlog.Log.LEVEL_INFO;
import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatchGeneratorTest {

    @BeforeAll
    static void setUp() {
        Log.set(LEVEL_INFO);
    }

    @Test
    void makePatch_whenNullArguments_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> generator.makePatch(null, null, null));
    }

    @Test
    void makePatch_whenTopicNotFound_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> generator.makePatch(ACHIEVEMENTS, createDefaultRange(), createDefaultRange()));
    }

    @Test
    void makePatch_whenTopicFound_shouldSetTopicObject_andReturnPatchObject() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopic();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, createDefaultRange(), createDefaultRange());

        // THEN
        assertThat(generator.getTopicObject()).isNotNull();
        assertThat(actualPatchObject).isNotNull();
    }

    @Test
    void makePatch_whenUsingRealDatabase_andEntryIdentifiersAsEnumeration_shouldReturnCorrectPatchObjectWithExistingEntries() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, ItemRange.fromCliOption("0,1"), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForOneTopicWithoutREFSupport(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andRefsAsEnumeration_shouldReturnCorrectPatchObjectWithExistingRefs() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ItemRange.fromCliOption("734,735"), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteResources_shouldReturnCorrectPatchObjectWithExistingRefs() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(HAIR, ItemRange.fromCliOption("54522"), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopics(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andUniqueRef_andAssociatedTopics_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(CAR_PHYSICS_DATA, ItemRange.fromCliOption("606298799"), createDefaultRange());

        // THEN
        assertPatchGeneratedForAssociatedTopics(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteContentsReference_shouldReturnCorrectPatchObjectWithExistingRefs_andOtherTopic() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, ItemRange.fromCliOption("540091906"), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopicsWithRemoteContentsReference(actualPatchObject);
    }
    
    @Test
    void makePatch_whenUsingRealDatabase_andRefsAsBounds_shouldReturnCorrectPatchObjectWithExistingRefs() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ItemRange.fromCliOption("0..735"), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andUniqueFieldRank_shouldReturnCorrectPatchObjectWithPartialChanges() throws ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption("72825");
        ItemRange fieldRange = ItemRange.fromCliOption("2");

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValue(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andFieldRanksAsBounds_shouldReturnCorrectPatchObjectWithPartialChanges() throws ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption("735");
        ItemRange fieldRange = ItemRange.fromCliOption("2..4");

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValues(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andFieldRanksAsEnumeration_shouldReturnCorrectPatchObjectWithPartialChanges() throws ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption("735");
        ItemRange fieldRange = ItemRange.fromCliOption("2,3,4");

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValues(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andSingleFieldRank_andNoREFSupport_shouldReturnCorrectPatchObjectWithPartialChanges() throws ReflectiveOperationException {
        // GIVEN
        ItemRange fieldRange = ItemRange.fromCliOption("1");

        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, ALL, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicNoRefSupportAndPartialValues(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andAllRefs_andSameResourceValuesForLocales_shouldReturnCorrectPatchObjectWithExistingRefs() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, createDefaultRange(), createDefaultRange());

        // THEN
        assertPatchGeneratedWithAllEntriesForOneTopic(actualPatchObject);
    }

    @Test
    void makePatch_whenUsingRealDatabase_andHugeTopic_andAllRefs_shouldNotGenerateSameInstructionTwice() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, createDefaultRange(), createDefaultRange());

        // THEN
        assertThat(actualPatchObject.getChanges()).hasSize(1346);
    }

    private static ItemRange createDefaultRange() {
        return ALL;
    }

    private static PatchGenerator createPatchGenerator(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseObjects);
    }

    private static List<DbDto> createDatabaseObjectsWithOneTopic() {
        DbDto topicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                    .forTopic(ACHIEVEMENTS)
                    .build())
                .withData(DbDataDto.builder().build())
                .build();

        return singletonList(topicObject);
    }

    private static List<DbDto> createDatabaseObjectsWithOneTopicFromRealFile() {
        return singletonList(DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.ACHIEVEMENTS));
    }

    private static  List<DbDto> createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles() {
        return asList(
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_PHYSICS_DATA),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BRANDS));
    }

    private static  List<DbDto> createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles() {
        return asList(
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_PHYSICS_DATA),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BRANDS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_RIMS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.RIMS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_COLORS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.INTERIOR),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_PACKS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.AFTER_MARKET_PACKS),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CAR_SHOPS));
    }

    private static  List<DbDto> createDatabaseObjectsWithFourLinkedTopicsFromRealFiles() {
        return asList(
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.CLOTHES),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.HAIR),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.PNJ),
                DatabaseHelper.createDatabaseTopicForReadOnly(DbDto.Topic.BRANDS));
    }

    private static void assertPatchGeneratedWithAllEntriesForOneTopic(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(1472); //74 UPDATE + 1398 UPDATE_RES

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getRef()).isEqualTo("53836935|2");
        assertThat(changeObject1.getValues()).hasSize(9);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("53836935");
        assertThat(changeObject1.getValues().get(1)).isEqualTo("2");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(74);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);

        assertThat(actualChanges).extracting("ref").contains("58136935", "56459455");
        assertThat(actualChanges).extracting("locale").contains(null, FRANCE, ITALY, UNITED_STATES, JAPAN, GERMANY, SPAIN, CHINA, KOREA);
        assertThat(actualChanges).extracting("topic").containsOnly(ACHIEVEMENTS);
        assertThat(actualChanges).extracting("value").contains(null, "COLLEZIONISTA ESTREMO");
    }

    private static void assertPatchGeneratedWithinRangeForOneTopicWithoutREFSupport(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(60); // 2 UPDATE + 58 UPDATE_RES

        assertThat(actualChanges).extracting("topic").containsOnly(ACHIEVEMENTS);

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getRef()).isEqualTo("55736935|5");
        assertThat(changeObject1.getValues()).hasSize(9);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("55736935");
        assertThat(changeObject1.getValues().get(1)).isEqualTo("5");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(2);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);
    }

    private static void assertPatchGeneratedWithinRangeForOneTopic(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(2); //1 UPDATE + 1 UPDATE_RES (1 local resource for any locale)

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(BRANDS);
        assertThat(changeObject1.getRef()).isEqualTo("735");
        assertThat(changeObject1.getValues()).hasSize(7);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("735");
        assertThat(changeObject1.getValues().get(6)).isEqualTo("1");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(1);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);
        assertThat(changeObject2.getTopic()).isEqualTo(BRANDS);

        assertThat(actualChanges).extracting("ref").containsAll(asList("735", "55338337"));
        assertThat(actualChanges).extracting("locale").containsExactly(null, DEFAULT);
        assertThat(actualChanges).extracting("value").contains(null, "AC");
    }

    private static void assertPatchGeneratedWithinRangeForOneTopicAndPartialValues(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();

        assertThat(actualChanges)
                .hasSize(2) //1 UPDATE(3 partial values) + 1 UPDATE_RES
                .extracting("ref").containsExactly("735", "55338337");
        assertThat(actualChanges).extracting("topic").containsOnly(BRANDS);
        assertThat(actualChanges).extracting("locale").containsNull();
        assertThat(actualChanges).extracting("values").containsNull();
        assertThat(actualChanges).extracting("value").contains(null, "AC");

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getPartialValues())
                .hasSize(3)
                .extracting("rank").containsExactly(2, 3, 4);
        assertThat(changeObject1.getPartialValues()).extracting("value").containsExactly("55338337", "55338337", "55338337");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(1);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);
        assertThat(changeObject2.getPartialValues()).isNull();
    }

    private static void assertPatchGeneratedWithinRangeForOneTopicNoRefSupportAndPartialValues(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();

        assertThat(actualChanges)
                .hasSize(110) //67 UPDATE(1 partial value) + 43 UPDATE_RES
                .extracting("ref").doesNotContainNull();
        assertThat(actualChanges).extracting("topic").containsOnly(ACHIEVEMENTS);
        assertThat(actualChanges).extracting("type").containsOnly(UPDATE, UPDATE_RES);
        assertThat(actualChanges).extracting("locale").containsNull();
        assertThat(actualChanges).extracting("values").containsOnly((String) null);
        assertThat(actualChanges).extracting("value").containsNull();
        assertThat(actualChanges).extracting("partialValues").containsNull();

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getPartialValues())
                .hasSize(1)
                .extracting("rank").containsExactly(1);
        assertThat(changeObject1.getPartialValues()).extracting("value").containsExactly("53836935");
    }

    private static void assertPatchGeneratedWithinRangeForOneTopicAndPartialValue(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();

        assertThat(actualChanges)
                .hasSize(2) //1 UPDATE(1 partial value) + 1 UPDATE_RES
                .extracting("ref").containsExactly("72825", "56338337");
        assertThat(actualChanges).extracting("topic").containsOnly(BRANDS);
        assertThat(actualChanges).extracting("locale").containsExactly(null, DEFAULT);
        assertThat(actualChanges).extracting("values").containsOnly((Object)null);
        assertThat(actualChanges).extracting("value").containsExactly(null, "ALFA");

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getPartialValues())
                .hasSize(1)
                .extracting("rank").containsExactly(2);
        assertThat(changeObject1.getPartialValues()).extracting("value").containsExactly("56338337");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(1);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);
        assertThat(changeObject2.getPartialValues()).isNull();
    }

    private static void assertPatchGeneratedWithinRangeForLinkedTopics(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(7); //1 UPDATE + 6 UPDATE_RES ( 5 local resources + 1 remote resource, any locale)

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(1);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(HAIR);
        assertThat(changeObject1.getRef()).isEqualTo("54522");
        assertThat(changeObject1.getValues()).hasSize(7);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("54522");
        assertThat(changeObject1.getValues().get(6)).isEqualTo("54713527");

        assertThat(actualChanges).extracting("type").contains(UPDATE, UPDATE_RES);
        assertThat(actualChanges).extracting("topic").contains(CLOTHES, HAIR);
        assertThat(actualChanges).extracting("locale").containsExactly(DEFAULT, null, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT);
        assertThat(actualChanges).extracting("value").contains("Mixte", null, "Rasta", "HA01", "DEFAULT_BOTTOMS", "DEFAULT_TOPS_SHIRT", "ChemiseAFleurs01");
    }

    private static void assertPatchGeneratedWithinRangeForLinkedTopicsWithRemoteContentsReference(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(91); //2 UPDATE + 89 UPDATE_RES

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(81);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(PNJ);
        assertThat(changeObject1.getRef()).isEqualTo("540091906");
        assertThat(changeObject1.getValues()).hasSize(17);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("540091906");

        assertThat(actualChanges).extracting("type").contains(UPDATE, UPDATE_RES);
        assertThat(actualChanges).extracting("topic").contains(BRANDS, CLOTHES, PNJ);
    }

    private static void assertPatchGeneratedForAssociatedTopics(DbPatchDto patchObject) throws IOException {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(96);
        // 13 UPDATE (1 CAR_PHYSICS, 1 BRANDS, 1 CAR_RIMS, 1 RIMS, 4 CAR_COLORS, 1 CAR_PACKS, 3 AFTERMARKET_PACKS, 1 INTERIOR)
        // 83 UPDATE_RES (60 CAR_PHYSICS, 1 BRANDS, 4 RIMS, 9 CAR_COLORS, 6 AFTERMARKET_PACKS, 2 INTERIOR, 1 CAR SHOPS)

        String jsonPatch = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(patchObject);
        Log.debug(PatchGeneratorTest.class.getSimpleName(), jsonPatch);
    }
}
