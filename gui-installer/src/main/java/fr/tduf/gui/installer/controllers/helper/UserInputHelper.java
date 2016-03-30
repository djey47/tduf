package fr.tduf.gui.installer.controllers.helper;

import com.esotericsoftware.minlog.Log;
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
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides methods to request input from user
 */
public class UserInputHelper {
    private static final String THIS_CLASS_NAME = UserInputHelper.class.getSimpleName();

    /**
     * Invokes slot dialog to select one to perform install. Updates provided context with selection.
     */
    public static void selectAndDefineVehicleSlot(DatabaseContext context, Window parentWindow) throws IOException {
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
        if (selectedItem == null) {
            throw new IOException("Aborted by user");
        }

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        Optional<String> potentialVehicleSlot = selectedItem
                .map((item) -> item.referenceProperty().get());
        potentialVehicleSlot.ifPresent((slotRef) -> createPatchPropertiesForVehicleSlot(slotRef, context.getPatchProperties(), context.getMiner()));
        if (!potentialVehicleSlot.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No vehicle slot selected, will be creating a new one.");
        }
    }

    private static void createPatchPropertiesForVehicleSlot(String slotReference, PatchProperties patchProperties, BulkDatabaseMiner miner) {
        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
        int selectedCarIdentifier = vehicleSlotsHelper.getVehicleIdentifier(slotReference);

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.valueOf(selectedCarIdentifier).toString());
    }

    private static SlotsBrowserStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return SlotsBrowserStageDesigner.init(stage);
    }
}
