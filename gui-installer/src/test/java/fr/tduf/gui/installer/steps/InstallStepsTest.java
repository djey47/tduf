package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InstallStepsTest {

    private static final Class<InstallStepsTest> thisClass = InstallStepsTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("guiInstaller-tests").toString();

        prepareTduDirectoryLayout();
    }

    @Test
    public void copyFilesStep_withFakeFilesAllPresent_shouldCopyThemToCorrectLocation() throws Exception {
        // GIVEN
        String assetsDirectory = new File(thisClass.getResource("/assets-all").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();


        // WHEN
        InstallSteps.copyFilesStep(configuration);


        // THEN
        System.out.println("Testing TDU directory: " + tempDirectory);

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "AC_289.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Vehicules", "Rim", "AC", "AC_289_F_01.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "LowRes", "Gauges", "AC_289.bnk").toFile()).exists();
        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "FrontEnd", "HiRes", "Gauges", "AC_289.bnk").toFile()).exists();

        assertThat(Paths.get(tempDirectory, "Euro", "Bnk", "Sound", "Vehicules", "AC_289_audio.bnk").toFile()).exists();
    }

    @Test
    public void unpackDatabaseToJson() throws IOException, URISyntaxException {
        // GIVEN
        createFakeDatabase();

        String assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .withAssetsDirectory(assetsDirectory)
                .build();


        // WHEN
//        InstallSteps.unpackDatabaseToJson(configuration);


        // THEN
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

    private void createFakeDatabase() throws IOException {
        Path databaseBanksPath = Paths.get(tempDirectory, "Euro", "Bnk", "Database");
        FilesHelper.createDirectoryIfNotExists(databaseBanksPath.toString());

        Files.createFile(databaseBanksPath.resolve("DB.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_CH.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_FR.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_GE.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_KO.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_IT.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_JA.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_SP.bnk"));
        Files.createFile(databaseBanksPath.resolve("DB_US.bnk"));
    }
}