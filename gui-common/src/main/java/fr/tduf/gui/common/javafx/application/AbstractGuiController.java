package fr.tduf.gui.common.javafx.application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

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
        } catch (IOException ioe) {
            throw new RuntimeException("Window initializing failed.", ioe);
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
        if (root == null || root.getScene() == null) {
            return null;
        }
        return root.getScene().getWindow();
    }

    /**
     * Closes window associated to current scene.
     */
    protected void closeWindow() {
        getStage().close();
    }

    /**
     * Displays window associated to current scene.
     */
    protected void showWindow() {
        getStage().show();
    }

    /**
     * Displays window associated to current scene and blocks current thread until it closes.
     */
    protected void showModalWindow() {
        getStage().showAndWait();
    }

    /**
     * @return mouse cursor property for root node
     */
    protected ObjectProperty<Cursor> rootCursorProperty() {
        return requireNonNull(root, "Root node is not available yet.")
            .cursorProperty();
    }

    /**
     * @return window bar title property
     */
    protected StringProperty titleProperty() {
        return getStage().titleProperty();
    }

    // Visible for testing
    Stage getStage() {
        return (Stage) requireNonNull(getWindow(), "Window has not been initialized yet.");
    }
}
