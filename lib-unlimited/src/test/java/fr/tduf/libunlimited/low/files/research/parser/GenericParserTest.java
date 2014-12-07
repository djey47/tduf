package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {

    private static Class<GenericParserTest> thisClass = GenericParserTest.class;

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

    @Test
    public void parse_whenRealFiles_shouldFillStore() throws URISyntaxException, IOException {
        // GIVEN
        URI uri = thisClass.getResource("/banks/Bnk1.map").toURI();
        byte[] mapContents = Files.readAllBytes(Paths.get(uri))                        ;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mapContents);

        InputStream structureAsStream = getClass().getResourceAsStream("/files/structures/MAP4-map.json");
        FileStructureDto fileStructure = new ObjectMapper().readValue(structureAsStream, FileStructureDto.class);


        // WHEN
        GenericParser genericParser = GenericParser.load(inputStream, fileStructure);
        genericParser.parse();


        // THEN
        assertThat(genericParser.getStore()).hasSize(13); // = Tag + 4*(Hash+Size1+Size2)
        assertThat(genericParser.getStore().get("entry_list[0].file_name_hash")).isEqualTo("858241");
        assertThat(genericParser.getStore().get("entry_list[1].file_name_hash")).isEqualTo("1507153");
        assertThat(genericParser.getStore().get("entry_list[2].file_name_hash")).isEqualTo("1521845");
        assertThat(genericParser.getStore().get("entry_list[3].file_name_hash")).isEqualTo("1572722");
    }
}