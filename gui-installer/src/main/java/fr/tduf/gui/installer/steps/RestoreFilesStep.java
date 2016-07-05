package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.RESTORE_FILES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;

/**
 * Copy safe banks files to game directory back.
 */
class RestoreFilesStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreFilesStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");

        Path backupPath = Paths.get(getInstallerConfiguration().resolveFilesBackupDirectory());
        Path banksPath = Paths.get(getInstallerConfiguration().resolveBanksDirectory());

        Log.info(THIS_CLASS_NAME, "->Restoring files from " + backupPath + "...");

        Files.walk(backupPath)
                .filter(Files::isRegularFile)
                .forEach(bankFilePath -> {
                    final Path subTree = backupPath.relativize(bankFilePath);
                    final Path originalFilePath = banksPath.resolve(subTree);
                    try {
                        Files.copy(bankFilePath, originalFilePath, REPLACE_EXISTING);
                    } catch (IOException ioe) {
                        throw new InternalStepException(RESTORE_FILES, "Unable to copy backup file: " + bankFilePath + " to " + originalFilePath, ioe);
                    }
                });
    }
}
