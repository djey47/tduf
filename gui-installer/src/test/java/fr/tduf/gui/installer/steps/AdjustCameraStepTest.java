package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto.GenuineCamViewDto.Type.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.UID;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AdjustCameraStepTest {

    @Mock
    private GenuineCamGateway cameraSupportMock;

    @Captor
    private ArgumentCaptor<String> camFileCaptor;

    @Captor
    private ArgumentCaptor<GenuineCamViewsDto> customizeCamCaptor;

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

        DbDto carPhysicsObject = createCarPhysicsObject("999999");
        DbDto carColorsObject = createDefaultTopicObject(CAR_COLORS);
        PatchProperties patchProperties = new PatchProperties();
        databaseContext = new DatabaseContext(asList(carPhysicsObject, carColorsObject), "");
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

    @Test(expected = StepException.class)
    public void perform_whenCameraIdInProperties_andInvalidCustomization_shouldThrowException() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("CAMERA", "200");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201^25");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN-THEN
        try {
            step.start();
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
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN
        step.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201);
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
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN
        step.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(anyString(), eq(200), customizeCamCaptor.capture());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsExactly(Hood, Hood_Back, Cockpit, Cockpit_Back);
        assertThat(actualViews).extracting("cameraId").containsExactly(201, 202, 203, 204);
        assertThat(actualViews).extracting("viewId").containsExactly(24, 44, 23, 43);
    }

    @Test(expected = StepException.class)
    public void perform_whenCameraIdNotInProperties_andSlotNotInDatabase_shouldThrowException() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", "999998");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN-THEN
        try {
            step.start();
        } catch (StepException se) {
            assertThat(se).hasCauseExactlyInstanceOf(IllegalStateException.class);
            throw se;
        }
    }

    @Test(expected = StepException.class)
    public void perform_whenCameraIdAndSlotNotInProperties_shouldThrowException() throws StepException, IOException {
        // GIVEN
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN-THEN
        try {
            step.start();
        } catch (StepException se) {
            assertThat(se).hasCauseExactlyInstanceOf(IllegalStateException.class);
            throw se;
        }
    }

    @Test
    public void perform_whenCameraIdNotInProperties_andSingleCustomization_shouldFetchFromDatabase_andCallBankSupportComponent() throws StepException, IOException {
        // GIVEN
        databaseContext.getPatchProperties().register("SLOTREF", "999999");
        databaseContext.getPatchProperties().register("CAMERA.HOOD", "201|HOOD");
        final GenericStep step = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.ADJUST_CAMERA);

        // WHEN
        step.start();

        // THEN
        verify(cameraSupportMock).customizeCamera(camFileCaptor.capture(), eq(200), customizeCamCaptor.capture());
        assertThat(Paths.get(camFileCaptor.getValue()).toString()).endsWith(Paths.get("Euro", "Bnk", "Database", "Cameras.bin").toString());
        List<GenuineCamViewsDto.GenuineCamViewDto> actualViews = customizeCamCaptor.getValue().getViews();
        assertThat(actualViews).extracting("viewType").containsOnly(Hood);
        assertThat(actualViews).extracting("cameraId").containsOnly(201);
        assertThat(actualViews).extracting("viewId").containsOnly(24);
    }

    private static DbDto createCarPhysicsObject(String slotReference) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(CAR_PHYSICS_DATA)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(UID)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(DbDataDto.Entry.builder()
                                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotReference).build())
                                .addItem(DbDataDto.Item.builder().ofFieldRank(98).withRawValue("200").build())
                                .build())
                        .build())
                .build();
    }

    private static DbDto createDefaultTopicObject(DbDto.Topic topic) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(topic)
                        .build())
                .withData(DbDataDto.builder().build())
                .build();
    }
}
