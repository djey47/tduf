package fr.tduf.gui.installer.common;

/**
 * Gives all constants to be used by application logic.
 */
public class InstallerConstants {

    public static final String FILE_README = "./assets/README/README.txt";

    public static final String PATTERN_BACKUP_DIRECTORY = "uu-MM-dd HH-mm-ss";
    public static final String DIRECTORY_BACKUP = "backup";
    public static final String DIRECTORY_SUB_BACKUP_DATABASE = "database";
    public static final String DIRECTORY_SUB_BACKUP_FILES = "files";

    // TODO remove ./ when possible
    public static final String DIRECTORY_ASSETS = "./assets";
    public static final String DIRECTORY_3D = "3D";
    public static final String DIRECTORY_DATABASE = "DATABASE";
    public static final String DIRECTORY_GAUGES_LOW = "GAUGES/LOW";
    public static final String DIRECTORY_GAUGES_HIGH = "GAUGES/HI";
    public static final String DIRECTORY_RIMS = "3D/RIMS";
    public static final String DIRECTORY_SOUND = "SOUND";

    public static final String FILE_NAME_EFFECTIVE_PROPERTIES = "installed.properties";
    public static final String FILE_NAME_EFFECTIVE_PATCH = "installed.mini.json";
    public static final String FILE_NAME_SNAPSHOT_PATCH = "SNAPSHOT.mini.json";

    private InstallerConstants() {}
}
