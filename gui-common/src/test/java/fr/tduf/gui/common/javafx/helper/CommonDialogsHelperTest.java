package fr.tduf.gui.common.javafx.helper;

import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Interactive testing - can't be asserted automatically
 */
@Ignore
public class CommonDialogsHelperTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test
    public void alertDialog() {
        // GIVEN
        final String description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        // WHEN
        CommonDialogsHelper.showDialog(Alert.AlertType.WARNING, "title", "message", description, null);
    }

    @Test
    public void inputValueDialog() {
        // GIVEN
        final String prompt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit,";

        // WHEN
        final Optional<String> actualInput = CommonDialogsHelper.showInputValueDialog("Enter sed then click OK", prompt, null);

        // THEN
        assertThat(actualInput).contains("sed");
    }

    @Test
    public void browseForFilename_whenSaveMode_andExtensionFilters() {
        // GIVEN
        List<FileChooser.ExtensionFilter> filters = asList(
                new FileChooser.ExtensionFilter("Filter 1", "*.txt"),
                new FileChooser.ExtensionFilter("Filter 2", "*"));
        CommonDialogsHelper.FileBrowsingOptions options = CommonDialogsHelper.FileBrowsingOptions.builder()
                .forSaving()
                .withExtensionFilters(filters)
                .withInitialDirectory(".")
                .withDialogTitle("Enter toto.txt")
                .build();

        // WHEN
        Optional<File> actualFile = CommonDialogsHelper.browseForFilename(options, null);

        // THEN
        assertThat(actualFile).isPresent();
        assertThat(actualFile.get()).hasName("toto.txt");
    }

    @Test
    public void browseForFilename_whenLoadMode() {
        // GIVEN
        CommonDialogsHelper.FileBrowsingOptions options = CommonDialogsHelper.FileBrowsingOptions.builder()
                .forLoading()
                .build();

        // WHEN-THEN
        CommonDialogsHelper.browseForFilename(options, null);
    }
}
