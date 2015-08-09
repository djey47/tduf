package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class InstallStepsTest {

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("guiInstaller-tests").toString();
    }

    @Test
    public void copyFilesStep_withFakeFilesAllPresent_shouldCopyThemToCorrectLocation() throws Exception {
        // GIVEN
        prepareTduDirectoryLayout();

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build();

        String assetsDirectory = new File(this.getClass().getResource("/assets-all").toURI()).getAbsolutePath();


        // WHEN
        InstallSteps.copyFilesStep(assetsDirectory, configuration);


        // THEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289_I.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Sound", "Vehicules", "AC_289_audio.bnk").toFile()).exists();
    }

    private void prepareTduDirectoryLayout() throws IOException {
        Path vehicleBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules");
        FilesHelper.createDirectoryIfNotExists(vehicleBanksPath.toString());

        Path rimBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim");
        FilesHelper.createDirectoryIfNotExists(rimBanksPath.toString());

        Path lowGaugesBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "LowRes", "Gauges");
        FilesHelper.createDirectoryIfNotExists(lowGaugesBanksPath.toString());

        Path highGaugesBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "HiRes", "Gauges");
        FilesHelper.createDirectoryIfNotExists(highGaugesBanksPath.toString());

        Path soundBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Sound", "Vehicules");
        FilesHelper.createDirectoryIfNotExists(soundBanksPath.toString());
    }
}