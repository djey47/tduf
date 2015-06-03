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
            mainStageController.getViewDataController().updateItemProperties(contentItem);
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

    void addLinkedEntry(String sourceEntryRef, Optional<String> targetEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getGenHelper());
        DbDataDto.Entry newEntry = getGenHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), targetTopic);

        // FIXME we assume source reference is first field ...
        newEntry.getItems().get(0).setRawValue(sourceEntryRef);
        // FIXME we assume target reference is second field ...
        targetEntryRef.ifPresent((ref) -> newEntry.getItems().get(1).setRawValue(ref));
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