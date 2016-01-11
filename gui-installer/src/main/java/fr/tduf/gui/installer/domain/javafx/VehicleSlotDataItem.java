package fr.tduf.gui.installer.domain.javafx;

import com.google.common.base.MoreObjects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.OptionalLong;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Only applies to a vehicle slot.
 * Also includes database entry identifier (optional).
 */
public class VehicleSlotDataItem {
    private OptionalLong internalEntryId;

    private StringProperty reference = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setName(String name) {
        this.name.set(name);
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
                .add("name", name)
                .toString();
    }
}
