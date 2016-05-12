package fr.tduf.gui.installer.controllers.converter;

import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class VehicleSlotDataItemToStringConverter extends StringConverter<Optional<VehicleSlotDataItem>> {
    @Override
    public String toString(Optional<VehicleSlotDataItem> slotItem) {
        if (slotItem == null) {
            return "";
        }

        return slotItem
                .map(item -> item.referenceProperty().get())
                .orElse("");
    }

    @Override
    public Optional<VehicleSlotDataItem> fromString(String ref) {
        if (StringUtils.isEmpty(ref)) {
            return empty();
        }

        VehicleSlotDataItem vehicleSlotDataItem = new VehicleSlotDataItem();
        vehicleSlotDataItem.setReference(ref);
        return of(vehicleSlotDataItem);
    }
}
