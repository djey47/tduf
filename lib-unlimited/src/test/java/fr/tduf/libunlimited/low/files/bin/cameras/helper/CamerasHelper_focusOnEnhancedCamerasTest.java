package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.BINOCULARS;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CamerasHelper_focusOnEnhancedCamerasTest {

    private static byte[] camContents;
    private static CamerasParser readOnlyParser;

    private CameraInfoEnhanced cameraInfoEnhanced;

    private GenuineCamGateway cameraSupportMock = mock(GenuineCamGateway.class);

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");

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

    @Test
    void useViews_whenCameraExists_andViewExists_shouldUpdateSettings() throws Exception {
        // GIVEN
        String tempDirectory = fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForLibrary();
        Path camFilePath = Paths.get(tempDirectory, "cameras.bin");
        String camFile = camFilePath.toString();
        Files.write(camFilePath, camContents, StandardOpenOption.CREATE);

        CameraInfo configuration = CameraInfo.builder()
                .forIdentifier(1000)
                .addView(CameraInfo.CameraView.from(Hood, 101, Hood))
                .addView(CameraInfo.CameraView.from(Cockpit_Back, 101, Cockpit_Back))
                .build();

        CameraInfo cameraInfoFromModdingTools = CameraInfo.builder()
                .forIdentifier(1000L)
                .addView(CameraInfo.CameraView.from(Hood, 101L, Hood))
                .addView(CameraInfo.CameraView.from(Cockpit_Back, 101L, Cockpit_Back))
                .addView(CameraInfo.CameraView.from(Hood_Back, 0, Unknown))
                .addView(CameraInfo.CameraView.from(Cockpit, 0, Unknown))
                .build();
        when(cameraSupportMock.getCameraInfo(camFile, 1000L)).thenReturn(cameraInfoFromModdingTools);


        // WHEN
        CameraInfo actualCameraInfo = CamerasHelper.useViews(configuration, camFile);


        // THEN
        ArgumentCaptor<GenuineCamViewsDto> argumentCaptor = ArgumentCaptor.forClass(GenuineCamViewsDto.class);
        verify(cameraSupportMock).customizeCamera(eq(camFile), eq(1000L), argumentCaptor.capture());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViewsParameters = argumentCaptor.getValue().getViews();
        assertThat(actualViewsParameters).hasSize(2);
        assertThat(actualViewsParameters).extracting("viewType").containsExactly(ViewKind.Hood, ViewKind.Cockpit_Back);
        assertThat(actualViewsParameters).extracting("cameraId").containsOnly(101L);
        assertThat(actualViewsParameters).extracting("viewId").containsExactly(24, 43);

        Map<ViewKind, CameraInfo.CameraView> viewsByType = actualCameraInfo.getViewsByKind();
        CameraInfo.CameraView hoodBackView = viewsByType.get(Hood_Back);
        assertThat(hoodBackView.getSourceCameraIdentifier()).isEqualTo(0);
        assertThat(hoodBackView.getSourceType()).isNull();
        CameraInfo.CameraView cockpitView = viewsByType.get(Cockpit);
        assertThat(cockpitView.getSourceCameraIdentifier()).isEqualTo(0);
        assertThat(cockpitView.getSourceType()).isNull();
        CameraInfo.CameraView hoodView = viewsByType.get(Hood);
        assertThat(hoodView.getSourceCameraIdentifier()).isEqualTo(101L);
        assertThat(hoodView.getSourceType()).isEqualTo(Hood);
        CameraInfo.CameraView cockpitBackView = viewsByType.get(Cockpit_Back);
        assertThat(cockpitBackView.getSourceCameraIdentifier()).isEqualTo(101L);
        assertThat(cockpitBackView.getSourceType()).isEqualTo(Cockpit_Back);
    }

    @Test
    void cameraSetExists_whenIdNotInIndexNorInSettings_shouldReturnFalse() {
        // given
        Map<Integer, Short> index = new HashMap<>(0);
        Map<Integer, List<CameraViewEnhanced>> views = new HashMap<>(0);
        CameraInfoEnhanced cameraInfo = CameraInfoEnhanced.builder()
                .withIndex(index)
                .withViews(views)
                .build();

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100, cameraInfo)).isFalse();
    }

    @Test
    void cameraSetExists_whenIdInIndexAndInSettings_shouldReturnTrue() {
        // given
        Map<Integer, Short> index = new HashMap<>(1);
        index.put(100, (short) 4);
        Map<Integer, List<CameraViewEnhanced>> views = new HashMap<>(1);
        views.put(100, new ArrayList<>(0));
        CameraInfoEnhanced cameraInfo = CameraInfoEnhanced.builder()
                .withIndex(index)
                .withViews(views)
                .build();

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100, cameraInfo)).isTrue();
    }

    @Test
    void cameraSetExists_whenIdInSettingsOnly_shouldReturnFalse() {
        // given
        Map<Integer, List<CameraViewEnhanced>> views = new HashMap<>(1);
        views.put(100, new ArrayList<>(0));
        CameraInfoEnhanced cameraInfo = CameraInfoEnhanced.builder()
                .withIndex(new LinkedHashMap<>(0))
                .withViews(views)
                .build();

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100, cameraInfo)).isFalse();
    }

    @Test
    void cameraSetExists_whenIdInIndexOnly_shouldReturnFalse() {
        // given
        Map<Integer, Short> index = new HashMap<>(1);
        index.put(100, (short) 4);
        CameraInfoEnhanced cameraInfo = CameraInfoEnhanced.builder()
                .withIndex(index)
                .withViews(new LinkedHashMap<>(0))
                .build();

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100, cameraInfo)).isFalse();
    }

    @Test
    void updateViews_whenNullConfiguration_shouldThrowException() throws IOException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> CamerasHelper.updateViews(null, cameraInfoEnhanced));
    }

    @Test
    void updateViews_whenEmptyConfiguration_shouldThrowException() throws IOException {
        // GIVEN
        CameraInfo configuration = CameraInfo.builder().build();

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> CamerasHelper.updateViews(configuration, cameraInfoEnhanced));
    }

    @Test
    void updateViews_whenCameraDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN
        CameraInfo.CameraView cameraView = CameraInfo.CameraView.from(Bumper, 0, Bumper);
        CameraInfo configuration = CameraInfo.builder()
                .forIdentifier(0)
                .addView(cameraView)
                .build();

        // WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.updateViews(configuration, cameraInfoEnhanced));
    }

    @Test
    void updateViews_whenCameraExists_butViewDoesNot_shouldDoNothing() throws Exception {
        // GIVEN
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(TYPE, Follow_Far);
        viewProps.put(BINOCULARS, 0L);

        CameraInfo.CameraView cameraView = CameraInfo.CameraView.fromProps(viewProps);
        CameraInfo configuration = CameraInfo.builder()
                .forIdentifier(1000)
                .addView(cameraView)
                .build();

        // WHEN
        CamerasHelper.updateViews(configuration, cameraInfoEnhanced);

        // THEN
        Map<ViewKind, CameraViewEnhanced> viewsByKind = cameraInfoEnhanced.getViewsByKindForCameraSet(1000);
        assertThat(viewsByKind.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Hood).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
    }

    @Test
    void updateViews_whenCameraExists_andViewExists_shouldUpdateSettings_andOriginalStore() throws Exception {
        // GIVEN
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(TYPE, Hood);
        viewProps.put(BINOCULARS, 0L);

        CameraInfo.CameraView cameraView = CameraInfo.CameraView.fromProps(viewProps);
        CameraInfo configuration = CameraInfo.builder()
                .forIdentifier(1000)
                .addView(cameraView)
                .build();

        // WHEN
        CamerasHelper.updateViews(configuration, cameraInfoEnhanced);

        // THEN
        Map<ViewKind, CameraViewEnhanced> viewsByType = cameraInfoEnhanced.getViewsByKindForCameraSet(1000);
        assertThat(viewsByType.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Hood).getSettings().get(BINOCULARS)).isEqualTo(0L);
        assertThat(viewsByType.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
    }
}
