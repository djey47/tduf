package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Retrieve latest backup directory and update all contexts with right paths.
 */
class RetrieveBackupStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RetrieveBackupStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Path backupRootPath = Paths.get(InstallerConstants.DIRECTORY_BACKUP);

        try (Stream<Path> stream = Files.walk(backupRootPath, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .sorted( (path1, path2) -> path2.toString().compareTo(path1.toString()) )
                    .findFirst()
                    .ifPresent(backupPath -> getInstallerConfiguration().setBackupDirectory(backupPath.toString()));
        }

        if (getInstallerConfiguration().getBackupDirectory() == null) {
            Log.info(THIS_CLASS_NAME, "->No backup found, will revert vehicle slot if possible");
        } else {
            Log.info(THIS_CLASS_NAME, "->Using backup directory: " + getInstallerConfiguration().getBackupDirectory());
        }
    }
}
