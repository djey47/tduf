package fr.tduf.gui.database.plugins.common;

import fr.tduf.gui.common.javafx.helper.ControlHelper;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static fr.tduf.gui.database.common.DisplayConstants.LABEL_BUTTON_BROWSE;
import static fr.tduf.gui.database.common.DisplayConstants.TOOLTIP_BUTTON_BROWSE_RESOURCES;
import static fr.tduf.gui.database.common.FxConstants.CSS_CLASS_VERTICAL_BUTTON_BOX;
import static fr.tduf.gui.database.plugins.common.FxConstants.CSS_CLASS_BUTTON_MEDIUM;
import static javafx.geometry.Orientation.HORIZONTAL;

/**
 * Provides builders of common components for plugins
 */
public class PluginComponentBuilders {
    /**
     * @return a builder to generate button column, with separators when needed
     */
    public static ButtonColumnBuilder buttonColumn() {
        return new ButtonColumnBuilder();
    }

    /**
     * @param editorContext - global context, providing main stage controller
     * @param onTheFlyContext - runtime context, providing location of requested resource (topic - field rank - raw value)
     * @return a button instance, allowing to browse resources within topic
     */
    public static Button createBrowseResourceButton(EditorContext editorContext, OnTheFlyContext onTheFlyContext) {
        Button browseResourceButton = new Button(LABEL_BUTTON_BROWSE);

        browseResourceButton.getStyleClass().add(CSS_CLASS_BUTTON_MEDIUM);
        ControlHelper.setTooltipText(browseResourceButton, TOOLTIP_BUTTON_BROWSE_RESOURCES);
        browseResourceButton.setOnAction(
                editorContext.getMainStageController().getViewData().handleBrowseResourcesButtonMouseClick(onTheFlyContext.getRemoteTopic(), onTheFlyContext.getRawValueProperty(), onTheFlyContext.getFieldRank()));

        return browseResourceButton;
    }

    /**
     * 'A la carte' builder for buttons in column layout
     */
    public static class ButtonColumnBuilder {
        private final List<Node> buttonsOrSeparator = new ArrayList<>();

        public ButtonColumnBuilder withButton(ButtonBase button) {
            buttonsOrSeparator.add(button);
            return this;
        }

        public ButtonColumnBuilder withSeparator() {
            buttonsOrSeparator.add(new Separator(HORIZONTAL));
            return this;
        }

        public VBox build() {
            VBox buttonColumnBox = new VBox();

            buttonColumnBox.getStyleClass().add(CSS_CLASS_VERTICAL_BUTTON_BOX);
            buttonColumnBox.getChildren().addAll(buttonsOrSeparator);

            return buttonColumnBox;
        }
    }
}
