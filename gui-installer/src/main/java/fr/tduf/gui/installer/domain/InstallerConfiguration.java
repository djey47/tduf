package fr.tduf.gui.installer.domain;

import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.GenuineCamGateway;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;

import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Class to embed all settings necessary to install/uninstall.
 */
public class InstallerConfiguration {

    private String testDriveUnlimitedDirectory;

    private String assetsDirectory;

    private String backupDirectory;

    private String installerDirectory;

    private BankSupport bankSupport;

    private GenuineCamGateway cameraSupport;

    private InstallerConfiguration() {
    }

    public String resolveBanksDirectory() {
        return Paths.get(testDriveUnlimitedDirectory, "Euro", "Bnk").toString();
    }

    public String resolveMagicMapFile() {
        String bankDirectory = resolveBanksDirectory();
        return Paths.get(bankDirectory, MapHelper.MAPPING_FILE_NAME).toString();
    }

    public String resolveDatabaseDirectory() {
        return Paths.get(resolveBanksDirectory()).resolve("Database").toString();
    }

    public String resolveFilesBackupDirectory() {
        return Paths.get(backupDirectory, InstallerConstants.DIRECTORY_SUB_BACKUP_FILES).toString();
    }

    public String getAssetsDirectory() {
        return assetsDirectory;
    }

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    public BankSupport getBankSupport() {
        return bankSupport;
    }

    public GenuineCamGateway getCameraSupport() {
        return cameraSupport;
    }

    public String getInstallerDirectory() {
        return installerDirectory;
    }

    /**
     * @return builder, to create custom instances.
     */
    public static InstallerConfigurationBuilder builder() {
        return new InstallerConfigurationBuilder();
    }

    public static class InstallerConfigurationBuilder {
        private String testDriveUnlimitedDirectory;
        private String assetsDirectory = InstallerConstants.DIRECTORY_ASSETS;
        private String installerDirectory = ".";
        private BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());
        private GenuineCamGateway cameraSupport = new GenuineCamGateway(new CommandLineHelper());

        public InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory) {
            this.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;
            return this;
        }

        public InstallerConfigurationBuilder withAssetsDirectory(String assetsDirectory) {
            this.assetsDirectory = Paths.get(installerDirectory).resolve(assetsDirectory).toString();
            return this;
        }

        public InstallerConfigurationBuilder overridingCameraSupport(GenuineCamGateway cameraSupport) {
            this.cameraSupport = cameraSupport;
            return this;
        }

        public InstallerConfigurationBuilder overridingInstallerDirectory(String installerDirectory) {
            this.installerDirectory = installerDirectory;
            return this;
        }

        public InstallerConfiguration build() {
            InstallerConfiguration installerConfiguration = new InstallerConfiguration();
            installerConfiguration.testDriveUnlimitedDirectory = requireNonNull(testDriveUnlimitedDirectory, "TDU directory is required.");
            installerConfiguration.assetsDirectory = requireNonNull(assetsDirectory, "Assets directory is required.");
            installerConfiguration.bankSupport = requireNonNull(bankSupport, "Bank Support component is required.");
            installerConfiguration.cameraSupport = requireNonNull(cameraSupport, "Camera Support component is required.");
            installerConfiguration.installerDirectory = requireNonNull(installerDirectory, "Installer directory is required.");

            return installerConfiguration;
        }
    }
}
