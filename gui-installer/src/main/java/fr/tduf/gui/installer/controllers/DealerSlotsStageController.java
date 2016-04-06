package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import javafx.fxml.FXML;

import java.io.IOException;

public class DealerSlotsStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = SlotsBrowserStageController.class.getSimpleName();

    @FXML
    private void handleOkButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleOkButtonAction");

        closeWindow();
    }

    @Override
    protected void init() throws IOException {

    }

    /**
     *
     * @throws Exception
     */
    public void initAndShowModalDialog() throws Exception {
        showModalWindow();
    }
}
