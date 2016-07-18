package fr.tduf.libunlimited.common.configuration;

import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigurationTest {
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    private String configFileName;

    @Before
    public void setUp() throws IOException {
        configFileName = Paths.get(FilesHelper.createTempDirectoryForLibrary(), "test.properties").toString();
        ApplicationConfiguration.setConfigurationFile(configFileName);
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
}
