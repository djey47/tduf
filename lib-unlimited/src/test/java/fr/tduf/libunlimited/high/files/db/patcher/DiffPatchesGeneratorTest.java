package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class DiffPatchesGeneratorTest {

    private final static Class<DiffPatchesGeneratorTest> thisClass = DiffPatchesGeneratorTest.class;

    private final static List<DbDto> referenceDatabaseObjects = readReferenceDatabase();

    @BeforeAll
    static void setUp() {
//        Log.set(Log.LEVEL_DEBUG);
    }

    @Test
    void prepare_shouldReturnInstance() {
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
    void makePatches_whenNoDifference_shouldReturnEmptySet() {
        // GIVEN
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(referenceDatabaseObjects, referenceDatabaseObjects);

        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();

        // THEN
        assertThat(actualPatchObjects).isEmpty();
    }

    @Test
    void makePatches_whenNewContentsEntry_andREF_shouldAddFullUpdate_andStrictMode() throws Exception {
        // GIVEN
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/ref-newEntry/TDU_CarPhysicsData.data.json");
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
        assertThat(actualChangeObject.isStrictMode()).isTrue();
        assertThat(actualChangeObject.getRef()).isEqualTo("99999999");
        assertThat(actualChangeObject.getPartialValues()).isNull();
        assertThat(actualChangeObject.getTopic()).isEqualTo(CAR_PHYSICS_DATA);

        final List<String> actualValues = actualChangeObject.getValues();
        assertThat(actualValues).hasSize(103);

        final ContentEntryDto addedEntry = generator.getDatabaseMiner().getContentEntryFromTopicWithReference("99999999", CAR_PHYSICS_DATA).get();
        for (int i = 0; i < actualValues.size() ; i++) {
            assertThat(actualValues.get(i)).isEqualTo(addedEntry.getItems().get(i).getRawValue());
        }
    }

    @Test
    void makePatches_whenExistingContentsEntry_andREF_andChangedItem_shouldAddPartialUpdate() throws Exception {
        // GIVEN
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/ref-existingEntry/TDU_CarPhysicsData.data.json");
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
        assertThat(actualChangeObject.isStrictMode()).isFalse();
        assertThat(actualChangeObject.getRef()).isEqualTo("1210773243");
        assertThat(actualChangeObject.getValues()).isNull();
        assertThat(actualChangeObject.getTopic()).isEqualTo(CAR_PHYSICS_DATA);

        final List<DbFieldValueDto> actualPartialValues = actualChangeObject.getPartialValues();
        assertThat(actualPartialValues).hasSize(1);

        final DbFieldValueDto actualPartialValue = actualPartialValues.get(0);
        assertThat(actualPartialValue.getRank()).isEqualTo(103);
        assertThat(actualPartialValue.getValue()).isEqualTo("105");
    }

    @Test
    void makePatches_whenNewContentsEntry_andNoREF_shouldAddFullUpdate() throws Exception {
        // GIVEN
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/noref-newEntry/TDU_CarRims.data.json");
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
        assertThat(actualChangeObject.isStrictMode()).isFalse();
        assertThat(actualChangeObject.getRef()).isNull();
        assertThat(actualChangeObject.getPartialValues()).isNull();
        assertThat(actualChangeObject.getTopic()).isEqualTo(CAR_RIMS);

        final List<String> actualValues = actualChangeObject.getValues();
        assertThat(actualValues)
                .hasSize(2)
                .containsExactly("700912892" , "1139916842");
    }

    @Test
    void makePatches_whenNewResourceEntries_shouldAddFullResourceUpdates_andStrictMode() throws Exception {
        // GIVEN
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/newResource/TDU_Hair.data.json");
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(currentDatabaseObjects, referenceDatabaseObjects);


        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();


        // THEN
        assertThat(actualPatchObjects).hasSize(1);

        DbPatchDto actualPatchObject = actualPatchObjects.stream().findAny().get();
        assertThat(actualPatchObject.getComment()).isEqualTo(HAIR.name());

        List<DbPatchDto.DbChangeDto> actualChanges = actualPatchObject.getChanges();
        assertThat(actualChanges)
                .hasSize(2) // 1 global, 1 local (same value for all)
                .extracting("type").containsOnly(UPDATE_RES);
        assertThat(actualChanges).extracting("strictMode").containsOnly(true);
        assertThat(actualChanges).extracting("ref").containsOnly("54713528", "54713529");
        assertThat(actualChanges).extracting("topic").containsOnly(HAIR);
        assertThat(actualChanges).extracting("value").contains("StringPanthere01", "CulottePetitBateau01");
        assertThat(actualChanges).extracting("locale").containsOnly(DEFAULT);
    }

    @Test
    void makePatches_whenNewResourceEntry_withLocalizedValues_shouldAddFullResourceUpdates_andStrictMode() throws Exception {
        // GIVEN
        List<DbDto> currentDatabaseObjects = readDatabase("/db/json/diff/newResource-localized/TDU_Hair.data.json");
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(currentDatabaseObjects, referenceDatabaseObjects);


        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();


        // THEN
        assertThat(actualPatchObjects).hasSize(1);

        DbPatchDto actualPatchObject = actualPatchObjects.stream().findAny().get();
        assertThat(actualPatchObject.getComment()).isEqualTo(HAIR.name());

        List<DbPatchDto.DbChangeDto> actualChanges = actualPatchObject.getChanges();
        assertThat(actualChanges)
                .hasSize(8)
                .extracting("type").containsOnly(UPDATE_RES);
        assertThat(actualChanges).extracting("strictMode").containsOnly(true);
        assertThat(actualChanges).extracting("ref").containsOnly("54713528");
        assertThat(actualChanges).extracting("topic").containsOnly(HAIR);
        assertThat(actualChanges).extracting("locale").containsOnly((Object[]) new Locale[] { ITALY, FRANCE, UNITED_STATES, KOREA, JAPAN, GERMANY, CHINA, SPAIN });
        assertThat(actualChanges).extracting("value").contains("StringPanthere01-FR", "StringPanthere01-CH", "StringPanthere01-US");
    }

    private static List<DbDto> readReferenceDatabase() {
        return asList(
                DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA),
                DatabaseHelper.createDatabaseTopicForReadOnly(CAR_RIMS),
                DatabaseHelper.createDatabaseTopicForReadOnly(HAIR));
    }

    private static List<DbDto> readDatabase(String jsonFileResource) throws URISyntaxException {
        String jsonDirectory = new File(thisClass.getResource(jsonFileResource).toURI()).getParent();
        return DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);
    }
}
