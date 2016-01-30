package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Only copies files in assets subfolders to correct TDU locations.
 */
public class CopyFilesStep extends GenericStep {

    private static final String THIS_CLASS_NAME = UpdateMagicMapStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getPatchProperties(), "Patch properties are required.");

        String banksDirectory = getInstallerConfiguration().resolveBanksDirectory();
        asList(DIRECTORY_3D, DIRECTORY_SOUND, DIRECTORY_GAUGES_LOW, DIRECTORY_GAUGES_HIGH, DIRECTORY_RIMS)
                .forEach((asset) -> {
                    try {
                        copyAssets(asset, getInstallerConfiguration().getAssetsDirectory(), banksDirectory, getDatabaseContext().getMiner(), getPatchProperties().getVehicleSlotReference().get());
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to perform copy step", ioe);
                    }
                });
    }

    // TODO convert to instance method
    private static void copyAssets(String assetName, String assetsDirectory, String banksDirectory, BulkDatabaseMiner miner, String slotReference) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetName) ;

        Path assetPath = Paths.get(assetsDirectory, assetName);
        Path targetPath = getTargetPath(assetName, banksDirectory);

        Files.walk(assetPath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        copyAsset(path, targetPath, assetName, miner, slotReference);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static Path getTargetPath(String assetName, String banksDirectory) {
        Path banksPath = Paths.get(banksDirectory);
        Path targetPath;
        switch(assetName) {
            case DIRECTORY_3D:
                targetPath = banksPath.resolve("Vehicules");
                break;
            case DIRECTORY_SOUND:
                targetPath = banksPath.resolve("Sound").resolve("Vehicules");
                break;
            case DIRECTORY_GAUGES_HIGH:
                targetPath = banksPath.resolve("FrontEnd").resolve("HiRes").resolve("Gauges");
                break;
            case DIRECTORY_GAUGES_LOW:
                targetPath = banksPath.resolve("FrontEnd").resolve("LowRes").resolve("Gauges");
                break;
            case DIRECTORY_RIMS:
                targetPath = banksPath.resolve("Vehicules").resolve("Rim");
                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetName);
        }
        return targetPath;
    }

    private static void copyAsset(Path assetPath, Path targetPath, String assetName, BulkDatabaseMiner miner, String slotReference) throws IOException {

        FilesHelper.createDirectoryIfNotExists(targetPath.toString());

        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
        String assetFileName = assetPath.getFileName().toString();

        String targetFileName = null;
        if (DIRECTORY_3D.equals(assetName)) {
            if (getNameWithoutExtension(assetFileName).endsWith(FileConstants.SUFFIX_INTERIOR_BANK_FILE)) {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, INTERIOR_MODEL);
            } else {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);
            }
        }

        if (DIRECTORY_SOUND.equals(assetName)) {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);
        }

        if (DIRECTORY_GAUGES_HIGH.equals(assetName)
                || DIRECTORY_GAUGES_LOW.equals(assetName)) {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, HUD);
        }

        if (DIRECTORY_RIMS.equals(assetName)) {
            String rimBrandName = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);
            targetPath = targetPath.resolve(rimBrandName);
            Files.createDirectories(targetPath);

            // TODO replace with regex extraction for more accuracy
            VehicleSlotsHelper.BankFileType rimBankFileType = assetPath.getFileName().toString().contains("_F_") ?
                    FRONT_RIM:
                    REAR_RIM;
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, rimBankFileType);
        }

        if (targetFileName != null) {
            copySingleAsset(assetPath, targetPath, targetFileName);
        }
    }

    private static void copySingleAsset(Path assetPath, Path targetPath, String targetFileName) {
        try {
            Path finalPath = targetPath.resolve(targetFileName);
            Log.info(THIS_CLASS_NAME, "*> " + assetPath + " to " + finalPath);
            Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
