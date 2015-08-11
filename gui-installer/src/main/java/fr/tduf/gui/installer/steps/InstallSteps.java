package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
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
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.createTempDirectory;
import static java.util.Arrays.asList;

/**
 * Orchestrates all operations to install vehicle mod.
 */
public class InstallSteps {

    /**
     * Entry point for full install
     * @param configuration : settings to install required mod.
     */
    public static void install(InstallerConfiguration configuration) {
        // TODO handle exceptions

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

        try {
            updateDatabaseStep(configuration);
        } catch (IOException | ReflectiveOperationException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Only copies files in assets subfolders to correct TDU locations.
     * @param configuration : settings to perform current step
     */
    public static void copyFilesStep(InstallerConfiguration configuration) {
        System.out.println("Entering step: Copy Files");

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
        System.out.println("Entering step: Update Magic Map");

        String bankDirectory = getTduBanksDirectory(configuration);
        String magicMapFile = Paths.get(bankDirectory, MapHelper.MAPPING_FILE_NAME).toString();

        System.out.println("Magic Map file: " + magicMapFile);

        MagicMapHelper.fixMagicMap(bankDirectory)

                .forEach((fileName) -> System.out.println("*> added checksum of " + fileName));

        return magicMapFile;
    }

    /**
     * @param configuration : settings to perform current step
     */
    public static void updateDatabaseStep(InstallerConfiguration configuration) throws IOException, ReflectiveOperationException {
        System.out.println("Entering step: Update Database");

        String jsonDatabaseDirectory = Files.createTempDirectory("guiInstaller").toString();

        // TODO check if all files have been created
        unpackDatabaseToJson(configuration, jsonDatabaseDirectory);

        // TODO check if all files have been written
        applyPatches(configuration, jsonDatabaseDirectory);

        repackJsonDatabase(configuration, jsonDatabaseDirectory);
    }

    static List<String> unpackDatabaseToJson(InstallerConfiguration configuration, String jsonDatabaseDirectory) throws IOException {
        String databaseDirectory = getTduDatabaseDirectory(configuration);

        System.out.println("Unpacking TDU database: " + databaseDirectory);

        String unpackedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.of(jsonDatabaseDirectory), configuration.getBankSupport());

        System.out.println("Unpacked TDU database directory: " + unpackedDatabaseDirectory);

        List<String> jsonFiles = JsonGateway.dump(unpackedDatabaseDirectory, jsonDatabaseDirectory, false, new ArrayList<>());

        System.out.println("Prepared JSON database directory: " + jsonDatabaseDirectory);

        return jsonFiles;
    }

    static List<String> applyPatches(InstallerConfiguration configuration, String jsonDatabaseDirectory) throws IOException, ReflectiveOperationException {
        System.out.println("Loading JSON database: " + jsonDatabaseDirectory);

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, allTopicObjects);

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

        System.out.println("Saving JSON database: " + jsonDatabaseDirectory);

        return DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, jsonDatabaseDirectory);
    }

    static void repackJsonDatabase(InstallerConfiguration configuration, String jsonDatabaseDirectory) throws IOException {
        System.out.println("Converting JSON database: " + jsonDatabaseDirectory);

        String extractedDatabaseDirectory = createTempDirectory();

        JsonGateway.gen(jsonDatabaseDirectory, extractedDatabaseDirectory, false, new ArrayList<>());

        System.out.println("Converted TDU database directory: " + extractedDatabaseDirectory);

        String databaseDirectory = getTduDatabaseDirectory(configuration);

        DatabaseBankHelper.repackDatabaseFromDirectory(extractedDatabaseDirectory, databaseDirectory, Optional.of(jsonDatabaseDirectory), configuration.getBankSupport());

        System.out.println("Repacked database: " + extractedDatabaseDirectory + " to " + databaseDirectory);
    }

    private static String getTduDatabaseDirectory(InstallerConfiguration configuration) {
        Path banksPath = Paths.get(getTduBanksDirectory(configuration));
        return banksPath.resolve("Database").toString();
    }

    private static void copyAssets(String assetName, String assetsDirectory, String banksDirectory) throws IOException {
        System.out.println("Copying assets: " + assetName) ;

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

        System.out.println("*> " + assetPath + " to " + finalPath);

        try {
            Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyPatch(Path patchPath, DatabasePatcher patcher) throws IOException {
        System.out.println("*> Now applying patch: " + patchPath);

        DbPatchDto patchObject = new ObjectMapper().readValue(patchPath.toFile(), DbPatchDto.class);
        patcher.apply(patchObject);
    }
}