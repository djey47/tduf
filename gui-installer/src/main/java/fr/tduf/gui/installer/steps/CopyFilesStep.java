package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
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
public class CopyFilesStep extends GenericStep {

    private static final String THIS_CLASS_NAME = CopyFilesStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getDatabaseContext().getPatchProperties(), "Patch properties are required.");

        asList(DIRECTORY_3D, DIRECTORY_SOUND, DIRECTORY_GAUGES_LOW, DIRECTORY_GAUGES_HIGH, DIRECTORY_RIMS)
                .forEach((assetsDirectory) -> {
                    try {
                        parseAssetsDirectory(assetsDirectory);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to perform copy step", ioe);
                    }
                });
    }

    private void parseAssetsDirectory(String assetDirectoryName) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetDirectoryName);

        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), assetDirectoryName);
        Path targetPath = getTargetPath(assetDirectoryName);

        Files.walk(assetPath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        copyAsset(path, targetPath, assetDirectoryName);
                    } catch (IOException e) {
                        e.printStackTrace();
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

        String targetFileName;
        switch (assetDirectoryName) {
            case DIRECTORY_3D:
                targetFileName = getTargetFileNameForExteriorAndInterior(slotReference, assetPath.getFileName().toString(), vehicleSlotsHelper);
                break;
            case DIRECTORY_SOUND:
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);
                break;
            case DIRECTORY_GAUGES_LOW:
            case DIRECTORY_GAUGES_HIGH:
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, HUD);
                break;
            case DIRECTORY_RIMS:
                targetPath = getTargetRimParentDirectory(slotReference, targetPath, vehicleSlotsHelper);
                targetFileName = getTargetFileNameForRims(slotReference, assetPath, targetPath, vehicleSlotsHelper);
                break;
            default:
                targetFileName = null;
        }

        if (targetFileName != null) {
            copySingleAsset(assetPath, targetPath, targetFileName, true);
        }
    }

    private static Path getTargetRimParentDirectory(String slotReference, Path targetPath, VehicleSlotsHelper vehicleSlotsHelper) throws IOException {
        String rimBrandName = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);
        return targetPath.resolve(rimBrandName);
    }

    private static String getTargetFileNameForExteriorAndInterior(String slotReference, String assetFileName, VehicleSlotsHelper vehicleSlotsHelper) {
        String targetFileName;
        if (FileConstants.PATTERN_INTERIOR_MODEL_BANK_FILE_NAME.matcher(assetFileName).matches()) {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, INTERIOR_MODEL);
        } else {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);
        }
        return targetFileName;
    }

    private static String getTargetFileNameForRims(String slotReference, Path assetPath, Path targetPath, VehicleSlotsHelper vehicleSlotsHelper) {
        String targetFileName = null;

        Matcher matcher = FileConstants.PATTERN_RIM_BANK_FILE_NAME.matcher(assetPath.getFileName().toString());
        if (matcher.matches()) {
            String typeGroupValue = matcher.group(1);

            VehicleSlotsHelper.BankFileType rimBankFileType = FileConstants.INDICATOR_FRONT_RIMS.equalsIgnoreCase(typeGroupValue) ? FRONT_RIM : REAR_RIM;
            String targetFileNameForFrontRim = vehicleSlotsHelper.getBankFileName(slotReference, FRONT_RIM);
            String targetFileNameForRearRim = vehicleSlotsHelper.getBankFileName(slotReference, REAR_RIM);
            if (FRONT_RIM == rimBankFileType) {
                targetFileName = targetFileNameForFrontRim;
            }

            if (!targetFileNameForFrontRim.equals(targetFileNameForRearRim)) {
                if (REAR_RIM == rimBankFileType) {
                    targetFileName = targetFileNameForRearRim;
                } else {
                    // Case of single rim file but slot requires 2 different file names => front file copied to rear if not existing already
                    copySingleAsset(assetPath, targetPath, targetFileNameForRearRim, false);
                }
            }
        }
        return targetFileName;
    }

    private static void copySingleAsset(Path assetPath, Path targetPath, String targetFileName, boolean overwrite) {
        try {
            FilesHelper.createDirectoryIfNotExists(targetPath.toString());

            Path finalPath = targetPath.resolve(targetFileName);

            if (overwrite || !Files.exists(finalPath)) {
                Log.info(THIS_CLASS_NAME, "*> " + assetPath + " to " + finalPath);
                Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
