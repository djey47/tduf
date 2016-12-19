package fr.tduf.libunlimited.common.configuration;

import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigurationTest {
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    private String configFileName;
    private String genuineConfigFileName;

    @Before
    public void setUp() throws IOException {
        configFileName = Paths.get(FilesHelper.createTempDirectoryForLibrary(), ".tduf", "test.properties").toString();
        genuineConfigFileName = Paths.get(FilesHelper.createTempDirectoryForLibrary(), "test.properties").toString();
        ApplicationConfiguration.setConfigurationFile(configFileName);
        ApplicationConfiguration.setGenuineConfigurationFile(genuineConfigFileName);
    }

    @Test
    public void store_shouldCreatePropertiesFile() throws Exception {
        // GIVEN-WHEN
        applicationConfiguration.store();

        // THEN
        assertThat(new File(configFileName)).exists();
    }

    @Test
    public void load_whenNoConfigFile_shouldCreateIt() throws Exception {
        // GIVEN-WHEN
        applicationConfiguration.load();

        // THEN
        assertThat(new File(configFileName)).exists();
    }

    @Test
    public void load_whenConfigFile() throws Exception {
        // GIVEN
        Path configFilePath = Paths.get(configFileName);
        Files.createDirectories(configFilePath.getParent());
        Files.createFile(configFilePath);

        // WHEN-THEN
        applicationConfiguration.load();
    }

    @Test
    public void load_whenConfigFileAtGenuineLocation_shouldCreateAtNewLocation_andDeleteOriginalFile() throws Exception {
        // GIVEN
        Files.createFile(Paths.get(genuineConfigFileName));

        // WHEN
        applicationConfiguration.load();

        // THEN
        assertThat(new File(configFileName)).exists();
        assertThat(new File(genuineConfigFileName)).doesNotExist();
    }

    @Test
    public void reset_shouldClearSettings_andUpdateFile() throws IOException {
        // GIVEN
        applicationConfiguration.setProperty("key", "value");

        // WHEN
        applicationConfiguration.reset();

        // THEN
        assertThat(new File(configFileName)).exists();
        applicationConfiguration.load();
        assertThat(applicationConfiguration).isEmpty();
    }
}
