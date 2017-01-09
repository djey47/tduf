package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.plugins.common.PluginContext;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.gui.database.services.DatabaseLoader;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MainStageControllerTest {
    @Mock
    private DatabaseLoader databaseLoaderMock;

    @Mock
    private PluginHandler pluginHandlerMock;

    @Mock
    private MainStageViewDataController viewDataControllerMock;

    @InjectMocks
    private MainStageController controller;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenNoLoadedObjects_shouldDoNothing() {
        // given
        when(databaseLoaderMock.fetchValue()).thenReturn(new ArrayList<>(0));

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isEmpty();
        verifyZeroInteractions(pluginHandlerMock, viewDataControllerMock);
    }

    @Test
    void handleDatabaseLoaderSuccess_whenLoadedObjects_shouldReplaceObjects_andUpdateDisplay() {
        // given
        DbDto previousDatabaseObject = DbDto.builder().build();
        controller.getDatabaseObjects().add(previousDatabaseObject);
        List<DbDto> loadedObjects = singletonList(DbDto.builder().build());
        when(databaseLoaderMock.fetchValue()).thenReturn(loadedObjects);
        when(databaseLoaderMock.databaseLocationProperty()).thenReturn(new SimpleStringProperty("/db"));
        PluginContext pluginContext = new PluginContext();
        when(pluginHandlerMock.getContext()).thenReturn(pluginContext);

        // when
        controller.handleDatabaseLoaderSuccess();

        // then
        assertThat(controller.getDatabaseObjects()).isEqualTo(loadedObjects);
        assertThat(pluginContext.getDatabaseLocation()).isEqualTo("/db");
        verify(pluginHandlerMock).initializeAllPlugins();
        verify(viewDataControllerMock).updateDisplayWithLoadedObjects();
    }
}
