package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.Files.getFileExtension;
import static java.nio.file.Files.isRegularFile;
import static java.util.Objects.requireNonNull;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
public class UpdateDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = UpdateDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        final List<DbDto> topicObjects = getDatabaseContext().getTopicObjects();
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, topicObjects);

        Log.info(THIS_CLASS_NAME, "->Applying TDUF mini patch...");
        patcher.applyWithProperties(getDatabaseContext().getPatchObject(), getDatabaseContext().getPatchProperties());

        String slotRef = getDatabaseContext().getPatchProperties().getVehicleSlotReference().get();
        applyPerformancePackage(topicObjects, slotRef);
    }

    private void applyPerformancePackage(List<DbDto> topicObjects, String slotRef) throws ReflectiveOperationException, IOException {
        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);
        final Optional<Path> patchFilePath = Files.walk(assetPath, 1)

                .filter((path) -> isRegularFile(path))

                .filter((path) -> "tdupk".equalsIgnoreCase(getFileExtension(path.toString())))

                .findFirst();
        if (!patchFilePath.isPresent()) {

        }

        final TdupeGateway tdupeGateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, topicObjects);

        Log.info(THIS_CLASS_NAME, "->Applying TDUF performance package...");
        String performancePackFile = "";
        tdupeGateway.applyPerformancePackToEntryWithReference(Optional.of(slotRef), performancePackFile);

    }
}
