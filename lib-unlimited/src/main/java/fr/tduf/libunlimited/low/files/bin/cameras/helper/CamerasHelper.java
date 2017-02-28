package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.SetConfigurationDto;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.ViewConfigurationDto;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.List;
import java.util.NoSuchElementException;

import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Static methods tho access or modify camera information in datastore.
 */
public class CamerasHelper {
    private static final String THIS_CLASS_NAME = CamerasHelper.class.getSimpleName();

    public static final String FILE_CAMERAS_BIN = "cameras.bin";

    private static final String INSTRUCTION_SEPARATOR = ";";

    private static GenuineCamGateway cameraSupport = new GenuineCamGateway(new CommandLineHelper());

    private CamerasHelper(){}

    /**
     * Creates a camera set at targetCameraId with all views from set at sourceCameraId
     * @param sourceCameraId    : identifier of camera to get views from
     * @param targetCameraId    : identifier of camera to create views. May not exist already, in that case will add a new set
     * @param camerasDatabase   : loaded cameras contents.
     */
    public static void duplicateCameraSet(int sourceCameraId, int targetCameraId, CamerasDatabase camerasDatabase) {
        requireNonNull(camerasDatabase, "Loaded camera information is required.");

        checkCameraSetExists(sourceCameraId, camerasDatabase);

        if(camerasDatabase.cameraSetExists(targetCameraId)) {
            Log.warn(THIS_CLASS_NAME, "Unable to overwrite existing camera set: " + targetCameraId);
            return;
        }

        List<CameraView> sourceViews = camerasDatabase.getViewsForCameraSet(sourceCameraId);
        Integer viewCount = sourceViews.size();
        camerasDatabase.updateIndex(targetCameraId, viewCount.shortValue());

        List<CameraView> clonedViews = sourceViews.stream()
                .map(sourceView -> sourceView.cloneForNewViewSet(targetCameraId))
                .collect(toList());
        camerasDatabase.updateViews(targetCameraId, clonedViews);
    }

    /**
     * Creates all camera sets at targetId with all views from sourceId
     * @param instructions      : list of <sourceCameraId>;<targetCameraId>
     * @param cameraInfo        : loaded cameras contents.
     */
    public static void batchDuplicateCameraSets(List<String> instructions, CamerasDatabase cameraInfo) {
        requireNonNull(instructions, "A list of instructions is required.");

        instructions.forEach(instruction -> {
            String[] compounds = instruction.split(INSTRUCTION_SEPARATOR);
            duplicateCameraSet(Integer.valueOf(compounds[0]), Integer.valueOf(compounds[1]), cameraInfo);
        });
    }

    /**
     * Kept for compatibility reasons
     * @param cameraIdentifier      : identifier of camera
     * @param camerasDatabase    : parsed cameras contents
     * @return view properties for requested camera set.
     */
    public static CameraSetInfo fetchInformation(int cameraIdentifier, CamerasDatabase camerasDatabase) {
        CameraSetInfo.CameraInfoBuilder cameraInfoBuilder = CameraSetInfo.builder()
                .forIdentifier(cameraIdentifier);

        camerasDatabase.getViewsForCameraSet(cameraIdentifier).stream()
                .map(cameraViewEnhanced -> CameraView.fromProps(cameraViewEnhanced.getSettings(), cameraViewEnhanced.getKind()))
                .forEach(cameraInfoBuilder::addView);

        return cameraInfoBuilder.build();
    }

    /**
     * @param camerasDatabase : loaded cameras contents
     * @return all cameras and their view properties.
     */
    public static List<CameraSetInfo> fetchAllInformation(CamerasDatabase camerasDatabase) {
        return camerasDatabase.getAllSetIdentifiers().stream()
                .map(setIdentifier -> fetchInformation(setIdentifier, camerasDatabase))
                .collect(toList());
    }

    /**
     * @param cameraIdentifier      : identifier of camera
     * @param viewKind              : existing view in set
     * @param camerasDatabase    : loaded cameras contents
     * @return view properties for requested camera.
     */
    public static EnumMap<ViewProps, ?> fetchViewProperties(int cameraIdentifier, ViewKind viewKind, CamerasDatabase camerasDatabase) {
        return fetchInformation(cameraIdentifier, camerasDatabase).getViews().stream()
                .filter(cv -> viewKind == cv.getKind())
                .findAny()
                .map(CameraView::getSettings)
                .orElseThrow(() -> new NoSuchElementException("Camera view not found: (" + cameraIdentifier + ", " + viewKind + ")"));
    }

    /**
     * @param configuration         : view properties to be updated
     * @param camerasDatabase    : cameras contents to be updated
     */
    public static void updateViews(SetConfigurationDto configuration, CamerasDatabase camerasDatabase) {
        int cameraIdentifier = validateConfiguration(configuration);
        camerasDatabase.getViewsForCameraSet(cameraIdentifier)
                .forEach(view -> {
                    ViewKind viewKind = view.getKind();

                    configuration.getViews().stream()
                            .filter(v -> viewKind == v.getOriginalKind())
                            .findAny()
                            .ifPresent(conf -> conf.getSettings().entrySet()
                                    .forEach(entry -> view.getSettings().put(entry.getKey(), entry.getValue())));
                });
    }

