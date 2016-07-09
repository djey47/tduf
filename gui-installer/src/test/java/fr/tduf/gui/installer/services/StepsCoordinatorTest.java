package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.services.tasks.InstallTask;
import fr.tduf.gui.installer.services.tasks.UninstallTask;
import javafx.concurrent.Task;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StepsCoordinatorTest {

    private StepsCoordinator coordinator = new StepsCoordinator();

    @Test
    public void createTask_whenInstall_shouldReturnInstallTask() {
        // GIVEN
        coordinator.uninstallProperty().set(false);

        // WHEN
        final Task<Void> actualTask = coordinator.createTask();

        // THEN
        assertThat(actualTask).isOfAnyClassIn(InstallTask.class);
    }

    @Test
    public void createTask_whenUninstall_shouldReturnUninstallTask() {
        // GIVEN
        coordinator.uninstallProperty().set(true);

        // WHEN
        final Task<Void> actualTask = coordinator.createTask();

        // THEN
        assertThat(actualTask).isOfAnyClassIn(UninstallTask.class);
    }
}
