package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import javafx.collections.ObservableList;

/**
 * Context specific to mapping plugin operations
 * Necessary because of multiple plugin views for a single database entry
 */
public class OnTheFlyMappingContext extends OnTheFlyContext {
    private ObservableList<MappingEntry> files;

    public void setFiles(ObservableList<MappingEntry> files) {
        this.files = files;
    }

    public ObservableList<MappingEntry> getFiles() {
        return files;
    }
}
