package fr.tduf.gui.installer.services;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class StepsCoordinatorTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test(expected=StepException.class)
    public void handleStepExceptionInInstallTask_whenNonCriticalError_shouldRethrowException() throws StepException {
        // GIVEN
        ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>(InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .build());
        ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>(new DatabaseContext(new ArrayList<>(), ""));
        StepsCoordinator.InstallTask installTask = new StepsCoordinator.InstallTask(configuration, context);
        StepException stepException = new StepException(GenericStep.StepType.UPDATE_MAGIC_MAP, "Non critical error for database", null);

        // WHEN
        try {
            installTask.handleStepException(stepException);
        } catch (StepException se) {
            assertThat(se).isEqualTo(stepException);
            throw se;
        }
    }

    @Test(expected=StepException.class)
    public void handleStepExceptionInInstallTask_whenDatabaseCriticalError_shouldRethrowException() throws StepException {
        // GIVEN
        ObjectProperty<InstallerConfiguration> configuration = new SimpleObjectProperty<>(InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .build());
        ObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>(new DatabaseContext(new ArrayList<>(), ""));
        StepsCoordinator.InstallTask installTask = new StepsCoordinator.InstallTask(configuration, context);
        StepException stepException = new StepException(GenericStep.StepType.UPDATE_DATABASE, "Critical error for database", null);

        // WHEN
        try {
            installTask.handleStepException(stepException);
        } catch (StepException se) {
            assertThat(se).isEqualTo(stepException);
            throw se;
        }
    }
}
