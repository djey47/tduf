package fr.tduf.gui.installer.controllers;

import fr.tduf.gui.common.helper.javafx.AbstractGuiController;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.javafx.VehicleSlotDataItem;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DatabaseCheckStageController extends AbstractGuiController {

    @FXML
    private VBox errorPanel;

    private Set<IntegrityError> integrityErrors;

    private VehicleSlotsHelper vehicleSlotsHelper;

    private BulkDatabaseMiner miner;

    private ObservableList<VehicleSlotDataItem> slotsData = FXCollections.observableArrayList();

    private Property<DbDto.Topic> currentTopicProperty;

    private Property<Optional<VehicleSlotDataItem>> selectedSlotProperty;

    @Override
    public void init() {
        initHeaderPane();
    }

    /**
     * Creates and display dialog.
     */
    public void initAndShowModalDialog(Set<IntegrityError> integrityErrors) {

        this.integrityErrors = requireNonNull(integrityErrors, "A list of integrity errors is required.");
//        this.miner = requireNonNull(miner, "Database miner instance is required.");

//        vehicleSlotsHelper = VehicleSlotsHelper.load(miner);
//
//        selectedSlotProperty.setValue(empty());
//
//        currentTopicProperty.setValue(CAR_PHYSICS_DATA);
//
//        updateSlotsStageData();
//
//        potentialSlotReference.ifPresent(this::selectEntryInTableAndScroll);

        initErrorDetails();

        showModalWindow();
    }

    private void initHeaderPane() {
    }

    private void initErrorDetails() {

        integrityErrors
                .forEach((error) -> {

                    TextField textField = new TextField(error.getError());

                    errorPanel.getChildren().add(textField);

                });
    }
}
