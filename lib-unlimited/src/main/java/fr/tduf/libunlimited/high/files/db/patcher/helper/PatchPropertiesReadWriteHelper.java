package fr.tduf.libunlimited.high.files.db.patcher.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Helper class to open or save patch properties with disk.
 */
public class PatchPropertiesReadWriteHelper {
    private static final String THIS_CLASS_NAME = PatchPropertiesReadWriteHelper.class.getSimpleName();

    /**
     * @param patchFile : JSON patch file to be applied
     * @return loaded properties if a corresponding property file exists
     */
    public static PatchProperties readPatchProperties(File patchFile) throws IOException {

        final PatchProperties patchProperties = new PatchProperties();

        readProperties(patchFile, patchProperties);

        return patchProperties;
    }

    /**
     * @param patchFile : JSON mini patch file to be applied
     * @return loaded properties if a corresponding property file exists
     */
    public static DatabasePatchProperties readDatabasePatchProperties(File patchFile) throws IOException {

        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();

        readProperties(patchFile, patchProperties);

        return patchProperties;
    }

    /**
     * @param patchFile : JSON mini patch file which has just been applied
     * @return path of effective property file if any, otherwise empty
     */
    public static Optional<String> writeEffectivePatchProperties(DatabasePatchProperties patchProperties, String patchFile) throws IOException {
        return writeEffectivePatchPropertiesWithPrefix(patchProperties, patchFile, "effective-");
    }

    /**
     * @param patchFile : JSON mini patch file which has just been applied
     * @return path of property file if any, otherwise empty
     */
    public static Optional<String> writePatchProperties(DatabasePatchProperties patchProperties, String patchFile) throws IOException {
        return writeEffectivePatchPropertiesWithPrefix(patchProperties, patchFile, "");
    }

    private static void readProperties(File patchFile, PatchProperties patchProperties) throws IOException {
        requireNonNull(patchProperties, "Patch properties are required");

        String propertyFile = patchFile + ".properties";
        final File propertyFileHandle = new File(propertyFile);
        if(propertyFileHandle.exists()) {

            Log.info(THIS_CLASS_NAME, "Using patch properties file: " + propertyFile);

            final InputStream inputStream = new FileInputStream(propertyFileHandle);
            patchProperties.load(inputStream);

        } else {

            Log.info(THIS_CLASS_NAME, "Patch properties file not provided: " + propertyFile);

        }
    }

    private static Optional<String> writeEffectivePatchPropertiesWithPrefix(DatabasePatchProperties patchProperties, String patchFile, String prefix) throws IOException {
        if (patchProperties.isEmpty()) {
            return empty();
        }

        final Path patchPath = Paths.get(patchFile);
        Path patchParentPath = patchPath.getParent();
        String patchFileName = patchPath.getFileName().toString();
        final String targetFileName = prefix + patchFileName + ".properties";
        String targetPropertyFile = patchParentPath.resolve(targetFileName).toString();

        Log.info(THIS_CLASS_NAME, "Writing properties file: " + targetPropertyFile);

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);

        return of(targetPropertyFile);
    }
}
