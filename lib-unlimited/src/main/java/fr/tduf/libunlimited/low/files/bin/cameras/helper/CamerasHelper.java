package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public class CamerasHelper {

    /**
     * Creates or replace a camera set at targetCameraId with all views from set at sourceCameraId.
     * @param sourceCameraId    : identifier of camera to get views from
     * @param targetCameraId    : identifier of camera to create views. May not exist already, in that case will add a new set.
     * @param parser            : parsed cameras contents
     */
    public static void duplicateCameraSet(long sourceCameraId, long targetCameraId, CamerasParser parser) {
        DataStore dataStore = requireNonNull(parser, "Parser with cameras contents is required.").getDataStore();

        updateIndexInDatastore(dataStore, sourceCameraId, targetCameraId, parser.getCameraIndex());

        updateViewsInDatastore(dataStore, sourceCameraId, targetCameraId, parser);

        parser.flushCaches();
    }

    private static void updateViewsInDatastore(DataStore dataStore, long sourceCameraId, long targetCameraId, CamerasParser parser) {
        AtomicInteger viewIndex = new AtomicInteger(parser.getTotalViewCount());
        parser.getCameraViews().get(sourceCameraId).stream()

                .map((originalViewStore) -> cloneViewStoreForNewCamera(originalViewStore, targetCameraId))

                .forEach((clonedViewStore) -> dataStore.mergeRepeatedValues("views", viewIndex.getAndIncrement(), clonedViewStore));
    }

    private static void updateIndexInDatastore(DataStore dataStore, long sourceCameraId, long targetCameraId, Map<Long, Short> cameraIndex) {
        int currentIndexEntryCount = cameraIndex.size();
        short viewCount = cameraIndex.get(sourceCameraId);
        dataStore.addRepeatedIntegerValue("index", "cameraId", currentIndexEntryCount, targetCameraId);
        dataStore.addRepeatedIntegerValue("index", "viewCount", currentIndexEntryCount, viewCount);

        dataStore.addInteger("indexSize", currentIndexEntryCount + 1);
    }

    private static DataStore cloneViewStoreForNewCamera(DataStore viewStore, long targetCameraId) {
        DataStore newStore = viewStore.copy();

        newStore.addInteger("cameraId", targetCameraId);

        return newStore;
    }
}