package fr.tduf.libunlimited.common.configuration;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.common.game.domain.Locale;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import static fr.tduf.libunlimited.common.forever.FileConstants.DIRECTORY_CONFIGURATION;
import static fr.tduf.libunlimited.common.forever.FileConstants.FORMAT_THEME_FILE;
import static java.util.Collections.enumeration;
import static java.util.Optional.ofNullable;

/**
 * Stores all information for TDUF tools
 */
public class ApplicationConfiguration extends Properties {
    private static final String THIS_CLASS_NAME = ApplicationConfiguration.class.getSimpleName();

    private static String configurationFile = Paths.get(DIRECTORY_CONFIGURATION, "tduf.properties").toString();

    private static final String KEY_DATABASE_DIR = "tdu.database.directory";
    private static final String KEY_TDU_DIR = "tdu.root.directory";
    private static final String KEY_EDITOR_LOCALE = "tduf.editor.locale";
    private static final String KEY_EDITOR_PROFILE = "tduf.editor.profile";
    private static final String KEY_EDITOR_PLUGINS_ENABLED = "tduf.editor.plugins.enabled";
    private static final String KEY_EDITOR_DEBUGGING_ENABLED = "tduf.editor.debugging.enabled";
    private static final String KEY_EDITOR_CUSTOM_THEME = "tduf.editor.theme";

    /**
     * Solves the issue of random key ordering
     * @return sorted keys as enumeration
     */
    @Override
    public synchronized Enumeration<Object> keys() {
        return enumeration(new TreeSet<>(super.keySet()));
    }

    /**
     * @return full path to game database if it exists, else return default location from game directory, or empty otherwise
     */
    public Optional<Path> getDatabasePath() {
        Path fallbackDatabasePath = getGamePath()
                .map(gamePath -> gamePath
                        .resolve(FileConstants.DIRECTORY_EURO)
                        .resolve(FileConstants.DIRECTORY_BANKS)
                        .resolve(FileConstants.DIRECTORY_DATABASE))
                .orElse(null);

        return ofNullable(resolvePathProperty(KEY_DATABASE_DIR, fallbackDatabasePath));
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
        return ofNullable(resolvePathProperty(KEY_TDU_DIR, null));
    }    
    
    /**
     * @param gameLocation  : path to root game directory
     */
    public void setGamePath(String gameLocation) {
        setProperty(KEY_TDU_DIR, gameLocation);
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
     * @return true when enabled Database Editor plugins, false otherwise
     */
    public boolean isEditorPluginsEnabled() {
        return resolveBooleanProperty(KEY_EDITOR_PLUGINS_ENABLED, true);
    }

    /**
     * @param enabled   : true to enable editor plugins, false otherwise
     */
    public void setEditorPluginsEnabled(boolean enabled) {
        setProperty(KEY_EDITOR_PLUGINS_ENABLED, Boolean.toString(enabled));
    }

    /**
     * @return true when enabled Database Editor debugging, false otherwise
     */
    public boolean isEditorDebuggingEnabled() {
        return resolveBooleanProperty(KEY_EDITOR_DEBUGGING_ENABLED, false);
    }

    /**
     * @return path for custom theme css
     */
    public Optional<Path> getEditorCustomThemeCss() {
       return(ofNullable(getProperty(KEY_EDITOR_CUSTOM_THEME))
               .map(theme -> Paths.get(DIRECTORY_CONFIGURATION, String.format(FORMAT_THEME_FILE, theme))));
    }

    /**
     * Saves current configuration into user home directory.
     * @throws IOException when storage error occurs
     */
    public void store() throws IOException {
        Path parentPath = Paths.get(configurationFile).getParent();
        Files.createDirectories(parentPath);

        try (OutputStream os = new FileOutputStream(configurationFile)) {
            store(os, "TDUF configuration");
        }
    }

    /**
     * Loads configuration from user home directory.
     * @throws IOException when access error occurs
     */
    public void load() throws IOException {
        try {
            try (InputStream is = new FileInputStream(configurationFile)) {
                load(is);
            }
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

    private Path resolvePathProperty(String propKey, Path defaultValue) {
        return ofNullable(getProperty(propKey))
                .map(Paths::get)
                .orElse(defaultValue);
    }

    private boolean resolveBooleanProperty(String propKey, boolean defaultValue) {
        return ofNullable(getProperty(propKey))
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    // For tests
    static void setConfigurationFile(String file) {
        configurationFile = file;
    }
}
