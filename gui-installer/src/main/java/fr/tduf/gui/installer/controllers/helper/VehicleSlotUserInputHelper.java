package fr.tduf.gui.installer.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.VehicleSlotsStageController;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.gui.installer.stages.VehicleSlotsStageDesigner;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides methods to request input from user
 */
public class VehicleSlotUserInputHelper {
    private static final String THIS_CLASS_NAME = VehicleSlotUserInputHelper.class.getSimpleName();

    private VehicleSlotUserInputHelper() {}

    /**
     * Invokes slot dialog to select one to perform install. Updates provided context with selection.
     * @param context       : information about loaded database
     * @param parentWindow  : container to host selection dialog
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

        VehicleSlotsStageController slotsBrowserController = initSlotsBrowserController(parentWindow);
        VehicleSlotDataItem selectedItem = slotsBrowserController.initAndShowModalDialog(context.getMiner());

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        String slotRef = selectedItem.referenceProperty().get();
        VehicleSlot vehicleSlot = VehicleSlotsHelper.load(context.getMiner()).getVehicleSlotFromReference(slotRef)
                .orElseThrow(() -> new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotRef)));

        context.getUserSelection().selectVehicleSlot(vehicleSlot);
    }

    private static VehicleSlotsStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return VehicleSlotsStageDesigner.init(stage);
    }
}
