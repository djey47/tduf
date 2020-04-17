package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.controllers.MainStageChangeDataController;
import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PluginHandlerTest {

    @Mock
    DatabasePlugin pluginInstanceMock;

    @Mock
    Pane parentPaneMock;

    private ObservableList<Node> parentPaneChildren = FXCollections.observableArrayList();
    private PluginHandler pluginHandler = new PluginHandler(TestingParent.testingInstance(), MainStageChangeDataController.testingInstance());

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(parentPaneMock.getChildren()).thenReturn(parentPaneChildren);
    }

    @AfterEach
    void tearDown() {
        parentPaneChildren.clear();
    }

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

    @Test
    void initializePluginInstance_shouldCallOnInit_andResetErrorOnNormalBehaviour() throws IOException {
        // given-when
        pluginHandler.initializePluginInstance(pluginInstanceMock, "TEST_PLUGIN");

        // then
        verify(pluginInstanceMock).onInit(eq(pluginHandler.getContext()));
        verify(pluginInstanceMock).setInitError(isNull());
    }

    @Test
    void initializePluginInstance_shouldCallOnInit_andSetErrorOnAbnormalBehaviour() throws IOException {
        // given
        IOException initException = new IOException("This is an init exception");
        doThrow(initException)
                .when(pluginInstanceMock).onInit(eq(pluginHandler.getContext()));

        // when
        pluginHandler.initializePluginInstance(pluginInstanceMock, "TEST_PLUGIN");

        // then
        verify(pluginInstanceMock).setInitError(eq(initException));
    }

    @Test
    void renderPluginInstance_whenNoInitError_shouldRenderWithPluginInstance() {
        // given-when
        pluginHandler.renderPluginInstance(pluginInstanceMock, "TEST_PLUGIN", parentPaneMock);

        // then
        verify(pluginInstanceMock).renderControls(eq(pluginHandler.getContext()));
        assertThat(parentPaneChildren).hasSize(1);
    }

    @Test
    void renderPluginInstance_whenInitError_shouldNotRenderWithPluginInstance() {
        // given
        when(pluginInstanceMock.getInitError()).thenReturn(of(new IOException()));

        // when
        pluginHandler.renderPluginInstance(pluginInstanceMock, "TEST_PLUGIN", parentPaneMock);

        // then
        verify(pluginInstanceMock, never()).renderControls(any(EditorContext.class));
        assertThat(parentPaneChildren).hasSize(1);
    }

    @Test
    void triggerOnSaveForPluginInstance_whenNoError() throws IOException {
        // given-when
        pluginHandler.triggerOnSaveForPluginInstance(pluginInstanceMock);

        // then
        verify(pluginInstanceMock).onSave(eq(pluginHandler.getContext()));
    }

    @Test
    void triggerOnSaveForPluginInstance_whenError_shouldEndNormally() throws IOException {
        // given
        doThrow(new IOException()).when(pluginInstanceMock).onSave(any(EditorContext.class));

        // when
        pluginHandler.triggerOnSaveForPluginInstance(pluginInstanceMock);

        // then
        verify(pluginInstanceMock).onSave(any(EditorContext.class));
    }

    private static class TestingParent extends Parent {
        private static Parent testingInstance() {
            return new TestingParent();
        }
    }
}
