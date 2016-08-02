package fr.tduf.libunlimited.common.configuration;

import com.esotericsoftware.minlog.Log;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static java.util.Optional.ofNullable;

/**
 * Stores all information for TDUF tools
 */
public class ApplicationConfiguration extends Properties {
    private static final String THIS_CLASS_NAME = ApplicationConfiguration.class.getSimpleName();

    private static String configurationFile = Paths.get(System.getProperty("user.home"), "tduf.properties").toString();

    private static final String KEY_DATABASE_DIR = "tdu.database.directory";
    private static final String KEY_TDU_DIR = "tdu.root.directory";

    /**
     * @return full path to game database if it exists, else return default location from game directory, or empty otherwise
     */
    public Optional<Path> getDatabasePath() {
        if (getPathProperty(KEY_DATABASE_DIR).isPresent()) {
            return getPathProperty(KEY_DATABASE_DIR);
        }

        return getGamePath()
                .map(gamePath -> gamePath
                        .resolve("Euro")
                        .resolve("Bnk")
                        .resolve("Database"));
    }


    /**
     * @param databaseLocation  : path to game database
     */
    public void setDatabasePath(String databaseLocation) {
        setProperty(KEY_DATABASE_DIR, databaseLocation);
    }

    /**
     * @return full path to game if it exists, or empty otherwise
     */
    public Optional<Path> getGamePath() {
        return ofNullable(getProperty(KEY_TDU_DIR))
                .map(Paths::get);
    }

    /**
     * Saves current configuration into user home directory.
     * @throws IOException when storage error occurs
     */
    public void store() throws IOException {
        OutputStream os = new FileOutputStream(configurationFile);
        store(os, "TDUF configuration");
    }

    /**
     * Loads configuration from user home directory.
     * @throws IOException when access error occurs
     */
    public void load() throws IOException {
        try {
            InputStream is = new FileInputStream(configurationFile);
            load(is);
        } catch (FileNotFoundException fnfe) {
            Log.info(THIS_CLASS_NAME, "Configuration file does not exist, still. It will be created.", fnfe);
            store();
        }
    }

    private Optional<Path> getPathProperty(String propKey) {
        return ofNullable(getProperty(propKey))
                .map(Paths::get);
    }

    // For tests
    static void setConfigurationFile(String file) {
        configurationFile = file;
    }
}
