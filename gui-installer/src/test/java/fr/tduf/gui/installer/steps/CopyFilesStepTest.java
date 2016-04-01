package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.COPY_FILES;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class CopyFilesStepTest {

    private static final Class<CopyFilesStepTest> thisClass = CopyFilesStepTest.class;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = InstallerTestsHelper.createTempDirectory();

        FilesHelper.prepareTduDirectoryLayout(tempDirectory);
    }

    @Test
    public void copyFilesStep_withFakeFilesAllPresent_shouldCopyThemToCorrectLocation_withRightNames() throws Exception {
        // GIVEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        InstallerConfiguration configuration = createConfigurationForCar();
        DatabaseContext databaseContext = InstallerTestsHelper.createJsonDatabase();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists("606298799"); // AC427 (car)
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);


        // WHEN
        GenericStep.starterStep(configuration, databaseContext)
                .nextStep(COPY_FILES).start();


        // THEN
        Path banksPath = Paths.get(tempDirectory, "Euro", "Bnk");
        Path vehicleBanksPath = banksPath.resolve("Vehicules");
        Path soundBanksPath = banksPath.resolve("Sound").resolve("Vehicules");
        Path highHudBanksPath = banksPath.resolve("FrontEnd").resolve("HiRes").resolve("Gauges");
        Path lowHudBanksPath = banksPath.resolve("FrontEnd").resolve("LowRes").resolve("Gauges");
        Path rimBanksPath = vehicleBanksPath.resolve("Rim").resolve("AC");

        Path vehicleModelAssetsPath = Paths.get(configuration.getAssetsDirectory(), "3D");
        assertThat(vehicleBanksPath.resolve("AC_427.bnk").toFile())
                .exists()
                .hasSameContentAs(vehicleModelAssetsPath.resolve("AC_289.bnk").toFile());
        assertThat(vehicleBanksPath.resolve("AC_427_I.bnk").toFile())
                .exists()
                .hasSameContentAs(vehicleModelAssetsPath.resolve("AC_289_I.bnk").toFile());

        Path soundAssetsPath = Paths.get(configuration.getAssetsDirectory(), "SOUND");
        assertThat(soundBanksPath.resolve("AC_427_audio.bnk").toFile())
                .exists()
                .hasSameContentAs(soundAssetsPath.resolve("AC_289_audio.bnk").toFile());

        Path hudAssetsPath = Paths.get(configuration.getAssetsDirectory(), "GAUGES");
        assertThat(highHudBanksPath.resolve("AC_427.bnk").toFile())
                .exists()
                .hasSameContentAs(hudAssetsPath.resolve("HI").resolve("AC_289.bnk").toFile());
        assertThat(lowHudBanksPath.resolve("AC_427.bnk").toFile())
                .exists()
                .hasSameContentAs(hudAssetsPath.resolve("LOW").resolve("AC_289.bnk").toFile());

        Path rimAssetsPath = vehicleModelAssetsPath.resolve("RIMS");
        assertThat(rimBanksPath.resolve("AC_427_F_01.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("AC_289_F_01.bnk").toFile());
    }

    @Test
    public void copyFilesStep_withDifferentRimsFrontRear_shouldCopyThemToCorrectLocation_withRightNames() throws Exception {
        // GIVEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        InstallerConfiguration configuration = createConfigurationForBike();
        DatabaseContext databaseContext = InstallerTestsHelper.createJsonDatabase();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists("1208897332"); // Triumph Daytona (bike)
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);


        // WHEN
        GenericStep.starterStep(configuration, databaseContext)
                .nextStep(COPY_FILES).start();


        // THEN
        Path rimBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "Triumph");

        Path rimAssetsPath = Paths.get(configuration.getAssetsDirectory(), "3D", "RIMS");
        assertThat(rimBanksPath.resolve("DAYTONA_955I_F.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("BIKE_F_01.bnk").toFile());
        assertThat(rimBanksPath.resolve("DAYTONA_955I_R.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("BIKE_R_01.bnk").toFile());
    }

    @Test
    public void copyFilesStep_withDifferentRimsFrontRear_andSlotHasSameFileNameForFrontAndRear_shouldOnlyCopyFrontRim() throws Exception {
        // GIVEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        InstallerConfiguration configuration = createConfigurationForBike();
        DatabaseContext databaseContext = InstallerTestsHelper.createJsonDatabase();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists("606298799"); // AC427 (car)
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);


        // WHEN
        GenericStep.starterStep(configuration, databaseContext)
                .nextStep(COPY_FILES).start();


        // THEN
        Path rimBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "AC");

        Path rimAssetsPath = Paths.get(configuration.getAssetsDirectory(), "3D", "RIMS");
        assertThat(rimBanksPath.resolve("AC_427_F_01.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("BIKE_F_01.bnk").toFile());
    }

    @Test
    public void copyFilesStep_withSameRimsFrontRear_andSlotHasDifferentFileNameForFrontAndRear_shouldCopyFrontRimTwice() throws Exception {
        // GIVEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        InstallerConfiguration configuration = createConfigurationForCar();
        DatabaseContext databaseContext = InstallerTestsHelper.createJsonDatabase();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists("1208897332"); // Triumph Daytona (bike)
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);


        // WHEN
        GenericStep.starterStep(configuration, databaseContext)
                .nextStep(COPY_FILES).start();


        // THEN
        Path rimBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "Triumph");

        Path rimAssetsPath = Paths.get(configuration.getAssetsDirectory(), "3D", "RIMS");
        assertThat(rimBanksPath.resolve("DAYTONA_955I_F.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("AC_289_F_01.bnk").toFile());
        assertThat(rimBanksPath.resolve("DAYTONA_955I_R.bnk").toFile())
                .exists()
                .hasSameContentAs(rimAssetsPath.resolve("AC_289_F_01.bnk").toFile());
    }

    private InstallerConfiguration createConfigurationForCar() throws URISyntaxException {
        String assetsDirectory = new File(thisClass.getResource("/assets-all/car").toURI()).getAbsolutePath();
        return InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();
    }

    private InstallerConfiguration createConfigurationForBike() throws URISyntaxException {
        String assetsDirectory = new File(thisClass.getResource("/assets-all/bike").toURI()).getAbsolutePath();
        return InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();
    }
}
