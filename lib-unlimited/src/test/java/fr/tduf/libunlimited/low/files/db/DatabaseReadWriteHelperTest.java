package fr.tduf.libunlimited.low.files.db;


import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.STRUCTURE_FIELDS_COUNT_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseReadWriteHelperTest {

    private static Class<DatabaseReadWriteHelperTest> thisClass = DatabaseReadWriteHelperTest.class;

    @Test
    public void readDatabase_whenRealFile_andClearContents_shouldReturnDatabaseContents_andMissingResourceNotices() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();
        ArrayList<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabase(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


        // THEN
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();

        assertThat(integrityErrors).hasSize(8);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND);
    }

    @Test
    public void readDatabase_whenRealFileWithErrors_andClearContents_shouldUpdateIntegrityErrors() throws URISyntaxException, IOException {
        // GIVEN
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // Errors : field count mismatch in structure
        File dbFile = new File(thisClass.getResource("/db/errors/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();


        // WHEN
        DatabaseReadWriteHelper.readDatabase(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


        // THEN
        assertThat(integrityErrors).isNotEmpty();
        assertThat(integrityErrors).extracting("errorTypeEnum").contains(STRUCTURE_FIELDS_COUNT_MISMATCH);
    }

    @Test
    public void readDatabase_whenRealFile_andEncryptedContents_shouldReturnDatabaseContents_andMissingResourceNotices() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/encrypted/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();
        ArrayList<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabase(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, false, integrityErrors);


        // THEN
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();

        assertThat(integrityErrors).hasSize(8);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND);
    }

    @Test
    public void parseTopicContentsFromDirectory_whenRealFile_andClear_Contents_shouldReturnContentsAsLines() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();

        // WHEN
        List<String> actualContentLines = DatabaseReadWriteHelper.parseTopicContentsFromDirectory(DbDto.Topic.ACHIEVEMENTS, databaseDirectory);

        // THEN
        assertThat(actualContentLines).isNotNull();
        assertThat(actualContentLines).hasSize(90);
    }

    @Test
    public void parseTopicResourcesFromDirectory_whenRealFiles_andClearContents_shouldReturnContentsAsCollections() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/res/TDU_Achievements.fr").toURI());
        String databaseDirectory = dbFile.getParent();

        // WHEN
        List<List<String>> allResources = DatabaseReadWriteHelper.parseTopicResourcesFromDirectory(DbDto.Topic.ACHIEVEMENTS, databaseDirectory);

        // THEN
        assertThat(allResources).isNotNull();
        assertThat(allResources).hasSize(8);
        assertThat(allResources.get(0)).hasSize(253); //fr
        assertThat(allResources.get(1)).hasSize(0);
        assertThat(allResources.get(2)).hasSize(0);
        assertThat(allResources.get(3)).hasSize(0);
        assertThat(allResources.get(4)).hasSize(0);
        assertThat(allResources.get(5)).hasSize(0);
        assertThat(allResources.get(6)).hasSize(253); //it
        assertThat(allResources.get(7)).hasSize(0);
    }

    @Test
    public void writeDatabaseToJson_whenProvidedContents_andClearContents_shouldCreateFiles() throws IOException {
        // GIVEN
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forTopic(DbDto.Topic.ACHIEVEMENTS)
                .build();
        DbDataDto dbDataDto = DbDataDto.builder()
                .build();
        DbDto dbDto = DbDto.builder()
                .withStructure(dbStructureDto)
                .withData(dbDataDto)
                .build();

        String outputDirectory = "./tests/";
        new File(outputDirectory).mkdirs();


        // WHEN
        DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, outputDirectory);


        // THEN
        assertThat(new File(outputDirectory + "TDU_Achievements.json")).exists();
    }
}