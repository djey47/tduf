package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.common.helper.javafx.ControlHelper;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Provides all common features to DynamicHelpers.
 */
public abstract class AbstractDynamicControlsHelper {

    protected MainStageController controller;

    protected AbstractDynamicControlsHelper(MainStageController controller) {
        this.controller = controller;
    }

    protected HBox addFieldBox(Optional<String> potentialGroupName, double boxHeight, VBox defaultTab) {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(boxHeight);
        fieldBox.setPadding(new Insets(5.0));

        if (potentialGroupName.isPresent()) {
            String groupName = potentialGroupName.get();
            if (!controller.getTabContentByName().containsKey(groupName)) {
                throw new IllegalArgumentException("Unknown group name: " + groupName);
            }
            controller.getTabContentByName().get(groupName).getChildren().add(fieldBox);
        } else {
            defaultTab.getChildren().add(fieldBox);
        }
        return fieldBox;
    }

    // TODO factorize label creation
    protected static Label addCustomLabel(HBox fieldBox, boolean readOnly, String text) {
        Label customLabel = new Label(text);

        customLabel.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_LABEL);
        if (readOnly) {
            customLabel.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        customLabel.setPrefWidth(175);
        fieldBox.getChildren().add(customLabel);
        return customLabel;
    }

    // TODO factorize label creation
    protected static void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName) {
        Label fieldNameLabel = new Label(fieldName);

        fieldNameLabel.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_NAME);
        if (readOnly) {
            fieldNameLabel.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        fieldNameLabel.setPrefWidth(225.0);
        fieldBox.getChildren().add(fieldNameLabel);
    }

    protected static void addContextualButton(Pane fieldPane, String buttonLabel, String tooltipText, EventHandler<ActionEvent> action) {
        Button contextualButton = new Button(buttonLabel);
        contextualButton.setPrefWidth(34);
        ControlHelper.setTooltipText(contextualButton, tooltipText);

        contextualButton.setOnAction(action);

        fieldPane.getChildren().add(contextualButton);
    }
}