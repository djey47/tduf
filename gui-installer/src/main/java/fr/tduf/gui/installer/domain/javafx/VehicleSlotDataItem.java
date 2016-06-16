package fr.tduf.gui.installer.domain.javafx;

import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.SecurityOptions;
import fr.tduf.gui.installer.domain.VehicleSlot;
import javafx.beans.property.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents data to be displayed in a TableView.
 * Only applies to a vehicle slot.
 */
public class VehicleSlotDataItem {
    private ObjectProperty<VehicleSlot> vehicleSlot = new SimpleObjectProperty<>();
    private StringProperty reference = new SimpleStringProperty();
    private StringProperty name = new SimpleStringProperty();
    private IntegerProperty carId = new SimpleIntegerProperty();
    private BooleanProperty modded = new SimpleBooleanProperty();

    public VehicleSlotDataItem(VehicleSlot vehicleSlot) {
        this.vehicleSlot.set(vehicleSlot);
        reference.setValue(vehicleSlot.getRef());
        name.setValue(VehicleSlotsHelper.getVehicleName(vehicleSlot));
        carId.set(vehicleSlot.getCarIdentifier());
        modded.setValue(SecurityOptions.INSTALLED.equals(vehicleSlot.getSecurityOptions().getOptionOne()));
    }

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

    public ObjectProperty<VehicleSlot> vehicleSlotPoperty() {
        return vehicleSlot;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("reference", reference)
                .append("name", name)
                .append("carId", carId)
                .append("modded", modded)
                .toString();
    }
}
