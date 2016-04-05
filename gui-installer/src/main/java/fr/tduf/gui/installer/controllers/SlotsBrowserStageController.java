package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class SlotsBrowserStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = SlotsBrowserStageController.class.getSimpleName();

    @FXML
    private Label instructionsLabel;

    @FXML
    private TextField slotRefTextField;

    @FXML
    TableView<VehicleSlotDataItem> slotsTableView;

    private VehicleSlotsHelper vehicleSlotsHelper;

    private ObservableList<VehicleSlotDataItem> slotsData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private Property<Optional<VehicleSlotDataItem>> selectedSlotProperty;

    private Optional<VehicleSlotDataItem> returnedSlot;

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleCreateNewSlotHyperlinkAction(ActionEvent actionEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleCreateNewSlotHyperlinkAction");

        returnedSlot = empty();

        closeWindow();
    }

    @FXML
    private void handleSlotsTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleSlotsTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton()) {
            TableViewHelper.getMouseSelectedItem(mouseEvent, VehicleSlotDataItem.class)
                    .ifPresent((item) -> selectedSlotProperty.setValue(of(item)));
        }
    }

    @FXML
    private void handleSearchSlotButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchSlotButtonAction");

        askForReferenceAndSelectItem();
    }

    @FXML
    private void handleOkButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOkButtonAction");

        returnedSlot = selectedSlotProperty.getValue();

        closeWindow();
    }

    /**
     * Creates and display dialog.
     *
     * @param potentialSlotReference : slot reference to be selected (optional)
     * @param miner                  : instance of database miner to parse contents
     * @return selected item, if any.
     */
    public Optional<VehicleSlotDataItem> initAndShowModalDialog(Optional<String> potentialSlotReference, BulkDatabaseMiner miner) throws Exception {
        requireNonNull(miner, "Database miner instance is required.");

        vehicleSlotsHelper = VehicleSlotsHelper.load(miner);

        selectedSlotProperty.setValue(empty());

        currentTopicProperty.setValue(CAR_PHYSICS_DATA);

        updateSlotsStageData();

        potentialSlotReference.ifPresent(this::selectEntryInTableAndScroll);

        showModalWindow();

        if (returnedSlot == null) {
            throw new Exception("Aborted by user.");
        }

        return returnedSlot;
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();
        selectedSlotProperty = new SimpleObjectProperty<>();

        slotRefTextField.textProperty().bindBidirectional(selectedSlotProperty, new StringConverter<Optional<VehicleSlotDataItem>>() {
            @Override
            public String toString(Optional<VehicleSlotDataItem> slotItem) {
                if (slotItem == null) {
                    slotItem = empty();
                }

                return slotItem
                        .map((item) -> item.referenceProperty().get())
                        .orElse("");
            }

            @Override
            public Optional<VehicleSlotDataItem> fromString(String ref) {
                if (StringUtils.isEmpty(ref)) {
                    return empty();
                }

                VehicleSlotDataItem vehicleSlotDataItem = new VehicleSlotDataItem();
                vehicleSlotDataItem.setReference(ref);
                return of(vehicleSlotDataItem);
            }
        });
    }

    private void initTablePane() {
        TableColumn<VehicleSlotDataItem, ?> refColumn = slotsTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<VehicleSlotDataItem, ?> nameColumn = slotsTableView.getColumns().get(1);
        nameColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().nameProperty());

        TableColumn<VehicleSlotDataItem, ?> carIdColumn = slotsTableView.getColumns().get(2);
        carIdColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().carIdProperty());

        slotsTableView.setItems(slotsData);
    }

    private void selectEntryInTableAndScroll(String entryReference) {
        slotsData.stream()

                .filter((resource) -> resource.referenceProperty().get().equals(entryReference))

                .findAny()

                .ifPresent((browsedResource) -> {
                    slotsTableView.getSelectionModel().select(browsedResource);
                    slotsTableView.scrollTo(browsedResource);
                });
    }

    private void updateSlotsStageData() {
        slotsData.clear();

        slotsData.addAll(vehicleSlotsHelper.getDrivableVehicleSlots().stream()

                .map((vehicleSlot) -> {
                    VehicleSlotDataItem dataItem = new VehicleSlotDataItem();

                    dataItem.setReference(vehicleSlot.getRef());
                    dataItem.setName(VehicleSlotsHelper.getVehicleName(vehicleSlot));
                    dataItem.setCarId(vehicleSlot.getCarIdentifier());

                    return dataItem;
                })

                .collect(toList()));
    }

    private void askForReferenceAndSelectItem() {
        CommonDialogsHelper.showInputValueDialog(
                DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_SEARCH_SLOT,
                DisplayConstants.LABEL_SEARCH_SLOT)

                .ifPresent((entryReference) -> TableViewHelper.selectItemAndScroll(
                        oneItem -> oneItem.referenceProperty().getValue().equals(entryReference),
                        slotsTableView));
    }
}
