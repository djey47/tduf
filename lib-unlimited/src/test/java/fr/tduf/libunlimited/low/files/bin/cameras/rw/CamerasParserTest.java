package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;


class CamerasParserTest {

    private static byte[] camContents;

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
    }

    @Test
    void parse_whenRealFiles_shouldLoadCamerasContents_andFillCaches() throws URISyntaxException, IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);


        // WHEN
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();


        // THEN
        assertThat(camerasParser).isNotNull();
        assertThat(camerasParser.getCameraIndex()).hasSize(150);
        assertThat(camerasParser.getCameraViews()).hasSize(148);
        assertThat(camerasParser.getTotalViewCount()).isEqualTo(591);

        assertThat(camerasParser.getCachedCameraIndex()).isNotEmpty();
        assertThat(camerasParser.getCachedCameraViews()).isNotEmpty();
        assertThat(camerasParser.getCachedTotalViewCount()).isNotNull();
    }

    @Test
    void flushCaches_shouldNullifyAllCaches() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
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
}
