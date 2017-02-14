package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Hood_Back;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CamerasHelper_focusOnEnhancedCamerasTest {

    private static CamerasParser readOnlyParser;

    private CameraInfoEnhanced cameraInfoEnhanced;

    private GenuineCamGateway cameraSupportMock = mock(GenuineCamGateway.class);

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        byte[] camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");

        try (ByteArrayInputStream cameraInputStream = new ByteArrayInputStream(camContents)) {
            readOnlyParser = CamerasParser.load(cameraInputStream);
        }
    }

    @BeforeEach
    void setUp() {
        CamerasHelper.setCameraSupport(cameraSupportMock);
        cameraInfoEnhanced = readOnlyParser.parse();
    }

    @Test
    void duplicateCameraSet_whenNullInfo_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> CamerasHelper.duplicateCameraSet(1, 1001, (CameraInfoEnhanced)null));
    }

    @Test
    void duplicateCameraSet_whenSourceDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.duplicateCameraSet(0, 401, cameraInfoEnhanced));
    }

    @Test
    void duplicateCameraSet_whenSourceExists_shouldAddSet() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 401, cameraInfoEnhanced);

        // THEN
        assertThat(cameraInfoEnhanced.getIndexSize()).isEqualTo(151);   // 150 -> 151
        assertThat(cameraInfoEnhanced.cameraSetExistsInIndex(401)).isTrue();
        assertThat(cameraInfoEnhanced.getSetsCount()).isEqualTo(149);   // 148 -> 149 : 2 camera entries are genuinely missing (9997 and ?!)
        assertThat(cameraInfoEnhanced.getViewsForCameraSet(401)).hasSize(4);
        assertThat(cameraInfoEnhanced.getTotalViewCount()).isEqualTo(595); // 591 -> 595
    }

    @Test
    void duplicateCameraSet_whenSourceAndTargetExist_shouldDoNothing() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 15, cameraInfoEnhanced);

        // THEN
        assertThat(cameraInfoEnhanced.getIndexSize()).isEqualTo(150);
        assertThat(cameraInfoEnhanced.getSetsCount()).isEqualTo(148);
        assertThat(cameraInfoEnhanced.getTotalViewCount()).isEqualTo(591);
    }

    @Test
    void batchDuplicateCameraSets_whenSourceExist_shouldAddSets() throws Exception {
        // GIVEN-WHEN
        List<String> instructions = asList ("1;401", "1;402", "1;403");
        CamerasHelper.batchDuplicateCameraSets(instructions, cameraInfoEnhanced);

        // THEN
        assertThat(cameraInfoEnhanced.getIndexSize()).isEqualTo(153);
        assertThat(cameraInfoEnhanced.cameraSetExistsInIndex(401));
        assertThat(cameraInfoEnhanced.cameraSetExistsInIndex(402));
        assertThat(cameraInfoEnhanced.cameraSetExistsInIndex(403));
        assertThat(cameraInfoEnhanced.getSetsCount()).isEqualTo(151);
        assertThat(cameraInfoEnhanced.getViewsForCameraSet(401)).hasSize(4);
        assertThat(cameraInfoEnhanced.getViewsForCameraSet(402)).hasSize(4);
        assertThat(cameraInfoEnhanced.getViewsForCameraSet(403)).hasSize(4);
        assertThat(cameraInfoEnhanced.getTotalViewCount()).isEqualTo(603);
    }

    @Test
    void fetchInformation_whenCameraDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchInformation(0, cameraInfoEnhanced));
    }

    @Test
    void fetchInformation_whenCameraExists_shouldReturnViews() {
        // GIVEN
        int cameraIdentifier = 1000;

        // WHEN
        CameraInfo cameraInfo = CamerasHelper.fetchInformation(cameraIdentifier, cameraInfoEnhanced);

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

    @Test
    void fetchAllInformation_shouldReturnAllCameras() {
        // GIVEN-WHEN
        List<CameraInfo> cameraInfos = CamerasHelper.fetchAllInformation(cameraInfoEnhanced);

        // THEN
        assertThat(cameraInfos).hasSize(148);
    }

    @Test
    void fetchViewProperties_whenViewDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchViewProperties(1000, Follow_Large_Back, cameraInfoEnhanced));
    }

    @Test
    void fetchViewProperties_whenViewExists_shouldReturnProps() {
        // GIVEN-WHEN
        EnumMap<ViewProps, ?> actualProperties = CamerasHelper.fetchViewProperties(1000, Cockpit, cameraInfoEnhanced);

        // THEN
        assertThat(actualProperties).hasSize(15);
        assertThat(actualProperties.keySet()).containsExactlyInAnyOrder(ViewProps.values());
        assertThat(actualProperties.values()).doesNotContainNull();
    }

}
