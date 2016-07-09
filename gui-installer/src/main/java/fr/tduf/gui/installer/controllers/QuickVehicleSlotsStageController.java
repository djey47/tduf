package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.converter.VehicleKindToStringConverter;
import fr.tduf.gui.installer.controllers.helper.TableCellFactoryHelper;
import fr.tduf.gui.installer.domain.exceptions.AbortedInteractiveStepException;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * FX Controller for QUICK vehicle slot selector
 */
public class QuickVehicleSlotsStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = QuickVehicleSlotsStageController.class.getSimpleName();

    @FXML
    private ChoiceBox<VehicleSlotsHelper.VehicleKind> vehicleKindFilterChoiceBox;

    @FXML
    private TableView<VehicleSlotDataItem> slotsTableView;

    @FXML
    private TableColumn<VehicleSlotDataItem, Boolean> installedSlotTableColumn;

    private VehicleSlotsHelper vehicleSlotsHelper;

    private ObservableList<VehicleSlotDataItem> slotsData = FXCollections.observableArrayList();

    private Property<VehicleSlotDataItem> selectedSlotProperty;

    private Property<VehicleSlotsHelper.SlotKind> slotKindProperty = new SimpleObjectProperty<>();

    private VehicleSlotDataItem currentSlot;

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleSlotsTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleSlotsTableMouseClick");

        TableViewHelper.getMouseSelectedItem(mouseEvent, VehicleSlotDataItem.class)
                .ifPresent(item -> currentSlot = item);
    }

    @FXML
    private void handleSearchSlotButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchSlotButtonAction");

        askForReferenceAndSelectItem();
    }

    @FXML
    private void handleResetButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleResetButtonAction");

        if (currentSlot != null) {
            selectedSlotProperty.setValue(currentSlot);
            closeWindow();
        }
    }

    /**
     * Creates and display dialog.
     *
     * @param miner                  : instance of database miner to parse contents
     * @return selected item, if any.
     */
    public VehicleSlotDataItem initAndShowModalDialog(BulkDatabaseMiner miner) throws AbortedInteractiveStepException {
        requireNonNull(miner, "Database miner instance is required.");

        vehicleSlotsHelper = VehicleSlotsHelper.load(miner);

        selectedSlotProperty.setValue(null);

        updateSlotsStageData(slotKindProperty.getValue(), VehicleSlotsHelper.VehicleKind.DRIVABLE);

        showModalWindow();

        if (selectedSlotProperty.getValue() == null) {
            throw new AbortedInteractiveStepException();
        }

        return selectedSlotProperty.getValue();
    }

    private void initHeaderPane() {
        slotKindProperty.setValue(VehicleSlotsHelper.SlotKind.ALL);
        selectedSlotProperty = new SimpleObjectProperty<>();

        vehicleKindFilterChoiceBox.setConverter(new VehicleKindToStringConverter());
        vehicleKindFilterChoiceBox.getItems().addAll(asList(VehicleSlotsHelper.VehicleKind.values()));
        vehicleKindFilterChoiceBox.setValue(VehicleSlotsHelper.VehicleKind.DRIVABLE);
        vehicleKindFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            updateSlotsStageData(slotKindProperty.getValue(), newValue);
        });
    }

    private void initTablePane() {
        installedSlotTableColumn.setCellValueFactory(cellData -> cellData.getValue().moddedProperty());
        installedSlotTableColumn.setCellFactory(column -> TableCellFactoryHelper.createCheckBoxCell());

        TableColumn<VehicleSlotDataItem, ?> refColumn = slotsTableView.getColumns().get(1);
        refColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<VehicleSlotDataItem, ?> nameColumn = slotsTableView.getColumns().get(2);
        nameColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().nameProperty());

        TableColumn<VehicleSlotDataItem, ?> carIdColumn = slotsTableView.getColumns().get(3);
        carIdColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().carIdProperty());

        slotsTableView.setItems(slotsData);
    }

    private void updateSlotsStageData(VehicleSlotsHelper.SlotKind slotKind, VehicleSlotsHelper.VehicleKind vehicleKind) {
        slotsData.clear();

        slotsData.addAll(vehicleSlotsHelper.getVehicleSlots(slotKind, vehicleKind).stream()
                .map(VehicleSlotDataItem::fromVehicleSlot)
                .collect(toList()));
    }

    private void askForReferenceAndSelectItem() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_SLOT,
                DisplayConstants.LABEL_SEARCH_SLOT)

                .ifPresent(entryReference -> TableViewHelper.selectItemAndScroll(
                        oneItem -> oneItem.referenceProperty().getValue().equals(entryReference),
                        slotsTableView));
    }

    public Property<VehicleSlotsHelper.SlotKind> slotKindProperty() {
        return slotKindProperty;
    }

    public Property<VehicleSlotDataItem> selectedSlotProperty() {
        return selectedSlotProperty;
    }
}
