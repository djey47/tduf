package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.game.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraSetInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class AdjustCameraStepTest {
    private static String tduTempDirectory;

    private static final String SLOTREF_1 = "999999";
    private static final String SLOTREF_2 = "999998";

    private static DatabaseContext databaseContext;

    @Mock
    private GenuineCamGateway cameraSupportMock;

    @Mock
    private VehicleSlotsHelper vehicleSlotsHelperMock;

    @Captor
    private ArgumentCaptor<String> camFileCaptor;

    @Captor
    private ArgumentCaptor<GenuineCamViewsDto> customizeCamCaptor;

    private AdjustCameraStep adjustCameraStep;

    @BeforeAll
    static void globalSetUp() throws IOException {
        tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);
        FilesHelper.createFakeDatabase(tduDatabasePath.toString());

        byte[] camBytes = fr.tduf.libunlimited.common.helper.FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        Files.write(tduDatabasePath.resolve("Cameras.bin"), camBytes);

        databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), new DatabasePatchProperties());
    }

    @BeforeEach
    void setUp() throws IOException, StepException {
        initMocks(this);
        
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .overridingCameraSupport(cameraSupportMock)
                .build();

        databaseContext.getPatchProperties().clear();

        adjustCameraStep = (AdjustCameraStep) GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        mockVehicleSlots();
        mockCameras();
    }

    @Test
    void perform_whenCameraIdInProperties_andNoCustomization_shouldNotCallBankSupportComponent() throws StepException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verifyNoInteractions(cameraSupportMock);
    }

    @Test
    void perform_whenCameraIdInProperties_andInvalidCustomization_shouldThrowException() {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201^25");

        // WHEN-THEN
        Throwable actual = assertThrows(StepException.class,
                () -> adjustCameraStep.start());
        assertThat(actual).hasCauseExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perform_whenCameraIdInProperties_andSingleCustomization_shouldCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201);
        assertThat(actualViews).extracting("viewId").containsOnly(24);
    }

    @Test
    void perform_whenCameraIdInProperties_andMultipleCustomization_shouldCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");
        databaseContext.getPatchProperties().register("CAMERA.HOODBACK", "202|HOODBACK");
        databaseContext.getPatchProperties().register("CAMERA.COCKPIT", "203|COCKPIT");
        databaseContext.getPatchProperties().register("CAMERA.COCKPITBACK", "204|COCKPITBACK");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(anyString(), eq(200), customizeCamCaptor.capture());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsExactly(Hood, Hood_Back, Cockpit, Cockpit_Back);
        assertThat(actualViews).extracting("cameraId").containsExactly(201, 202, 203, 204);
        assertThat(actualViews).extracting("viewId").containsExactly(24, 44, 23, 43);
    }

    @Test
    void perform_whenCameraIdNotInProperties_andSlotNotInDatabase_shouldThrowException() {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", SLOTREF_2);

        // WHEN-THEN
        Throwable actual = assertThrows(StepException.class,
                () -> adjustCameraStep.start());
        assertThat(actual).hasCauseExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void perform_whenCameraIdAndSlotNotInProperties_shouldThrowException() {
        // GIVEN-WHEN-THEN
        Throwable actual = assertThrows(StepException.class,
                () -> adjustCameraStep.start());
        assertThat(actual).hasCauseExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void perform_whenCameraIdNotInProperties_andSingleCustomization_shouldFetchFromDatabase_andCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", SLOTREF_1);
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201);
        assertThat(actualViews).extracting("viewId").containsOnly(24);
    }

    private void mockVehicleSlots() {
        adjustCameraStep.setVehicleSlotsHelper(vehicleSlotsHelperMock);

        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF_1)
                .withCameraIdentifier(200)
                .build();

        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(SLOTREF_1)).thenReturn(of(vehicleSlot));
        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(SLOTREF_2)).thenReturn(empty());
    }

    private void mockCameras() throws IOException {
        CameraSetInfo cameraSetInfo = CameraSetInfo.builder().forIdentifier(200).build();
        when(cameraSupportMock.getCameraInfo(anyString(), eq(200))).thenReturn(cameraSetInfo);
        CamerasHelper.setCameraSupport(cameraSupportMock);
    }
}
