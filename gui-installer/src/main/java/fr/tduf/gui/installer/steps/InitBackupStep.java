package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.InstallerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Objects.requireNonNull;

/**
 * Creates backup directory and update all contexts with right paths.
 */
public class InitBackupStep extends GenericStep {

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        LocalDateTime now = LocalDateTime.now();

        Path backupRootPath = Paths.get(InstallerConstants.DIRECTORY_BACKUP);

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(InstallerConstants.PATTERN_BACKUP_DIRECTORY);
        Path backupCurrentPath = backupRootPath.resolve(formatter.format(now)).toAbsolutePath();

        Path filesBackupPath = backupCurrentPath.resolve(InstallerConstants.DIRECTORY_SUB_BACKUP_FILES).toAbsolutePath();
        Path databaseBackupPath = backupCurrentPath.resolve(InstallerConstants.DIRECTORY_SUB_BACKUP_DATABASE).toAbsolutePath();

        Files.createDirectories(filesBackupPath);
        Files.createDirectories(databaseBackupPath);

        getDatabaseContext().setBackupDatabaseDirectory(databaseBackupPath.toString());
        getInstallerConfiguration().setBackupDirectory(backupCurrentPath.toString());
    }
}
