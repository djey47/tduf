package fr.tduf.libunlimited.common.cache;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseBanksCacheHelperTest {

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    private String databaseDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();

        databaseDirectory = Paths.get(tempDirectory, "Euro", "Bnk", "Database").toString();

        createFakeDatabase(databaseDirectory, "");
    }

    @Test
    public void unpackDatabaseToJsonWithCacheSupport_whenCachePresentAndBankFilesOlder_shouldNotUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = createCacheDirectory();
        Files.createFile(cachePath.resolve("last"));


        // WHEN
        String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseDirectory), bankSupportMock);


        // THEN
        verifyZeroInteractions(bankSupportMock);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
    }

    @Test
    public void unpackDatabaseToJsonWithCacheSupport_whenCachePresentAndBankFilesNewer_shouldUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = initCacheTimestamp();


        // WHEN
        String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseDirectory), bankSupportMock);


        // THEN
        Path databasePath = getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
    }

    @Test
    public void unpackDatabaseToJsonWithCacheSupport_whenNoCache_shouldUseBankSupportComponent_andReturnNewCacheDirectory() throws IOException {
        // GIVEN-WHEN
        String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseDirectory), bankSupportMock);


        // THEN
        Path databasePath = getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        final Path cachePath = Paths.get(databaseDirectory, "json-cache");
        assertThat(jsonDirectory).isEqualTo(cachePath.toString());
        assertThat(cachePath.toFile()).exists();
    }

    @Test
    public void updateCacheDirectory_whenNoCache_shouldCreateCacheDirectory() throws IOException {
        // GIVEN-WHEN
        DatabaseBanksCacheHelper.updateCacheDirectory(Paths.get(databaseDirectory));

        // THEN
        final File lastFile = Paths.get(databaseDirectory).resolve("json-cache").resolve("last").toFile();
        assertThat(lastFile).exists();
        assertThat(lastFile.lastModified()).isGreaterThan(0);
    }

    @Test
    public void updateCacheDirectory_whenCacheExists_shouldUpdateTimestampInLastFile() throws IOException {
        // GIVEN
        Path cachePath = initCacheTimestamp();

        // WHEN
        DatabaseBanksCacheHelper.updateCacheDirectory(Paths.get(databaseDirectory));

        // THEN
        final File lastFile = cachePath.resolve("last").toFile();
        assertThat(lastFile).exists();
        assertThat(lastFile.lastModified()).isGreaterThan(0);
    }

    private Path initCacheTimestamp() throws IOException {
        final Path cachePath = createCacheDirectory();
        assert Files.createFile(cachePath.resolve("last")).toFile().setLastModified(0);

        return cachePath;
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

    private static Path getTduDatabasePath(String tempDirectory) {
        return Paths.get(tempDirectory).resolve("Euro").resolve("Bnk").resolve("Database");
    }

    private static void createFakeDatabase(String databaseDirectory, String bankFileNamePrefix) throws IOException {
        Path databaseBanksPath = Paths.get(databaseDirectory);
        Files.createDirectories(databaseBanksPath);

        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_CH.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_FR.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_GE.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_KO.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_IT.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_JA.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_SP.bnk"));
        Files.createFile(databaseBanksPath.resolve(bankFileNamePrefix + "DB_US.bnk"));
    }
}
