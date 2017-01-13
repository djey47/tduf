package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluginHandlerTest {
    private PluginHandler pluginHandler = new PluginHandler(MainStageChangeDataController.testingInstance());

    @Test
    void renderPluginByName_whenUnknownPlugin_shouldNotThrowException() {
        // given-when-then
        pluginHandler.renderPluginByName("foo", new HBox());
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
