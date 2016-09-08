package fr.tduf.gui.database.domain.javafx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Only applies to a content entry.
 * Also includes database entry identifier (optional).
 */
public class ContentEntryDataItem {
    private IntegerProperty internalEntryId = new SimpleIntegerProperty();

    private StringProperty reference = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public IntegerProperty internalEntryIdProperty() {
        return internalEntryId;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public void setInternalEntryId(int internalEntryId) {
        this.internalEntryId.set(internalEntryId);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
