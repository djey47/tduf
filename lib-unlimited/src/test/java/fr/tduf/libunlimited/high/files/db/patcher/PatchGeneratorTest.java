package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
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
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PatchGeneratorTest {

    @Before
    public void setUp() {
        Log.set(LEVEL_INFO);
    }

    @After
    public void tearDown() {
        BulkDatabaseMiner.clearAllCaches();
    }

    @Test(expected = NullPointerException.class)
    public void makePatch_whenNullArguments_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN
        generator.makePatch(null, null);

        // THEN: NPE
    }

    @Test(expected = IllegalArgumentException.class)
    public void makePatch_whenTopicNotFound_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN
        PatchGenerator generator = createPatchGenerator(new ArrayList<>());

        // WHEN
        generator.makePatch(ACHIEVEMENTS, ReferenceRange.fromCliOption(Optional.empty()));

        // THEN: IAE
    }

    @Test
    public void makePatch_whenTopicFound_shouldSetTopicObject_andReturnPatchObject() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopic();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, ReferenceRange.fromCliOption(Optional.empty()));

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
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ReferenceRange.fromCliOption(Optional.of("734,735")));

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteResources_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(HAIR, ReferenceRange.fromCliOption(Optional.of("54522")));

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopics(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andAssociatedTopics_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(CAR_PHYSICS_DATA, ReferenceRange.fromCliOption(Optional.of("606298799")));

        // THEN
        assertPatchGeneratedForAssociatedTopics(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteContentsReference_shouldReturnCorrectPatchObjectWithExistingRefs_andOtherTopic() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, ReferenceRange.fromCliOption(Optional.of("540091906")));

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopicsWithRemoteContentsReference(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andRefsAsBounds_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(BRANDS, ReferenceRange.fromCliOption(Optional.of("0..735")));

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andAllRefs_andSameResourceValuesForLocales_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(ACHIEVEMENTS, ReferenceRange.fromCliOption(Optional.empty()));

        // THEN
        assertPatchGeneratedWithAllEntriesForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andHugeTopic_andAllRefs_shouldNotGenerateSameInstructionTwice() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithFourLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(PNJ, ReferenceRange.fromCliOption(Optional.<String>empty()));

        // THEN
        assertThat(actualPatchObject.getChanges()).hasSize(1346);
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
        DbDto topicObject = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Achievements.json");

        return singletonList(topicObject);
    }

    private static  List<DbDto> createDatabaseObjectsWithTwoLinkedTopicsFromRealFiles() throws IOException, URISyntaxException {
        DbDto topicObject1 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarPhysicsData.json");
        DbDto topicObject2 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Brands.json");

        return asList(topicObject1, topicObject2);
    }

    private static  List<DbDto> createDatabaseObjectsWithCarPhysicsAssociatedTopicsFromRealFiles() throws IOException, URISyntaxException {
        DbDto topicObject1 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarPhysicsData.json");
        DbDto topicObject2 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Brands.json");
        DbDto topicObject3 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarRims.json");
        DbDto topicObject4 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Rims.json");
        DbDto topicObject5 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarColors.json");
        DbDto topicObject6 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Interior.json");
        DbDto topicObject7 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarPacks.json");
        DbDto topicObject8 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_AfterMarketPacks.json");
        DbDto topicObject9 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarShops.json");

        return asList(topicObject1, topicObject2, topicObject3, topicObject4, topicObject5, topicObject6, topicObject7, topicObject8, topicObject9);
    }

    private static  List<DbDto> createDatabaseObjectsWithFourLinkedTopicsFromRealFiles() throws IOException, URISyntaxException {
        DbDto topicObject1 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Clothes.json");
        DbDto topicObject2 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Hair.json");
        DbDto topicObject3 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_PNJ.json");
        DbDto topicObject4 = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_Brands.json");

        return asList(topicObject1, topicObject2, topicObject3, topicObject4);
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
        assertThat(actualChanges).hasSize(84);
        // 9 UPDATE (1 CAR_PHYSICS, 1 BRANDS, 1 CAR_RIMS, 1 RIMS, 4 CAR_COLORS, 1 CAR_PACKS)
        // 75 UPDATE_RES (60 CAR_PHYSICS, 1 BRANDS, 5 RIMS, 9 CAR_COLORS)

        String jsonPatch = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(patchObject);
        Log.debug(PatchGeneratorTest.class.getSimpleName(), jsonPatch);
    }
}
