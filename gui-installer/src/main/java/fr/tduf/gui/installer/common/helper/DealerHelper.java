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
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle dealers.
 */
public class DealerHelper extends CommonHelper {
    /**
     * Criteria for dealer lookups
     */
    public enum DealerKind {
        ALL(),
        CAR_DEALER(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_DEALER),
        BIKE_DEALER(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_BIKE_DEALER),
        DEALER(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_DEALER, DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_BIKE_DEALER),
        RENTAL(DatabaseConstants.RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_RENTAL);

        private final List<String> fileNamePrefixes;

        DealerKind(String... fileNamePrefixes) {
            this.fileNamePrefixes = asList(fileNamePrefixes);
        }
    }

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
     * @return all dealers mathcing dealerKind criteria.
     */
    public List<Dealer> getDealers(DealerKind dealerKind) {
        return miner.getDatabaseTopic(CAR_SHOPS).get().getData().getEntries().stream()

                .filter((carShopsEntry) -> entryMatchesDealerKind(carShopsEntry, dealerKind))

                .map(this::dealerEntryToDomainObject)

                .collect(toList());
    }

    private boolean entryMatchesDealerKind(DbDataDto.Entry carShopsEntry, DealerKind dealerkind) {
        if (DealerKind.ALL == dealerkind) {
            return true;
        }

        Optional<Resource> potentialFileName = getResourceFromDatabaseEntry(carShopsEntry, CAR_SHOPS, DatabaseConstants.FIELD_RANK_DEALER_NAME);
        if (!potentialFileName.isPresent()) {
            return false;
        }

        final String fileName = potentialFileName.get().getValue();
        AtomicBoolean matches = new AtomicBoolean(false);
        dealerkind.fileNamePrefixes
                .forEach((prefix) -> {
                    if (fileName.startsWith(prefix)) {
                        matches.set(true);
                    }
                });

        return matches.get();
    }

    private Dealer dealerEntryToDomainObject(DbDataDto.Entry dealerEntry) {
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
