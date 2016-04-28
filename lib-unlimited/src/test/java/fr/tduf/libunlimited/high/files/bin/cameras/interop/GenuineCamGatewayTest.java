package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway.CLI_COMMAND_CAM_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenuineCamGatewayTest {

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineCamGateway genuineCamGateway;

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
        // TODO enhance test
    }

    private void mockCommandLineHelperToReturnCameraViewsSuccess(String bankFileName, int camId) throws IOException, URISyntaxException {
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/CAM-L.output.json");
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_CAM_LIST, 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(GenuineCamGateway.EXE_TDUMT_CLI, CLI_COMMAND_CAM_LIST, bankFileName, Integer.valueOf(camId).toString())).thenReturn(processResult);
    }

}