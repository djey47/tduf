package fr.tduf.gui.database.plugins.cameras;

import fr.tduf.gui.database.plugins.common.EditorContext;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CamerasPluginTest {
    private final CamerasPlugin camerasPlugin = new CamerasPlugin();
    private final EditorContext context = new EditorContext();

    @Test
    void onInit_whenNoCameras_shouldNotAttemptLoading() throws IOException {
        // given
        context.setDatabaseLocation(".");

        // when
        camerasPlugin.onInit(context);

        // then
        assertThat(context.getCamerasContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoCamerasLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        camerasPlugin.onSave(context);
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() throws IOException {
        // given
        context.getCamerasContext().setPluginLoaded(false);

        // when
        Node actualNode = camerasPlugin.renderControls(context);

        // then
        assertThat(actualNode).isInstanceOf(HBox.class);
        assertThat(((HBox) actualNode).getChildren()).isEmpty();
    }
}
