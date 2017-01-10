package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.PluginContext;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CamerasPluginTest {
    private CamerasPlugin camerasPlugin = new CamerasPlugin();

    @Test
    void onInit_whenNoCameras_shouldNotAttemptLoading() throws IOException {
        // given
        PluginContext context = new PluginContext();
        context.setDatabaseLocation(".");

        // when
        camerasPlugin.onInit(context);

        // then
        assertThat(context.getCamerasContext().isPluginLoaded()).isFalse();
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() throws IOException {
        // given
        PluginContext context = new PluginContext();
        context.getCamerasContext().setPluginLoaded(false);

        // when
        Node actualNode = camerasPlugin.renderControls(context);

        // then
        assertThat(actualNode).isInstanceOf(HBox.class);
        assertThat(((HBox) actualNode).getChildren()).isEmpty();
    }
}
