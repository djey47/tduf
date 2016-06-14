package fr.tduf.gui.installer.domain;

import fr.tduf.gui.installer.domain.javafx.DealerSlotData;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Caches all domain objects matching choices made by the user
 */
public class UserSelection {
    private Optional<VehicleSlot> vehicleSlot = empty();
    private Optional<DealerSlotData> dealerSlot = empty();

    private UserSelection() {}

    public Optional<VehicleSlot> getVehicleSlot() {
        return vehicleSlot;
    }

    public Optional<DealerSlotData> getDealerSlot() {
        return dealerSlot;
    }

    public void selectVehicleSlot(VehicleSlot vehicleSlot) {
        this.vehicleSlot = of(vehicleSlot);
    }

    public void resetVehicleSlot() {
        this.vehicleSlot = empty();
    }

    public void selectDealerSlot(DealerSlotData dealerSlotData) {
        this.dealerSlot = of(dealerSlotData);
    }

    public static UserSelection none() {
        return new UserSelection();
    }
}
