package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.controllers.SlotsBrowserStageController;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.gui.installer.stages.SlotsBrowserStageDesigner;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.createTempDirectory;
import static java.util.Objects.requireNonNull;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
public class UpdateDatabaseStep extends GenericStep {

    private static final String THIS_CLASS_NAME = UpdateDatabaseStep.class.getSimpleName();

    private static final String PLACEHOLDER_NAME_SLOT_REFERENCE = "SLOTREF";

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Optional<String> potentialVehicleSlot = selectVehicleSlot();
        if (potentialVehicleSlot.isPresent()) {
            getInstallerConfiguration().setEffectiveVehicleSlot(potentialVehicleSlot.get());
        } else {
            // TODO find a way to set effective slot
            Log.info(THIS_CLASS_NAME, "No vehicle slot selected.");
        }

        applyPatches(potentialVehicleSlot);

        repackJsonDatabase();

        // TODO check if all files have been written
    }

    List<String> applyPatches(Optional<String> potentialVehicleSlot) throws IOException, ReflectiveOperationException {
        Log.info(THIS_CLASS_NAME, "->Loading JSON database: " + getDatabaseContext().getJsonDatabaseDirectory());

        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());

        Path patchPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);

        Files.walk(patchPath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .sorted(Comparator.<Path>naturalOrder())

                .forEach((patch) -> {
                    try {
                        applyPatch(patch, patcher, potentialVehicleSlot);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Log.info(THIS_CLASS_NAME, "->Saving JSON database: " + getDatabaseContext().getJsonDatabaseDirectory());

        return DatabaseReadWriteHelper.writeDatabaseTopicsToJson(getDatabaseContext().getTopicObjects(), getDatabaseContext().getJsonDatabaseDirectory());
    }

    void repackJsonDatabase() throws IOException {
        Log.info(THIS_CLASS_NAME, "->Converting JSON database: " + getDatabaseContext());

        String jsonDatabaseDirectory = getDatabaseContext().getJsonDatabaseDirectory();
        String extractedDatabaseDirectory = createTempDirectory();

        JsonGateway.gen(jsonDatabaseDirectory, extractedDatabaseDirectory, false, new ArrayList<>());

        Log.info(THIS_CLASS_NAME, "->Converted TDU database directory: " + extractedDatabaseDirectory);

        String databaseDirectory = getInstallerConfiguration().resolveDatabaseDirectory();

        DatabaseBankHelper.repackDatabaseFromDirectory(extractedDatabaseDirectory, databaseDirectory, Optional.of(jsonDatabaseDirectory), getInstallerConfiguration().getBankSupport());

        Log.info(THIS_CLASS_NAME, "->Repacked database: " + extractedDatabaseDirectory + " to " + databaseDirectory);
    }

    private Optional<String> selectVehicleSlot() throws IOException {
        Log.info(THIS_CLASS_NAME, "->Selecting vehicle slot");

        requireNonNull(getDatabaseContext(), "Database context is required.");

        SlotsBrowserStageController slotsBrowserController = initSlotsBrowserController(getInstallerConfiguration().getMainWindow());

        Optional<VehicleSlotDataItem> selectedItem = slotsBrowserController.initAndShowModalDialog(Optional.empty(), getDatabaseContext().getMiner());

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        return selectedItem
                .map((item) -> item.referenceProperty().get());
    }

    private static void applyPatch(Path patchPath, DatabasePatcher patcher, Optional<String> potentialVehicleSlot) throws IOException {
        Log.info(THIS_CLASS_NAME, "*> Now applying patch: " + patchPath);

        final File patchFile = patchPath.toFile();
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);

        potentialVehicleSlot.ifPresent((slotRef) -> overridePatchPropertiesForVehicleSlot(slotRef, patchProperties));

        PatchProperties effectivePatchProperties = patcher.applyWithProperties(patchObject, patchProperties);

        PatchPropertiesReadWriteHelper.writePatchProperties(effectivePatchProperties, patchFile.getAbsolutePath());
    }

    private static void overridePatchPropertiesForVehicleSlot(String slotRef, PatchProperties patchProperties) {
        patchProperties.register(PLACEHOLDER_NAME_SLOT_REFERENCE, slotRef);
    }

    private static SlotsBrowserStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return SlotsBrowserStageDesigner.init(stage);
    }
}
