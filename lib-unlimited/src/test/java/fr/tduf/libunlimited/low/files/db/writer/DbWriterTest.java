package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.common.DbHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class DbWriterTest {

    private Path tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests");
    }

    @Test
    public void load_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        //GIVEN
        DbDto dbDto = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .build())
                .withData(DbDataDto.builder()
                        .build())
                .build();

        //WHEN
        DbWriter dbWriter = DbWriter.load(dbDto);

        //THEN
        assertThat(dbWriter).isNotNull();
    }

    @Test
    public void writeAll_whenRealContents_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        DbWriter.load(initialDbDto).writeAll(tempDirectory.toString());

        //THEN
        assertOutputFileMatchesReference("TDU_Achievements.db", "/db/");
        assertOutputFileMatchesReference("TDU_Achievements.fr", "/db/res/clean/");
        assertOutputFileMatchesReference("TDU_Achievements.it", "/db/res/clean/");

        List<String> dbContents = DbHelper.readContentsFromRealFile(tempDirectory + "/TDU_Achievements.db", "UTF-8", "\r\n");
        List<List<String>> dbResources = DbHelper.readResourcesFromRealFiles(
                tempDirectory + "/TDU_Achievements.fr",
                tempDirectory + "/TDU_Achievements.it");
        DbDto finalDbDto = DbParser.load(dbContents, dbResources).parseAll();
        assertThat(finalDbDto).isEqualTo(initialDbDto);
    }

    @Test
    public void writeAll_whenRealContents_shouldCreateContentsFile_withSizeMultipleOf8() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        DbWriter.load(initialDbDto).writeAll(tempDirectory.toString());

        //THEN
        File actualContentsFile = assertFileExistAndGet("TDU_Achievements.db");
        assertThat(actualContentsFile.length() % 8).isEqualTo(0);
    }

    @Test
    public void writeAllAsJson_whenRealContents_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        DbWriter.load(initialDbDto).writeAllAsJson(tempDirectory.toString());

        //THEN
        assertJsonOutputFileMatchesReference("TDU_Achievements.json", "/db/");
    }

    private void assertOutputFileMatchesReference(String outputFileName, String resourceDirectory) throws URISyntaxException {
        File actualContentsFile = assertFileExistAndGet(outputFileName);
        File expectedContentsFile = new File(getClass().getResource(resourceDirectory + outputFileName).toURI());

        assertThat(actualContentsFile).describedAs("File must match reference one: " + expectedContentsFile.getPath()).hasContentEqualTo(expectedContentsFile);
    }

    private void assertJsonOutputFileMatchesReference(String outputFileName, String resourceDirectory) throws URISyntaxException, IOException {
        File actualContentsFile = assertFileExistAndGet(outputFileName);
        byte[] actualEncoded = Files.readAllBytes(actualContentsFile.toPath());
        String actualJson = new String(actualEncoded, Charset.forName("UTF-8"));

        File expectedContentsFile = new File(getClass().getResource(resourceDirectory + outputFileName).toURI());
        byte[] expectedEncoded = Files.readAllBytes(expectedContentsFile.toPath());
        String expectedJson = new String(expectedEncoded, Charset.forName("UTF-8"));

        assertJsonEquals("File must match reference one: " + expectedContentsFile.getPath(), expectedJson, actualJson);
    }

    private File assertFileExistAndGet(String fileName) {
        File actualContentsFile = new File(tempDirectory + "/" + fileName);
        assertThat(actualContentsFile.exists()).describedAs("File must exist: " + actualContentsFile.getPath()).isTrue();
        return actualContentsFile;
    }
}