    /**
     * Applies view properties from other camera set.
     * @param configuration     : views to use
     * @param sourceCamerasFile : camera contents to be modified
     * @return updated view properties.
     */
    public static CameraSetInfo useViews(SetConfigurationDto configuration, String sourceCamerasFile) throws IOException {
        int cameraIdentifier = validateConfiguration(configuration);

        GenuineCamViewsDto customizeInput = mapCameraInfoToGenuineCamViews(configuration);
        cameraSupport.customizeCamera(sourceCamerasFile, cameraIdentifier, customizeInput);

        CameraSetInfo cameraInfoFromTDUMT = cameraSupport.getCameraInfo(sourceCamerasFile, cameraIdentifier);
        CameraSetInfo cameraInfoFromTDUF = fetchInformation(cameraIdentifier, loadAndParseCamerasDatabase(sourceCamerasFile));

        return mergeCameraInfo(cameraInfoFromTDUF, cameraInfoFromTDUMT);
    }

    /**
     * @return parsed camera contents.
     */
    public static CamerasDatabase loadAndParseCamerasDatabase(String cameraFile) throws IOException {
        CamerasParser parser = CamerasParser.load(getCamerasInputStream(cameraFile));
        return parser.parse();
    }

    /**
     * Write file according to cameras.bin file format
     * @param camerasDatabase    : parsed camera contents
     * @param cameraFile            : file to be written. Existing file will be replaced.
     * @throws IOException when a file system error occurs
     */
    public static void saveCamerasDatabase(CamerasDatabase camerasDatabase, String cameraFile) throws IOException {
        ByteArrayOutputStream outputStream = CamerasWriter.load(camerasDatabase).write();
        Files.write(Paths.get(cameraFile), outputStream.toByteArray(), StandardOpenOption.CREATE);
    }

    /**
     * @param cameraId              : identifier of camera set to use
     * @param camerasDatabase    : parsed camera contents
     * @return true if a set with provded id exists in index and in settings
     */
    public static boolean cameraSetExists(int cameraId, CamerasDatabase camerasDatabase) {
        return camerasDatabase.cameraSetExistsInIndex(cameraId)
                && camerasDatabase.cameraSetExistsInSettings(cameraId);
    }

    private static CameraSetInfo mergeCameraInfo(CameraSetInfo cameraInfoFromTDUF, CameraSetInfo cameraInfoFromTDUMT) {
        return CameraSetInfo.builder()
                .forIdentifier(cameraInfoFromTDUF.getCameraIdentifier())
                .withUsedViews(cameraInfoFromTDUF.getViews(), cameraInfoFromTDUMT.getViews())
                .build();
    }

    private static GenuineCamViewsDto mapCameraInfoToGenuineCamViews(SetConfigurationDto configuration) {
        List<GenuineCamViewsDto.GenuineCamViewDto> views = configuration.getViews().stream()
                .map(CamerasHelper::mapCameraViewToGenuineCamView)
                .collect(toList());

        return GenuineCamViewsDto.withViews(views);
    }

    private static GenuineCamViewsDto.GenuineCamViewDto mapCameraViewToGenuineCamView(ViewConfigurationDto viewConfiguration) {
        GenuineCamViewsDto.GenuineCamViewDto genuineCamViewDto = new GenuineCamViewsDto.GenuineCamViewDto();

        genuineCamViewDto.setViewType(viewConfiguration.getOriginalKind());
        genuineCamViewDto.setCameraId(viewConfiguration.getUsedSetIdentifier());
        genuineCamViewDto.setViewId(viewConfiguration.getUsedKind().getInternalId());
        genuineCamViewDto.setCustomized(true);

        return genuineCamViewDto;
    }

    private static int validateConfiguration(SetConfigurationDto configuration) {
        requireNonNull(configuration, "Camera set configuration is required.");

        if (configuration.getViews().isEmpty()) {
            throw new IllegalArgumentException("No views to update in provided configuration.");
        }

        return configuration.getSetIdentifier();
    }

    private static ByteArrayInputStream getCamerasInputStream(String sourceCameraFile) throws IOException {
        return new ByteArrayInputStream(readAllBytes(Paths.get(sourceCameraFile)));
    }

    private static void checkCameraSetExists(int cameraSetId, CamerasDatabase cameraInfo) {
        if (!cameraInfo.cameraSetExistsInSettings(cameraSetId)) {
            throw new NoSuchElementException("Unknown source camera identifier: " + cameraSetId);
        }
    }

    // For testing
    public static void setCameraSupport(GenuineCamGateway genuineCamGateway) {
        cameraSupport = genuineCamGateway;
    }
}
