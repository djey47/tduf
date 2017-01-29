package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.common.TDUCPConstants;
import fr.tduf.gui.installer.common.helper.DealerHelper;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.IntStream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Apply reference patch using generated properties to restore a TDUCP slot to genuine state.
 */
class RestoreSlotStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreSlotStep.class.getSimpleName();

    private DatabasePatcher databasePatcher;

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getDatabaseContext().getUserSelection(), "User selection is required.");

        final VehicleSlot slot = getDatabaseContext().getUserSelection().getVehicleSlot()
                .orElseThrow(() -> new IllegalStateException("No vehicle slot has been selected"));
        final String slotReference = slot.getRef();
        final String carIdentifier = Integer.toString(slot.getCarIdentifier());

        DbPatchDto cleanSlotPatch = FilesHelper.readObjectFromJsonResourceFile(
                DbPatchDto.class,
                InstallerConstants.RESOURCE_NAME_CLEAN_PATCH);

        DbPatchDto resetSlotPatch = FilesHelper.readObjectFromJsonResourceFile(
                DbPatchDto.class,
                retrievePatchResourceName(slotReference));

        DatabasePatchProperties patchProperties = createPatchProperties(slotReference, carIdentifier);

        Log.info(THIS_CLASS_NAME, "->Restoring TDUCP slot...");
        if(databasePatcher == null) {
            databasePatcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());
        }

        DbPatchDto restorePatchObject = DbPatchDto.builder().build();
        restorePatchObject.getChanges().addAll(cleanSlotPatch.getChanges());
        restorePatchObject.getChanges().addAll(resetSlotPatch.getChanges());
        enhancePatchWithDealerOps(restorePatchObject, slotReference);
        databasePatcher.applyWithProperties(restorePatchObject, patchProperties);
    }

    private void enhancePatchWithDealerOps(DbPatchDto patchObject, String vehicleSlotReference) {
        DealerHelper.load(getDatabaseContext().getMiner()).searchForVehicleSlot(vehicleSlotReference)
                .forEach( (dealerReference, ranks) -> {
                    List<DbFieldValueDto> partialEntryValues = ranks.stream()
                            .map(rank -> DbFieldValueDto.fromCouple(rank + DatabaseConstants.DELTA_RANK_DEALER_SLOTS, DatabaseConstants.CODE_FREE_DEALER_SLOT))
                            .collect(toList());
                    DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                            .withType(UPDATE)
                            .forTopic(CAR_SHOPS)
                            .asReference(dealerReference)
                            .withPartialEntryValues(partialEntryValues)
                            .build();
                    patchObject.getChanges().add(changeObject);
                });
    }

    private static String retrievePatchResourceName(String slotReference) {
        boolean carSlotFlag;
        if (VehicleSlotsHelper.isTDUCPNewCarSlot(slotReference)) {
            carSlotFlag = true;
        } else if (VehicleSlotsHelper.isTDUCPNewBikeSlot(slotReference)) {
            carSlotFlag = false;
        } else {
            throw new IllegalArgumentException("Not a TDUCP new slot: " + slotReference);
        }

        return carSlotFlag ? InstallerConstants.RESOURCE_NAME_TDUCP_CAR_PATCH : InstallerConstants.RESOURCE_NAME_TDUCP_BIKE_PATCH;
    }

    private static DatabasePatchProperties createPatchProperties(String slotReference, String carIdentifier) {
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);
        patchProperties.setResourceBankNameIfNotExists(
                String.format(TDUCPConstants.FMT_RES_BANK_FILENAME, carIdentifier));
        patchProperties.register(
                PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_MODEL,
                String.format(TDUCPConstants.FMT_RES_MODEL, carIdentifier));
        patchProperties.register(
                PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_VERSION,
                String.format(TDUCPConstants.FMT_RES_VERSION, carIdentifier));
        patchProperties.setBankNameIfNotExists(
                String.format(TDUCPConstants.FMT_BANK_FILENAME, carIdentifier));
        patchProperties.register(
                PlaceholderConstants.PLACEHOLDER_NAME_MODEL,
                String.format(TDUCPConstants.FMT_MODEL, carIdentifier));
        patchProperties.register(
                PlaceholderConstants.PLACEHOLDER_NAME_VERSION,
                String.format(TDUCPConstants.FMT_VERSION, carIdentifier));

        enhancePropertiesForRims(carIdentifier, patchProperties);

        enhancePropertiesForPaintJobs(carIdentifier, patchProperties);

        enhancePropertiesForInteriors(carIdentifier, patchProperties);

        return patchProperties;
    }

    private static void enhancePropertiesForInteriors(String carIdentifier, DatabasePatchProperties patchProperties) {
        IntStream.rangeClosed(0, 9)
                .forEach(intRank -> {
                    patchProperties.setInteriorReferenceIfNotExists(
                            String.format(TDUCPConstants.FMT_REF_INTERIOR, carIdentifier, intRank), intRank);
                    patchProperties.setInteriorMainColorIdIfNotExists(
                            DatabaseConstants.CODE_INTERIOR_COLOR_NONE, intRank);
                    patchProperties.setInteriorSecondaryColorIdIfNotExists(
                            DatabaseConstants.CODE_INTERIOR_COLOR_NONE, intRank);
                    patchProperties.setInteriorMaterialIdIfNotExists(
                            DatabaseConstants.CODE_INTERIOR_COLOR_NONE, intRank);
                });
    }

    private static void enhancePropertiesForPaintJobs(String carIdentifier, DatabasePatchProperties patchProperties) {
        IntStream.rangeClosed(0, 9)
                .forEach(pjRank -> {
                    patchProperties.setExteriorMainColorIdIfNotExists(
                            DatabaseConstants.CODE_EXTERIOR_COLOR_BLUE_01, pjRank);
                    patchProperties.setExteriorSecondaryColorIdIfNotExists(
                            DatabaseConstants.CODE_EXTERIOR_COLOR_NONE, pjRank);
                    patchProperties.setCalipersColorIdIfNotExists(
                            DatabaseConstants.CODE_EXTERIOR_COLOR_NONE, pjRank);
                    patchProperties.setExteriorColorNameResourceIfNotExists(
                            String.format(TDUCPConstants.FMT_RES_EXT_COLOR, carIdentifier, pjRank), pjRank);
                    patchProperties.setExteriorColorNameIfNotExists(
                            String.format(TDUCPConstants.FMT_EXT_COLOR, carIdentifier, pjRank), pjRank);
                });
    }

    private static void enhancePropertiesForRims(String carIdentifier, DatabasePatchProperties patchProperties) {
        IntStream.rangeClosed(0, 9)
                .forEach(rimRank -> {
                    patchProperties.setRimsSlotReferenceIfNotExists(
                            String.format(TDUCPConstants.FMT_REF_RIM, carIdentifier, rimRank), rimRank);
                    patchProperties.register(
                            String.format(PlaceholderConstants.PLACEHOLDER_NAME_FMT_RESOURCE_RIM_NAME, rimRank),
                            String.format(TDUCPConstants.FMT_RES_RIM_NAME, carIdentifier, rimRank));
                    patchProperties.setResourceFrontRimBankIfNotExists(
                            String.format(TDUCPConstants.FMT_RES_RIM_FRONT_BANK_FILENAME, carIdentifier, rimRank), rimRank);
                    patchProperties.setResourceRearRimBankIfNotExists(
                            String.format(TDUCPConstants.FMT_RES_RIM_REAR_BANK_FILENAME, carIdentifier, rimRank), rimRank);
                    patchProperties.setRimNameIfNotExists(
                            String.format(TDUCPConstants.FMT_RIM_NAME, carIdentifier, rimRank), rimRank);
                    patchProperties.setFrontRimBankNameIfNotExists(
                            String.format(TDUCPConstants.FMT_RIM_FRONT_BANK_FILENAME, carIdentifier, rimRank), rimRank);
                    patchProperties.setRearRimBankNameIfNotExists(
                            String.format(TDUCPConstants.FMT_RIM_REAR_BANK_FILENAME, carIdentifier, rimRank), rimRank);
                });
    }

    // For testing use
    void setPatcherComponent(DatabasePatcher patcher) {
        databasePatcher = patcher;
    }
}
