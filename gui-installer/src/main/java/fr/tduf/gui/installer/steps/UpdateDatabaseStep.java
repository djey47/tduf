package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
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

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        applyPatches();

        repackJsonDatabase();

        // TODO check if all files have been written
    }

    List<String> applyPatches() throws IOException, ReflectiveOperationException {
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
                        applyPatch(patch, patcher);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        Log.info(THIS_CLASS_NAME, "->Saving JSON database: " + getDatabaseContext().getJsonDatabaseDirectory());

        return DatabaseReadWriteHelper.writeDatabaseTopicsToJson(getDatabaseContext().getTopicObjects(), getDatabaseContext().getJsonDatabaseDirectory());
    }

    void repackJsonDatabase() throws IOException {
        Log.info(THIS_CLASS_NAME, "->Converting JSON database: " + getDatabaseContext().getJsonDatabaseDirectory());

        String jsonDatabaseDirectory = getDatabaseContext().getJsonDatabaseDirectory();
        String extractedDatabaseDirectory = createTempDirectory();

        JsonGateway.gen(jsonDatabaseDirectory, extractedDatabaseDirectory, new ArrayList<>());

        Log.info(THIS_CLASS_NAME, "->Converted TDU database directory: " + extractedDatabaseDirectory);

        String databaseDirectory = getInstallerConfiguration().resolveDatabaseDirectory();

        DatabaseBankHelper.repackDatabaseFromDirectory(extractedDatabaseDirectory, databaseDirectory, Optional.of(jsonDatabaseDirectory), getInstallerConfiguration().getBankSupport());

        handleCacheDirectory(databaseDirectory);

        Log.info(THIS_CLASS_NAME, "->Repacked database: " + extractedDatabaseDirectory + " to " + databaseDirectory);
    }

    private void applyPatch(Path patchPath, DatabasePatcher patcher) throws IOException {
        Log.info(THIS_CLASS_NAME, "*> Now applying patch: " + patchPath);

        final File patchFile = patchPath.toFile();
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);

        selectAndDefineVehicleSlot(patchProperties);

        // TODO handle placeholder resolver errors
        PatchProperties effectivePatchProperties = patcher.applyWithProperties(patchObject, patchProperties);

        PatchPropertiesReadWriteHelper.writePatchProperties(effectivePatchProperties, patchFile.getAbsolutePath());

        // TODO handle no patch or many patches (NPE risk or properties erasure)
        setPatchProperties(effectivePatchProperties);
    }

    private void selectAndDefineVehicleSlot(PatchProperties patchProperties) throws IOException {
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Optional<String> forcedVehicleSlotRef = patchProperties.getVehicleSlotReference();
        if (forcedVehicleSlotRef.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->Forced using vehicle slot: " +  forcedVehicleSlotRef.get());
            return;
        }

        Log.info(THIS_CLASS_NAME, "->Selecting vehicle slot");

        SlotsBrowserStageController slotsBrowserController = initSlotsBrowserController(getInstallerConfiguration().getMainWindow());
        Optional<VehicleSlotDataItem> selectedItem = slotsBrowserController.initAndShowModalDialog(Optional.empty(), getDatabaseContext().getMiner());
        if(selectedItem == null) {
            throw new IOException("Aborted by user");
        }

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        Optional<String> potentialVehicleSlot = selectedItem
                .map((item) -> item.referenceProperty().get());
        potentialVehicleSlot.ifPresent((slotRef) -> createPatchPropertiesForVehicleSlot(slotRef, patchProperties));
        if (!potentialVehicleSlot.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No vehicle slot selected, will be creating a new one.");
        }
    }

    private void createPatchPropertiesForVehicleSlot(String slotReference, PatchProperties patchProperties) {
        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(getDatabaseContext().getMiner());
        int selectedCarIdentifier = vehicleSlotsHelper.getVehicleIdentifier(slotReference);

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.valueOf(selectedCarIdentifier).toString());
    }

    // TODO externalize to helper
    private void handleCacheDirectory(String databaseDirectory) throws IOException {
        Path cacheDirectoryPath = Paths.get(databaseDirectory, "json-cache");
        Path lastFilePath = cacheDirectoryPath.resolve("last");

        final File lastFile = lastFilePath.toFile();
        if(lastFile.exists()) {
            Log.debug(THIS_CLASS_NAME, "Database cache timestamp exists, last update: " + lastFile.lastModified());

            // Arbitray adds one second to fight against second-rounding of time in some file systems
            final long now = System.currentTimeMillis() + 1000;
            assert lastFile.setLastModified(now);

            Log.debug(THIS_CLASS_NAME, "Database cache timestamp updated to " + now);
        } else {
            Log.debug(THIS_CLASS_NAME, "Database cache timestamp does not exist, will be created");

            Files.createDirectories(cacheDirectoryPath);
            Files.createFile(lastFilePath);
        }
    }

    private static SlotsBrowserStageController initSlotsBrowserController(Window mainWindow) throws IOException {
        Stage stage = new Stage();
        stage.initOwner(mainWindow);

        return SlotsBrowserStageDesigner.init(stage);
    }
}
