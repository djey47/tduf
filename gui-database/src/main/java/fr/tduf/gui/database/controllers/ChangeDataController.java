package fr.tduf.gui.database.controllers;

import static java.util.Objects.requireNonNull;

public class ChangeDataController {
    private final MainStageController mainStageController;

    ChangeDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }
}
