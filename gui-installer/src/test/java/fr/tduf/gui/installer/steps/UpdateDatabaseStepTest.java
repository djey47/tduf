package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.*;
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
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseStepTest {
    private static final Class<UpdateDatabaseStepTest> thisClass = UpdateDatabaseStepTest.class;

    private static final String SLOT_REFERENCE = "30000000";

    @Mock
    private BankSupport bankSupportMock;

    private DatabaseContext databaseContext;

    private String assetsDirectory;

    private PatchProperties patchProperties;
    private InstallerConfiguration installerConfiguration;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Log.set(Log.LEVEL_DEBUG);

        patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOT_REFERENCE);

        databaseContext = InstallerTestsHelper.createJsonDatabase();
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);

        String tempDirectory = InstallerTestsHelper.createTempDirectory();
        assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();

        installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();
        installerConfiguration.setBackupDirectory(tempDirectory);
    }

    @Test
    public void perform_shouldCreate_effectivePropertiesFile_andEffectivePatchFile() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN-WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();

        // THEN
        assertThat(Paths.get(installerConfiguration.getBackupDirectory(), "installed.properties")).exists();
        assertThat(Paths.get(installerConfiguration.getBackupDirectory(), "installed.mini.json")).exists();
    }

    @Test
    public void perform_withoutPerformancePack_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
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


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
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
    public void perform_withPaintJobProperties_shouldAddCarColors_updateInstructions() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        String mainColorId = "1111";
        String secColorId = "2222";
        String calColorId = "3333";
        String nameId = "4444";
        String name = "PJ name";
        String intId = "5555";
        String intIdNone = "11319636";
        String intManufacturerId = "62938337";
        String intNameId = "53365512";
        String intMainColorId = "6666";
        String intSecondaryColorId = "7777";
        String intMaterialId = "8888";

        patchProperties.setExteriorColorNameResourceIfNotExists(nameId, 1);
        patchProperties.setExteriorColorNameIfNotExists(name, 1);
        patchProperties.setExteriorMainColorIdIfNotExists(mainColorId, 1);
        patchProperties.setExteriorSecondaryColorIdIfNotExists(secColorId, 1);
        patchProperties.setCalipersColorIdIfNotExists(calColorId, 1);
        patchProperties.setInteriorMainColorIdIfNotExists(intMainColorId, 1);
        patchProperties.setInteriorSecondaryColorIdIfNotExists(intSecondaryColorId, 1);
        patchProperties.setInteriorMaterialIdIfNotExists(intMaterialId, 1);

        databaseContext.getUserSelection().selectVehicleSlot(VehicleSlot.builder()
                .withRef(SLOT_REFERENCE)
                .addPaintJob(PaintJob.builder()
                        .withName(Resource.from(nameId, name))
                        .withColors(Resource.from(mainColorId, ""), Resource.from(secColorId, ""), Resource.from(calColorId, ""))
                        .addInteriorPattern(intId)
                        .build())
                .build());


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).hasSize(4);
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE, UPDATE_RES);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_PHYSICS_DATA, CAR_COLORS, INTERIOR);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(SLOT_REFERENCE, null, nameId, intId);
        assertThat(patchObject.getChanges()).extracting("values").containsOnly(
                null,
                asList(
                        SLOT_REFERENCE,
                        mainColorId,
                        nameId,
                        secColorId,
                        calColorId,
                        "0",
                        "0",
                        intId,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone,
                        intIdNone
                ),
                null,
                asList(
                        intId,
                        intManufacturerId,
                        intNameId,
                        intMainColorId,
                        intSecondaryColorId,
                        intMaterialId,
                        "0"
                ));
        assertThat(patchObject.getChanges()).extracting("value").containsOnly(
                null,
                null,
                name);
    }

    @Test
    public void perform_withoutDealerProperties_shouldAddCarPhysicsUpdateInstruction() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN-WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
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

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(installerConfiguration, databaseContext)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.perform();

        // THEN
    }
}
