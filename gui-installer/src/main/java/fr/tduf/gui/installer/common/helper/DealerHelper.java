package fr.tduf.gui.installer.common.helper;

import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.libunlimited.common.game.domain.Dealer;
import fr.tduf.libunlimited.common.game.domain.Resource;
import fr.tduf.libunlimited.high.files.db.common.helper.CarShopsHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.*;
import static fr.tduf.gui.installer.common.DisplayConstants.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

/**
 * Component to get advanced information on vehicle dealers.
 */
public class DealerHelper extends CommonHelper {
    /**
     * Criteria for dealer lookups
     */
    public enum DealerKind {
        ALL(ITEM_DEALER_KIND_ALL),
        CAR_DEALER(singletonList(RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_DEALER), ITEM_DEALER_KIND_CAR_DEALER),
        BIKE_DEALER(singletonList(RESOURCE_VALUE_PREFIX_FILE_NAME_BIKE_DEALER), ITEM_DEALER_KIND_BIKE_DEALER),
        RENTAL(singletonList(RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_RENTAL), ITEM_DEALER_KIND_RENTAL);

        private final List<String> fileNamePrefixes;
        private final String label;

        DealerKind(String label) {
            this(null, label);
        }

        DealerKind(List<String> fileNamePrefixes, String label) {
            this.fileNamePrefixes = fileNamePrefixes;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private VehicleSlotsHelper vehicleSlotsHelper;
    private final CarShopsHelper carShopsMetaDataHelper;

    private DealerHelper(BulkDatabaseMiner miner) {
        super(miner);
        vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
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
     * @param dealerKind : kind of dealers to be returned
     * @return all dealers matching dealerKind criteria.
     */
    public List<Dealer> getDealers(DealerKind dealerKind) {
        return miner.getDatabaseTopic(CAR_SHOPS)
                .orElseThrow(() -> new IllegalStateException("No data for car shops topic"))
                .getData().getEntries().stream()
                .filter(carShopsEntry -> entryMatchesDealerKind(carShopsEntry, dealerKind))
                .map(this::dealerEntryToDomainObject)
                .collect(toList());
    }

    /**
     * @param vehicleSlotReference : reference of vehicle slot to be retrieved
     * @return all dealer slots ranks (by dealer reference) used by specified vehicle.
     */
    public Map<String, List<Integer>> searchForVehicleSlot(String vehicleSlotReference) {
        return miner.getDatabaseTopic(CAR_SHOPS)
                .orElseThrow(() -> new IllegalStateException("No data for car shops topic"))
                .getData().getEntries().stream()
                .parallel()
                .collect(toConcurrentMap(
                        DealerHelper::getDealerReferenceFromEntry,
                        entry -> getSlotItemsForVehicle(vehicleSlotReference, entry))).entrySet().stream()
                .filter(mapEntry -> !mapEntry.getValue().isEmpty())
                .collect(toMap(
                        Map.Entry::getKey,
                        mapEntry -> mapEntry.getValue().stream()
                                .map(DealerHelper::getSlotRankFromFieldRank)
                                .collect(Collectors.toList())));
    }

    private boolean entryMatchesDealerKind(ContentEntryDto carShopsEntry, DealerKind dealerkind) {
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
                .forEach(prefix -> {
                    if (fileName.startsWith(prefix)) {
                        matches.set(true);
                    }
                });

        return matches.get();
    }

    // Ignore warning: method reference
    private Dealer dealerEntryToDomainObject(ContentEntryDto dealerEntry) {
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

    // Ignore warning: method reference
    private Dealer.Slot slotItemToDomainObject(ContentItemDto slotItem) {
        return Dealer.Slot.builder()
                .withRank(getSlotRankFromFieldRank(slotItem))
                .havingVehicle(vehicleSlotsHelper.getVehicleSlotFromReference(slotItem.getRawValue()).orElse(null))
                .build();
    }

    private List<Dealer.Slot> getActualSlots(ContentEntryDto carShopsEntry, Optional<DbMetadataDto.DealerMetadataDto> carShopsReference) {
        return carShopsEntry.getItems().stream()
                .filter(item -> item.getFieldRank() >= DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                        && item.getFieldRank() <= DatabaseConstants.FIELD_RANK_DEALER_SLOT_15)
                .filter(slotItem -> ! carShopsReference.isPresent()
                        || carShopsReference.get().getAvailableSlots().contains(getSlotRankFromFieldRank(slotItem)))
                .map(this::slotItemToDomainObject)
                .collect(toList());
    }

    private List<ContentItemDto> getSlotItemsForVehicle(String vehicleSlotReference, ContentEntryDto carShopsEntry) {
        final String dealerReference = getDealerReferenceFromEntry(carShopsEntry);
        Optional<DbMetadataDto.DealerMetadataDto> carShopsReference = carShopsMetaDataHelper.getCarShopsReferenceForDealerReference(dealerReference);

        return carShopsEntry.getItems().stream()
                .filter(item -> item.getFieldRank() >= DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                        && item.getFieldRank() <= DatabaseConstants.FIELD_RANK_DEALER_SLOT_15)
                .filter(slotItem -> ! carShopsReference.isPresent()
                        || carShopsReference.get().getAvailableSlots().contains(getSlotRankFromFieldRank(slotItem)))
                .filter(availableSlotItem -> vehicleSlotReference.equals(availableSlotItem.getRawValue()))
                .collect(toList());
    }

    private static int getSlotRankFromFieldRank(ContentItemDto slotItem) {
        return slotItem.getFieldRank() - DatabaseConstants.FIELD_RANK_DEALER_SLOT_1 + 1;
    }

    private static String getDealerReferenceFromEntry(ContentEntryDto entry) {
        return getStringValueFromDatabaseEntry(entry, 1)
                .orElseThrow(() -> new IllegalStateException("No item at rank 1"));
    }

    // For testing use
    void setVehicleSlotsHelper(VehicleSlotsHelper vehicleSlotsHelper) {
        this.vehicleSlotsHelper = vehicleSlotsHelper;
    }
}
