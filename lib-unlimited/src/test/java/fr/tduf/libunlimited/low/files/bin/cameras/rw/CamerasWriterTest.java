package fr.tduf.libunlimited.low.files.bin.cameras.rw;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CamerasWriterTest {
    @Test
    void load_shouldReturnWriterInstance() throws IOException {
        // GIVEN
        CamerasDatabase cameraInfo = CamerasDatabase.builder().build();

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(cameraInfo);

        // THEN
        assertThat(actualWriter).isNotNull();
    }

    @Test
    void write_shouldReturnOriginalContentsBack() throws IOException {
        // To regen json input file:
        // FileTool jsonify -i [..]/tduf/lib-testing/src/main/resources/bin/Cameras.bin -s [..]/tduf/lib-unlimited/src/main/resources/files/structures/BIN-cameras-map.json

        // GIVEN
        String camerasContentsFromJSONFile = FilesHelper.readTextFromResourceFile("/bin/Cameras.bin.json");
        CamerasParser camerasParser = CamerasParser.load(new XByteArrayInputStream(new byte[0]));
        camerasParser.getDataStore().fromJsonString(camerasContentsFromJSONFile);
        CamerasDatabase cameraInfo = camerasParser.generate();

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(cameraInfo);
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        byte[] expectedcamerasContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedcamerasContents);
    }
}
