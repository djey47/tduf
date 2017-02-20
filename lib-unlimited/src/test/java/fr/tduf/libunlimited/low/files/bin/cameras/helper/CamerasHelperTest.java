package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
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

class CamerasHelperTest {

    private static byte[] camContents;
    private static CamerasParser readOnlyParser;

    private GenuineCamGateway cameraSupportMock = mock(GenuineCamGateway.class);

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");

        try (ByteArrayInputStream cameraInputStream = new ByteArrayInputStream(camContents)) {
            readOnlyParser = CamerasParser.load(cameraInputStream);
            readOnlyParser.parse();
        }
    }

    @BeforeEach
    void setUp() {
        CamerasHelper.setCameraSupport(cameraSupportMock);
    }

    @Test
    void duplicateCameraSet_whenNullParser_shouldThrowNullPointerException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> CamerasHelper.duplicateCameraSet(1L, 1001L, null));
    }

    @Test
    void duplicateCameraSet_whenSourceDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
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
        assertThrows(NoSuchElementException.class,
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

    @Test
    void fetchAllInformation_shouldReturnAllCameras() {
        // GIVEN-WHEN
        List<CameraInfo> cameraInfos = CamerasHelper.fetchAllInformation(readOnlyParser);

        // THEN
        assertThat(cameraInfos).hasSize(148);
    }

    @Test
    void fetchViewProperties_whenViewDoesNotExist_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NoSuchElementException.class,
                () -> CamerasHelper.fetchViewProperties(1000, Follow_Large_Back, readOnlyParser));
    }

    @Test
    void fetchViewProperties_whenViewExists_shouldReturnProps() {
        // GIVEN
        long cameraIdentifier = 1000;

        // WHEN
        EnumMap<ViewProps, ?> actualProperties = CamerasHelper.fetchViewProperties(cameraIdentifier, Cockpit, readOnlyParser);

        // THEN
        assertThat(actualProperties).hasSize(15);
        assertThat(actualProperties.keySet()).containsExactlyInAnyOrder(ViewProps.values());
        assertThat(actualProperties.values()).doesNotContainNull();
    }

    @Test
    void updateViews_whenNullConfiguration_shouldThrowException() throws IOException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> CamerasHelper.updateViews(null, readOnlyParser));
    }

    @Test
    void updateViews_whenEmptyConfiguration_shouldThrowException() throws IOException {
        // GIVEN
        CameraInfo configuration = CameraInfo.builder().build();

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> CamerasHelper.updateViews(configuration, readOnlyParser));
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
                () -> CamerasHelper.updateViews(configuration, readOnlyParser));
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
        CameraInfo actualCameraInfo = CamerasHelper.updateViews(configuration, readOnlyParser);

        // THEN
        Map<ViewKind, CameraInfo.CameraView> viewsByType = actualCameraInfo.getViewsByKind();
        assertThat(viewsByType.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Hood).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
    }

    @Test
    void updateViews_whenCameraExists_andViewExists_shouldUpdateSettings_andOriginalStore() throws Exception {
        // GIVEN
        CamerasParser readWriteParser = getReadWriteParser();
        EnumMap<ViewProps, Object> viewProps = new EnumMap<>(ViewProps.class);
        viewProps.put(TYPE, Hood);
        viewProps.put(BINOCULARS, 0L);

        CameraInfo.CameraView cameraView = CameraInfo.CameraView.fromProps(viewProps);
        CameraInfo configuration = CameraInfo.builder()
                .forIdentifier(1000)
                .addView(cameraView)
                .build();

        // WHEN
        CameraInfo actualCameraInfo = CamerasHelper.updateViews(configuration, readWriteParser);

        // THEN
        Map<ViewKind, CameraInfo.CameraView> viewsByType = actualCameraInfo.getViewsByKind();
        assertThat(viewsByType.get(Cockpit).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Cockpit_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);
        assertThat(viewsByType.get(Hood).getSettings().get(BINOCULARS)).isEqualTo(0L);
        assertThat(viewsByType.get(Hood_Back).getSettings().get(BINOCULARS)).isNotEqualTo(0L);

        DataStore originalViewStoreForHood = CamerasHelper.extractViewStores(1000L, readWriteParser).get(2);
        assertThat(originalViewStoreForHood.getInteger("binoculars")).contains(0L);
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
        CamerasParser parserMock = mock(CamerasParser.class);
        Map<Long, Short> index = new HashMap<>(0);
        Map<Long, List<DataStore>> views = new HashMap<>(0);
        when(parserMock.getCameraIndex()).thenReturn(index);
        when(parserMock.getCameraViews()).thenReturn(views);

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100L, parserMock)).isFalse();
    }

    @Test
    void cameraSetExists_whenIdInIndexAndInSettings_shouldReturnTrue() {
        // given
        CamerasParser parserMock = mock(CamerasParser.class);
        Map<Long, Short> index = new HashMap<>(1);
        index.put(100L, (short) 4);
        Map<Long, List<DataStore>> views = new HashMap<>(1);
        views.put(100L, new ArrayList<>(0));
        when(parserMock.getCameraIndex()).thenReturn(index);
        when(parserMock.getCameraViews()).thenReturn(views);

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100L, parserMock)).isTrue();
    }

    @Test
    void cameraSetExists_whenIdInSettingsOnly_shouldReturnFalse() {
        // given
        CamerasParser parserMock = mock(CamerasParser.class);
        Map<Long, List<DataStore>> views = new HashMap<>(1);
        views.put(100L, new ArrayList<>(0));
        when(parserMock.getCameraIndex()).thenReturn(new HashMap<>(0));
        when(parserMock.getCameraViews()).thenReturn(views);

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100L, parserMock)).isFalse();
    }

    @Test
    void cameraSetExists_whenIdInIndexOnly_shouldReturnFalse() {
        // given
        CamerasParser parserMock = mock(CamerasParser.class);
        Map<Long, Short> index = new HashMap<>(1);
        index.put(100L, (short) 4);
        when(parserMock.getCameraIndex()).thenReturn(index);
        when(parserMock.getCameraViews()).thenReturn(new HashMap<>(0));

        // when-then
        assertThat(CamerasHelper.cameraSetExists(100L, parserMock)).isFalse();
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
