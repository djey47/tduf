package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.TYPE;
import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.UNKNOWN;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to produce TDU file contents for cameras.
 */
public class CamerasWriter extends GenericWriter<CameraInfoEnhanced> {

    private DataStore sourceStore;

    @Deprecated
    private CamerasWriter(DataStore dataStore) throws IOException {
        super(CameraInfoEnhanced.builder().build());
        this.sourceStore = dataStore;
    }

    private CamerasWriter(CameraInfoEnhanced cameraInfoEnhanced) throws IOException {
        super(cameraInfoEnhanced);
    }

    /**
     * Creates a writer from pre-filled datastore.
     * @param dataStore : store providing data to be written
     */
    @Deprecated
    public static CamerasWriter load(DataStore dataStore) throws IOException {
        return new CamerasWriter(requireNonNull(dataStore, "A data store is required."));
    }

    /**
     * Creates a writer from pre-existing domain object
     * @param cameraInfoEnhanced : store providing data to be written
     */
    public static CamerasWriter load(CameraInfoEnhanced cameraInfoEnhanced) throws IOException {
        return new CamerasWriter(requireNonNull(cameraInfoEnhanced, "A cameras domain object is required."));
    }

    @Override
    protected void fillStore() {
        DataStore dataStore = getDataStore();
        CameraInfoEnhanced data = getData();

        // TODO remove sourceStore and merge later
        if (sourceStore != null) {
            dataStore.mergeAll(this.sourceStore);
            return;
        }

        // TODO extract methods
        // Next gen
        dataStore.addValue("header", UNKNOWN, data.getOriginalDataStore().getRawValue("header")
                .orElseThrow(() -> new IllegalStateException("header entry not found in store")));
        dataStore.addInteger("indexSize", data.getIndexSize());
        dataStore.addValue("magicForty", UNKNOWN, data.getOriginalDataStore().getRawValue("magicForty")
                .orElseThrow(() -> new IllegalStateException("magicForty entry not found in store")));

        AtomicLong currentIndex = new AtomicLong(0);
        data.getIndexEntriesAsStream()
                .forEach(indexEntry -> {
                    long currentIndexAsLong = currentIndex.get();

                    dataStore.addRepeatedIntegerValue("index", "cameraId", currentIndexAsLong, indexEntry.getKey());
                    dataStore.addRepeatedIntegerValue("index", "viewCount", currentIndexAsLong, indexEntry.getValue());
                    currentIndex.incrementAndGet();
                });

        AtomicLong currentViewIndex = new AtomicLong(0);
        data.getViewEntriesAsStream()
                .forEach(viewEntry -> viewEntry.getValue()
                        .forEach(viewEnhanced -> {
                            long currentViewIndexAsLong = currentViewIndex.get();
                            DataStore sourceViewStore = viewEnhanced.getOriginalDataStore();

                            // From domain object
                            dataStore.addRepeatedIntegerValue("views", "cameraId", currentViewIndexAsLong, viewEnhanced.getCameraSetId());
                            dataStore.addRepeatedTextValue("views", "label", currentViewIndexAsLong, viewEnhanced.getLabel());
                            dataStore.addRepeatedIntegerValue("views", "type", currentViewIndexAsLong, viewEnhanced.getKind().getInternalId());
                            dataStore.addRepeatedTextValue("views", "name", currentViewIndexAsLong, viewEnhanced.getName());

                            // From attached view props
                            viewEnhanced.getSettings().entrySet().stream()
                                    .filter(propsEntry -> TYPE != propsEntry.getKey())
                                    .forEach(propsEntry -> {
                                        ViewProps props = propsEntry.getKey();
                                        dataStore.addRepeatedIntegerValue("views", props.getStoreFieldName(), currentViewIndexAsLong, (Long) propsEntry.getValue());
                                    });

                            // From original store (unaltered values)
                            dataStore.addRepeatedRawValue("views", "properties", currentViewIndexAsLong, sourceViewStore.getRawValue("properties")
                                    .orElseThrow(() -> new IllegalStateException("properties entry not found in view store")));
                            dataStore.addRepeatedIntegerValue("views", "tag", currentViewIndexAsLong, sourceViewStore.getInteger("tag")
                                    .orElseThrow(() -> new IllegalStateException("tag entry not found in view store")));
                            dataStore.addRepeatedRawValue("views", "settingsPart1", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart1")
                                    .orElseThrow(() -> new IllegalStateException("settingsPart1 entry not found in view store")));
                            dataStore.addRepeatedRawValue("views", "settingsPart2", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart2")
                                    .orElseThrow(() -> new IllegalStateException("settingsPart2 entry not found in view store")));
                            dataStore.addRepeatedRawValue("views", "settingsPart3", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart3")
                                    .orElseThrow(() -> new IllegalStateException("settingsPart3 entry not found in view store")));
                            dataStore.addRepeatedRawValue("views", "settingsPart4", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart4")
                                    .orElseThrow(() -> new IllegalStateException("settingsPart4 entry not found in view store")));
                            dataStore.addRepeatedRawValue("views", "settingsPart5", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart5")
                                    .orElseThrow(() -> new IllegalStateException("settingsPart5 entry not found in view store")));

                            currentViewIndex.incrementAndGet();
                        }));
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    DataStore getSourceStore() {
        return sourceStore;
    }
}