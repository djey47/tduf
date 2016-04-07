package fr.tduf.gui.installer.domain.javafx;

import fr.tduf.gui.installer.domain.Dealer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static java.util.Objects.requireNonNull;

/**
 * Represents data to be displayed in TableView.
 * Only applies to a vehicle dealers and slot.
 */
public class DealerSlotData {

    private DealerDataItem dealerDataItem;
    private SlotDataItem slotDataItem;

    public static DealerSlotData from(DealerDataItem dealerDataItem, SlotDataItem slotDataItem) {
        DealerSlotData dataItem = new DealerSlotData();

        dataItem.dealerDataItem = requireNonNull(dealerDataItem, "Dealer data is required.");
        dataItem.slotDataItem = requireNonNull(slotDataItem, "Slot data is required.");

        return dataItem;
    }

    public static class DealerDataItem {

        private StringProperty reference = new SimpleStringProperty();

        public static DealerDataItem fromDealer(Dealer dealer) {
            DealerDataItem item = new DealerDataItem();

            item.reference.setValue(dealer.getRef());

            return item;
        }

        public StringProperty referenceProperty() {
            return reference;
        }
    }

    public static class SlotDataItem {

        private IntegerProperty rank = new SimpleIntegerProperty();

        public static SlotDataItem fromDealerSlot(Dealer dealer, int rank) {
            SlotDataItem item = new SlotDataItem();

            item.rank.setValue(rank);

            return item;
        }

        public IntegerProperty rankProperty() {
            return rank;
        }
    }
}
