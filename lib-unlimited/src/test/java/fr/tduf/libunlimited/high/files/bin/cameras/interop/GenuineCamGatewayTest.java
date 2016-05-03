package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.high.files.common.interop.GenuineGateway.CommandLineOperation.*;
import static fr.tduf.libunlimited.high.files.common.interop.GenuineGateway.EXE_TDUMT_CLI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenuineCamGatewayTest {

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
        int camId = 326;
        mockCommandLineHelperToReturnCameraViewsSuccess(camFileName, camId);

        // WHEN
        CameraInfo actualCameraInfo = genuineCamGateway.getCameraInfo(camFileName, camId);

        // THEN
        assertThat(actualCameraInfo).isNotNull();
        // TODO enhance test when mapping done
    }

    @Test
    public void customizeCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        String camFileName = "cameras.bin";
        int camId = 326;
        GenuineCamViewsDto customizeInput = FilesHelper.readObjectFromJsonResourceFile(GenuineCamViewsDto.class, "/files/interop/tdumt-cli/CAM-C.input.json");
        mockCommandLineHelperToReturnCameraCustomizeSuccess(camFileName, camId);

        // WHEN
        genuineCamGateway.customizeCamera(camFileName, camId, customizeInput);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CAM_CUSTOMIZE.getCommand()), eq(camFileName), eq(Integer.valueOf(camId).toString()), commandArgumentsCaptor.capture());

        String expectedInputContents = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(customizeInput);
        assertThat(new File(commandArgumentsCaptor.getValue()))
                .exists()
                .hasContent(expectedInputContents);
    }

    @Test
    public void resetCamera_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        String camFileName = "cameras.bin";
        int camId = 326;
        mockCommandLineHelperToReturnCameraResetSuccess(camFileName, camId);

        // WHEN
        genuineCamGateway.resetCamera(camFileName, camId);

        // THEN
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CAM_RESET.getCommand()), eq(camFileName), eq(Integer.valueOf(camId).toString()));
    }

    private void mockCommandLineHelperToReturnCameraViewsSuccess(String bankFileName, int camId) throws IOException, URISyntaxException {
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/CAM-L.output.json");
        ProcessResult processResult = new ProcessResult(CAM_LIST.getCommand(), 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CAM_LIST.getCommand(), bankFileName, Integer.valueOf(camId).toString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnCameraCustomizeSuccess(String bankFileName, int camId) throws IOException, URISyntaxException {
        ProcessResult processResult = new ProcessResult(CAM_CUSTOMIZE.getCommand(), 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CAM_CUSTOMIZE.getCommand()), eq(bankFileName), eq(Integer.valueOf(camId).toString()), anyString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnCameraResetSuccess(String bankFileName, int camId) throws IOException, URISyntaxException {
        ProcessResult processResult = new ProcessResult(CAM_RESET.getCommand(), 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CAM_RESET.getCommand()), eq(bankFileName), eq(Integer.valueOf(camId).toString()))).thenReturn(processResult);
    }
}
