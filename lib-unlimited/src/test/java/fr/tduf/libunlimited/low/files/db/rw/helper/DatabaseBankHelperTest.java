package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
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
        System.out.println("Using temporary target directory: " + tempDir);

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
    public void repackDatabaseFromDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN
        String databaseDirectory = new File(thisClass.getResource("/db/full/unpacked/original-DB.bnk").toURI()).getParent();
        String targetDirectory = this.tempDirectory;


        // WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(databaseDirectory, targetDirectory, bankSupportMock);


        // THEN
        verify(bankSupportMock, times(9)).preparePackAll(eq(databaseDirectory), anyString());

        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB.bnk").toString()), eq(Paths.get(targetDirectory, "DB.bnk").toString()));
        verify(bankSupportMock).packAll(eq(Paths.get(databaseDirectory, "DB_FR.bnk").toString()), eq(Paths.get(targetDirectory, "DB_FR.bnk").toString()));
    }

    @Test(expected = NullPointerException.class)
    public void repackDatabaseFromDirectory_whenNullArgumentsShouldThrowException() throws IOException {
        // GIVEN-WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(null, null, null);

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

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-tests").toString();
    }
}