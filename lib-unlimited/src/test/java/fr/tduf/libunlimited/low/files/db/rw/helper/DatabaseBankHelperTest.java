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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    public void unpackDatabaseFromDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN
        String databaseDirectory = new File(thisClass.getResource("/db/full/DB.bnk").toURI()).getParent();

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


        // WHEN
        String actualDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(databaseDirectory, bankSupportMock);


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

    @Test(expected = NullPointerException.class)
    public void unpackDatabaseFromDirectory_whenNullArgumentsShouldThrowException() throws IOException {
        // GIVEN-WHEN
        DatabaseBankHelper.unpackDatabaseFromDirectory(null, null);

        // THEN: NPE
    }

    @Test
    public void repackDatabaseFromDirectory_shouldCallBankSupport_andReturnOutputDirectory() throws IOException, URISyntaxException {
        // GIVEN
        String databaseDirectory = new File(thisClass.getResource("/db/full/unpacked/TDU_CarPhysicsData.db").toURI()).getParent();
        String targetDirectory = this.tempDirectory;


        // WHEN
        DatabaseBankHelper.repackDatabaseFromDirectory(databaseDirectory, targetDirectory, bankSupportMock);


        // THEN
        DatabaseBankHelper.getDatabaseBankFileNames()

                .forEach((bankFileName) -> {
                    try {
                        verify(bankSupportMock).packAll(anyString(), eq(Paths.get(targetDirectory, bankFileName).toString()));
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                });
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
}