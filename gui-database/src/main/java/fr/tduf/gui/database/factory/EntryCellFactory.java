package fr.tduf.gui.database.factory;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EntryCellFactory implements Callback<ListView<ContentEntryDataItem>, ListCell<ContentEntryDataItem>> {
    @Override
    public ListCell<ContentEntryDataItem> call(ListView<ContentEntryDataItem> param) {
        return new ListCell<ContentEntryDataItem>() {
            @Override
            protected void updateItem(ContentEntryDataItem item, boolean empty) {

                if (item == null) {
                    setText(null);
                } else {
                    // TODO Replace ?? with new DisplayConstant
                    setText(String.format(DisplayConstants.VALUE_ENTRY_CELL, item.internalEntryIdProperty().get() + 1, item.valueProperty().get()));
                }

                super.updateItem(item, empty);
            }
        };
    }
}