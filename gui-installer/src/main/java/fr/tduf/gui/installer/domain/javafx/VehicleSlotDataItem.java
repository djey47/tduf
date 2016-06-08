package fr.tduf.gui.installer.domain.javafx;

import javafx.beans.property.*;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents data to be displayed in a TableView.
 * Only applies to a vehicle slot.
 * Also includes database entry identifier (optional).
 */
public class VehicleSlotDataItem {
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

    public void setModded(boolean modded) {
        this.modded.set(modded);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
