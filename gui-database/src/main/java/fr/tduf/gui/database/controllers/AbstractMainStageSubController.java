package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.util.Deque;
import java.util.List;
import java.util.Map;

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
        this.mainStageController.setMiner(miner);
    }

    protected MainStageViewDataController getViewDataController() {
        return mainStageController.getViewDataController();
    }

    protected MainStageChangeDataController getChangeDataController() {
        return mainStageController.getChangeDataController();
    }

    protected FieldsBrowserStageController getFieldsBrowserStageController() {
        return mainStageController.getFieldsBrowserStageController();
    }

    protected EntriesStageController getEntriesStageController() {
        return mainStageController.getEntriesStageController();
    }

    protected void setLayoutObject(EditorLayoutDto editorLayoutDto) {
        mainStageController.layoutObject = editorLayoutDto;
    }

    protected EditorLayoutDto getLayoutObject() {
        return mainStageController.layoutObject;
    }

    protected EditorLayoutDto.EditorProfileDto getCurrentProfileObject() {
        return mainStageController.profileObject;
    }

    protected List<DbDto> getDatabaseObjects() {
        // Getter kept for testing
        return mainStageController.getDatabaseObjects();
    }

    protected DbDto getCurrentTopicObject() {
        return mainStageController.currentTopicObject;
    }

    protected Deque<EditorLocation> getNavigationHistory() {
        return mainStageController.navigationHistory;
    }

    protected ApplicationConfiguration getApplicationConfiguration() {
        // Getter kept for testing
        return mainStageController.getApplicationConfiguration();
    }

    protected ObservableList<ContentEntryDataItem> getBrowsableEntries() {
        return mainStageController.browsableEntries;
    }

    protected long getCurrentEntryIndex() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryIndex();
    }

    protected DbDto.Topic getCurrentTopic() {
        // Getter kept for testing
        return mainStageController.getCurrentTopic();
    }

    protected Property<Long> currentEntryIndexProperty() {
        return mainStageController.currentEntryIndexProperty;
    }

    protected SimpleStringProperty currentEntryLabelProperty() {
        return mainStageController.currentEntryLabelProperty;
    }

    protected Property<DbDto.Topic> currentTopicProperty() {
        return mainStageController.currentTopicProperty;
    }

    protected Property<Locale> currentLocaleProperty() {
        return mainStageController.currentLocaleProperty;
    }

    protected Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank() {
        return mainStageController.rawValuePropertyByFieldRank;
    }

    protected Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank() {
        return mainStageController.resolvedValuePropertyByFieldRank;
    }

    protected Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> getResourceListByTopicLink() {
        return mainStageController.resourceListByTopicLink;
    }

    protected TextField getDatabaseLocationTextField() {
        return mainStageController.databaseLocationTextField;
    }

    protected ChoiceBox<String> getProfilesChoiceBox() {
        return mainStageController.profilesChoiceBox;
    }

    protected ChoiceBox<Locale> getLocalesChoiceBox() {
        return mainStageController.localesChoiceBox;
    }

    protected Button getLoadDatabaseButton() {
        return mainStageController.loadDatabaseButton;
    }

    protected TabPane getTabPane() {
        return mainStageController.tabPane;
    }
}
