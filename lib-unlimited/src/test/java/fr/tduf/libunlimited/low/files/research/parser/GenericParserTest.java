package fr.tduf.libunlimited.low.files.research.parser;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {

    @Test
    public void newParser_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        // GIVEN
        byte[] bytes = {0x1, 0x2, 0x3, 0x4};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        // WHEN
        GenericParser<String> actualParser = new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                return null;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/MAP4-map.json";
            }
        };

        // THEN
        assertThat(actualParser.getInputStream()).isEqualTo(inputStream);
        assertThat(actualParser.getFileStructure()).isNotNull();
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