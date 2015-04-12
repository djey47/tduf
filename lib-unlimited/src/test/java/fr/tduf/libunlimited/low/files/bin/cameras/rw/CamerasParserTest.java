package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class CamerasParserTest {

    @Test
    public void parse_whenRealFiles_shouldLoadCamerasContents_andFillCaches() throws URISyntaxException, IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = getCamerasInputStreamFromFile();


        // WHEN
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();


        // THEN
        assertThat(camerasParser).isNotNull();
        assertThat(camerasParser.getCameraIndex()).hasSize(151);
        assertThat(camerasParser.getCameraViews()).hasSize(149);
        assertThat(camerasParser.getTotalViewCount()).isEqualTo(595);

        assertThat(camerasParser.getCachedCameraIndex()).isNotEmpty();
        assertThat(camerasParser.getCachedCameraViews()).isNotEmpty();
        assertThat(camerasParser.getCachedTotalViewCount()).isNotNull();
    }

    @Test
    public void flushCaches_shouldNullifyAllCaches() throws IOException, URISyntaxException {
        // GIVEN
        CamerasParser camerasParser = CamerasParser.load(getCamerasInputStreamFromFile());
        camerasParser.parse();
        camerasParser.getCameraViews();
        camerasParser.getCameraIndex();

        // WHEN
        camerasParser.flushCaches();

        // THEN
        assertThat(camerasParser.getCachedCameraViews()).isNull();
        assertThat(camerasParser.getCachedCameraIndex()).isNull();
        assertThat(camerasParser.getCachedTotalViewCount()).isNull();
    }

    private static ByteArrayInputStream getCamerasInputStreamFromFile() throws URISyntaxException, IOException {
        byte[] camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        return new ByteArrayInputStream(camContents);
    }
}