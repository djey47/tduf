package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {

    @Test
    public void load_whenProvided_shouldReturnParserInstance() throws Exception {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {});
        FileStructureDto fileStructure = FileStructureDto.builder()
                .build();

        // WHEN
        GenericParser genericParser = GenericParser.load(inputStream, fileStructure);

        // THEN
        assertThat(genericParser).isNotNull();
    }
}