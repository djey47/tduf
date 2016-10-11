package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.AppConstants;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.controllers.helper.DynamicFieldControlsHelper;
import fr.tduf.gui.database.controllers.helper.DynamicLinkControlsHelper;
import fr.tduf.gui.database.converter.CurrentEntryIndexToStringConverter;
import fr.tduf.gui.database.converter.DatabaseTopicToStringConverter;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.factory.EntryCellFactory;
import fr.tduf.libunlimited.common.configuration.ApplicationConfiguration;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.size;

/**
 * Specialized controller to display database contents.
 */
public class MainStageViewDataController extends AbstractMainStageSubController {
    private static final String THIS_CLASS_NAME = MainStageViewDataController.class.getSimpleName();
    private static final Class<MainStageViewDataController> thisClass = MainStageViewDataController.class;

    private static final String MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC = "No database object for topic: ";

    final Property<Locale> currentLocaleProperty = new SimpleObjectProperty<>(SettingsConstants.DEFAULT_LOCALE);
    final Property<EditorLayoutDto.EditorProfileDto> currentProfileProperty = new SimpleObjectProperty<>();

    private final DynamicFieldControlsHelper dynamicFieldControlsHelper;
    private final DynamicLinkControlsHelper dynamicLinkControlsHelper;

    private final ObservableList<ContentEntryDataItem> browsableEntries = FXCollections.observableArrayList();
    private final Map<String, VBox> tabContentByName = new HashMap<>();
    private final Map<Integer, SimpleStringProperty> rawValuesByFieldRank = new HashMap<>();
    private final Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> resourcesByTopicLink = new HashMap<>();
    private final Map<Integer, SimpleStringProperty> resolvedValuesByFieldRank = new HashMap<>();


    MainStageViewDataController(MainStageController mainStageController) {
        super(mainStageController);

        dynamicFieldControlsHelper = new DynamicFieldControlsHelper(mainStageController);
        dynamicLinkControlsHelper = new DynamicLinkControlsHelper(mainStageController);
    }

    void initTopToolbar() {
        getCreditsLabel().setText(DisplayConstants.LABEL_STATUS_VERSION);
    }

    void initSettingsPane(String databaseDirectory, ChangeListener<Locale> localeChangeListener, ChangeListener<String> profileChangeListener) throws IOException {
        getSettingsPane().setExpanded(false);

        loadAndFillLocales(localeChangeListener);

        loadAndFillProfiles(profileChangeListener);

        getDatabaseLocationTextField().setText(databaseDirectory);
    }

    void initTopicEntryHeaderPane(ChangeListener<ContentEntryDataItem> entryChangeListener) {
        getCurrentTopicLabel().textProperty().bindBidirectional(currentTopicProperty(), new DatabaseTopicToStringConverter());
        getCurrentEntryLabel().textProperty().bindBidirectional(currentEntryLabelProperty());

        getEntryNumberComboBox().setItems(browsableEntries);
        getEntryNumberComboBox().setCellFactory(new EntryCellFactory());
        getEntryNumberComboBox().getSelectionModel().selectedItemProperty()
                .addListener(entryChangeListener);
    }

    void initStatusBar() {
        getEntryNumberTextField().textProperty().bindBidirectional(currentEntryIndexProperty(), new CurrentEntryIndexToStringConverter());
        getEntryItemsCountLabel().textProperty().bind(size(browsableEntries).asString(DisplayConstants.LABEL_ITEM_ENTRY_COUNT));
    }

