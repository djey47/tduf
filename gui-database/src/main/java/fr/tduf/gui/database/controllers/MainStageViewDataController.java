package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.gui.common.AppConstants;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.helper.DynamicFieldControlsHelper;
import fr.tduf.gui.database.controllers.helper.DynamicLinkControlsHelper;
import fr.tduf.gui.database.converter.ContentEntryToStringConverter;
import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.ItemViewModel;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.factory.EntryCellFactory;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static fr.tduf.gui.common.ImageConstants.Resource.BOX_EMPTY_BLUE;
import static fr.tduf.gui.common.ImageConstants.Resource.MAGNIFIER_BLUE;
import static fr.tduf.gui.common.ImageConstants.SIZE_BUTTON_PICTO;
import static fr.tduf.gui.database.common.SupportConstants.LOG_TARGET_PROFILE_NAME;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.*;

/**
 * Specialized controller to display database contents.
 */
public class MainStageViewDataController extends AbstractMainStageSubController {
    private static final Class<MainStageViewDataController> thisClass = MainStageViewDataController.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private static final String MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC = "No database object for topic: ";

    private static final Predicate<ContentEntryDataItem> PREDICATE_DEFAULT_ENTRY_FILTER = (ContentEntryDataItem item) -> true;

    final Property<Locale> currentLocaleProperty = new SimpleObjectProperty<>(SettingsConstants.DEFAULT_LOCALE);
    final Property<EditorLayoutDto.EditorProfileDto> currentProfileProperty = new SimpleObjectProperty<>();

    private final DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private final DynamicLinkControlsHelper dynamicLinkControlsHelper;

    private final ObservableList<ContentEntryDataItem> browsableEntries = FXCollections.observableArrayList();

    private final FilteredList<ContentEntryDataItem> filteredEntries = browsableEntries.filtered(PREDICATE_DEFAULT_ENTRY_FILTER);

    private final Map<String, VBox> tabContentByName = new HashMap<>();
    private final Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourcesByTopicLink = new HashMap<>();

    private final ItemViewModel itemPropsByFieldRank = new ItemViewModel();

