package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Long.valueOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Static methods tho access or modify camera information in datastore.
 */
public class CamerasHelper {
    private static final String THIS_CLASS_NAME = CamerasHelper.class.getSimpleName();

    private static final String KEY_INDEX = "index";
    private static final String KEY_VIEWS = "views";
    private static final String KEY_CAMERA_ID = "cameraId";
    private static final String KEY_VIEW_COUNT = "viewCount";
    private static final String KEY_INDEX_SIZE = "indexSize";

    private CamerasHelper(){}

    /**
     * Creates a camera set at targetCameraId with all views from set at sourceCameraId
     * @param sourceCameraId    : identifier of camera to get views from
     * @param targetCameraId    : identifier of camera to create views. May not exist already, in that case will add a new set
     * @param parser            : parsed cameras contents.
     */
    public static void duplicateCameraSet(long sourceCameraId, long targetCameraId, CamerasParser parser) {
        DataStore dataStore = requireNonNull(parser, "Parser with cameras contents is required.").getDataStore();

        final Map<Long, Short> cameraIndex = parser.getCameraIndex();
        if (!cameraIndex.containsKey(sourceCameraId)
                || !parser.getCameraViews().containsKey(sourceCameraId)) {
            throw new NoSuchElementException("Unknown source camera identifier: " + sourceCameraId);
        }

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
     * Creates all camera sets at targetId with all views from sourceId
     * @param instructions      : list of <sourceCameraId>;<targetCameraId>
     * @param parser            : parsed cameras contents.
     */
    public static void batchDuplicateCameraSets(List<String> instructions, CamerasParser parser) {
        requireNonNull(instructions, "A list of instructions is required.");

        instructions.forEach(instruction -> {
            String[] compounds = instruction.split(";");
            duplicateCameraSet(valueOf(compounds[0]), valueOf(compounds[1]), parser);
        });
    }

    /**
     * @param cameraIdentifier  : identifier of camera
     * @param parser            : parsed cameras contents
     * @return view properties for requested camera.
     */
    public static CameraInfo fetchInformation(long cameraIdentifier, CamerasParser parser) {
        List<DataStore> viewStores = parser.getCameraViews().get(cameraIdentifier);
        if (viewStores == null) {
            throw new NoSuchElementException("Requested camera identifier does not exist: " + cameraIdentifier);
        }

        CameraInfo.CameraInfoBuilder cameraInfoBuilder = CameraInfo.builder()
                .forIdentifier((int) cameraIdentifier);

        viewStores.stream()
                .map(parser::getViewProps)
                .map(CameraInfo.CameraView::fromProps)
                .forEach(cameraInfoBuilder::addView);

        return cameraInfoBuilder.build();
    }

    private static void updateIndexInDatastore(DataStore dataStore, long sourceCameraId, long targetCameraId, Map<Long, Short> cameraIndex) {
        short viewCount = cameraIndex.get(sourceCameraId);
            int currentIndexEntryCount = cameraIndex.size();
            dataStore.addRepeatedIntegerValue(KEY_INDEX, KEY_CAMERA_ID, currentIndexEntryCount, targetCameraId);
            dataStore.addRepeatedIntegerValue(KEY_INDEX, KEY_VIEW_COUNT, currentIndexEntryCount, viewCount);
            dataStore.addInteger(KEY_INDEX_SIZE, currentIndexEntryCount + 1L);
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

        newStore.addInteger(KEY_CAMERA_ID, targetCameraId);

        return newStore;
    }
}
