package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.util.Objects.requireNonNull;

/**
 * Copy safe banks files to game directory back.
 */
public class RestoreFilesStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreFilesStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");

        Path backupPath = Paths.get(getInstallerConfiguration().resolveFilesBackupDirectory());
        Path banksPath = Paths.get(getInstallerConfiguration().resolveBanksDirectory());

        Log.info(THIS_CLASS_NAME, "->Restoring files from " + backupPath + "...");

        Files.walk(backupPath)
                .filter(Files::isRegularFile)
                .forEach((bankFilePath) -> {
                    final Path subTree = backupPath.relativize(bankFilePath);
                    final Path originalFilePath = banksPath.resolve(subTree);
                    try {
                        Files.copy(bankFilePath, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new RuntimeException("Unable to restore backup: " + bankFilePath, ioe);
                    }
                });
    }
}
