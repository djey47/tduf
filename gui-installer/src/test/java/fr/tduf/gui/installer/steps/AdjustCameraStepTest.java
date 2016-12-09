package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto.GenuineCamViewDto.Type.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdjustCameraStepTest {
    private static final String SLOTREF_1 = "999999";
    private static final String SLOTREF_2 = "999998";

    @Mock
    private GenuineCamGateway cameraSupportMock;

    @Mock
    private VehicleSlotsHelper vehicleSlotsHelperMock;

    @Captor
    private ArgumentCaptor<String> camFileCaptor;

    @Captor
    private ArgumentCaptor<GenuineCamViewsDto> customizeCamCaptor;

    private InstallerConfiguration installerConfiguration;

    private DatabaseContext databaseContext;

    private AdjustCameraStep adjustCameraStep;


    @Before
    public void setUp() throws IOException, StepException {
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);

        Files.createDirectories(tduDatabasePath);
        FilesHelper.createFakeDatabase(tduDatabasePath.toString(), "");

        installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .overridingCameraSupport(cameraSupportMock)
                .build();

        databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), new PatchProperties());

        adjustCameraStep = (AdjustCameraStep) GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        mockVehicleSlots();
    }

    @Test
    public void perform_whenCameraIdInProperties_andNoCustomization_shouldNotCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verifyZeroInteractions(cameraSupportMock);
    }

    @Test(expected = StepException.class)
    public void perform_whenCameraIdInProperties_andInvalidCustomization_shouldThrowException() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201^25");

        // WHEN-THEN
        try {
            adjustCameraStep.start();
        } catch (StepException se) {
            assertThat(se).hasCauseExactlyInstanceOf(IllegalArgumentException.class);
            throw se;
        }
    }

    @Test
    public void perform_whenCameraIdInProperties_andSingleCustomization_shouldCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200L), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201L);
        assertThat(actualViews).extracting("viewId").containsOnly(24);
    }

    @Test
    public void perform_whenCameraIdInProperties_andMultipleCustomization_shouldCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");
        databaseContext.getPatchProperties().register("CAMERA.HOODBACK", "202|HOODBACK");
        databaseContext.getPatchProperties().register("CAMERA.COCKPIT", "203|COCKPIT");
        databaseContext.getPatchProperties().register("CAMERA.COCKPITBACK", "204|COCKPITBACK");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(anyString(), eq(200L), customizeCamCaptor.capture());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsExactly(Hood, Hood_Back, Cockpit, Cockpit_Back);
        assertThat(actualViews).extracting("cameraId").containsExactly(201L, 202L, 203L, 204L);
        assertThat(actualViews).extracting("viewId").containsExactly(24, 44, 23, 43);
    }

    @Test(expected = StepException.class)
    public void perform_whenCameraIdNotInProperties_andSlotNotInDatabase_shouldThrowException() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", SLOTREF_2);

        // WHEN-THEN
        try {
            adjustCameraStep.start();
        } catch (StepException se) {
            assertThat(se).hasCauseExactlyInstanceOf(IllegalStateException.class);
            throw se;
        }
    }

    @Test(expected = StepException.class)
    public void perform_whenCameraIdAndSlotNotInProperties_shouldThrowException() throws StepException, IOException {
        // GIVEN-WHEN-THEN
        try {
            adjustCameraStep.start();
        } catch (StepException se) {
            assertThat(se).hasCauseExactlyInstanceOf(IllegalStateException.class);
            throw se;
        }
    }

    @Test
    public void perform_whenCameraIdNotInProperties_andSingleCustomization_shouldFetchFromDatabase_andCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", SLOTREF_1);
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");

        // WHEN
        adjustCameraStep.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200L), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201L);
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
}
