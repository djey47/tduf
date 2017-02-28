package fr.tduf.libunlimited.high.files.bin.cameras.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.common.interop.GenuineGateway;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
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
    public CameraSetInfo getCameraInfo(String camFileName, int camId) throws IOException {
        String result = callCommandLineInterface(CAM_LIST, camFileName, Integer.toString(camId));

        GenuineCamViewsDto outputObject = new ObjectMapper().readValue(result, GenuineCamViewsDto.class);
        return mapGenuineCamViewsToCameraInfo(outputObject, camId);
    }

    /**
     * tdumt-cli syntax: CAM-C <camFileName> <camId> <customizeInputFileName>
     */
    public void customizeCamera(String camFileName, int camId, GenuineCamViewsDto customizeInput) throws IOException {
        String customizeInputFileName = createCamCustomizeInputFile(customizeInput);

        callCommandLineInterface(CAM_CUSTOMIZE, camFileName, Integer.toString(camId), customizeInputFileName);
    }

    /**
     * tdumt-cli syntax: CAM-R <camFileName> <camId>
     */
    public void resetCamera(String camFileName, int camId) throws IOException {
        callCommandLineInterface(CAM_RESET, camFileName, Integer.toString(camId));
    }

    private static CameraSetInfo mapGenuineCamViewsToCameraInfo(GenuineCamViewsDto genuineCamViews, int camId) {
        final CameraSetInfo.CameraInfoBuilder cameraInfoBuilder = CameraSetInfo.builder()
                .forIdentifier(camId);

        genuineCamViews.getViews()
                .forEach(genuineView -> cameraInfoBuilder.addView(mapGenuineCamViewToCameraView(genuineView)));

        return cameraInfoBuilder.build();
    }

    private static CameraView mapGenuineCamViewToCameraView(GenuineCamViewsDto.GenuineCamViewDto genuineView) {
        ViewKind sourceType = ViewKind.fromInternalId(genuineView.getViewId());
        return CameraView.from(genuineView.getViewType(), genuineView.getCameraId(), sourceType);
    }

    private static String createCamCustomizeInputFile(GenuineCamViewsDto customizeInput) throws IOException {
        File inputFile = Files.createTempDirectory("libUnlimited-cameras").resolve("CustomizeInput.json").toFile();
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(inputFile, customizeInput);

        String inputFileName = inputFile.getAbsolutePath();
        Log.debug(THIS_CLASS_NAME, "inputFileName: " + inputFileName);
        return inputFileName;
    }
}
