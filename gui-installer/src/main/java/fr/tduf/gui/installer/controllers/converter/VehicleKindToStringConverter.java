package fr.tduf.gui.installer.controllers.converter;

import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import javafx.util.StringConverter;

public class VehicleKindToStringConverter extends StringConverter<VehicleSlotsHelper.VehicleKind> {
    @Override
    public String toString(VehicleSlotsHelper.VehicleKind vehicleKind) {
        return vehicleKind.getLabel();
    }

    @Override
    public VehicleSlotsHelper.VehicleKind fromString(String label) {
        return null;
    }
}
