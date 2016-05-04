package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Deletes current backup directory.
 */
class RemoveBackupStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RemoveBackupStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        final Path backupCurrentPath = Paths.get(getInstallerConfiguration().getBackupDirectory());

        Log.info(THIS_CLASS_NAME, "->Deleting backup directory: " + backupCurrentPath);

        FileUtils.deleteQuietly(backupCurrentPath.toFile());

        getDatabaseContext().setBackupDatabaseDirectory(null);
        getInstallerConfiguration().setBackupDirectory(null);
    }
}
