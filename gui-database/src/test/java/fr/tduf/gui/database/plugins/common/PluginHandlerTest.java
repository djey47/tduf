package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginHandlerTest {

    private PluginHandler pluginHandler = new PluginHandler(TestingParent.testingInstance(), MainStageChangeDataController.testingInstance());

    @BeforeEach
    void setUp() {}

    @Test
    void new_shouldRetrieveStylesheets() {
        // GIVEN
        Parent testingParent = TestingParent.testingInstance();

        // WHEN
        new PluginHandler(testingParent, MainStageChangeDataController.testingInstance());

        // THEN
        assertThat(testingParent.getStylesheets()).isNotEmpty();
    }

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

    @Test
    void fetchCss_whenResourceExists_shouldReturnCorrectCss() {
        // given
        String resourcePath = "/css/example.css";

        // when
        String actualCss = PluginHandler.fetchCss(resourcePath);

        // then
        assertThat(actualCss)
                .startsWith("file:/")
                .endsWith("/css/example.css");
    }

    @Test
    void fetchCss_whenResourceDoesNotExist_shouldThrowException() {
        // given
        String resourcePath = "/css/fail.css";

        // when-then
        assertThrows(NullPointerException.class,
                () -> PluginHandler.fetchCss(resourcePath));
    }

    private static class TestingParent extends Parent {
        private static Parent testingInstance() {
            return new TestingParent();
        }
    }
}
