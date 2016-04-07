package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;

import java.util.List;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle dealers.
 */
public class DealerHelper {
    private final BulkDatabaseMiner miner;

    private DealerHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static DealerHelper load(BulkDatabaseMiner miner) {
        return new DealerHelper(requireNonNull(miner, "Database miner instance is required."));
    }

    /**
     * @return all dealers
     */
    public List<Dealer> getDealers() {

        return miner.getDatabaseTopic(CAR_SHOPS).get().getData().getEntries().stream()

                .map((entry) -> Dealer.builder()
                        .withRef(entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEALER_REF).get().getRawValue())
                        .withSlots(getActualSlots(entry))
                        .build())

                .collect(toList());
    }

    // TODO filter slots with reference if available
    private List<Dealer.Slot> getActualSlots(DbDataDto.Entry carShopsEntry) {

        return carShopsEntry.getItems().stream()

                .filter((item) -> item.getFieldRank() >= DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                        && item.getFieldRank() <= DatabaseConstants.FIELD_RANK_DEALER_SLOT_15)

                .map((slotItem) -> Dealer.Slot.builder().withRank(slotItem.getFieldRank() - DatabaseConstants.FIELD_RANK_DEALER_SLOT_1 + 1).build())

                .collect(toList());
    }
}
