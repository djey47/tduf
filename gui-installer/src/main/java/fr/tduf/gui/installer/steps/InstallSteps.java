package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.controllers.SlotsBrowserStageController;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.gui.installer.stages.SlotsBrowserStageDesigner;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.banks.mapping.helper.MagicMapHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.stage.Stage;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.createTempDirectory;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Orchestrates all operations to install vehicle mod.
 */
public class InstallSteps {

    private static final String THIS_CLASS_NAME = InstallSteps.class.getSimpleName();

    /**
     * Entry point for full install
     * @param configuration : settings to install required mod.
     */
    public static void install(InstallerConfiguration configuration) {
        // TODO handle exceptions

        Log.trace(THIS_CLASS_NAME, "->Starting full install");


        // TODO create database backup to perform rollback is anything fails ?
        DatabaseContext databaseContext = null;
        try {
            databaseContext = loadDatabaseStep(configuration);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        try {
            updateDatabaseStep(configuration, databaseContext);
        } catch (IOException | ReflectiveOperationException ioe) {
            ioe.printStackTrace();
        }

        try {
            copyFilesStep(configuration);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        try {
            updateMagicMapStep(configuration);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Only copies files in assets subfolders to correct TDU locations.
     * @param configuration : settings to perform current step
     */
    public static void copyFilesStep(InstallerConfiguration configuration) {
        Log.trace(THIS_CLASS_NAME, "->Entering step: Copy Files");

        String banksDirectory = getTduBanksDirectory(configuration);
        asList(DIRECTORY_3D, DIRECTORY_RIMS, DIRECTORY_GAUGES, DIRECTORY_SOUND)
                .forEach((asset) -> {
                    try {
                        InstallSteps.copyAssets(asset, configuration.getAssetsDirectory(), banksDirectory);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to perform copy step", ioe);
                    }
                });
    }

    /**
     * Only updates TDU mapping system to accept new files.
     * @param configuration : settings to perform current step
     * @return name of updated magic map file.
     * @throws IOException
     */
    public static String updateMagicMapStep(InstallerConfiguration configuration) throws IOException {
        Log.trace(THIS_CLASS_NAME, "->Entering step: Update Magic Map");

        String bankDirectory = getTduBanksDirectory(configuration);
        String magicMapFile = Paths.get(bankDirectory, MapHelper.MAPPING_FILE_NAME).toString();

        Log.info(THIS_CLASS_NAME, "->Magic Map file: " + magicMapFile);

        MagicMapHelper.fixMagicMap(bankDirectory)

                .forEach((fileName) -> Log.info(THIS_CLASS_NAME, "*> added checksum of " + fileName));

        return magicMapFile;
    }

    /**
     * Applies available patch onto loaded database then convert it to TDU format back
     * @param configuration     : settings to perform current step
     * @param databaseContext   : information to process loaded database
     */
    public static void updateDatabaseStep(InstallerConfiguration configuration, DatabaseContext databaseContext) throws IOException, ReflectiveOperationException {
        Log.trace(THIS_CLASS_NAME, "->Entering step: Update Database");

        requireNonNull(configuration, "Installer configuration is required.");
        requireNonNull(databaseContext, "Database context is required.");

        String vehicleSlot = selectVehicleSlot(databaseContext);

        applyPatches(configuration, databaseContext);

        repackJsonDatabase(configuration, databaseContext);

        // TODO check if all files have been written
    }

    static List<String> unpackDatabaseToJson(InstallerConfiguration configuration, String jsonDatabaseDirectory) throws IOException {
        String databaseDirectory = getTduDatabaseDirectory(configuration);

        Log.info(THIS_CLASS_NAME, "->Unpacking TDU database: " + databaseDirectory);

        String unpackedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.of(jsonDatabaseDirectory), configuration.getBankSupport());

        Log.info(THIS_CLASS_NAME, "->Unpacked TDU database directory: " + unpackedDatabaseDirectory);

        // TODO do not ignore integrity errors!
        List<String> jsonFiles = JsonGateway.dump(unpackedDatabaseDirectory, jsonDatabaseDirectory, false, new ArrayList<>(), new LinkedHashSet<>());

        Log.info(THIS_CLASS_NAME, "->Prepared JSON database directory: " + jsonDatabaseDirectory);

        return jsonFiles;
    }

    static String selectVehicleSlot(DatabaseContext databaseContext) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Selecting vehicle slot");

        requireNonNull(databaseContext, "Database context is required.");

        SlotsBrowserStageController slotsBrowserController = initSlotsBrowserController();

        Optional<VehicleSlotDataItem> selectedItem = slotsBrowserController.initAndShowModalDialog(databaseContext.getMiner());

        Log.info(THIS_CLASS_NAME, "->Using vehicle slot: " + selectedItem);

        return selectedItem
                .map((item) -> item.referenceProperty().get())
                .orElse("");
    }

    static List<String> applyPatches(InstallerConfiguration configuration, DatabaseContext databaseContext) throws IOException, ReflectiveOperationException {
        Log.info(THIS_CLASS_NAME, "->Loading JSON database: " + databaseContext.getJsonDatabaseDirectory());

        requireNonNull(configuration, "Installer configuration is required.");
        requireNonNull(databaseContext, "Database context is required.");

        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, databaseContext.getTopicObjects());

        Path patchPath = Paths.get(configuration.getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);

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

        Log.info(THIS_CLASS_NAME, "->Saving JSON database: " + databaseContext.getJsonDatabaseDirectory());

        return DatabaseReadWriteHelper.writeDatabaseTopicsToJson(databaseContext.getTopicObjects(), databaseContext.getJsonDatabaseDirectory());
    }

    static void repackJsonDatabase(InstallerConfiguration configuration, DatabaseContext databaseContext) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Converting JSON database: " + databaseContext);

        requireNonNull(configuration, "Installer configuration is required.");
        requireNonNull(databaseContext, "Database context is required.");

        String jsonDatabaseDirectory = databaseContext.getJsonDatabaseDirectory();
        String extractedDatabaseDirectory = createTempDirectory();

        JsonGateway.gen(jsonDatabaseDirectory, extractedDatabaseDirectory, false, new ArrayList<>());

        Log.info(THIS_CLASS_NAME, "->Converted TDU database directory: " + extractedDatabaseDirectory);

        String databaseDirectory = getTduDatabaseDirectory(configuration);

        DatabaseBankHelper.repackDatabaseFromDirectory(extractedDatabaseDirectory, databaseDirectory, Optional.of(jsonDatabaseDirectory), configuration.getBankSupport());

        Log.info(THIS_CLASS_NAME, "->Repacked database: " + extractedDatabaseDirectory + " to " + databaseDirectory);
    }

