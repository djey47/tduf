package fr.tduf.gui.common.javafx.helper;

import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TableViewHelperTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Test(expected=NullPointerException.class)
    public void getMouseSelectedItem_whenNullEvent_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        TableViewHelper.getMouseSelectedItem(null, String.class);

        // THEN: NPE
    }

    @Test
    public void getMouseSelectedItem_andNoItemSelected_shouldReturnAbsent() {
        // GIVEN-WHEN
        Optional<String> potentialItem = TableViewHelper.getMouseSelectedItem(createDefaultMouseEvent(new TableRow<>()), String.class);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test(expected=NullPointerException.class)
    public void selectItemAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN
        TableViewHelper.selectItemAndScroll(item -> false, null);

        // THEN: NPE
    }

    @Test(expected=NullPointerException.class)
    public void selectItemAndScroll_whenNullPredicate_shouldThrowException() {
        // GIVEN-WHEN
        TableViewHelper.selectItemAndScroll(null, new TableView<>());

        // THEN: NPE
    }

    @Test
    public void selectItemAndScroll_whenFound_shouldReturnIt() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectItemAndScroll("2"::equals, tableView);

        // THEN
        assertThat(potentialItem).contains("2");
    }

    @Test
    public void selectItemAndScroll_whenNotFound_shouldReturnIt() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectItemAndScroll("SHOULD_NOT_BE_THERE"::equals, tableView);

        // THEN
        assertThat(potentialItem).isEmpty();
    }

    @Test(expected=NullPointerException.class)
    public void selectRowAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN
        TableViewHelper.selectRowAndScroll(0, null);

        // THEN: NPE
    }

    @Test
    public void selectRowAndScroll_whenIndexNotAvailable_shouldReturnAbsent() {
        // GIVEN
        TableView<String> tableView = new TableView<>();

        // WHEN-THEN
        assertThat(TableViewHelper.selectRowAndScroll(0, tableView)).isEmpty();
    }

    @Test
    public void selectRowAndScroll_whenIndexAvailable_shouldReturnCorrectItem() {
        // GIVEN
        TableView<String> tableView = createTableViewWithThreeItems();

        // WHEN
        Optional<String> potentialItem = TableViewHelper.selectRowAndScroll(1, tableView);

        // THEN
        assertThat(potentialItem).contains("2");
    }

    @Test(expected=NullPointerException.class)
    public void selectLastRowAndScroll_whenNullTableView_shouldThrowException() {
        // GIVEN-WHEN
        TableViewHelper.selectLastRowAndScroll(null);

        // THEN: NPE
    }

    @Test
    public void selectLastRowAndScroll_whenNoItems_shouldReturnAbsent() {
        // GIVEN
        TableView<String> tableView = new TableView<>();

        // WHEN-THEN
        assertThat(TableViewHelper.selectLastRowAndScroll(tableView)).isEmpty();
    }

    @Test
    public void selectLastRowAndScroll_whenManyItems_shouldReturnLastOne() {
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
