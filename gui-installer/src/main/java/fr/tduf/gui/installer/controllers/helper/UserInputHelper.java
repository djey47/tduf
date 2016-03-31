package fr.tduf.gui.installer.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.SlotsBrowserStageController;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.gui.installer.stages.SlotsBrowserStageDesigner;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.Files.getNameWithoutExtension;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.EXTERIOR_MODEL;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Provides methods to request input from user
 */
public class UserInputHelper {
    private static final String THIS_CLASS_NAME = UserInputHelper.class.getSimpleName();

    /**
     * Invokes slot dialog to select one to perform install. Updates provided context with selection.
     */
    public static void selectAndDefineVehicleSlot(DatabaseContext context, Window parentWindow) throws Exception {
        requireNonNull(context, "Database context is required.");
        requireNonNull(context.getPatchProperties(), "Patch properties are required.");

        Optional<String> forcedVehicleSlotRef = context.getPatchProperties().getVehicleSlotReference();
        if (forcedVehicleSlotRef.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->Forced using vehicle slot: " + forcedVehicleSlotRef.get());
            return;
        }

        Log.info(THIS_CLASS_NAME, "->Selecting vehicle slot");

        SlotsBrowserStageController slotsBrowserController = initSlotsBrowserController(parentWindow);
        Optional<VehicleSlotDataItem> selectedItem = slotsBrowserController.initAndShowModalDialog(Optional.empty(), context.getMiner());

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        Optional<String> potentialVehicleSlot = selectedItem
                .map((item) -> item.referenceProperty().get());
        potentialVehicleSlot.ifPresent((slotRef) -> createPatchPropertiesForVehicleSlot(slotRef, context.getPatchProperties(), context.getMiner()));
        if (!potentialVehicleSlot.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No vehicle slot selected, will be creating a new one.");
        }
    }

    static void createPatchPropertiesForVehicleSlot(String slotReference, PatchProperties patchProperties, BulkDatabaseMiner miner) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with slot information");

        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
        int selectedCarIdentifier = vehicleSlotsHelper.getVehicleIdentifier(slotReference);
        String selectedBankName = getNameWithoutExtension(vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL));
        String selectedResourceBankName = vehicleSlotsHelper.getCarFileNameReference(slotReference);
        String selectedRimReference = vehicleSlotsHelper.getDefaultRimIdentifier(slotReference);
        String selectedResourceRimBrandReference = vehicleSlotsHelper.getDefaultRimDirectoryResource(slotReference);
        String selectedFrontRimBank = getNameWithoutExtension(vehicleSlotsHelper.getBankFileName(slotReference, FRONT_RIM));
        String selectedResourceFrontRimBankName = vehicleSlotsHelper.getDefaultRimFileNameReference(slotReference, FRONT_RIM);
        String selectedRearRimBank = getNameWithoutExtension(vehicleSlotsHelper.getBankFileName(slotReference, REAR_RIM));
        String selectedResourceRearRimBankName = vehicleSlotsHelper.getDefaultRimFileNameReference(slotReference, REAR_RIM);

        List<String> values = asList(selectedBankName, selectedResourceBankName, selectedRimReference, selectedFrontRimBank, selectedRearRimBank, selectedResourceFrontRimBankName, selectedResourceRearRimBankName);
        if (selectedCarIdentifier == VehicleSlotsHelper.DEFAULT_VEHICLE_ID
                || values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException("Unable to get valid information for vehicle slot: " + slotReference);
        }

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.valueOf(selectedCarIdentifier).toString());
        patchProperties.setBankNameIfNotExists(selectedBankName);
        patchProperties.setResourceBankNameIfNotExists(selectedResourceBankName);

        patchProperties.setRimsSlotReferenceIfNotExists(selectedRimReference, 1);
        patchProperties.setResourceRimsBrandIfNotExists(selectedResourceRimBrandReference, 1);
        patchProperties.setFrontRimBankNameIfNotExists(selectedFrontRimBank, 1);
        patchProperties.setResourceFrontRimBankIfNotExists(selectedResourceFrontRimBankName, 1);
        patchProperties.setRearRimBankNameIfNotExists(selectedRearRimBank, 1);
        patchProperties.setResourceRearRimBankIfNotExists(selectedResourceRearRimBankName, 1);
    }

    private static SlotsBrowserStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return SlotsBrowserStageDesigner.init(stage);
    }
}