    MainStageViewDataController(MainStageController mainStageController) {
        super(mainStageController);

        dynamicFieldControlsHelper = new DynamicFieldControlsHelper(mainStageController);
        dynamicLinkControlsHelper = new DynamicLinkControlsHelper(mainStageController);
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButtonForLinkedTopic clicked, targetTopic:" + targetTopic + LOG_TARGET_PROFILE_NAME + targetProfileName);

            switchToSelectedResourceForLinkedTopic(tableViewSelectionModel.getSelectedItem(), targetTopic, targetProfileName);
        };
    }

    public EventHandler<MouseEvent> handleLinkTableMouseClick(String targetProfileName, DbDto.Topic targetTopic) {
        return mouseEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleLinkTableMouseClick, targetProfileName:" + targetProfileName + ", targetTopic:" + targetTopic);

            if (MouseButton.PRIMARY == mouseEvent.getButton()
                    && mouseEvent.getClickCount() == 2) {
                TableViewHelper.getMouseSelectedItem(mouseEvent, ContentEntryDataItem.class)
                        .ifPresent(selectedResource -> switchToSelectedResourceForLinkedTopic(selectedResource, targetTopic, targetProfileName));
            }
        };
    }

    public EventHandler<ActionEvent> handleBrowseResourcesButtonMouseClick(DbDto.Topic targetTopic, StringProperty targetReferenceProperty, int fieldRank) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->browseResourcesButton clicked");

            getResourcesStageController().initAndShowDialog(targetReferenceProperty, fieldRank, getLocalesChoiceBox().getValue(), targetTopic);
        };
    }

    public EventHandler<ActionEvent> handleBrowseEntriesButtonMouseClick(DbDto.Topic targetTopic, List<Integer> labelFieldRanks, StringProperty targetEntryReferenceProperty, int fieldRank) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->browseEntriesButton clicked");

            getEntriesStageController().initAndShowDialog(targetEntryReferenceProperty.get(), fieldRank, targetTopic, labelFieldRanks);
        };
    }

    public EventHandler<ActionEvent> handleGotoReferenceButtonMouseClick(DbDto.Topic targetTopic, int fieldRank, String targetProfileName) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->gotoReferenceButton clicked, targetTopic:" + targetTopic + LOG_TARGET_PROFILE_NAME + targetProfileName);

            switchToProfileAndRemoteEntry(targetProfileName, currentEntryIndexProperty().getValue(), fieldRank, currentTopicProperty().getValue(), targetTopic);
        };
    }

    /**
     * @return initial database directory
     */
    Optional<String> initSubController() throws IOException {
        initGUIComponentsProperties();

        initGUIComponentsGraphics();

        initTopToolbar();

        Optional<String> initialDatabaseDirectory = resolveInitialDatabaseDirectory();
        initSettingsPane(
                initialDatabaseDirectory.orElse(SettingsConstants.DATABASE_DIRECTORY_DEFAULT),
                (observable, oldValue, newValue) -> handleLocaleChoiceChanged(newValue),
                (observable, oldValue, newValue) -> handleProfileChoiceChanged(newValue));

        initTopicEntryHeaderPane(
                (observable, oldValue, newValue) -> handleEntryChoiceChanged(newValue));

        initStatusBar();

        return initialDatabaseDirectory;
    }

    void initGUIComponentsGraphics() {
        Image filterImage = new Image(MAGNIFIER_BLUE.getStream(), SIZE_BUTTON_PICTO, SIZE_BUTTON_PICTO, true, true);
        getEntryFilterButton().setGraphic(new ImageView(filterImage));

        Image emptyFilterImage = new Image(BOX_EMPTY_BLUE.getStream(), SIZE_BUTTON_PICTO, SIZE_BUTTON_PICTO, true, true);
        getEntryEmptyFilterButton().setGraphic(new ImageView(emptyFilterImage));
    }

    void initTopToolbar() {
        getCreditsLabel().setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    void initSettingsPane(String databaseDirectory, ChangeListener<Locale> localeChangeListener, ChangeListener<EditorLayoutDto.EditorProfileDto> profileChangeListener) throws IOException {
        getSettingsPane().setExpanded(false);

        loadAndFillLocales(localeChangeListener);

        loadAndFillProfiles(profileChangeListener);

        getDatabaseLocationTextField().setText(databaseDirectory);
    }

    void initTopicEntryHeaderPane(ChangeListener<ContentEntryDataItem> entryChangeListener) {
        getCurrentTopicLabel().textProperty().bindBidirectional(currentTopicProperty(), new DatabaseTopicToStringConverter());

        ComboBox<ContentEntryDataItem> entrySelectorComboBox = getEntryNumberComboBox();
        entrySelectorComboBox.setItems(filteredEntries);
        ContentEntryToStringConverter converter = new ContentEntryToStringConverter(browsableEntries, currentEntryIndexProperty(), currentEntryLabelProperty());
        entrySelectorComboBox.setConverter(converter);
        bindBidirectional(currentEntryLabelProperty(), entrySelectorComboBox.valueProperty(), converter);
        entrySelectorComboBox.getSelectionModel().selectedItemProperty()
                .addListener(entryChangeListener);
        entrySelectorComboBox.setCellFactory(new EntryCellFactory());
        entrySelectorComboBox.setVisibleRowCount(15);

        getFilteredEntryItemsCountLabel().textProperty().bind(size(filteredEntries).asString());
        getUnfilteredEntryItemsCountLabel().textProperty().bind(size(browsableEntries).asString(DisplayConstants.LABEL_ITEM_ENTRY_COUNT));
    }

    void initStatusBar() {
        getEntryNumberTextField().textProperty().bindBidirectional(currentEntryIndexProperty(), new CurrentEntryIndexToStringConverter());
        getEntryItemsCountLabel().textProperty().bind(size(browsableEntries).asString(DisplayConstants.LABEL_ITEM_ENTRY_COUNT));
    }

    void updateDisplayWithLoadedObjects() {
        getNavigationHistory().clear();

        switchToInitialProfile();

        updateConfiguration();
    }

    void updateBrowsableEntryLabel(int internalEntryId) {
        browsableEntries.stream()
                .filter(entry -> entry.internalEntryIdProperty().get() == internalEntryId)
                .findAny()
                .ifPresent(entry -> {
                    final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                            currentProfileProperty.getValue().getName(),
                            getLayoutObject());

                    String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(internalEntryId, getCurrentTopic(), currentLocaleProperty.getValue(), labelFieldRanks, getMiner(), getLayoutObject());
                    entry.setValue(entryValue);
                });
    }

    void applyProfile(EditorLayoutDto.EditorProfileDto profile) {
        final DbDto.Topic topic = profile.getTopic();
        final DbDto currentTopicObject = getMiner().getDatabaseTopic(topic)
                .orElseThrow(() -> new IllegalStateException(MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC + topic));

        currentTopicProperty().setValue(topic);

        currentProfileProperty.setValue(profile);
        setCurrentTopicObject(currentTopicObject);

        refreshAll();

        updateConfiguration();
    }

    void applySelectedLocale() {
        updateAllPropertiesWithItemValues();
        updateConfiguration();
    }

    void refreshAll() {
        itemPropsByFieldRank.clear();
        resourcesByTopicLink.clear();
        currentEntryIndexProperty().setValue(0);

        fillBrowsableEntries();

        initTabPane();
    }

    void updateAllPropertiesWithItemValues() {
        updateCurrentEntryLabelProperty();

        int entryIndex = currentEntryIndexProperty().getValue();
        DbDto.Topic currentTopic = currentTopicProperty().getValue();
        getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, currentTopic)
                .ifPresent(entry -> entry.getItems().forEach(this::updateItemProperties));

        resourcesByTopicLink.entrySet().forEach(this::updateLinkProperties);
    }

    void updateItemProperties(ContentItemDto item) {
        final int fieldRank = item.getFieldRank();
        final StringProperty rawValueProperty =  itemPropsByFieldRank.rawValuePropertyAtFieldRank(fieldRank);
        if (rawValueProperty == null) {
            return;
        }
        rawValueProperty.set(item.getRawValue());

        if (itemPropsByFieldRank.resolvedValuePropertyAtFieldRank(fieldRank) == null) {
            return;
        }

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, getCurrentTopicObject().getStructure().getFields());
        if (REFERENCE == structureField.getFieldType()) {
            updateReferenceProperties(item, structureField);
        } else if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }
    }

    void updateAllLinkProperties(TopicLinkDto topicLinkObject) {
        resourcesByTopicLink.entrySet().stream()
                .filter(mapEntry -> mapEntry.getKey().equals(topicLinkObject))
                .findAny()
                .ifPresent(this::updateLinkProperties);
    }

    void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<ContentEntryDataItem> values = remoteEntry.getValue();
        values.clear();

        final int currentEntryIndex = currentEntryIndexProperty().getValue();
        String currentEntryRef = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndex, currentTopicProperty().getValue())
                .orElseThrow(() -> new IllegalStateException("No REF available for entry at id: " + currentEntryIndex));

        final DbDto.Topic linkTopic = linkObject.getTopic();
        DbDto linkedTopicObject = getMiner().getDatabaseTopic(linkTopic)
                .orElseThrow(() -> new IllegalStateException(MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC + linkTopic));

        linkedTopicObject.getData().getEntries().stream()
                .filter(contentEntry -> currentEntryRef.equals(contentEntry.getItemAtRank(1)
                        .orElseThrow(() -> new IllegalStateException("No content item at rank 1 for entry id: " + contentEntry.getId()))
                        .getRawValue())
                )
                .map(contentEntry -> fetchLinkResourceFromContentEntry(linkedTopicObject, contentEntry, linkObject))
                .forEach(values::add);

        if (values.isEmpty()) {
            itemPropsByFieldRank.clearItem(linkObject.getId());
        }
    }

    void updateEntriesAndSwitchTo(int entryIndex) {
        fillBrowsableEntries();
        int effectiveIndex = entryIndex;
        if (effectiveIndex < 0) {
            effectiveIndex = 0;
        }
        switchToContentEntry(effectiveIndex);
    }

    void switchToContentEntry(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= browsableEntries.size()) {
            return;
        }

        currentEntryIndexProperty().setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }

    void switchToNextEntry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex >= getBrowsableEntries().size() - 1) {
            return;
        }

        switchToContentEntry(++currentEntryIndex);
    }

    void switchToNext10Entry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        int lastEntryIndex = getBrowsableEntries().size() - 1;
        if (currentEntryIndex + 10 >= lastEntryIndex) {
            currentEntryIndex = lastEntryIndex;
        } else {
            currentEntryIndex += 10;
        }

        switchToContentEntry(currentEntryIndex);
    }

    void switchToPreviousEntry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex <= 0) {
            return;
        }

        switchToContentEntry(--currentEntryIndex);
    }

    void switchToPrevious10Entry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex - 10 < 0) {
            currentEntryIndex = 0;
        } else {
            currentEntryIndex -= 10;
        }

        switchToContentEntry(currentEntryIndex);
    }

    void switchToFirstEntry() {
        switchToContentEntry(0);
    }

    void switchToFirstFilteredEntry() {
        filteredEntries.stream()
                .findFirst()
                .map(item -> item.internalEntryIdProperty().get())
                .ifPresent(this::switchToContentEntry);
    }

    void switchToLastEntry() {
        switchToContentEntry(getBrowsableEntries().size() - 1);
    }

    void switchToEntryWithReference(String entryReference, DbDto.Topic topic) {
        getMiner().getContentEntryInternalIdentifierWithReference(entryReference, topic)
                .ifPresent(this::switchToContentEntry);
    }

    void switchToPreviousLocation() {
        Deque<EditorLocation> navigationHistory = getNavigationHistory();
        if (navigationHistory.isEmpty()) {
            return;
        }

        EditorLocation previousLocation = navigationHistory.pop();
        switchToProfileAndEntry(previousLocation.getProfileName(), previousLocation.getEntryId(), false);
        getTabPane().selectionModelProperty().get().select(previousLocation.getTabId());
    }

    List<String> selectEntriesFromTopic() {
        DbDto.Topic currentTopic = currentTopicProperty().getValue();

        if (DatabaseStructureQueryHelper.isUidSupportForTopic(currentTopic)) {
            String potentialEntryReference = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty().getValue(), currentTopic)
                    .orElse(null);
            final List<ContentEntryDataItem> selectedItems = getEntriesStageController().initAndShowModalDialogForMultiSelect(potentialEntryReference, currentTopic, currentProfileProperty.getValue().getName());

            return selectedItems.stream()
                    .map(item -> item.referenceProperty().get())
                    .collect(toList());
        }

        return new ArrayList<>(0);
    }

    List<String> selectFieldsFromTopic() {
        final DbDto.Topic currentTopic = currentTopicProperty().getValue();

        if (DatabaseStructureQueryHelper.isUidSupportForTopic(currentTopic)) {
            return getFieldsBrowserStageController().initAndShowModalDialog(currentTopic).stream()
                    .map(item -> Integer.valueOf(item.rankProperty().get()).toString())
                    .collect(toList());
        }

        return new ArrayList<>(0);
    }

    void updateCurrentEntryLabelProperty() {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                currentProfileProperty.getValue().getName(),
                getLayoutObject());
        String entryLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                currentEntryIndexProperty().getValue(),
                currentTopicProperty().getValue(),
                currentLocaleProperty.getValue(),
                labelFieldRanks,
                getMiner(),
                getLayoutObject());
        currentEntryLabelProperty().setValue(entryLabel);
    }

    Optional<String> resolveInitialDatabaseDirectory() {
        final Optional<String> pathParameter = DatabaseEditor.getCommandLineParameters().stream()
                .filter(p -> !p.startsWith(AppConstants.SWITCH_PREFIX))
                .findAny();

        if (pathParameter.isPresent()) {
            return pathParameter;
        }

        return getApplicationConfiguration().getDatabasePath()
                .map(Path::toString);
    }

    String fetchRemoteContentsWithEntryRef(int fieldRank, DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        OptionalInt potentialEntryId = getMiner().getContentEntryInternalIdentifierWithReference(remoteEntryReference, remoteTopic);
        if (potentialEntryId.isPresent()) {
            return DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    potentialEntryId.getAsInt(), remoteTopic,
                    currentLocaleProperty.getValue(),
                    remoteFieldRanks,
                    getMiner(),
                    getLayoutObject());
        }

        itemPropsByFieldRank.errorPropertyAtFieldRank(fieldRank)
                .set(true);
        itemPropsByFieldRank.errorMessagePropertyAtFieldRank(fieldRank)
                .set(DisplayConstants.TOOLTIP_ERROR_CONTENT_NOT_FOUND);

        return DisplayConstants.VALUE_FIELD_DEFAULT;
    }

    void applyEntryFilter() {
        String filterCriteria = getEntryFilterTextField().textProperty().getValueSafe().toLowerCase();
        if (filterCriteria.isEmpty()) {
            resetEntryFilter();
            return;
        }

        filteredEntries.setPredicate((ContentEntryDataItem item) -> {
            String entryRank = String.valueOf(item.internalEntryIdProperty().get() + 1);
            String entryLabel = item.valueProperty().get().toLowerCase();
            String entryReference = item.referenceProperty().get();

            return entryRank.equals(filterCriteria)
                    || entryLabel.contains(filterCriteria)
                    || entryReference.equals(filterCriteria);
        });

        switchToFirstFilteredEntry();
    }

    void resetEntryFilter() {
        getEntryFilterTextField().textProperty().setValue("");

        filteredEntries.setPredicate(PREDICATE_DEFAULT_ENTRY_FILTER);

        switchToFirstEntry();
    }

    private void handleProfileChoiceChanged(EditorLayoutDto.EditorProfileDto newProfile) {
        Log.trace(THIS_CLASS_NAME, "->handleProfileChoiceChanged: " + newProfile);

        if (newProfile == null || getDatabaseObjects().isEmpty()) {
            return;
        }

        applyProfile(newProfile);
    }

    private void handleLocaleChoiceChanged(Locale newLocale) {
        Log.trace(THIS_CLASS_NAME, "->handleLocaleChoiceChanged: " + newLocale.name());

        if (getDatabaseObjects().isEmpty()) {
            return;
        }

        applySelectedLocale();
    }

    private void handleEntryChoiceChanged(ContentEntryDataItem newEntry) {
        Log.trace(THIS_CLASS_NAME, "->handleEntryChoiceChanged: " + newEntry);

        ofNullable(newEntry)
                .map(entry -> entry.internalEntryIdProperty().get())
                .ifPresent(this::switchToContentEntry);
    }

    private void switchToProfileAndRemoteEntry(String profileName, int localEntryIndex, int fieldRank, DbDto.Topic localTopic, DbDto.Topic remoteTopic) {
        getMiner().getRemoteContentEntryWithInternalIdentifier(localTopic, fieldRank, localEntryIndex, remoteTopic)
                .ifPresent(remoteContentEntry -> switchToProfileAndEntry(profileName, remoteContentEntry.getId(), true));
    }

    private void switchToSelectedResourceForLinkedTopic(ContentEntryDataItem selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
        ofNullable(selectedResource)
                .ifPresent(resource -> {
                    int remoteContentEntryId;
                    String entryReference = selectedResource.referenceProperty().get();
                    if (entryReference == null) {
                        remoteContentEntryId = selectedResource.internalEntryIdProperty().get();
                    } else {
                        remoteContentEntryId = getMiner().getContentEntryInternalIdentifierWithReference(entryReference, targetTopic)
                                .orElseThrow(() -> new IllegalStateException("No entry with ref: " + entryReference + " for topic: " + targetTopic));
                    }

                    switchToProfileAndEntry(targetProfileName, remoteContentEntryId, true);
                });
    }

    private void updateConfiguration() {
        try {
            final ApplicationConfiguration applicationConfiguration = getApplicationConfiguration();

            applicationConfiguration.setDatabasePath(getDatabaseLocationTextField().getText());
            applicationConfiguration.setEditorLocale(currentLocaleProperty.getValue());

            if (currentProfileProperty.getValue() != null) {
                applicationConfiguration.setEditorProfile(currentProfileProperty.getValue().getName());
            }

            applicationConfiguration.store();
        } catch (IOException ioe) {
            Log.warn(THIS_CLASS_NAME, "Unable to save application configuration", ioe);
        }
    }

    private void initGUIComponentsProperties() {
        getMouseCursorProperty().bind(
                when(getRunningServiceProperty())
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );
    }

    private void initTabPane() {
        initGroupTabs();

        initDynamicControls();

        updateAllPropertiesWithItemValues();
    }

    private void initGroupTabs() {
        final ObservableList<Tab> allTabs = getTabPane().getTabs();
        allTabs.clear();
        tabContentByName.clear();

        createTabAndAppend(DisplayConstants.TAB_NAME_DEFAULT, allTabs);

        final List<String> registeredGroups = currentProfileProperty.getValue().getGroups();
        if (registeredGroups == null) {
            return;
        }
        registeredGroups.forEach(groupName -> createTabAndAppend(groupName, allTabs));
    }

    private void createTabAndAppend(String tabName, ObservableList<Tab> allTabs) {
        VBox tabContainer = new VBox();
        Tab tab = new Tab(tabName, new ScrollPane(tabContainer));
        allTabs.add(tab);
        tabContentByName.put(tabName, tabContainer);
    }

    private void initDynamicControls() {
        final EditorLayoutDto.EditorProfileDto currentProfile = currentProfileProperty.getValue();
        if (currentProfile.getFieldSettings() != null) {
            dynamicFieldControlsHelper.addAllFieldsControls(
                    getLayoutObject(),
                    getProfilesChoiceBox().getValue().getName(),
                    getCurrentTopic());
        }

        if (currentProfile.getTopicLinks() != null) {
            dynamicLinkControlsHelper.addAllLinksControls(currentProfile);
        }
    }

    private void fillBrowsableEntries() {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                currentProfileProperty.getValue().getName(),
                getLayoutObject());

        final DbDto.Topic currentTopic = getCurrentTopic();
        browsableEntries.setAll(
                getMiner().getDatabaseTopic(currentTopic)
                        .map(topicObject -> topicObject.getData().getEntries().stream()
                                .map(topicEntry -> getDisplayableEntryForCurrentLocale(topicEntry, labelFieldRanks, currentTopic))
                                .collect(toList()))
                        .orElse(new ArrayList<>()));
    }

    private ContentEntryDataItem getDisplayableEntryForCurrentLocale(ContentEntryDto topicEntry, List<Integer> labelFieldRanks, DbDto.Topic topic) {
        ContentEntryDataItem contentEntryDataItem = new ContentEntryDataItem();

        int entryInternalIdentifier = topicEntry.getId();
        contentEntryDataItem.setInternalEntryId(entryInternalIdentifier);

        String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(entryInternalIdentifier, topic, currentLocaleProperty.getValue(), labelFieldRanks, getMiner(), getLayoutObject());
        contentEntryDataItem.setValue(entryValue);

        String entryReference = Long.toString(entryInternalIdentifier);
        Optional<String> potentialEntryReference = getMiner().getContentEntryReferenceWithInternalIdentifier(entryInternalIdentifier, topic);
        if (potentialEntryReference.isPresent()) {
            entryReference = potentialEntryReference.get();
        }
        contentEntryDataItem.setReference(entryReference);

        return contentEntryDataItem;
    }

    private void updateResourceProperties(ContentItemDto resourceItem, DbStructureDto.Field structureField) {
        Locale locale = currentLocaleProperty.getValue();
        DbDto.Topic resourceTopic = getCurrentTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        BooleanProperty errorProperty = itemPropsByFieldRank
                .errorPropertyAtFieldRank(resourceItem.getFieldRank());
        errorProperty.set(false);
        StringProperty errorMessageProperty = itemPropsByFieldRank
                .errorMessagePropertyAtFieldRank(resourceItem.getFieldRank());
        errorMessageProperty.set("");
        String resourceReference = resourceItem.getRawValue();
        String resourceValue = getMiner().getLocalizedResourceValueFromTopicAndReference(resourceReference, resourceTopic, locale)
                .orElseGet(() -> {
                    errorProperty.set(true);
                    errorMessageProperty.set(DisplayConstants.TOOLTIP_ERROR_RESOURCE_NOT_FOUND);
                    return DisplayConstants.VALUE_RESOURCE_DEFAULT;
                });
        itemPropsByFieldRank
                .resolvedValuePropertyAtFieldRank(resourceItem.getFieldRank())
                .set(resourceValue);
    }

    private void updateReferenceProperties(ContentItemDto referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        final List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), getProfilesChoiceBox().valueProperty().get().getName(), getLayoutObject());
        fieldSettings
                .map(FieldSettingsDto::getRemoteReferenceProfile)
                .ifPresent(remoteReferenceProfile -> remoteFieldRanks.addAll(
                        EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(remoteReferenceProfile, getLayoutObject())));

        String remoteContents = fetchRemoteContentsWithEntryRef(structureField.getRank(), remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
        itemPropsByFieldRank
                .resolvedValuePropertyAtFieldRank(referenceItem.getFieldRank())
                .set(remoteContents);
    }

    private ContentEntryDataItem fetchLinkResourceFromContentEntry(DbDto topicObject, ContentEntryDto contentEntry, TopicLinkDto linkObject) {
        List<Integer> remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(linkObject.getRemoteReferenceProfile(), getLayoutObject());
        ContentEntryDataItem databaseEntry = new ContentEntryDataItem();
        int entryId = contentEntry.getId();
        databaseEntry.setInternalEntryId(entryId);
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = getMiner().getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            databaseEntry.setReference(remoteEntryReference);
            databaseEntry.setInternalEntryId(contentEntry.getId());
            databaseEntry.setValue(fetchRemoteContentsWithEntryRef(linkObject.getId(), remoteTopic, remoteEntryReference, remoteFieldRanks));
        } else {
            // Classic topic (e.g. Car_Colors)
            databaseEntry.setInternalEntryId(entryId);
            databaseEntry.setValue(DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    entryId, linkObject.getTopic(),
                    currentLocaleProperty.getValue(),
                    remoteFieldRanks,
                    getMiner(),
                    getLayoutObject()));
        }
        return databaseEntry;
    }

    private void switchToInitialProfile() {
        final SingleSelectionModel<EditorLayoutDto.EditorProfileDto> selectionModel = getProfilesChoiceBox().getSelectionModel();
        selectionModel.clearSelection(); // ensures event will be fired even though 1st item is selected

        final Optional<EditorLayoutDto.EditorProfileDto> potentialProfile = getApplicationConfiguration().getEditorProfile()
                .flatMap(this::lookupChoiceboxProfileByName);
        // TODO [2.0] Java 9: https://docs.oracle.com/javase/9/docs/api/java/util/Optional.html#ifPresentOrElse-java.util.function.Consumer-java.lang.Runnable-
        if (potentialProfile.isPresent()) {
            selectionModel.select(potentialProfile.get());
        } else {
            selectionModel.selectFirst();
        }
    }

    private void switchToProfileAndEntry(String profileName, int entryIndex, boolean storeLocation) {
        if (storeLocation) {
            EditorLocation currentLocation = new EditorLocation(
                    getTabPane().selectionModelProperty().get().getSelectedIndex(),
                    currentProfileProperty.getValue().getName(),
                    currentEntryIndexProperty().getValue());
            getNavigationHistory().push(currentLocation);
        }

        final Optional<EditorLayoutDto.EditorProfileDto> targetProfile = lookupChoiceboxProfileByName(profileName);
        if (targetProfile.isPresent()) {
            getProfilesChoiceBox().setValue(targetProfile.get());
            switchToContentEntry(entryIndex);
        } else {
            throw new IllegalArgumentException(String.format("Unable to switch to profile %s @entry %d", profileName, entryIndex ));
        }
    }

    private void loadAndFillLocales(ChangeListener<Locale> localeChangeListener) {
        getApplicationConfiguration().getEditorLocale().ifPresent(currentLocaleProperty::setValue);

        //noinspection ResultOfMethodCallIgnored
        Locale.valuesAsStream()
                .collect(toCollection(() -> getLocalesChoiceBox().getItems()));

        getLocalesChoiceBox().valueProperty().bindBidirectional(currentLocaleProperty);

        getLocalesChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener(localeChangeListener);

    }

    private void loadAndFillProfiles(ChangeListener<EditorLayoutDto.EditorProfileDto> profileChangeListener) throws IOException {
        // Load available profiles from internal configuration
        final EditorLayoutDto editorLayoutDto = new ObjectMapper().readValue(thisClass.getResource(SettingsConstants.PATH_RESOURCE_PROFILES), EditorLayoutDto.class);
        getProfilesChoiceBox().getItems().addAll(editorLayoutDto.getProfiles());
        setLayoutObject(editorLayoutDto);

        getProfilesChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener(profileChangeListener);
    }

    private Optional<EditorLayoutDto.EditorProfileDto> lookupChoiceboxProfileByName(String profileName) {
        return getProfilesChoiceBox().getItems().stream()
                .filter(profile -> profile.getName().equals(profileName))
                .findFirst();
    }

    public Map<String, VBox> getTabContentByName() {
        return tabContentByName;
    }

    public Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> getResourcesByTopicLink() {
        return resourcesByTopicLink;
    }

    public Property<EditorLayoutDto.EditorProfileDto> currentProfile() {
        return currentProfileProperty;
    }

    ObservableList<ContentEntryDataItem> getBrowsableEntries() {
        return browsableEntries;
    }

    FilteredList<ContentEntryDataItem> getFilteredEntries() {
        return filteredEntries;
    }

    public ItemViewModel getItemPropsByFieldRank() {
        return itemPropsByFieldRank;
    }
}
