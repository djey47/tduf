package fr.tduf.gui.installer.domain.javafx;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.Dealer;
import javafx.beans.property.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Represents data to be displayed in TableView.
 * Only applies to a vehicle dealers and slot.
 */
public class DealerSlotData {

    private DealerDataItem dealerDataItem;
    private SlotDataItem slotDataItem;

    /**
     * Unique way to create an instance
     * @param dealerDataItem
     * @param slotDataItem
     * @return
     */
    public static DealerSlotData from(DealerDataItem dealerDataItem, SlotDataItem slotDataItem) {
        DealerSlotData dataItem = new DealerSlotData();

        dataItem.dealerDataItem = requireNonNull(dealerDataItem, "Dealer data is required.");
        dataItem.slotDataItem = requireNonNull(slotDataItem, "Slot data is required.");

        return dataItem;
    }

    public DealerDataItem getDealerDataItem() {
        return dealerDataItem;
    }

    public SlotDataItem getSlotDataItem() {
        return slotDataItem;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("dealerDataItem", dealerDataItem)
                .append("slotDataItem", slotDataItem)
                .toString();
    }

    public static class DealerDataItem {

        private ObjectProperty<Dealer> dealer = new SimpleObjectProperty<>();
        private StringProperty reference = new SimpleStringProperty();
        private ObjectProperty<List<Dealer.Slot>> slots = new SimpleObjectProperty<>();
        private StringProperty name = new SimpleStringProperty();
        private StringProperty freeSlots = new SimpleStringProperty();
        private StringProperty location = new SimpleStringProperty();

        public static DealerDataItem fromDealer(Dealer dealer) {
            DealerDataItem item = new DealerDataItem();

            item.dealer.setValue(dealer);
            item.reference.setValue(dealer.getRef());
            item.slots.setValue(dealer.getSlots());
            item.name.setValue(dealer.getDisplayedName().getValue());
            item.freeSlots.setValue(String.format(DisplayConstants.LABEL_FMT_FREE_SLOTS, dealer.computeFreeSlotCount(), dealer.getSlots().size()));
            item.location.setValue(dealer.getLocation());

            return item;
        }

        public ObjectProperty<Dealer> dealerProperty() {
            return dealer;
        }

        public StringProperty referenceProperty() {
            return reference;
        }

        public ObjectProperty<List<Dealer.Slot>> slotsProperty() {
            return slots;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty freeSlotsProperty() {
            return freeSlots;
        }

        public StringProperty locationProperty() {
            return location;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("reference", reference)
                    .append("name", name)
                    .append("location", location)
                    .toString();
        }
    }

    public static class SlotDataItem {

        private IntegerProperty rank = new SimpleIntegerProperty();
        private StringProperty vehicleName = new SimpleStringProperty();

        public static SlotDataItem fromDealerSlot(Dealer.Slot slot) {
            SlotDataItem item = new SlotDataItem();

            final String vehicleName = slot.getVehicleSlot()
                    .filter(vehicleSlot -> !DatabaseConstants.REF_FREE_DEALER_SLOT.equals(vehicleSlot.getRef()))
                    .map(VehicleSlotsHelper::getVehicleName)
                    .orElse(DisplayConstants.LABEL_FREE_DEALER_SLOT);

            item.rank.setValue(slot.getRank());
            item.vehicleNameProperty().setValue(vehicleName);

            return item;
        }

        public IntegerProperty rankProperty() {
            return rank;
        }

        public StringProperty vehicleNameProperty() {
            return vehicleName;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("rank", rank)
                    .append("vehicleName", vehicleName)
                    .toString();
        }
    }
}
