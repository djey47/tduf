package fr.tduf.gui.database.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Also includes database entry identifier (optional).
 */
public class RemoteResource {
    private StringProperty reference = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    private long internalEntryId;

    public StringProperty referenceProperty() {
        return reference;
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

    public long getInternalEntryId() {
        return internalEntryId;
    }

    public void setInternalEntryId(long internalEntryId) {
        this.internalEntryId = internalEntryId;
    }

    public String toDisplayableValue() {
        return reference.get() + " : " + value.get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("reference", reference)
                .add("value", value)
                .toString();
    }
}