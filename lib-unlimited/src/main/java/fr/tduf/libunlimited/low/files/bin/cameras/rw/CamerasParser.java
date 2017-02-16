package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.*;
import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Allow to read data from cameras.bin file.
 */
public class CamerasParser extends GenericParser<CameraInfoEnhanced> {

    private Map<Long, Short> cachedCameraIndex;

    private Map<Long, List<DataStore>> cachedCameraViews;

    private Integer cachedTotalViewCount;

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
    protected CameraInfoEnhanced generate() {
        // TODO extract methods
        int indexSize = getDataStore().getInteger("indexSize")
                .orElseThrow(() -> new IllegalStateException("indexSize attribute not found in store"))
                .intValue();
        Map<Integer, Short> index = new LinkedHashMap<>(indexSize);
        getDataStore().getRepeatedValues("index").forEach(store -> {
            int cameraId = store.getInteger("cameraId")
                    .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"))
                    .intValue();
            short viewCount = store.getInteger("viewCount")
                    .orElseThrow(() -> new IllegalStateException("viewCount attribute not found in store"))
                    .shortValue();
            index.put(cameraId, viewCount);
        });

        Map<Integer, List<CameraViewEnhanced>> views = new LinkedHashMap<>();
        getDataStore().getRepeatedValues("views").forEach(store -> {
            int cameraId = store.getInteger("cameraId")
                    .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"))
                    .intValue();

            List<CameraViewEnhanced> currentViews;
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
            currentViews.add(CameraViewEnhanced.builder()
                    .fromDatastore(store)
                    .forCameraSetId(cameraId)
                    .ofKind(ViewKind.fromInternalId(kind))
                    .withLabel(label)
                    .withName(name)
                    .withSettings(getViewProps(store))
                    .build());

            views.put(cameraId, currentViews);
        });


        return CameraInfoEnhanced.builder()
                .fromDatastore(getDataStore())
                .withIndex(index)
                .withViews(views)
                .build();
    }

    @Override
    public String getStructureResource() {
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
        getDataStore().getRepeatedValues("index").forEach((store) -> {
                    long cameraId = store.getInteger("cameraId")
                            .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"));
                    short viewCount = store.getInteger("viewCount")
                            .orElseThrow(() -> new IllegalStateException("viewCount attribute not found in store"))
                            .shortValue();
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
        getDataStore().getRepeatedValues("views").forEach(store -> {
                    long cameraId = store.getInteger("cameraId")
                            .orElseThrow(() -> new IllegalStateException("cameraId attribute not found in store"));

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
     * Returns count of all registered views.
     */
    public int getTotalViewCount() {
        if (cachedTotalViewCount != null) {
            return cachedTotalViewCount;
        }

        cachedTotalViewCount = getCameraViews().values().stream()
                .mapToInt(List::size)
                .reduce(0, (size1, size2) -> size1 + size2);

        return cachedTotalViewCount;
    }

    /**
     * @return all handled view properties
     */
    public EnumMap<ViewProps, Object> getViewProps(DataStore viewStore) {
        viewStore.getRawValue(ViewProps.TYPE.getStoreFieldName())
                .orElseThrow(() -> new IllegalArgumentException("No view data store provided"));

        EnumMap<ViewProps, Object> props = new EnumMap<>(ViewProps.class);
        ViewProps.valuesStream()
                .forEach(prop -> prop.retrieveFrom(viewStore)
                        .ifPresent(val -> props.put(prop, val)));

        return props;
    }

    /**
     * Resets all caches to reload data from store.
     * Should be used after modifying store contents.
     */
    public void flushCaches() {
        cachedCameraViews = null;
        cachedCameraIndex = null;
        cachedTotalViewCount = null;
    }

    /**
     * @return genuine view type from prop info
     */
    public static Optional<ViewKind> getViewType(DataStore viewStore, DataStoreProps viewProp) {
        return getNumeric(viewStore, viewProp)
                .map(v -> ViewKind.fromInternalId(v.intValue()));
    }

    /**
     * Update view type to prop
     */
    public static void setViewType(Object viewKind, DataStore viewStore, DataStoreProps viewProp) {
        setNumeric(((ViewKind)viewKind).getInternalId(), viewStore, viewProp);
    }

    Map<Long, List<DataStore>> getCachedCameraViews() {
        return cachedCameraViews;
    }

    Map<Long, Short> getCachedCameraIndex() {
        return cachedCameraIndex;
    }

    Integer getCachedTotalViewCount() {
        return cachedTotalViewCount;
    }
}
