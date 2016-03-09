package fr.tduf.libunlimited.common.cache;

import com.esotericsoftware.minlog.Log;
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
import java.nio.file.StandardOpenOption;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.lang.Long.valueOf;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseBanksCacheHelperTest {

    private static final Class<DatabaseBanksCacheHelperTest> thisClass = DatabaseBanksCacheHelperTest.class;

    @Mock
    private BankSupport bankSupportMock;

    private String tempDirectory;

    private String databaseDirectory;

    @Before
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();

        databaseDirectory = Paths.get(tempDirectory, "Euro", "Bnk", "Database").toString();

        createFakeDatabase(databaseDirectory, "");

        Log.set(Log.LEVEL_INFO);
    }

    @Test
    public void unpackDatabaseToJsonWithCacheSupport_whenCachePresentAndBankFilesOlder_shouldNotUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = createCacheDirectory();
        long originalTimestamp = System.currentTimeMillis();
        final File lastFile = initCacheTimestamp(originalTimestamp).toFile();


        // WHEN
        String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseDirectory), bankSupportMock);


        // THEN
        verifyZeroInteractions(bankSupportMock);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());

        assertLastFileUntouched(lastFile.toPath(), originalTimestamp);
    }

    @Test
    public void unpackDatabaseToJsonWithCacheSupport_whenCachePresentAndBankFilesNewer_shouldUseBankSupportComponent_andReturnCacheDirectory() throws IOException {
        // GIVEN
        final Path cachePath = initCacheTimestamp(0);


        // WHEN
        String jsonDirectory = DatabaseBanksCacheHelper.unpackDatabaseToJsonWithCacheSupport(Paths.get(databaseDirectory), bankSupportMock);


        // THEN
        Path databasePath = getTduDatabasePath(tempDirectory);

        verifyBankSupportComponentUsage(databasePath);

        assertThat(jsonDirectory).isEqualTo(cachePath.toString());

        assertLastFileUpdated(cachePath, 0);
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

        assertLastFileUpdated(cachePath, -1);
    }

    @Test
    public void repackDatabaseFromJsonWithCacheSupport_whenCacheInfoDoesNotExist_shouldCallBankSupportComponent_andSetTimestamp() throws IOException, ReflectiveOperationException {
        // GIVEN
        createCacheDirectory();
        String jsonDatabaseDirectory = createJsonDatabase(databaseDirectory);
        createFakeDatabase(jsonDatabaseDirectory, "original-");
        final Path tduDatabasePath = getTduDatabasePath(tempDirectory);

        // WHEN
        DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(tduDatabasePath, bankSupportMock);

        // THEN
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_CH.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_FR.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_GE.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_KO.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_IT.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_JA.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_SP.bnk").toString()));
        verify(bankSupportMock).packAll(anyString(), eq(tduDatabasePath.resolve("DB_US.bnk").toString()));
        verifyNoMoreInteractions(bankSupportMock);

        Path lastFilePath = tduDatabasePath.resolve("json-cache").resolve("last");
        final File lastFile = lastFilePath.toFile();
        assertThat(lastFile).exists();
    }

    @Test
    public void repackDatabaseFromJsonWithCacheSupport_whenCacheInfoExists_shouldUpdateTimestamp() throws IOException, ReflectiveOperationException, InterruptedException {
        // GIVEN
        createCacheDirectory();
        String jsonDatabaseDirectory = createJsonDatabase(databaseDirectory);
        final Path tduDatabasePath = getTduDatabasePath(tempDirectory);
        Path lastFilePath = tduDatabasePath.resolve("json-cache").resolve("last");
        Files.createDirectories(lastFilePath.getParent());
        Files.createFile(lastFilePath);
        long initialTimestamp = lastFilePath.toFile().lastModified();

        createFakeDatabase(jsonDatabaseDirectory, "original-");

        // WHEN
        Thread.sleep(1000);
        DatabaseBanksCacheHelper.repackDatabaseFromJsonWithCacheSupport(tduDatabasePath, bankSupportMock );

        // THEN
        assertThat(lastFilePath.toFile().lastModified()).isGreaterThan(initialTimestamp);
    }

    @Test
    public void updateCacheDirectory_whenNoCache_shouldCreateCacheDirectory() throws IOException {
        // GIVEN-WHEN
        DatabaseBanksCacheHelper.updateCacheTimestamp(Paths.get(databaseDirectory));

        // THEN
        assertLastFileUpdated(Paths.get(databaseDirectory).resolve("json-cache"), -1);
    }

    @Test
    public void updateCacheDirectory_whenCacheExists_shouldUpdateTimestampInLastFile() throws IOException {
        // GIVEN
        Path cachePath = initCacheTimestamp(0);

        // WHEN
        DatabaseBanksCacheHelper.updateCacheTimestamp(Paths.get(databaseDirectory));

        // THEN
        assertLastFileUpdated(cachePath, 0);
    }

    @Test
    public void resolveCachePath() {
        // GIVEN
        final Path databasePath = Paths.get("/home/djey/tdu/db/");

        // WHEN
        final Path actualPath = DatabaseBanksCacheHelper.resolveCachePath(databasePath);

        // THEN
        assertThat(actualPath.getParent()).isEqualTo(databasePath);
        assertThat(actualPath.getFileName()).isEqualTo(Paths.get("json-cache"));
    }

    private Path initCacheTimestamp(long time) throws IOException {
        final Path cachePath = createCacheDirectory();
        final Path lastFilePath = cachePath.resolve("last");
        Files.write(lastFilePath, singletonList(valueOf(time).toString()), StandardOpenOption.CREATE_NEW);

        return cachePath;
    }

    private Path createCacheDirectory() throws IOException {
        final Path cachePath = Paths.get(databaseDirectory, "json-cache");
        return Files.createDirectories(cachePath);
    }

    private static File assertLastFileExists(Path cachePath) {
        final File lastFile = cachePath.resolve("last").toFile();
        assertThat(lastFile).exists();
        return lastFile;
    }

    private static void assertLastFileUpdated(Path cachePath, long initialTime) throws IOException {
        final File lastFile = assertLastFileExists(cachePath);

        String timestamp = Files.readAllLines(lastFile.toPath()).get(0);
        assertThat(valueOf(timestamp)).isGreaterThan(initialTime);
    }

    private static void assertLastFileUntouched(Path cachePath, long initialTime) throws IOException {
        final File lastFile = assertLastFileExists(cachePath);

        String timestamp = Files.readAllLines(lastFile.toPath()).get(0);
        assertThat(valueOf(timestamp)).isEqualTo(initialTime);
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

    private static String createJsonDatabase(String realDatabaseDirectory) throws IOException {
        Path jsonDatabasePath = Paths.get(realDatabaseDirectory, "json-cache");

        Path originalJsonDatabasePath = Paths.get(thisClass.getResource("/db/json").getFile());
        Files.walk(originalJsonDatabasePath, 1)

                .filter((path) -> Files.isRegularFile(path))

                .filter((path) -> EXTENSION_JSON.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(path.toString())))

                .forEach((path) -> {
                    try {
                        Files.copy(path, jsonDatabasePath.resolve(path.getFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return jsonDatabasePath.toString();
    }
}
