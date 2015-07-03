package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.common.helper.javafx.ControlHelper;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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

    protected static void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName, Optional<String> potentialToolTipText) {
        Label fieldLabel = addLabel(fieldBox, readOnly, 225.0, fieldName);

        potentialToolTipText.ifPresent((text) -> fieldLabel.setTooltip(new Tooltip(text)));

        fieldLabel.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_NAME);
    }

    protected static Label addCustomLabel(HBox fieldBox, boolean readOnly, String text) {
        return addLabel(fieldBox, readOnly, 175.0, text);
    }

    protected static Label addLabel(HBox fieldBox, boolean readOnly, double width, String text) {
        Label label = new Label(text);

        label.getStyleClass().add(FxConstants.CSS_CLASS_FIELD_LABEL);
        if (readOnly) {
            label.getStyleClass().add(FxConstants.CSS_CLASS_READONLY_FIELD);
        }
        label.setPrefWidth(width);
        fieldBox.getChildren().add(label);
        return label;
    }

    protected static void addContextualButton(Pane fieldPane, String buttonLabel, String tooltipText, EventHandler<ActionEvent> action) {
        Button contextualButton = new Button(buttonLabel);
        contextualButton.setPrefWidth(34);
        ControlHelper.setTooltipText(contextualButton, tooltipText);

        contextualButton.setOnAction(action);

        fieldPane.getChildren().add(contextualButton);
    }
}