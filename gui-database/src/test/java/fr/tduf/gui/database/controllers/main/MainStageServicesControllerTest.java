package fr.tduf.gui.database.controllers.main;

import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.*;
import javafx.concurrent.Worker;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageServicesControllerTest {
    @Mock
    private DatabaseLoader databaseLoaderMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @Mock
    private Window windowMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @Mock
    private MainStageController mainStageControllerMock;

    @Mock
    private MainStageViewDataController viewDataControllerMock;

    @Mock
    private ReadOnlyStringProperty messagePropertyMock;

    @InjectMocks
    private MainStageServicesController controller;

    private final BooleanProperty modifiedProperty = new SimpleBooleanProperty(false);

    private final BooleanProperty runningPropertyMock = new SimpleBooleanProperty(false);

    private final ObjectProperty<Worker.State> statePropertyMock = new SimpleObjectProperty<>();

    @BeforeEach
    void setUp() {
        initMocks(this);

        controller.setDatabaseLoader(databaseLoaderMock);

        when(applicationConfigurationMock.isEditorPluginsEnabled()).thenReturn(false);

        when(mainStageControllerMock.modifiedProperty()).thenReturn(modifiedProperty);
        when(mainStageControllerMock.getWindow()).thenReturn(windowMock);
        when(mainStageControllerMock.getPluginHandler()).thenReturn(pluginHandlerMock);
        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(mainStageControllerMock.getViewData()).thenReturn(viewDataControllerMock);

        when(databaseLoaderMock.fetchValue()).thenReturn(new ArrayList<>(0));
        when(databaseLoaderMock.messageProperty()).thenReturn(messagePropertyMock);
        when(databaseLoaderMock.runningProperty()).thenReturn(runningPropertyMock);
        when(databaseLoaderMock.stateProperty()).thenReturn(statePropertyMock);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenNoLoadedObjects_shouldDoNothing() {
        // given
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(singletonList(DbDto.builder().build()));

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isNotEmpty();
        verify(mainStageControllerMock, never()).initAfterDatabaseLoading();
    }

    @Test
    void handleDatabaseLoaderSuccess_whenLoadedObjects_andPluginsEnabled_shouldReplaceObjects_andInvokeMainController() {
        // given
        when(databaseLoaderMock.fetchValue()).thenReturn(singletonList(DbDto.builder().build()));
        when(mainStageControllerMock.getDatabaseObjects()).thenReturn(new ArrayList<>(0));

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isEqualTo(databaseLoaderMock.fetchValue());
        verify(mainStageControllerMock).initAfterDatabaseLoading();
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
        // given-when
        controller.handleDatabaseSaverSuccess();

        // then
        verifyNoInteractions(pluginHandlerMock);
    }

    @Test
    void initServicePropertiesAndListeners() {
        // given-when-then
        controller.initServicePropertiesAndListeners();
    }

    @Test
    void getLoaderStateChangeListenerCallback_whenRunning_shouldToggleSplashON() {
        // when
        controller.getLoaderStateChangeListener().changed(null, null, Worker.State.RUNNING);

        // then
        verify(viewDataControllerMock).toggleSplashImage(true);
    }

    @Test
    void getLoaderStateChangeListenerCallback_whenSuccess_shouldToggleSplashOFF() {
        // when
        controller.getLoaderStateChangeListener().changed(null, null, Worker.State.SUCCEEDED);

        // then
        verify(viewDataControllerMock).toggleSplashImage(false);
    }

    @Test
    void getLoaderStateChangeListenerCallback_whenFailure_shouldToggleSplashOFF() {
        // when
        controller.getLoaderStateChangeListener().changed(null, null, Worker.State.FAILED);

        // then
        verify(viewDataControllerMock).toggleSplashImage(false);
    }
}
