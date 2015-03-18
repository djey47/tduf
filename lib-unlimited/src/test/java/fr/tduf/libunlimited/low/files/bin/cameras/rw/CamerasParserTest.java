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
    public void parse_whenRealFiles_shouldLoadCamerasContents() throws URISyntaxException, IOException {
        // GIVEN
        URI uri = thisClass.getResource("/bin/Cameras.bin").toURI();
        byte[] mapContents = Files.readAllBytes(Paths.get(uri));
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(mapContents);

        // WHEN
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();

        // THEN
        assertThat(camerasParser).isNotNull();
        assertThat(camerasParser.getCameraIndex()).hasSize(151);
        assertThat(camerasParser.getCameraViews()).hasSize(149);
    }
}