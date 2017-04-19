package fr.tduf.gui.launcher;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.launcher.controllers.MainStageController;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;

import static javafx.event.Event.ANY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

class LauncherTest {
    @Mock
    private MainStageController mainStageControllerMock;
    
    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void onExitHandler_whenNoMainController_shouldThrowException() throws IOException {
        // given
        WindowEvent event = new WindowEvent(null, ANY);

        // when-then
        assertThrows(NullPointerException.class,
                () -> new Launcher().onExitHandler().handle(event));
    }

    @Test
    void onExitHandler_shouldInvokeController_toSaveConfiguration() throws IOException {
        // given
        AbstractGuiApp.setMainController(mainStageControllerMock);
        WindowEvent event = new WindowEvent(null, ANY);

        // when
        new Launcher().onExitHandler().handle(event);
        
        // then
        verify(mainStageControllerMock).saveConfiguration();
    }
}
