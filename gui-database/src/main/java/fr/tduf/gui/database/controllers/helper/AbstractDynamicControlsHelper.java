package fr.tduf.gui.database.controllers.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.ImageConstants;
import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.listener.ErrorChangeListener;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Map;

import static fr.tduf.gui.database.common.DisplayConstants.TAB_NAME_DEFAULT;
import static java.util.Optional.ofNullable;
import static javafx.beans.binding.Bindings.not;

/**
 * Provides all common features to DynamicHelpers.
 */
abstract class AbstractDynamicControlsHelper {
    private static final String THIS_CLASS_NAME = AbstractDynamicControlsHelper.class.getSimpleName();

    protected MainStageController controller;

    protected AbstractDynamicControlsHelper(MainStageController controller) {
        this.controller = controller;
    }

    protected HBox addFieldBox(String groupName, double boxHeight) {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(boxHeight);
        fieldBox.setPadding(new Insets(5.0));

        final Map<String, VBox> tabContentByName = controller.getViewData().getTabContentByName();
        String effectiveGroupName = ofNullable(groupName)
                .map(name -> {
                    if (tabContentByName.containsKey(name)) {
                        return name;
                    }
                    Log.warn(THIS_CLASS_NAME, "Unknown group name: " + groupName + ", will use default.");
                    return null;
                })
                .orElse(TAB_NAME_DEFAULT);
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
        contextualButton.getStyleClass().add(FxConstants.CSS_CLASS_BUTTON_SMALL);
        ControlHelper.setTooltipText(contextualButton, tooltipText);

        contextualButton.setOnAction(action);

        fieldPane.getChildren().add(contextualButton);

        return contextualButton;
    }

    protected static void addContextualButtonWithActivationCondition(Pane fieldPane, String buttonLabel, String tooltipText, EventHandler<ActionEvent> action, ObservableBooleanValue activationCondition) {
        Button contextualButton = addContextualButton(fieldPane, buttonLabel, tooltipText, action);
        contextualButton.disableProperty().bind(not(activationCondition));
    }

    protected static void addErrorSign(HBox hBox, BooleanProperty errorProperty, StringProperty errorMessageProperty) {
        Image errorSignImage = new Image(ImageConstants.RESOURCE_ERROR, 24.0, 24.0, true, true);

        ImageView imageView = new ImageView(errorSignImage);
        imageView.visibleProperty().bindBidirectional(errorProperty);

        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(errorMessageProperty);
        Tooltip.install(imageView, tooltip);

        hBox.getChildren().add(imageView);
    }

    protected BulkDatabaseMiner getMiner() {
        return controller.getMiner();
    }
}
