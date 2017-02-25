package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CamerasHelperTest {

    private static byte[] camContents;

    private GenuineCamGateway cameraSupportMock = mock(GenuineCamGateway.class);

    @BeforeAll
    static void globalSetUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
    }

    @BeforeEach
    void setUp() {
        CamerasHelper.setCameraSupport(cameraSupportMock);
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
}
