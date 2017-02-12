package fr.tduf.gui.common.javafx.helper;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * Helper class to make TableView controls handling easier.
 */
public class TableViewHelper {

    /**
     * @param mouseEvent    : event which was dispatched from GUI
     * @param itemType      : type of item to be returned
     * @return mouse selected item, absent otherwise (incl case of row header clicked).
     */
    public static <T> Optional<T> getMouseSelectedItem(MouseEvent mouseEvent, Class<T> itemType) {
        requireNonNull(mouseEvent, "A mouse event is required.");
        requireNonNull(itemType, "Type of returned item is required.");

        Node node = ((Node) mouseEvent.getTarget()).getParent();
        if (node == null) {
            return empty();
        }

        T selectedItem = null;
        if (node instanceof TableRow) {
            //noinspection unchecked
            selectedItem = (T) ((TableRow) node).getItem();
        } else if (node.getParent() instanceof TableRow) {
            //noinspection unchecked
            selectedItem = (T) ((TableRow) node.getParent()).getItem();
        }

        return Optional.ofNullable(selectedItem);
    }

    /**
     * Selects and scrolls to first item matching a search criteria
     * @param searchPredicate   : predicate specifying search criteria.
     * @param tableView         : table view to be processed
     * @param <T>               : Type of items in TableView
     * @return selected item, if any, at given row. Absent otherwise.
     */
    public static <T> Optional<T> selectItemAndScroll(Predicate<T> searchPredicate, TableView<T> tableView) {
        requireNonNull(searchPredicate, "A search predicate is required.");
        requireNonNull(tableView, "A TableView is required.");

        if (tableView.getItems().isEmpty()) {
            return empty();
        }

        int rowIndex = 0;
        for (T item : tableView.getItems()) {
            if (searchPredicate.test(item)) {
                return selectRowAndScroll(rowIndex, tableView);
            }
            rowIndex++;
        }

        return empty();
    }

    // TODO Only keep BiPredicate version
    /**
     * Selects and scrolls to first item matching a search criteria
     * @param searchPredicate   : predicate specifying search criteria. Current row index is also provided.
     * @param tableView         : table view to be processed
     * @param <T>               : Type of items in TableView
     * @return selected item, if any, at given row. Absent otherwise.
     */
    public static <T> Optional<T> selectItemAndScroll(BiPredicate<T, Integer> searchPredicate, TableView<T> tableView) {
        requireNonNull(searchPredicate, "A search predicate is required.");
        requireNonNull(tableView, "A TableView is required.");

        if (tableView.getItems().isEmpty()) {
            return empty();
        }

        int rowIndex = 0;
        for (T item : tableView.getItems()) {
            if (searchPredicate.test(item, rowIndex)) {
                return selectRowAndScroll(rowIndex, tableView);
            }
            rowIndex++;
        }

        return empty();
    }

    /**
     * @param rowIndex  : 0-based position in table rows
     * @param tableView : table view to be processed
     * @param <T>       : Type of items in TableView
     * @return selected item, if any, at given row. Absent otherwise.
     */
    public static <T> Optional<T> selectRowAndScroll(int rowIndex, TableView<T> tableView) {
        requireNonNull(tableView, "A TableView is required.");

        if (rowIndex < 0 || rowIndex >= tableView.getItems().size()) {
            return empty();
        }

        tableView.getSelectionModel().select(rowIndex);
        tableView.scrollTo(rowIndex);

        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    /**
     * @param tableView : table view to be processed
     * @param <T>       : Type of items in TableView
     * @return selected item, if any, at last row. Absent otherwise.
     */
    public static <T> Optional<T> selectLastRowAndScroll(TableView<T> tableView) {
        requireNonNull(tableView, "A TableView is required.");

        ObservableList<T> items = tableView.getItems();
        if (items.isEmpty()) {
            return empty();
        }

        return selectRowAndScroll(items.size() - 1, tableView);
    }
}
