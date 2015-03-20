package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CamerasHelperTest {

    private static final Class<CamerasHelperTest> thisClass = CamerasHelperTest.class;

    private CamerasParser parser;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        URI uri = thisClass.getResource("/bin/Cameras.bin").toURI();
        byte[] camContents = Files.readAllBytes(Paths.get(uri));
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);

        parser = CamerasParser.load(camInputStream);
        parser.parse();
    }

    @Test(expected = NullPointerException.class)
    public void duplicateCameraSet_whenNullParser_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 1001, null);

        // THEN: NPE
    }

    @Test
    public void duplicateCameraSet_whenSourceExists_shouldAddSet() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 10001, parser);

        // THEN
        assertThat(parser.getDataStore().getInteger("indexSize").get()).isEqualTo(152);
        assertThat(parser.getCameraIndex()).hasSize(152);
        assertThat(parser.getCameraViews()).hasSize(150);
    }
}