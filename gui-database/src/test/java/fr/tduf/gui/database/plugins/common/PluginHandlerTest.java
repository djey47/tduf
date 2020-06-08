package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.database.controllers.main.MainStageChangeDataController;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    @Mock
    private OnTheFlyContext onTheFlyContextMock;

    private final ObservableList<Node> parentPaneChildren = FXCollections.observableArrayList();
    private final PluginHandler pluginHandler = new PluginHandler(TestingParent.testingInstance(), MainStageChangeDataController.testingInstance());

    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(onTheFlyContextMock.getParentPane()).thenReturn(parentPaneMock);
        when(parentPaneMock.getChildren()).thenReturn(parentPaneChildren);
        when(pluginInstanceMock.getName()).thenReturn("UNIT TESTS PLUGIN");
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
        pluginHandler.renderPluginByName("foo", onTheFlyContextMock);
    }

    @Test
    void renderPluginByName_whenDefaultPlugin_shouldRenderAndAttach() {
        // given-when
        pluginHandler.renderPluginByName("NOPE", onTheFlyContextMock);

        // then
        assertThat(parentPaneChildren).hasSize(1);
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
        verify(pluginInstanceMock).onInit(eq("TEST_PLUGIN"), eq(pluginHandler.getEditorContext()));
        verify(pluginInstanceMock).setInitError(isNull());
    }

    @Test
    void initializePluginInstance_shouldCallOnInit_andSetErrorOnAbnormalBehaviour() throws IOException {
        // given
        IOException initException = new IOException("This is an init exception");
        doThrow(initException)
                .when(pluginInstanceMock).onInit(eq("TEST_PLUGIN"), eq(pluginHandler.getEditorContext()));

        // when
        pluginHandler.initializePluginInstance(pluginInstanceMock, "TEST_PLUGIN");

        // then
        verify(pluginInstanceMock).setInitError(eq(initException));
    }

    @Test
    void renderPluginInstance_whenNoInitError_shouldRenderWithPluginInstance() {
        // given-when
        pluginHandler.renderPluginInstance(pluginInstanceMock, onTheFlyContextMock);

        // then
        verify(pluginInstanceMock).renderControls(eq(onTheFlyContextMock));
        assertThat(parentPaneChildren).hasSize(1);
    }

    @Test
    void renderPluginInstance_whenInitError_shouldNotRenderWithPluginInstance() {
        // given
        IOException initError = new IOException();
        when(pluginInstanceMock.getInitError()).thenReturn(of(initError));

        // when
        pluginHandler.renderPluginInstance(pluginInstanceMock, onTheFlyContextMock);

        // then
        verify(pluginInstanceMock, never()).renderControls(any(OnTheFlyContext.class));
        assertThat(parentPaneChildren).hasSize(1);
    }

    @Test
    void renderPluginInstance_whenRenderError_shouldRenderErrorPlaceholder() {
        // given
        RuntimeException renderError = new RuntimeException();
        when(pluginInstanceMock.renderControls(any(OnTheFlyContext.class))).thenThrow(renderError);

        // when
        pluginHandler.renderPluginInstance(pluginInstanceMock, onTheFlyContextMock);

        // then
        assertThat(parentPaneChildren).hasSize(1);
    }

    @Test
    void triggerOnSaveForPluginInstance_whenNoError_shouldClearPreviousError() throws IOException {
        // given-when
        pluginHandler.triggerOnSaveForPluginInstance(pluginInstanceMock);

        // then
        verify(pluginInstanceMock).onSave();
        verify(pluginInstanceMock).setSaveError(isNull());
    }

    @Test
    void triggerOnSaveForPluginInstance_whenError_shouldTriggerError_andRethrowException() throws IOException {
        // given
        IOException saveError = new IOException("This is a save error");
        doThrow(saveError).when(pluginInstanceMock).onSave();

        // when-then
        IOException actualError = assertThrows(IOException.class,
                () -> pluginHandler.triggerOnSaveForPluginInstance(pluginInstanceMock));
        assertThat(actualError).isSameAs(saveError);
        verify(pluginInstanceMock).onSave();
        verify(pluginInstanceMock).setSaveError(eq(saveError));
    }

    @Test
    void initializeOnTheFlyContextForPlugin_whenExistingPlugin_shouldReturnNewContextInstance() {
        // given-when
        OnTheFlyContext actualContext1 = PluginHandler.initializeOnTheFlyContextForPlugin("NOPE");
        OnTheFlyContext actualContext2 = PluginHandler.initializeOnTheFlyContextForPlugin("NOPE");

        // then
        assertThat(actualContext1).isNotSameAs(actualContext2);
    }

    @Test
    void initializeOnTheFlyContextForPlugin_whenNonExistingPlugin_shouldNotThrowException() {
        // given-when
        OnTheFlyContext actualContext = PluginHandler.initializeOnTheFlyContextForPlugin("NONE");

        // then
        assertThat(actualContext).isNotNull();
    }

    private static class TestingParent extends Parent {
        private static Parent testingInstance() {
            return new TestingParent();
        }
    }
}
