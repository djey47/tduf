package fr.tduf.libunlimited.common.configuration;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.Locale;

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
    private static final String KEY_EDITOR_LOCALE = "tduf.editor.locale";
    private static final String KEY_EDITOR_PROFILE = "tduf.editor.profile";

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
     * @param locale    : current locale to be saved for database Editor
     */
    public void setEditorLocale(Locale locale) {
        setProperty(KEY_EDITOR_LOCALE, locale.getCode());
    }

    /**
     * @return last used editor locale if any, or empty otherwise
     */
    public Optional<Locale> getEditorLocale() {
        return ofNullable(getProperty(KEY_EDITOR_LOCALE))
                .map(Locale::fromCode);
    }

    /**
     * @param profileName    : current profile to be saved for database Editor
     */
    public void setEditorProfile(String profileName) {
        setProperty(KEY_EDITOR_PROFILE, profileName);
    }

    /**
     * @return last used editor profile if any, or empty otherwise
     */
    public Optional<String> getEditorProfile() {
        return ofNullable(getProperty(KEY_EDITOR_PROFILE));
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


    /**
     * Deletes all settings and saves configuration file
     */
    public void reset() throws IOException {
        clear();
        store();
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
