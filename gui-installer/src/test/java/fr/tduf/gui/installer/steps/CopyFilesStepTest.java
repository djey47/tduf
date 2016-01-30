package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.COPY_FILES;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class CopyFilesStepTest {

    private static final Class<CopyFilesStepTest> thisClass = CopyFilesStepTest.class;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = TestHelper.createTempDirectory();

        TestHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    public void copyFilesStep_withFakeFilesAllPresent_shouldCopyThemToCorrectLocation_withRightNames() throws Exception {
        // GIVEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        String assetsDirectory = new File(thisClass.getResource("/assets-all").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();
        DatabaseContext databaseContext = TestHelper.createJsonDatabase();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReference("606298799");


        // WHEN
        GenericStep.starterStep(configuration, databaseContext, patchProperties)
                .nextStep(COPY_FILES).start();


        // THEN
        Path vehicleBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules");


        Path vehicleModelAssetsPath = Paths.get(assetsDirectory, "3D");
        assertThat(vehicleBanksPath.resolve("AC_427.bnk").toFile())
                .exists()
                .hasSameContentAs(vehicleModelAssetsPath.resolve("AC_289.bnk").toFile());

        assertThat(vehicleBanksPath.resolve("AC_427_I.bnk").toFile())
                .exists()
                .hasSameContentAs(vehicleModelAssetsPath.resolve("AC_289_I.bnk").toFile());

//        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "AC", "AC_289_F_01.bnk").toFile()).exists();
//
//        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "LowRes", "Gauges", "AC_289.bnk").toFile()).exists();
//        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "HiRes", "Gauges", "AC_289.bnk").toFile()).exists();
//
//        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Sound", "Vehicules", "AC_289_audio.bnk").toFile()).exists();
    }
}
