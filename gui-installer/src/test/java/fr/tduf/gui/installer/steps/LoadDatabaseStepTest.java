package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.LOAD_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LoadDatabaseStepTest {

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    private InstallerConfiguration configuration;
    private String databaseDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = TestHelper.createTempDirectory();

        databaseDirectory = Paths.get(tempDirectory, "Euro", "Bnk", "Database").toString();

        configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();

        TestHelper.createFakeDatabase(databaseDirectory, "");
    }

    @Test
    public void handleCacheDirectory_whenCachePresentAndBankFilesOlder_shouldNotUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = createCacheDirectory();
        Files.createFile(cachePath.resolve("last"));


        // WHEN
        LoadDatabaseStep loadDatabaseStep = (LoadDatabaseStep)
                GenericStep.starterStep(configuration, null, null)
                        .nextStep(LOAD_DATABASE);
        String jsonDirectory = loadDatabaseStep.handleCacheDirectory();


        // THEN
        verifyZeroInteractions(bankSupportMock);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
    }

    @Test
    public void handleCacheDirectory_whenCachePresentAndBankFilesNewer_shouldUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = createCacheDirectory();
        assert Files.createFile(cachePath.resolve("last")).toFile().setLastModified(0);


        // WHEN
        LoadDatabaseStep loadDatabaseStep = (LoadDatabaseStep)
                GenericStep.starterStep(configuration, null, null)
                        .nextStep(LOAD_DATABASE);
        String jsonDirectory = loadDatabaseStep.handleCacheDirectory();


        // THEN
        Path databasePath = TestHelper.getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
    }

    @Test
    public void handleCacheDirectory_whenNoCache_shouldUseBankSupportComponent_andReturnNewCacheDirectory() throws IOException {
        // GIVEN-WHEN
        LoadDatabaseStep loadDatabaseStep = (LoadDatabaseStep)
                GenericStep.starterStep(configuration, null, null)
                        .nextStep(LOAD_DATABASE);
        String jsonDirectory = loadDatabaseStep.handleCacheDirectory();


        // THEN
        Path databasePath = TestHelper.getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        final Path cachePath = Paths.get(databaseDirectory, "json-cache");
        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
        assertThat(cachePath.toFile()).exists();
    }

    @Test
    public void unpackDatabaseToJson_shouldCallBankSupportComponent() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        LoadDatabaseStep loadDatabaseStep = (LoadDatabaseStep)
                GenericStep.starterStep(configuration, null, null)
                        .nextStep(LOAD_DATABASE);
        List<String> actualJsonFiles = loadDatabaseStep.unpackDatabaseToJson(TestHelper.createTempDirectory());


        // THEN
        Path databasePath = TestHelper.getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        assertThat(actualJsonFiles).isEmpty();
    }

    private Path createCacheDirectory() throws IOException {
        final Path cachePath = Paths.get(databaseDirectory, "json-cache");
        return Files.createDirectories(cachePath);
    }

    private void verifyBankSupportComponentUsage(Path databasePath) throws IOException {
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
    }
}
