package fr.tduf.gui.installer.controllers;

import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class DatabaseCheckStageController extends AbstractGuiController {

    @FXML
    private VBox errorPanel;

    private Set<IntegrityError> integrityErrors;

    @Override
    public void init() {
        initHeaderPane();
    }

    /**
     * Creates and display dialog.
     */
    public void initAndShowModalDialog(Set<IntegrityError> integrityErrors) {
        this.integrityErrors = requireNonNull(integrityErrors, "A list of integrity errors is required.");

        initErrorDetails();

        showModalWindow();
    }

    private void initHeaderPane() {
    }

    private void initErrorDetails() {

        Map<IntegrityError.ErrorTypeEnum, Set<IntegrityError>> errorsByType = integrityErrors.stream()

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

        errorsByType
                .forEach((errorType, errors) -> {

                    Map<IntegrityError.ErrorInfoEnum, Object> information = errors.stream().findAny().get().getInformation();


                    String format = "%s (%d)";

                    String errorMessage = String.format(errorType.getErrorMessageFormat(), "");
                    Label errorLabel = new Label(String.format(format, errorMessage, errors.size()));

                    TableView<IntegrityError> tableView = new TableView<>();

                    information.keySet().forEach((name) -> tableView.getColumns().add(new TableColumn<>(name.toString())));





                    VBox flowPane = new VBox(errorLabel, tableView);

                    errorPanel.getChildren().add(flowPane);
                });
    }
}
