package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle dealers.
 */
public class DealerHelper {
    private static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    private final BulkDatabaseMiner miner;
    private VehicleSlotsHelper vehicleSlotsHelper;

    private DealerHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
        this.vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
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

                .map((carShopsEntry) -> {
                    Optional<Resource> displayedName = getResourceFromDatabaseEntry(carShopsEntry, CAR_SHOPS, DatabaseConstants.FIELD_RANK_DEALER_LIBELLE);

                    // TODO resolve location with reference
                    return Dealer.builder()
                            .withRef(carShopsEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEALER_REF).get().getRawValue())
                            .withDisplayedName(displayedName.orElse(null))
                            .withLocation("???")
                            .withSlots(getActualSlots(carShopsEntry))
                            .build();
                })

                .collect(toList());
    }

    // TODO filter slots with reference if available
    private List<Dealer.Slot> getActualSlots(DbDataDto.Entry carShopsEntry) {

        return carShopsEntry.getItems().stream()

                .filter((item) -> item.getFieldRank() >= DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                        && item.getFieldRank() <= DatabaseConstants.FIELD_RANK_DEALER_SLOT_15)

                .map((slotItem) -> Dealer.Slot.builder()
                        .withRank(slotItem.getFieldRank() - DatabaseConstants.FIELD_RANK_DEALER_SLOT_1 + 1)
                        .havingVehicle(vehicleSlotsHelper.getVehicleSlotFromReference(slotItem.getRawValue()).orElse(null))
                        .build())

                .collect(toList());
    }

    // TODO move to common helper
    private Optional<Resource> getResourceFromDatabaseEntry(DbDataDto.Entry entry, DbDto.Topic topic, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map((item) -> {
                    String value = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), topic, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), value);
                });
    }
}
