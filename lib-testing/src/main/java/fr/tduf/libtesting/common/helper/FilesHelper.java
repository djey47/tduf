package fr.tduf.libtesting.common.helper;

import fr.tduf.libunlimited.common.game.FileConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FilesHelper {
    private static final Class<FilesHelper> thisClass = FilesHelper.class;

    public static final String[] DATABASE_BANK_FILES = {"DB.bnk", "DB_CH.bnk", "DB_FR.bnk", "DB_GE.bnk", "DB_KO.bnk", "DB_US.bnk", "DB_JA.bnk", "DB_SP.bnk", "DB_IT.bnk"};

    public static String createTempDirectoryForLauncher() throws IOException {
        return Files.createTempDirectory("guiLauncher-tests").toString();
    }    
    
    public static String createTempDirectoryForInstaller() throws IOException {
        return Files.createTempDirectory("guiInstaller-tests").toString();
    }

    public static String createTempDirectoryForLibrary() throws IOException {
        return Files.createTempDirectory("libUnlimited-tests").toString();
    }

    public static String createTempDirectoryForDatabaseEditor() throws IOException {
        return Files.createTempDirectory("guiDatabase-tests").toString();
    }

    public static void prepareTduDirectoryLayout(String tduDirectory) throws IOException {
        Path banksPath = Paths.get(tduDirectory, "Euro", "Bnk");

        Path vehicleBanksPath = banksPath.resolve("Vehicules");
        createDirectoryIfNotExists(vehicleBanksPath.toString());

        Path rimBanksPath = vehicleBanksPath.resolve("Rim");
        createDirectoryIfNotExists(rimBanksPath.toString());

        Path frontEndPath = banksPath.resolve("FrontEnd");
        Path lowGaugesBanksPath = frontEndPath.resolve("LowRes").resolve("Gauges");
        createDirectoryIfNotExists(lowGaugesBanksPath.toString());
        Path highGaugesBanksPath = frontEndPath.resolve("HiRes").resolve("Gauges");
        createDirectoryIfNotExists(highGaugesBanksPath.toString());

        Path soundBanksPath = banksPath.resolve("Sound").resolve("Vehicules");
        createDirectoryIfNotExists(soundBanksPath.toString());

        Path magicMapPath = Paths.get(thisClass.getResource("/banks/Bnk1.map").getFile());
        Files.copy(magicMapPath, banksPath.resolve(magicMapPath.getFileName()));

        Files.createFile(banksPath.resolve("test1.bnk"));
        Files.createFile(banksPath.resolve("test2.bnk"));
        Files.createFile(banksPath.resolve("test3.bnk"));
    }

    public static void createFakeDatabase(String databaseDirectory) throws IOException {
        Path databaseBanksPath = Paths.get(databaseDirectory);
        Files.createDirectories(databaseBanksPath);

        Stream.of(DATABASE_BANK_FILES)
                .forEach((fileName) -> {
                    try {
                        Files.createFile(databaseBanksPath.resolve(fileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static Path getTduDatabasePath(String tempDirectory) {
        return Paths.get(tempDirectory)
                .resolve(FileConstants.DIRECTORY_EURO)
                .resolve(FileConstants.DIRECTORY_BANKS)
                .resolve(FileConstants.DIRECTORY_DATABASE);
    }

    private static void createDirectoryIfNotExists(String directoryToCreate) throws IOException {
        Files.createDirectories(Paths.get(directoryToCreate));
    }
}
