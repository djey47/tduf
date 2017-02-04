package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

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
    public String toString(ContentEntryDataItem entry) {
        return entry == null ?
                null:
                String.format(DisplayConstants.VALUE_ENTRY_CELL, currentEntryIndexProperty.getValue() + 1, currentEntryLabelProperty.get());
    }

    @Override
    public ContentEntryDataItem fromString(String label) {
        return label == null ?
                null:
                browsableEntries.get(currentEntryIndexProperty.getValue());
    }
}
