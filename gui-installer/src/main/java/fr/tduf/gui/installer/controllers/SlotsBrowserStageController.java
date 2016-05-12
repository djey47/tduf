package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.SecurityOptions;
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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
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
    private ChoiceBox<VehicleSlotsHelper.VehicleKind> vehicleKindFilterChoiceBox;

    @FXML
    private ChoiceBox<VehicleSlotsHelper.SlotKind> slotKindFilterChoiceBox;

    @FXML
    private TableView<VehicleSlotDataItem> slotsTableView;

    @FXML
    private TableColumn<VehicleSlotDataItem, Boolean> installedSlotTableColumn;

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

        updateSlotsStageData(VehicleSlotsHelper.SlotKind.TDUCP, VehicleSlotsHelper.VehicleKind.DRIVABLE);

        potentialSlotReference.ifPresent(this::selectEntryInTableAndScroll);

        showModalWindow();

        if (returnedSlot == null) {
            throw new Exception(DisplayConstants.MESSAGE_ABORTED_USER);
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
                    return "";
                }

                return slotItem
                        .map(item -> item.referenceProperty().get())
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

        vehicleKindFilterChoiceBox.setConverter(new StringConverter<VehicleSlotsHelper.VehicleKind>() {
            @Override
            public String toString(VehicleSlotsHelper.VehicleKind vehicleKind) {
                return vehicleKind.getLabel();
            }

            @Override
            public VehicleSlotsHelper.VehicleKind fromString(String label) {
                return null;
            }
        });
        vehicleKindFilterChoiceBox.getItems().addAll(asList(VehicleSlotsHelper.VehicleKind.values()));
        vehicleKindFilterChoiceBox.setValue(VehicleSlotsHelper.VehicleKind.DRIVABLE);
        vehicleKindFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            updateSlotsStageData(slotKindFilterChoiceBox.getSelectionModel().getSelectedItem(), newValue);
        });

        slotKindFilterChoiceBox.setConverter(new StringConverter<VehicleSlotsHelper.SlotKind>() {
            @Override
            public String toString(VehicleSlotsHelper.SlotKind slotKind) {
                return slotKind.getLabel();
            }

            @Override
            public VehicleSlotsHelper.SlotKind fromString(String label) {
                return null;
            }
        });
        slotKindFilterChoiceBox.getItems().addAll(asList(VehicleSlotsHelper.SlotKind.values()));
        slotKindFilterChoiceBox.setValue(VehicleSlotsHelper.SlotKind.TDUCP);
        slotKindFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            updateSlotsStageData(newValue, vehicleKindFilterChoiceBox.getSelectionModel().getSelectedItem());
        });
    }

    private void initTablePane() {
        installedSlotTableColumn.setCellValueFactory(cellData -> cellData.getValue().moddedProperty());
        installedSlotTableColumn.setCellFactory(column -> new TableCell<VehicleSlotDataItem, Boolean>() {

            private final HBox hBox = createHBox();
            private CheckBox checkBox;

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(hBox);
                }
            }

            private HBox createHBox() {
                final HBox hBox = new HBox();

                checkBox = createCheckBox();

                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().add(checkBox);

                return hBox;
            }

            // TODO create readOnly CheckBox component in commons
            private CheckBox createCheckBox() {
                final CheckBox checkBox = new CheckBox();

                checkBox.setDisable(true);
                checkBox.setStyle("-fx-opacity: 1");

                return checkBox;
            }
        });

        TableColumn<VehicleSlotDataItem, ?> refColumn = slotsTableView.getColumns().get(1);
        refColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<VehicleSlotDataItem, ?> nameColumn = slotsTableView.getColumns().get(2);
        nameColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().nameProperty());

        TableColumn<VehicleSlotDataItem, ?> carIdColumn = slotsTableView.getColumns().get(3);
        carIdColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().carIdProperty());

        slotsTableView.setItems(slotsData);
    }

    // Ignore warning
    private void selectEntryInTableAndScroll(String entryReference) {
        slotsData.stream()

                .filter(resource -> resource.referenceProperty().get().equals(entryReference))

                .findAny()

                .ifPresent(browsedResource -> {
                    slotsTableView.getSelectionModel().select(browsedResource);
                    slotsTableView.scrollTo(browsedResource);
                });
    }

    private void updateSlotsStageData(VehicleSlotsHelper.SlotKind slotKind, VehicleSlotsHelper.VehicleKind vehicleKind) {
        slotsData.clear();

        slotsData.addAll(vehicleSlotsHelper.getVehicleSlots(slotKind, vehicleKind).stream()

                .map(vehicleSlot -> {
                    VehicleSlotDataItem dataItem = new VehicleSlotDataItem();

                    dataItem.setReference(vehicleSlot.getRef());
                    dataItem.setName(VehicleSlotsHelper.getVehicleName(vehicleSlot));
                    dataItem.setCarId(vehicleSlot.getCarIdentifier());
                    dataItem.setModded(SecurityOptions.INSTALLED == vehicleSlot.getSecurityOptions().getOptionOne());

                    return dataItem;
                })

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
}
