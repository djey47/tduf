package fr.tduf.gui.common.javafx.application;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Parent class for all JavaFX controllers in TDUF applications.
 * Provides common features.
 */
public abstract class AbstractGuiController implements Initializable {
    @FXML
    protected Parent root;

    /**
     * Generic init code.
     * See {@link Initializable#initialize}
     */
    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }
    }

    /**
     * Specific init code.
     */
    protected abstract void init() throws IOException;

    /**
     * @return Window associated to current scene.
     */
    public Window getWindow() {
        return root.getScene().getWindow();
    }

    /**
     * Closes window associated to current scene.
     */
    protected void closeWindow() {
        ((Stage) getWindow()).close();
    }

    /**
     * Displays window associated to current scene.
     */
    protected void showWindow() {
        ((Stage) getWindow()).show();
    }

    /**
     * Displays window associated to current scene and blocks current thread until it closes.
     */
    protected void showModalWindow() {
        ((Stage) getWindow()).showAndWait();
    }

    protected ObjectProperty<Cursor> mouseCursorProperty() { return root.cursorProperty(); }
}