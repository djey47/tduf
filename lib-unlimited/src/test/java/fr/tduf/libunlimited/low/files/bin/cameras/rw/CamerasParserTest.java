package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CamerasParserTest {

    private static final Class<CamerasParserTest> thisClass = CamerasParserTest.class;

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

        assertThat(camerasParser.getCachedCameraIndex()).isNotEmpty();
        assertThat(camerasParser.getCachedCameraViews()).isNotEmpty();
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
    }

    private static ByteArrayInputStream getCamerasInputStreamFromFile() throws URISyntaxException, IOException {
        URI uri = thisClass.getResource("/bin/Cameras.bin").toURI();
        byte[] camContents = Files.readAllBytes(Paths.get(uri));
        return new ByteArrayInputStream(camContents);
    }
}