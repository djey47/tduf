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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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
        mainStageController.setMiner(miner);
    }

    protected MainStageViewDataController getViewDataController() {
        return mainStageController.getViewData();
    }

    protected MainStageChangeDataController getChangeDataController() {
        return mainStageController.getChangeData();
    }

    protected FieldsBrowserStageController getFieldsBrowserStageController() {
        return mainStageController.getFieldsBrowserStageController();
    }

    protected EntriesStageController getEntriesStageController() {
        return mainStageController.getEntriesStageController();
    }

    protected void setLayoutObject(EditorLayoutDto editorLayoutDto) {
        mainStageController.setLayoutObject(editorLayoutDto);
    }

    protected EditorLayoutDto getLayoutObject() {
        // Getter kept for testing
        return mainStageController.getLayoutObject();
    }

    protected EditorLayoutDto.EditorProfileDto getCurrentProfileObject() {
        // Getter kept for testing
        return mainStageController.getCurrentProfileObject();
    }

    protected void setCurrentProfileObject(EditorLayoutDto.EditorProfileDto profileObject) {
        // Setter kept for testing
        mainStageController.setCurrentProfileObject(profileObject);
    }

    protected List<DbDto> getDatabaseObjects() {
        // Getter kept for testing
        return mainStageController.getDatabaseObjects();
    }

    protected DbDto getCurrentTopicObject() {
        return mainStageController.getCurrentTopicObject();
    }

    protected void setCurrentTopicObject(DbDto currentTopicObject) {
        // Setter kept for testing
        mainStageController.setCurrentTopicObject(currentTopicObject);
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

    protected Property<Long> currentEntryIndexProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryIndexProperty();
    }

    protected Property<DbDto.Topic> currentTopicProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentTopicProperty();
    }

    protected Property<Locale> currentLocaleProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentLocaleProperty();
    }

    protected SimpleStringProperty currentEntryLabelProperty() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryLabelProperty();
    }

    protected Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> getResourceListByTopicLink() {
        // Getter kept for testing
        return mainStageController.getResourceListByTopicLink();
    }

    protected Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank() {
        return mainStageController.resolvedValuePropertyByFieldRank;
    }

    protected TextField getDatabaseLocationTextField() {
        return mainStageController.databaseLocationTextField;
    }

    protected ChoiceBox<String> getProfilesChoiceBox() {
        // Getter kept for testing
        return mainStageController.getProfilesChoiceBox();
    }

    protected Label getCreditsLabel() {
        return mainStageController.creditsLabel;
    }

    protected TitledPane getSettingsPane() {
        return mainStageController.settingsPane;
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

    protected VBox getDefaultTab() {
        return mainStageController.getDefaultTab();
    }
}
