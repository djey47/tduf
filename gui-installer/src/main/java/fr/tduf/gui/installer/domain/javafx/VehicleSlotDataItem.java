package fr.tduf.gui.installer.domain.javafx;

import com.google.common.base.MoreObjects;
import javafx.beans.property.*;

import java.util.OptionalLong;

/**
 * Represents data to be displayed in a TableView.
 * Only applies to a vehicle slot.
 * Also includes database entry identifier (optional).
 */
public class VehicleSlotDataItem {
    // TODO remove
    private OptionalLong internalEntryId;

    private StringProperty reference = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();

    private IntegerProperty carId = new SimpleIntegerProperty();

    private BooleanProperty modded = new SimpleBooleanProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public IntegerProperty carIdProperty() {
        return carId;
    }

    public BooleanProperty moddedProperty() { return modded; }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setCarId(int carId) { this.carId.set(carId); }

    public long getInternalEntryId() {
        return internalEntryId.getAsLong();
    }

    public void setInternalEntryId(long internalEntryId) {
        this.internalEntryId = OptionalLong.of(internalEntryId);
    }

    public void setModded(boolean modded) {
        this.modded.set(modded);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("internalEntryId", internalEntryId)
                .add("carId", carId)
                .add("reference", reference)
                .add("name", name)
                .add("modded", modded)
                .toString();
    }
}
