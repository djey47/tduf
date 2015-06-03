package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Specialized controller to update database contents.
 */
public class MainStageChangeDataController {
    private final MainStageController mainStageController;

    MainStageChangeDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void updateContentItem(DbDto.Topic topic, int fieldRank, String newRawValue) {
        DbDataDto.Item contentItem = getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, mainStageController.currentEntryIndexProperty.getValue()).get();

        if (!contentItem.getRawValue().equals(newRawValue)) {
            contentItem.setRawValue(newRawValue);

            // TODO see to update item properties automatically upon property change
            this.mainStageController.getViewDataController().updateItemProperties(contentItem);
        }
    }

    void removeResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, boolean forAllLocales) {
        List<DbResourceDto.Locale> affectedLocales = singletonList(locale);
        if (forAllLocales) {
            affectedLocales = asList(DbResourceDto.Locale.values());
        }

        // TODO extract to lib -> modifier?
        affectedLocales.stream()

                .map((affectedLocale) -> getMiner().getResourceFromTopicAndLocale(topic, locale).get().getEntries())

                .forEach((resources) -> resources.stream()

                        .filter((resource) -> resource.getReference().equals(resourceReference))

                        .findAny()

                        .ifPresent(resources::remove));
    }

    // TODO extract to lib -> modifier?
    void removeEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        List<DbDataDto.Entry> topicEntries = getMiner().getDatabaseTopic(topic).get().getData().getEntries();
        topicEntries.stream()

                .filter((entry) -> entry.getId() == entryId)

                .findAny()

                .ifPresent(topicEntries::remove);
    }

    void addLinkedEntry(String sourceEntryRef, DbDto.Topic targetTopic) {

        DatabaseHelper databaseGenHelper = new DatabaseHelper(getMiner());

        DbDataDto.Entry newEntry = databaseGenHelper.addContentsEntryWithDefaultItems(Optional.<String>empty(), targetTopic);
        // FIXME we assume source reference is first field ...
        newEntry.getItems().get(0).setRawValue(sourceEntryRef);
    }

    void addLinkedEntry(String sourceEntryRef, String targetEntryRef, DbDto.Topic targetTopic) {

        DbDto targetTopicObject = getMiner().getDatabaseTopic(targetTopic).get();
        List<DbStructureDto.Field> structureFields = targetTopicObject.getStructure().getFields();
        DbStructureDto.Field sourceStructureField = structureFields.get(0);
        DbStructureDto.Field targetStructureField = structureFields.get(1);

        List<DbDataDto.Entry> linkedEntries = targetTopicObject.getData().getEntries();

        DbDataDto.Item sourceEntryRefItem = DbDataDto.Item.builder()
                .fromStructureField(sourceStructureField)
                .withRawValue(sourceEntryRef)
                .build();
        DbDataDto.Item targetEntryRefItem = DbDataDto.Item.builder()
                .fromStructureField(targetStructureField)
                .withRawValue(targetEntryRef)
                .build();
        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(linkedEntries.size())
                .addItem(sourceEntryRefItem, targetEntryRefItem)
                .build();

        linkedEntries.add(newEntry);
    }

    // TODO use helper
    void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        checkResourceDoesNotExistWithReference(topic, locale, newResourceReference);

        DbResourceDto.Entry existingResourceEntry = getMiner().getResourceEntryFromTopicAndLocaleWithReference(oldResourceReference, topic, locale).get();

        existingResourceEntry.setReference(newResourceReference);
        existingResourceEntry.setValue(newResourceValue);
    }

    // TODO use helper
    void addResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, String resourceValue) {
        checkResourceDoesNotExistWithReference(topic, locale, resourceReference);

        List<DbResourceDto.Entry> resourceEntries = getMiner().getResourceFromTopicAndLocale(topic, locale).get().getEntries();

        resourceEntries.add(DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue(resourceValue)
                .build());
    }

    private void checkResourceDoesNotExistWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference) {
        getMiner().getResourceEntryFromTopicAndLocaleWithReference(resourceReference, topic, locale)
                .ifPresent((resourceEntry) -> {
                    throw new IllegalArgumentException("Resource already exists with reference: " + resourceReference);
                });
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }
}