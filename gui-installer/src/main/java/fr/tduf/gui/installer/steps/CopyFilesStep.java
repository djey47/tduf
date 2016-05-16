package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;

import static com.google.common.io.Files.getFileExtension;
import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Only copies files in assets subfolders to correct TDU locations.
 */
class CopyFilesStep extends GenericStep {

    private static final String THIS_CLASS_NAME = CopyFilesStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getDatabaseContext().getPatchProperties(), "Patch properties are required.");

        asList(DIRECTORY_3D, DIRECTORY_SOUND, DIRECTORY_GAUGES_LOW, DIRECTORY_GAUGES_HIGH, DIRECTORY_RIMS)
                .forEach(assetsDirectoryTag -> {
                    try {
                        parseAssetsDirectory(assetsDirectoryTag);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to process " + assetsDirectoryTag + " assets: " + ioe.getMessage(), ioe);
                    }
                });
    }

    private void parseAssetsDirectory(String assetDirectoryTag) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetDirectoryTag);

        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), assetDirectoryTag);
        Path targetPath = getTargetPath(assetDirectoryTag);

        Files.walk(assetPath, 1)

                .filter(Files::isRegularFile)

                .filter(path -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(getFileExtension(path.toString())))

                .forEach(path -> {
                    try {
                        copyAsset(path, targetPath, assetDirectoryTag);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to copy " + path.getFileName() + ": " + ioe.getMessage(), ioe);
                    }
                });
    }

    private Path getTargetPath(String assetDirectoryName) {
        Path banksPath = Paths.get(getInstallerConfiguration().resolveBanksDirectory());
        Path targetPath;
        switch (assetDirectoryName) {
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
                throw new IllegalArgumentException("Unhandled asset type: " + assetDirectoryName);
        }
        return targetPath;
    }

    private void copyAsset(Path assetPath, Path targetPath, String assetDirectoryName) throws IOException {
        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(getDatabaseContext().getMiner());
        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference().get();

        VehicleSlot vehicleSlot = vehicleSlotsHelper.getVehicleSlotFromReference(slotReference).get();

        String targetFileName;
        switch (assetDirectoryName) {
            case DIRECTORY_3D:
                targetFileName = getTargetFileNameForExteriorAndInterior(vehicleSlot, assetPath.getFileName().toString());
                break;
            case DIRECTORY_SOUND:
                targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, SOUND, true);
                break;
            case DIRECTORY_GAUGES_LOW:
            case DIRECTORY_GAUGES_HIGH:
                targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, HUD, true);
                break;
            case DIRECTORY_RIMS:
                targetPath = targetPath.resolve(vehicleSlot.getDefaultRims().getParentDirectoryName().getValue());
                targetFileName = getTargetFileNameForRims(vehicleSlot, assetPath);
                if (targetFileName == null) {
                    return;
                }
                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetDirectoryName);
        }

        copySingleAssetWithBackup(assetPath, targetPath, targetFileName);
    }

    private static String getTargetFileNameForExteriorAndInterior(VehicleSlot vehicleSlot, String assetFileName) {
        String targetFileName;
        if (FileConstants.PATTERN_INTERIOR_MODEL_BANK_FILE_NAME.matcher(assetFileName).matches()) {
            targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, INTERIOR_MODEL, true);
        } else {
            targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, EXTERIOR_MODEL, true);
        }
        return targetFileName;
    }

    private String getTargetFileNameForRims(VehicleSlot vehicleSlot, Path assetPath) throws IOException {
        String targetFileName = null;

        Matcher matcher = FileConstants.PATTERN_RIM_BANK_FILE_NAME.matcher(assetPath.getFileName().toString());
        if (matcher.matches()) {
            String typeGroupValue = matcher.group(1);

            VehicleSlotsHelper.BankFileType rimBankFileType = FileConstants.INDICATOR_FRONT_RIMS.equalsIgnoreCase(typeGroupValue) ? FRONT_RIM : REAR_RIM;
            String targetFileNameForFrontRim = VehicleSlotsHelper.getBankFileName(vehicleSlot, FRONT_RIM, true);
            String targetFileNameForRearRim = VehicleSlotsHelper.getBankFileName(vehicleSlot, REAR_RIM, true);

            if (targetFileNameForFrontRim.equals(targetFileNameForRearRim)) {
                if (REAR_RIM == rimBankFileType) {
                    throw new IllegalArgumentException("Target slot does only accept single rim model for front/rear. Please remove rear rims file from assets.");
                }
                targetFileName = targetFileNameForFrontRim;
            } else {
                if (FRONT_RIM == rimBankFileType) {
                    targetFileName = targetFileNameForFrontRim;
                } else {
                    targetFileName = targetFileNameForRearRim;
                }
            }
        }
        return targetFileName;
    }

    private void copySingleAssetWithBackup(Path assetPath, Path targetPath, String targetFileName) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetPath.toString());


        Path finalPath = targetPath.resolve(targetFileName);
        if (Files.exists(finalPath)) {
            final Path subTree = Paths.get(getInstallerConfiguration().resolveBanksDirectory()).relativize(finalPath);
            final Path backupFinalPath = Paths.get(getInstallerConfiguration().resolveFilesBackupDirectory()).resolve(subTree);

            Log.info(THIS_CLASS_NAME, "*> BACKUP " + finalPath + " to " + backupFinalPath);
            Files.createDirectories(backupFinalPath.getParent());
            Files.copy(finalPath, backupFinalPath);
        }

        Log.info(THIS_CLASS_NAME, "*> INSTALL " + assetPath + " to " + finalPath);
        Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
