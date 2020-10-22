package fr.tduf.gui.database.stages;

import fr.tduf.gui.common.javafx.application.AbstractGuiApp;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AbstractEditorDesignerTest {
    private final TestEditorDesigner editorDesigner = new TestEditorDesigner();

    @Mock
    private DatabaseEditor databaseEditorMock;

    @Mock
    private ApplicationConfiguration applicationConfigurationMock;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        AbstractGuiApp.setTestInstance(databaseEditorMock);
        when(databaseEditorMock.getApplicationConfiguration()).thenReturn(applicationConfigurationMock);
    }

    @Test
    void initCommonCss_whenNoConfiguration_shouldUseDefaultColors() {
        // given
        Parent root = new AnchorPane();

        // when
        editorDesigner.init(root);

        // then
        assertThat(root.getStylesheets().stream()
                .map(css -> css.substring(css.lastIndexOf("/") - 4))
                .collect(toList())).containsExactly("/css/Common.css", "/css/ToolBars.css", "/css/Colors.css");
    }

    @Test
    void initCommonCss_whenCustomTheme_andCssFileExists_shouldReturnIt() throws IOException {
        // given
        Parent root = new AnchorPane();
        String tempDirectory = TestingFilesHelper.createTempDirectoryForDatabaseEditor();
        Path cssThemePath = Paths.get(tempDirectory, "css",  "theme-dark.css");
        Files.createDirectory(cssThemePath.getParent());
        Files.createFile(cssThemePath);
        when(applicationConfigurationMock.getEditorCustomThemeCss()).thenReturn(of(cssThemePath));

        // when
        editorDesigner.init(root);

        // then
        assertThat(root.getStylesheets().stream()
                .map(css -> css.substring(css.lastIndexOf("/") - 4))
                .collect(toList())).containsExactly("/css/Common.css", "/css/ToolBars.css", "/css/theme-dark.css");
    }

    @Test
    void initCommonCss_whenCustomTheme_andCssFileDoesNotExist_shouldUseDefaultColors() {
        // given
        Parent root = new AnchorPane();
        Path cssThemePath = Paths.get("/css",  "theme-dark.css");
        when(applicationConfigurationMock.getEditorCustomThemeCss()).thenReturn(of(cssThemePath));

        // when
        editorDesigner.init(root);

        // then
        assertThat(root.getStylesheets().stream()
                .map(css -> css.substring(css.lastIndexOf("/") - 4))
                .collect(toList())).containsExactly("/css/Common.css", "/css/ToolBars.css", "/css/Colors.css");
    }

    private static class TestEditorDesigner extends AbstractEditorDesigner {
        private void init(Parent root) {
            initCommonCss(root);
        }
    }
}
