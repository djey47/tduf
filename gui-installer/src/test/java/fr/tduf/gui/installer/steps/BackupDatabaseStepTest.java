package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class BackupDatabaseStepTest {

    @Test
    public void perform_shouldEnhanceDatabaseContext() throws Exception {
        // GIVEN
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);
        Files.createDirectories(tduDatabasePath);
        FilesHelper.createFakeDatabase(tduDatabasePath.toString(), "");
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.BACKUP_DATABASE);

        // WHEN
        step.start();

        // THEN
        assertThat(databaseContext.getBackupDatabaseDirectory()).isNotNull();
        // TODO assert backup files present
    }
}
