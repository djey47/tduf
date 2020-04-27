package fr.tduf.gui.database.factory;

import fr.tduf.gui.database.converter.ContentEntryToStringConverter;
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
                String displayedText = ContentEntryToStringConverter.getLabelFromEntry(item);
                setText(displayedText);

                super.updateItem(item, empty);
            }
        };
    }
}
