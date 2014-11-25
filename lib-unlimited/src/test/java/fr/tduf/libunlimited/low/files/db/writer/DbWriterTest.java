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
                        .atVersion("1,2")
                        .withCategoryCount(6)
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
    public void writeAll_whenRealContents_shouldCreateFiles() throws IOException, URISyntaxException {
        //GIVEN
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_Achievements.json");
        ObjectMapper objectMapper = new ObjectMapper();
        DbDto dbDto = objectMapper.readValue(resourceAsStream, DbDto.class);


        //WHEN
        DbWriter.load(dbDto).writeAll(tempDirectory.toString());


        //THEN
        String outputContentsFileName = tempDirectory + "/TDU_Achievements.db";
        File actualContentsFile = new File(outputContentsFileName);
        assertThat(actualContentsFile.exists()).isTrue();
        File expectedContentsFile = new File(getClass().getResource("/db/TDU_Achievements.db").toURI());
        assertThat(actualContentsFile).hasContentEqualTo(expectedContentsFile);
    }
}