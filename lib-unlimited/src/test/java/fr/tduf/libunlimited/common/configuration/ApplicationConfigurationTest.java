package fr.tduf.libunlimited.common.configuration;

import fr.tduf.libtesting.common.helper.FilesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigurationTest {
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    private String configFileName;
    private String genuineConfigFileName;

    @BeforeEach
    void setUp() throws IOException {
        configFileName = Paths.get(FilesHelper.createTempDirectoryForLibrary(), ".tduf", "test.properties").toString();
        genuineConfigFileName = Paths.get(FilesHelper.createTempDirectoryForLibrary(), "test.properties").toString();
        ApplicationConfiguration.setConfigurationFile(configFileName);
        ApplicationConfiguration.setGenuineConfigurationFile(genuineConfigFileName);
    }

    @Test
    void store_shouldCreatePropertiesFile() throws Exception {
        // GIVEN-WHEN
        applicationConfiguration.store();

        // THEN
        assertThat(new File(configFileName)).exists();
    }

    @Test
    void load_whenNoConfigFile_shouldCreateIt() throws Exception {
        // GIVEN-WHEN
        applicationConfiguration.load();

        // THEN
        assertThat(new File(configFileName)).exists();
    }

    @Test
    void load_whenConfigFile() throws Exception {
        // GIVEN
        Path configFilePath = Paths.get(configFileName);
        Files.createDirectories(configFilePath.getParent());
        Files.createFile(configFilePath);

        // WHEN-THEN
        applicationConfiguration.load();
    }

    @Test
    void load_whenConfigFileAtGenuineLocation_shouldCreateAtNewLocation_andDeleteOriginalFile() throws Exception {
        // GIVEN
        Files.createFile(Paths.get(genuineConfigFileName));

        // WHEN
        applicationConfiguration.load();

        // THEN
        assertThat(new File(configFileName)).exists();
        assertThat(new File(genuineConfigFileName)).doesNotExist();
    }

    @Test
    void reset_shouldClearSettings_andUpdateFile() throws IOException {
        // GIVEN
        applicationConfiguration.setProperty("key", "value");

        // WHEN
        applicationConfiguration.reset();

        // THEN
        assertThat(new File(configFileName)).exists();
        applicationConfiguration.load();
        assertThat(applicationConfiguration).isEmpty();
    }
    
    @Test
    void isEditorPluginsEnabled_whenNoSetting_shouldReturnTrue() {
        // given-when-then
        assertThat(applicationConfiguration.isEditorPluginsEnabled()).isTrue();
    }    
    
    @Test
    void isEditorPluginsEnabled_whenSettingToFalse_shouldReturnFalse() {
        // given
        applicationConfiguration.setProperty("tduf.editor.plugins.enabled", "false");
        
        // when-then
        assertThat(applicationConfiguration.isEditorPluginsEnabled()).isFalse();
    }    
    
    @Test
    void isEditorPluginsEnabled_whenSettingToTrue_shouldReturnTrue() {
        // given
        applicationConfiguration.setProperty("tduf.editor.plugins.enabled", "true");
        
        // when-then
        assertThat(applicationConfiguration.isEditorPluginsEnabled()).isTrue();
    }

    @Test
    void isEditorDebuggingEnabled_whenNoSetting_shouldReturnFalse() {
        // given-when-then
        assertThat(applicationConfiguration.isEditorDebuggingEnabled()).isFalse();
    }

    @Test
    void isEditorDebuggingEnabled_whenSettingToFalse_shouldReturnFalse() {
        // given
        applicationConfiguration.setProperty("tduf.editor.debugging.enabled", "false");

        // when-then
        assertThat(applicationConfiguration.isEditorDebuggingEnabled()).isFalse();
    }

    @Test
    void isEditorDebuggingEnabled_whenSettingToTrue_shouldReturnTrue() {
        // given
        applicationConfiguration.setProperty("tduf.editor.debugging.enabled", "true");

        // when-then
        assertThat(applicationConfiguration.isEditorDebuggingEnabled()).isTrue();
    }
}
