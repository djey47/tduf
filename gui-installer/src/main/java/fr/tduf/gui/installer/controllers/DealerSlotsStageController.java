package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.DealerHelper;
import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;


public class DealerSlotsStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = VehicleSlotsStageController.class.getSimpleName();

    private Property<DealerSlotData.DealerDataItem> selectedDealerProperty;
    private Property<DealerSlotData.SlotDataItem> selectedSlotProperty;

    private Optional<DealerSlotData> returnedSlot;

    private ObservableList<DealerSlotData.DealerDataItem> dealersData = FXCollections.observableArrayList();
    private ObservableList<DealerSlotData.SlotDataItem> slotsData = FXCollections.observableArrayList();

    private DealerHelper dealerHelper;


    @FXML
    private ChoiceBox<DealerHelper.DealerKind> dealerKindFilterChoiceBox;

    @FXML
    private TextField dealerRefTextField;

    @FXML
    private TextField slotTextField;

    @FXML
    private TableView<DealerSlotData.DealerDataItem> dealersTableView;

    @FXML
    private TableView<DealerSlotData.SlotDataItem> slotsTableView;

    @FXML
    private void handleOkButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOkButtonAction");

        if (selectedDealerProperty.getValue() == null
                || selectedSlotProperty.getValue() == null) {
            return;
        }

        returnedSlot = Optional.of(DealerSlotData.from(selectedDealerProperty.getValue(), selectedSlotProperty.getValue()));

        closeWindow();
    }

    @FXML
    private void handleNoSlotButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleNoSlotButtonAction");

        returnedSlot = empty();

        closeWindow();
    }

    @FXML
    private void handleDealersTableMouseClick(MouseEvent event) {
        Log.trace(THIS_CLASS_NAME, "->handleDealersTableMouseClick");

        TableViewHelper.getMouseSelectedItem(event, DealerSlotData.DealerDataItem.class)
                .ifPresent(item -> {
                    selectedDealerProperty.setValue(item);
                    updateSlotsData(item);
                });
    }

    @FXML
    private void handleSlotsTableMouseClick(MouseEvent event) {
        Log.trace(THIS_CLASS_NAME, "->handleSlotsTableMouseClick");

        TableViewHelper.getMouseSelectedItem(event, DealerSlotData.SlotDataItem.class)
                .ifPresent(selectedSlotProperty::setValue);
    }

    @Override
    public void init() throws IOException {
        initHeaderPane();

        initTablePane();
    }

    public Optional<DealerSlotData> initAndShowModalDialog(BulkDatabaseMiner miner) throws Exception {
        requireNonNull(miner, "Database miner instance is required.");

        dealerHelper = DealerHelper.load(miner);

        updateDealersData(DealerHelper.DealerKind.ALL);

        returnedSlot = null;

        showModalWindow();

        if (returnedSlot == null) {
            throw new Exception(DisplayConstants.MESSAGE_ABORTED_USER);
        }

        return returnedSlot;
    }

    private void initHeaderPane() {
        selectedDealerProperty = new SimpleObjectProperty<>();
        selectedSlotProperty = new SimpleObjectProperty<>();

        dealerRefTextField.textProperty().bindBidirectional(selectedDealerProperty, new StringConverter<DealerSlotData.DealerDataItem>() {
            @Override
            public String toString(DealerSlotData.DealerDataItem dealerItem) {
                return ofNullable(dealerItem)
                        .map(item -> item.referenceProperty().get())
                        .orElse("");
            }

            @Override
            public DealerSlotData.DealerDataItem fromString(String ref) {
                if (StringUtils.isEmpty(ref)) {
                    return null;
                }

                return DealerSlotData.DealerDataItem.fromDealer(Dealer.builder().withRef(ref).build());
            }
        });

        slotTextField.textProperty().bindBidirectional(selectedSlotProperty, new StringConverter<DealerSlotData.SlotDataItem>() {
            @Override
            public String toString(DealerSlotData.SlotDataItem slotItem) {
                return ofNullable(slotItem)
                        .map(item -> item.rankProperty().get())
                        .map(Integer::valueOf)
                        .map(Object::toString)
                        .orElse("");
            }

            @Override
            public DealerSlotData.SlotDataItem fromString(String rank) {
                if (StringUtils.isEmpty(rank)) {
                    return null;
                }

                return DealerSlotData.SlotDataItem.fromDealerSlot(Dealer.Slot.builder().withRank(Integer.valueOf(rank)).build());
            }
        });

        dealerKindFilterChoiceBox.setConverter(new StringConverter<DealerHelper.DealerKind>() {
            @Override
            public String toString(DealerHelper.DealerKind dealerKind) {
                return dealerKind.getLabel();
            }

            @Override
            public DealerHelper.DealerKind fromString(String label) {
                return null;
            }
        });
        dealerKindFilterChoiceBox.getItems().addAll(asList(DealerHelper.DealerKind.values()));
        dealerKindFilterChoiceBox.setValue(DealerHelper.DealerKind.ALL);
        dealerKindFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return;
            }
            updateDealersData(newValue);
        });
    }

    private void initTablePane() {
        TableColumn<DealerSlotData.DealerDataItem, ?> refColumn = dealersTableView.getColumns().get(0);
        refColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().referenceProperty());
        TableColumn<DealerSlotData.DealerDataItem, ?> nameColumn = dealersTableView.getColumns().get(1);
        nameColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().nameProperty());
        TableColumn<DealerSlotData.DealerDataItem, ?> locationColumn = dealersTableView.getColumns().get(2);
        locationColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().locationProperty());
        TableColumn<DealerSlotData.DealerDataItem, ?> freeSlotsColumn = dealersTableView.getColumns().get(3);
        freeSlotsColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().freeSlotsProperty());

        dealersTableView.setItems(dealersData);

        TableColumn<DealerSlotData.SlotDataItem, ?> rankColumn = slotsTableView.getColumns().get(0);
        rankColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().rankProperty());
        TableColumn<DealerSlotData.SlotDataItem, ?> vehicleNameColumn = slotsTableView.getColumns().get(1);
        vehicleNameColumn.setCellValueFactory(cellData -> (ObservableValue) cellData.getValue().vehicleNameProperty());

        slotsTableView.setItems(slotsData);
    }

    private void updateDealersData(DealerHelper.DealerKind selectedDealerKind) {
        dealersData.clear();

        dealersData.addAll(dealerHelper.getDealers(selectedDealerKind).stream()
                .map(DealerSlotData.DealerDataItem::fromDealer)
                .sorted((item1, item2) -> item1.nameProperty().get().compareTo(item2.nameProperty().get()))
                .collect(toList()));
    }

    private void updateSlotsData(DealerSlotData.DealerDataItem dealerDataItem) {
        slotsData.clear();

        slotsData.addAll(dealerDataItem.slotsProperty().get().stream()

                .map(DealerSlotData.SlotDataItem::fromDealerSlot)

                .collect(toList()));
    }
}
