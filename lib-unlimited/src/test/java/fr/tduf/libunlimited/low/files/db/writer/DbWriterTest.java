package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        DbDto dbDto = new ObjectMapper().readValue(resourceAsStream, DbDto.class);

        //WHEN
        DbWriter.load(dbDto).writeAll(tempDirectory.toString());

        //THEN
        assertOutputFileMatchesReference("TDU_Achievements.db", "/db/");
        assertOutputFileMatchesReference("TDU_Achievements.fr", "/db/res/clean/");
        assertOutputFileMatchesReference("TDU_Achievements.it", "/db/res/clean/");

        //TODO load files and compare against dbDto
    }

    private void assertOutputFileMatchesReference(String outputFileName, String resourceDirectory) throws URISyntaxException {
        File actualContentsFile = new File(tempDirectory + "/" + outputFileName);
        assertThat(actualContentsFile.exists()).describedAs("File must exist: " + actualContentsFile.getPath()).isTrue();

        File expectedContentsFile = new File(getClass().getResource(resourceDirectory + outputFileName).toURI());
        assertThat(actualContentsFile).describedAs("File must match reference one: " + expectedContentsFile.getPath()).hasContentEqualTo(expectedContentsFile);
    }
}