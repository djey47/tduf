package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static fr.tduf.libunlimited.low.files.research.domain.Type.UNKNOWN;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to produce TDU file contents for cameras.
 */
public class CamerasWriter extends GenericWriter<CamerasDatabase> {

    private static final String FIELD_VIEWS = "views";
    private static final String FIELD_INDEX = "index";
    private static final String FIELD_HEADER = "header";
    private static final String FIELD_VIEW_COUNT = "viewCount";
    private static final String FIELD_INDEX_SIZE = "indexSize";
    private static final String FIELD_MAGIC_FORTY = "magicForty";
    private static final String FIELD_CAMERA_ID = "cameraId";
    private static final String FIELD_LABEL = "label";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PROPERTIES = "properties";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_SETTINGS_PART_1 = "settingsPart1";
    private static final String FIELD_SETTINGS_PART_2 = "settingsPart2";
    private static final String FIELD_SETTINGS_PART_3 = "settingsPart3";
    private static final String FIELD_SETTINGS_PART_4 = "settingsPart4";
    private static final String FIELD_SETTINGS_PART_5 = "settingsPart5";

    private CamerasWriter(CamerasDatabase camerasDatabase) throws IOException {
        super(camerasDatabase);
    }

    /**
     * Creates a writer from pre-existing domain object
     * @param camerasDatabase : store providing data to be written
     */
    public static CamerasWriter load(CamerasDatabase camerasDatabase) throws IOException {
        return new CamerasWriter(requireNonNull(camerasDatabase, "A cameras domain object is required."));
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
        CamerasDatabase data = getData() ;

        dataStore.addValue(FIELD_HEADER, UNKNOWN, data.getOriginalDataStore().getRawValue(FIELD_HEADER)
                .orElseThrow(() -> new IllegalStateException("header entry not found in store")));
        dataStore.addInteger32(FIELD_INDEX_SIZE, data.getIndexSize());
        dataStore.addValue(FIELD_MAGIC_FORTY, UNKNOWN, data.getOriginalDataStore().getRawValue(FIELD_MAGIC_FORTY)
                .orElseThrow(() -> new IllegalStateException("magicForty entry not found in store")));
    }

    private void fillViewIndex() {
        AtomicLong currentIndex = new AtomicLong(0);
        getData().getIndexEntriesAsStream()
                .forEach(indexEntry -> fillIndexEntry(currentIndex.getAndIncrement(), indexEntry));
    }

    private void fillIndexEntry(long currentIndex, Map.Entry<Integer, Short> indexEntry) {
        DataStore dataStore = getDataStore();

        dataStore.addRepeatedInteger32(FIELD_INDEX, FIELD_CAMERA_ID, currentIndex, indexEntry.getKey());
        dataStore.addRepeatedInteger32(FIELD_INDEX, FIELD_VIEW_COUNT, currentIndex, indexEntry.getValue());
    }

    private void fillViewSettings() {
        AtomicLong currentViewIndex = new AtomicLong(0);
        getData().getViewEntriesAsStream()
                .flatMap(viewEntry -> viewEntry.getValue().stream())
                .forEach(viewEnhanced -> fillSettingsEntry(currentViewIndex.getAndIncrement(), viewEnhanced));
    }

    private void fillSettingsEntry(long currentViewIndex, CameraView viewEnhanced) {
        DataStore dataStore = getDataStore();
        DataStore sourceViewStore = viewEnhanced.getOriginalDataStore();

        // From domain object
        dataStore.addRepeatedInteger32(FIELD_VIEWS, FIELD_CAMERA_ID, currentViewIndex, viewEnhanced.getCameraSetId());
        dataStore.addRepeatedText(FIELD_VIEWS, FIELD_LABEL, currentViewIndex, viewEnhanced.getLabel());
        dataStore.addRepeatedInteger32(FIELD_VIEWS, FIELD_TYPE, currentViewIndex, viewEnhanced.getKind().getInternalId());
        dataStore.addRepeatedText(FIELD_VIEWS, FIELD_NAME, currentViewIndex, viewEnhanced.getName());

        // From attached view props
        viewEnhanced.getSettings().entrySet()
                .forEach(propsEntry -> fillViewProperties(currentViewIndex, propsEntry));

        // From original store (unaltered values)
        Set<String> fieldNames = new HashSet<>(asList(FIELD_PROPERTIES, FIELD_TAG, FIELD_SETTINGS_PART_1, FIELD_SETTINGS_PART_2, FIELD_SETTINGS_PART_3, FIELD_SETTINGS_PART_4, FIELD_SETTINGS_PART_5));
        sourceViewStore.copyFields(fieldNames, dataStore, FIELD_VIEWS, currentViewIndex);
    }

    private void fillViewProperties(long currentViewIndexAsLong, Map.Entry<ViewProps, Object> propsEntry) {
        Object value = propsEntry.getValue();
        long effectiveValue;
        if (value instanceof Integer) {
            effectiveValue = ((Integer)value).longValue();
        } else {
            effectiveValue = (long) value;
        }

        getDataStore().addRepeatedInteger32(FIELD_VIEWS, propsEntry.getKey().getStoreFieldName(), currentViewIndexAsLong, effectiveValue);
    }
}
