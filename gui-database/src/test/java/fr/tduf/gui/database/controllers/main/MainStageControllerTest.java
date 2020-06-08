package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.controllers.main.MainStageController;
import fr.tduf.gui.database.controllers.main.MainStageViewDataController;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageControllerTest extends ApplicationTest {
    @Mock
    private DatabaseLoader databaseLoaderMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @Mock
    private MainStageViewDataController viewDataControllerMock;
    
    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @InjectMocks
    private MainStageController controller;

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @AfterEach
    void tearDown() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenNoLoadedObjects_shouldDoNothing() {
        // given
        when(databaseLoaderMock.fetchValue()).thenReturn(new ArrayList<>(0));

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isEmpty();
        verifyNoInteractions(pluginHandlerMock, viewDataControllerMock);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenLoadedObjects_andPluginsEnabled_shouldReplaceObjects_andUpdateDisplay() {
        // given
        DbDto previousDatabaseObject = DbDto.builder().build();
        controller.getDatabaseObjects().add(previousDatabaseObject);
        List<DbDto> loadedObjects = singletonList(DbDto.builder().build());
        when(databaseLoaderMock.fetchValue()).thenReturn(loadedObjects);
        when(databaseLoaderMock.databaseLocationProperty()).thenReturn(new SimpleStringProperty("/db"));
        EditorContext pluginContext = new EditorContext();
        when(pluginHandlerMock.getEditorContext()).thenReturn(pluginContext);
        when(applicationConfigurationMock.getGamePath()).thenReturn(of(Paths.get("/tdu")));
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isEqualTo(loadedObjects);
        assertThat(pluginContext.getDatabaseLocation()).isEqualTo("/db");
        assertThat(pluginContext.getGameLocation()).isEqualTo("/tdu");
        verify(pluginHandlerMock).initializeAllPlugins();
        verify(viewDataControllerMock).updateDisplayWithLoadedObjects();
    }

    @Test
    void handleDatabaseSaverSuccess_whenPluginsEnabled_shouldInvokePluginHandler() {
        // given
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);

        // when
        controller.handleDatabaseSaverSuccess();

        // then
        verify(pluginHandlerMock).triggerOnSaveForAllPLugins();
    }

    @Test
    void handleDatabaseSaverSuccess_whenPluginsDisabled_shouldInvokePluginHandler() {
        // given
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(false);

        // when
        controller.handleDatabaseSaverSuccess();

        // then
        verifyNoInteractions(pluginHandlerMock);
    }

    @Test
    void initConfiguration_shouldLoadConfiguration_andKeepDefaultLogLevel() throws IOException {
        // given-when
        controller.initConfiguration();

        // then
        verify(applicationConfigurationMock).load();
        assertThat(Log.ERROR).isTrue();
    }

    @Test
    void initConfiguration_whenDebuggingModeEnabled_shouldLoadConfiguration_andApplyDebugLogLevel() throws IOException {
        // given
        when(applicationConfigurationMock.isEditorDebuggingEnabled()).thenReturn(true);

        // when
        controller.initConfiguration();

        // then
        verify(applicationConfigurationMock).load();
        assertThat(Log.ERROR).isTrue();
    }

    @Test
    void handleEntryFilterButtonAction_shouldInvokeViewDataController() {
        // given-when
        controller.handleEntryFilterButtonAction();

        // then
        verify(viewDataControllerMock).applyEntryFilter();
    }

    @Test
    void handleEmptyEntryFilterButtonAction_shouldInvokeViewDataController() {
        // given-when
        controller.handleEmptyEntryFilterButtonAction();

        // then
        verify(viewDataControllerMock).resetEntryFilter();
    }

    @Test
    void handleFilterTextFieldKeyPressed_whenNonEnterKey_shouldDoNothing() {
        // given-when
        controller.handleFilterTextFieldKeyPressed(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.COLON, false, false, false, false ));

        // then
        verifyNoInteractions(viewDataControllerMock);
    }

    @Test
    void handleFilterTextFieldKeyPressed_whenEnterKey_shouldInvokeViewDataController() {
        // given-when
        controller.handleFilterTextFieldKeyPressed(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false ));

        // then
        verify(viewDataControllerMock).applyEntryFilter();
    }
}
