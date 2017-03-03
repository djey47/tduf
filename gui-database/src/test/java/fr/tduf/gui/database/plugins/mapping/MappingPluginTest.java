package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.plugins.common.EditorContext;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MappingPluginTest {
    private final MappingPlugin mappingPlugin = new MappingPlugin();
    private final EditorContext context = new EditorContext();

    @Test
    void onInit_whenNoMapping_shouldNotAttemptLoading() throws IOException {
        // given
        context.setGameLocation(".");

        // when
        mappingPlugin.onInit(context);

        // then
        assertThat(context.getMappingContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoCamerasLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        mappingPlugin.onSave(context);
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() throws IOException {
        // given
        context.getMappingContext().setPluginLoaded(false);

        // when
        Node actualNode = mappingPlugin.renderControls(context);

        // then
        assertThat(actualNode).isInstanceOf(HBox.class);
        assertThat(((HBox) actualNode).getChildren()).isEmpty();
    }
}