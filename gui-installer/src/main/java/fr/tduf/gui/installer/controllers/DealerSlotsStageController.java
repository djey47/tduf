package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class DealerSlotsStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = SlotsBrowserStageController.class.getSimpleName();

    private Property<DealerSlotData.DealerDataItem> selectedDealerProperty;
    private Property<DealerSlotData.SlotDataItem> selectedSlotProperty;

    private Optional<DealerSlotData> returnedSlot;

    private ObservableList<DealerSlotData.DealerDataItem> dealersData = FXCollections.observableArrayList();
    private ObservableList<DealerSlotData.SlotDataItem> slotsData = FXCollections.observableArrayList();

    private DealerHelper dealerHelper;


    @FXML
    private TextField dealerRefTextField;

    @FXML
    private TextField slotTextField;

    @FXML
    private TableView<DealerSlotData.DealerDataItem> dealersTableView;

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

    @Override
    public void init() throws IOException {
        initHeaderPane();

        initTablePane();
    }

    /**
     *
     * @throws Exception
     * @param miner
     */
    public Optional<DealerSlotData> initAndShowModalDialog(BulkDatabaseMiner miner) throws Exception {
        requireNonNull(miner, "Database miner instance is required.");

        dealerHelper = DealerHelper.load(miner);

        updateDealersData();

        showModalWindow();

        if (returnedSlot == null) {
            throw new Exception("Aborted by user.");
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
                        .map((item) -> item.referenceProperty().get())
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
                        .map((item) -> item.rankProperty().get())
                        .map(Integer::valueOf)
                        .map(Object::toString)
                        .orElse("");
            }

            @Override
            public DealerSlotData.SlotDataItem fromString(String rank) {
                if (StringUtils.isEmpty(rank)) {
                    return null;
                }

                return DealerSlotData.SlotDataItem.fromDealerSlot(Dealer.builder().withRef("").build(), Integer.valueOf(rank));
            }
        });
    }

    private void initTablePane() {
        TableColumn<DealerSlotData.DealerDataItem, ?> refColumn = dealersTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().referenceProperty());

        dealersTableView.setItems(dealersData);
    }

    private void updateDealersData() {
        dealersData.clear();

        dealersData.addAll(dealerHelper.getDealers().stream()

                .map(DealerSlotData.DealerDataItem::fromDealer)

                .collect(toList()));
    }
}
