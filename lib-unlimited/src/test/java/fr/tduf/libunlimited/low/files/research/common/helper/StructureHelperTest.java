package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.INTEGER;
import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.REPEATER;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StructureHelperTest {

    @Test
    public void retrieveStructureFromLocation_whenClasspathResource_shouldReturnResource() throws Exception {
        // GIVEN
        String resourcePath = "/files/structures/TEST-map.json";

        // WHEN
        FileStructureDto actualStructure = StructureHelper.retrieveStructureFromLocation(resourcePath);

        // THEN
        assertThat(actualStructure).isNotNull();
    }

    @Test
    public void retrieveStructureFromLocation_whenExternalResource_shouldReturnResource() throws Exception {
        // GIVEN
        String resourcePath = "./src/test/resources/files/structures/TEST-map.json";

        // WHEN
        FileStructureDto actualStructure = StructureHelper.retrieveStructureFromLocation(resourcePath);

        // THEN
        assertThat(actualStructure).isNotNull();
    }

    @Test
    public void encryptIfNeeded_whenNoCryptoMode_shouldReturnInitialContents() throws IOException {
        // GIVEN
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(100);

        // WHEN
        ByteArrayOutputStream actualOutputStream = StructureHelper.encryptIfNeeded(outputStream, null);

        // THEN
        assertThat(actualOutputStream).isEqualTo(outputStream);
    }

    @Test
    public void encryptIfNeeded_whenCryptoMode_shouldReturnEncryptedContents() throws IOException {
        // GIVEN
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(new byte[8]);

        // WHEN
        ByteArrayOutputStream actualOutputStream = StructureHelper.encryptIfNeeded(outputStream, 0);

        // THEN
        assertThat(actualOutputStream.size()).isEqualTo(outputStream.size());
        assertThat(actualOutputStream).isNotEqualTo(outputStream);
    }

    @Test
    public void decryptIfNeeded_whenNoCryptoMode_shouldReturnInitialContents() throws IOException {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{ 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8 });

        // WHEN
        ByteArrayInputStream actualInputStream = StructureHelper.decryptIfNeeded(inputStream, null);

        // THEN
        assertThat(actualInputStream).isEqualTo(inputStream);
    }

    @Test
    public void decryptIfNeeded_whenCryptoMode_shouldReturnEncryptedContents() throws IOException {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{ 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8 });

        // WHEN
        ByteArrayInputStream actualInputStream = StructureHelper.decryptIfNeeded(inputStream, 0);

        // THEN
        inputStream.reset();
        assertThat(actualInputStream.available()).isEqualTo(actualInputStream.available());
        assertThat(actualInputStream).isNotEqualTo(inputStream);
    }

    @Test
    public void getFieldDefinitionFromFullName_whenFieldExist_shouldReturnDef() {
        // GIVEN
        FileStructureDto.Field field = FileStructureDto.Field.builder()
                .forName("my_field")
                .withType(INTEGER)
                .signed(true)
                .build();
        FileStructureDto.Field repeaterField = FileStructureDto.Field.builder()
                .forName("entry_list")
                .withType(REPEATER)
                .withSubFields(singletonList(field))
                .build();
        FileStructureDto fileStructureObject = FileStructureDto.builder()
                .addFields(singletonList(repeaterField))
                .build();

        // WHEN
        FileStructureDto.Field actualField = StructureHelper.getFieldDefinitionFromFullName("entry_list[0].my_field", fileStructureObject).get();

        // THEN
        assertThat(actualField.isSigned()).isTrue();
        assertThat(actualField.getType()).isEqualTo(INTEGER);
    }

    @Test
    public void getFieldDefinitionFromFullName_whenFieldDoesNotExist_shouldReturnDef() {
        // GIVEN
        FileStructureDto.Field field = FileStructureDto.Field.builder()
                .forName("my_field")
                .withType(INTEGER)
                .signed(true)
                .build();
        FileStructureDto.Field repeaterField = FileStructureDto.Field.builder()
                .forName("entry_list")
                .withType(REPEATER)
                .withSubFields(singletonList(field))
                .build();
        FileStructureDto fileStructureObject = FileStructureDto.builder()
                .addFields(singletonList(repeaterField))
                .build();

        // WHEN
        Optional<FileStructureDto.Field> potentialField = StructureHelper.getFieldDefinitionFromFullName("entry_list[0].my_field1", fileStructureObject);

        // THEN
        assertThat(potentialField).isEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getFieldDefinitionFromFullName_whenNullStructureObject_shouldThrowException() {
        // GIVEN-WHEN
        StructureHelper.getFieldDefinitionFromFullName("entry_list[0].my_field", null);

        // THEN: NPE
    }
}