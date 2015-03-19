package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Allow to read data from cameras.bin file.
 */
public class CamerasParser extends GenericParser<String> {

    private Map<Long, Short> cachedCameraIndex;

    private Map<Long, List<DataStore>> cachedCameraViews;

    private CamerasParser(ByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static CamerasParser load(ByteArrayInputStream inputStream) throws IOException {
        return new CamerasParser(
                requireNonNull(inputStream, "A stream containing map contents is required"));
    }

    @Override
    protected String generate() {
        return null;
    }

    @Override
    protected String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    /**
     * Returns index: view count per camera id.
     */
    public Map<Long, Short> getCameraIndex() {
        if (cachedCameraIndex != null) {
            return cachedCameraIndex;
        }

        cachedCameraIndex = new LinkedHashMap<>();
        this.getDataStore().getRepeatedValues("index").stream()

                .forEach((store) -> {
                    long cameraId = store.getInteger("cameraId").get();
                    short viewCount = store.getInteger("viewCount").get().shortValue();
                    cachedCameraIndex.put(cameraId, viewCount);
                });

        return cachedCameraIndex;
    }

    /**
     * Returns camera views per camera id.
     */
    public Map<Long, List<DataStore>> getCameraViews() {
        if (cachedCameraViews != null) {
            return cachedCameraViews;
        }

        cachedCameraViews = new LinkedHashMap<>();
        this.getDataStore().getRepeatedValues("views").stream()

                .forEach((store) -> {
                    long cameraId = store.getInteger("cameraId").get();

                    List<DataStore> currentViews;
                    if (cachedCameraViews.containsKey(cameraId)) {
                        currentViews = cachedCameraViews.get(cameraId);
                    } else {
                        currentViews = new ArrayList<>();
                        cachedCameraViews.put(cameraId, currentViews);
                    }

                    currentViews.add(store);
                });

        return cachedCameraViews;
    }

    /**
     *
     */
    // TODO call this method after changing datastore
    public void flushCaches() {
        cachedCameraViews = null;
        cachedCameraIndex = null;
    }

    Map<Long, List<DataStore>> getCachedCameraViews() {
        return cachedCameraViews;
    }

    Map<Long, Short> getCachedCameraIndex() {
        return cachedCameraIndex;
    }
}