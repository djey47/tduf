package fr.tduf.libunlimited.low.files.db;


import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Ignore;
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
    public void readDatabase_whenFileNotFound_shouldReturnMissingContentsNotice() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File("TDU_Achievements.db.nope");
        String databaseDirectory = dbFile.getParent();
        ArrayList<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabase(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


        // THEN
        assertThat(actualdbDto).isNull();

        assertThat(integrityErrors).hasSize(1);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.CONTENTS_NOT_FOUND);
    }

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
    public void readDatabaseFromJson_whenFileNotFound_shouldReturnNull() throws URISyntaxException, IOException {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseReadWriteHelper.readDatabaseFromJson(DbDto.Topic.ACHIEVEMENTS, "")).isNull();
    }

    @Test
    public void readDatabaseFromJson_whenRealFile_shouldReturnCorrespondingDto() throws URISyntaxException, IOException {
        // GIVEN
        File jsonFile = new File(thisClass.getResource("/db/TDU_Achievements.json").toURI());
        String jsonDirectory = jsonFile.getParent();

        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory);

        // THEN
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();
        assertThat(actualdbDto.getResources()).isNotEmpty();
    }

    @Test
    public void parseTopicContentsFromDirectory_whenFileNotFound_shouldReturnEmptyList() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File("TDU_Achievements.db.nope");

        // WHEN
        List<String> actualContentLines = DatabaseReadWriteHelper.parseTopicContentsFromFile(dbFile.getPath());

        // THEN
        assertThat(actualContentLines).isNotNull();
        assertThat(actualContentLines).isEmpty();
    }

    @Test
    public void parseTopicContentsFromDirectory_whenRealFile_andClear_Contents_shouldReturnContentsAsLines() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/TDU_Achievements.db").toURI());

        // WHEN
        List<String> actualContentLines = DatabaseReadWriteHelper.parseTopicContentsFromFile(dbFile.getPath());

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

        String outputDirectory = createTestOutputDirectory();


        // WHEN
        DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, outputDirectory);


        // THEN
        assertThat(new File(outputDirectory + "TDU_Achievements.json")).exists();
    }

    @Test
    @Ignore
    public void writeDatabase_whenProvidedContents_WithoutEncryption_shouldCreateClearFiles() throws URISyntaxException, IOException {
        // GIVEN
        String jsonDirectory = new File(thisClass.getResource("/db/TDU_Achievements.json").toURI()).getParent();
        DbDto dbDto = DatabaseReadWriteHelper.readDatabaseFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory);

        String outputDirectory = createTestOutputDirectory();


        // WHEN
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabase(dbDto, outputDirectory, true);


        // THEN
        assertThat(writtenFiles).hasSize(3);

        writtenFiles.stream()
                .forEach((fileName) -> assertThat(new File(fileName)).exists());
    }

    @Test
    public void writeDatabase_whenProvidedContents_WithEncryption_shouldCreateEncryptedFiles() {

    }

    private static String createTestOutputDirectory() {
        String outputDirectory = "./tests/";
        new File(outputDirectory).mkdirs();
        return outputDirectory;
    }
}