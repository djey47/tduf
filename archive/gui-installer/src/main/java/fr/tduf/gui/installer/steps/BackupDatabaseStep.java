package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Copy database banks files to safe location.
 */
class BackupDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = BackupDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Path targetPath = Paths.get(getDatabaseContext().getBackupDatabaseDirectory());

        Log.info(THIS_CLASS_NAME, "->Backuping current database to " + targetPath + "...");

        Path databasePath = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory());
        try (Stream<Path> databaseStream = Files.walk(databasePath, 1)) {

            databaseStream.filter(Files::isRegularFile)

                    .filter(filePath -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(FilesHelper.getExtension(filePath.toString())))

                    .filter(bankFilePath -> bankFilePath.getFileName().toString().startsWith("DB"))

                    .forEach(databaseBankFilePath -> {
                        try {
                            Path targetFilePath = targetPath.resolve(databaseBankFilePath.getFileName());
                            Files.copy(databaseBankFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ioe) {
                            throw new RuntimeException("Unable to copy " + databaseBankFilePath.getFileName() + ": " + ioe.getMessage(), ioe);
                        }
                    });
        }
    }
}
