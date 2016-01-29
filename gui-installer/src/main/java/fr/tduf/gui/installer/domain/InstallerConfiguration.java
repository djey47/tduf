package fr.tduf.gui.installer.domain;

import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import javafx.stage.Window;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * Class to embed all settings necessary to install/uninstall.
 */
public class InstallerConfiguration {

    private String testDriveUnlimitedDirectory;

    private String assetsDirectory;

    private BankSupport bankSupport;

    private Window mainWindow;

    private String effectiveVehicleSlot;

    private InstallerConfiguration() {}

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

    public String getTestDriveUnlimitedDirectory() {
        return testDriveUnlimitedDirectory;
    }

    public String getAssetsDirectory() {
        return assetsDirectory;
    }

    public BankSupport getBankSupport() {
        return bankSupport;
    }

    public void setEffectiveVehicleSlot(String effectiveVehicleSlot) {
        this.effectiveVehicleSlot = effectiveVehicleSlot;
    }

    public String getEffectiveVehicleSlot() {
        return effectiveVehicleSlot;
    }

    /**
     * @return builder, to create custom instances.
     */
    public static InstallerConfigurationBuilder builder() {
        return new InstallerConfigurationBuilder() {

            private Window mainWindow;

            private String testDriveUnlimitedDirectory;

            private String assetsDirectory = ".";

            private BankSupport bankSupport = new GenuineBnkGateway(new CommandLineHelper());

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
            public InstallerConfigurationBuilder usingBankSupport(BankSupport bankSupport) {
                this.bankSupport = bankSupport;
                return this;
            }

            @Override
            public InstallerConfigurationBuilder withMainWindow(Window window) {
                this.mainWindow = window;
                return this;
            }

            @Override
            public InstallerConfiguration build() {
                requireNonNull(testDriveUnlimitedDirectory, "TDU directory is required.");
                requireNonNull(assetsDirectory, "Assets directory is required.");
                requireNonNull(bankSupport, "Bank Support component is required.");

                InstallerConfiguration installerConfiguration = new InstallerConfiguration();
                installerConfiguration.testDriveUnlimitedDirectory = testDriveUnlimitedDirectory;
                installerConfiguration.assetsDirectory = assetsDirectory;
                installerConfiguration.bankSupport = bankSupport;
                installerConfiguration.mainWindow = mainWindow;

                return installerConfiguration;
            }
        };
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public interface InstallerConfigurationBuilder {
        InstallerConfigurationBuilder withTestDriveUnlimitedDirectory(String testDriveUnlimitedDirectory);

        InstallerConfigurationBuilder withAssetsDirectory(String testDriveUnlimitedDirectory);

        InstallerConfigurationBuilder usingBankSupport(BankSupport bankSupport);

        InstallerConfigurationBuilder withMainWindow(Window window);

        InstallerConfiguration build();
    }
}