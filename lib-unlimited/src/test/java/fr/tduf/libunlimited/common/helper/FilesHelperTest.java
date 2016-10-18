package fr.tduf.libunlimited.common.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesHelperTest {

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForLibrary();
    }

    @Test
    public void createDirectoryIfNotExists_whenExisting_shouldDoNothing() throws IOException {
        // GIVEN-WHEN
        FilesHelper.createDirectoryIfNotExists(tempDirectory);

        // THEN
        File actualDirectory = new File(tempDirectory);
        assertThat(actualDirectory).exists();
        assertThat(actualDirectory).isDirectory();
    }

    @Test
    public void createDirectoryIfNotExists_whenNonExisting_shouldCreateIt() throws IOException {
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
    public void createFileIfNotExists_whenExisting_shouldDoNothing() throws IOException {
        // GIVEN
        Path pathToCreate = Paths.get(tempDirectory, "nope");
        Files.createFile(pathToCreate);

        // WHEN
        FilesHelper.createFileIfNotExists(pathToCreate.toString());

        // THEN
        assertThat(pathToCreate.toFile()).exists();
    }

    @Test
    public void createFileIfNotExists_whenNonExisting_shouldCreateIt() throws IOException {
        // GIVEN
        Path pathToCreate = Paths.get(tempDirectory, "nope");

        // WHEN
        FilesHelper.createFileIfNotExists(pathToCreate.toString());

        // THEN
        assertThat(pathToCreate.toFile()).exists();
    }

    @Test(expected = NullPointerException.class)
    public void readTextFromResourceFile_whenResourceNotFound_shouldThrowNullPointerException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        FilesHelper.readTextFromResourceFile("/not a resource/");

        // THEN: exception
    }

    @Test
    public void readTextFromResourceFile_whenResourceFound_shouldReturnContents() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        String actualContents = FilesHelper.readTextFromResourceFile("/files/file.txt");

        // THEN
        assertThat(actualContents).hasSize(128);
    }

    @Test(expected = NullPointerException.class)
    public void readTextFromResourceFile_withNullEncoding_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        FilesHelper.readTextFromResourceFile("/files/file.txt", null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void readBytesFromResourceFile_whenResourceNotFound_shouldThrowNullPointerException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        FilesHelper.readBytesFromResourceFile("/not a resource/");

        // THEN: exception
    }

    @Test
    public void readBytesFromResourceFile_whenResourceFound_shouldReturnContents() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        byte[] actualContents = FilesHelper.readBytesFromResourceFile("/files/file.txt");

        // THEN
        assertThat(actualContents).hasSize(128);
    }

    @Test(expected = EOFException.class)
    public void readObjectFromJsonResourceFile_whenResourceNotFound_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/not a resource/");

        // THEN: EOFE
    }

    @Test
    public void readObjectFromJsonResourceFile_whenResourceFound_shouldReturnObjectContents() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        DbStructureDto actualObject = FilesHelper.readObjectFromJsonResourceFile(DbStructureDto.class, "/db/json/mapper/topicObject.structure.json");

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject.getTopic()).isEqualTo(DbDto.Topic.ACHIEVEMENTS);
    }

    @Test(expected = NullPointerException.class)
    public void getFileNameFromResourcePath_whenResourceNotFound_shouldThrowNullPointerException() throws URISyntaxException {
        // GIVEN-WHEN
        FilesHelper.getFileNameFromResourcePath("/not a resource/");

        // THEN: exception
    }

    @Test
    public void getFileNameFromResourcePath_whenResourceFound_shouldReturnAbsoluteFilePath() throws URISyntaxException {
        // GIVEN-WHEN
        String actualFileName = FilesHelper.getFileNameFromResourcePath("/files/file.txt");

        // THEN
        assertThat(actualFileName.replace('\\', '/')).endsWith("/resources/test/files/file.txt");
    }

    @Test
    public void writeJsonObjectToFile_whenValidObject_shouldCreateFileWithSameContents() throws IOException, URISyntaxException {
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
    public void getExtension_whenNoExtension_shouldReturnEmptyString() {
        // GIVEN-WHEN
        final String actualExtension = FilesHelper.getExtension("/etc/default/docker");

        // THEN
        assertThat(actualExtension).isEmpty();
    }

    @Test
    public void getExtension_whenExtension_shouldReturnIt() {
        // GIVEN-WHEN
        final String actualExtension = FilesHelper.getExtension("/etc/default/docker.conf");

        // THEN
        assertThat(actualExtension).isEqualTo("conf");
    }

    @Test(expected = NullPointerException.class)
    public void getExtension_whenNullFileName_shouldThrowException() {
        // GIVEN-WHEN
        FilesHelper.getExtension(null);

        // THEN: NPE
    }

    @Test
    public void getNameWithoutExtension_whenNoExtension_shouldReturnSameName() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("docker");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test
    public void getNameWithoutExtension_whenExtension() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("docker.conf");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test
    public void getNameWithoutExtension_whenExtension_andPath_shouldReturnWithoutPath() {
        // GIVEN-WHEN
        final String actualName = FilesHelper.getNameWithoutExtension("/etc/docker.conf");

        // THEN
        assertThat(actualName).isEqualTo("docker");
    }

    @Test(expected = NullPointerException.class)
    public void getNameWithoutExtension_whenNullFileName_shouldThrowException() {
        // GIVEN-WHEN
        FilesHelper.getNameWithoutExtension(null);

        // THEN: NPE
    }

    @Test
    public void readXMLDocumentFromFile_whenCorrectFile_shouldParseIt() throws URISyntaxException, IOException {
        // GIVEN
        final String sampleFile = FilesHelper.getFileNameFromResourcePath("/common/samples/sample.xml");

        // WHEN
        final Document actualDocument = FilesHelper.readXMLDocumentFromFile(sampleFile);

        // THEN
        assertThat(actualDocument).isNotNull();
        assertThat(actualDocument.getDocumentElement().getTagName()).isEqualTo("note");
    }

    @Test(expected = IOException.class)
    public void readXMLDocumentFromFile_whenIncorrectFile_shouldThrowException() throws URISyntaxException, IOException {
        // GIVEN
        final String sampleFile = FilesHelper.getFileNameFromResourcePath("/common/samples/sample_malformed.xml");

        // WHEN
        FilesHelper.readXMLDocumentFromFile(sampleFile);

        // THEN: IOE
    }
}
