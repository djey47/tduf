package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class RestoreFilesStepTest {

    @Test
    void perform_shouldRestoreBackup() throws Exception {
        // GIVEN
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        Path bankFilesPath = Paths.get(tduTempDirectory).resolve("Euro").resolve("Bnk").resolve("Vehicules");
        Files.createDirectories(bankFilesPath);

        Path backupFilesPath = Paths.get(tduTempDirectory, "backup", "files");
        Path backupFile = backupFilesPath.resolve("Vehicules").resolve("AC_427.BNK");
        Files.createDirectories(backupFile.getParent());
        Files.createFile(backupFile);

        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .build();
        installerConfiguration.setBackupDirectory(backupFilesPath.getParent().toString());
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        GenericStep restoreFilesStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.RESTORE_FILES);


        // WHEN
        restoreFilesStep.perform();


        // THEN
        assertThat(bankFilesPath.resolve("AC_427.BNK")).exists();
    }
}
