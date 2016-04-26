package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveBackupStepTest {

    @Test
    public void perform_shouldRemoveBackupDirectory() throws Exception {
        // GIVEN
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        Path backupPath = Paths.get(tduTempDirectory, "backup");
        Files.createDirectories(backupPath);

        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .build();
        installerConfiguration.setBackupDirectory(backupPath.toString());
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        GenericStep restoreFilesStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.REMOVE_BACKUP);


        // WHEN
        restoreFilesStep.perform();


        // THEN
        assertThat(backupPath).doesNotExist();
    }
}
