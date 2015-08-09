package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;

/**
 * Class to embed all settings necessary to install/uninstall.
 */
public class InstallerConfiguration {

    private String testDriveUnlimitedDirectory;

    private String assetsDirectory;

    private InstallerConfiguration() {}

    public String getTestDriveUnlimitedDirectory() {
        return testDriveUnlimitedDirectory;
    }

    public String getAssetsDirectory() {
        return assetsDirectory;
    }

    public static InstallerConfigurationBuilder builder() {
        return new InstallerConfigurationBuilder() {

            private String testDriveUnlimitedDirectory;

            private String assetsDirectory = ".";

            @Override
            public InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory) {
                this.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;
                return this;
            }

            @Override
            public InstallerConfigurationBuilder withAssetsDirectory(String assetsDirectory) {
                this.assetsDirectory = assetsDirectory;
                return this;
            }

            @Override
            public InstallerConfiguration build() {
                requireNonNull(testDriveUnlimitedDirectory, "TDU directory is required.");
                requireNonNull(assetsDirectory, "Assets directory is required.");

                InstallerConfiguration installerConfiguration = new InstallerConfiguration();
                installerConfiguration.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;
                installerConfiguration.assetsDirectory = assetsDirectory;

                return installerConfiguration;
            }
        };
    }

    public interface InstallerConfigurationBuilder {
        InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory);

        InstallerConfigurationBuilder withAssetsDirectory(String testDriveUnlimitedDirectory);

        InstallerConfiguration build();
    }
}