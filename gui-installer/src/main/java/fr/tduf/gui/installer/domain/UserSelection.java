package fr.tduf.gui.installer.domain;

import fr.tduf.libunlimited.common.game.domain.Dealer;
import fr.tduf.libunlimited.common.game.domain.VehicleSlot;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Caches all domain objects matching choices made by the user
 */
public class UserSelection {
    private Optional<VehicleSlot> vehicleSlot = empty();
    private Optional<Dealer> dealer = empty();
    private int dealerSlotRank = 0;

    private UserSelection() {}

    public Optional<VehicleSlot> getVehicleSlot() {
        return vehicleSlot;
    }

    public Optional<Dealer> getDealer() {
        return dealer;
    }

    public int getDealerSlotRank() {
        return dealerSlotRank;
    }

    public void selectVehicleSlot(VehicleSlot vehicleSlot) {
        this.vehicleSlot = of(vehicleSlot);
    }

    public void resetVehicleSlot() {
        this.vehicleSlot = empty();
    }

    public void selectDealerSlot(Dealer dealer, int slotRank) {
        this.dealer = of(dealer);
        this.dealerSlotRank = slotRank;
    }

    public static UserSelection none() {
        return new UserSelection();
    }
}
