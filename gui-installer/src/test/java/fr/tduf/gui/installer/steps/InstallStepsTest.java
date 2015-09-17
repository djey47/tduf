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
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class InstallStepsTest {

    private static final Class<InstallStepsTest> thisClass = InstallStepsTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = createTempDirectory();

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
    public void updateMagicMapStep_whenMapFilesExists_andNewFiles_shouldUpdateMap() throws IOException {
        // GIVEN
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .build();

        // WHEN
        InstallSteps.updateMagicMapStep(configuration);

        // THEN
        File actualMagicMapFile = Paths.get(tempDirectory, "Euro", "Bnk", "Bnk1.map").toFile();
        File expectedMagicMapFile = new File(thisClass.getResource("/banks/Bnk1-enhanced.map").getFile());
        assertThat(actualMagicMapFile).hasContentEqualTo(expectedMagicMapFile);
    }

    @Test
    public void unpackDatabaseToJson_shouldCallBankSupportComponent() throws IOException, URISyntaxException {
        // GIVEN
        createFakeDatabase();

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();


        // WHEN
        List<String> actualJsonFiles = InstallSteps.unpackDatabaseToJson(configuration, createTempDirectory());


        // THEN
        Path databasePath = getTduDatabasePath();

        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_CH.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_FR.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_KO.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_GE.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_IT.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_SP.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_US.bnk").toString()), anyString());
        verify(bankSupportMock).extractAll(eq(databasePath.resolve("DB_JA.bnk").toString()), anyString());
        verifyNoMoreInteractions(bankSupportMock);

        assertThat(actualJsonFiles).isEmpty();
    }

    @Test
    public void applyPatches_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException {
        // GIVEN
        String jsonDatabaseDirectory = createJsonDatabase();

        String assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();


        // WHEN
        InstallSteps.applyPatches(configuration, jsonDatabaseDirectory);


        // THEN
    }

    @Test
    public void repackJsonDatabase_shouldCallBankSupportComponent() throws IOException {
        // GIVEN
        createFakeDatabase();

        String jsonDatabaseDirectory = createJsonDatabase();

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();


        // WHEN
        InstallSteps.repackJsonDatabase(configuration, jsonDatabaseDirectory);


        // THEN
        Path databasePath = getTduDatabasePath();

        verify(bankSupportMock).preparePackAll(anyString(), eq("DB.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_CH.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_FR.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_GE.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_IT.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_JA.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_KO.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_SP.bnk"));
        verify(bankSupportMock).preparePackAll(anyString(), eq("DB_US.bnk"));

        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_CH.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_FR.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_GE.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_KO.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_IT.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_JA.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_SP.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(databasePath.resolve("DB_US.bnk").toString()));
        verifyNoMoreInteractions(bankSupportMock);
    }

    private void prepareTduDirectoryLayout() throws IOException {
        Path banksPath = Paths.get(tempDirectory, "Euro", "Bnk");

        Path vehicleBanksPath = banksPath.resolve("Vehicules");
        FilesHelper.createDirectoryIfNotExists(vehicleBanksPath.toString());

        Path rimBanksPath = vehicleBanksPath.resolve("Rim");
        FilesHelper.createDirectoryIfNotExists(rimBanksPath.toString());

        Path frontEndPath = banksPath.resolve("FrontEnd");
        Path lowGaugesBanksPath = frontEndPath.resolve("LowRes").resolve("Gauges");
        FilesHelper.createDirectoryIfNotExists(lowGaugesBanksPath.toString());
        Path highGaugesBanksPath = frontEndPath.resolve("HiRes").resolve("Gauges");
        FilesHelper.createDirectoryIfNotExists(highGaugesBanksPath.toString());

        Path soundBanksPath = banksPath.resolve("Sound").resolve("Vehicules");
        FilesHelper.createDirectoryIfNotExists(soundBanksPath.toString());

        Path magicMapPath = Paths.get(thisClass.getResource("/banks/Bnk1.map").getFile());
        Files.copy(magicMapPath, banksPath.resolve(magicMapPath.getFileName()));

        Files.createFile(banksPath.resolve("test1.bnk"));
        Files.createFile(banksPath.resolve("test2.bnk"));
        Files.createFile(banksPath.resolve("test3.bnk"));
    }

    private Path getTduDatabasePath() {
        return Paths.get(tempDirectory).resolve("Euro").resolve("Bnk").resolve("Database");
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

    private String createJsonDatabase() throws IOException {
        String jsonDatabaseDirectory = createTempDirectory();

        Path jsonDatabasePath = Paths.get(thisClass.getResource("/db-json").getFile());
        Files.walk(jsonDatabasePath)

                .filter((path) -> !Files.isDirectory(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        Files.copy(path, Paths.get(jsonDatabaseDirectory).resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


        return jsonDatabaseDirectory;
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("guiInstaller-tests").toString();
    }
}