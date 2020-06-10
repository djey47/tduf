package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.converter.ModifiedFlagToTitleConverter;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Paths;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageControllerTest extends ApplicationTest {
    @Mock
    private DatabaseLoader databaseLoaderMock;

    @Mock
    private MainStageViewDataController viewDataControllerMock;

    @Mock
    private MainStageServicesController servicesControllerMock;

    @Mock
    private Window windowMock;

    @Mock
    private WindowEvent eventMock;

    @Mock
    private Label statusLabelMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @Mock
    private StringProperty titlePropertyMock;

    @InjectMocks
    private MainStageController controller;
    private MainStageController spyController;

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(servicesControllerMock.runningServiceProperty()).thenReturn(new SimpleBooleanProperty(false));
        when(servicesControllerMock.getDatabaseLoader()).thenReturn(databaseLoaderMock);

        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(false);

        when(statusLabelMock.textProperty()).thenReturn(mock(StringProperty.class));

        when(databaseLoaderMock.bankSupportProperty()).thenReturn(new SimpleObjectProperty<>());
        when(databaseLoaderMock.databaseLocationProperty()).thenReturn(new SimpleStringProperty());

        // Spy instance to mock some JavaFx-tied calls
        spyController = spy(controller);
        spyController.modifiedProperty().setValue(false);
        spyController.statusLabel = statusLabelMock;
        doReturn(titlePropertyMock).when(spyController).titleProperty();
        doReturn(false).when(spyController).confirmLosingChanges(anyString());
        doReturn(windowMock).when(spyController).getWindow();
    }

    @AfterEach
    void tearDown() {
        Log.set(Log.LEVEL_INFO);
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
    void initAfterDatabaseLoading_whenPluginsEnabled_shouldInitPlugins() {
        // given
        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(true);
        when(applicationConfigurationMock.getGamePath()).thenReturn(of(Paths.get("/path/to/tdu")));
        when(servicesControllerMock.getDatabaseLoader()).thenReturn(databaseLoaderMock);
        when(databaseLoaderMock.databaseLocationProperty()).thenReturn(new SimpleStringProperty("/path/to/database"));
        EditorContext editorContext = new EditorContext();
        when(pluginHandlerMock.getEditorContext()).thenReturn(editorContext);

        // when
        spyController.initAfterDatabaseLoading();

        // then
        verify(pluginHandlerMock).initializeAllPlugins();
        assertThat(editorContext.getDatabaseLocation()).isEqualTo("/path/to/database");
        assertThat(editorContext.getGameLocation()).isEqualTo("/path/to/tdu");
        assertThat(editorContext.getMiner()).isNotNull();
        assertThat(editorContext.getMainWindow()).isSameAs(windowMock);
        assertThat(editorContext.getMainStageController()).isSameAs(spyController);
    }

    @Test
    void initAfterDatabaseLoading_whenPluginsDisabled_shouldNotInitPlugins() {
        // given-when
        spyController.initAfterDatabaseLoading();

        // then
        verifyNoInteractions(pluginHandlerMock);
    }

    @Test
    void initAfterDatabaseLoading_shouldUpdateDisplay_andResetModifiedFlag() {
        // given
        spyController.modifiedProperty().setValue(true);

        // when
        spyController.initAfterDatabaseLoading();

        // then
        verify(viewDataControllerMock).updateDisplayWithLoadedObjects();
        verify(windowMock).addEventFilter(eq(WindowEvent.WINDOW_CLOSE_REQUEST), any());
        verify(titlePropertyMock).bindBidirectional(eq(spyController.modifiedProperty()), any(ModifiedFlagToTitleConverter.class));
        assertThat(spyController.getMiner()).isNotNull();
        assertThat(spyController.modifiedProperty().getValue()).isFalse();
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
        when(servicesControllerMock.runningServiceProperty()).thenReturn(new SimpleBooleanProperty(true));

        // when
        controller.loadDatabaseFromDirectory("/path/to/database");

        // then
        verifyNoInteractions(statusLabelMock);
        verifyNoInteractions(databaseLoaderMock);
    }

    @Test
    void loadDatabaseFromDirectory_whenModifiedFlagSetToTrue_andConfirmLosingChanges_shouldBindStatus_andInvokeLoaderService() {
        // given
        spyController.modifiedProperty().setValue(true);
        doReturn(true).when(spyController).confirmLosingChanges(anyString());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        assertThat(databaseLoaderMock.bankSupportProperty().getValue()).isNotNull();
        assertThat(databaseLoaderMock.databaseLocationProperty().getValue()).isEqualTo("/path/to/database");
        verify(statusLabelMock.textProperty()).unbind();
        verify(statusLabelMock.textProperty()).bind(same(databaseLoaderMock.messageProperty()));
        verify(databaseLoaderMock).restart();
    }

    @Test
    void loadDatabaseFromDirectory_whenModifiedFlagSetToTrue_andLosingChangesRefused_shouldDoNothing() {
        // given
        spyController.modifiedProperty().setValue(true);
        doReturn(false).when(spyController).confirmLosingChanges(anyString());

        // when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verifyNoInteractions(statusLabelMock);
        verifyNoInteractions(databaseLoaderMock);
    }

    @Test
    void loadDatabaseFromDirectory_whenModifiedFlagSetToFalse_shouldInvokeLoaderService() {
        // given-when
        spyController.loadDatabaseFromDirectory("/path/to/database");

        // then
        verify(databaseLoaderMock).restart();
    }
}
