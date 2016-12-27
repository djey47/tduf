package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.listener.ErrorChangeListener;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Optional;

import static javafx.beans.binding.Bindings.not;

/**
 * Provides all common features to DynamicHelpers.
 */
abstract class AbstractDynamicControlsHelper {

    protected MainStageController controller;

    protected AbstractDynamicControlsHelper(MainStageController controller) {
        this.controller = controller;
    }

    protected HBox addFieldBox(Optional<String> potentialGroupName, double boxHeight) {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(boxHeight);
        fieldBox.setPadding(new Insets(5.0));

        final Map<String, VBox> tabContentByName = controller.getViewData().getTabContentByName();
        String effectiveGroupName = DisplayConstants.TAB_NAME_DEFAULT;
        if (potentialGroupName.isPresent()) {
            String groupName = potentialGroupName.get();
            if (!tabContentByName.containsKey(groupName)) {
                throw new IllegalArgumentException("Unknown group name: " + groupName);
            }
            effectiveGroupName = groupName;
        }
        tabContentByName.get(effectiveGroupName).getChildren().add(fieldBox);
        return fieldBox;
    }

    protected void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName, String toolTipText, int fieldRank) {
        Label fieldLabel = addLabel(fieldBox, readOnly, 225.0, fieldName);

        fieldLabel.setTooltip(new Tooltip(toolTipText));

        controller.getViewData()
                .getItemPropsByFieldRank()
                .errorPropertyAtFieldRank(fieldRank)
                .addListener(new ErrorChangeListener(fieldLabel));

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

    protected static Button addContextualButton(Pane fieldPane, String buttonLabel, String tooltipText, EventHandler<ActionEvent> action) {
        Button contextualButton = new Button(buttonLabel);
        contextualButton.setPrefWidth(34);
        ControlHelper.setTooltipText(contextualButton, tooltipText);

        contextualButton.setOnAction(action);

        fieldPane.getChildren().add(contextualButton);

        return contextualButton;
    }

    protected static Button addContextualButtonWithActivationCondition(Pane fieldPane, String buttonLabel, String tooltipText, EventHandler<ActionEvent> action, ObservableBooleanValue activationCondition) {
        Button contextualButton = addContextualButton(fieldPane, buttonLabel, tooltipText, action);

        contextualButton.disableProperty().bind(not(activationCondition));

        return contextualButton;
    }

    protected BulkDatabaseMiner getMiner() {
        return controller.getMiner();
    }
}
