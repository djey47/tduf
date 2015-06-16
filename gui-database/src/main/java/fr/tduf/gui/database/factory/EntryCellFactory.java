package fr.tduf.gui.database.factory;

import fr.tduf.gui.database.domain.DatabaseEntry;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EntryCellFactory implements Callback<ListView<DatabaseEntry>, ListCell<DatabaseEntry>> {
    @Override
    public ListCell<DatabaseEntry> call(ListView<DatabaseEntry> param) {
        return new ListCell<DatabaseEntry>() {
            @Override
            protected void updateItem(DatabaseEntry item, boolean empty) {

                if (item == null) {
                    setText(null);
                } else {
                    setText(String.format("%d:%s", item.getInternalEntryId() + 1, item.valueProperty().get()));
                }

                super.updateItem(item, empty);
            }
        };
    }
}