package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


public class RestoreDatabaseStepTest {

    @Test
    public void perform_shouldRestoreDatabaseFiles_andClearCache() throws Exception {
        // GIVEN
        final String tduTempDirectory = FilesHelper.createTempDirectoryForInstaller();
        final Path tduDatabasePath = FilesHelper.getTduDatabasePath(tduTempDirectory);
        Files.createDirectories(tduDatabasePath);

        Path backupDatabasePath = Paths.get(tduTempDirectory, "backup", "database");
        Path databaseBackupFile = backupDatabasePath.resolve("DB.BNK");
        Files.createDirectories(backupDatabasePath);
        Files.createFile(databaseBackupFile);

        Path jsonCachePath = DatabaseBanksCacheHelper.resolveCachePath(tduDatabasePath);
        Files.createDirectories(jsonCachePath);

        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduTempDirectory)
                .build();
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(), "");
        databaseContext.setBackupDatabaseDirectory(backupDatabasePath.toString());
        GenericStep restoreDatabaseStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.RESTORE_DATABASE);


        // WHEN
        restoreDatabaseStep.perform();


        // THEN
        assertThat(tduDatabasePath.resolve("DB.BNK")).exists();
        assertThat(jsonCachePath).doesNotExist();
    }
}