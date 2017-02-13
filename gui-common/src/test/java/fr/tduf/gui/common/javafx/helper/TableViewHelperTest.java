package fr.tduf.gui.common.javafx.helper;

import fr.tduf.libtesting.common.helper.javafx.NonApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableViewHelperTest {

    @BeforeAll
    static void globalSetUp() {
        NonApp.initJavaFX();
    }

    @Test
    void getMouseSelectedItem_whenNullEvent_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TableViewHelper.getMouseSelectedItem(null, String.class));

        // THEN: NPE
    }

    @Test
    void getMouseSelectedItem_andNoItemSelected_shouldReturnEmpty() {
        // GIVEN-WHEN
        Optional<String> potentialItem = TableViewHelper.getMouseSelectedItem(createDefaultMouseEvent(new TableRow<>()), String.class);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    void selectItemAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TableViewHelper.selectItemAndScroll((item, row) -> false, null));
    }

    @Test
    void selectItemAndScroll_whenNullPredicate_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TableViewHelper.selectItemAndScroll(null, new TableView<>()));
    }

    @Test
    void selectItemAndScroll_whenFound_shouldReturnIt() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectItemAndScroll((item, row) -> "2".equals(item), tableView);

        // THEN
        assertThat(potentialItem).contains("2");
    }

    @Test
    void selectItemAndScroll_whenNotFound_shouldReturnIt() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectItemAndScroll((item, row) -> "SHOULD_NOT_BE_THERE".equals(item), tableView);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test
    void selectRowAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TableViewHelper.selectRowAndScroll(0, null));
    }

    @Test
    void selectRowAndScroll_whenIndexNotAvailable_shouldReturnAbsent() {
        // GIVEN
        TableView<String> tableView = new TableView<>();

        // WHEN-THEN
        assertThat(TableViewHelper.selectRowAndScroll(0, tableView)).isEmpty();
    }

    @Test
    void selectRowAndScroll_whenIndexAvailable_shouldReturnCorrectItem() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectRowAndScroll(1, tableView);

        // THEN
        assertThat(potentialItem).contains("2");
    }

    @Test
    void selectLastRowAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TableViewHelper.selectLastRowAndScroll(null));
    }

    @Test
    void selectLastRowAndScroll_whenNoItems_shouldReturnAbsent() {
        // GIVEN
        TableView<String> tableView = new TableView<>();

        // WHEN-THEN
        assertThat(TableViewHelper.selectLastRowAndScroll(tableView)).isEmpty();
    }

    @Test
    void selectLastRowAndScroll_whenManyItems_shouldReturnLastOne() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectLastRowAndScroll(tableView);

        // THEN
        assertThat(potentialItem).contains("3");
    }

    private static MouseEvent createDefaultMouseEvent(EventTarget rowTarget) {
        return new MouseEvent(null, rowTarget, MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, false, false, false, null);
    }

    private static TableView<String> createTableViewWithThreeItems() {
        ObservableList<String> values = FXCollections.observableArrayList();
        values.addAll("1", "2", "3");
        TableView<String> tableView = new TableView<>();
        tableView.setItems(values);
        return tableView;
    }
}
