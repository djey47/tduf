package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.SetConfigurationDto;
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
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CamerasHelperTest {

    private static byte[] camContents;
    private static CamerasParser readOnlyParser;

    private CamerasDatabase camerasDatabase;

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
        camerasDatabase = readOnlyParser.parse();
    }

    @Test
    void duplicateCameraSet_whenNullInfo_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> CamerasHelper.duplicateCameraSet(1, 1001, null));
    }

    @Test
    void duplicateCameraSet_whenSourceDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.duplicateCameraSet(0, 401, camerasDatabase));
    }

    @Test
    void duplicateCameraSet_whenSourceExists_shouldAddSet() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 401, camerasDatabase);

        // THEN
        assertThat(camerasDatabase.getIndexSize()).isEqualTo(151);   // 150 -> 151
        assertThat(camerasDatabase.cameraSetExistsInIndex(401)).isTrue();
        assertThat(camerasDatabase.getSetsCount()).isEqualTo(149);   // 148 -> 149 : 2 camera entries are genuinely missing (9997 and ?!)
        assertThat(camerasDatabase.getViewsForCameraSet(401)).hasSize(4);
        assertThat(camerasDatabase.getTotalViewCount()).isEqualTo(595); // 591 -> 595
    }

    @Test
    void duplicateCameraSet_whenSourceAndTargetExist_shouldDoNothing() throws Exception {
        // GIVEN-WHEN
        CamerasHelper.duplicateCameraSet(1, 15, camerasDatabase);

        // THEN
        assertThat(camerasDatabase.getIndexSize()).isEqualTo(150);
        assertThat(camerasDatabase.getSetsCount()).isEqualTo(148);
        assertThat(camerasDatabase.getTotalViewCount()).isEqualTo(591);
    }

    @Test
    void batchDuplicateCameraSets_whenSourceExist_shouldAddSets() throws Exception {
        // GIVEN-WHEN
        List<String> instructions = asList ("1;401", "1;402", "1;403");
        CamerasHelper.batchDuplicateCameraSets(instructions, camerasDatabase);

        // THEN
        assertThat(camerasDatabase.getIndexSize()).isEqualTo(153);
        assertThat(camerasDatabase.cameraSetExistsInIndex(401));
        assertThat(camerasDatabase.cameraSetExistsInIndex(402));
        assertThat(camerasDatabase.cameraSetExistsInIndex(403));
        assertThat(camerasDatabase.getSetsCount()).isEqualTo(151);
        assertThat(camerasDatabase.getViewsForCameraSet(401)).hasSize(4);
        assertThat(camerasDatabase.getViewsForCameraSet(402)).hasSize(4);
        assertThat(camerasDatabase.getViewsForCameraSet(403)).hasSize(4);
        assertThat(camerasDatabase.getTotalViewCount()).isEqualTo(603);
    }

    @Test
    void fetchInformation_whenCameraDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchInformation(0, camerasDatabase));
    }

    @Test
    void fetchInformation_whenCameraExists_shouldReturnViews() {
        // GIVEN
        int cameraIdentifier = 1000;

        // WHEN
        CameraSetInfo cameraSetInfo = CamerasHelper.fetchInformation(cameraIdentifier, camerasDatabase);

        // THEN
        assertThat(cameraSetInfo.getCameraIdentifier()).isEqualTo((int)cameraIdentifier);
        assertThat(cameraSetInfo.getViews()).hasSize(4);
        assertThat(cameraSetInfo.getViews())
                .extracting("kind")
                .contains(
                        Cockpit,
                        Cockpit_Back,
                        Hood,
                        Hood_Back);
    }

    @Test
    void fetchAllInformation_shouldReturnAllCameras() {
        // GIVEN-WHEN
        List<CameraSetInfo> cameraSetInfos = CamerasHelper.fetchAllInformation(camerasDatabase);

        // THEN
        assertThat(cameraSetInfos).hasSize(148);
    }

    @Test
    void fetchViewProperties_whenViewDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchViewProperties(1000, Follow_Large_Back, camerasDatabase));
    }

    @Test
    void fetchViewProperties_whenViewExists_shouldReturnProps() {
        // GIVEN-WHEN
        EnumMap<ViewProps, ?> actualProperties = CamerasHelper.fetchViewProperties(1000, Cockpit, camerasDatabase);

        // THEN
        assertThat(actualProperties).hasSize(ViewProps.values().length);
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

        SetConfigurationDto configuration = SetConfigurationDto.builder()
                .forIdentifier(1000)
                .addView(CameraView.from(Hood, 101, Hood))
                .addView(CameraView.from(Cockpit_Back, 101, Cockpit_Back))
                .build();

        CameraSetInfo cameraInfoFromModdingTools = CameraSetInfo.builder()
                .forIdentifier(1000)
                .addView(CameraView.from(Hood, 101, Hood))
                .addView(CameraView.from(Cockpit_Back, 101, Cockpit_Back))
                .addView(CameraView.from(Hood_Back, 0, Unknown))
                .addView(CameraView.from(Cockpit, 0, Unknown))
                .build();
        when(cameraSupportMock.getCameraInfo(camFile, 1000)).thenReturn(cameraInfoFromModdingTools);


        // WHEN
        CameraSetInfo actualCameraInfo = CamerasHelper.useViews(configuration, camFile);


        // THEN
        ArgumentCaptor<GenuineCamViewsDto> argumentCaptor = ArgumentCaptor.forClass(GenuineCamViewsDto.class);
        verify(cameraSupportMock).customizeCamera(eq(camFile), eq(1000), argumentCaptor.capture());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViewsParameters = argumentCaptor.getValue().getViews();
        assertThat(actualViewsParameters).hasSize(2);
        assertThat(actualViewsParameters).extracting("viewType").containsExactly(ViewKind.Hood, ViewKind.Cockpit_Back);
        assertThat(actualViewsParameters).extracting("cameraId").containsOnly(101);
        assertThat(actualViewsParameters).extracting("viewId").containsExactly(24, 43);

        Map<ViewKind, CameraView> viewsByType = actualCameraInfo.getViewsByKind();
        CameraView hoodBackView = viewsByType.get(Hood_Back);
        assertThat(hoodBackView.getUsedCameraSetId()).isNull();
        assertThat(hoodBackView.getUsedKind()).isNull();
        CameraView cockpitView = viewsByType.get(Cockpit);
        assertThat(cockpitView.getUsedCameraSetId()).isNull();
        assertThat(cockpitView.getUsedKind()).isNull();
        CameraView hoodView = viewsByType.get(Hood);
        assertThat(hoodView.getUsedCameraSetId()).isEqualTo(101);
        assertThat(hoodView.getUsedKind()).isEqualTo(Hood);
        CameraView cockpitBackView = viewsByType.get(Cockpit_Back);
        assertThat(cockpitBackView.getUsedCameraSetId()).isEqualTo(101);
        assertThat(cockpitBackView.getUsedKind()).isEqualTo(Cockpit_Back);
    }

    @Test
    void cameraSetExists_whenIdNotInIndexNorInSettings_shouldReturnFalse() {
        // given
        Map<Integer, Short> index = new HashMap<>(0);
        Map<Integer, List<CameraView>> views = new HashMap<>(0);
        CamerasDatabase cameraInfo = CamerasDatabase.builder()
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
        Map<Integer, List<CameraView>> views = new HashMap<>(1);
        views.put(100, new ArrayList<>(0));
        CamerasDatabase cameraInfo = CamerasDatabase.builder()
                .withIndex(index)
                .withViews(views)
                .build();

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100, cameraInfo)).isTrue();
    }

    @Test
    void cameraSetExists_whenIdInSettingsOnly_shouldReturnFalse() {
        // given
        Map<Integer, List<CameraView>> views = new HashMap<>(1);
        views.put(100, new ArrayList<>(0));
        CamerasDatabase cameraInfo = CamerasDatabase.builder()
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
        CamerasDatabase cameraInfo = CamerasDatabase.builder()
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
                () -> CamerasHelper.updateViews(null, camerasDatabase));
    }

    @Test
    void updateViews_whenEmptyConfiguration_shouldThrowException() throws IOException {
        // GIVEN
        SetConfigurationDto configuration = SetConfigurationDto.builder().build();

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> CamerasHelper.updateViews(configuration, camerasDatabase));
    }

    @Test
    void updateViews_whenCameraDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN
        CameraView cameraView = CameraView.from(Bumper, 0, Bumper);
        SetConfigurationDto configuration = SetConfigurationDto.builder()
                .forIdentifier(0)
                .addView(cameraView)
                .build();

        // WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.updateViews(configuration, camerasDatabase));
    }

    @Test
    void updateViews_whenCameraExists_butViewDoesNot_shouldDoNothing() throws Exception {
        // GIVEN
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(BINOCULARS, 0L);

        CameraView cameraView = CameraView.fromProps(viewProps, Follow_Far);
        SetConfigurationDto configuration = SetConfigurationDto.builder()
                .forIdentifier(1000)
                .addView(cameraView)
                .build();

        // WHEN
        CamerasHelper.updateViews(configuration, camerasDatabase);

        // THEN
        Map<ViewKind, CameraView> viewsByKind = camerasDatabase.getViewsByKindForCameraSet(1000);
        assertThat(viewsByKind.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Hood).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByKind.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
    }

    @Test
    void updateViews_whenCameraExists_andViewExists_shouldUpdateSettings_andOriginalStore() throws Exception {
        // GIVEN
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(BINOCULARS, 0L);

        CameraView cameraView = CameraView.fromProps(viewProps, Hood);
        SetConfigurationDto configuration = SetConfigurationDto.builder()
                .forIdentifier(1000)
                .addView(cameraView)
                .build();

        // WHEN
        CamerasHelper.updateViews(configuration, camerasDatabase);

        // THEN
        Map<ViewKind, CameraView> viewsByType = camerasDatabase.getViewsByKindForCameraSet(1000);
        assertThat(viewsByType.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Hood).getSettings().get(BINOCULARS)).isEqualTo(0L);
        assertThat(viewsByType.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
    }
}
