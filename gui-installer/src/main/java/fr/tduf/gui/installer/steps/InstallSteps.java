package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.banks.mapping.helper.MagicMapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Orchestrates all operations to install vehicle mod.
 */
public class InstallSteps {

    /**
     * Entry point for full install
     * @param configuration : settings to install required mod.
     */
    public static void install(InstallerConfiguration configuration) {
        // TODO call update magic map step and check for errors
    }

    public static void copyFilesStep(InstallerConfiguration configuration) {

    }

    /**
     * Only updates TDU mapping system to accept new files.
     * @param configuration : settings to perform current step
     * @return name of updated magic map file.
     * @throws IOException
     */
    public static String updateMagicMapStep(InstallerConfiguration configuration) throws IOException {

        String bankDirectory = Paths.get(configuration.getTestDriveUnlimitedDirectory(), "Euro", "Bnk").toString();
        MagicMapHelper.fixMagicMap(bankDirectory);

        return Paths.get(bankDirectory, MapHelper.MAPPING_FILE_NAME).toString();
    }

    public static void updateDatabaseStep(InstallerConfiguration configuration) {

    }
}