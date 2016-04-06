package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.installer.domain.javafx.DealerSlotDataItem;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.Optional;

public class DealerSlotsStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = SlotsBrowserStageController.class.getSimpleName();

    private Property<DealerSlotDataItem.DealerDataItem> selectedDealerProperty;
    private Property<DealerSlotDataItem.SlotDataItem> selectedSlotProperty;

    private Optional<DealerSlotDataItem> returnedSlot;

    @FXML
    private void handleOkButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOkButtonAction");

        if (selectedDealerProperty.getValue() == null
                || selectedSlotProperty.getValue() == null) {
            return;
        }

        returnedSlot = Optional.of(DealerSlotDataItem.from(selectedDealerProperty.getValue(), selectedSlotProperty.getValue()));

        closeWindow();
    }

    @Override
    public void init() throws IOException {
        initHeaderPane();
    }

    /**
     *
     * @throws Exception
     */
    public Optional<DealerSlotDataItem> initAndShowModalDialog() throws Exception {
        showModalWindow();

        if (returnedSlot == null) {
            throw new Exception("Aborted by user.");
        }

        return returnedSlot;
    }

    private void initHeaderPane() {
        selectedDealerProperty = new SimpleObjectProperty<>();
        selectedSlotProperty = new SimpleObjectProperty<>();
    }
}
