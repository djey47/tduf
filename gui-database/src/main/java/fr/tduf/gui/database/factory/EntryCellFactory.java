package fr.tduf.gui.database.factory;

import fr.tduf.gui.database.domain.RemoteResource;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EntryCellFactory implements Callback<ListView<RemoteResource>, ListCell<RemoteResource>> {
    @Override
    public ListCell<RemoteResource> call(ListView<RemoteResource> param) {
        return new ListCell<RemoteResource>() {
            @Override
            protected void updateItem(RemoteResource item, boolean empty) {

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