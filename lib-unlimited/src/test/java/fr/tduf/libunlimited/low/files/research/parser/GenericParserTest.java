package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {
    private static final Class<GenericParserTest> thisClass = GenericParserTest.class;

    private static final String DATA = "data";

    @Test
    public void newParser_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();

        // WHEN
        GenericParser<String> actualParser = createGenericParser(inputStream);

        // THEN
        assertThat(actualParser.getInputStream()).isEqualTo(inputStream);
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    public void parse_whenProvidedFiles_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();
        GenericParser<String> actualParser = createGenericParser(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
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

    private GenericParser<String> createGenericParser(final ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(7);

                // Field 1
                assertThat(getDataStore().getText("tag")).isEqualTo("ABCDEFGHIJ");

                // Field 2 - item 0
                assertThat(getDataStore().getNumeric("repeater[0].number")).isEqualTo(500L);
                assertThat(getDataStore().getText("repeater[0].text")).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter")).isEqualTo(new byte[]{0xA});
                // Field 2 - item 1
                assertThat(getDataStore().getNumeric("repeater[1].number")).isEqualTo(1000L);
                assertThat(getDataStore().getText("repeater[1].text")).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter")).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private ByteArrayInputStream createInputStreamFromReferenceFile() throws IOException, URISyntaxException {
        URI fileURI = thisClass.getResource("/files/samples/TEST.bin").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(fileURI));
        return new ByteArrayInputStream(bytes);
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