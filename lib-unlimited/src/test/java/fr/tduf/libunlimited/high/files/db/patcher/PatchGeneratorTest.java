package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.esotericsoftware.minlog.Log.LEVEL_INFO;
import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PatchGeneratorTest {

    @Before
    public void setUp() {
        Log.set(LEVEL_INFO);
    }

    @After
    public void tearDown() {}

    @Test(expected = NullPointerException.class)
    public void makePatch_whenNullArguments_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN
        generator.makePatch(null, null, null);

        // THEN: NPE
    }

    @Test(expected = IllegalArgumentException.class)
    public void makePatch_whenTopicNotFound_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN
        generator.makePatch(ACHIEVEMENTS, createDefaultRange(), createDefaultRange());

        // THEN: IAE
    }

    @Test
    public void makePatch_whenTopicFound_shouldSetTopicObject_andReturnPatchObject() throws ReflectiveOperationException {
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
    public void makePatch_whenUsingRealDatabase_andRefsAsEnumeration_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ItemRange.fromCliOption(Optional.of("734,735")), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteResources_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(HAIR, ItemRange.fromCliOption(Optional.of("54522")), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopics(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andAssociatedTopics_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(CAR_PHYSICS_DATA, ItemRange.fromCliOption(Optional.of("606298799")), createDefaultRange());

        // THEN
        assertPatchGeneratedForAssociatedTopics(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteContentsReference_shouldReturnCorrectPatchObjectWithExistingRefs_andOtherTopic() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, ItemRange.fromCliOption(Optional.of("540091906")), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopicsWithRemoteContentsReference(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andRefsAsBounds_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ItemRange.fromCliOption(Optional.of("0..735")), createDefaultRange());

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueFieldRank_shouldReturnCorrectPatchObjectWithPartialChanges() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption(Optional.of("72825"));
        ItemRange fieldRange = ItemRange.fromCliOption(Optional.of("2"));

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValue(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andFieldRanksAsBounds_shouldReturnCorrectPatchObjectWithPartialChanges() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption(Optional.of("735"));
        ItemRange fieldRange = ItemRange.fromCliOption(Optional.of("2..4"));

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValues(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andFieldRanksAsEnumeration_shouldReturnCorrectPatchObjectWithPartialChanges() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        ItemRange refRange = ItemRange.fromCliOption(Optional.of("735"));
        ItemRange fieldRange = ItemRange.fromCliOption(Optional.of("2,3,4"));

        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);


        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, refRange, fieldRange);


        // THEN
        assertPatchGeneratedWithinRangeForOneTopicAndPartialValues(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andAllRefs_andSameResourceValuesForLocales_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, createDefaultRange(), createDefaultRange());

        // THEN
        assertPatchGeneratedWithAllEntriesForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andHugeTopic_andAllRefs_shouldNotGenerateSameInstructionTwice() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, ItemRange.fromCliOption(Optional.empty()), createDefaultRange());

        // THEN
        assertThat(actualPatchObject.getChanges()).hasSize(1346);
    }

    private static ItemRange createDefaultRange() {
        return ItemRange.ALL;
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

    private static List<DbDto> createDatabaseObjectsWithOneTopicFromRealFile() throws IOException, URISyntaxException {
        return singletonList(readAchievements());
    }

    private static  List<DbDto> createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles() throws IOException, URISyntaxException {
        return asList(readCarPhysicsData(), readBrands());
    }

    private static  List<DbDto> createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles() throws IOException, URISyntaxException {
        return asList(
                readCarPhysicsData(),
                readBrands(),
                readCarRims(),
                readRims(),
                readCarColors(),
                readInterior(),
                readCarPacks(),
                readAfterMarketPacks(),
                readCarShops());
    }

    private static  List<DbDto> createDatabaseObjectsWithFourLinkedTopicsFromRealFiles() throws IOException, URISyntaxException {
        return asList(
                readClothes(),
                readHair(),
                readPNJ(),
                readBrands());
    }

    private static DbDto readClothes() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Clothes.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Clothes.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Clothes.structure.json"))
                .build();
    }

    private static DbDto readBrands() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Brands.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Brands.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Brands.structure.json"))
                .build();
    }

    private static DbDto readHair() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Hair.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Hair.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Hair.structure.json"))
                .build();
    }

    private static DbDto readPNJ() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_PNJ.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_PNJ.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_PNJ.structure.json"))
                .build();
    }

    private static DbDto readCarPhysicsData() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_CarPhysicsData.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_CarPhysicsData.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_CarPhysicsData.structure.json"))
                .build();
    }

    private static DbDto readRims() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Rims.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Rims.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Rims.structure.json"))
                .build();
    }

    private static DbDto readCarRims() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_CarRims.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_CarRims.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_CarRims.structure.json"))
                .build();
    }

    private static DbDto readAchievements() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Achievements.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Achievements.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Achievements.structure.json"))
                .build();
    }

    private static DbDto readCarColors() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_CarColors.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_CarColors.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_CarColors.structure.json"))
                .build();
    }

    private static DbDto readInterior() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_Interior.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_Interior.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_Interior.structure.json"))
                .build();
    }

    private static DbDto readCarPacks() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_CarPacks.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_CarPacks.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_CarPacks.structure.json"))
                .build();
    }

    private static DbDto readAfterMarketPacks() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_AfterMarketPacks.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_AfterMarketPacks.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_AfterMarketPacks.structure.json"))
                .build();
    }

    private static DbDto readCarShops() throws URISyntaxException, IOException {
        return DbDto.builder()
                .withData(FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/TDU_CarShops.data.json"))
                .withResource(FilesHelper.readObjectFromJsonResourceFile(DbResourceDto.class, "/db/json/TDU_CarShops.resources.json"))
                .withStructure(FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/TDU_CarShops.structure.json"))
                .build();
    }

    private static void assertPatchGeneratedWithAllEntriesForOneTopic(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(1472); //74 UPDATE + 1398 UPDATE_RES

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getRef()).isNull();
        assertThat(changeObject1.getValues()).hasSize(9);

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(74);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);

        assertThat(actualChanges).extracting("ref").contains("58136935", "56459455");
        assertThat(actualChanges).extracting("locale").contains(null, FRANCE, ITALY, UNITED_STATES, JAPAN, GERMANY, SPAIN, CHINA, KOREA);
        assertThat(actualChanges).extracting("topic").containsOnly(ACHIEVEMENTS);
        assertThat(actualChanges).extracting("value").contains(null, "COLLEZIONISTA ESTREMO");
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
        assertThat(actualChanges).extracting("locale").containsOnly(new Object[]{null});
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

    private static void assertPatchGeneratedWithinRangeForOneTopicAndPartialValue(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();

        assertThat(actualChanges)
                .hasSize(2) //1 UPDATE(1 partial value) + 1 UPDATE_RES
                .extracting("ref").containsExactly("72825", "56338337");
        assertThat(actualChanges).extracting("topic").containsOnly(BRANDS);
        assertThat(actualChanges).extracting("locale").containsNull();
        assertThat(actualChanges).extracting("values").containsNull();
        assertThat(actualChanges).extracting("value").contains(null, "ALFA");

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
        assertThat(actualChanges).extracting("locale").containsOnly(new Object[]{null});
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
