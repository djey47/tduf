package fr.tduf.gui.database.factory;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.ContentEntry;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EntryCellFactory implements Callback<ListView<ContentEntry>, ListCell<ContentEntry>> {
    @Override
    public ListCell<ContentEntry> call(ListView<ContentEntry> param) {
        return new ListCell<ContentEntry>() {
            @Override
            protected void updateItem(ContentEntry item, boolean empty) {

                if (item == null) {
                    setText(null);
                } else {
                    setText(String.format(DisplayConstants.VALUE_ENTRY_CELL, item.getInternalEntryId() + 1, item.valueProperty().get()));
                }

                super.updateItem(item, empty);
            }
        };
    }
}