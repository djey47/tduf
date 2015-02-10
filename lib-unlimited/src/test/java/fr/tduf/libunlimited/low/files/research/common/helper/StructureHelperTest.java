package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
}