package fr.tduf.gui.database.plugins.common;

import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PluginHandlerTest {
    private PluginHandler pluginHandler = new PluginHandler();

    @Test
    void renderPluginByName_whenUnknownPlugin_shouldThrowException() {
        // given-when-then
        assertThrows(IllegalArgumentException.class,
                () -> pluginHandler.renderPluginByName("foo", new HBox()));
    }

    @Test
    void renderPluginByName_whenDefaultPlugin_shouldRenderAndAttach() {
        // given
        HBox parentNode = new HBox();

        // when
        pluginHandler.renderPluginByName("NOPE", parentNode);

        // then
        assertThat(parentNode.getChildren()).hasSize(1);
    }
}
