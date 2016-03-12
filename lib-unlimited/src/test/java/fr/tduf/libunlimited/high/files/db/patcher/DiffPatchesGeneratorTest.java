package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static org.assertj.core.api.Assertions.assertThat;

public class DiffPatchesGeneratorTest {

    private final static Class<DiffPatchesGeneratorTest> thisClass = DiffPatchesGeneratorTest.class;

    @Before
    public void setUp() {
//        Log.set(Log.LEVEL_DEBUG);
    }

    @Test
    public void prepare_shouldReturnInstance() throws Exception {
        // GIVEN-WHEN
        DiffPatchesGenerator actualGenerator = DiffPatchesGenerator.prepare(new ArrayList<>(), new ArrayList<>());

        // THEN
        assertThat(actualGenerator).isNotNull();
        assertThat(actualGenerator.getDatabaseObjects()).isNotNull();
        assertThat(actualGenerator.getReferenceDatabaseObjects()).isNotNull();
        assertThat(actualGenerator.getDatabaseMiner()).isNotNull();
        assertThat(actualGenerator.getReferenceDatabaseMiner()).isNotNull();
    }

    @Test
    public void makePatches_whenNoDifference_shouldReturnEmptySet() throws Exception {
        // GIVEN
        List<DbDto> databaseObjects = readReferenceDatabase();
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(databaseObjects, databaseObjects);

        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();

        // THEN
        assertThat(actualPatchObjects).isEmpty();
    }

    @Test
    public void makePatches_whenNewContentsEntry_andREF_shouldAddFullUpdate() throws Exception {
        // GIVEN
        List<DbDto> referenceDatabaseObjects = readReferenceDatabase();
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/ref-newEntry/TDU_CarPhysicsData.json");
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(currentDatabaseObjects, referenceDatabaseObjects);


        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();


        // THEN
        assertThat(actualPatchObjects).hasSize(1);

        DbPatchDto actualPatchObject = actualPatchObjects.stream().findAny().get();
        assertThat(actualPatchObject.getComment()).isEqualTo(CAR_PHYSICS_DATA.name());
        assertThat(actualPatchObject.getChanges()).hasSize(1);

        DbPatchDto.DbChangeDto actualChangeObject = actualPatchObject.getChanges().get(0);
        assertThat(actualChangeObject.getType()).isEqualTo(UPDATE);
        assertThat(actualChangeObject.getRef()).isEqualTo("99999999");
        assertThat(actualChangeObject.getPartialValues()).isNull();
        final List<String> actualValues = actualChangeObject.getValues();
        assertThat(actualValues).hasSize(103);

        final DbDataDto.Entry addedEntry = generator.getDatabaseMiner().getContentEntryFromTopicWithReference("99999999", CAR_PHYSICS_DATA).get();
        for (int i = 0; i < actualValues.size() ; i++) {
            assertThat(actualValues.get(i)).isEqualTo(addedEntry.getItems().get(i).getRawValue());
        }
    }
    @Test
    public void makePatches_whenExistingContentsEntry_andREF_andChangedItem_shouldAddPartialUpdate() throws Exception {
        // GIVEN
        List<DbDto> referenceDatabaseObjects = readReferenceDatabase();
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/ref-existingEntry/TDU_CarPhysicsData.json");
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(currentDatabaseObjects, referenceDatabaseObjects);


        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();


        // THEN
        assertThat(actualPatchObjects).hasSize(1);

        DbPatchDto actualPatchObject = actualPatchObjects.stream().findAny().get();
        assertThat(actualPatchObject.getComment()).isEqualTo(CAR_PHYSICS_DATA.name());
        assertThat(actualPatchObject.getChanges()).hasSize(1);

        DbPatchDto.DbChangeDto actualChangeObject = actualPatchObject.getChanges().get(0);
        assertThat(actualChangeObject.getType()).isEqualTo(UPDATE);
        assertThat(actualChangeObject.getRef()).isEqualTo("1210773243");
        assertThat(actualChangeObject.getValues()).isNull();
        final List<DbFieldValueDto> actualPartialValues = actualChangeObject.getPartialValues();
        assertThat(actualPartialValues).hasSize(1);

        final DbFieldValueDto actualPartialValue = actualPartialValues.get(0);
        assertThat(actualPartialValue.getRank()).isEqualTo(103);
        assertThat(actualPartialValue.getValue()).isEqualTo("105");
    }

    @Test
    public void makePatches_whenNewContentsEntry_andNoREF_shouldAddFullUpdate() throws Exception {
        // GIVEN
        List<DbDto> referenceDatabaseObjects = readReferenceDatabase();
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/noref-newEntry/TDU_CarRims.json");
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(currentDatabaseObjects, referenceDatabaseObjects);


        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();


        // THEN
        assertThat(actualPatchObjects).hasSize(1);

        DbPatchDto actualPatchObject = actualPatchObjects.stream().findAny().get();
        assertThat(actualPatchObject.getComment()).isEqualTo(CAR_RIMS.name());
        assertThat(actualPatchObject.getChanges()).hasSize(1);

        DbPatchDto.DbChangeDto actualChangeObject = actualPatchObject.getChanges().get(0);
        assertThat(actualChangeObject.getType()).isEqualTo(UPDATE);
        assertThat(actualChangeObject.getRef()).isNull();
        assertThat(actualChangeObject.getPartialValues()).isNull();
        final List<String> actualValues = actualChangeObject.getValues();
        assertThat(actualValues)
                .hasSize(2)
                .containsExactly("700912892" , "1139916842");
    }

    private static List<DbDto> readReferenceDatabase() throws URISyntaxException {
        return readDatabase("/db/json/diff/TDU_CarPhysicsData.json");
    }

    private static List<DbDto> readDatabase(String jsonFile) throws URISyntaxException {
        String jsonDirectory = new File(thisClass.getResource(jsonFile).toURI()).getParent();
        return DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);
    }
}
