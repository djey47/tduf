package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class DatabaseCheckStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = DatabaseCheckStageController.class.getSimpleName();

    private Set<IntegrityError> integrityErrors;

    private IntegerProperty totalErrorCountProperty = new SimpleIntegerProperty(0);

    @FXML
    private Label errorCountLabel;

    @FXML
    private VBox errorPanel;

    @FXML
    public void handleTryFixButtonAction(ActionEvent actionEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleUpdateMagicMapMenuItemAction");
    }

    @Override
    public void init() {
        initHeaderPane();
    }

    /**
     * Creates and display dialog.
     */
    public void initAndShowModalDialog(Set<IntegrityError> integrityErrors) {
        this.integrityErrors = requireNonNull(integrityErrors, "A list of integrity errors is required.");

        totalErrorCountProperty.set(integrityErrors.size());

        initErrorDetails();

        showModalWindow();
    }

    private void initHeaderPane() {
        errorCountLabel.textProperty().bindBidirectional(totalErrorCountProperty, new NumberStringConverter());
    }

    private void initErrorDetails() {

        Map<IntegrityError.ErrorTypeEnum, Set<IntegrityError>> errorsByType = groupErrorsByType(integrityErrors);

        errorsByType
                .forEach((errorType, errors) -> {
                    Label errorLabel = createErrorLabel(errorType, errors);
                    TableView<IntegrityError> tableView = createErrorsTableView(errors);

                    // TODO anchor Vbox to parent pane ...
                    VBox vbox = new VBox();
                    vbox.getChildren().addAll(new Separator(Orientation.HORIZONTAL), errorLabel, tableView);
                    vbox.setPrefWidth(((AnchorPane) root).getPrefWidth());
                    errorPanel.getChildren().add(vbox);
                });
    }

    private static Label createErrorLabel(IntegrityError.ErrorTypeEnum errorType, Set<IntegrityError> errors) {
        String complementFormat = "(%d)";
        String complement = String.format(complementFormat, errors.size());
        String errorMessage = String.format(errorType.getErrorMessageFormat(), complement);
        Label errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("error-label");
        return errorLabel;
    }

    private static TableView<IntegrityError> createErrorsTableView(Set<IntegrityError> errors) {
        Map<IntegrityError.ErrorInfoEnum, Object> information = errors.stream().findAny().get().getInformation();
        final ObservableList<IntegrityError> integrityErrors = FXCollections.observableArrayList();

        TableView<IntegrityError> tableView = new TableView<>();
        tableView.setPrefHeight(38 * errors.size());

        // TODO order columns
        information.entrySet().forEach((entry) -> {
            final IntegrityError.ErrorInfoEnum itemName = entry.getKey();
            final TableColumn<IntegrityError, ?> column = new TableColumn<>(itemName.toString());

            column.setCellValueFactory((cellData) -> {
                StringProperty prop = new SimpleStringProperty(cellData.getValue().getInformation().get(itemName).toString());
                return (ObservableValue) prop;
            });

            tableView.getColumns().add(column);
        });

        // TODO order errors
        integrityErrors.addAll(errors);
        tableView.setItems(integrityErrors);
        return tableView;
    }

    private static Map<IntegrityError.ErrorTypeEnum, Set<IntegrityError>> groupErrorsByType(Set<IntegrityError> integrityErrors) {
        // TODO order by type
        return integrityErrors.stream()
                    .collect(Collectors.toMap(
                            IntegrityError::getErrorTypeEnum,
                            (ie) -> {
                                Set<IntegrityError> errorSet = new HashSet<>();
                                errorSet.add(ie);
                                return errorSet;
                            },
                            (hs1, hs2) -> {
                                hs1.addAll(hs2);
                                return hs1;
                            }));
    }
}
