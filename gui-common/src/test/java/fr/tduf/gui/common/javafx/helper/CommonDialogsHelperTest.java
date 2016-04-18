package fr.tduf.gui.common.javafx.helper;

import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.scene.control.Alert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class CommonDialogsHelperTest {
    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test
    public void alertDialog() {
        // GIVEN
        final String description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        // WHEN
        CommonDialogsHelper.showDialog(Alert.AlertType.WARNING, "title", "message", description);
    }

    @Test
    public void inputValueDialog() {
        // GIVEN
        final String prompt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit,";

        // WHEN
        final Optional<String> actualInput = CommonDialogsHelper.showInputValueDialog("Enter sed then click OK", prompt);

        // THEN
        assertThat(actualInput).contains("sed");
    }

    @Test
    public void browseForFilename_loadMode() {
        // GIVEN


        // WHEN
        CommonDialogsHelper.browseForFilename(true, null);
    }

    @Test
    public void browseForFilename_saveMode() {
        // GIVEN


        // WHEN
        CommonDialogsHelper.browseForFilename(false, null);
    }
}
