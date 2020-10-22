package fr.tduf.libunlimited.common.configuration;

import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigurationTest {
    private static final String TDU_ROOT_DIR = "/home/user/apps/tdu";

    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    private String configFileName;

    @BeforeEach
    void setUp() throws IOException {
        configFileName = Paths.get(TestingFilesHelper.createTempDirectoryForLibrary(), ".tduf", "test.properties").toString();
        ApplicationConfiguration.setConfigurationFile(configFileName);
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

    @Test
    void getGamePath_whenNoSetting_shouldReturnEmpty() {
        // given-when-then
        assertThat(applicationConfiguration.getGamePath()).isEmpty();
    }

    @Test
    void getGamePath_whenSettingPresent_shouldReturnIt() {
        // given
        applicationConfiguration.setProperty("tdu.root.directory", TDU_ROOT_DIR);

        // when
        Optional<Path> actualGamePath = applicationConfiguration.getGamePath();

        //then
        assertThat(actualGamePath).contains(Paths.get(TDU_ROOT_DIR));
    }

    @Test
    void getDatabasePath_whenNoSetting_shouldReturnEmpty() {
        // given-when-then
        assertThat(applicationConfiguration.getDatabasePath()).isEmpty();
    }

    @Test
    void getDatabasePath_whenGameRootSettingPresent_shouldReturnComputed() {
        // given
        applicationConfiguration.setProperty("tdu.root.directory", TDU_ROOT_DIR);

        // when
        Optional<Path> actualDatabasePath = applicationConfiguration.getDatabasePath();

        //then
        Path expectedDatabasePath = Paths.get(TDU_ROOT_DIR).resolve("Euro/Bnk/Database");
        assertThat(actualDatabasePath).contains(expectedDatabasePath);
    }

    @Test
    void getDatabasePath_whenAllSettingsPresent_shouldReturnDatabaseDirectory() {
        // given
        String customDatabaseDir = "/home/user/apps/tdu-test/Euro/Bnk/Database";
        applicationConfiguration.setProperty("tdu.root.directory", TDU_ROOT_DIR);
        applicationConfiguration.setProperty("tdu.database.directory", customDatabaseDir);

        // when
        Optional<Path> actualDatabasePath = applicationConfiguration.getDatabasePath();

        //then
        Path expectedDatabasePath = Paths.get(customDatabaseDir);
        assertThat(actualDatabasePath).contains(expectedDatabasePath);
    }

    @Test
    void getEditorCustomThemeCss_whenPropertyDoesNotExist_shouldReturnEmpty() {
        // given-when
        Optional<Path> actualPath = applicationConfiguration.getEditorCustomThemeCss();

        // then
        assertThat(actualPath).isEmpty();
    }

    @Test
    void getEditorCustomThemeCss_whenPropertyExists_shouldReturnCorrectPath() {
        // given
        applicationConfiguration.setProperty("tduf.editor.theme", "clear");

        // when
        Optional<Path> actualPath = applicationConfiguration.getEditorCustomThemeCss();

        // then
        assertThat(actualPath).isPresent();
        Path actual = actualPath.get();
        assertThat(actual.getParent().toString()).endsWith(".tduf");
        assertThat(actual.toString()).endsWith("theme-clear.css");
    }

    @Test
    void keys_shouldReturnKeysSortedAlphabetically() {
        // given
        applicationConfiguration.setProperty("f", "value");
        applicationConfiguration.setProperty("e", "value");
        applicationConfiguration.setProperty("d", "value");
        applicationConfiguration.setProperty("c", "value");
        applicationConfiguration.setProperty("b", "value");
        applicationConfiguration.setProperty("a", "value");

        // when
        Enumeration<Object> actualKeys = applicationConfiguration.keys();

        // then
        assertThat(Collections.list(actualKeys)).containsExactly("a", "b", "c", "d", "e" ,"f");
    }

}
