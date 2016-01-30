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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.LOAD_DATABASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LoadDatabaseStepTest {

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = TestHelper.createTempDirectory();
    }

    @Test
    public void unpackDatabaseToJson_shouldCallBankSupportComponent() throws IOException, URISyntaxException {
        // GIVEN
        String databaseDirectory = Paths.get(tempDirectory, "Euro", "Bnk", "Database").toString();
        TestHelper.createFakeDatabase(databaseDirectory, "");

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();


        // WHEN
        LoadDatabaseStep loadDatabaseStep = (LoadDatabaseStep)
                GenericStep.starterStep(configuration, null, null)
                        .nextStep(LOAD_DATABASE);
        List<String> actualJsonFiles = loadDatabaseStep.unpackDatabaseToJson(TestHelper.createTempDirectory());


        // THEN
        Path databasePath = TestHelper.getTduDatabasePath(tempDirectory);

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
}
