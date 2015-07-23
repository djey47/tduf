package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to display database contents.
 */
public class MainStageViewDataController {

    private static final Class<MainStageViewDataController> thisClass = MainStageViewDataController.class;

    private final MainStageController mainStageController;

    MainStageViewDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void fillBrowsableEntries(DbDto.Topic topic) {
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                mainStageController.getCurrentProfileObject().getName(),
                mainStageController.getLayoutObject());

        ObservableList<ContentEntryDataItem> browsableEntryList = mainStageController.browsableEntryList;
        browsableEntryList.clear();
        getMiner().getDatabaseTopic(topic)
                .ifPresent((topicObject) -> browsableEntryList.addAll(topicObject.getData().getEntries().stream()

                                .map((topicEntry) -> getDisplayableEntryForCurrentLocale(topicEntry, labelFieldRanks, topic))

                                .collect(toList()))
                );
    }

    void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> mainStageController.localesChoiceBox.getItems().add(locale));

        mainStageController.currentLocaleProperty = new SimpleObjectProperty<>(UNITED_STATES);
        mainStageController.localesChoiceBox.valueProperty().bindBidirectional(mainStageController.currentLocaleProperty);
    }

    void loadAndFillProfiles() throws IOException {
        mainStageController.setLayoutObject(new ObjectMapper().readValue(thisClass.getResource(SettingsConstants.PATH_RESOURCE_PROFILES), EditorLayoutDto.class));
        mainStageController.getLayoutObject().getProfiles()
                .forEach((profileObject) -> mainStageController.profilesChoiceBox.getItems().add(profileObject.getName()));
    }

    void updateAllPropertiesWithItemValues() {
        long entryIndex = mainStageController.currentEntryIndexProperty.getValue();
        DbDto.Topic currentTopic = mainStageController.currentTopicProperty.getValue();
        final List<Integer> labelFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(
                mainStageController.getCurrentProfileObject().getName(),
                mainStageController.getLayoutObject());
        String entryLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                entryIndex,
                currentTopic,
                mainStageController.currentLocaleProperty.getValue(),
                labelFieldRanks,
                getMiner());
        mainStageController.currentEntryLabelProperty.setValue(entryLabel);

        getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, currentTopic)
                .ifPresent((entry) -> entry.getItems().forEach(this::updateItemProperties));

        mainStageController.getResourceListByTopicLink().entrySet().forEach(this::updateLinkProperties);
    }

    void updateItemProperties(DbDataDto.Item item) {
        mainStageController.rawValuePropertyByFieldRank.get(item.getFieldRank()).set(item.getRawValue());

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, mainStageController.getCurrentTopicObject().getStructure().getFields());
        if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }

        if (DbStructureDto.FieldType.REFERENCE == structureField.getFieldType()
                && mainStageController.resolvedValuePropertyByFieldRank.containsKey(item.getFieldRank())) {
            updateReferenceProperties(item, structureField);
        }
    }

    void updateLinkProperties(TopicLinkDto topicLinkObject) {
        mainStageController.resourceListByTopicLink.entrySet().stream()

                .filter((mapEntry) -> mapEntry.getKey().equals(topicLinkObject))

                .findAny()

                .ifPresent(this::updateLinkProperties);
    }

    void updateEntryCountAndSwitchToEntry(long entryIndex) {
        updateEntryCount();
        switchToContentEntry(entryIndex);
    }

    void updateEntryCount() {
        mainStageController.entryItemsCountProperty.setValue(mainStageController.getCurrentTopicObject().getData().getEntries().size());
    }

    void switchToSelectedResourceForLinkedTopic(ContentEntryDataItem selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
        if (selectedResource != null) {
            String entryReference = selectedResource.referenceProperty().get();
            long remoteContentEntryId;
            OptionalLong potentialEntryId = getMiner().getContentEntryIdFromReference(entryReference, targetTopic);
            if (potentialEntryId.isPresent()) {
                remoteContentEntryId = potentialEntryId.getAsLong();
            } else {
                remoteContentEntryId = Long.valueOf(entryReference);
            }

            switchToProfileAndEntry(targetProfileName, remoteContentEntryId, true);
        }
    }

    void switchToProfileAndEntry(String profileName, long entryIndex, boolean storeLocation) {
        if (storeLocation) {
            EditorLocation currentLocation = new EditorLocation(
                    mainStageController.tabPane.selectionModelProperty().get().getSelectedIndex(),
                    mainStageController.getCurrentProfileObject().getName(),
                    mainStageController.currentEntryIndexProperty.getValue());
            mainStageController.getNavigationHistory().push(currentLocation);
        }

        mainStageController.profilesChoiceBox.setValue(profileName);
        switchToContentEntry(entryIndex);
    }

    void switchToContentEntry(long entryIndex) {
        if (entryIndex < 0 || entryIndex >= mainStageController.getCurrentTopicObject().getData().getEntries().size()) {
            return;
        }

        mainStageController.currentEntryIndexProperty.setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }

    void switchToNextEntry() {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        if (currentEntryIndex >= mainStageController.getCurrentTopicObject().getData().getEntries().size() - 1) {
            return;
        }

        switchToContentEntry(++currentEntryIndex);
    }

    void switchToNext10Entry() {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        long lastEntryIndex = mainStageController.getCurrentTopicObject().getData().getEntries().size() - 1;
        if (currentEntryIndex + 10 >= lastEntryIndex) {
            currentEntryIndex = lastEntryIndex;
        } else {
            currentEntryIndex += 10;
        }

        switchToContentEntry(currentEntryIndex);
    }

    void switchToPreviousEntry() {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        if (currentEntryIndex <= 0) {
            return;
        }

        switchToContentEntry(--currentEntryIndex);
    }

    void switchToPrevious10Entry() {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
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
        switchToContentEntry(mainStageController.getCurrentTopicObject().getData().getEntries().size() - 1);
    }

    void switchToPreviousLocation() {
        Stack<EditorLocation> navigationHistory = mainStageController.getNavigationHistory();
        if (navigationHistory.isEmpty()) {
            return;
        }

        EditorLocation previousLocation = navigationHistory.pop();
        switchToProfileAndEntry(previousLocation.getProfileName(), previousLocation.getEntryId(), false);
        switchToTabWithId(previousLocation.getTabId());
    }

    void switchToTabWithId(int tabId) {
        mainStageController.tabPane.selectionModelProperty().get().select(tabId);
    }

    private ContentEntryDataItem getDisplayableEntryForCurrentLocale(DbDataDto.Entry topicEntry, List<Integer> labelFieldRanks, DbDto.Topic topic) {
        ContentEntryDataItem contentEntryDataItem = new ContentEntryDataItem();

        long entryInternalIdentifier = topicEntry.getId();
        contentEntryDataItem.setInternalEntryId(entryInternalIdentifier);

        String entryValue = DatabaseQueryHelper.fetchResourceValuesWithEntryId(entryInternalIdentifier, topic, mainStageController.currentLocaleProperty.getValue(), labelFieldRanks, getMiner());
        contentEntryDataItem.setValue(entryValue);

        String entryReference = Long.valueOf(entryInternalIdentifier).toString();
        Optional<String> potentialEntryReference = getMiner().getContentEntryRefWithInternalIdentifier(entryInternalIdentifier, topic);
        if (potentialEntryReference.isPresent()) {
            entryReference = potentialEntryReference.get();
        }
        contentEntryDataItem.setReference(entryReference);

        return contentEntryDataItem;
    }

    private void updateResourceProperties(DbDataDto.Item resourceItem, DbStructureDto.Field structureField) {
        DbDto.Topic resourceTopic = mainStageController.getCurrentTopicObject().getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        Optional<DbResourceDto.Entry> potentialResourceEntry = getMiner().getResourceEntryFromTopicAndLocaleWithReference(resourceItem.getRawValue(), resourceTopic, mainStageController.currentLocaleProperty.getValue());
        if (potentialResourceEntry.isPresent()) {
            String resourceValue = potentialResourceEntry.get().getValue();
            mainStageController.resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(resourceValue);
        } else {
            mainStageController.resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(DisplayConstants.VALUE_ERROR_RESOURCE_NOT_FOUND);
        }
    }

    private void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<ContentEntryDataItem>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<ContentEntryDataItem> values = remoteEntry.getValue();
        values.clear();

        String currentEntryRef = getMiner().getContentEntryRefWithInternalIdentifier(mainStageController.currentEntryIndexProperty.getValue(), mainStageController.currentTopicProperty.getValue()).get();
        DbDto linkedTopicObject = getMiner().getDatabaseTopic(linkObject.getTopic()).get();
        linkedTopicObject.getData().getEntries().stream()

                .filter((contentEntry) -> currentEntryRef.equals(contentEntry.getItems().get(0).getRawValue()))

                .map((contentEntry) -> fetchLinkResourceFromContentEntry(linkedTopicObject, contentEntry, linkObject))

                .forEach(values::add);
    }

    private void updateReferenceProperties(DbDataDto.Item referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), mainStageController.profilesChoiceBox.getValue(), this.mainStageController.getLayoutObject());
        if (fieldSettings.isPresent()) {
            String remoteReferenceProfile = fieldSettings.get().getRemoteReferenceProfile();
            if (remoteReferenceProfile != null) {
                remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(remoteReferenceProfile, this.mainStageController.getLayoutObject());
            }
        }

        String remoteContents = fetchRemoteContentsWithEntryRef(remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
        mainStageController.resolvedValuePropertyByFieldRank.get(referenceItem.getFieldRank()).set(remoteContents);
    }

    private ContentEntryDataItem fetchLinkResourceFromContentEntry(DbDto topicObject, DbDataDto.Entry contentEntry, TopicLinkDto linkObject) {
        List<Integer> remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(linkObject.getRemoteReferenceProfile(), this.mainStageController.getLayoutObject());
        ContentEntryDataItem databaseEntry = new ContentEntryDataItem();
        long entryId = contentEntry.getId();
        databaseEntry.setInternalEntryId(entryId);
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = getMiner().getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            databaseEntry.setReference(remoteEntryReference);
            databaseEntry.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, remoteFieldRanks));
        } else {
            // Classic topic (e.g. Car_Colors)
            databaseEntry.setReference(Long.valueOf(entryId).toString());
            databaseEntry.setValue(DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    entryId, linkObject.getTopic(),
                    mainStageController.currentLocaleProperty.getValue(),
                    remoteFieldRanks,
                    getMiner()));
        }
        return databaseEntry;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        OptionalLong potentialEntryId = getMiner().getContentEntryIdFromReference(remoteEntryReference, remoteTopic);
        if (potentialEntryId.isPresent()) {
            return DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    potentialEntryId.getAsLong(), remoteTopic,
                    mainStageController.currentLocaleProperty.getValue(),
                    remoteFieldRanks,
                    getMiner());
        }
        return DisplayConstants.VALUE_ERROR_ENTRY_NOT_FOUND;
    }

    private BulkDatabaseMiner getMiner() {
        return mainStageController.getMiner();
    }
}