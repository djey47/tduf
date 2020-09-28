package fr.tduf.gui.database.controllers.main;

import fr.tduf.gui.database.controllers.*;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.plugins.common.PluginHandler;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Deque;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Parent of all sub controllers, providing common resources to them.
 */
abstract class AbstractMainStageSubController {
    private final MainStageController mainStageController;

    protected AbstractMainStageSubController(MainStageController mainStageControllerInstance) {
        requireNonNull(mainStageControllerInstance, "Main stage controller is required.");
        this.mainStageController = mainStageControllerInstance;
    }

    /**
     * @see MainStageController#notifyActionTermination(javafx.scene.control.Alert.AlertType, java.lang.String, java.lang.String, java.lang.String)
     */
    protected void notifyActionTermination(Alert.AlertType alertType, String subTitle, String message, String description) {
        mainStageController.notifyActionTermination(alertType, subTitle, message, description);
    }

    /**
     * @see MainStageController#fixDatabase()
     */
    protected void fixDatabase() {
        mainStageController.fixDatabase();
    }

    /**
     * @see MainStageViewDataController#refreshAll()
     */
    protected void refreshAllViewComponents() {
        getViewDataController().refreshAll();
    }

    /**
     * @see MainStageViewDataController#updateViewComponentsForContentItem(int, ContentItemDto)
     */
    protected void updateViewComponentsForContentItem(int entryIndex, ContentItemDto updatedItem) {
        getViewDataController().updateViewForContentItem(entryIndex, updatedItem);
    }

    /**
     * @see MainStageViewDataController#updateViewComponentsForContentItem(int, ContentItemDto)
     */
    protected void updateAllPropertiesForLink(TopicLinkDto topicLinkObject) {
        getViewDataController().updateAllLinkProperties(topicLinkObject);
    }

    /**
     * @see MainStageController#initAfterDatabaseLoading()
     */
    protected void initAfterDatabaseLoading() {
        mainStageController.initAfterDatabaseLoading();
    }

    /**
     * @see MainStageViewDataController#toggleSplashImage(boolean)
     */
    protected void toggleSplashImage(boolean isSplashOn) {
        getViewDataController().toggleSplashImage(isSplashOn);
    }

    protected AnchorPane getRoot() {
        return mainStageController.root;
    }

    protected VBox getMainVBox() {
        return mainStageController.getMainVBox();
    }

    protected BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
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

    protected DbStructureDto getCurrentStructure() {
        if (mainStageController.getCurrentTopicObject() == null) {
            return null;
        }
        return mainStageController.getCurrentTopicObject().getStructure();
    }

    protected DbDto.Topic getCurrentTopic() {
        return mainStageController.currentTopicProperty().getValue();
    }

    protected Deque<EditorLocation> getNavigationHistory() {
        return mainStageController.getNavigationHistory();
    }

    protected Label getCreditsLabel() {
        return mainStageController.creditsLabel;
    }

    protected ObjectProperty<Cursor> mouseCursorProperty() {
        return mainStageController.mouseCursorProperty();
    }

    protected TextField getDatabaseLocationTextField() {
        return mainStageController.getDatabaseLocationTextField();
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

    protected Label getCurrentTopicLabel() {
        return mainStageController.currentTopicLabel;
    }

    protected TextField getEntryNumberTextField() {
        return mainStageController.entryNumberTextField;
    }

    protected ComboBox<ContentEntryDataItem> getEntryNumberComboBox() {
        return mainStageController.entryNumberComboBox;
    }

    protected Label getUnfilteredEntryItemsCountLabel() {
        return mainStageController.unfilteredEntryItemsCountLabel;
    }

    protected Label getFilteredEntryItemsCountLabel() {
        return mainStageController.filteredEntryItemsCountLabel;
    }

    protected TextField getEntryFilterTextField() {
        return mainStageController.getEntryFilterTextField();
    }

    protected Button getEntryEmptyFilterButton() {
        return mainStageController.getEntryEmptyFilterButton();
    }

    protected Button getEntryFilterButton() {
        return mainStageController.getEntryFilterButton();
    }

    protected BooleanProperty getRunningServiceProperty() {
        return mainStageController.runningServiceProperty();
    }

    protected Window getWindow() {
        return mainStageController.getWindow();
    }

    protected PluginHandler getPluginHandler() {
        return mainStageController.getPluginHandler();
    }

    protected HBox getMainSplashHBox() {
        return mainStageController.getMainSplashBox();
    }

    protected ImageView getMainSplashImage() {
        return mainStageController.getMainSplashImage();
    }

    void setCurrentTopicObject(DbDto currentTopicObject) {
        // Setter kept for testing
        mainStageController.setCurrentTopicObject(currentTopicObject);
    }

    ApplicationConfiguration getApplicationConfiguration() {
        // Getter kept for testing
        return mainStageController.getApplicationConfiguration();
    }

    int getCurrentEntryIndex() {
        // Getter kept for testing
        return mainStageController.getCurrentEntryIndex();
    }

    Property<Integer> currentEntryIndexProperty() {
        // Getter kept for testing
        return mainStageController.currentEntryIndexProperty();
    }

    Property<DbDto.Topic> currentTopicProperty() {
        // Getter kept for testing
        return mainStageController.currentTopicProperty();
    }

    StringProperty currentEntryLabelProperty() {
        // Getter kept for testing
        return mainStageController.currentEntryLabelProperty();
    }

    BooleanProperty modifiedProperty() {
        // Getter kept for testing
        return mainStageController.modifiedProperty();
    }

    ChoiceBox<EditorLayoutDto.EditorProfileDto> getProfilesChoiceBox() {
        // Getter kept for testing
        return mainStageController.getProfilesChoiceBox();
    }

    EditorLayoutDto getLayoutObject() {
        // Getter kept for testing
        return mainStageController.getLayoutObject();
    }

    List<DbDto> getDatabaseObjects() {
        // Getter kept for testing
        return mainStageController.getDatabaseObjects();
    }

    private MainStageViewDataController getViewDataController() {
        return mainStageController.getViewData();
    }
}
