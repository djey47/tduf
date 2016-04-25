package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

/**
 * Copy safe database banks files to game directory back.
 */
public class RestoreDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Path databasePath = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory());
        Path databaseBackupPath = Paths.get(getDatabaseContext().getBackupDatabaseDirectory());

        Log.info(THIS_CLASS_NAME, "->Restoring current database from " + databaseBackupPath + "...");

        Files.walk(databaseBackupPath, 1)
                .filter(Files::isRegularFile)
                .forEach((bankFilePath) -> {
                    final Path originalFilePath = databasePath.resolve(bankFilePath.getFileName());
                    try {
                        Files.copy(bankFilePath, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to restore backup: " + bankFilePath, ioe);
                    }
                });
    }
}
