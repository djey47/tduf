package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
import fr.tduf.gui.database.common.helper.DatabaseQueryHelper;
import fr.tduf.gui.database.common.helper.EditorLayoutHelper;
import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.RemoteResource;
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
import javafx.scene.control.TableView;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

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

    void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> mainStageController.getLocalesChoiceBox().getItems().add(locale));

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
        DbDataDto.Entry entry = getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, currentTopic);

        String entryLabel = DisplayConstants.VALUE_UNKNOWN;
        if (mainStageController.getCurrentProfileObject().getEntryLabelFieldRanks() != null) {
            entryLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    entryIndex, currentTopic,
                    mainStageController.currentLocaleProperty.getValue(),
                    mainStageController.getCurrentProfileObject().getEntryLabelFieldRanks(),
                    getMiner());
        }
        mainStageController.currentEntryLabelProperty.setValue(entryLabel);

        entry.getItems().forEach(this::updateItemProperties);

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

    void switchToSelectedResourceForLinkedTopic(RemoteResource selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
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
        mainStageController.currentEntryIndexProperty.setValue(entryIndex);
        updateAllPropertiesWithItemValues();
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

    private void updateResourceProperties(DbDataDto.Item resourceItem, DbStructureDto.Field structureField) {
        DbDto.Topic resourceTopic = mainStageController.getCurrentTopicObject().getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        Optional<DbResourceDto.Entry> potentialResourceEntry = this.getMiner().getResourceEntryFromTopicAndLocaleWithReference(resourceItem.getRawValue(), resourceTopic, mainStageController.currentLocaleProperty.getValue());
        if (potentialResourceEntry.isPresent()) {
            String resourceValue = potentialResourceEntry.get().getValue();
            mainStageController.resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(resourceValue);
        } else {
            mainStageController.resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(DisplayConstants.VALUE_ERROR_RESOURCE_NOT_FOUND);
        }
    }

    private void updateLinkProperties(Map.Entry<TopicLinkDto, ObservableList<RemoteResource>> remoteEntry) {
        TopicLinkDto linkObject = remoteEntry.getKey();
        ObservableList<RemoteResource> values = remoteEntry.getValue();
        values.clear();

        DbDto topicObject = this.getMiner().getDatabaseTopic(linkObject.getTopic()).get();
        topicObject.getData().getEntries().stream()

                .filter((contentEntry) -> {
                    // TODO find another way of getting current reference
                    String currentRef = contentEntry.getItems().get(0).getRawValue();
                    return mainStageController.rawValuePropertyByFieldRank.get(1).getValue().equals(currentRef);
                })

                .map((contentEntry) -> fetchLinkResourceFromContentEntry(topicObject, contentEntry, linkObject))

                .forEach(values::add);
    }

    private void updateReferenceProperties(DbDataDto.Item referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

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

    private RemoteResource fetchLinkResourceFromContentEntry(DbDto topicObject, DbDataDto.Entry contentEntry, TopicLinkDto linkObject) {
        List<Integer> remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(linkObject.getRemoteReferenceProfile(), this.mainStageController.getLayoutObject());
        RemoteResource remoteResource = new RemoteResource();
        long entryId = contentEntry.getId();
        remoteResource.setInternalEntryId(entryId);
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            remoteResource.setReference(remoteEntryReference);
            remoteResource.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, remoteFieldRanks));
        } else {
            // Classic topic (e.g. Car_Colors)
            remoteResource.setReference(Long.valueOf(entryId).toString());
            remoteResource.setValue(DatabaseQueryHelper.fetchResourceValuesWithEntryId(
                    entryId, linkObject.getTopic(),
                    mainStageController.currentLocaleProperty.getValue(),
                    remoteFieldRanks,
                    getMiner()));
        }
        return remoteResource;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        OptionalLong potentialEntryId = this.getMiner().getContentEntryIdFromReference(remoteEntryReference, remoteTopic);
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
        return this.mainStageController.getMiner();
    }
}