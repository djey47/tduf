package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
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
import static fr.tduf.gui.installer.common.InstallerConstants.DIRECTORY_3D;
import static fr.tduf.gui.installer.common.InstallerConstants.DIRECTORY_SOUND;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.EXTERIOR_MODEL;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.INTERIOR_MODEL;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.SOUND;
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
        asList(DIRECTORY_3D, DIRECTORY_SOUND/*, DIRECTORY_RIMS, DIRECTORY_GAUGES_LOW, DIRECTORY_GAUGES_HIGH*/)
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
//            case DIRECTORY_GAUGES_LOW:
//                targetPath = Paths.get(banksDirectory, "FrontEnd");
//                break;
//            case DIRECTORY_RIMS:
//                targetPath = Paths.get(banksDirectory, "Vehicules", "Rim");
//                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetName);
        }
        return targetPath;
    }

    private static void copyAsset(Path assetPath, Path targetPath, String assetName, BulkDatabaseMiner miner, String slotReference) throws IOException {

        FilesHelper.createDirectoryIfNotExists(targetPath.toString());

        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
        String assetFileName = assetPath.getFileName().toString();
//        Path parentName = assetPath.getParent().getFileName();

        if (DIRECTORY_3D.equals(assetName)) {

            final String targetFileName;
            if (getNameWithoutExtension(assetFileName).endsWith(FileConstants.SUFFIX_INTERIOR_BANK_FILE)) {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, INTERIOR_MODEL);
            } else {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);
            }

            copySingleAsset(assetPath, targetPath, targetFileName);
        }

        if (DIRECTORY_SOUND.equals(assetName)) {

            final String targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);

            copySingleAsset(assetPath, targetPath, targetFileName);
        }

//        if (DIRECTORY_RIMS.equals(assetName)) {
//            targetPath = targetPath.resolve(parentName);
//        }
//
//        if (DIRECTORY_GAUGES_LOW.equals(assetName)) {
//            targetPath = targetPath.resolve("LowRes").resolve("Gauges");
//        }
//
//        if (DIRECTORY_GAUGES_HIGH.equals(assetName)) {
//            targetPath = targetPath.resolve("HiRes").resolve("Gauges");
//        }
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
