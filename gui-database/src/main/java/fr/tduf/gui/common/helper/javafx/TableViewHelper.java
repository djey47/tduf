package fr.tduf.gui.common.helper.javafx;

import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to make TableView controls handling easier.
 */
public class TableViewHelper {

    /**
     * @param mouseEvent    : event which was dispatched from GUI
     * @param <T>           : type of item to be returned
     * @return selected item, absent otherwise.
     */
    public static <T> Optional<T> getMouseSelectedItem(MouseEvent mouseEvent) {
        requireNonNull(mouseEvent, "A mouse event is required.");

        Node node = ((Node) mouseEvent.getTarget()).getParent();

        T selectedItem;
        if (node == null) {
            selectedItem = null;
        } else if (node instanceof TableRow) {
            selectedItem = (T) ((TableRow) node).getItem();
        } else {
            selectedItem = (T) ((TableRow) node.getParent()).getItem();
        }

        return Optional.ofNullable(selectedItem);
    }
}