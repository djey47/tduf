package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.REVERT_CAMERA;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RevertCameraStepTest {

    @Mock
    private GenuineCamGateway camGatewayMock;

    @Test
    public void perform_whenCameraProperties_shouldCallCameraGateway() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("./")
                .overridingCameraSupport(camGatewayMock)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("CAMERA", "241");
        patchProperties.register("CAMERA.BUMPER", "240|22");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(REVERT_CAMERA);

        // WHEN
        genericStep.perform();

        // THEN
        verify(camGatewayMock).resetCamera("./Euro/Bnk/Database/Cameras.bin", 241);
    }

    // TODO add tests: no custom cam property, no cam property
}
