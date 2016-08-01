package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MainStageViewDataControllerTest {
    @Mock
    private MainStageController mainStageControllerMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @InjectMocks
    private MainStageViewDataController controller;

    @Before
    public void setUp() {
        DatabaseEditor.getCommandLineParameters().clear();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmptyString() throws Exception {
        // GIVEN
        AtomicBoolean databaseAutoLoad = new AtomicBoolean(true);

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());


        // WHEN
        final String actualDirectory = controller.resolveInitialDatabaseDirectory(databaseAutoLoad);


        // THEN
        assertThat(actualDirectory).isEmpty();
        assertThat(databaseAutoLoad.get()).isFalse();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenWrongCommandLineParameter_andNoConfiguration_shouldReturnLocation() throws Exception {
        // GIVEN
        AtomicBoolean databaseAutoLoad = new AtomicBoolean(true);
        DatabaseEditor.getCommandLineParameters().add("-p");

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());


        // WHEN
        final String actualDirectory = controller.resolveInitialDatabaseDirectory(databaseAutoLoad);


        // THEN
        assertThat(actualDirectory).isEmpty();
        assertThat(databaseAutoLoad.get()).isFalse();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenRightCommandLineParameter_shouldReturnLocation() throws Exception {
        // GIVEN
        AtomicBoolean databaseAutoLoad = new AtomicBoolean(true);
        DatabaseEditor.getCommandLineParameters().add("/tdu/euro/bnk/database");


        // WHEN
        final String actualDirectory = controller.resolveInitialDatabaseDirectory(databaseAutoLoad);


        // THEN
        assertThat(actualDirectory).isEqualTo("/tdu/euro/bnk/database");
        assertThat(databaseAutoLoad.get()).isTrue();

        verifyZeroInteractions(mainStageControllerMock, applicationConfigurationMock);
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andConfiguration_shouldReturnSavedLocation() throws Exception {
        // GIVEN
        AtomicBoolean databaseAutoLoad = new AtomicBoolean(true);

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.of(Paths.get("/tdu/euro/bnk/database")));


        // WHEN
        final String actualDirectory = controller.resolveInitialDatabaseDirectory(databaseAutoLoad);


        // THEN
        assertThat(actualDirectory).isEqualTo("/tdu/euro/bnk/database");
        assertThat(databaseAutoLoad.get()).isTrue();
    }
}
