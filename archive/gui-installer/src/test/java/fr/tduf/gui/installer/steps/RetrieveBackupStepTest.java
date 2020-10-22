package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RetrieveBackupStepTest {
    @Test
    void perform_whenBackupDirectoryDoestNotExist_shouldThrowException() throws Exception {
        // GIVEN
        final Path fakeInstallerPath = Paths.get(TestingFilesHelper.createTempDirectoryForInstaller());
        Files.createDirectory(fakeInstallerPath.resolve("backup"));
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .overridingInstallerDirectory(fakeInstallerPath.toString())
                .build();
        DatabaseContext context = new DatabaseContext(new ArrayList<>(0), "");
        final GenericStep genericStep = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RETRIEVE_BACKUP);

        // WHEN-THEN
        assertThrows(InternalStepException.class,
                genericStep::perform);
    }

    @Test
    void perform_whenBackupDirectoriesExist_shouldReturnLatest() throws Exception {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .build();
        DatabaseContext context = new DatabaseContext(new ArrayList<>(0), "");
        final GenericStep genericStep = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RETRIEVE_BACKUP);

        // WHEN
        genericStep.perform();

        // THEN
        assertThat(configuration.getBackupDirectory()).isEqualTo("./backup/99-12-31 23-59-00");
        assertThat(context.getPatchObject()).isNotNull();
        assertThat(context.getPatchProperties().getVehicleSlotReference()).contains("300000001");
    }
}
