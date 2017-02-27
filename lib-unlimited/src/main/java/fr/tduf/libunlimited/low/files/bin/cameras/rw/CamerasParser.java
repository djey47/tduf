package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Allow to read data from cameras.bin file.
 */
public class CamerasParser extends GenericParser<CamerasDatabase> {

    private CamerasParser(ByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static CamerasParser load(ByteArrayInputStream inputStream) throws IOException {
        return new CamerasParser(
                requireNonNull(inputStream, "A stream containing cameras contents is required"));
    }

    @Override
    protected CamerasDatabase generate() {
        return CamerasDatabase.builder()
                .fromDatastore(getDataStore())
                .withIndex(generateCamerasIndex())
                .withViews(generateCamerasViews())
                .build();
    }

    EnumMap<ViewProps, Object> getViewProps(DataStore viewStore) {
        requireNonNull(viewStore, "View data store is required");

        EnumMap<ViewProps, Object> props = new EnumMap<>(ViewProps.class);
        ViewProps.valuesStream()
                .forEach(prop -> prop.retrieveFrom(viewStore)
                        .ifPresent(val -> props.put(prop, val)));

        return props;
    }

    private Map<Integer, List<CameraView>> generateCamerasViews() {
        Map<Integer, List<CameraView>> views = new LinkedHashMap<>();
        getDataStore().getRepeatedValues("views").forEach(updateViews(views));
        return views;
    }

    private Consumer<DataStore> updateViews(Map<Integer, List<CameraView>> views) {
        return store -> {
            int cameraId = store.getInteger("cameraId")
                    .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"))
                    .intValue();

            List<CameraView> currentViews;
            if (views.containsKey(cameraId)) {
                currentViews = views.get(cameraId);
            } else {
                currentViews = new ArrayList<>(4);
            }
            int kind = store.getInteger("type")
                    .orElseThrow(() -> new IllegalStateException("type attribute not found in store"))
                    .intValue();
            String label = store.getText("label")
                    .orElseThrow(() -> new IllegalStateException("label attribute not found in store"));
            String name = store.getText("name")
                    .orElseThrow(() -> new IllegalStateException("name attribute not found in store"));
            currentViews.add(CameraView.builder()
                    .fromDatastore(store)
                    .forCameraSetId(cameraId)
                    .ofKind(ViewKind.fromInternalId(kind))
                    .withLabel(label)
                    .withName(name)
                    .withSettings(getViewProps(store))
                    .build());

            views.put(cameraId, currentViews);
        };
    }

    private Map<Integer, Short> generateCamerasIndex() {
        int indexSize = getDataStore().getInteger("indexSize")
                .orElseThrow(() -> new IllegalStateException("indexSize attribute not found in store"))
                .intValue();
        Map<Integer, Short> index = new LinkedHashMap<>(indexSize);
        getDataStore().getRepeatedValues("index")
                .forEach(updateIndex(index));
        return index;
    }

    private Consumer<DataStore> updateIndex(Map<Integer, Short> index) {
        return store -> {
            int cameraId = store.getInteger("cameraId")
                    .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"))
                    .intValue();
            short viewCount = store.getInteger("viewCount")
                    .orElseThrow(() -> new IllegalStateException("viewCount attribute not found in store"))
                    .shortValue();
            index.put(cameraId, viewCount);
        };
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }
}
