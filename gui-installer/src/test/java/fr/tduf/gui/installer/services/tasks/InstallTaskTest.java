package fr.tduf.gui.installer.services.tasks;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class InstallTaskTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = FilesHelper.createTempDirectoryForInstaller();
        Files.createDirectories(Paths.get(tempDirectory, "files"));
    }

    @Test(expected=StepException.class)
    public void handleStepExceptionInInstallTask_whenNonCriticalError_shouldRethrowException() throws StepException {
        // GIVEN
        ObjectProperty<InstallerConfiguration> configuration = createInstallerConfiguration();
        ObjectProperty<DatabaseContext> context = createDatabaseContext();
        InstallTask installTask = new InstallTask(configuration, context);
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
        ObjectProperty<InstallerConfiguration> configuration = createInstallerConfiguration();
        ObjectProperty<DatabaseContext> context = createDatabaseContext();
        InstallTask installTask = new InstallTask(configuration, context);
        StepException stepException = new StepException(GenericStep.StepType.UPDATE_DATABASE, "Critical error for database", null);

        // WHEN
        try {
            installTask.handleStepException(stepException);
        } catch (StepException se) {
            assertThat(se).isEqualTo(stepException);
            throw se;
        }
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