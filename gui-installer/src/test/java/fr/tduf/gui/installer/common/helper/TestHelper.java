package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;

/**
 * Static class providing useful methods for testing.
 */
// TODO migrate to common test lib
public class TestHelper {

    private static final Class<TestHelper> thisClass = TestHelper.class;

    // TODO create methods for various origins
    public static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("guiInstaller-tests").toString();
    }

    public static void createFakeDatabase(String databaseDirectory, String bankFileNamePrefix) throws IOException {
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

    public static DatabaseContext createJsonDatabase() throws IOException {
        String jsonDatabaseDirectory = TestHelper.createTempDirectory();

        Path jsonDatabasePath = Paths.get(thisClass.getResource("/db-json").getFile());
        Files.walk(jsonDatabasePath, 1)

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

    public static void prepareTduDirectoryLayout(String tduDirectory) throws IOException {
        Path banksPath = Paths.get(tduDirectory, "Euro", "Bnk");

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

    public static Path getTduDatabasePath(String tempDirectory) {
        return Paths.get(tempDirectory).resolve("Euro").resolve("Bnk").resolve("Database");
    }
}
