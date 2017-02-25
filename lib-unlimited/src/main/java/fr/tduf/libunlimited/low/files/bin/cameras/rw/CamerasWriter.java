package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraViewEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static fr.tduf.libunlimited.low.files.research.domain.Type.UNKNOWN;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to produce TDU file contents for cameras.
 */
public class CamerasWriter extends GenericWriter<CameraInfoEnhanced> {

    private CamerasWriter(CameraInfoEnhanced cameraInfoEnhanced) throws IOException {
        super(cameraInfoEnhanced);
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
        fillHeadings();

        fillViewIndex();

        fillViewSettings();
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    private void fillHeadings() {
        DataStore dataStore = getDataStore();
        CameraInfoEnhanced data = getData() ;

        dataStore.addValue("header", UNKNOWN, data.getOriginalDataStore().getRawValue("header")
                .orElseThrow(() -> new IllegalStateException("header entry not found in store")));
        dataStore.addInteger32("indexSize", data.getIndexSize());
        dataStore.addValue("magicForty", UNKNOWN, data.getOriginalDataStore().getRawValue("magicForty")
                .orElseThrow(() -> new IllegalStateException("magicForty entry not found in store")));
    }

    private void fillViewIndex() {
        AtomicLong currentIndex = new AtomicLong(0);
        getData().getIndexEntriesAsStream()
                .forEach(indexEntry -> fillIndexEntry(currentIndex, indexEntry));
    }

    private void fillIndexEntry(AtomicLong currentIndex, Map.Entry<Integer, Short> indexEntry) {
        DataStore dataStore = getDataStore();
        long currentIndexAsLong = currentIndex.get();

        dataStore.addRepeatedInteger32("index", "cameraId", currentIndexAsLong, indexEntry.getKey());
        dataStore.addRepeatedInteger32("index", "viewCount", currentIndexAsLong, indexEntry.getValue());
        currentIndex.incrementAndGet();
    }

    private void fillViewSettings() {
        AtomicLong currentViewIndex = new AtomicLong(0);
        getData().getViewEntriesAsStream()
                .forEach(viewEntry -> viewEntry.getValue()
                        .forEach(viewEnhanced -> {
                            fillSettingsEntry(currentViewIndex, viewEnhanced);
                        }));
    }

    private void fillSettingsEntry(AtomicLong currentViewIndex, CameraViewEnhanced viewEnhanced) {
        DataStore dataStore = getDataStore();
        long currentViewIndexAsLong = currentViewIndex.get();
        DataStore sourceViewStore = viewEnhanced.getOriginalDataStore();

        // From domain object
        dataStore.addRepeatedInteger32("views", "cameraId", currentViewIndexAsLong, viewEnhanced.getCameraSetId());
        dataStore.addRepeatedText("views", "label", currentViewIndexAsLong, viewEnhanced.getLabel());
        dataStore.addRepeatedInteger32("views", "type", currentViewIndexAsLong, viewEnhanced.getKind().getInternalId());
        dataStore.addRepeatedText("views", "name", currentViewIndexAsLong, viewEnhanced.getName());

        // From attached view props
        viewEnhanced.getSettings().entrySet()
                .forEach(propsEntry -> fillViewProperties(currentViewIndexAsLong, propsEntry));

        // From original store (unaltered values)
        fillIgnoredValues(currentViewIndexAsLong, sourceViewStore);

        currentViewIndex.incrementAndGet();
    }

    private void fillViewProperties(long currentViewIndexAsLong, Map.Entry<ViewProps, Object> propsEntry) {
        Object value = propsEntry.getValue();
        long effectiveValue;
        if (value instanceof Integer) {
            effectiveValue = ((Integer)value).longValue();
        } else {
            effectiveValue = (long) value;
        }

        getDataStore().addRepeatedInteger32("views", propsEntry.getKey().getStoreFieldName(), currentViewIndexAsLong, effectiveValue);
    }

    private void fillIgnoredValues(long currentViewIndexAsLong, DataStore sourceViewStore) {
        // TODO create store method to transfer values to another (simple -> repeated)
        DataStore dataStore = getDataStore();
        dataStore.addRepeatedValue("views", "properties", currentViewIndexAsLong, sourceViewStore.getRawValue("properties")
                .orElseThrow(() -> new IllegalStateException("properties entry not found in view store")));
        dataStore.addRepeatedInteger32("views", "tag", currentViewIndexAsLong, sourceViewStore.getInteger("tag")
                .orElseThrow(() -> new IllegalStateException("tag entry not found in view store")));
        dataStore.addRepeatedValue("views", "settingsPart1", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart1")
                .orElseThrow(() -> new IllegalStateException("settingsPart1 entry not found in view store")));
        dataStore.addRepeatedValue("views", "settingsPart2", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart2")
                .orElseThrow(() -> new IllegalStateException("settingsPart2 entry not found in view store")));
        dataStore.addRepeatedValue("views", "settingsPart3", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart3")
                .orElseThrow(() -> new IllegalStateException("settingsPart3 entry not found in view store")));
        dataStore.addRepeatedValue("views", "settingsPart4", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart4")
                .orElseThrow(() -> new IllegalStateException("settingsPart4 entry not found in view store")));
        dataStore.addRepeatedValue("views", "settingsPart5", currentViewIndexAsLong, sourceViewStore.getRawValue("settingsPart5")
                .orElseThrow(() -> new IllegalStateException("settingsPart5 entry not found in view store")));
    }
}
