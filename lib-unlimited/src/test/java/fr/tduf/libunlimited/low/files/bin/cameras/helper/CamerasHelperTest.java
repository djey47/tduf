package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.expectThrows;

class CamerasHelperTest {

    private static byte[] camContents;

    private CamerasParser parser;

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
    }

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        try (ByteArrayInputStream cameraInputStream = new ByteArrayInputStream(camContents)) {
            parser = CamerasParser.load(cameraInputStream);
            parser.parse();
        }
    }

    @Test
    void duplicateCameraSet_whenNullParser_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN-THEN
        expectThrows(NullPointerException.class,
                () -> CamerasHelper.duplicateCameraSet(1, 1001, null));
    }

    @Test
    void duplicateCameraSet_whenSourceDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        expectThrows(IllegalArgumentException.class,
                () -> CamerasHelper.duplicateCameraSet(0, 401, parser));
    }

    @Test
    void duplicateCameraSet_whenSourceExists_shouldAddSet() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 401, parser);

        // THEN
        assertThat(parser.getCameraIndex()).hasSize(151);   // 150 -> 151
        assertThat(parser.getCameraIndex()).containsKey(401L);
        assertThat(parser.getCameraViews()).hasSize(149);   // 148 -> 149 : 2 camera entries are genuinely missing (9997 and ?!)
        assertThat(parser.getCameraViews().get(401L)).hasSize(4);
        assertThat(parser.getTotalViewCount()).isEqualTo(595); // 591 -> 595
    }

    @Test
    void duplicateCameraSet_whenSourceAndTargetExist_shouldDoNothing() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 15, parser);

        // THEN
        assertThat(parser.getCameraIndex()).hasSize(150);
        assertThat(parser.getCameraViews()).hasSize(148);
        assertThat(parser.getTotalViewCount()).isEqualTo(591);
    }

    @Test
    void batchDuplicateCameraSets_whenSourceExist_shouldAddSets() throws Exception {
        // GIVEN-WHEN
        List<String> instructions = asList ("1;401", "1;402", "1;403");
        CamerasHelper.batchDuplicateCameraSets(instructions, parser);

        // THEN
        assertThat(parser.getCameraIndex()).hasSize(153);
        assertThat(parser.getCameraIndex()).containsKey(401L);
        assertThat(parser.getCameraIndex()).containsKey(402L);
        assertThat(parser.getCameraIndex()).containsKey(403L);
        assertThat(parser.getCameraViews()).hasSize(151);
        assertThat(parser.getCameraViews().get(401L)).hasSize(4);
        assertThat(parser.getCameraViews().get(402L)).hasSize(4);
        assertThat(parser.getCameraViews().get(403L)).hasSize(4);
        assertThat(parser.getTotalViewCount()).isEqualTo(603);
    }
}
