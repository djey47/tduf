package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * Apply reference patch using generated properties to restore a TDUCP slot to genuine state.
 */
public class RestoreSlotStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreSlotStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getDatabaseContext().getPatchObject(), "Snapshot is required.");
        requireNonNull(getDatabaseContext().getPatchProperties(), "Effective patch properties are required.");
        requireNonNull(getDatabaseContext().getUserSelection(), "User selection is required.");

        final VehicleSlot slot = getDatabaseContext().getUserSelection().getVehicleSlot()
                .orElseThrow(() -> new IllegalStateException("No vehicle slot has been selected"));
        final String slotReference = slot.getRef();

        boolean carSlotFlag;
        if (VehicleSlotsHelper.isTDUCPNewCarSlot(slotReference)) {
            carSlotFlag = true;
        } else if (VehicleSlotsHelper.isTDUCPNewBikeSlot(slotReference)) {
            carSlotFlag = false;
        } else {
            throw new IllegalArgumentException("Not a TDUCP new slot: " + slotReference);
        }

        DbPatchDto cleanSlotPatch = FilesHelper.readObjectFromJsonResourceFile(
                DbPatchDto.class,
                FileConstants.RESOURCE_NAME_CLEAN_PATCH);
        DbPatchDto resetSlotPatch = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class,
                carSlotFlag ? FileConstants.RESOURCE_NAME_TDUCP_CAR_PATCH : FileConstants.RESOURCE_NAME_TDUCP_BIKE_PATCH);

        // TODO use format constants (create TDUCP constants class)
        String carIdentifier = Integer.toString(slot.getCarIdentifier());
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);
        patchProperties.setResourceBankNameIfNotExists(carIdentifier + "567");
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_MODEL, carIdentifier + "3407");
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_VERSION, carIdentifier + "8427");
        patchProperties.setBankNameIfNotExists("TDUCP_" + carIdentifier);
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_MODEL, "TDUCP Model " + carIdentifier);
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_VERSION, "Version " + carIdentifier );

        IntStream.rangeClosed(0, 9)
                .forEach(rimRank -> {
                    patchProperties.setRimsSlotReferenceIfNotExists("0000" + carIdentifier + rimRank, rimRank);
                    patchProperties.register(
                            String.format(PlaceholderConstants.PLACEHOLDER_NAME_FMT_RESOURCE_RIM_NAME, rimRank),
                            carIdentifier + rimRank + "562");
                    patchProperties.setResourceFrontRimBankIfNotExists(carIdentifier + rimRank + "1512", rimRank);
                    patchProperties.setResourceRearRimBankIfNotExists(carIdentifier + rimRank + "2512", rimRank);
                    patchProperties.setRimNameIfNotExists("TDUCP " + carIdentifier + " - rim set " + rimRank, rimRank);
                    patchProperties.setFrontRimBankNameIfNotExists("TDUCP_" + carIdentifier + "_F_0" + rimRank, rimRank);
                    patchProperties.setRearRimBankNameIfNotExists("TDUCP_" + carIdentifier + "_R_0" + rimRank, rimRank);
                });

        IntStream.rangeClosed(0, 9)
                .forEach(pjRank -> {
                    patchProperties.setExteriorMainColorIdIfNotExists("54356127", pjRank);
                    patchProperties.setExteriorSecondaryColorIdIfNotExists("53356127", pjRank);
                    patchProperties.setCalipersColorIdIfNotExists("53356127", pjRank);
                    patchProperties.setExteriorColorNameResourceIfNotExists(carIdentifier + pjRank + "457", pjRank);
                    patchProperties.setExteriorColorNameIfNotExists("TDUCP_" + carIdentifier + " exterior color " + pjRank, pjRank);
                });

        IntStream.rangeClosed(0, 9)
                .forEach(intRank -> {
                    patchProperties.setInteriorReferenceIfNotExists(carIdentifier + intRank + "9636", intRank);
                    patchProperties.setInteriorMainColorIdIfNotExists("53364643", intRank);
                    patchProperties.setInteriorSecondaryColorIdIfNotExists("53364643", intRank);
                    patchProperties.setInteriorMaterialIdIfNotExists("53364643", intRank);
                });

        Log.info(THIS_CLASS_NAME, "->Restoring TDUCP slot...");
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());
        patcher.applyWithProperties(cleanSlotPatch, patchProperties);
        patcher.applyWithProperties(resetSlotPatch, patchProperties);
    }
}
