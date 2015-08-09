package fr.tduf.gui.installer.domain;

import static java.util.Objects.requireNonNull;

/**
 * Class to embed all settings necessary to install/uninstall.
 */
public class InstallerConfiguration {

    private String testDriveUnlimitedDirectory;

    private InstallerConfiguration() {}

    public String getTestDriveUnlimitedDirectory() {
        return testDriveUnlimitedDirectory;
    }

    public static InstallerConfigurationBuilder builder() {
        return new InstallerConfigurationBuilder() {

            private String testDriveUnlimitedDirectory;

            @Override
            public InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory) {
                this.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;
                return this;
            }

            @Override
            public InstallerConfiguration build() {
                requireNonNull(testDriveUnlimitedDirectory, "TDU directory is required.");

                InstallerConfiguration installerConfiguration = new InstallerConfiguration();
                installerConfiguration.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;

                return installerConfiguration;

            }
        };
    }

    public interface InstallerConfigurationBuilder {
        InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory);

        InstallerConfiguration build();
    }
}