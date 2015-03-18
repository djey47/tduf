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
 *
 */
public class CamerasParser extends GenericParser<String> {

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
        return "";
    }

    @Override
    protected String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    /**
     * Returns index: view count per camera id.
     */
    public Map<Long, Short> getCameraIndex() {

        Map<Long, Short> viewCountByCameraId = new LinkedHashMap<>();

        this.getDataStore().getRepeatedValues("index").stream()

                .forEach((store) -> {
                    long cameraId = store.getInteger("cameraId").get();
                    short viewCount = store.getInteger("viewCount").get().shortValue();
                    viewCountByCameraId.put(cameraId, viewCount);
                });

        return viewCountByCameraId;
    }

    /**
     * Returns camera views per camera id.
     */
    public Map<Long, List<DataStore>> getCameraViews() {

        Map<Long, List<DataStore>> viewsByCamera = new LinkedHashMap<>();

        this.getDataStore().getRepeatedValues("views").stream()

                .forEach((store) -> {
                    long cameraId = store.getInteger("cameraId").get();

                    List<DataStore> currentViews;
                    if (viewsByCamera.containsKey(cameraId)) {
                        currentViews = viewsByCamera.get(cameraId);
                    } else {
                        currentViews = new ArrayList<>();
                        viewsByCamera.put(cameraId, currentViews);
                    }

                    currentViews.add(store);
                });

        return viewsByCamera;
    }
}