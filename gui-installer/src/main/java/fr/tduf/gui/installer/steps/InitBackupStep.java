package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

/**
 * Creates backup directory and update all contexts with right paths.
 */
class InitBackupStep extends GenericStep {
    private static final String THIS_CLASS_NAME = InitBackupStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        LocalDateTime now = LocalDateTime.now();

        Path backupRootPath = Paths.get(getInstallerConfiguration().getInstallerDirectory()).resolve(InstallerConstants.DIRECTORY_BACKUP);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(InstallerConstants.PATTERN_BACKUP_DIRECTORY);
        Path backupCurrentPath = backupRootPath.resolve(formatter.format(now)).toAbsolutePath();

        Log.info(THIS_CLASS_NAME, "->Using backup directory: " + backupCurrentPath);

        Path filesBackupPath = backupCurrentPath.resolve(InstallerConstants.DIRECTORY_SUB_BACKUP_FILES).toAbsolutePath();
        Path databaseBackupPath = backupCurrentPath.resolve(InstallerConstants.DIRECTORY_SUB_BACKUP_DATABASE).toAbsolutePath();

        Files.createDirectories(filesBackupPath);
        Files.createDirectories(databaseBackupPath);

        getDatabaseContext().setBackupDatabaseDirectory(databaseBackupPath.toString());
        getInstallerConfiguration().setBackupDirectory(backupCurrentPath.toString());
    }
}
