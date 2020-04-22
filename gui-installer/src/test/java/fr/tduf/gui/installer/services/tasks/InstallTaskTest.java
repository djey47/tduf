package fr.tduf.gui.installer.services.tasks;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstallTaskTest extends ApplicationTest {
    private String tempDirectory;

    @Override
    public void start(Stage stage) {}

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = TestingFilesHelper.createTempDirectoryForInstaller();
        Files.createDirectories(Paths.get(tempDirectory, "files"));
    }

    @Test
    void handleStepExceptionInInstallTask_whenNonCriticalError_shouldRethrowException() {
        // GIVEN
        ObjectProperty<InstallerConfiguration> configuration = createInstallerConfiguration();
        ObjectProperty<DatabaseContext> context = createDatabaseContext();
        InstallTask installTask = new InstallTask(configuration, context);
        StepException stepException = new StepException(GenericStep.StepType.UPDATE_MAGIC_MAP, "Non critical error for database", null);

        // WHEN-THEN
        StepException actualException = assertThrows(StepException.class,
                () -> installTask.handleStepException(stepException));
        assertThat(actualException).isEqualTo(stepException);
    }

    @Test
    void handleStepExceptionInInstallTask_whenDatabaseCriticalError_shouldRethrowException() {
        // GIVEN
        ObjectProperty<InstallerConfiguration> configuration = createInstallerConfiguration();
        ObjectProperty<DatabaseContext> context = createDatabaseContext();
        InstallTask installTask = new InstallTask(configuration, context);
        StepException stepException = new StepException(GenericStep.StepType.UPDATE_DATABASE, "Critical error for database", null);

        // WHEN-THEN
        StepException actualException = assertThrows(StepException.class,
                () -> installTask.handleStepException(stepException));
        assertThat(actualException).isEqualTo(stepException);
    }

    private ObjectProperty<DatabaseContext> createDatabaseContext() {
        final SimpleObjectProperty<DatabaseContext> context = new SimpleObjectProperty<>(new DatabaseContext(new ArrayList<>(), ""));
        context.getValue().setBackupDatabaseDirectory(tempDirectory);
        return context;
    }

    private ObjectProperty<InstallerConfiguration> createInstallerConfiguration() {
        final SimpleObjectProperty<InstallerConfiguration> installerConfiguration = new SimpleObjectProperty<>(InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build());
        installerConfiguration.getValue().setBackupDirectory(tempDirectory);
        return installerConfiguration;
    }
}
