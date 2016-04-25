package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.google.common.io.Files.getFileExtension;
import static java.util.Objects.requireNonNull;

/**
 * Copy database banks files to safe location.
 */
public class BackupDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = BackupDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Path targetPath = Paths.get(getDatabaseContext().getBackupDatabaseDirectory());

        Log.info(THIS_CLASS_NAME, "->Backuping current database to " + targetPath + "...");

        Path databasePath = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory());
        Files.walk(databasePath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((filePath) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(getFileExtension(filePath.toString())))

                .filter((bankFilePath) -> bankFilePath.getFileName().toString().startsWith("DB"))

                .forEach((databaseBankFilePath) -> {
                    try {
                        Path targetFilePath = targetPath.resolve(databaseBankFilePath.getFileName());
                        Files.copy(databaseBankFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to copy " + databaseBankFilePath.getFileName() + ": " + ioe.getMessage(), ioe);
                    }
                });
    }
}
