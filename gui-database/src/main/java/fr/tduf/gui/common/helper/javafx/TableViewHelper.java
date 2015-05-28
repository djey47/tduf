package fr.tduf.gui.common.helper.javafx;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
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
     * @return selected item, absent otherwise (incl case of row header clicked).
     */
    public static <T> Optional<T> getMouseSelectedItem(MouseEvent mouseEvent) {
        requireNonNull(mouseEvent, "A mouse event is required.");

        Node node = ((Node) mouseEvent.getTarget()).getParent();

        T selectedItem;
        if (node == null || node instanceof TableHeaderRow) {
            selectedItem = null;
        } else if (node instanceof TableRow) {
            selectedItem = (T) ((TableRow) node).getItem();
        } else {
            selectedItem = (T) ((TableRow) node.getParent()).getItem();
        }

        return Optional.ofNullable(selectedItem);
    }
}