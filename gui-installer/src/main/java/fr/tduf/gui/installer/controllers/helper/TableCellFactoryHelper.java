package fr.tduf.gui.installer.controllers.helper;

import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

/**
 * Helper class to create cells with special contents
 */
public class TableCellFactoryHelper {

    private TableCellFactoryHelper() {}

    /**
     * @return a centered checkbox cell, from a boolean value
     */
    public static TableCell<VehicleSlotDataItem, Boolean> createCheckBoxCell() {
        return new TableCell<VehicleSlotDataItem, Boolean>() {

            private final HBox hBox = createHBox();
            private CheckBox checkBox;

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(hBox);
                }
            }

            private HBox createHBox() {
                final HBox b = new HBox();

                checkBox = createCheckBox();

                b.setAlignment(Pos.CENTER);
                b.getChildren().add(checkBox);

                return b;
            }

            // TODO create readOnly CheckBox component in commons
            private CheckBox createCheckBox() {
                final CheckBox cb = new CheckBox();

                cb.setDisable(true);
                cb.setStyle("-fx-opacity: 1");

                return cb;
            }
        };
    }
}
