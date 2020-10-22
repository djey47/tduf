package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.libunlimited.common.game.domain.RimSlot;
import fr.tduf.libunlimited.common.game.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;

import static fr.tduf.gui.installer.common.InstallerConstants.*;
import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
                        throw new InternalStepException(getType(), "Unable to process " + assetsDirectoryTag + " assets: " + ioe.getMessage(), ioe);
                    }
                });
    }

    private void parseAssetsDirectory(String assetDirectoryTag) throws IOException {
        Log.info(THIS_CLASS_NAME, "->Copying assets: " + assetDirectoryTag);

        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), assetDirectoryTag);
        Path targetPath = getTargetPath(assetDirectoryTag);

        Files.walk(assetPath, 1)

                .filter(Files::isRegularFile)

                .filter(path -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))

                .forEach(path -> {
                    try {
                        copyAsset(path, targetPath, assetDirectoryTag);
                    } catch (IOException ioe) {
                        throw new InternalStepException(getType(), "Unable to copy " + path.getFileName() + ": " + ioe.getMessage(), ioe);
                    }
                });
    }

    private Path getTargetPath(String assetDirectoryName) {
        Path banksPath = Paths.get(getInstallerConfiguration().resolveBanksDirectory());
        Path targetPath;
        switch (assetDirectoryName) {
            case DIRECTORY_3D:
                targetPath = banksPath.resolve(DIRECTORY_VEHICLES);
                break;
            case DIRECTORY_SOUND:
                targetPath = banksPath.resolve(DIRECTORY_SOUNDS).resolve(DIRECTORY_VEHICLES);
                break;
            case DIRECTORY_GAUGES_HIGH:
                targetPath = banksPath.resolve(DIRECTORY_FRONT_END).resolve("HiRes").resolve(DIRECTORY_HUDS);
                break;
            case DIRECTORY_GAUGES_LOW:
                targetPath = banksPath.resolve(DIRECTORY_FRONT_END).resolve("LowRes").resolve(DIRECTORY_HUDS);
                break;
            case DIRECTORY_RIMS:
                targetPath = banksPath.resolve(DIRECTORY_VEHICLES).resolve("Rim");
                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetDirectoryName);
        }
        return targetPath;
    }

    private void copyAsset(Path assetPath, Path targetPath, String assetDirectoryName) throws IOException {
        VehicleSlotsHelper vehicleSlotsHelper = VehicleSlotsHelper.load(getDatabaseContext().getMiner());

        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference()
                .orElseThrow(() -> new InternalStepException(getType(), "No slot reference provided in properties."));

        VehicleSlot vehicleSlot = vehicleSlotsHelper.getVehicleSlotFromReference(slotReference)
                .orElseThrow(() -> new InternalStepException(getType(), "No vehicle slot found for reference: " + slotReference));

        Path effectiveTargetPath = targetPath;
        List<String> targetFileNames;
        switch (assetDirectoryName) {
            case DIRECTORY_3D:
                targetFileNames = singletonList(getTargetFileNameForExteriorAndInterior(vehicleSlot, assetPath.getFileName().toString()));
                break;
            case DIRECTORY_SOUND:
                targetFileNames = singletonList(VehicleSlotsHelper.getBankFileName(vehicleSlot, SOUND, true));
                break;
            case DIRECTORY_GAUGES_LOW:
            case DIRECTORY_GAUGES_HIGH:
                targetFileNames = singletonList(VehicleSlotsHelper.getBankFileName(vehicleSlot, HUD, true));
                break;
            case DIRECTORY_RIMS:
                effectiveTargetPath = getTargetPathForRims(assetPath, targetPath, vehicleSlot);
                targetFileNames = getTargetFileNamesForRims(vehicleSlot, assetPath);
                break;
            default:
                throw new IllegalArgumentException("Unhandled asset type: " + assetDirectoryName);
        }

        if (targetFileNames != null && !targetFileNames.isEmpty()) {
            copySingleAssetWithBackup(assetPath, effectiveTargetPath, targetFileNames);
        }
    }

    private static String getTargetFileNameForExteriorAndInterior(VehicleSlot vehicleSlot, String assetFileName) {
        String targetFileName;
        if (PATTERN_INTERIOR_MODEL_BANK_FILE.matcher(assetFileName).matches()) {
            targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, INT_3D, true);
        } else {
            targetFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, EXT_3D, true);
        }
        return targetFileName;
    }

    private Path getTargetPathForRims(Path rimAssetPath, Path targetPath, VehicleSlot vehicleSlot) {
        Matcher matcher = PATTERN_RIM_BANK_FILE.matcher(rimAssetPath.getFileName().toString());
        if (!matcher.matches()) {
            return null;
        }

        int rimRank = Integer.parseInt(matcher.group(2));
        final RimSlot rimSlot = vehicleSlot.getRimAtRank(rimRank)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle slot hasn't required rim at rank: " + rimRank));

        return targetPath.resolve(rimSlot.getParentDirectoryName().getValue());
    }

    private List<String> getTargetFileNamesForRims(VehicleSlot vehicleSlot, Path assetPath) throws IOException {
        Matcher matcher = FileConstants.PATTERN_RIM_BANK_FILE.matcher(assetPath.getFileName().toString());
        if (!matcher.matches()) {
            return null;
        }

        String typeGroupValue = matcher.group(1);
        int rimIndex = Integer.parseInt(matcher.group(2));

        MappedFileKind rimBankFileType = INDICATOR_FRONT_RIMS.equalsIgnoreCase(typeGroupValue) ? FRONT_RIMS_3D : REAR_RIMS_3D;
        String targetFileNameForFrontRim = VehicleSlotsHelper.getRimBankFileName(vehicleSlot, FRONT_RIMS_3D, rimIndex, true);
        String targetFileNameForRearRim = VehicleSlotsHelper.getRimBankFileName(vehicleSlot, REAR_RIMS_3D, rimIndex, true);

        if (targetFileNameForFrontRim.equals(targetFileNameForRearRim)) {
            if (REAR_RIMS_3D == rimBankFileType) {
                throw new IllegalArgumentException("Target slot does only accept single rim model for front/rear. Please remove rear rims file from assets.");
            }
            return singletonList(targetFileNameForFrontRim);
        } else {
            return FRONT_RIMS_3D == rimBankFileType ?
                     asList(targetFileNameForFrontRim, targetFileNameForRearRim) : singletonList(targetFileNameForRearRim);
        }
    }

    private void copySingleAssetWithBackup(Path assetPath, Path targetPath, List<String> targetFileNames) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetPath.toString());

        targetFileNames
                .forEach(targetFileName -> {
                    try {
                        final Path finalPath = targetPath.resolve(targetFileName);
                        final Path subTree = Paths.get(getInstallerConfiguration().resolveBanksDirectory()).relativize(finalPath);
                        final Path backupFinalPath = Paths.get(getInstallerConfiguration().resolveFilesBackupDirectory()).resolve(subTree);

                        if (Files.exists(finalPath)
                                && !Files.exists(backupFinalPath)) {
                            Log.info(THIS_CLASS_NAME, "*> BACKUP " + finalPath + " to " + backupFinalPath);
                            Files.createDirectories(backupFinalPath.getParent());

                            final File finalFile = finalPath.toFile();
                            //noinspection ResultOfMethodCallIgnored
                            finalFile.setWritable(true);
                            //noinspection ResultOfMethodCallIgnored
                            finalFile.setReadable(true);

                            Files.copy(finalPath, backupFinalPath);
                        }

                        Log.info(THIS_CLASS_NAME, "*> INSTALL " + assetPath + " to " + finalPath);
                        Files.copy(assetPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new IllegalArgumentException("Unable to copy asset with backup: " + assetPath.toString(), ioe);
                    }
                });
    }
}
