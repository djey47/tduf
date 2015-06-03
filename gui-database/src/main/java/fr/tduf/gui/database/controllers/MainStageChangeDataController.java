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

    private DatabaseHelper databaseGenHelper;

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

    void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        requireNonNull(getGenHelper());
        getGenHelper().updateResourceWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
    }

    void removeEntryWithIdentifier(long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getGenHelper());
        getGenHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    void removeResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, boolean forAllLocales) {
        List<DbResourceDto.Locale> affectedLocales = singletonList(locale);
        if (forAllLocales) {
            affectedLocales = asList(DbResourceDto.Locale.values());
        }

        databaseGenHelper.removeResourcesWithReference(topic, locale, resourceReference, affectedLocales);
    }

    void addLinkedEntry(String sourceEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getGenHelper());
        DbDataDto.Entry newEntry = getGenHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), targetTopic);
        // FIXME we assume source reference is first field ...
        newEntry.getItems().get(0).setRawValue(sourceEntryRef);
    }

    void addLinkedEntry(String sourceEntryRef, String targetEntryRef, DbDto.Topic targetTopic) {

        // TODO see to use DatabaseGenHelper
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

    void addResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String newResourceReference, String newResourceValue) {
        requireNonNull(getGenHelper());
        getGenHelper().addResourceWithReference(topic, locale, newResourceReference, newResourceValue);
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }

    private DatabaseHelper getGenHelper() {

        if (databaseGenHelper == null) {
            if (getMiner() == null) {
                return null;
            }

            databaseGenHelper = new DatabaseHelper(getMiner());
        }
        return databaseGenHelper;
    }
}