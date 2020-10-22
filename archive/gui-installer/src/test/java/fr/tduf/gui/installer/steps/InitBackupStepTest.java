package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class InitBackupStepTest {

    private Path currentBackupPath;

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteQuietly(currentBackupPath.toFile());
    }

    @Test
    void perform_shouldCreateBackupDirectories_andSetContexts() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        final GenericStep initBackupStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.INIT_BACKUP);

        // WHEN
        initBackupStep.perform();

        // THEN
        currentBackupPath = Paths.get(installerConfiguration.getBackupDirectory());
        assertThat(currentBackupPath).exists();
        assertThat(Paths.get(databaseContext.getBackupDatabaseDirectory())).exists();
    }
}
