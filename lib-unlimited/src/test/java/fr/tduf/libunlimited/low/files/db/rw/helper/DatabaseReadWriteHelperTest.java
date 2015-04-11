package fr.tduf.libunlimited.low.files.db.rw.helper;


import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseWriter;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.common.helper.AssertionsHelper.assertFileDoesNotMatchReference;
import static fr.tduf.libunlimited.common.helper.AssertionsHelper.assertFileMatchesReference;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.STRUCTURE_FIELDS_COUNT_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class DatabaseReadWriteHelperTest {

    private static Class<DatabaseReadWriteHelperTest> thisClass = DatabaseReadWriteHelperTest.class;

    private String tempDirectory;

    @Mock
    private DatabaseWriter databaseWriter;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();
    }

    @Test
    public void readDatabase_whenFileNotFound_shouldReturnMissingContentsNotice() throws URISyntaxException, IOException {
        // GIVEN
        File dbFile = new File("TDU_Achievements.db.nope");
        String databaseDirectory = dbFile.getParent();
        ArrayList<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


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
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


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
        DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, true, integrityErrors);


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
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, false, integrityErrors);


        // THEN
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();

        assertThat(integrityErrors).hasSize(8);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND);
    }

    @Test
    public void readDatabase_whenRealFile_andNonDecryptableContents_shouldReturnInvalidContentsNotice() throws URISyntaxException, IOException {
        // GIVEN (corrupted file)
        File dbFile = new File(thisClass.getResource("/db/encrypted/errors/sizeNotMultipleOf8/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();
        ArrayList<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DbDto actualdbDto = DatabaseReadWriteHelper.readDatabaseTopic(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, false, integrityErrors);


        // THEN
        assertThat(actualdbDto).isNull();

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
        File jsonFile = new File(thisClass.getResource("/db/dumped/TDU_Achievements.json").toURI());
        String jsonDirectory = jsonFile.getParent();

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
        File jsonFile = new File(thisClass.getResource("/db/dumped/TDU_Achievements.json").toURI());
        String jsonDirectory = jsonFile.getParent();


        // WHEN
        List<DbDto> actualTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);


        // THEN
        assertThat(actualTopicObjects).isNotNull();
        assertThat(actualTopicObjects).hasSize(2);

        assertTopicObject(actualTopicObjects.get(0));
        assertTopicObject(actualTopicObjects.get(1));
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
        List<IntegrityError> integrityErrors = new ArrayList<>();
        File dbFile = new File(thisClass.getResource("/db/res/TDU_Achievements.fr").toURI());
        String databaseDirectory = dbFile.getParent();


        // WHEN
        List<List<String>> allResources = DatabaseReadWriteHelper.parseTopicResourcesFromDirectoryAndCheck(DbDto.Topic.ACHIEVEMENTS, databaseDirectory, integrityErrors);


        // THEN
        assertThat(integrityErrors).hasSize(6);  // missing localized resources

        assertThat(allResources).isNotNull();
        assertThat(allResources).hasSize(8);

        Condition<List<String>> lineCountEqualTo253 = new Condition<>( (list) -> list.size() == 253, "253 lines" );
        Condition<List<String>> empty = new Condition<>(List::isEmpty, "no line" );
        assertThat(allResources)
                .has(lineCountEqualTo253, atIndex(0)) // fr or it
                .has(lineCountEqualTo253, atIndex(1)) // fr or it
                .is(empty, atIndex(2))
                .is(empty, atIndex(3))
                .is(empty, atIndex(4))
                .is(empty, atIndex(5))
                .is(empty, atIndex(6))
                .is(empty, atIndex(7));
    }

    @Test
    public void writeDatabaseTopicToJson_whenProvidedContents_andClearContents_shouldCreateFile_andReturnAbsolutePath() throws IOException {
        // GIVEN
        DbDto dbDto = createDatabaseTopicObject();


        // WHEN
        String actualFileName = DatabaseReadWriteHelper.writeDatabaseTopicToJson(dbDto, tempDirectory).get();


        // THEN
        String expectedFileName = new File(tempDirectory, "TDU_Achievements.json").getAbsolutePath();

        assertThat(actualFileName).isEqualTo(expectedFileName);

        assertThat(new File(expectedFileName)).exists();
    }

    @Test
    public void writeDatabaseTopicToJson_whenWriterFailure_shouldReturnAbsent() throws IOException {
        // GIVEN
        DbDto dbDto = createDatabaseTopicObject();


        // WHEN
        Optional<String> potentialFileName = DatabaseReadWriteHelper.writeDatabaseTopicToJson(dbDto, "~nope");


        // THEN
        assertThat(potentialFileName).isEmpty();
    }

    @Test
    public void writeDatabaseTopic_whenProvidedContents_WithoutEncryption_shouldCreateClearFiles() throws URISyntaxException, IOException {
        // GIVEN
        String jsonDirectory = new File(thisClass.getResource("/db/dumped/TDU_Achievements.json").toURI()).getParent();
        DbDto dbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory).get();


        // WHEN
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabaseTopic(dbDto, tempDirectory, true);


        // THEN
        assertThat(writtenFiles).hasSize(3);

        writtenFiles.stream()
                .forEach((fileName) -> assertThat(new File(fileName)).exists());

        assertFileMatchesReference(writtenFiles.get(0), "/db/");
    }

    @Test
    public void writeDatabaseTopic_whenProvidedContents_WithEncryption_shouldCreateEncryptedFiles() throws URISyntaxException, IOException {
        // GIVEN
        String jsonDirectory = new File(thisClass.getResource("/db/dumped/TDU_Achievements.json").toURI()).getParent();
        DbDto dbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(DbDto.Topic.ACHIEVEMENTS, jsonDirectory).get();


        // WHEN
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabaseTopic(dbDto, tempDirectory, false);


        // THEN
        assertThat(writtenFiles).hasSize(3);

        writtenFiles.stream()
                .forEach((fileName) -> assertThat(new File(fileName)).exists());

        assertFileDoesNotMatchReference(writtenFiles.get(0), "/db/encrypted/");
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

    private static void assertTopicObject(DbDto actualdbDto) {
        assertThat(actualdbDto).isNotNull();
        assertThat(actualdbDto.getData()).isNotNull();
        assertThat(actualdbDto.getResources()).isNotEmpty();
        assertThat(actualdbDto.getStructure()).isNotNull();
    }
}