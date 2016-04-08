package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.common.helper.CarShopsHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle dealers.
 */
public class DealerHelper extends CommonHelper {
    private final VehicleSlotsHelper vehicleSlotsHelper;
    private final CarShopsHelper carShopsMetaDataHelper;

    private DealerHelper(BulkDatabaseMiner miner) {
        super(miner);
        this.vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
        carShopsMetaDataHelper = new CarShopsHelper();
    }

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static DealerHelper load(BulkDatabaseMiner miner) {
        return new DealerHelper(miner);
    }

    /**
     * @return all dealers
     */
    public List<Dealer> getDealers() {
        // TODO filter: add vehicle type filter (ALL, CARS, BIKES)
        return miner.getDatabaseTopic(CAR_SHOPS).get().getData().getEntries().stream()

                .filter((carShopsEntry) -> {
                    Optional<Resource> fileName = getResourceFromDatabaseEntry(carShopsEntry, CAR_SHOPS, DatabaseConstants.FIELD_RANK_DEALER_NAME);

                    return !fileName.isPresent()
                            || fileName.get().getValue().startsWith(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_DEALER)
                            || fileName.get().getValue().startsWith(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_BIKE_DEALER);
                })

                .map((dealerEntry) -> {
                    String dealerReference = dealerEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEALER_REF).get().getRawValue();

                    Optional<Resource> displayedName = getResourceFromDatabaseEntry(dealerEntry, CAR_SHOPS, DatabaseConstants.FIELD_RANK_DEALER_LIBELLE);

                    Optional<DbMetadataDto.DealerMetadataDto> carShopsReference = carShopsMetaDataHelper.getCarShopsReferenceForDealerReference(dealerReference);
                    Optional<String> location = carShopsReference
                            .map(DbMetadataDto.DealerMetadataDto::getLocation);

                    return Dealer.builder()
                            .withRef(dealerReference)
                            .withDisplayedName(displayedName.orElse(null))
                            .withLocation(location.orElse(DisplayConstants.LABEL_UNKNOWN))
                            .withSlots(getActualSlots(dealerEntry, carShopsReference))
                            .build();
                })

                .collect(toList());
    }

    private List<Dealer.Slot> getActualSlots(DbDataDto.Entry carShopsEntry, Optional<DbMetadataDto.DealerMetadataDto> carShopsReference) {

        return carShopsEntry.getItems().stream()

                .filter((item) -> item.getFieldRank() >= DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                        && item.getFieldRank() <= DatabaseConstants.FIELD_RANK_DEALER_SLOT_15)

                .filter((slotItem) -> ! carShopsReference.isPresent()
                        || carShopsReference.get().getAvailableSlots().contains(getSlotRankFromFieldRank(slotItem)))

                .map((slotItem) -> Dealer.Slot.builder()
                        .withRank(getSlotRankFromFieldRank(slotItem))
                        .havingVehicle(vehicleSlotsHelper.getVehicleSlotFromReference(slotItem.getRawValue()).orElse(null))
                        .build())

                .collect(toList());
    }

    private static int getSlotRankFromFieldRank(DbDataDto.Item slotItem) {
        return slotItem.getFieldRank() - DatabaseConstants.FIELD_RANK_DEALER_SLOT_1 + 1;
    }
}
