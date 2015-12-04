package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.interop.TdumtPatchConverter;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

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
        DbDataDto.Item contentItem = getMiner().getContentItemWithEntryIdentifierAndFieldRank(topic, fieldRank, mainStageController.currentEntryIndexProperty.getValue()).get();
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

        getChangeHelper().removeResourcesWithReference(topic, resourceReference, affectedLocales);
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

    String exportCurrentEntryAsLine() {
        List<String> values = getRawValuesFromCurrentEntry();

        return String.join(DatabaseParser.VALUE_DELIMITER, values);
    }

    String exportCurrentEntryToPchValue() {
        List<String> values = getRawValuesFromCurrentEntry();
        Optional<String> potentialRef = getMiner().getContentEntryReferenceWithInternalIdentifier(
                mainStageController.currentEntryIndexProperty.getValue(),
                mainStageController.currentTopicProperty.getValue());

        return TdumtPatchConverter.getContentsValue(potentialRef, values);
    }

    boolean exportEntryToPatchFile(DbDto.Topic currentTopic, Optional<String> potentialEntryRef, String patchFileLocation) throws IOException {
        Optional<DbPatchDto> potentialPatchObject = potentialEntryRef
                .map((entryRef) -> generatePatchObject(currentTopic, entryRef, mainStageController.getDatabaseObjects()));

        if (potentialPatchObject.isPresent()) {
            FilesHelper.writeJsonObjectToFile(potentialPatchObject.get(), patchFileLocation);
            return true;
        }

        return false;
    }

    void importPatch(File patchFile) throws IOException, ReflectiveOperationException {
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, mainStageController.getDatabaseObjects());
        patcher.apply(patchObject);
    }

    void importPerformancePack(String packFile) throws ReflectiveOperationException {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        TdupeGateway gateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, mainStageController.getDatabaseObjects());
        gateway.applyPerformancePackToEntryWithIdentifier(currentEntryIndex, packFile);
    }

    private List<String> getRawValuesFromCurrentEntry() {
        DbDataDto.Entry currentEntry = getMiner().getContentEntryFromTopicWithInternalIdentifier(
                mainStageController.currentEntryIndexProperty.getValue(),
                mainStageController.currentTopicProperty.getValue()).get();
        return currentEntry.getItems().stream()

                .map(DbDataDto.Item::getRawValue)

                .collect(toList());
    }

    private static DbPatchDto generatePatchObject(DbDto.Topic currentTopic, String entryRef, List<DbDto> databaseObjects) {
        try {
            PatchGenerator patchGenerator = AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseObjects);
            ItemRange range = ItemRange.fromCollection(Collections.singletonList(entryRef));
            return patchGenerator.makePatch(currentTopic, range, ALL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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