package fr.tduf.libunlimited.low.files.db.rw.helper;


import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.libtesting.common.helper.AssertionsHelper.assertFileDoesNotMatchReference;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseReadWriteHelperTest {

    private static Class<DatabaseReadWriteHelperTest> thisClass = DatabaseReadWriteHelperTest.class;

    private String tempDirectory;

    private String existingAsFile;

    @Before
    public void setUp() throws IOException {
        tempDirectory = FilesHelper.createTempDirectoryForLibrary();
        existingAsFile = Files.createFile(Paths.get(tempDirectory, "file")).toString();
    }

    @Test
    public void readDatabase_whenFileNotFound_shouldReturnMissingContentsNotice() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File("TDU_Achievements.db.nope");
        String databaseDirectory = dbFile.getParent();
        Set<IntegrityError> integrityErrors = new HashSet<>();


        // WHEN
        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, integrityErrors);


        // THEN
        assertThat(potentialDbDto).isEmpty();

        assertThat(integrityErrors).hasSize(1);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.CONTENTS_NOT_FOUND);
    }

    @Test
    public void readDatabase_whenRealFile_andEncryptedContents_shouldReturnDatabaseContents_andMissingResourceNotices() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/encrypted/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();
        Set<IntegrityError> integrityErrors = new HashSet<>();


        // WHEN
        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, integrityErrors);


        // THEN
        assertThat(potentialDbDto).isPresent();
        assertThat(potentialDbDto.get().getData()).isNotNull();

        assertThat(integrityErrors).hasSize(8);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND);
    }

    @Test
    public void readDatabase_whenRealFile_andNonDecryptableContents_shouldReturnInvalidContentsNotice() throws URISyntaxException, IOException {
        // GIVEN (corrupted file)
        File dbFile = new File(thisClass.getResource("/db/encrypted/errors/sizeNotMultipleOf8/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();
        Set<IntegrityError> integrityErrors = new HashSet<>();


        // WHEN
        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, integrityErrors);


        // THEN
        assertThat(potentialDbDto).isEmpty();

        assertThat(integrityErrors).hasSize(1);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED);
    }

    @Test
    public void readDatabaseFromJson_whenFileNotFound_shouldReturnEmpty() throws URISyntaxException, IOException {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseReadWriteHelper.readDatabaseTopicFromJson(DbDto.Topic.ACHIEVEMENTS, "")).isEmpty();
    }

    @Test
    public void readDatabaseFromJson_whenRealFile_shouldReturnCorrespondingDto() throws URISyntaxException, IOException {
        // GIVEN
        String jsonDirectory = getJsonDirectoryFromResourceFile();

        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory).get();

        // THEN
        assertTopicObject(actualdbDto);
    }

    @Test
    public void readFullDatabaseFromJson_whenMissingFiles_shouldReturnEmptyList() {
        // GIVEN
        String jsonDirectory = "." ;

        // WHEN
        List<DbDto> actualDbDtos = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

        // THEN
        assertThat(actualDbDtos).isEmpty();
    }

    @Test
    public void readFullDatabaseFromJson_whenRealFiles_shouldReturnCorrespondingDtos() throws URISyntaxException {
        // GIVEN
        String jsonDirectory = getJsonDirectoryFromResourceFile();


        // WHEN
        List<DbDto> actualTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);


        // THEN
        assertThat(actualTopicObjects).isNotNull();
        assertThat(actualTopicObjects).hasSize(14);

        actualTopicObjects.forEach(DatabaseReadWriteHelperTest::assertTopicObject);
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
    public void parseTopicContentsFromDirectory_whenRealFile_shouldReturnContentsAsLines() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/TDU_Achievements.db").toURI());

        // WHEN
        List<String> actualContentLines = DatabaseReadWriteHelper.parseTopicContentsFromFile(dbFile.getPath());

        // THEN
        assertThat(actualContentLines).isNotNull();
        assertThat(actualContentLines).hasSize(90);
    }

    @Test
    public void parseTopicResourcesFromDirectory_whenRealFiles_shouldReturnContentsAsCollections() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        Set<IntegrityError> integrityErrors = new HashSet<>();
        File dbFile = new File(thisClass.getResource("/db/res/TDU_Achievements.fr").toURI());
        String databaseDirectory = dbFile.getParent();


        // WHEN
        Map<fr.tduf.libunlimited.common.game.domain.Locale, List<String>> allResources = DatabaseReadWriteHelper.parseTopicResourcesFromDirectoryAndCheck(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, integrityErrors);


        // THEN
        assertThat(integrityErrors).hasSize(6);  // missing localized resources

        assertThat(allResources).isNotNull();
        assertThat(allResources).hasSize(8);

        Condition<List<? extends String>> lineCountEqualTo253 = new Condition<>( (list) -> list.size() == 253, "253 lines" );
        assertThat(allResources.get(Locale.FRANCE)).has(lineCountEqualTo253);
        assertThat(allResources.get(Locale.ITALY)).has(lineCountEqualTo253);
    }

    @Test
    public void writeDatabaseTopicToJson_whenProvidedContents_shouldCreateFile_andReturnAbsolutePaths() throws IOException {
        // GIVEN
        DbDto dbDto = createDatabaseTopicObject();


        // WHEN
        List<String> actualFileNames = DatabaseReadWriteHelper.writeDatabaseTopicToJson(dbDto, tempDirectory);


        // THEN
        assertFileNamesMatcheAndFilesExist(actualFileNames);
    }

    @Test
    public void writeDatabaseTopicToJson_whenWriterFailure_shouldReturnEmptyNameList() throws IOException {
        // GIVEN
        DbDto dbDto = createDatabaseTopicObject();

        // WHEN
        List<String> actualFileNames = DatabaseReadWriteHelper.writeDatabaseTopicToJson(dbDto, existingAsFile);

        // THEN
        assertThat(actualFileNames).isEmpty();
    }

    @Test
    public void writeDatabaseTopicsToJson_whenProvidedContents_shouldCreateFiles_andReturnFileNames() {
        // GIVEN
        List<DbDto> topicObjects = singletonList(createDatabaseTopicObject());

        // WHEN
        List<String> writtenFileNames = DatabaseReadWriteHelper.writeDatabaseTopicsToJson(topicObjects, tempDirectory);

        // THEN
        assertFileNamesMatcheAndFilesExist(writtenFileNames);
    }

    @Test
    public void writeDatabaseTopicsToJson_whenWriterFailure_shouldReturnEmptyList() throws IOException {
        // GIVEN
        List<DbDto> topicObjects = singletonList(createDatabaseTopicObject());

        // WHEN
        List<String> writtenFileNames = DatabaseReadWriteHelper.writeDatabaseTopicsToJson(topicObjects, existingAsFile);

        // THEN
        assertThat(writtenFileNames).isEmpty();
    }

    @Test
    public void writeDatabaseTopic_whenProvidedContents_shouldCreateEncryptedFiles() throws URISyntaxException, IOException {
        // GIVEN
        String jsonDirectory = getJsonDirectoryFromResourceFile();
        DbDto dbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory).get();


        // WHEN
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabaseTopic(dbDto, tempDirectory);


        // THEN
        assertThat(writtenFiles).hasSize(9);

        writtenFiles.stream()
                .forEach((fileName) -> assertThat(new File(fileName)).exists());

        assertFileDoesNotMatchReference(writtenFiles.get(0), "/db/encrypted/");
    }

    private String getJsonDirectoryFromResourceFile() throws URISyntaxException {
        File jsonFile = new File(thisClass.getResource("/db/json/TDU_Achievements.data.json").toURI());
        return jsonFile.getParent();
    }

    private DbDto createDatabaseTopicObject() {
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forTopic(DbDto.Topic.ACHIEVEMENTS)
                .build();
        DbDataDto dbDataDto = DbDataDto.builder()
                .build();
        return DbDto.builder()
                .withStructure(dbStructureDto)
                .withData(dbDataDto)
                .build();
    }

    private void assertFileNamesMatcheAndFilesExist(List<String> fileNames) {
        String expectedDataFileName = Paths.get(tempDirectory, "TDU_Achievements.data.json").toAbsolutePath().toString();
        String expectedResourceFileName = Paths.get(tempDirectory, "TDU_Achievements.resources.json").toAbsolutePath().toString();
        String expectedStructureFileName = Paths.get(tempDirectory, "TDU_Achievements.structure.json").toAbsolutePath().toString();

        assertThat(fileNames)
                .hasSize(3)
                .containsOnly(
                        expectedDataFileName,
                        expectedStructureFileName,
                        expectedResourceFileName);

        assertThat(new File(expectedDataFileName)).exists();
        assertThat(new File(expectedResourceFileName)).exists();
        assertThat(new File(expectedStructureFileName)).exists();
    }

    private static void assertTopicObject(DbDto actualdbDto) {
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();
        assertThat(actualdbDto.getResource()).isNotNull();
        assertThat(actualdbDto.getStructure()).isNotNull();
    }
}
