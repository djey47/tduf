package fr.tduf.gui.common.helper.javafx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    private Parent root;

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

    protected Window getWindow() {
        return root.getScene().getWindow();
    }
}