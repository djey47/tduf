package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto.GenuineCamViewDto.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenuineCamGatewayTest {

    private static final long CAMERA_ID = 326L;

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineCamGateway genuineCamGateway;
    @Captor
    private ArgumentCaptor<String> commandArgumentsCaptor;

    @Before
    public void setUp() throws IOException {
        Log.set(Log.LEVEL_DEBUG);
    }

    @Test
    public void getCameraInfo_whenSuccess_shouldInvokeCommandLineCorrectly_andReturnObject() throws IOException, URISyntaxException {
        // GIVEN
        String camFileName = "cameras.bin";
        mockCommandLineHelperToReturnCameraViewsSuccess(camFileName, CAMERA_ID);

        // WHEN
        CameraInfo actualCameraInfo = genuineCamGateway.getCameraInfo(camFileName, CAMERA_ID);

        // THEN
        assertThat(actualCameraInfo).isNotNull();
        assertThat(actualCameraInfo.getCameraIdentifier()).isEqualToComparingFieldByField(CAMERA_ID);
        final List<CameraInfo.CameraView> actualViewSets = actualCameraInfo.getViews();
        assertThat(actualViewSets).hasSize(4);
        assertThat(actualViewSets).extracting(("type")).containsOnly(Hood, Hood_Back, Cockpit, Cockpit_Back);
        final CameraInfo.CameraView actualView = actualViewSets.stream().filter(v -> Hood == v.getType()).findAny().get();
        assertThat(actualView.getType()).isEqualTo(Hood);
        assertThat(actualView.getSourceCameraIdentifier()).isEqualTo(0);
        assertThat(actualView.getSourceType()).isEqualTo(Unknown);
    }

    @Test
    public void customizeCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        String camFileName = "cameras.bin";
        GenuineCamViewsDto customizeInput = FilesHelper.readObjectFromJsonResourceFile(GenuineCamViewsDto.class, "/files/interop/tdumt-cli/CAM-C.input.json");
        mockCommandLineHelperToReturnCameraCustomizeSuccess(camFileName, CAMERA_ID);

        // WHEN
        genuineCamGateway.customizeCamera(camFileName, CAMERA_ID, customizeInput);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq("mono"), anyString(), eq("CAM-C"), eq(camFileName), eq(Long.valueOf(CAMERA_ID).toString()), commandArgumentsCaptor.capture());

        String expectedInputContents = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(customizeInput);
        assertThat(new File(commandArgumentsCaptor.getValue()))
                .exists()
                .hasContent(expectedInputContents);
    }

    @Test
    public void resetCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        String camFileName = "cameras.bin";
        int CAMERA_ID = 326;
        mockCommandLineHelperToReturnCameraResetSuccess(camFileName, CAMERA_ID);

        // WHEN
        genuineCamGateway.resetCamera(camFileName, CAMERA_ID);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq("mono"), anyString(), eq("CAM-R"), eq(camFileName), eq(Integer.valueOf(CAMERA_ID).toString()));
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
