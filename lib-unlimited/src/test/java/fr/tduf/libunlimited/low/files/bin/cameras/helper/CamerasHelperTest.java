package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto.GenuineCamViewDto.Type.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.expectThrows;

class CamerasHelperTest {

    private static byte[] camContents;
    private static CamerasParser readOnlyParser;

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        try (ByteArrayInputStream cameraInputStream = new ByteArrayInputStream(camContents)) {
            readOnlyParser = CamerasParser.load(cameraInputStream);
            readOnlyParser.parse();
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
        expectThrows(NoSuchElementException.class,
                () -> CamerasHelper.duplicateCameraSet(0, 401, readOnlyParser));
    }

    @Test
    void duplicateCameraSet_whenSourceExists_shouldAddSet() throws Exception {
        // GIVEN-WHEN
        CamerasParser readWriteParser = getReadWriteParser();
        CamerasHelper.duplicateCameraSet(1, 401, readWriteParser);

        // THEN
        assertThat(readWriteParser.getCameraIndex()).hasSize(151);   // 150 -> 151
        assertThat(readWriteParser.getCameraIndex()).containsKey(401L);
        assertThat(readWriteParser.getCameraViews()).hasSize(149);   // 148 -> 149 : 2 camera entries are genuinely missing (9997 and ?!)
        assertThat(readWriteParser.getCameraViews().get(401L)).hasSize(4);
        assertThat(readWriteParser.getTotalViewCount()).isEqualTo(595); // 591 -> 595
    }

    @Test
    void duplicateCameraSet_whenSourceAndTargetExist_shouldDoNothing() throws Exception {
        // GIVEN-WHEN
        CamerasParser readWriteParser = getReadWriteParser();
        CamerasHelper.duplicateCameraSet(1, 15, readWriteParser);

        // THEN
        assertThat(readWriteParser.getCameraIndex()).hasSize(150);
        assertThat(readWriteParser.getCameraViews()).hasSize(148);
        assertThat(readWriteParser.getTotalViewCount()).isEqualTo(591);
    }

    @Test
    void batchDuplicateCameraSets_whenSourceExist_shouldAddSets() throws Exception {
        // GIVEN-WHEN
        List<String> instructions = asList ("1;401", "1;402", "1;403");
        CamerasParser readWriteParser = getReadWriteParser();
        CamerasHelper.batchDuplicateCameraSets(instructions, readWriteParser);

        // THEN
        assertThat(readWriteParser.getCameraIndex()).hasSize(153);
        assertThat(readWriteParser.getCameraIndex()).containsKey(401L);
        assertThat(readWriteParser.getCameraIndex()).containsKey(402L);
        assertThat(readWriteParser.getCameraIndex()).containsKey(403L);
        assertThat(readWriteParser.getCameraViews()).hasSize(151);
        assertThat(readWriteParser.getCameraViews().get(401L)).hasSize(4);
        assertThat(readWriteParser.getCameraViews().get(402L)).hasSize(4);
        assertThat(readWriteParser.getCameraViews().get(403L)).hasSize(4);
        assertThat(readWriteParser.getTotalViewCount()).isEqualTo(603);
    }

    @Test
    void fetchInformation_whenCameraDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        expectThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchInformation(0, readOnlyParser));
    }

    @Test
    void fetchInformation_whenCameraExists_shouldReturnViews() {
        // GIVEN
        long cameraIdentifier = 1000;

        // WHEN
        CameraInfo cameraInfo = CamerasHelper.fetchInformation(cameraIdentifier, readOnlyParser);

        // THEN
        assertThat(cameraInfo.getCameraIdentifier()).isEqualTo((int)cameraIdentifier);
        assertThat(cameraInfo.getViews()).hasSize(4);
        assertThat(cameraInfo.getViews())
                .extracting("type")
                .contains(
                    Cockpit,
                    Cockpit_Back,
                    Hood,
                    Hood_Back);
    }

    private static CamerasParser getReadWriteParser() throws IOException {
        CamerasParser parser;
        try (ByteArrayInputStream cameraInputStream = new ByteArrayInputStream(camContents)) {
            parser = CamerasParser.load(cameraInputStream);
            parser.parse();
        }
        return parser;
    }
}