    /**
     * Unpacks TDU database and loads JSON result.
     * @param configuration : settings to perform current step
     * @return information to process loaded database
     * @throws IOException
     */
    private static DatabaseContext loadDatabaseStep(InstallerConfiguration configuration) throws IOException {
        String jsonDatabaseDirectory = Files.createTempDirectory("guiInstaller").toString();

        unpackDatabaseToJson(configuration, jsonDatabaseDirectory);

        // TODO check if all files have been created

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        return new DatabaseContext(allTopicObjects, jsonDatabaseDirectory);
    }

    private static String getTduDatabaseDirectory(InstallerConfiguration configuration) {
        Path banksPath = Paths.get(getTduBanksDirectory(configuration));
        return banksPath.resolve("Database").toString();
    }

    private static void copyAssets(String assetName, String assetsDirectory, String banksDirectory) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetName) ;

        Path assetPath = Paths.get(assetsDirectory, assetName);
        Path targetPath = getTargetPath(assetName, banksDirectory);

        Files.walk(assetPath, 2)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        copyAsset(path, targetPath, assetName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static String getTduBanksDirectory(InstallerConfiguration configuration) {
        return Paths.get(configuration.getTestDriveUnlimitedDirectory(), "Euro", "Bnk").toString();
    }

    private static Path getTargetPath(String assetName, String banksDirectory) {
        Path targetPath;
        switch(assetName) {
            case DIRECTORY_3D:
                targetPath = Paths.get(banksDirectory, "Vehicules");
                break;
            case DIRECTORY_GAUGES:
                targetPath = Paths.get(banksDirectory, "FrontEnd");
                break;
            case DIRECTORY_RIMS:
                targetPath = Paths.get(banksDirectory, "Vehicules", "Rim");
                break;
            case DIRECTORY_SOUND:
                targetPath = Paths.get(banksDirectory, "Sound", "Vehicules");
                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetName);
        }
        return targetPath;
    }

    private static void copyAsset(Path assetPath, Path targetPath, String assetName) throws IOException {

        Path parentName = assetPath.getParent().getFileName();

        if (DIRECTORY_RIMS.equals(assetName)) {
            targetPath = targetPath.resolve(parentName);
        }

        if (DIRECTORY_GAUGES.equals(assetName)) {
            Path gaugesPath = null;
            if (DIRECTORY_HIRES.equals(parentName.toString())) {
                gaugesPath = Paths.get("HiRes");
            } else if (DIRECTORY_LOWRES.equals(parentName.toString())) {
                gaugesPath = Paths.get("LowRes");
            }
            targetPath = targetPath.resolve(gaugesPath).resolve("Gauges");
        }

        FilesHelper.createDirectoryIfNotExists(targetPath.toString());
        Path finalPath = targetPath.resolve(assetPath.getFileName());

        Log.info(THIS_CLASS_NAME, "*> " + assetPath + " to " + finalPath);

        try {
            Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyPatch(Path patchPath, DatabasePatcher patcher) throws IOException {
        Log.info(THIS_CLASS_NAME, "*> Now applying patch: " + patchPath);

        DbPatchDto patchObject = new ObjectMapper().readValue(patchPath.toFile(), DbPatchDto.class);
        patcher.apply(patchObject);
    }

    private static SlotsBrowserStageController initSlotsBrowserController() throws IOException {
        Stage stage = new Stage();
//        Platform.runLater(() -> entriesStage.initOwner(getWindow()));

        return SlotsBrowserStageDesigner.init(stage);
    }

}
