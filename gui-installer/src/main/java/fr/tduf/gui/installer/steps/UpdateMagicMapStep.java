package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.banks.mapping.helper.MagicMapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;

import java.io.IOException;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Only updates TDU mapping system to accept new files.
 */
class UpdateMagicMapStep extends GenericStep {

    private static final String THIS_CLASS_NAME = UpdateMagicMapStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");

        String bankDirectory = getInstallerConfiguration().resolveBanksDirectory();
        String magicMapFile = Paths.get(bankDirectory, MapHelper.MAPPING_FILE_NAME).toString();

        Log.info(THIS_CLASS_NAME, "->Magic Map file: " + magicMapFile);

        MagicMapHelper.fixMagicMap(bankDirectory)

                .forEach(fileName -> Log.info(THIS_CLASS_NAME, "*> added checksum of " + fileName));
    }
}
