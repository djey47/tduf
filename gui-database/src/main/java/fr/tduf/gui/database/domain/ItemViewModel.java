package fr.tduf.gui.database.domain;

import com.esotericsoftware.minlog.Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Organizes data properties and simplifies read/write access
 */
public class ItemViewModel {
    private static final String THIS_CLASS_NAME = ItemViewModel.class.getSimpleName();

    private Map<Integer, ItemProperties> itemPropsByFieldRank = new HashMap<>();

    /**
     * Removes all properties
     */
    public void clear() {
        itemPropsByFieldRank.clear();

        Log.debug(THIS_CLASS_NAME, "View data store was cleared");
    }

    /**
     * @return true if view model does not contain any property
     */
    public boolean isEmpty() {
        return itemPropsByFieldRank.isEmpty();
    }

    /**
     * @return a simple property for given field rank. Creates a property if it does not exist.
     */
    public StringProperty rawValuePropertyAtFieldRank(int fieldRank) {
        return getItemPropsAtFieldRank(fieldRank).rawValue;
    }

    /**
     * @return a simple property for given field rank. Creates a property if it does not exist.
     */
    public StringProperty resolvedValuePropertyAtFieldRank(int fieldRank) {
        return getItemPropsAtFieldRank(fieldRank).resolvedValue;
    }

    /**
     * @return a simple property for given field rank. Creates a property if it does not exist.
     */
    public BooleanProperty errorPropertyAtFieldRank(int fieldRank) {
        return getItemPropsAtFieldRank(fieldRank).error;
    }

    /**
     * @return a simple property for given field rank. Creates a property if it does not exist.
     */
    public StringProperty errorMessagePropertyAtFieldRank(int fieldRank) {
        return getItemPropsAtFieldRank(fieldRank).errorMessage;
    }

    private ItemProperties getItemPropsAtFieldRank(int fieldRank) {
        ItemProperties props = itemPropsByFieldRank.getOrDefault(fieldRank, new ItemProperties());
        itemPropsByFieldRank.putIfAbsent(fieldRank, props);

        return props;
    }

    /**
     * Property container
     */
    private class ItemProperties {
        private final StringProperty rawValue = new SimpleStringProperty();
        private final StringProperty resolvedValue = new SimpleStringProperty();
        private final BooleanProperty error = new SimpleBooleanProperty(false);
        private final StringProperty errorMessage = new SimpleStringProperty("");
    }
}
