package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffPatchesGeneratorTest {

    private final static Class<DiffPatchesGeneratorTest> thisClass = DiffPatchesGeneratorTest.class;

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
        String jsonDirectory = new File(thisClass.getResource("/db/json/TDU_Achievements.json").toURI()).getParent();
        List<DbDto> databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);
        DiffPatchesGenerator generator = DiffPatchesGenerator.prepare(databaseObjects, databaseObjects);

        // WHEN
        Set<DbPatchDto> actualPatchObjects = generator.makePatches();

        // THEN
        assertThat(actualPatchObjects).isEmpty();
    }
}
