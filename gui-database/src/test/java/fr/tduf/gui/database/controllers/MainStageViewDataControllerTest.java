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
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());

        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();

        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenWrongCommandLineParameter_andNoConfiguration_shouldReturnEmpty() throws Exception {
        // GIVEN
        DatabaseEditor.getCommandLineParameters().add("-p");

        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.empty());


        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();


        // THEN
        assertThat(actualDirectory).isEmpty();
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenRightCommandLineParameter_shouldReturnLocation() throws Exception {
        // GIVEN
        DatabaseEditor.getCommandLineParameters().add("/tdu/euro/bnk/database");


        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();


        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");

        verifyZeroInteractions(mainStageControllerMock, applicationConfigurationMock);
    }

    @Test
    public void resolveInitialDatabaseDirectory_whenNoCommandLineParameter_andConfiguration_shouldReturnSavedLocation() throws Exception {
        // GIVEN
        when(mainStageControllerMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
        when(applicationConfigurationMock.getDatabasePath()).thenReturn(Optional.of(Paths.get("/tdu/euro/bnk/database")));


        // WHEN
        final Optional<String> actualDirectory = controller.resolveInitialDatabaseDirectory();


        // THEN
        assertThat(actualDirectory).contains("/tdu/euro/bnk/database");
    }
}
