package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

import java.util.Deque;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all sub controllers
 */
abstract class AbstractMainStageSubController {
    private final MainStageController mainStageController;

    protected AbstractMainStageSubController(MainStageController mainStageControllerInstance) {
        requireNonNull(mainStageControllerInstance, "Main stage controller is required.");
        this.mainStageController = mainStageControllerInstance;
    }

    protected BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }

    protected void setMiner(BulkDatabaseMiner miner) {
        mainStageController.setMiner(miner);
    }

    protected MainStageViewDataController getViewDataController() {
        return mainStageController.getViewData();
    }

    protected FieldsBrowserStageController getFieldsBrowserStageController() {
        return mainStageController.getFieldsBrowserStageController();
    }

    protected EntriesStageController getEntriesStageController() {
        return mainStageController.getEntriesStageController();
    }

    protected ResourcesStageController getResourcesStageController() {
        return mainStageController.getResourcesStageController();
    }

    protected void setLayoutObject(EditorLayoutDto editorLayoutDto) {
        mainStageController.setLayoutObject(editorLayoutDto);
    }

    protected EditorLayoutDto getLayoutObject() {
        // Getter kept for testing
        return mainStageController.getLayoutObject();
    }

    protected List<DbDto> getDatabaseObjects() {
        // Getter kept for testing
        return mainStageController.getDatabaseObjects();
    }

    protected DbDto getCurrentTopicObject() {
        return mainStageController.getCurrentTopicObject();
    }

    protected DbDto.Topic getCurrentTopic() {
        if (mainStageController.getCurrentTopicObject() == null) {
            return null;
        }
        return mainStageController.getCurrentTopicObject().getTopic();
    }

    protected void setCurrentTopicObject(DbDto currentTopicObject) {
        // Setter kept for testing
        mainStageController.setCurrentTopicObject(currentTopicObject);
    }

    protected Deque<EditorLocation> getNavigationHistory() {
        return mainStageController.getNavigationHistory();
    }

    protected ApplicationConfiguration getApplicationConfiguration() {
        // Getter kept for testing
        return mainStageController.getApplicationConfiguration();
    }

    protected int getCurrentEntryIndex() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryIndex();
    }

    protected Property<Integer> currentEntryIndexProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryIndexProperty();
    }

    protected Property<DbDto.Topic> currentTopicProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentTopicProperty();
    }

    protected StringProperty currentEntryLabelProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryLabelProperty();
    }

    protected TextField getDatabaseLocationTextField() {
        return mainStageController.getDatabaseLocationTextField();
    }

    protected ChoiceBox<String> getProfilesChoiceBox() {
        // Getter kept for testing
        return mainStageController.getProfilesChoiceBox();
    }

    protected Label getCreditsLabel() {
        return mainStageController.creditsLabel;
    }

    protected TitledPane getSettingsPane() {
        return mainStageController.getSettingsPane();
    }

    protected ChoiceBox<Locale> getLocalesChoiceBox() {
        return mainStageController.getLocalesChoiceBox();
    }

    protected TabPane getTabPane() {
        return mainStageController.getTabPane();
    }

    protected Label getEntryItemsCountLabel() {
        return mainStageController.entryItemsCountLabel;
    }

    protected  Label getCurrentTopicLabel() {
        return mainStageController.currentTopicLabel;
    }

    protected TextField getEntryNumberTextField() {
        return mainStageController.entryNumberTextField;
    }

    protected ComboBox<ContentEntryDataItem> getEntryNumberComboBox() {
        return mainStageController.entryNumberComboBox;
    }
}
