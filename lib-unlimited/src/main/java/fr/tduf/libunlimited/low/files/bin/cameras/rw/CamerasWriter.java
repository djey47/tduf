package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;

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
        // TODO remove sourceStore and merge later
        if (sourceStore != null) {
            this.getDataStore().mergeAll(this.sourceStore);
            return;
        }

        // Next gen
        // TODO Update indexSize as required by formulas
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    DataStore getSourceStore() {
        return sourceStore;
    }
}