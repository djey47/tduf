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
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetDirectoryName) ;

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
        switch(assetDirectoryName) {
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

        FilesHelper.createDirectoryIfNotExists(targetPath.toString());

        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(getDatabaseContext().getMiner());
        String assetFileName = assetPath.getFileName().toString();
        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference().get();

        String targetFileName = null;
        if (DIRECTORY_3D.equals(assetDirectoryName)) {
            if (FileConstants.PATTERN_INTERIOR_MODEL_BANK_FILE_NAME.matcher(assetFileName).matches()) {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, INTERIOR_MODEL);
            } else {
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);
            }
        }

        if (DIRECTORY_SOUND.equals(assetDirectoryName)) {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);
        }

        if (DIRECTORY_GAUGES_HIGH.equals(assetDirectoryName)
                || DIRECTORY_GAUGES_LOW.equals(assetDirectoryName)) {
            targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, HUD);
        }

        if (DIRECTORY_RIMS.equals(assetDirectoryName)) {
            String rimBrandName = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);
            targetPath = targetPath.resolve(rimBrandName);
            Files.createDirectories(targetPath);

            Matcher matcher = FileConstants.PATTERN_RIM_BANK_FILE_NAME.matcher(assetPath.getFileName().toString());
            if (matcher.matches()) {
                String typeGroupValue = matcher.group(1);
                VehicleSlotsHelper.BankFileType rimBankFileType;
                if (FileConstants.INDICATOR_FRONT_RIMS.equalsIgnoreCase(typeGroupValue)) {
                    rimBankFileType = FRONT_RIM;
                } else {
                    rimBankFileType = REAR_RIM;
                }
                targetFileName = vehicleSlotsHelper.getBankFileName(slotReference, rimBankFileType);
            }
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
