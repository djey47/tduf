package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to produce TDU file contents for cameras.
 */
// TODO see to convert domain object to data store (see CamerasParser TODO)
public class CamerasWriter extends GenericWriter<String> {

    private DataStore sourceStore;

    private CamerasWriter(DataStore dataStore) throws IOException {
        super("");
        this.sourceStore = dataStore;
    }

    /**
     * Creates a writer from pre-filled datastore.
     * @param dataStore : store providing data to be written
     */
    public static CamerasWriter load(DataStore dataStore) throws IOException {
        return new CamerasWriter(requireNonNull(dataStore, "A data store is required."));
    }

    @Override
    protected void fillStore() {
        // TODO Update indexSize as required by formulas
        this.getDataStore().mergeAll(this.sourceStore);
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

    DataStore getSourceStore() {
        return sourceStore;
    }
}