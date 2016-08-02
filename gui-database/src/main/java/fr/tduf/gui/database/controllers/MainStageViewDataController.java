package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.AppConstants;
import fr.tduf.gui.database.DatabaseEditor;
import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.collections.ObservableList;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to display database contents.
 */
class MainStageViewDataController extends AbstractMainStageSubController {
    private static final String THIS_CLASS_NAME = MainStageViewDataController.class.getSimpleName();
    private static final Class<MainStageViewDataController> thisClass = MainStageViewDataController.class;

    MainStageViewDataController(MainStageController mainStageController) {
        super(mainStageController);
    }

    void updateDisplayWithLoadedObjects() {
        if (!getDatabaseObjects().isEmpty()) {
            setMiner(BulkDatabaseMiner.load(getDatabaseObjects()));

            getProfilesChoiceBox().getSelectionModel().clearSelection(); // ensures event will be fired even though 1st item is selected
            getProfilesChoiceBox().getSelectionModel().selectFirst();

            getNavigationHistory().clear();

            getLoadDatabaseButton().disableProperty().setValue(true);

            updateConfiguration();
        }
    }

    void fillBrowsableEntries() {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                getCurrentProfileObject().getName(),
                getLayoutObject());

        final DbDto.Topic currentTopic = getCurrentTopicObject().getTopic();
        getBrowsableEntries().setAll(
                getMiner().getDatabaseTopic(currentTopic)
                        .map(topicObject -> topicObject.getData().getEntries().stream()
                                .map(topicEntry -> getDisplayableEntryForCurrentLocale(topicEntry, labelFieldRanks, currentTopic))
                                .collect(toList()))
                        .orElse(new ArrayList<>()));
    }

    void updateBrowsableEntryLabel(long internalEntryId) {
        getBrowsableEntries().stream()
                .filter(entry -> entry.internalEntryIdProperty().get() == internalEntryId)
                .findAny()
                .ifPresent(entry -> {
                    final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                            getCurrentProfileObject().getName(),
                            getLayoutObject());

                    final DbDto.Topic currentTopic = getCurrentTopicObject().getTopic();
                    String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(internalEntryId, currentTopic, currentLocaleProperty().getValue(), labelFieldRanks, getMiner());
                    entry.setValue(entryValue);
                });
    }

    void fillLocales() {
        Locale.valuesAsStream()
                .collect(toCollection(() -> getLocalesChoiceBox().getItems()));

        getLocalesChoiceBox().valueProperty().bindBidirectional(currentLocaleProperty());
    }

    void loadAndFillProfiles() throws IOException {
        final EditorLayoutDto editorLayoutDto = new ObjectMapper().readValue(thisClass.getResource(SettingsConstants.PATH_RESOURCE_PROFILES), EditorLayoutDto.class);
        editorLayoutDto.getProfiles()
                .forEach(profileObject -> getProfilesChoiceBox().getItems().add(profileObject.getName()));
        setLayoutObject(editorLayoutDto);
    }

    void updateAllPropertiesWithItemValues() {
        updateCurrentEntryLabelProperty();

        long entryIndex = currentEntryIndexProperty().getValue();
        DbDto.Topic currentTopic = currentTopicProperty().getValue();
        getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, currentTopic)
                .ifPresent(entry -> entry.getItems().forEach(this::updateItemProperties));

        getResourceListByTopicLink().entrySet().forEach(this::updateLinkProperties);
    }

    void updateItemProperties(ContentItemDto item) {
        rawValuePropertyByFieldRank().get(item.getFieldRank()).set(item.getRawValue());

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, getCurrentTopicObject().getStructure().getFields());
        if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }

        if (DbStructureDto.FieldType.REFERENCE == structureField.getFieldType()
                && resolvedValuePropertyByFieldRank().containsKey(item.getFieldRank())) {
            updateReferenceProperties(item, structureField);
        }
    }

    void updateLinkProperties(TopicLinkDto topicLinkObject) {
        getResourceListByTopicLink().entrySet().stream()

                .filter(mapEntry -> mapEntry.getKey().equals(topicLinkObject))

                .findAny()

                .ifPresent(this::updateLinkProperties);
    }

    void updateEntriesAndSwitchTo(long entryIndex) {
        fillBrowsableEntries();
        switchToContentEntry(entryIndex);
    }

    void switchToSelectedResourceForLinkedTopic(ContentEntryDataItem selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
        ofNullable(selectedResource)
                .ifPresent(resource -> {
                    long remoteContentEntryId;
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

    void switchToProfileAndEntry(String profileName, long entryIndex, boolean storeLocation) {
        if (storeLocation) {
            EditorLocation currentLocation = new EditorLocation(
                    getTabPane().selectionModelProperty().get().getSelectedIndex(),
                    getCurrentProfileObject().getName(),
                    currentEntryIndexProperty().getValue());
            getNavigationHistory().push(currentLocation);
        }

        getProfilesChoiceBox().setValue(profileName);
        switchToContentEntry(entryIndex);
    }

    void switchToContentEntry(long entryIndex) {
        if (entryIndex < 0 || entryIndex >= getCurrentTopicObject().getData().getEntries().size()) {
            return;
        }

        currentEntryIndexProperty().setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }

    void switchToNextEntry() {
        long currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex >= getCurrentTopicObject().getData().getEntries().size() - 1) {
            return;
        }

        switchToContentEntry(++currentEntryIndex);
    }

    void switchToNext10Entry() {
        long currentEntryIndex = currentEntryIndexProperty().getValue();
        long lastEntryIndex = getCurrentTopicObject().getData().getEntries().size() - 1L;
        if (currentEntryIndex + 10 >= lastEntryIndex) {
            currentEntryIndex = lastEntryIndex;
        } else {
            currentEntryIndex += 10;
        }

        switchToContentEntry(currentEntryIndex);
    }

    void switchToPreviousEntry() {
        long currentEntryIndex = currentEntryIndexProperty().getValue();
        if (currentEntryIndex <= 0) {
            return;
        }

        switchToContentEntry(--currentEntryIndex);
    }

    void switchToPrevious10Entry() {
        long currentEntryIndex = currentEntryIndexProperty().getValue();
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
        switchToContentEntry(getCurrentTopicObject().getData().getEntries().size() - 1L);
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

    List<String> selectEntriesFromTopic(DbDto.Topic topic, String profileName) {
        DbDto databaseObject = getMiner().getDatabaseTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No database object for topic: " + topic));

        final Optional<DbStructureDto.Field> potentialUidField = DatabaseStructureQueryHelper.getUidField(databaseObject.getStructure().getFields());
        if (potentialUidField.isPresent()) {
            Optional<String> potentialEntryReference = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty().getValue(), topic);
            final List<ContentEntryDataItem> selectedItems = getEntriesStageController().initAndShowModalDialogForMultiSelect(potentialEntryReference, topic, profileName);

            return selectedItems.stream()

                    .map(item -> item.referenceProperty().get())

                    .collect(toList());
        }

        return new ArrayList<>();
    }

    List<String> selectFieldsFromTopic(DbDto.Topic topic) {
        return getFieldsBrowserStageController().initAndShowModalDialog(topic).stream()

                .map(item -> Integer.valueOf(item.rankProperty().get()).toString())

                .collect(toList());
    }

    void updateCurrentEntryLabelProperty() {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                getCurrentProfileObject().getName(),
                getLayoutObject());
        String entryLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                currentEntryIndexProperty().getValue(),
                currentTopicProperty().getValue(),
                currentLocaleProperty().getValue(),
                labelFieldRanks,
                getMiner());
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
            getApplicationConfiguration().setDatabasePath(getDatabaseLocationTextField().getText());
            getApplicationConfiguration().store();
        } catch (IOException ioe) {
            Log.warn(THIS_CLASS_NAME, "Unable to save application configuration", ioe);
        }
    }

    private ContentEntryDataItem getDisplayableEntryForCurrentLocale(ContentEntryDto topicEntry, List<Integer> labelFieldRanks, DbDto.Topic topic) {
        ContentEntryDataItem contentEntryDataItem = new ContentEntryDataItem();

        long entryInternalIdentifier = topicEntry.getId();
        contentEntryDataItem.setInternalEntryId(entryInternalIdentifier);

        String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(entryInternalIdentifier, topic, currentLocaleProperty().getValue(), labelFieldRanks, getMiner());
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
        Locale locale = currentLocaleProperty().getValue();
        DbDto.Topic resourceTopic = getCurrentTopicObject().getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        String resourceReference = resourceItem.getRawValue();
        String resourceValue = getMiner().getLocalizedResourceValueFromTopicAndReference(resourceReference, resourceTopic, locale)
                .orElse(DisplayConstants.VALUE_ERROR_RESOURCE_NOT_FOUND);

        resolvedValuePropertyByFieldRank().get(resourceItem.getFieldRank()).set(resourceValue);
    }

    // Ignore this warning (usage as method reference)
    private void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<ContentEntryDataItem> values = remoteEntry.getValue();
        values.clear();

        final Long currentEntryIndex = currentEntryIndexProperty().getValue();
        String currentEntryRef = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndex, currentTopicProperty().getValue())
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No REF available for entry at id: " + currentEntryIndex));

        final DbDto.Topic linkTopic = linkObject.getTopic();
        DbDto linkedTopicObject = getMiner().getDatabaseTopic(linkTopic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No database object for topic: " + linkTopic));

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

        List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), getProfilesChoiceBox().getValue(), getLayoutObject());
        if (fieldSettings.isPresent()) {
            String remoteReferenceProfile = fieldSettings.get().getRemoteReferenceProfile();
            if (remoteReferenceProfile != null) {
                remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(remoteReferenceProfile, getLayoutObject());
            }
        }

        String remoteContents = fetchRemoteContentsWithEntryRef(remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
        resolvedValuePropertyByFieldRank().get(referenceItem.getFieldRank()).set(remoteContents);
    }

    private ContentEntryDataItem fetchLinkResourceFromContentEntry(DbDto topicObject, ContentEntryDto contentEntry, TopicLinkDto linkObject) {
        List<Integer> remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(linkObject.getRemoteReferenceProfile(), getLayoutObject());
        ContentEntryDataItem databaseEntry = new ContentEntryDataItem();
        long entryId = contentEntry.getId();
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
                    currentLocaleProperty().getValue(),
                    remoteFieldRanks,
                    getMiner()));
        }
        return databaseEntry;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        OptionalLong potentialEntryId = getMiner().getContentEntryInternalIdentifierWithReference(remoteEntryReference, remoteTopic);
        if (potentialEntryId.isPresent()) {
            return DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    potentialEntryId.getAsLong(), remoteTopic,
                    currentLocaleProperty().getValue(),
                    remoteFieldRanks,
                    getMiner());
        }
        return DisplayConstants.VALUE_ERROR_ENTRY_NOT_FOUND;
    }
}