    void updateDisplayWithLoadedObjects() {
        setMiner(BulkDatabaseMiner.load(getDatabaseObjects()));

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

                    final DbDto.Topic currentTopic = getCurrentTopicObject().getTopic();
                    String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(internalEntryId, currentTopic, currentLocaleProperty.getValue(), labelFieldRanks, getMiner(), getLayoutObject());
                    entry.setValue(entryValue);
                });
    }

    void applyProfile(String profileName) {
        final EditorLayoutDto.EditorProfileDto profileObject = EditorLayoutHelper.getAvailableProfileByName(profileName, getLayoutObject());
        final DbDto.Topic topic = profileObject.getTopic();
        final DbDto currentTopicObject = getMiner().getDatabaseTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException(MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC + topic));

        currentTopicProperty().setValue(topic);

        currentProfileProperty.setValue(profileObject);
        setCurrentTopicObject(currentTopicObject);

        refreshAll();

        updateConfiguration();
    }

    void applySelectedLocale() {
        updateAllPropertiesWithItemValues();
        updateConfiguration();
    }

    void refreshAll() {
        resolvedValuesByFieldRank.clear();
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
        final SimpleStringProperty rawValueProperty = rawValuesByFieldRank.get(fieldRank);
        if (rawValueProperty == null) {
            return;
        }
        rawValueProperty.set(item.getRawValue());

        if (!resolvedValuesByFieldRank.containsKey(fieldRank)) {
            return;
        }

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, getCurrentTopicObject().getStructure().getFields());
        if (REFERENCE == structureField.getFieldType()) {
            updateReferenceProperties(item, structureField);
        } else if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }
    }

    void updateLinkProperties(TopicLinkDto topicLinkObject) {
        resourcesByTopicLink.entrySet().stream()
                .filter(mapEntry -> mapEntry.getKey().equals(topicLinkObject))
                .findAny()
                .ifPresent(this::updateLinkProperties);
    }

    void updateEntriesAndSwitchTo(int entryIndex) {
        fillBrowsableEntries();
        int effectiveIndex = entryIndex;
        if (effectiveIndex < 0) {
            effectiveIndex = 0;
        }
        switchToContentEntry(effectiveIndex);
    }

    void switchToSelectedResourceForLinkedTopic(ContentEntryDataItem selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
        ofNullable(selectedResource)
                .ifPresent(resource -> {
                    int remoteContentEntryId;
                    String entryReference = selectedResource.referenceProperty().get();
                    if (entryReference == null) {
                        remoteContentEntryId = selectedResource.internalEntryIdProperty().get();
                    } else {
                        remoteContentEntryId = getMiner().getContentEntryInternalIdentifierWithReference(entryReference, targetTopic)
                                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No entry with ref: " + entryReference + " for topic: " + targetTopic));
                    }

                    switchToProfileAndEntry(targetProfileName, remoteContentEntryId, true);
                });
    }

    void switchToProfileAndRemoteEntry(String profileName, int localEntryIndex, int fieldRank, DbDto.Topic localTopic, DbDto.Topic remoteTopic) {
        getMiner().getRemoteContentEntryWithInternalIdentifier(localTopic, fieldRank, localEntryIndex, remoteTopic)
                .ifPresent(remoteContentEntry -> switchToProfileAndEntry(profileName, remoteContentEntry.getId(), true));
    }

    void switchToContentEntry(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= getCurrentTopicObject().getData().getEntries().size()) {
            return;
        }

        currentEntryIndexProperty().setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }

    void switchToNextEntry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex >= getCurrentTopicObject().getData().getEntries().size() - 1) {
            return;
        }

        switchToContentEntry(++currentEntryIndex);
    }

    void switchToNext10Entry() {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        int lastEntryIndex = getCurrentTopicObject().getData().getEntries().size() - 1;
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

    void switchToLastEntry() {
        switchToContentEntry(getCurrentTopicObject().getData().getEntries().size() - 1);
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
            Optional<String> potentialEntryReference = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty().getValue(), currentTopic);
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
        rawValuesByFieldRank.clear();

        final EditorLayoutDto.EditorProfileDto currentProfile = currentProfileProperty.getValue();
        if (currentProfile.getFieldSettings() != null) {
            dynamicFieldControlsHelper.addAllFieldsControls(
                    getLayoutObject(),
                    getProfilesChoiceBox().getValue(),
                    getCurrentTopicObject().getTopic());
        }

        if (currentProfile.getTopicLinks() != null) {
            dynamicLinkControlsHelper.addAllLinksControls(currentProfile);
        }
    }

    private void fillBrowsableEntries() {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                currentProfileProperty.getValue().getName(),
                getLayoutObject());

        final DbDto.Topic currentTopic = getCurrentTopicObject().getTopic();
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
        DbDto.Topic resourceTopic = getCurrentTopicObject().getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        String resourceReference = resourceItem.getRawValue();
        String resourceValue = getMiner().getLocalizedResourceValueFromTopicAndReference(resourceReference, resourceTopic, locale)
                .orElse(DisplayConstants.VALUE_ERROR_RESOURCE_NOT_FOUND);
        final SimpleStringProperty valueProperty = resolvedValuesByFieldRank.get(resourceItem.getFieldRank());
        valueProperty.set(resourceValue);
    }

    private void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<ContentEntryDataItem> values = remoteEntry.getValue();
        values.clear();

        final int currentEntryIndex = currentEntryIndexProperty().getValue();
        String currentEntryRef = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndex, currentTopicProperty().getValue())
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No REF available for entry at id: " + currentEntryIndex));

        final DbDto.Topic linkTopic = linkObject.getTopic();
        DbDto linkedTopicObject = getMiner().getDatabaseTopic(linkTopic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException(MESSAGE_NO_DATABASE_OBJECT_FOR_TOPIC + linkTopic));

        linkedTopicObject.getData().getEntries().stream()
                .filter(contentEntry -> currentEntryRef.equals(contentEntry.getItemAtRank(1)
                        .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No content item at rank 1 for entry id: " + contentEntry.getId()))
                        .getRawValue())
                )
                .map(contentEntry -> fetchLinkResourceFromContentEntry(linkedTopicObject, contentEntry, linkObject))
                .forEach(values::add);
    }

    private void updateReferenceProperties(ContentItemDto referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        final List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), getProfilesChoiceBox().valueProperty().get(), getLayoutObject());
        fieldSettings
                .map(FieldSettingsDto::getRemoteReferenceProfile)
                .ifPresent(remoteReferenceProfile -> remoteFieldRanks.addAll(
                        EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(remoteReferenceProfile, getLayoutObject())));

        String remoteContents = fetchRemoteContentsWithEntryRef(remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
        resolvedValuesByFieldRank.get(referenceItem.getFieldRank()).set(remoteContents);
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
            databaseEntry.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, remoteFieldRanks));
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

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
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
        return DisplayConstants.VALUE_ERROR_ENTRY_NOT_FOUND;
    }

    private void switchToInitialProfile() {
        final SingleSelectionModel<String> selectionModel = getProfilesChoiceBox().getSelectionModel();
        selectionModel.clearSelection(); // ensures event will be fired even though 1st item is selected

        final Optional<String> potentialProfileName = getApplicationConfiguration().getEditorProfile();
        if (potentialProfileName.isPresent()) {
            selectionModel.select(potentialProfileName.get());
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

        getProfilesChoiceBox().setValue(profileName);
        switchToContentEntry(entryIndex);
    }

    private void loadAndFillLocales(ChangeListener<Locale> localeChangeListener) {
        getApplicationConfiguration().getEditorLocale().ifPresent(currentLocaleProperty::setValue);

        Locale.valuesAsStream()
                .collect(toCollection(() -> getLocalesChoiceBox().getItems()));

        getLocalesChoiceBox().valueProperty().bindBidirectional(currentLocaleProperty);

        getLocalesChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener(localeChangeListener);

    }

    private void loadAndFillProfiles(ChangeListener<String> profileChangeListener) throws IOException {
        final EditorLayoutDto editorLayoutDto = new ObjectMapper().readValue(thisClass.getResource(SettingsConstants.PATH_RESOURCE_PROFILES), EditorLayoutDto.class);
        editorLayoutDto.getProfiles()
                .forEach(profileObject -> getProfilesChoiceBox().getItems().add(profileObject.getName()));
        setLayoutObject(editorLayoutDto);

        getProfilesChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener(profileChangeListener);
    }

    public Map<String, VBox> getTabContentByName() {
        return tabContentByName;
    }

    public Map<Integer, SimpleStringProperty> getRawValuesByFieldRank() {
        return rawValuesByFieldRank;
    }

    public Map<TopicLinkDto, ObservableList<ContentEntryDataItem>> getResourcesByTopicLink() {
        return resourcesByTopicLink;
    }

    public Map<Integer, SimpleStringProperty> getResolvedValuesByFieldRank() {
        return resolvedValuesByFieldRank;
    }

    public Property<EditorLayoutDto.EditorProfileDto> currentProfile() {
        return currentProfileProperty;
    }

    ObservableList<ContentEntryDataItem> getBrowsableEntries() {
        return browsableEntries;
    }
}
