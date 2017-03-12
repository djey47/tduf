package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BackupDatabaseStepTest {

    @Test
    void perform_shouldCopyDatabaseFilesToBackupLocation() throws Exception {
        // GIVEN
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);
        Files.createDirectories(tduDatabasePath);

        final Path backupPath = Paths.get(tduTempDirectory, "backup");
        Files.createDirectories(backupPath);
        String actualBackupDir = backupPath.toString();

        FilesHelper.createFakeDatabase(tduDatabasePath.toString());
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        databaseContext.setBackupDatabaseDirectory(actualBackupDir);
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.BACKUP_DATABASE);


        // WHEN
        step.start();


        // THEN
        Stream.of(FilesHelper.DATABASE_BANK_FILES)
                .forEach((fileName) -> assertThat(Paths.get(actualBackupDir, fileName)).exists());
    }
}
