package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        generator.makePatch(DbDto.Topic.ACHIEVEMENTS, ReferenceRange.fromCliOption(Optional.empty()));

        // THEN: IAE
    }

    @Test
    public void makePatch_whenTopicFound_shouldSetTopicObject_andReturnPatchObject() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopic();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.ACHIEVEMENTS, ReferenceRange.fromCliOption(Optional.empty()));

        // THEN
        assertThat(generator.getTopicObject()).isNotNull();
        assertThat(actualPatchObject).isNotNull();
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andRefsAsEnumeration_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.CAR_PHYSICS_DATA, ReferenceRange.fromCliOption(Optional.of("606298799,606299799")));

        // THEN
        assertPatchGeneratedWithinRange(actualPatchObject);
    }

    @Test
    public void makePatch_whenUsingRealDatabase_andRefsAsBounds_shouldReturnCorrectPatchObjectWithExistingRefs() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDatabaseObjectsWithOneTopicFromRealFile();
        PatchGenerator generator = createPatchGenerator(databaseObjects);

        // WHEN
        DbPatchDto actualPatchObject = generator.makePatch(DbDto.Topic.CAR_PHYSICS_DATA, ReferenceRange.fromCliOption(Optional.of("606297799..606299799")));

        // THEN
        assertPatchGeneratedWithinRange(actualPatchObject);
    }

    private static PatchGenerator createPatchGenerator(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseObjects);
    }

    private static List<DbDto> createDatabaseObjectsWithOneTopic() {
        DbDto topicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                    .forTopic(DbDto.Topic.ACHIEVEMENTS)
                    .build())
                .build();

        return singletonList(topicObject);
    }

    private static  List<DbDto> createDatabaseObjectsWithOneTopicFromRealFile() throws IOException, URISyntaxException {
        DbDto topicObject = FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/TDU_CarPhysicsData.json");

        return singletonList(topicObject);
    }

    private static void assertPatchGeneratedWithinRange(DbPatchDto patchObject) {
        assertThat(patchObject).isNotNull();
        assertThat(patchObject.getChanges()).hasSize(129); //1 UPDATE + 128 UPDATE_RES (20 entries * 8 locales)

        DbPatchDto.DbChangeDto changeObject1 = patchObject.getChanges().get(0);
        assertThat(changeObject1.getType()).isEqualTo(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE);
        assertThat(changeObject1.getTopic()).isEqualTo(DbDto.Topic.CAR_PHYSICS_DATA);
        assertThat(changeObject1.getRef()).isEqualTo("606298799");
        assertThat(changeObject1.getValues()).hasSize(103);
        assertThat(changeObject1.getValues().get(0)).isEqualTo("606298799");
        assertThat(changeObject1.getValues().get(102)).isEqualTo("104");

        DbPatchDto.DbChangeDto changeObject2 = patchObject.getChanges().get(1);
        assertThat(changeObject2.getType()).isEqualTo(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES);
        assertThat(changeObject2.getTopic()).isEqualTo(DbDto.Topic.CAR_PHYSICS_DATA);
    }
}