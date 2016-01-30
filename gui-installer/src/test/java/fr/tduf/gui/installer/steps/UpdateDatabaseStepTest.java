package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static java.util.Optional.empty;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseStepTest {

    private static final Class<UpdateDatabaseStepTest> thisClass = UpdateDatabaseStepTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private DatabaseContext databaseContext;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        databaseContext = TestHelper.createJsonDatabase();

        tempDirectory = TestHelper.createTempDirectory();
    }

    @Test
    public void applyPatches_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException {
        // GIVEN
        String assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext, null)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.applyPatches(empty());

        // THEN
    }

    @Test
    public void repackJsonDatabase_shouldCallBankSupportComponent() throws IOException, ReflectiveOperationException {
        // GIVEN
        TestHelper.createFakeDatabase(databaseContext.getJsonDatabaseDirectory(), "original-");
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();

        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) (
                GenericStep.starterStep(configuration, databaseContext, null)
                        .nextStep(UPDATE_DATABASE));
        updateDatabaseStep.repackJsonDatabase();

        // THEN
        Path databasePath = TestHelper.getTduDatabasePath(tempDirectory);

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
}
