package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.REVERT_CAMERA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

class RevertCameraStepTest {

    @Mock
    private GenuineCamGateway camGatewayMock;

    private final DatabasePatchProperties patchProperties = new DatabasePatchProperties();

    @BeforeEach
    void setUp() {
        initMocks(this);
        
        patchProperties.clear();
    }

    @Test
    void perform_whenNoCameraProperty_shouldNotCallCameraGateway() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("./")
                .overridingCameraSupport(camGatewayMock)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(REVERT_CAMERA);

        // WHEN
        genericStep.perform();

        // THEN
        verifyZeroInteractions(camGatewayMock);
    }

    @Test
    void perform_whenCustomCameraPropertyOnly_andNoVehicleSlotProperty_shouldThrowException() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("./")
                .overridingCameraSupport(camGatewayMock)
                .build();
        DatabaseContext databaseContext = InstallerTestsHelper.createDatabaseContext();
        patchProperties.register("CAMERA.BUMPER", "240|22");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(REVERT_CAMERA);

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
            genericStep::perform);
    }

    @Test
    void perform_whenCustomCameraPropertyOnly_shouldLoadVehicleFromDatabase_andCallCameraGateway() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("./")
                .overridingCameraSupport(camGatewayMock)
                .build();
        DatabaseContext databaseContext = InstallerTestsHelper.createDatabaseContext();
        patchProperties.setVehicleSlotReferenceIfNotExists("606298799");
        patchProperties.register("CAMERA.BUMPER", "240|22");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(REVERT_CAMERA);

        // WHEN
        genericStep.perform();

        // THEN
        verify(camGatewayMock).resetCamera("./Euro/Bnk/Database/cameras.bin", 102);
    }

    @Test
    void perform_whenCameraProperties_shouldCallCameraGateway() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("./")
                .overridingCameraSupport(camGatewayMock)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        patchProperties.register("CAMERA", "241");
        patchProperties.register("CAMERA.BUMPER", "240|22");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(REVERT_CAMERA);

        // WHEN
        genericStep.perform();

        // THEN
        verify(camGatewayMock).resetCamera("./Euro/Bnk/Database/cameras.bin", 241);
    }
}
