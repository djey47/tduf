package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseStepTest {
    private static final Class<UpdateDatabaseStepTest> thisClass = UpdateDatabaseStepTest.class;

    private static final String SLOT_REFERENCE = "30000000";
    private static final String BRAND_REFERENCE = "81940960";
    private static final String CARID = "3000";
    private static final String BANKNAME = "TDUCP_3000";
    private static final String RES_BANKNAME = "30000567";
    private static final String BANKNAME_FR_1 = "TDUCP_3000_F_01";
    private static final String BANKNAME_RR_1 = "TDUCP_3000_R_01";
    private static final String RES_BANKNAME_FR_1 = "3000000010";
    private static final String RES_BANKNAME_RR_1 = "3000000011";
    private static final String RIMREF_1 = "3000000001";
    private static final String RES_RIMBRAND_1 = "654857";
    private static final String RIMBRAND_1 = "Default";

    @Mock
    private BankSupport bankSupportMock;

    private DatabaseContext databaseContext;

    private String assetsDirectory;

    private InstallerConfiguration installerConfiguration;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Log.set(Log.LEVEL_DEBUG);

        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOT_REFERENCE);
        patchProperties.setBrandReferenceIfNotExists(BRAND_REFERENCE);
        patchProperties.setInteriorMainColorIdIfNotExists("", 1);
        patchProperties.setInteriorSecondaryColorIdIfNotExists("", 1);
        patchProperties.setInteriorMaterialIdIfNotExists("", 1);
        patchProperties.register("RIMWIDTH.FR.1", "0");
        patchProperties.register("RIMWIDTH.RR.1", "0");
        patchProperties.register("RIMHEIGHT.FR.1", "0");
        patchProperties.register("RIMHEIGHT.RR.1", "0");
        patchProperties.register("RIMDIAM.FR.1", "0");
        patchProperties.register("RIMDIAM.RR.1", "0");
        patchProperties.register("RIMNAME.1", "");

        databaseContext = InstallerTestsHelper.createDatabaseContext();
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        databaseContext.getUserSelection().selectVehicleSlot(createVehicleSlot());

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
        assertThat(Paths.get(installerConfiguration.getBackupDirectory(), "installed.mini.json.properties")).exists();
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

    private static VehicleSlot createVehicleSlot() {
        return VehicleSlot.builder()
                .withRef(SLOT_REFERENCE)
                .withBrand(createBrandSlot())
                .withCarIdentifier(Integer.valueOf(CARID))
                .withFileName(Resource.from(RES_BANKNAME, BANKNAME))
                .addRim(RimSlot.builder()
                        .withRef(RIMREF_1)
                        .atRank(1)
                        .withParentDirectoryName(Resource.from(RES_RIMBRAND_1, RIMBRAND_1))
                        .withRimsInformation(RimSlot.RimInfo.builder()
                                        .withFileName(Resource.from(RES_BANKNAME_FR_1, BANKNAME_FR_1))
                                        .build(),
                                RimSlot.RimInfo.builder()
                                        .withFileName(Resource.from(RES_BANKNAME_RR_1, BANKNAME_RR_1))
                                        .build())
                        .setDefaultRims(true)
                        .build())
                .build();
    }

    private static Brand createBrandSlot() {
        return Brand.builder().withReference(BRAND_REFERENCE).build();
    }
}
