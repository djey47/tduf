package fr.tduf.gui.common.javafx.helper;

import fr.tduf.gui.common.javafx.helper.options.FileBrowsingOptions;
import fr.tduf.gui.common.javafx.helper.options.SimpleDialogOptions;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Interactive testing - can't be asserted automatically")
class CommonDialogsHelperTest extends ApplicationTest {
    @Override
    public void start(Stage stage) {}

    @Test
    void alertDialog() {
        // GIVEN
        final SimpleDialogOptions dialogOptions = SimpleDialogOptions.builder()
                .withContext(Alert.AlertType.INFORMATION)
                .withTitle("Testing alert dialog box")
                .withMessage("This is a message")
                .withDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
                .build();

        // WHEN
        interact(() -> CommonDialogsHelper.showDialog(dialogOptions, null));
    }

    @Test
    void inputValueDialog() {
        // GIVEN
        final String prompt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit,";

        // WHEN-THEN
        interact(() -> {
            final Optional<String> actualInput = CommonDialogsHelper.showInputValueDialog("Enter sed then click OK", prompt, null);
            assertThat(actualInput).contains("sed");
        });
    }

    @Test
    void browseForFilename_whenSaveMode_andExtensionFilters() {
        // GIVEN
        List<FileChooser.ExtensionFilter> filters = asList(
                new FileChooser.ExtensionFilter("Filter 1", "*.txt"),
                new FileChooser.ExtensionFilter("Filter 2", "*"));
        FileBrowsingOptions options = FileBrowsingOptions.builder()
                .forSaving()
                .withExtensionFilters(filters)
                .withInitialDirectory(".")
                .withDialogTitle("Enter toto.txt")
                .build();

        // WHEN-THEN
        interact(() -> {
            Optional<File> actualFile = CommonDialogsHelper.browseForFilename(options, null);
            assertThat(actualFile).isPresent();
            assertThat(actualFile.get()).hasName("toto.txt");
        });
    }

    @Test
    void browseForFilename_whenLoadMode() {
        // GIVEN
        FileBrowsingOptions options = FileBrowsingOptions.builder()
                .forLoading()
                .build();

        // WHEN-THEN
        interact(() -> CommonDialogsHelper.browseForFilename(options, null));
    }
}
