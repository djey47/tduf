package fr.tduf.gui.database.domain.javafx;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.OptionalLong;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Only applies to a content entry.
 * Also includes database entry identifier (optional).
 */
public class ContentEntryDataItem {
    private LongProperty internalEntryId = new SimpleLongProperty();

    private StringProperty reference = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public LongProperty internalEntryIdProperty() {
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

    public void setInternalEntryId(long internalEntryId) {
        this.internalEntryId.set(internalEntryId);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
