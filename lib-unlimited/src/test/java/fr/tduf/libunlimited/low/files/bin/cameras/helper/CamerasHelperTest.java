package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class CamerasHelperTest {

    private CamerasParser parser;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        byte[] camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
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
        CamerasHelper.duplicateCameraSet(1, 401, parser);

        // THEN
        assertThat(parser.getDataStore().getInteger("indexSize").get()).isEqualTo(151);
        assertThat(parser.getCameraIndex()).hasSize(151);   // 150 -> 151
        assertThat(parser.getCameraViews()).hasSize(149);   // 148 -> 149 : 2 camera entries are genuinely missing (9997 and ?!)
        assertThat(parser.getTotalViewCount()).isEqualTo(595); // 591 -> 595
    }

    @Test(expected = NullPointerException.class)
    public void duplicateAllCameraSets_whenNullParser_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateAllCameraSets(1001, null);

        // THEN: NPE
    }

    @Test
    @Ignore //Test trop long, à voir sur un plus petit fichier. Couvert par le test d'intégration
    public void duplicateAllCameraSets_whenSourceExists_shouldCloneAllSets() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateAllCameraSets(10000, parser);

        // THEN
        assertThat(parser.getDataStore().getInteger("indexSize").get()).isEqualTo(298);
        assertThat(parser.getCameraIndex()).hasSize(298);
        assertThat(parser.getCameraViews()).hasSize(296);
        assertThat(parser.getTotalViewCount()).isEqualTo(1186);
    }
}
