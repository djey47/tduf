package fr.tduf.libunlimited.low.files.db.rw.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseBankHelperTest {

    private static Class<DatabaseBankHelperTest> thisClass = DatabaseBankHelperTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    @Before
    public void setUp() throws IOException {
        Log.set(Log.LEVEL_INFO);

        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();
    }

    @Test
    public void unpackDatabaseFromDirectory_whenNoTargetDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN
        String databaseDirectory = new File(thisClass.getResource("/db/full/DB.bnk").toURI()).getParent();

        mockBankSupportToExtract();


        // WHEN
        String actualDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.<String>empty(), bankSupportMock);


        // THEN
        assertThat(actualDirectory).isNotNull();

        DatabaseBankHelper.getDatabaseBankFileNames()

                .forEach((bankFileName) -> {
                    try {
                        verify(bankSupportMock).extractAll(eq(Paths.get(databaseDirectory, bankFileName).toString()), anyString());
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }

                    assertThat(new File(actualDirectory, "original-" + bankFileName)).exists();
                });
    }

    @Test
    public void unpackDatabaseFromDirectory_whenTargetDirectory_shouldCopyOriginalBankFiles() throws IOException, URISyntaxException {
        // GIVEN
        String tempDir = createTempDirectory();
        String databaseDirectory = new File(thisClass.getResource("/db/full/DB.bnk").toURI()).getParent();

        mockBankSupportToExtract();


        // WHEN
        String actualDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, Optional.of(tempDir), bankSupportMock);


        // THEN
        Log.info(thisClass.getSimpleName(), "Using temporary target directory: " + tempDir);

        assertThat(actualDirectory).isNotNull();

        DatabaseBankHelper.getDatabaseBankFileNames()

                .forEach((bankFileName) -> assertThat(new File(tempDir, "original-" + bankFileName)).exists());
    }

    @Test(expected = NullPointerException.class)
    public void unpackDatabaseFromDirectory_whenNullArgumentsShouldThrowException() throws IOException {
        // GIVEN-WHEN
        DatabaseBankHelper.unpackDatabaseFromDirectory(null, Optional.<String>empty(), null);

        // THEN: NPE
    }

    @Test
    public void repackDatabaseFromDirectory_whenNoOriginalBanksDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN: original banks provided in same directory
        String databaseDirectory = thisClass.getResource("/db/full/unpacked").getFile();
        String targetDirectory = tempDirectory;

        copyOriginalBanksToDirectory(databaseDirectory);


        // WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(databaseDirectory, targetDirectory, Optional.empty(), bankSupportMock);


        // THEN
        verifyBankSupportCalls(databaseDirectory, targetDirectory);
    }

    @Test
    public void repackDatabaseFromDirectory_whenOriginalBanksDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN: original banks provided in another directory
        String originalBanksDirectory = thisClass.getResource("/db/full/original-banks").getFile();
        String databaseDirectory = thisClass.getResource("/db/full/unpacked").getFile();
        String targetDirectory = tempDirectory;


        // WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(databaseDirectory, targetDirectory, Optional.of(originalBanksDirectory), bankSupportMock);


        // THEN
        verifyBankSupportCalls(databaseDirectory, targetDirectory);
    }

    @Test(expected = NullPointerException.class)
    public void repackDatabaseFromDirectory_whenNullArgumentsShouldThrowException() throws IOException {
        // GIVEN-WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(null, null, Optional.<String>empty(), null);

        // THEN: NPE
    }

    @Test
    public void getDatabaseBankFileNames_shouldReturnCorrectList() {
        // GIVEN-WHEN
        List<String> actualFileNames = DatabaseBankHelper.getDatabaseBankFileNames();

        // THEN
        String[] expectedFileNames = {"DB.bnk", "DB_CH.bnk", "DB_FR.bnk", "DB_GE.bnk", "DB_IT.bnk", "DB_JA.bnk", "DB_KO.bnk", "DB_SP.bnk", "DB_US.bnk"};
        assertThat(actualFileNames).containsOnly(expectedFileNames);
    }

    private void mockBankSupportToExtract() throws IOException {
        Mockito
                .doAnswer((invocation) -> {
                    String bankFileName = (String) invocation.getArguments()[0];
                    String outputDirectory = (String) invocation.getArguments()[1];

                    String bankShortFileName = Paths.get(bankFileName).getFileName().toString();
                    Files.createDirectory(Paths.get(outputDirectory, bankShortFileName));
                    Files.createFile(Paths.get(outputDirectory, "original-" + bankShortFileName));

                    return invocation;
                })

                .when(bankSupportMock).extractAll(anyString(), anyString());
    }

    private void verifyBankSupportCalls(String databaseDirectory, String targetDirectory) throws IOException {
        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB.bnk").toString()), eq(Paths.get(targetDirectory, "DB.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_FR.bnk").toString()), eq(Paths.get(targetDirectory, "DB_FR.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_GE.bnk").toString()), eq(Paths.get(targetDirectory, "DB_GE.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_IT.bnk").toString()), eq(Paths.get(targetDirectory, "DB_IT.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_SP.bnk").toString()), eq(Paths.get(targetDirectory, "DB_SP.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_CH.bnk").toString()), eq(Paths.get(targetDirectory, "DB_CH.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_KO.bnk").toString()), eq(Paths.get(targetDirectory, "DB_KO.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_US.bnk").toString()), eq(Paths.get(targetDirectory, "DB_US.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_JA.bnk").toString()), eq(Paths.get(targetDirectory, "DB_JA.bnk").toString()));
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-tests").toString();
    }

    private static void copyOriginalBanksToDirectory(String databaseDirectory) throws IOException {
        String originalBanksDirectory = thisClass.getResource("/db/full/original-banks").getFile();
        Files.walk(Paths.get(originalBanksDirectory))

                .filter(Files::isRegularFile)

                .filter((path) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        Files.copy(path, Paths.get(databaseDirectory).resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
