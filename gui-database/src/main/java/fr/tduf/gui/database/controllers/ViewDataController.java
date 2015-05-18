package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.domain.EditorLocation;
import fr.tduf.gui.database.domain.RemoteResource;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.gui.database.helper.EditorLayoutHelper;
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

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to display database contents.
 */
public class ViewDataController {

    private static final Class<ViewDataController> thisClass = ViewDataController.class;

    private MainStageController mainStageController;

    private Property<Integer> entryItemsCountProperty;
    private Property<Long> currentEntryIndexProperty;
    private Property<DbDto.Topic> currentTopicProperty;
    private Property<DbResourceDto.Locale> currentLocaleProperty;
    private SimpleStringProperty currentEntryLabelProperty;
    private Map<Integer, SimpleStringProperty> rawValuePropertyByFieldRank = new HashMap<>();
    private Map<Integer, SimpleStringProperty> resolvedValuePropertyByFieldRank = new HashMap<>();

    ViewDataController(MainStageController mainStageController) {
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

        this.currentLocaleProperty = new SimpleObjectProperty<>(DbResourceDto.Locale.UNITED_STATES);
        this.mainStageController.getLocalesChoiceBox().valueProperty().bindBidirectional(this.currentLocaleProperty);
    }

    void loadAndFillProfiles() throws IOException {
        this.mainStageController.setLayoutObject(new ObjectMapper().readValue(thisClass.getResource("/layout/defaultProfiles.json"), EditorLayoutDto.class));
        this.mainStageController.getLayoutObject().getProfiles()
                .forEach((profileObject) -> mainStageController.getProfilesChoiceBox().getItems().add(profileObject.getName()));
    }

    void updateAllPropertiesWithItemValues() {
        long entryIndex = this.currentEntryIndexProperty.getValue();
        DbDataDto.Entry entry = getMiner().getContentEntryFromTopicWithInternalIdentifier(entryIndex, this.mainStageController.getCurrentTopicObject().getTopic());

        String entryLabel = "<?>";
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

    void switchToProfileAndEntry(String profileName, long entryIndex) {
        EditorLocation currentLocation = new EditorLocation(
                this.mainStageController.getTabPane().selectionModelProperty().get().getSelectedIndex(),
                this.mainStageController.getCurrentProfileObject().getName(),
                this.currentEntryIndexProperty.getValue());
        this.mainStageController.getNavigationHistory().push(currentLocation);

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
        switchToProfileAndEntry(previousLocation.getProfileName(), previousLocation.getEntryId());
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
        }
    }

    private void updateReferenceProperties(DbDataDto.Item referenceItem, DbStructureDto.Field structureField) {
        DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();

        List<Integer> remoteFieldRanks = new ArrayList<>();
        Optional<FieldSettingsDto> fieldSettings = EditorLayoutHelper.getFieldSettingsByRankAndProfileName(structureField.getRank(), this.mainStageController.getProfilesChoiceBox().getValue(), this.mainStageController.getLayoutObject());
        if (fieldSettings.isPresent()) {
            remoteFieldRanks = fieldSettings.get().getRemoteFieldRanks();
        }

        String remoteContents = fetchRemoteContentsWithEntryRef(remoteTopic, referenceItem.getRawValue(), remoteFieldRanks);
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

                .map((contentEntry) -> fetchLinkResourceFromContentEntry(contentEntry, linkObject, topicObject))

                .forEach(values::add);
    }

    private RemoteResource fetchLinkResourceFromContentEntry(DbDataDto.Entry contentEntry, TopicLinkDto linkObject, DbDto topicObject) {
        RemoteResource remoteResource = new RemoteResource();
        if (topicObject.getStructure().getFields().size() == 2) {
            // Association topic (e.g. Car_Rims)
            String remoteTopicRef = topicObject.getStructure().getFields().get(1).getTargetRef();
            DbDto.Topic remoteTopic = this.getMiner().getDatabaseTopicFromReference(remoteTopicRef).getTopic();

            String remoteEntryReference = contentEntry.getItems().get(1).getRawValue();
            remoteResource.setReference(remoteEntryReference);
            remoteResource.setValue(fetchRemoteContentsWithEntryRef(remoteTopic, remoteEntryReference, linkObject.getRemoteFieldRanks()));
        } else {
            long entryId = contentEntry.getId();
            remoteResource.setReference(Long.valueOf(entryId).toString());
            remoteResource.setValue(fetchContentsWithEntryId(linkObject.getTopic(), entryId, linkObject.getRemoteFieldRanks()));
        }
        return remoteResource;
    }

    private String fetchRemoteContentsWithEntryRef(DbDto.Topic remoteTopic, String remoteEntryReference, List<Integer> remoteFieldRanks) {
        requireNonNull(remoteFieldRanks, "A list of field ranks (even empty) must be provided.");

        long remoteEntryId = this.getMiner().getContentEntryIdFromReference(remoteEntryReference, remoteTopic).getAsLong();
        return fetchContentsWithEntryId(remoteTopic, remoteEntryId, remoteFieldRanks);
    }

    private String fetchContentsWithEntryId(DbDto.Topic topic, long entryId, List<Integer> fieldRanks) {
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return "??";
        }

        List<String> contents = fieldRanks.stream()

                .map((fieldRank) -> {
                    Optional<DbResourceDto.Entry> potentialRemoteResourceEntry = this.getMiner().getRemoteResourceEntryWithInternalIdentifier(topic, fieldRank, entryId, currentLocaleProperty.getValue());
                    if (potentialRemoteResourceEntry.isPresent()) {
                        return potentialRemoteResourceEntry.get().getValue();
                    }
                    return "??";
                })

                .collect(toList());

        return String.join(" - ", contents);
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

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}