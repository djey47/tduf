package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.common.helper.DbHelper;
import fr.tduf.libunlimited.low.files.db.dto.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static fr.tduf.libtesting.common.helper.AssertionsHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseWriterTest {

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();
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
        DatabaseWriter databaseWriter = DatabaseWriter.load(dbDto);

        //THEN
        assertThat(databaseWriter).isNotNull();
    }

    @Test
    public void writeAll_whenRealContents_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/json/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);


        //WHEN
        List<String> actualFilenames = DatabaseWriter.load(initialDbDto).writeAll(tempDirectory);


        //THEN
        String actualContentsFileName = new File(tempDirectory, "TDU_Achievements.db").getAbsolutePath();
        String actualResourceFileName1 = new File(tempDirectory, "TDU_Achievements.fr").getAbsolutePath();
        String actualResourceFileName2 = new File(tempDirectory, "TDU_Achievements.it").getAbsolutePath();

        assertThat(actualFilenames).contains(
                actualContentsFileName,
                actualResourceFileName1,
                actualResourceFileName2);

        assertFileMatchesReference(actualContentsFileName, "/db/");
        assertFileMatchesReference(actualResourceFileName1, "/db/res/clean/");
        assertFileMatchesReference(actualResourceFileName2, "/db/res/clean/");

        assertFilesMatchReferenceObject(initialDbDto, actualContentsFileName, actualResourceFileName1, actualResourceFileName2);
    }

    @Test
    public void writeAll_whenRealContents_withReferenceField_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/json/TDU_Bots.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);


        //WHEN
        List<String> actualFilenames = DatabaseWriter.load(initialDbDto).writeAll(tempDirectory);


        //THEN
        String actualContentsFileName = new File(tempDirectory, "TDU_Bots.db").getAbsolutePath();
        String actualResourceFileNameFrench = new File(tempDirectory, "TDU_Bots.fr").getAbsolutePath();
        String actualResourceFileNameItalian = new File(tempDirectory, "TDU_Bots.it").getAbsolutePath();

        assertThat(actualFilenames).contains(
                actualContentsFileName,
                actualResourceFileNameFrench,
                actualResourceFileNameItalian);

        assertFileMatchesReference(actualContentsFileName, "/db/");
        assertFileMatchesReference(actualResourceFileNameFrench, "/db/res/clean/");

        assertFilesMatchReferenceObject(initialDbDto, actualContentsFileName, actualResourceFileNameFrench, actualResourceFileNameItalian);
    }

    @Test
    public void writeAll_whenRealContents_shouldCreateContentsFile_withSizeMultipleOf8() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/json/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        List<String> actualFilenames = DatabaseWriter.load(initialDbDto).writeAll(tempDirectory);

        //THEN
        assertThat(actualFilenames).hasSize(9);

        File actualContentsFile = assertFileExistAndGet(new File(tempDirectory,"TDU_Achievements.db").getAbsolutePath());
        assertThat(actualContentsFile.length() % 8).isEqualTo(0);
    }

    @Test
    public void writeAllAsJson_whenRealContents_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException, JSONException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/json/TDU_Achievements.json");
        DbDto initialDbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        String actualFileName = DatabaseWriter.load(initialDbDto).writeAllAsJson(tempDirectory);

        //THEN
        String expectedFileName = new File(tempDirectory, "TDU_Achievements.json").getAbsolutePath();

        assertThat(actualFileName).isEqualTo(expectedFileName);

        assertJsonFileMatchesReference(expectedFileName, "/db/json/");
    }

    private static void assertFilesMatchReferenceObject(DbDto referenceDto, String contentsFileName, String... resourceFileNames) throws FileNotFoundException {
        List<String> dbContents = DbHelper.readContentsFromRealFile(contentsFileName, "UTF-8");
        Map<DbResourceEnhancedDto.Locale, List<String>> dbResources = DbHelper.readResourcesFromRealFiles(resourceFileNames);
        DbDto finalDbDto = DatabaseParser.load(dbContents, dbResources).parseAll();
        assertThat(finalDbDto).isEqualTo(referenceDto);
    }
}
