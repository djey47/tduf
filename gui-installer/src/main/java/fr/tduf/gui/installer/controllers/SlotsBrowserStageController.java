package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.common.helper.javafx.CommonDialogsHelper;
import fr.tduf.gui.common.helper.javafx.TableViewHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SlotsBrowserStageController extends AbstractGuiController {

    private static final String THIS_CLASS_NAME = SlotsBrowserStageController.class.getSimpleName();

    private static final int FIELD_RANK_MANUFACTURER_NAME = 3;  // BRANDS

    private static final int FIELD_RANK_CAR_BRAND = 2;          // CAR PHYSICS
    private static final int FIELD_RANK_CAR_REAL_NAME = 12;
    private static final int FIELD_RANK_CAR_MODEL_NAME = 13;
    private static final int FIELD_RANK_CAR_VERSION_NAME = 14;

    private static final String RESOURCE_VALUE_NONE = "??";

    @FXML
    private Label instructionsLabel;

    @FXML
    TableView<VehicleSlotDataItem> slotsTableView;

    private BulkDatabaseMiner miner;

    private ObservableList<VehicleSlotDataItem> slotsData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private Optional<VehicleSlotDataItem> selectedSlot;

    @Override
    public void init() {
        initHeaderPane();

        initTablePane();
    }

    @FXML
    private void handleSlotsTableMouseClick(MouseEvent mouseEvent) {
        Log.trace(THIS_CLASS_NAME, "->handleSlotsTableMouseClick");

        if (MouseButton.PRIMARY == mouseEvent.getButton()) {
            final Optional<VehicleSlotDataItem> potentialMouseSelectedItem = TableViewHelper.getMouseSelectedItem(mouseEvent, VehicleSlotDataItem.class);
            if (potentialMouseSelectedItem.isPresent()) {
                selectedSlot = potentialMouseSelectedItem;
            }

            closeWindow();
        }
    }

    @FXML
    private void handleSearchSlotButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleSearchSlotButtonAction");

        askForReferenceAndSelectItem();
    }

    /**
     * Creates and display dialog.
     * @param potentialSlotReference    : slot reference to be selected (optional)
     * @param miner                     : instance of database miner to parse contents
     * @return selected item, if any.
     */
    public Optional<VehicleSlotDataItem> initAndShowModalDialog(Optional<String> potentialSlotReference, BulkDatabaseMiner miner) {
        this.miner = requireNonNull(miner, "Database miner instance is required.");

        selectedSlot = Optional.empty();

        currentTopicProperty.setValue(CAR_PHYSICS_DATA);

        updateSlotsStageData();

        potentialSlotReference.ifPresent( this::selectEntryInTableAndScroll );

        showModalWindow();

        return selectedSlot;
    }

    private void initHeaderPane() {
        currentTopicProperty = new SimpleObjectProperty<>();
    }

    private void initTablePane() {
        TableColumn<VehicleSlotDataItem, ?> refColumn = slotsTableView.getColumns().get(0);
        refColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().referenceProperty());

        TableColumn<VehicleSlotDataItem, ?> nameColumn = slotsTableView.getColumns().get(1);
        nameColumn.setCellValueFactory((cellData) -> (ObservableValue) cellData.getValue().nameProperty());

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

        DbDto.Topic topic = currentTopicProperty.getValue();
        miner.getDatabaseTopic(topic)
                .ifPresent((topicObject) -> slotsData.addAll(topicObject.getData().getEntries().stream()

                                // TODO filter drivable slots (exclude traffic and default)

                                .map((entry) -> {
                                    VehicleSlotDataItem dataItem = new VehicleSlotDataItem();

                                    long entryInternalIdentifier = entry.getId();
                                    dataItem.setInternalEntryId(entryInternalIdentifier);

                                    String slotName = computeVehicleName(topic, entryInternalIdentifier);
                                    dataItem.setName(slotName);

                                    String slotReference = miner.getContentEntryReferenceWithInternalIdentifier(entryInternalIdentifier, topic).get();
                                    dataItem.setReference(slotReference);

                                    return dataItem;
                                })

                                .collect(toList()))
                );
    }

    // TODO move to high level library
    private String computeVehicleName(DbDto.Topic topic, long entryInternalIdentifier) {

        return miner.getContentEntryFromTopicWithInternalIdentifier(entryInternalIdentifier, topic)

                .map((entry) -> {

                    final DbResourceDto.Locale defaultLocale = UNITED_STATES;

                    final String realName = miner.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, FIELD_RANK_CAR_REAL_NAME, entryInternalIdentifier, defaultLocale)

                            .map(DbResourceDto.Entry::getValue)

                            .orElse(RESOURCE_VALUE_NONE);

                    if (!RESOURCE_VALUE_NONE.equals(realName)) {
                        return realName;
                    }

                    final String brandName = miner.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, FIELD_RANK_CAR_BRAND, entryInternalIdentifier, BRANDS)

                            .flatMap((brandsEntry) -> miner.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, FIELD_RANK_MANUFACTURER_NAME, brandsEntry.getId(), defaultLocale))

                            .map(DbResourceDto.Entry::getValue)

                            .map((resourceValue) -> RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                            .orElse("");

                    final String modelName = miner.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, FIELD_RANK_CAR_MODEL_NAME, entryInternalIdentifier, defaultLocale)

                            .map(DbResourceDto.Entry::getValue)

                            .map((resourceValue) -> RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                            .orElse("");

                    final String versionName = miner.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, FIELD_RANK_CAR_VERSION_NAME, entryInternalIdentifier, defaultLocale)

                            .map(DbResourceDto.Entry::getValue)

                            .map((resourceValue) -> RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                            .orElse("");

                    return String.format("%s %s %s", brandName, modelName, versionName).trim();
                })

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
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
