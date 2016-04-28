package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.common.helper.CommandLineHelper.EXIT_CODE_SUCCESS;

/**
 * CamBin support, implementation relying on TDUMT-cli application.
 */
// TODO create common gateway component
public class GenuineCamGateway {
    private static final String THIS_CLASS_NAME = GenuineCamGateway.class.getSimpleName();

    static final String EXE_TDUMT_CLI = Paths.get(".", "tools", "tdumt-cli", "tdumt-cli.exe").toString();
    // TODO replace with enum
    static final String CLI_COMMAND_CAM_LIST = "CAM-L";
    static final String CLI_COMMAND_CAM_CUSTOMIZE = "CAM-C";

    private CommandLineHelper commandLineHelper;

    public GenuineCamGateway(CommandLineHelper commandLineHelper) {
        this.commandLineHelper = commandLineHelper;
    }

    /**
     * tdumt-cli syntax: CAM-L <camFileName> <camId>
     */
    public CameraInfo getCameraInfo(String camFileName, int camId) throws IOException {
        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_CAM_LIST, camFileName, Integer.valueOf(camId).toString());
        handleCommandLineErrors(processResult);

        GenuineCamViewsDto outputObject = new ObjectMapper().readValue(processResult.getOut(), GenuineCamViewsDto.class);

        return mapGenuineCamViewsToCameraInfo(outputObject);
    }

    /**
     * tdumt-cli syntax: CAM-C <camFileName> <camId> <customizeInputFileName>
     */
    public void customizeCamera(String camFileName, int camId, GenuineCamViewsDto customizeInput) throws IOException {
        String customizeInputFileName = createCamCustomizeInputFile(customizeInput);
        ProcessResult processResult = commandLineHelper.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_CAM_CUSTOMIZE, camFileName, Integer.valueOf(camId).toString(), customizeInputFileName);
        handleCommandLineErrors(processResult);
    }

    private static void handleCommandLineErrors(ProcessResult processResult) throws IOException {
        if (processResult.getReturnCode() != EXIT_CODE_SUCCESS) {
            Exception parentException = new Exception(processResult.getErr());
            throw new IOException("Unable to execute genuine CLI command: " + processResult.getCommandName(), parentException);
        }
    }

    private static CameraInfo mapGenuineCamViewsToCameraInfo(GenuineCamViewsDto outputObject) {
        // TODO mapping
        return new CameraInfo();
    }

    private static String createCamCustomizeInputFile(GenuineCamViewsDto customizeInput) throws IOException {
        File inputFile = Files.createTempDirectory("libUnlimited-cameras").resolve("CustomizeInput.json").toFile();
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(inputFile, customizeInput);

        String inputFileName = inputFile.getAbsolutePath();
        Log.debug(THIS_CLASS_NAME, "inputFileName: " + inputFileName);
        return inputFileName;
    }
}
