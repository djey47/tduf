package fr.tduf.libunlimited.low.files.bin.cameras.rw;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class CamerasWriter_focusOnEnhancedTest {

    @Test
    void load_shouldReturnWriterInstance() throws IOException {
        // GIVEN
        CameraInfoEnhanced cameraInfo = CameraInfoEnhanced.builder().build();

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(cameraInfo);

        // THEN
        assertThat(actualWriter).isNotNull();
    }

    @Test
    void write_shouldReturnOriginalContentsBack() throws IOException, URISyntaxException {
        // GIVEN
        String camerasContentsFromJSONFile = FilesHelper.readTextFromResourceFile("/bin/Cameras.bin.json");
        CamerasParser camerasParser = CamerasParser.load(new ByteArrayInputStream(new byte[0]));
        camerasParser.getDataStore().fromJsonString(camerasContentsFromJSONFile);
        CameraInfoEnhanced cameraInfo = camerasParser.generate();

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(cameraInfo);
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        byte[] camerasContentsFromFile = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(camerasContentsFromFile);
//        Files.write(Paths.get("./Cameras.written.bin"), actualOutputStream.toByteArray(), StandardOpenOption.CREATE);
    }
}
