package fr.tduf.gui.database.controllers;


import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.database.controllers.main.MainStageController;

/**
 * Java FX parent controller for all Database Editor controllers
 */
public abstract class AbstractEditorController extends AbstractGuiController {
    protected MainStageController mainStageController;

    public void setMainStageController(MainStageController mainStageController) {
        this.mainStageController = mainStageController;
    }
}
