package fr.tduf.gui.installer.domain.javafx;

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

    }

    public static class SlotDataItem {

    }
}
