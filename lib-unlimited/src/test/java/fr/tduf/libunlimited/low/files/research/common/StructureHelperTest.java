package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

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
}