package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

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

    private DatabaseGenHelper databaseGenHelper;

    MainStageChangeDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void updateContentItem(DbDto.Topic topic, int fieldRank, String newRawValue) {
        DbDataDto.Item contentItem = getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, mainStageController.currentEntryIndexProperty.getValue()).get();
        if (contentItem.getRawValue().equals(newRawValue)) {
            return;
        }

        contentItem.setRawValue(newRawValue);
        mainStageController.getViewDataController().updateItemProperties(contentItem);
    }

    void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        requireNonNull(getGenHelper());
        getChangeHelper().updateResourceWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
    }

    void removeEntryWithIdentifier(long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getGenHelper());
        getChangeHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    void removeResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, boolean forAllLocales) {
        List<DbResourceDto.Locale> affectedLocales = singletonList(locale);
        if (forAllLocales) {
            affectedLocales = asList(DbResourceDto.Locale.values());
        }

        getChangeHelper().removeResourcesWithReference(topic, locale, resourceReference, affectedLocales);
    }

    long addEntryForCurrentTopic() {
        requireNonNull(getChangeHelper());
        DbDataDto.Entry newEntry = getChangeHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), mainStageController.currentTopicProperty.getValue());

        return newEntry.getId();
    }

    long duplicateCurrentEntry() {
        requireNonNull(getChangeHelper());
        DbDataDto.Entry newEntry = getChangeHelper().duplicateEntryWithIdentifier(
                mainStageController.currentEntryIndexProperty.getValue(),
                mainStageController.currentTopicProperty.getValue());

        return newEntry.getId();
    }

    void addLinkedEntry(String sourceEntryRef, Optional<String> targetEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getChangeHelper());
        DbDataDto.Entry newEntry = getChangeHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), targetTopic);
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(newEntry, sourceEntryRef, targetEntryRef);
    }

    void addResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String newResourceReference, String newResourceValue) {
        requireNonNull(getGenHelper());
        getChangeHelper().addResourceWithReference(topic, locale, newResourceReference, newResourceValue);
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getMiner();
    }

    private DatabaseGenHelper getGenHelper() {

        if (databaseGenHelper == null) {
            if (getMiner() == null) {
                return null;
            }

            databaseGenHelper = new DatabaseGenHelper(getMiner());
        }
        return databaseGenHelper;
    }

    private DatabaseChangeHelper getChangeHelper() {
        requireNonNull(getGenHelper());

        return getGenHelper().getChangeHelper();
    }
}