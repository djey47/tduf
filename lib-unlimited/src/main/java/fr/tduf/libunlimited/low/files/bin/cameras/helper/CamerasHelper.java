package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
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

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromProps;
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
     * @param cameraInfo        : loaded cameras contents.
     */
    public static void duplicateCameraSet(int sourceCameraId, int targetCameraId, CameraInfoEnhanced cameraInfo) {
        requireNonNull(cameraInfo, "Loaded camera information is required.");

        checkCameraSetExists(sourceCameraId, cameraInfo);

        if(cameraInfo.cameraSetExists(targetCameraId)) {
            Log.warn(THIS_CLASS_NAME, "Unable to overwrite existing camera set: " + targetCameraId);
            return;
        }

        Integer viewCount = cameraInfo.getViewsForCameraSet(sourceCameraId).size();
        cameraInfo.updateIndex(targetCameraId, viewCount.shortValue());

        List<CameraViewEnhanced> clonedViews = cameraInfo.getViewsForCameraSet(sourceCameraId).stream()
                .map(sourceView -> sourceView.cloneForNewViewSet(targetCameraId))
                .collect(toList());
        cameraInfo.updateViews(targetCameraId, clonedViews);
    }

    /**
     * Creates all camera sets at targetId with all views from sourceId
     * @param instructions      : list of <sourceCameraId>;<targetCameraId>
     * @param cameraInfo        : loaded cameras contents.
     */
    public static void batchDuplicateCameraSets(List<String> instructions, CameraInfoEnhanced cameraInfo) {
        requireNonNull(instructions, "A list of instructions is required.");

        instructions.forEach(instruction -> {
            String[] compounds = instruction.split(INSTRUCTION_SEPARATOR);
            duplicateCameraSet(Integer.valueOf(compounds[0]), Integer.valueOf(compounds[1]), cameraInfo);
        });
    }

    /**
     * Kept for compatibility reasons
     * @param cameraIdentifier      : identifier of camera
     * @param cameraInfoEnhanced    : parsed cameras contents
     * @return view properties for requested camera set.
     */
    // TODO return type to be replaced with enhanced objects
    public static CameraInfo fetchInformation(int cameraIdentifier, CameraInfoEnhanced cameraInfoEnhanced) {
        CameraInfo.CameraInfoBuilder cameraInfoBuilder = CameraInfo.builder()
                .forIdentifier(cameraIdentifier);

        cameraInfoEnhanced.getViewsForCameraSet(cameraIdentifier).stream()
                .map(cameraViewEnhanced -> fromProps(cameraViewEnhanced.getSettings(), cameraViewEnhanced.getKind()))
                .forEach(cameraInfoBuilder::addView);

        return cameraInfoBuilder.build();
    }

    /**
     * @param cameraInfoEnhanced : loaded cameras contents
     * @return all cameras and their view properties.
     */
    // TODO return type to be replaced with enhanced objects
    public static List<CameraInfo> fetchAllInformation(CameraInfoEnhanced cameraInfoEnhanced) {
        return cameraInfoEnhanced.getAllSetIdentifiers().stream()
                .map(setIdentifier -> fetchInformation(setIdentifier, cameraInfoEnhanced))
                .collect(toList());
    }

    /**
     * @param cameraIdentifier      : identifier of camera
     * @param viewKind              : existing view in set
     * @param cameraInfoEnhanced    : loaded cameras contents
     * @return view properties for requested camera.
     */
    public static EnumMap<ViewProps, ?> fetchViewProperties(int cameraIdentifier, ViewKind viewKind, CameraInfoEnhanced cameraInfoEnhanced) {
        return fetchInformation(cameraIdentifier, cameraInfoEnhanced).getViews().stream()
                .filter(cv -> viewKind == cv.getType())
                .findAny()
                .map(CameraInfo.CameraView::getSettings)
                .orElseThrow(() -> new NoSuchElementException("Camera view not found: (" + cameraIdentifier + ", " + viewKind + ")"));
    }

    /**
     * @param configuration         : view properties to be updated
     * @param cameraInfoEnhanced    : cameras contents to be updated
     */
    // TODO create dedicated object for configuration
    public static void updateViews(CameraInfo configuration, CameraInfoEnhanced cameraInfoEnhanced) {
        Long cameraIdentifier = validateConfiguration(configuration);
        cameraInfoEnhanced.getViewsForCameraSet(cameraIdentifier.intValue())
                .forEach(view -> {
                    ViewKind viewKind = view.getKind();

                    configuration.getViews().stream()
                            .filter(v -> viewKind == v.getType())
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
    // TODO create dedicated object for configuration
    // TODO return enhanced object with source and target info
    public static CameraInfo useViews(CameraInfo configuration, String sourceCamerasFile) throws IOException {
        Long cameraIdentifier = validateConfiguration(configuration);

        GenuineCamViewsDto customizeInput = mapCameraInfoToGenuineCamViews(configuration);
        cameraSupport.customizeCamera(sourceCamerasFile, cameraIdentifier, customizeInput);

        CameraInfo cameraInfoFromTDUMT = cameraSupport.getCameraInfo(sourceCamerasFile, cameraIdentifier);
        CameraInfo cameraInfoFromTDUF = fetchInformation(cameraIdentifier.intValue(), loadAndParseCamerasDatabase(sourceCamerasFile));

        return mergeCameraInfo(cameraInfoFromTDUF, cameraInfoFromTDUMT);
    }

    /**
     * @return parsed camera contents.
     */
    public static CameraInfoEnhanced loadAndParseCamerasDatabase(String cameraFile) throws IOException {
        CamerasParser parser = CamerasParser.load(getCamerasInputStream(cameraFile));
        return parser.parse();
    }

    /**
     * Write file according to cameras.bin file format
     * @param cameraInfoEnhanced    : parsed camera contents
     * @param cameraFile            : file to be written. Existing file will be replaced.
     * @throws IOException when a file system error occurs
     */
    public static void saveCamerasDatabase(CameraInfoEnhanced cameraInfoEnhanced, String cameraFile) throws IOException {
        ByteArrayOutputStream outputStream = CamerasWriter.load(cameraInfoEnhanced).write();
        Files.write(Paths.get(cameraFile), outputStream.toByteArray(), StandardOpenOption.CREATE);
    }

    /**
     * @param cameraId              : identifier of camera set to use
     * @param cameraInfoEnhanced    : parsed camera contents
     * @return true if a set with provded id exists in index and in settings
     */
    public static boolean cameraSetExists(int cameraId, CameraInfoEnhanced cameraInfoEnhanced) {
        return cameraInfoEnhanced.cameraSetExistsInIndex(cameraId)
                && cameraInfoEnhanced.cameraSetExistsInSettings(cameraId);
    }

    private static CameraInfo mergeCameraInfo(CameraInfo cameraInfoFromTDUF, CameraInfo cameraInfoFromTDUMT) {
        return CameraInfo.builder()
                .forIdentifier(cameraInfoFromTDUF.getCameraIdentifier())
                .withUsedViews(cameraInfoFromTDUF.getViews(), cameraInfoFromTDUMT.getViews())
                .build();
    }

    private static GenuineCamViewsDto mapCameraInfoToGenuineCamViews(CameraInfo configuration) {
        List<GenuineCamViewsDto.GenuineCamViewDto> views = configuration.getViews().stream()
                .map(CamerasHelper::mapCameraViewToGenuineCamView)
                .collect(toList());

        return GenuineCamViewsDto.withViews(views);
    }

    private static GenuineCamViewsDto.GenuineCamViewDto mapCameraViewToGenuineCamView(CameraInfo.CameraView view) {
        GenuineCamViewsDto.GenuineCamViewDto genuineCamViewDto = new GenuineCamViewsDto.GenuineCamViewDto();

        genuineCamViewDto.setViewType(view.getType());
        genuineCamViewDto.setCameraId(Long.valueOf(view.getSourceCameraIdentifier()).intValue());
        genuineCamViewDto.setViewId(view.getSourceType().getInternalId());
        genuineCamViewDto.setCustomized(true);

        return genuineCamViewDto;
    }

    private static long validateConfiguration(CameraInfo configuration) {
        requireNonNull(configuration, "View configuration is required.");

        if (configuration.getViews().isEmpty()) {
            throw new IllegalArgumentException("No views to update in provided configuration.");
        }

        return configuration.getCameraIdentifier();
    }

    private static ByteArrayInputStream getCamerasInputStream(String sourceCameraFile) throws IOException {
        return new ByteArrayInputStream(readAllBytes(Paths.get(sourceCameraFile)));
    }

    private static void checkCameraSetExists(int cameraSetId, CameraInfoEnhanced cameraInfo) {
        if (!cameraInfo.cameraSetExistsInSettings(cameraSetId)) {
            throw new NoSuchElementException("Unknown source camera identifier: " + cameraSetId);
        }
    }

    // For testing
    public static void setCameraSupport(GenuineCamGateway genuineCamGateway) {
        cameraSupport = genuineCamGateway;
    }
}
