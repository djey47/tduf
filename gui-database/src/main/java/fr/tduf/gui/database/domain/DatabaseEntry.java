package fr.tduf.gui.database.domain;

import com.google.common.base.MoreObjects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.OptionalLong;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Applies to a resource or content entry.
 * Also includes database entry identifier (optional).
 */
public class DatabaseEntry {
    private OptionalLong internalEntryId;

    private StringProperty reference = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public StringProperty valueProperty() {
        return value;
    }

    /**
     * @return reference-value pair to be displayed for current entry.
     */
    public String toDisplayableValue() {
        return reference.get() + " : " + value.get();
    } // TODO externalize string format

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public long getInternalEntryId() {
        return internalEntryId.getAsLong();
    }

    public void setInternalEntryId(long internalEntryId) {
        this.internalEntryId = OptionalLong.of(internalEntryId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("internalEntryId", internalEntryId)
                .add("reference", reference)
                .add("value", value)
                .toString();
    }
}