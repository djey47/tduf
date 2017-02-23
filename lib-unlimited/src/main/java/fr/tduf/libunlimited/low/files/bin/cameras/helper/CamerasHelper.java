package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasWriter;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Long.valueOf;
import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Static methods tho access or modify camera information in datastore.
 */
public class CamerasHelper {
    private static final String THIS_CLASS_NAME = CamerasHelper.class.getSimpleName();

    public static final String FILE_CAMERAS_BIN = "cameras.bin";

    private static final String KEY_INDEX = "index";
    private static final String KEY_VIEWS = "views";
    private static final String KEY_CAMERA_ID = "cameraId";
    private static final String KEY_VIEW_COUNT = "viewCount";
    private static final String KEY_INDEX_SIZE = "indexSize";

    private static final String INSTRUCTION_SEPARATOR = ";";

    private static GenuineCamGateway cameraSupport = new GenuineCamGateway(new CommandLineHelper());

    private CamerasHelper(){}

    /**
     * Creates a camera set at targetCameraId with all views from set at sourceCameraId
     * @param sourceCameraId    : identifier of camera to get views from
     * @param targetCameraId    : identifier of camera to create views. May not exist already, in that case will add a new set
     * @param parser            : parsed cameras contents.
     */
    @Deprecated
    public static void duplicateCameraSet(long sourceCameraId, long targetCameraId, CamerasParser parser) {
        DataStore dataStore = requireNonNull(parser, "Parser with cameras contents is required.").getDataStore();

        final Map<Long, Short> cameraIndex = parser.getCameraIndex();

        checkCameraSetExists(sourceCameraId, parser);

        if (cameraIndex.containsKey(targetCameraId)
                || parser.getCameraViews().containsKey(targetCameraId)) {
            Log.warn(THIS_CLASS_NAME, "Unable to overwrite existing camera set: " + targetCameraId);
            return;
        }

        updateIndexInDatastore(dataStore, sourceCameraId, targetCameraId, cameraIndex);

        updateViewsInDatastore(dataStore, sourceCameraId, targetCameraId, parser);

        parser.flushCaches();
    }

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
     * @param parser            : parsed cameras contents.
     */
    @Deprecated
    public static void batchDuplicateCameraSets(List<String> instructions, CamerasParser parser) {
        requireNonNull(instructions, "A list of instructions is required.");

        instructions.forEach(instruction -> {
            String[] compounds = instruction.split(";");
            duplicateCameraSet(valueOf(compounds[0]), valueOf(compounds[1]), requireNonNull(parser, "Parser with cameras contents is required."));
        });
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
     * @param cameraIdentifier  : identifier of camera
     * @param parser            : parsed cameras contents
     * @return view properties for requested camera.
     */
    @Deprecated
    public static CameraInfo fetchInformation(long cameraIdentifier, CamerasParser parser) {
        List<DataStore> viewStores = extractViewStores(cameraIdentifier, parser);

        CameraInfo.CameraInfoBuilder cameraInfoBuilder = CameraInfo.builder()
                .forIdentifier((int) cameraIdentifier);

        viewStores.stream()
                .map(parser::getViewProps)
                .map(CameraInfo.CameraView::fromProps)
                .forEach(cameraInfoBuilder::addView);

        return cameraInfoBuilder.build();
    }

    /**
     * Kept for compatibility reasons
     * @param cameraIdentifier      : identifier of camera
     * @param cameraInfoEnhanced    : parsed cameras contents
     * @return view properties for requested camera set.
     */
    // TODO To be replaced with enhanced objects
    public static CameraInfo fetchInformation(int cameraIdentifier, CameraInfoEnhanced cameraInfoEnhanced) {
        CameraInfo.CameraInfoBuilder cameraInfoBuilder = CameraInfo.builder()
                .forIdentifier(cameraIdentifier);

        List<CameraViewEnhanced> allViews = cameraInfoEnhanced.getViewsForCameraSet(cameraIdentifier);
        allViews.stream()
                .map(CameraViewEnhanced::getSettings)
                .map(CameraInfo.CameraView::fromProps)
                .forEach(cameraInfoBuilder::addView);

        return cameraInfoBuilder.build();
    }

    /**
     * @param parser : parsed cameras contents
     * @return all cameras and their view properties.
     */
    @Deprecated
    public static List<CameraInfo> fetchAllInformation(CamerasParser parser) {
        return parser.getCameraViews().entrySet().stream()
                .map(Map.Entry::getKey)
                .map(cameraId -> CamerasHelper.fetchInformation(cameraId, parser))
                .collect(toList());
    }

    /**
     * @param cameraInfoEnhanced : loaded cameras contents
     * @return all cameras and their view properties.
     */
    // TODO To be replaced with enhanced objects
    public static List<CameraInfo> fetchAllInformation(CameraInfoEnhanced cameraInfoEnhanced) {
        return cameraInfoEnhanced.getAllSetIdentifiers().stream()
                .map(setIdentifier -> fetchInformation(setIdentifier, cameraInfoEnhanced))
                .collect(toList());
    }

    /**
     * @param cameraIdentifier  : identifier of camera
     * @param viewKind          : existing view in set
     * @param parser            : parsed cameras contents
     * @return view properties for requested camera.
     */
    @Deprecated
    public static EnumMap<ViewProps, ?> fetchViewProperties(long cameraIdentifier, ViewKind viewKind, CamerasParser parser) {
        return fetchInformation(cameraIdentifier, parser).getViews().stream()
                .filter(cv -> viewKind == cv.getType())
                .findAny()
                .map(CameraInfo.CameraView::getSettings)
                .orElseThrow(() -> new NoSuchElementException("Camera view not found: (" + cameraIdentifier + ", " + viewKind + ")"));
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
     * @param configuration : view properties to be updated
     * @param parser        : parsed cameras contents
     * @return updated view properties.
     */
    @Deprecated
    public static CameraInfo updateViews(CameraInfo configuration, CamerasParser parser) {
        long cameraIdentifier = validateConfiguration(configuration);
        extractViewStores(cameraIdentifier, parser)
                .forEach(viewStore -> {
                    ViewKind viewKind = (ViewKind) ViewProps.TYPE.retrieveFrom(viewStore)
                            .orElseThrow(() -> new IllegalStateException("No view type in store"));

                    configuration.getViews().stream()
                            .filter(view -> viewKind == view.getType())
                            .findAny()
                            .ifPresent(conf -> conf.getSettings().entrySet()
                                    .forEach(entry -> entry.getKey().updateIn(viewStore, entry.getValue())));

                    parser.getDataStore().mergeRepeatedValues(KEY_VIEWS, viewStore.getRepeatIndex(), viewStore);
                });

        parser.flushCaches();

        return fetchInformation(cameraIdentifier, parser);
    }

    /**
     * @param configuration         : view properties to be updated
     * @param cameraInfoEnhanced    : parsed cameras contents
     * @return updated view properties.
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
    @Deprecated
    public static CamerasParser loadAndParseFile(String cameraFile) throws IOException {
        CamerasParser parser = CamerasParser.load(getCamerasInputStream(cameraFile));
        parser.parse();
        return parser;
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
     * @param camerasParser : parsed camera contents
     * @param cameraFile    : file to be written. Existing file will be replaced.
     * @throws IOException when a file system error occurs
     */
    @Deprecated
    public static void saveFile(CamerasParser camerasParser, String cameraFile) throws IOException {
        ByteArrayOutputStream outputStream = CamerasWriter.load(camerasParser.getDataStore()).write();
        Files.write(Paths.get(cameraFile), outputStream.toByteArray(), StandardOpenOption.CREATE);
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
     * @param camerasParser : parsed camera contents
     * @return true if a set with provded id exists in index and in settings
     */
    @Deprecated
    public static boolean cameraSetExists(long cameraId, CamerasParser camerasParser) {
        Map<Long, Short> cameraIndex = camerasParser.getCameraIndex() ;
        return cameraIndex.containsKey(cameraId)
                && camerasParser.getCameraViews().containsKey(cameraId);
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

    @Deprecated
    static List<DataStore> extractViewStores(long cameraIdentifier, CamerasParser parser) {
        List<DataStore> viewStores = requireNonNull(parser, "Parser with cameras contents is required.")
                .getCameraViews().get(cameraIdentifier);
        if (viewStores == null) {
            throw new NoSuchElementException("Requested camera identifier does not exist: " + cameraIdentifier);
        }
        return viewStores;
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

    private static void updateIndexInDatastore(DataStore dataStore, long sourceCameraId, long targetCameraId, Map<Long, Short> cameraIndex) {
        short viewCount = cameraIndex.get(sourceCameraId);
        int currentIndexEntryCount = cameraIndex.size();
        dataStore.addRepeatedInteger32(KEY_INDEX, KEY_CAMERA_ID, currentIndexEntryCount, targetCameraId);
        dataStore.addRepeatedInteger32(KEY_INDEX, KEY_VIEW_COUNT, currentIndexEntryCount, viewCount);
        dataStore.addInteger32(KEY_INDEX_SIZE, currentIndexEntryCount + 1L);
    }

    private static void updateViewsInDatastore(DataStore dataStore, long sourceCameraId, long targetCameraId, CamerasParser parser) {
        final Map<Long, List<DataStore>> cameraViews = parser.getCameraViews();
        final List<DataStore> clonedViewStores = cameraViews.get(sourceCameraId).stream()
                .map(originalViewStore -> cloneViewStoreForNewCamera(originalViewStore, targetCameraId))
                .collect(toList());

        AtomicInteger viewIndex = new AtomicInteger(parser.getTotalViewCount());
        clonedViewStores
                .forEach(clonedViewStore -> dataStore.mergeRepeatedValues(KEY_VIEWS, viewIndex.getAndIncrement(), clonedViewStore));
    }

    private static DataStore cloneViewStoreForNewCamera(DataStore viewStore, long targetCameraId) {
        DataStore newStore = viewStore.copy();

        newStore.addInteger32(KEY_CAMERA_ID, targetCameraId);

        return newStore;
    }

    private static ByteArrayInputStream getCamerasInputStream(String sourceCameraFile) throws IOException {
        return new ByteArrayInputStream(readAllBytes(Paths.get(sourceCameraFile)));
    }

    @Deprecated
    private static void checkCameraSetExists(long cameraId, CamerasParser camerasParser) {
        if (!cameraSetExists(cameraId, camerasParser)) {
            throw new NoSuchElementException("Unknown source camera identifier: " + cameraId);
        }
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
