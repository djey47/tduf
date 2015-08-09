package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.banks.mapping.helper.MagicMapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;

import java.io.IOException;
import java.nio.file.*;

import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static java.util.Arrays.asList;

/**
 * Orchestrates all operations to install vehicle mod.
 */
public class InstallSteps {

    /**
     * Entry point for full install
     * @param configuration : settings to install required mod.
     */
    public static void install(InstallerConfiguration configuration) throws IOException {
        // TODO check for errors

        copyFilesStep(configuration);

        updateMagicMapStep(configuration);

        updateDatabaseStep(configuration);
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
                    } catch (IOException e) {
                        e.printStackTrace();
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
    public static void updateDatabaseStep(InstallerConfiguration configuration) {
        System.out.println("Entering step: Update Database");

        // TODO
    }

    private static void copyAssets(String assetName, String assetsDirectory, String banksDirectory) throws IOException {
        System.out.println("Copying assets: " + assetName) ;

        Path assetPath = Paths.get(assetsDirectory, assetName);
        Path targetPath = getTargetPath(assetName, banksDirectory);

        Files.walk(assetPath, 1, FileVisitOption.FOLLOW_LINKS)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> copyAsset(path, targetPath));
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
                // TODO handle hires+lores
                targetPath = Paths.get(banksDirectory, "FrontEnd", "HiRes");
                break;
            case DIRECTORY_RIMS:
                // TODO handle rim manufacturer folder
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

    private static void copyAsset(Path assetPath, Path targetPath) {
        Path finalPath = targetPath.resolve(assetPath.getFileName());

        System.out.println("*> " + assetPath + " to " + finalPath);
        try {
            Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}