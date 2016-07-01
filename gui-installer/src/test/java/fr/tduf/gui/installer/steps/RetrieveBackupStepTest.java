package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


public class RetrieveBackupStepTest {
    @Test
    public void perform_whenBackupDirectoriesExist_shouldReturnLatest() throws Exception {
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
