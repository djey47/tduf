package fr.tduf.libunlimited.low.files.research.writer;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericWriterTest {

    private static Class<GenericWriterTest> thisClass = GenericWriterTest.class;

    @Test
    public void load_whenProvided_shouldReturnParserInstance() throws Exception {
        // GIVEN-WHEN
        FileStructureDto fileStructure = FileStructureDto.builder().build();
        GenericWriter genericWriter = GenericWriter.load(fileStructure);

        // THEN
        assertThat(genericWriter).isNotNull();
    }

    @Test
    public void write_whenRealFiles_shouldReturnBytes() throws IOException {
        // GIVEN
        URL structureURL = thisClass.getResource("/files/structures/MAP4-map.json");
        FileStructureDto fileStructure = new ObjectMapper().readValue(structureURL, FileStructureDto.class);

        // WHEN
        ByteArrayOutputStream actualOutputStream = GenericWriter.load(fileStructure).write();

        // THEN
//        assertThat(actualOutputStream).isNotNull();
//        byte[] expectedContents = {};
//        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedContents);
    }
}