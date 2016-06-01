package fr.tduf.gui.installer.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.DealerSlotsStageController;
import fr.tduf.gui.installer.controllers.VehicleSlotsStageController;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.PaintJob;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.gui.installer.stages.DealerSlotsStageDesigner;
import fr.tduf.gui.installer.stages.VehicleSlotsStageDesigner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * Provides methods to request input from user
 */
public class UserInputHelper {
    private static final String THIS_CLASS_NAME = UserInputHelper.class.getSimpleName();

    private UserInputHelper() {}

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

        VehicleSlotsStageController slotsBrowserController = initSlotsBrowserController(parentWindow);
        Optional<VehicleSlotDataItem> selectedItem = slotsBrowserController.initAndShowModalDialog(empty(), context.getMiner());

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        Optional<String> potentialVehicleSlot = selectedItem
                .map(item -> item.referenceProperty().get());
        potentialVehicleSlot.ifPresent(slotRef -> {
            VehicleSlot vehicleSlot = VehicleSlotsHelper.load(context.getMiner()).getVehicleSlotFromReference(slotRef)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotRef)));

            createPatchPropertiesForVehicleSlot(vehicleSlot, context.getPatchProperties());

            context.getUserSelection().selectVehicleSlot(vehicleSlot);
        });
        if (!potentialVehicleSlot.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No vehicle slot selected, will be creating a new one.");
        }
    }

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

    static void createPatchPropertiesForVehicleSlot(VehicleSlot vehicleSlot, PatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with slot information");

        String slotReference = vehicleSlot.getRef();
        int selectedCarIdentifier = vehicleSlot.getCarIdentifier();
        if (VehicleSlotsHelper.DEFAULT_VEHICLE_ID == selectedCarIdentifier) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        String selectedBankName = VehicleSlotsHelper.getBankFileName(vehicleSlot, EXTERIOR_MODEL, false);
        String selectedResourceBankName = vehicleSlot.getFileName().getRef();
        List<String> values = asList(selectedBankName, selectedResourceBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.toString(selectedCarIdentifier));
        patchProperties.setBankNameIfNotExists(selectedBankName);
        patchProperties.setResourceBankNameIfNotExists(selectedResourceBankName);

        createPatchPropertiesForRims(vehicleSlot, patchProperties);

        createPatchPropertiesForPaintJobs(vehicleSlot, patchProperties);
    }

    static void createPatchPropertiesForDealerSlot(DealerSlotData dealerSlotData, PatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with dealer slot information");

        patchProperties.setDealerReferenceIfNotExists(dealerSlotData.getDealerDataItem().referenceProperty().get());
        patchProperties.setDealerSlotIfNotExists(dealerSlotData.getSlotDataItem().rankProperty().get());
    }

    private static void createPatchPropertiesForRims(VehicleSlot vehicleSlot, PatchProperties patchProperties) {
        String selectedRimReference = vehicleSlot.getDefaultRims().getRef();
        String selectedResourceRimBrandReference = vehicleSlot.getDefaultRims().getParentDirectoryName().getRef();
        String selectedFrontRimBank = VehicleSlotsHelper.getBankFileName(vehicleSlot, FRONT_RIM, false);
        String selectedResourceFrontRimBankName = vehicleSlot.getDefaultRims().getFrontRimInfo().getFileName().getRef();
        String selectedRearRimBank = VehicleSlotsHelper.getBankFileName(vehicleSlot, REAR_RIM, false);
        String selectedResourceRearRimBankName = vehicleSlot.getDefaultRims().getRearRimInfo().getFileName().getRef();

        List<String> values = asList(selectedRimReference, selectedFrontRimBank, selectedRearRimBank, selectedResourceFrontRimBankName, selectedResourceRearRimBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, vehicleSlot.getRef()));
        }

        patchProperties.setRimsSlotReferenceIfNotExists(selectedRimReference, 1);
        patchProperties.setResourceRimsBrandIfNotExists(selectedResourceRimBrandReference, 1);
        patchProperties.setFrontRimBankNameIfNotExists(selectedFrontRimBank, 1);
        patchProperties.setResourceFrontRimBankIfNotExists(selectedResourceFrontRimBankName, 1);
        patchProperties.setRearRimBankNameIfNotExists(selectedRearRimBank, 1);
        patchProperties.setResourceRearRimBankIfNotExists(selectedResourceRearRimBankName, 1);
    }

    private static void createPatchPropertiesForPaintJobs(VehicleSlot vehicleSlot, PatchProperties patchProperties) {
        AtomicInteger paintJobIndex = new AtomicInteger(1);
        vehicleSlot.getPaintJobs()
                .forEach(paintJob -> {
                    String nameRef = paintJob.getName().getRef();
                    patchProperties.setExteriorColorNameResourceIfNotExists(nameRef, paintJobIndex.getAndIncrement());
                });

        searchFirstInteriorPatternReference(vehicleSlot).ifPresent(ref -> patchProperties.setInteriorReferenceIfNotExists(ref, 1));
    }

    private static Optional<String> searchFirstInteriorPatternReference(VehicleSlot vehicleSlot) {
        List<PaintJob> paintJobs = vehicleSlot.getPaintJobs();
        if (paintJobs.isEmpty()) {
            return empty();
        }

        return paintJobs.get(0).getInteriorPatternRefs().stream()
                .findFirst();
    }

    private static VehicleSlotsStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return VehicleSlotsStageDesigner.init(stage);
    }

    private static DealerSlotsStageController initDealerSlotsController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return DealerSlotsStageDesigner.init(stage);
    }
}
