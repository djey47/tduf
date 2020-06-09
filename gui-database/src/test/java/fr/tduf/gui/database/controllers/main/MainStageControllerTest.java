package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
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

    @Mock
    private Window windowMock;

    @Mock
    private WindowEvent eventMock;

    @Mock
    private Label statusLabel;

    @Mock
    private ReadOnlyStringProperty messagePropertyMock;

    @InjectMocks
    private MainStageController controller;
    // Controller variant, used to spy JavaFX-related methods
    private MainStageController spyController;

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @BeforeEach
    void setUp() {
        initMocks(this);

        spyController = spy(controller);
        doReturn(new SimpleStringProperty()).when(spyController).titleProperty();
        doReturn(windowMock).when(spyController).getWindow();
        doReturn(false).when(spyController).confirmLosingChanges(anyString());
        spyController.modifiedProperty().setValue(false);

        when(databaseLoaderMock.messageProperty()).thenReturn(messagePropertyMock);
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
        verifyNoInteractions(pluginHandlerMock, viewDataControllerMock, windowMock);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenLoadedObjects_andPluginsEnabled_shouldReplaceObjects_andUpdateDisplay_andResetModifiedFlagWithTitleBinding() {
        // given
        DbDto previousDatabaseObject = DbDto.builder().build();
        spyController.getDatabaseObjects().add(previousDatabaseObject);
        spyController.modifiedProperty().setValue(true);
        List<DbDto> loadedObjects = singletonList(DbDto.builder().build());
        when(databaseLoaderMock.fetchValue()).thenReturn(loadedObjects);
        when(databaseLoaderMock.databaseLocationProperty()).thenReturn(new SimpleStringProperty("/db"));
        EditorContext pluginContext = new EditorContext();
        when(pluginHandlerMock.getEditorContext()).thenReturn(pluginContext);
        when(applicationConfigurationMock.getGamePath()).thenReturn(of(Paths.get("/tdu")));
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);

        // when
        spyController.handleDatabaseLoaderSuccess();

        // then
        assertThat(spyController.modifiedProperty().getValue()).isFalse();
        assertThat(spyController.titleProperty().getValue()).isEqualTo("TDUF Database Editor ");
        assertThat(spyController.getDatabaseObjects()).isEqualTo(loadedObjects);
        assertThat(pluginContext.getDatabaseLocation()).isEqualTo("/db");
        assertThat(pluginContext.getGameLocation()).isEqualTo("/tdu");
        verify(pluginHandlerMock).initializeAllPlugins();
        verify(viewDataControllerMock).updateDisplayWithLoadedObjects();
        verify(windowMock).addEventFilter(eq(WINDOW_CLOSE_REQUEST), any());
    }

    @Test
    void handleDatabaseSaverSuccess_shouldResetModifiedFlag() {
        // given
        controller.modifiedProperty().setValue(true);

        // when
        controller.handleDatabaseSaverSuccess();

        // then
        assertThat(controller.modifiedProperty().getValue()).isFalse();
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
    void handleDatabaseSaverSuccess_whenPluginsDisabled_shouldNotInvokePluginHandler() {
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

    @Test
    void handleApplicationExit_whenModifiedFlagSetToFalse_shouldNotConfirm_norConsumeEvent() {
        // given-when
        spyController.handleApplicationExit(eventMock);

        // then
        verify(spyController, never()).confirmLosingChanges(anyString());
        verify(eventMock, never()).consume();
    }

    @Test
    void handleApplicationExit_whenModifiedFlagSetToTrue_andLosingChangesRefused_shouldConsumeEvent() {
        // given
        spyController.modifiedProperty().setValue(true);

        // when
        spyController.handleApplicationExit(eventMock);

        // then
        verify(spyController).confirmLosingChanges(" : Leaving");
        verify(eventMock).consume();
    }

    @Test
    void handleApplicationExit_whenModifiedFlagSetToTrue_andConfirmLosingChanges_shouldNotConsumeEvent() {
        // given
        spyController.modifiedProperty().setValue(true);
        doReturn(true).when(spyController).confirmLosingChanges(anyString());

        // when
        spyController.handleApplicationExit(eventMock);

        // then
        verify(eventMock, never()).consume();
    }

    @Test
    void loadDatabaseFromDirectory_whenServiceAlreadyRunning_shouldDoNothing() {
        // given
        spyController.runningServiceProperty().setValue(true);
        when(statusLabel.textProperty()).thenReturn(new SimpleStringProperty());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verifyNoInteractions(statusLabel);
        verifyNoInteractions(databaseLoaderMock);
    }

    @Test
    @Disabled("Bug because of binding???")
    void loadDatabaseFromDirectory_whenModifiedFlagSetToTrue_andConfirmLosingChanges_shouldBindStatus_andInvokeLoaderService() {
        // given
        spyController.modifiedProperty().setValue(true);
        doReturn(true).when(spyController).confirmLosingChanges(anyString());
        when(statusLabel.textProperty()).thenReturn(new SimpleStringProperty());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verify(statusLabel).textProperty();
        verify(databaseLoaderMock).bankSupportProperty();
        verify(databaseLoaderMock).databaseLocationProperty();
        verify(databaseLoaderMock).restart();
    }

    @Test
    void loadDatabaseFromDirectory_whenModifiedFlagSetToTrue_andLosingChangesRefused_shouldDoNothing() {
        // given
        spyController.modifiedProperty().setValue(true);
        doReturn(false).when(spyController).confirmLosingChanges(anyString());
        when(statusLabel.textProperty()).thenReturn(new SimpleStringProperty());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verifyNoInteractions(statusLabel);
        verifyNoInteractions(databaseLoaderMock);
    }

    @Test
    @Disabled("Bug because of binding???")
    void loadDatabaseFromDirectory_whenModifiedFlagSetToFalse_shouldBindStatus_andInvokeLoaderService() {
        // given
        when(statusLabel.textProperty()).thenReturn(new SimpleStringProperty());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verify(statusLabel).textProperty();
        verify(databaseLoaderMock).bankSupportProperty();
        verify(databaseLoaderMock).databaseLocationProperty();
        verify(databaseLoaderMock).restart();
    }
}
