package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PatchGeneratorTest {

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
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.BRANDS, ReferenceRange.fromCliOption(Optional.of("734,735")));

        // THEN
        assertPatchGeneratedWithinRangeForOneTopic(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteResources_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithThreeLinkedTopicsFromRealFiles();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.HAIR, ReferenceRange.fromCliOption(Optional.of("54522")));

        // THEN
        assertPatchGeneratedWithinRangeForLinkedTopics(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andUniqueRef_andRemoteContentsReference_shouldReturnCorrectPatchObjectWithExistingRefs_andOtherTopic() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithThreeLinkedTopicsFromRealFiles();
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
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.BRANDS, ReferenceRange.fromCliOption(Optional.of("0..735")));

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

    private static  List<DbDto> createDatabaseObjectsWithThreeLinkedTopicsFromRealFiles() throws IOException, URISyntaxException {
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
        assertThat(changeObject1.getTopic()).isEqualTo(DbDto.Topic.BRANDS);
        assertThat(changeObject1.getRef()).isEqualTo("735");
        assertThat(changeObject1.getValues()).hasSize(7);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("735");
        assertThat(changeObject1.getValues().get(6)).isEqualTo("1");

        DbPatchDto.DbChangeDto changeObject2 = actualChanges.get(1);
        assertThat(changeObject2.getType()).isEqualTo(UPDATE_RES);
        assertThat(changeObject2.getTopic()).isEqualTo(DbDto.Topic.BRANDS);

        assertThat(actualChanges).extracting("ref").containsAll(asList("735", "55338337"));
        assertThat(actualChanges).extracting("locale").containsOnly(new Object[]{null});
        assertThat(actualChanges).extracting("value").contains(null, "AC");
    }

    private static void assertPatchGeneratedWithinRangeForLinkedTopics(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(7); //1 UPDATE + 6 UPDATE_RES ( 5 local resources + 1 remote resource, any locale)

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(DbDto.Topic.HAIR);
        assertThat(changeObject1.getRef()).isEqualTo("54522");
        assertThat(changeObject1.getValues()).hasSize(7);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("54522");
        assertThat(changeObject1.getValues().get(6)).isEqualTo("54713527");

        assertThat(actualChanges).extracting("type").contains(UPDATE_RES);
        assertThat(actualChanges).extracting("topic").contains(DbDto.Topic.CLOTHES);
        assertThat(actualChanges).extracting("locale").containsOnly(new Object[]{null});
        assertThat(actualChanges).extracting("value").contains(null, "HA01", "DEFAULT_TOPS_SHIRT", "Rasta", "Mixte", "DEFAULT_BOTTOMS", "ChemiseAFleurs01");
    }

    private static void assertPatchGeneratedWithinRangeForLinkedTopicsWithRemoteContentsReference(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();

        List<DbPatchDto.DbChangeDto> actualChanges = patchObject.getChanges();
        assertThat(actualChanges).hasSize(91); //2 UPDATE + 89 UPDATE_RES

        DbPatchDto.DbChangeDto changeObject1 = actualChanges.get(0);
        assertThat(changeObject1.getType()).isEqualTo(UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(PNJ);
        assertThat(changeObject1.getRef()).isEqualTo("540091906");
        assertThat(changeObject1.getValues()).hasSize(17);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("540091906");

        assertThat(actualChanges).extracting("type").contains(UPDATE_RES);
        assertThat(actualChanges).extracting("topic").contains(PNJ, CLOTHES, BRANDS);
    }
}