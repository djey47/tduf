package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import static fr.tduf.gui.database.common.DisplayConstants.VALUE_RESOURCE_NONE;

public class ContentEntryToStringConverter extends StringConverter<ContentEntryDataItem> {
    private final ObservableList<ContentEntryDataItem> browsableEntries;
    private final Property<Integer> currentEntryIndexProperty;
    private final StringProperty currentEntryLabelProperty;

    public ContentEntryToStringConverter(ObservableList<ContentEntryDataItem> browsableEntries, Property<Integer> currentEntryIndexProperty, StringProperty currentEntryLabelProperty) {
        this.browsableEntries = browsableEntries;
        this.currentEntryIndexProperty = currentEntryIndexProperty;
        this.currentEntryLabelProperty = currentEntryLabelProperty;
    }

    @Override
    public String toString(ContentEntryDataItem item) {
        if (item == null) {
            return null;
        }

        return getLabelFromEntry(currentEntryIndexProperty.getValue(), currentEntryLabelProperty.getValue());
    }

    @Override
    public ContentEntryDataItem fromString(String label) {
        return label == null ?
                null:
                browsableEntries.get(currentEntryIndexProperty.getValue());
    }

    /**
     * @return Displayed label for specified entry item, or null if it's null
     */
    public static String getLabelFromEntry(ContentEntryDataItem item) {
        if (item == null) {
            return null;
        }

        return getLabelFromEntry(item.internalEntryIdProperty().get(), item.valueProperty().getValue());
    }

    /**
     * @return Displayed label for specified entry information
     */
    public static String getLabelFromEntry(int entryIndex, String entryLabel) {
        return String.format(DisplayConstants.VALUE_ENTRY_CELL, entryIndex + 1, entryLabel)
                        .replace(DatabaseConstants.RESOURCE_VALUE_NONE, VALUE_RESOURCE_NONE);
    }

    /**
     * Getter for testing use
     */
    ObservableList<ContentEntryDataItem> getBrowsableEntries() {
        return browsableEntries;
    }
}
