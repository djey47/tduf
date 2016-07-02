package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.gui.installer.steps.helper.PatchEnhancer;
import fr.tduf.gui.installer.steps.helper.SnapshotBuilder;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
class UpdateDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = UpdateDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        new PatchEnhancer(getDatabaseContext()).enhancePatchObject();

        new SnapshotBuilder(getDatabaseContext()).take(getInstallerConfiguration().getBackupDirectory());

        applyMiniPatch();

        applyPerformancePackage(getDatabaseContext().getPatchProperties().getVehicleSlotReference()
                .orElseThrow(() -> new InternalStepException(getType(), "Vehicle slot reference not found in properties")));
    }

    private void applyMiniPatch() throws ReflectiveOperationException, IOException {
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());

        Log.info(THIS_CLASS_NAME, "->Applying TDUF mini patch...");
        Path backupPath = Paths.get(getInstallerConfiguration().getBackupDirectory());
        writeEffectivePatch(backupPath, getDatabaseContext().getPatchObject());

        PatchProperties effectiveProperties = patcher.applyWithProperties(getDatabaseContext().getPatchObject(), getDatabaseContext().getPatchProperties());

        writeEffectiveProperties(backupPath, effectiveProperties);
    }

    private void applyPerformancePackage(String slotRef) throws ReflectiveOperationException, IOException {
        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);
        Optional<Path> potentialPPFilePath;
        try (Stream<Path> assetStream = Files.walk(assetPath, 1)) {
            potentialPPFilePath = assetStream

                    .filter(Files::isRegularFile)

                    .filter(path -> TdupeGateway.EXTENSION_PERFORMANCE_PACK.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))

                    .findFirst();
        }

        if (!potentialPPFilePath.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No TDUPE performance package to apply");
            return;
        }

        final TdupeGateway tdupeGateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, getDatabaseContext().getTopicObjects());

        String ppFilePath = potentialPPFilePath.get().toString();
        Log.info(THIS_CLASS_NAME, "->Applying TDUPE Performance pack: " + ppFilePath + "...");
        tdupeGateway.applyPerformancePackToEntryWithReference(Optional.of(slotRef), ppFilePath);
    }

    private void writeEffectivePatch(Path backupPath, DbPatchDto patchObject) throws IOException {
        String targetPatchFile = backupPath.resolve(InstallerConstants.FILE_NAME_EFFECTIVE_PATCH).toString();

        Log.info(THIS_CLASS_NAME, "->Writing effective patch to " + targetPatchFile + "...");

        FilesHelper.writeJsonObjectToFile(patchObject, targetPatchFile);
    }

    private void writeEffectiveProperties(Path backupPath, PatchProperties patchProperties) throws IOException {
        String targetPropertyFile = backupPath.resolve(InstallerConstants.FILE_NAME_EFFECTIVE_PROPERTIES).toString();

        Log.info(THIS_CLASS_NAME, "->Writing effective properties to " + targetPropertyFile + "...");

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);
    }
}
