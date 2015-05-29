package fr.tduf.gui.common.helper.javafx;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
     * @return mouse selected item, absent otherwise (incl case of row header clicked).
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

    /**
     * @param rowIndex  : 0-based position in table rows
     * @param tableView : table view to be processed
     * @param <T>       : Type of items in TableView
     * @return selected item, if any, at given row. Absent otherwise.
     */
    public static <T> Optional<T> selectRowAndScroll(int rowIndex, TableView<T> tableView) {
        requireNonNull(tableView, "A TableView is required.");

        tableView.getSelectionModel().select(rowIndex);
        tableView.scrollTo(rowIndex);

        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }
}