package fr.tduf.gui.installer.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.controllers.DealerSlotsStageController;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
import fr.tduf.gui.installer.stages.DealerSlotsStageDesigner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides methods to request input from user
 */
public class DealerSlotUserInputHelper {
    private static final String THIS_CLASS_NAME = DealerSlotUserInputHelper.class.getSimpleName();

    private DealerSlotUserInputHelper() {}

    /**
     * Invokes dealer slot dialog to select one to perform install. Updates provided context with selection.
     */
    public static void selectAndDefineDealerSlot(DatabaseContext context, Window parentWindow) throws Exception {
        requireNonNull(context, "Database context is required.");
        requireNonNull(context.getPatchProperties(), "Patch properties are required.");

        Optional<String> forcedDealerRef = context.getPatchProperties().getDealerReference();
        Optional<Integer> forcedDealerSlot = context.getPatchProperties().getDealerSlot();

        if (forcedDealerRef.isPresent() && forcedDealerSlot.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->Forced using dealer " + forcedDealerRef.get() + " at slot " + forcedDealerSlot.get());
            return;
        }

        Log.info(THIS_CLASS_NAME, "->Selecting dealer slot");

        DealerSlotsStageController dealerSlotsController = initDealerSlotsController(parentWindow);
        Optional<DealerSlotData> selectedItem = dealerSlotsController.initAndShowModalDialog(context.getMiner());

        Log.info(THIS_CLASS_NAME, "->Using dealer slot: " + selectedItem);

        selectedItem.ifPresent(slotData -> {
            createPatchPropertiesForDealerSlot(slotData, context.getPatchProperties());

            context.getUserSelection().selectDealerSlot(slotData);
        });
        if (!selectedItem.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No dealer slot selected, will not locate vehicle.");
        }
    }

    static void createPatchPropertiesForDealerSlot(DealerSlotData dealerSlotData, PatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with dealer slot information");

        patchProperties.setDealerReferenceIfNotExists(dealerSlotData.getDealerDataItem().referenceProperty().get());
        patchProperties.setDealerSlotIfNotExists(dealerSlotData.getSlotDataItem().rankProperty().get());
    }

    private static DealerSlotsStageController initDealerSlotsController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DealerSlotsStageDesigner.init(stage);
    }
}
