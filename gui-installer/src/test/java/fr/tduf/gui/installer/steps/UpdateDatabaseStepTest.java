package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseStepTest {
    private static final Class<UpdateDatabaseStepTest> thisClass = UpdateDatabaseStepTest.class;

    private static final String SLOT_REFERENCE = "30000000";

    @Mock
    private BankSupport bankSupportMock;

    private DatabaseContext databaseContext;

    private String tempDirectory;
    private String assetsDirectory;

    private PatchProperties patchProperties;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Log.set(Log.LEVEL_DEBUG);

        patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOT_REFERENCE);

        databaseContext = InstallerTestsHelper.createJsonDatabase();
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);

        tempDirectory = InstallerTestsHelper.createTempDirectory();
        assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
    }

    @Test
    public void perform_withoutPerformancePack_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();

        // THEN
    }

    @Test
    public void perform_withDealerProperties_shouldAddCarShops_andCarPhysics_updateInstructions() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        String dealerRef = "0000";
        patchProperties.setDealerReferenceIfNotExists(dealerRef);
        patchProperties.setDealerSlotIfNotExists(10);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE, UPDATE);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_SHOPS, CAR_PHYSICS_DATA);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(dealerRef, SLOT_REFERENCE);
        assertThat(patchObject.getChanges()).extracting("partialValues").containsOnly(
                singletonList(DbFieldValueDto.fromCouple(13, SLOT_REFERENCE)),
                singletonList(DbFieldValueDto.fromCouple(100, "100")));
    }

    @Test
    public void perform_withoutDealerProperties_shouldAddCarPhysicsUpdateInstruction() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_PHYSICS_DATA);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(SLOT_REFERENCE);
        assertThat(patchObject.getChanges()).extracting("partialValues").containsOnly(singletonList(DbFieldValueDto.fromCouple(100, "100")));
    }

    @Test
    public void perform_withPerformancePack_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        assetsDirectory = new File(thisClass.getResource("/assets-patch-tdupk-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();

        // THEN
    }
}
