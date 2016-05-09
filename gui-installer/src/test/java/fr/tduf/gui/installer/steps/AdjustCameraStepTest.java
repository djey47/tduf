package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AdjustCameraStepTest {

    @Mock
    private GenuineCamGateway cameraSupportMock;

    private InstallerConfiguration installerConfiguration;
    private DatabaseContext databaseContext;

    @Before
    public void setUp() throws IOException {
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);

        Files.createDirectories(tduDatabasePath);
        FilesHelper.createFakeDatabase(tduDatabasePath.toString(), "");

        installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .overridingCameraSupport(cameraSupportMock)
                .build();

        PatchProperties patchProperties = new PatchProperties();
        databaseContext = new DatabaseContext(new ArrayList<>(), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
    }

    @Test
    public void perform_whenCameraIdInProperties_andNoCustomization_shouldNotCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN
        step.start();

        // THEN
        verifyZeroInteractions(cameraSupportMock);
    }

    @Test
    public void perform_whenCameraIdInProperties_andSingleCustomization_shouldCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|25");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN
        step.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(anyString(), eq(200), any(GenuineCamViewsDto.class));
        // TODO use captors to perform stronger assertions
    }

    // TODO test with invalid cam|view

    // TODO test with camera id from database
}
