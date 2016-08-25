package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.common.helper.DbHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static fr.tduf.libtesting.common.helper.AssertionsHelper.*;
import static fr.tduf.libunlimited.common.helper.FilesHelper.readObjectFromJsonResourceFile;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseWriterTest {
    private  static final String RESOURCE_JSON_DATABASE_REF = "/db/json/ref/";
    private static final String RESOURCE_DATABASE_CLEAN = "/db/res/clean/";

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = FilesHelper.createTempDirectoryForLibrary();
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
        DbDto initialDbDto = readObjectFromJsonResourceFile(DbDto.class, "/db/json/parsing/TDU_Achievements.json");


        //WHEN
        List<String> actualFilenames = DatabaseWriter.load(initialDbDto).writeAll(tempDirectory);


        //THEN
        String actualContentsFileName = new File(tempDirectory, "TDU_Achievements.db").getAbsolutePath();
        String actualResourceFileName1 = new File(tempDirectory, "TDU_Achievements.fr").getAbsolutePath();
        String actualResourceFileName2 = new File(tempDirectory, "TDU_Achievements.it").getAbsolutePath();
        String actualResourceFileName3 = new File(tempDirectory, "TDU_Achievements.ge").getAbsolutePath();
        String actualResourceFileName4 = new File(tempDirectory, "TDU_Achievements.sp").getAbsolutePath();
        String actualResourceFileName5 = new File(tempDirectory, "TDU_Achievements.us").getAbsolutePath();
        String actualResourceFileName6 = new File(tempDirectory, "TDU_Achievements.ja").getAbsolutePath();
        String actualResourceFileName7 = new File(tempDirectory, "TDU_Achievements.ko").getAbsolutePath();
        String actualResourceFileName8 = new File(tempDirectory, "TDU_Achievements.ch").getAbsolutePath();

        assertThat(actualFilenames).contains(
                actualContentsFileName,
                actualResourceFileName1,
                actualResourceFileName2,
                actualResourceFileName3,
                actualResourceFileName4,
                actualResourceFileName5,
                actualResourceFileName6,
                actualResourceFileName7,
                actualResourceFileName8);

        assertFileMatchesReference(actualContentsFileName, "/db/");
        assertFileMatchesReference(actualResourceFileName1, RESOURCE_DATABASE_CLEAN);
        assertFileMatchesReference(actualResourceFileName2, RESOURCE_DATABASE_CLEAN);

        assertFilesMatchReferenceObject(initialDbDto,
                actualContentsFileName,
                actualResourceFileName1,
                actualResourceFileName2,
                actualResourceFileName3,
                actualResourceFileName4,
                actualResourceFileName5,
                actualResourceFileName6,
                actualResourceFileName7,
                actualResourceFileName8);
    }

    @Test
    public void writeAll_whenRealContents_withReferenceField_shouldCreateFiles_andFillThem() throws IOException, URISyntaxException {
        //GIVEN
        DbDto initialDbDto = readObjectFromJsonResourceFile(DbDto.class, "/db/json/parsing/TDU_Bots.json");


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
        assertFileMatchesReference(actualResourceFileNameFrench, RESOURCE_DATABASE_CLEAN);

        assertFilesMatchReferenceObject(initialDbDto, actualContentsFileName, actualResourceFileNameFrench, actualResourceFileNameItalian);
    }

    @Test
    public void writeAll_whenRealContents_shouldCreateContentsFile_withSizeMultipleOf8() throws IOException, URISyntaxException {
        //GIVEN
        DbDto initialDbDto = readObjectFromJsonResourceFile(DbDto.class, "/db/json/parsing/TDU_Achievements.json");

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
        DbDto initialDbDto = DatabaseHelper.createDatabaseTopicForReadOnly(ACHIEVEMENTS);

        //WHEN
        List<String> actualFileNames = DatabaseWriter.load(initialDbDto).writeAllAsJson(tempDirectory);

        //THEN
        final String expectedDataFileName = Paths.get(tempDirectory, "TDU_Achievements.data.json").toAbsolutePath().toString();
        final String expectedStructureFileName = Paths.get(tempDirectory, "TDU_Achievements.structure.json").toAbsolutePath().toString();
        final String expectedResourceFileName = Paths.get(tempDirectory, "TDU_Achievements.resources.json").toAbsolutePath().toString();

        assertThat(actualFileNames)
                .hasSize(3)
                .containsOnly(
                        expectedDataFileName,
                        expectedStructureFileName,
                        expectedResourceFileName);

        assertJsonFileMatchesReference(expectedDataFileName, RESOURCE_JSON_DATABASE_REF);
        assertJsonFileMatchesReference(expectedStructureFileName, RESOURCE_JSON_DATABASE_REF);
        assertJsonFileMatchesReference(expectedResourceFileName, RESOURCE_JSON_DATABASE_REF);
    }

    private static void assertFilesMatchReferenceObject(DbDto referenceDto, String contentsFileName, String... resourceFileNames) throws FileNotFoundException {
        List<String> dbContents = DbHelper.readContentsFromRealFile(contentsFileName, "UTF-8");
        Map<Locale, List<String>> dbResources = DbHelper.readResourcesFromRealFiles(resourceFileNames);
        DbDto finalDbDto = DatabaseParser.load(dbContents, dbResources).parseAll();
        assertThat(finalDbDto).isEqualTo(referenceDto);
    }
}
