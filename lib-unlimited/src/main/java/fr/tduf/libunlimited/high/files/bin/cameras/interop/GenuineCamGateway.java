package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.common.interop.GenuineGateway;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static fr.tduf.libunlimited.high.files.common.interop.GenuineGateway.CommandLineOperation.*;

/**
 * CamBin support, implementation relying on TDUMT-cli application.
 */
public class GenuineCamGateway extends GenuineGateway {
    private static final String THIS_CLASS_NAME = GenuineCamGateway.class.getSimpleName();

    public GenuineCamGateway(CommandLineHelper commandLineHelper) {
        super(commandLineHelper);
    }

    /**
     * tdumt-cli syntax: CAM-L <camFileName> <camId>
     */
    public CameraInfo getCameraInfo(String camFileName, int camId) throws IOException {
        String result = callCommandLineInterface(CAM_LIST, camFileName, Integer.valueOf(camId).toString());

        GenuineCamViewsDto outputObject = new ObjectMapper().readValue(result, GenuineCamViewsDto.class);
        return mapGenuineCamViewsToCameraInfo(outputObject);
    }

    /**
     * tdumt-cli syntax: CAM-C <camFileName> <camId> <customizeInputFileName>
     */
    public void customizeCamera(String camFileName, int camId, GenuineCamViewsDto customizeInput) throws IOException {
        String customizeInputFileName = createCamCustomizeInputFile(customizeInput);

        callCommandLineInterface(CAM_CUSTOMIZE, camFileName, Integer.valueOf(camId).toString(), customizeInputFileName);
    }

    /**
     * tdumt-cli syntax: CAM-R <camFileName> <camId>
     */
    public void resetCamera(String camFileName, int camId) throws IOException {
        callCommandLineInterface(CAM_RESET, camFileName, Integer.valueOf(camId).toString());
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
