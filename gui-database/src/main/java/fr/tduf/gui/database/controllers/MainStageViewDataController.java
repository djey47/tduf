package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.common.SettingsConstants;
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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
// TODO see to inherit main controller
public class MainStageViewDataController {

    private static final Class<MainStageViewDataController> thisClass = MainStageViewDataController.class;

    private final MainStageController mainStageController;

    private Property<Integer> entryItemsCountProperty;
    private Property<Long> currentEntryIndexProperty;
    private Property<DbDto.Topic> currentTopicProperty;
    private Property<DbResourceDto.Locale> currentLocaleProperty;
    private SimpleStringProperty currentEntryLabelProperty;
    private Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    private Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();

    MainStageViewDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void initTabViewDataProperties() {
        this.currentTopicProperty.setValue(this.mainStageController.getCurrentTopicObject().getTopic());
        this.currentEntryIndexProperty.setValue(0L);
        this.entryItemsCountProperty.setValue(this.mainStageController.getCurrentTopicObject().getData().getEntries().size());
        this.rawValuePropertyByFieldRank.clear();
        this.resolvedValuePropertyByFieldRank.clear();
    }

    void initNavViewDataProperties() {
        this.currentTopicProperty = new SimpleObjectProperty<>();
        this.entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        this.currentEntryIndexProperty = new SimpleObjectProperty<>(-1L);
        this.currentEntryLabelProperty = new SimpleStringProperty("");
    }

