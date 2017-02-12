package fr.tduf.gui.common.javafx.scene.control;

import fr.tduf.gui.common.DisplayConstants;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * Simple dialog allowing to enter a search pattern and perform search, then moving to next result.
 * Does not have any intelligence, must be provided via callbacks.
 */
public class SearchValueDialog  {
    private final Dialog<Void> dialog;

    private TextField patternTextField = new TextField();

    private Consumer<String> next = p -> { };
    private Consumer<String> first = p -> { };

    /**
     * @param title : value to be displayed in title bar of tool window
     */
    public SearchValueDialog(String title) {
        dialog = new Dialog<>();

        initDialogSettings(title);

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
    public void setCallbacks(Consumer<String> first, Consumer<String> next) {
        this.first = first;
        this.next = next;
    }

    private void initDialogSettings(String title) {
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(false);
        dialog.setTitle(title);
    }

    // TODO externalize constants
    private void initControls() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        StringProperty patternValueProperty = patternTextField.textProperty();
        patternValueProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            first.accept(newValue);
        });

        Button firstButton = new Button("|◀");
        firstButton.setOnAction(event -> {
            String value = patternValueProperty.getValue();
            if (value.isEmpty()) {
                return;
            }
            first.accept(value);
        });

        Button nextButton = new Button("▶");
        nextButton.setOnAction(event -> {
            String value = patternValueProperty.getValue();
            if (value.isEmpty()) {
                return;
            }
            next.accept(value);
        });

        grid.add(patternTextField, 0, 0);
        grid.add(firstButton, 1, 0);
        grid.add(nextButton, 2, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType(DisplayConstants.LABEL_BUTTON_CLOSE, ButtonBar.ButtonData.CANCEL_CLOSE));
    }
}
