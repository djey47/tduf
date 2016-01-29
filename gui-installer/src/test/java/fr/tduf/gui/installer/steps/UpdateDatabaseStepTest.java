package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.TestHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
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

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
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

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = TestHelper.createTempDirectory();
    }

    @Test
    public void applyPatches_shouldNotCrash() throws URISyntaxException, IOException, ReflectiveOperationException {
        // GIVEN
        DatabaseContext databaseContext = createJsonDatabase();

        String assetsDirectory = new File(thisClass.getResource("/assets-patch-only").toURI()).getAbsolutePath();
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .withAssetsDirectory(assetsDirectory)
                .build();

        GenericStep previousStep = GenericStep.starterStep(configuration, databaseContext);


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) GenericStep.loadStep(UPDATE_DATABASE, previousStep);
        updateDatabaseStep.applyPatches(empty());


        // THEN
    }

    @Test
    public void repackJsonDatabase_shouldCallBankSupportComponent() throws IOException, ReflectiveOperationException {
        // GIVEN
        DatabaseContext databaseContext = createJsonDatabase();
        TestHelper.createFakeDatabase(databaseContext.getJsonDatabaseDirectory(), "original-");

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tempDirectory)
                .usingBankSupport(bankSupportMock)
                .build();

        GenericStep previousStep = GenericStep.starterStep(configuration, databaseContext);


        // WHEN
        final UpdateDatabaseStep updateDatabaseStep = (UpdateDatabaseStep) GenericStep.loadStep(UPDATE_DATABASE, previousStep);
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

    private static DatabaseContext createJsonDatabase() throws IOException {
        String jsonDatabaseDirectory = TestHelper.createTempDirectory();

        Path jsonDatabasePath = Paths.get(thisClass.getResource("/db-json").getFile());
        Files.walk(jsonDatabasePath)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        Files.copy(path, Paths.get(jsonDatabaseDirectory).resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        List<DbDto> topicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDatabaseDirectory);

        return new DatabaseContext(topicObjects, jsonDatabaseDirectory);
    }
}