    void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> this.mainStageController.getLocalesChoiceBox().getItems().add(locale));

        this.currentLocaleProperty = new SimpleObjectProperty<>(UNITED_STATES);
        this.mainStageController.getLocalesChoiceBox().valueProperty().bindBidirectional(this.currentLocaleProperty);
    }

    void loadAndFillProfiles() throws IOException {
        this.mainStageController.setLayoutObject(new ObjectMapper().readValue(thisClass.getResource(SettingsConstants.PATH_RESOURCE_PROFILES), EditorLayoutDto.class));
        this.mainStageController.getLayoutObject().getProfiles()
                .forEach((profileObject) -> mainStageController.getProfilesChoiceBox().getItems().add(profileObject.getName()));
    }

    void updateAllPropertiesWithItemValues() {
        long entryIndex = this.currentEntryIndexProperty.getValue();
        DbDataDto.Entry entry = getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, this.mainStageController.getCurrentTopicObject().getTopic());

        String entryLabel = DisplayConstants.VALUE_UNKNOWN;
        if (this.mainStageController.getCurrentProfileObject().getEntryLabelFieldRanks() != null) {
            entryLabel = fetchContentsWithEntryId(this.currentTopicProperty.getValue(), entryIndex, this.mainStageController.getCurrentProfileObject().getEntryLabelFieldRanks());
        }
        this.currentEntryLabelProperty.setValue(entryLabel);

        entry.getItems().forEach(this::updateItemProperties);

        this.mainStageController.getResourceListByTopicLink().entrySet().forEach(this::updateLinkProperties);
    }

    void updateItemProperties(DbDataDto.Item item) {
        this.rawValuePropertyByFieldRank.get(item.getFieldRank()).set(item.getRawValue());

        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, this.mainStageController.getCurrentTopicObject().getStructure().getFields());
        if (structureField.isAResourceField()) {
            updateResourceProperties(item, structureField);
        }

        if (DbStructureDto.FieldType.REFERENCE == structureField.getFieldType()
                && resolvedValuePropertyByFieldRank.containsKey(item.getFieldRank())) {
            updateReferenceProperties(item, structureField);
        }
    }

    void switchToSelectedResourceForLinkedTopic(RemoteResource selectedResource, DbDto.Topic targetTopic, String targetProfileName) {
        if (selectedResource != null) {
            String entryReference = selectedResource.referenceProperty().get();
            long remoteContentEntryId;
            OptionalLong potentialEntryId = this.getMiner().getContentEntryIdFromReference(entryReference, targetTopic);
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
                    this.mainStageController.getTabPane().selectionModelProperty().get().getSelectedIndex(),
                    this.mainStageController.getCurrentProfileObject().getName(),
                    this.currentEntryIndexProperty.getValue());
            this.mainStageController.getNavigationHistory().push(currentLocation);
        }

        this.mainStageController.getProfilesChoiceBox().setValue(profileName);
        switchToContentEntry(entryIndex);
    }

    void switchToContentEntry(long entryIndex) {
        this.currentEntryIndexProperty.setValue(entryIndex);
        updateAllPropertiesWithItemValues();
    }

    void switchToPreviousLocation() {
        Stack<EditorLocation> navigationHistory = this.mainStageController.getNavigationHistory();
        if (navigationHistory.isEmpty()) {
            return;
        }

        EditorLocation previousLocation = navigationHistory.pop();
        switchToProfileAndEntry(previousLocation.getProfileName(), previousLocation.getEntryId(),false);
        switchToTabWithId(previousLocation.getTabId());
    }

    void switchToTabWithId(int tabId) {
        this.mainStageController.getTabPane().selectionModelProperty().get().select(tabId);
    }

    private void updateResourceProperties(DbDataDto.Item resourceItem, DbStructureDto.Field structureField) {
        DbDto.Topic resourceTopic = this.mainStageController.getCurrentTopicObject().getTopic();
        if (structureField.getTargetRef() != null) {
            resourceTopic = this.getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        }

        Optional<DbResourceDto.Entry> potentialResourceEntry = this.getMiner().getResourceEntryFromTopicAndLocaleWithReference(resourceItem.getRawValue(), resourceTopic, this.currentLocaleProperty.getValue());
        if (potentialResourceEntry.isPresent()) {
            String resourceValue = potentialResourceEntry.get().getValue();
            resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(resourceValue);
        } else {
            resolvedValuePropertyByFieldRank.get(resourceItem.getFieldRank()).set(DisplayConstants.VALUE_ERROR_RESOURCE_NOT_FOUND);
        }
    }

    private void updateReferenceProperties(DbDataDto.Item referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), this.mainStageController.getProfilesChoiceBox().getValue(), this.mainStageController.getLayoutObject());
        if (fieldSettings.isPresent()) {
            String remoteReferenceProfile = fieldSettings.get().getRemoteReferenceProfile();
            if (remoteReferenceProfile != null) {
                remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(remoteReferenceProfile, this.mainStageController.getLayoutObject());
            }
        }

        String remoteContents=fetchRemoteContentsWithEntryRef(remoteTopic,referenceItem.getRawValue(), remoteFieldRanks);
        resolvedValuePropertyByFieldRank.get(referenceItem.getFieldRank()).set(remoteContents);
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
                    return rawValuePropertyByFieldRank.get(1).getValue().equals(currentRef);
        })

        .map((contentEntry)->fetchLinkResourceFromContentEntry(topicObject, contentEntry,linkObject))

        .forEach(values::add);
    }

    private RemoteResource fetchLinkResourceFromContentEntry(DbDto topicObject, DbDataDto.Entry contentEntry, TopicLinkDto linkObject) {
        List<Integer> remoteFieldRanks = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(linkObject.getRemoteReferenceProfile(),this.mainStageController.getLayoutObject());
        RemoteResource remoteResource = new RemoteResource();
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            remoteResource.setReference(remoteEntryReference);
            remoteResource.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, remoteFieldRanks));
        } else {
            // Classic topic (e.g. Car_Colors)
            long entryId = contentEntry.getId();
            remoteResource.setReference(Long.valueOf(entryId).toString());
            remoteResource.setValue(fetchContentsWithEntryId(linkObject.getTopic(), entryId, remoteFieldRanks));
        }
        return remoteResource;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        OptionalLong potentialEntryId = this.getMiner().getContentEntryIdFromReference(remoteEntryReference, remoteTopic);
        if (potentialEntryId.isPresent()) {
            return fetchContentsWithEntryId(remoteTopic, potentialEntryId.getAsLong(), remoteFieldRanks);
        }
        return DisplayConstants.VALUE_ERROR_ENTRY_NOT_FOUND;
    }

    private String fetchContentsWithEntryId(DbDto.Topic topic, long entryId, List<Integer> fieldRanks) {
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return DisplayConstants.VALUE_UNKNOWN;
        }

        List<String> contents = fieldRanks.stream()

        .map((fieldRank)->{
        Optional<DbResourceDto.Entry>potentialRemoteResourceEntry=this.getMiner().getResourceEntryWithInternalIdentifier(topic,fieldRank,entryId,currentLocaleProperty.getValue());
        if(potentialRemoteResourceEntry.isPresent()){
        return potentialRemoteResourceEntry.get().getValue();
        }

        return this.getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic,fieldRank,entryId).get().getRawValue();
        })

                .collect(toList());

        return String.join(DisplayConstants.SEPARATOR_VALUES, contents);
    }

    Property<Long> getCurrentEntryIndexProperty() {
        return currentEntryIndexProperty;
    }

    Property<DbDto.Topic> getCurrentTopicProperty() {
        return currentTopicProperty;
    }

    SimpleStringProperty getCurrentEntryLabelProperty() {
        return currentEntryLabelProperty;
    }

    Map<Integer, SimpleStringProperty> getResolvedValuePropertyByFieldRank() {
        return resolvedValuePropertyByFieldRank;
    }

    Map<Integer, SimpleStringProperty> getRawValuePropertyByFieldRank() {
        return rawValuePropertyByFieldRank;
    }

    Property<Integer> getEntryItemsCountProperty() {
        return entryItemsCountProperty;
    }

    Property<DbResourceDto.Locale> getCurrentLocaleProperty() {
        return currentLocaleProperty;
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}