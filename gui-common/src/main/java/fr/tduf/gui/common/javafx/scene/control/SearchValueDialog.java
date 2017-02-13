package fr.tduf.gui.common.javafx.scene.control;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.function.Predicate;

import static fr.tduf.gui.common.DisplayConstants.*;
import static fr.tduf.gui.common.FxConstants.CSS_CLASS_ERROR_PATTERN_TEXT_FIELD;
import static fr.tduf.gui.common.FxConstants.PATH_RESOURCE_CSS_DIALOGS;

/**
 * Simple dialog allowing to enter a search pattern and perform search, then moving to next result.
 * Does not have any intelligence, must be provided via callbacks.
 */
public class SearchValueDialog  {
    private static final Class<SearchValueDialog> thisClass = SearchValueDialog.class;

    private final Dialog<Void> dialog;

    private TextField patternTextField = new TextField();

    private Predicate<String> next = p -> false;
    private Predicate<String> first = p -> false;

    private BooleanProperty errorProperty = new SimpleBooleanProperty(false);

    /**
     * @param title         : value to be displayed in title bar of tool window
     * @param headerText    : value to be displayed in dialog header
     */
    public SearchValueDialog(String title, String headerText) {
        dialog = new Dialog<>();

        initDialogSettings(title, headerText);

        initControls();
    }

    /**
     * Displays this dialog without modality
     * @param parentWindow  : window to host this dialog
     */
    public void show(Window parentWindow) {
        if(dialog.getOwner() == null) {
            dialog.initOwner(parentWindow);
        }
        dialog.show();
        Platform.runLater(patternTextField::requestFocus);
    }

    /**
     * Defines callbacks to use
     * @param first : action to perform when typing in pattern text field, or acting on first |< button
     * @param next  : action to perform when acting on next > button
     */
    public void setCallbacks(Predicate<String> first, Predicate<String> next) {
        this.first = first;
        this.next = next;
    }

    private void initDialogSettings(String title, String headerText) {
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(false);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        String dialogsCss = thisClass.getResource(PATH_RESOURCE_CSS_DIALOGS).toExternalForm();
        dialog.getDialogPane().getStylesheets().add(dialogsCss);
    }

    private void initControls() {

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        patternTextField.setPromptText(PLACEHOLDER_SEARCH_PATTERN);
        StringProperty patternValueProperty = patternTextField.textProperty();
        patternValueProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            boolean result = first.test(newValue);
            errorProperty.set(!result);
        });

        Button firstButton = new Button(LABEL_BUTTON_FIRST_RESULT);
        firstButton.setOnAction(event -> {
            String value = patternValueProperty.getValue();
            if (value.isEmpty()) {
                return;
            }
            boolean result = first.test(value);
            errorProperty.set(!result);
        });
        ControlHelper.setTooltipText(firstButton, LABEL_TOOLTIP_FIRST_RESULT);

        Button nextButton = new Button(LABEL_BUTTON_NEXT_RESULT);
        nextButton.setOnAction(event -> {
            String value = patternValueProperty.getValue();
            if (value.isEmpty()) {
                return;
            }
            boolean result = next.test(value);
            if (!result) {
                Log.debug("No more matches, back to first result...");
                result = first.test(value);
            }
            errorProperty.set(!result);
        });
        ControlHelper.setTooltipText(nextButton, LABEL_TOOLTIP_NEXT_RESULT);

        grid.add(patternTextField, 0, 0);
        grid.add(firstButton, 1, 0);
        grid.add(nextButton, 2, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType(LABEL_BUTTON_CLOSE, ButtonBar.ButtonData.CANCEL_CLOSE));

        errorProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue == newValue) {
                return;
            }
            // TODO use pseudo classes
            if (newValue) {
                patternTextField.getStyleClass().add(CSS_CLASS_ERROR_PATTERN_TEXT_FIELD);
            } else {
                patternTextField.getStyleClass().remove(CSS_CLASS_ERROR_PATTERN_TEXT_FIELD);
            }
        });
    }
}
