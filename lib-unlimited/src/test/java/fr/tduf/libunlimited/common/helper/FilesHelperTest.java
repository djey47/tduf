package fr.tduf.libunlimited.common.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilesHelperTest {

    private String tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForLibrary();
    }

    @Test
    void createDirectoryIfNotExists_whenExisting_shouldDoNothing() throws IOException {
        // GIVEN-WHEN
        FilesHelper.createDirectoryIfNotExists(tempDirectory);

        // THEN
        File actualDirectory = new File(tempDirectory);
        assertThat(actualDirectory).exists();
        assertThat(actualDirectory).isDirectory();
    }

    @Test
    void createDirectoryIfNotExists_whenNonExisting_shouldCreateIt() throws IOException {
        // GIVEN
        String directoryToCreate = Paths.get(tempDirectory, "1", "2", "3").toString();

        // WHEN
        FilesHelper.createDirectoryIfNotExists(directoryToCreate);

        // THEN
        File actualDirectory = new File(directoryToCreate);
        assertThat(actualDirectory).exists();
        assertThat(actualDirectory).isDirectory();
    }

    @Test
    void createFileIfNotExists_whenExisting_shouldDoNothing() throws IOException {
        // GIVEN
        Path pathToCreate = Paths.get(tempDirectory, "nope");
        Files.createFile(pathToCreate);

        // WHEN
        FilesHelper.createFileIfNotExists(pathToCreate.toString());

        // THEN
        assertThat(pathToCreate.toFile()).exists();
    }

    @Test
    void createFileIfNotExists_whenNonExisting_shouldCreateIt() throws IOException {
        // GIVEN
        Path pathToCreate = Paths.get(tempDirectory, "nope");

        // WHEN
        FilesHelper.createFileIfNotExists(pathToCreate.toString());

        // THEN
        assertThat(pathToCreate.toFile()).exists();
    }

    @Test
    void readTextFromResourceFile_whenResourceNotFound_shouldThrowNullPointerException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.readTextFromResourceFile("/not a resource/"));
    }

    @Test
    void readTextFromResourceFile_whenResourceFound_shouldReturnContents() throws IOException {
        // GIVEN-WHEN
        String actualContents = FilesHelper.readTextFromResourceFile("/files/file.txt");

        // THEN
        assertThat(actualContents).hasSize(128);
    }

    @Test
    void readTextFromResourceFile_withNullEncoding_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.readTextFromResourceFile("/files/file.txt", null));
    }

    @Test
    void readBytesFromResourceFile_whenResourceNotFound_shouldThrowNullPointerException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.readBytesFromResourceFile("/not a resource/"));
    }

    @Test
    void readBytesFromResourceFile_whenResourceFound_shouldReturnContents() throws IOException {
        // GIVEN-WHEN
        byte[] actualContents = FilesHelper.readBytesFromResourceFile("/files/file.txt");

        // THEN
        assertThat(actualContents).hasSize(128);
    }

    @Test
    void readObjectFromJsonResourceFile_whenResourceNotFound_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/not a resource/"));
    }

    @Test
    void readObjectFromJsonResourceFile_whenResourceFound_shouldReturnObjectContents() throws IOException {
        // GIVEN-WHEN
        DbStructureDto actualObject = FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/mapper/topicObject.structure.json");

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject.getTopic()).isEqualTo(DbDto.Topic.ACHIEVEMENTS);
    }

    @Test
    void getFileNameFromResourcePath_whenResourceNotFound_shouldThrowNullPointerException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.getFileNameFromResourcePath("/not a resource/"));
    }

    @Test
    void getFileNameFromResourcePath_whenResourceFound_shouldReturnAbsoluteFilePath() throws URISyntaxException {
        // GIVEN-WHEN
        String actualFileName = FilesHelper.getFileNameFromResourcePath("/files/file.txt");

        // THEN
        assertThat(actualFileName.replace('\\', '/')).endsWith("/resources/test/files/file.txt");
    }

    @Test
    void writeJsonObjectToFile_whenValidObject_shouldCreateFileWithSameContents() throws IOException {
        // GIVEN
        Path outputFilePath = Paths.get(tempDirectory, "writtenJson", "TDU_Achievements.json");
        DbDataDto sourceObject = FilesHelper.readObjectFromJsonResourceFile(DbDataDto.class, "/db/json/mapper/topicObject.data.json");

        // WHEN
        FilesHelper.writeJsonObjectToFile(sourceObject, outputFilePath.toString());

        // THEN
        DbDataDto actualObject = new ObjectMapper().readValue(outputFilePath.toFile(), DbDataDto.class);
        assertThat(actualObject).isEqualTo(sourceObject);
    }

    @Test
    void getExtension_whenNoExtension_shouldReturnEmptyString() {
        // GIVEN-WHEN
        final String actualExtension = FilesHelper.getExtension("/etc/default/docker");

        // THEN
        assertThat(actualExtension).isEmpty();
    }

    @Test
    void getExtension_whenExtension_shouldReturnIt() {
        // GIVEN-WHEN
        final String actualExtension = FilesHelper.getExtension("/etc/default/docker.conf");

        // THEN
        assertThat(actualExtension).isEqualTo("conf");
    }

    @Test
    void getExtension_whenNullFileName_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.getExtension(null));
    }

    @Test
    void getNameWithoutExtension_whenNoExtension_shouldReturnSameName() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("docker");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test
    void getNameWithoutExtension_whenExtension() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("docker.conf");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test
    void getNameWithoutExtension_whenExtension_andPath_shouldReturnWithoutPath() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("/etc/docker.conf");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test
    void getNameWithoutExtension_whenNullFileName_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> FilesHelper.getNameWithoutExtension(null));
    }

    @Test
    void readXMLDocumentFromFile_whenCorrectFile_shouldParseIt() throws URISyntaxException, IOException {
        // GIVEN
        final String sampleFile = FilesHelper.getFileNameFromResourcePath("/common/samples/sample.xml");

        // WHEN
        final Document actualDocument = FilesHelper.readXMLDocumentFromFile(sampleFile);

        // THEN
        assertThat(actualDocument).isNotNull();
        assertThat(actualDocument.getDocumentElement().getTagName()).isEqualTo("note");
    }

    @Test
    void readXMLDocumentFromFile_whenIncorrectFile_shouldThrowException() throws URISyntaxException {
        // GIVEN
        final String sampleFile = FilesHelper.getFileNameFromResourcePath("/common/samples/sample_malformed.xml");

        // WHEN-THEN
        assertThrows(IOException.class,
                () -> FilesHelper.readXMLDocumentFromFile(sampleFile));
    }
}
