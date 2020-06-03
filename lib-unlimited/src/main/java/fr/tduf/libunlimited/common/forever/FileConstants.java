package fr.tduf.libunlimited.common.forever;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Constants for common TDUF file names and directories
 */
public class FileConstants {
    private static final String THIS_CLASS_NAME = FileConstants.class.getSimpleName();

    public static String DIRECTORY_ROOT;
    public static String DIRECTORY_EXTERNAL_STRUCTURES;

    static {
        try {
            DIRECTORY_ROOT = FilesHelper.getRootDirectory().toString();
            DIRECTORY_EXTERNAL_STRUCTURES = Paths.get(DIRECTORY_ROOT, "structures").toString();
        } catch (IOException ioe) {
            Log.warn(THIS_CLASS_NAME, "Unable to resolve application root directory", ioe);
            DIRECTORY_ROOT = null;
            DIRECTORY_EXTERNAL_STRUCTURES = null;
        }
    }
}
