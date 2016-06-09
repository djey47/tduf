package fr.tduf.gui.installer.controllers.converter;

import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class VehicleSlotDataItemToStringConverter extends StringConverter<VehicleSlotDataItem> {
    @Override
    public String toString(VehicleSlotDataItem slotItem) {
        if (slotItem == null) {
            return "";
        }

        return slotItem.referenceProperty().get();
    }

    @Override
    public VehicleSlotDataItem fromString(String ref) {
        if (StringUtils.isEmpty(ref)) {
            return null;
        }

        VehicleSlotDataItem vehicleSlotDataItem = new VehicleSlotDataItem();
        vehicleSlotDataItem.setReference(ref);
        return vehicleSlotDataItem;
    }
}
