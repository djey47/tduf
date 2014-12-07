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
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {

    private static Class<GenericParserTest> thisClass = GenericParserTest.class;

    @Test
    public void load_whenProvided_shouldReturnParserInstance() throws Exception {
        // GIVEN-WHEN
        GenericParser genericParser = createDefaultGenericParser();

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

    @Test
    public void computeStructureSize_withoutSubFields_shouldReturnRealSizeInBytes() {
        // GIVEN
        List<FileStructureDto.Field> fields = createFields();

        // WHEN
        int actualStructureSize = GenericParser.computeStructureSize(fields);

        // THEN
        assertThat(actualStructureSize).isEqualTo(24);
    }

    @Test
    public void computeStructureSize_withSubFields_andFixedSize_shouldReturnRealSizeInBytes() {
        // GIVEN
        List<FileStructureDto.Field> subFields = createFields();

        FileStructureDto.Field field1 = FileStructureDto.Field.builder()
                .forName("tag")
                .ofSizeBytes(5)
                .withType(FileStructureDto.Type.TEXT)
                .build();
        FileStructureDto.Field field2 = FileStructureDto.Field.builder()
                .forName("entry_list")
                .withType(FileStructureDto.Type.REPEATER)
                .withSubFields(subFields)
                .ofSubItemCount(4)
                .build();

        List<FileStructureDto.Field> fields = asList(field1, field2);


        // WHEN
        int actualStructureSize = GenericParser.computeStructureSize(fields);


        // THEN
        assertThat(actualStructureSize).isEqualTo(101); // = 5 + 4*24
    }

    @Test
    public void getNumericListOf_whenProvidedStore_shouldReturnSelectedValues() {
        // GIVEN
        GenericParser genericParser = createDefaultGenericParser();
        enhanceStoreWithEntries(genericParser);

        // WHEN
        List<Long> values = genericParser.getNumericListOf("my_field");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values).containsAll(asList(10L, 20L, 30L));
    }

    @Test
    public void getRepeatedValuesOf_whenProvidedStore_shouldReturnCorrespondingValues() {
        // GIVEN
        GenericParser genericParser = createDefaultGenericParser();
        enhanceStoreWithEntries(genericParser);

        // WHEN
        List<Map<String, String>> values = genericParser.getRepeatedValuesOf("entry_list");

        // THEN
        assertThat(values).isNotNull();
        assertThat(values).hasSize(3);
        assertThat(values.get(0)).hasSize(2);
        assertThat(values.get(1)).hasSize(2);
        assertThat(values.get(2)).hasSize(2);
    }

    private static void enhanceStoreWithEntries(GenericParser genericParser) {
        genericParser.getStore().put("entry_list[0].my_field", "10");
        genericParser.getStore().put("entry_list[0].a_field", "az");
        genericParser.getStore().put("entry_list[1].my_field", "20");
        genericParser.getStore().put("entry_list[1].a_field", "bz");
        genericParser.getStore().put("entry_list[2].my_field", "30");
        genericParser.getStore().put("entry_list[2].a_field", "cz");
    }

    private static GenericParser createDefaultGenericParser() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[] {});
        FileStructureDto fileStructure = FileStructureDto.builder()
                .build();
        return GenericParser.load(inputStream, fileStructure);
    }

    private static List<FileStructureDto.Field> createFields() {
        FileStructureDto.Field field1 = FileStructureDto.Field.builder()
                .forName("file_name_hash")
                .withType(FileStructureDto.Type.NUMBER)
                .ofSizeBytes(4)
                .build();
        FileStructureDto.Field field2 = FileStructureDto.Field.builder()
                .forName("size_bytes_1")
                .withType(FileStructureDto.Type.NUMBER)
                .ofSizeBytes(4)
                .build();
        FileStructureDto.Field field3 = FileStructureDto.Field.builder()
                .forName("gap_1")
                .withType(FileStructureDto.Type.DELIMITER)
                .ofSizeBytes(4)
                .build();
        FileStructureDto.Field field4 = FileStructureDto.Field.builder()
                .forName("size_bytes_2")
                .withType(FileStructureDto.Type.NUMBER)
                .ofSizeBytes(4)
                .build();
        FileStructureDto.Field field5 = FileStructureDto.Field.builder()
                .forName("gap_2")
                .withType(FileStructureDto.Type.DELIMITER)
                .ofSizeBytes(4)
                .build();
        FileStructureDto.Field field6 = FileStructureDto.Field.builder()
                .forName("entry_end")
                .withType(FileStructureDto.Type.DELIMITER)
                .ofSizeBytes(4)
                .build();
        return asList(field1, field2, field3, field4, field5, field6);
    }
}