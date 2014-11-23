package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class DbWriterTest {

    @Test
    public void load_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        //GIVEN
        DbDto dbDto = DbDto.builder().build();

        //WHEN
        DbWriter dbWriter = DbWriter.load(dbDto);

        //THEN
        assertThat(dbWriter).isNotNull();
    }

    @Ignore
    public void writeAll_whenRealContents_shouldCreateFiles() throws IOException {
        //GIVEN
        // TODO reinject ref and topic into structure instead of root db
        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_Achievements.json");
        ObjectMapper objectMapper = new ObjectMapper();
        DbDto dbDto = objectMapper.readValue(resourceAsStream, DbDto.class);
        Path tempDirectory = Files.createTempDirectory("libUnlimited-tests");

        //WHEN
        DbWriter.load(dbDto).writeAll(tempDirectory.toString());

        //THEN
        String outputContentsFile = tempDirectory + "//TDU_Achievements.db";
        assertThat(new File(outputContentsFile).exists()).isTrue();
//        String outputResourceFile = tempDirectory + "";
    }
}