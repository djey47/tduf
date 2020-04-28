package fr.tduf.gui.common.javafx.helper.options;

import fr.tduf.gui.common.helper.MessagesHelper;
import javafx.scene.control.Alert;

/**
 * Specifies single dialog appearance and behaviour.
 */
public class SimpleDialogOptions {

    private SimpleDialogOptions() {}

    protected Alert.AlertType alertContext = Alert.AlertType.NONE;
    protected String title = "";
    protected String message = "";
    protected String description = "";

    /**
     * @return instance creator
     */
    public static SimpleDialogOptionsBuilder builder() {
        return new SimpleDialogOptionsBuilder();
    }

    public Alert.AlertType getAlertContext() {
        return alertContext;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public static class SimpleDialogOptionsBuilder extends SimpleDialogOptions {
        public SimpleDialogOptionsBuilder withContext(Alert.AlertType alertContext) {
            this.alertContext = alertContext;
            return this;
        }

        public SimpleDialogOptionsBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public SimpleDialogOptionsBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public SimpleDialogOptionsBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public SimpleDialogOptionsBuilder forException(Exception e) {
            this.description = MessagesHelper.getGenericErrorMessage(e);
            return this;
        }

        public SimpleDialogOptions build() {
            SimpleDialogOptions simpleDialogOptions = new SimpleDialogOptions();

            simpleDialogOptions.alertContext = alertContext;
            simpleDialogOptions.title = title;
            simpleDialogOptions.description = description;
            simpleDialogOptions.message = message;

            return simpleDialogOptions;
        }
    }
}
