package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class GenuineCamGatewayTest {

    private static final int CAMERA_ID = 326;
    private static final String CAMERAS_FILE_NAME = "Cameras.bin";

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineCamGateway genuineCamGateway;

    @Captor
    private ArgumentCaptor<String> commandArgumentsCaptor;

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_DEBUG);
        initMocks(this);
    }

    @Test
    void getCameraInfo_whenSuccess_shouldInvokeCommandLineCorrectly_andReturnObject() throws IOException, URISyntaxException {
        // GIVEN
        mockCommandLineHelperToReturnCameraViewsSuccess(CAMERAS_FILE_NAME, CAMERA_ID);

        // WHEN
        CameraSetInfo actualCameraInfo = genuineCamGateway.getCameraInfo(CAMERAS_FILE_NAME, CAMERA_ID);

        // THEN
        assertThat(actualCameraInfo).isNotNull();
        assertThat(actualCameraInfo.getCameraIdentifier()).isEqualToComparingFieldByField(CAMERA_ID);
        final List<CameraView> actualViewSets = actualCameraInfo.getViews();
        assertThat(actualViewSets).hasSize(4);
        assertThat(actualViewSets).extracting(("kind")).containsOnly(Hood, Hood_Back, Cockpit, Cockpit_Back);
        final CameraView actualView = actualViewSets.stream().filter(v -> Hood == v.getKind()).findAny().get();
        assertThat(actualView.getKind()).isEqualTo(Hood);
        assertThat(actualView.getUsedCameraSetId()).isEqualTo(0);
        assertThat(actualView.getUsedKind()).isEqualTo(Unknown);
    }

    @Test
    void customizeCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        GenuineCamViewsDto customizeInput = FilesHelper.readObjectFromJsonResourceFile(GenuineCamViewsDto.class, "/files/interop/tdumt-cli/CAM-C.input.json");
        mockCommandLineHelperToReturnCameraCustomizeSuccess(CAMERAS_FILE_NAME, CAMERA_ID);

        // WHEN
        genuineCamGateway.customizeCamera(CAMERAS_FILE_NAME, CAMERA_ID, customizeInput);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq("mono"), anyString(), eq("CAM-C"), eq(CAMERAS_FILE_NAME), eq(Long.valueOf(CAMERA_ID).toString()), commandArgumentsCaptor.capture());

        String expectedInputContents = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(customizeInput);
        assertThat(new File(commandArgumentsCaptor.getValue()))
                .exists()
                .hasContent(expectedInputContents);
    }

    @Test
    void resetCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        int CAMERA_ID = 326;
        mockCommandLineHelperToReturnCameraResetSuccess(CAMERAS_FILE_NAME, CAMERA_ID);

        // WHEN
        genuineCamGateway.resetCamera(CAMERAS_FILE_NAME, CAMERA_ID);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq("mono"), anyString(), eq("CAM-R"), eq(CAMERAS_FILE_NAME), eq(Integer.valueOf(CAMERA_ID).toString()));
    }

    private void mockCommandLineHelperToReturnCameraViewsSuccess(String bankFileName, long CAMERA_ID) throws IOException, URISyntaxException {
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/CAM-L.output.json");
        ProcessResult processResult = new ProcessResult("CAM-L", 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("CAM-L"), eq(bankFileName), eq(Long.valueOf(CAMERA_ID).toString()))).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnCameraCustomizeSuccess(String bankFileName, long CAMERA_ID) throws IOException, URISyntaxException {
        ProcessResult processResult = new ProcessResult("CAM-C", 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("CAM-C"), eq(bankFileName), eq(Long.valueOf(CAMERA_ID).toString()), anyString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnCameraResetSuccess(String bankFileName, long CAMERA_ID) throws IOException, URISyntaxException {
        ProcessResult processResult = new ProcessResult("CAM-R", 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("CAM-R"), eq(bankFileName), eq(Long.valueOf(CAMERA_ID).toString()))).thenReturn(processResult);
    }
}
