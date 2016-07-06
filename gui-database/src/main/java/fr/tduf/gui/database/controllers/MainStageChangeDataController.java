package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.common.game.domain.Locale;
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
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.fromCollection;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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
        requireNonNull(getChangeHelper());

        final long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        getChangeHelper().updateItemRawValueAtIndexAndFieldRank(topic, currentEntryIndex, fieldRank, newRawValue)
                .ifPresent((updatedItem) -> {
                    mainStageController.getViewDataController().updateItemProperties(updatedItem);
                    mainStageController.getViewDataController().updateBrowsableEntryLabel(currentEntryIndex);
                    mainStageController.getViewDataController().updateCurrentEntryLabelProperty();
                });
    }

    void updateResourceWithReference(DbDto.Topic topic, Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        requireNonNull(getChangeHelper());
        getChangeHelper().updateResourceItemWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
    }

    void removeEntryWithIdentifier(long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    void moveEntryWithIdentifier(int step, long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().moveEntryWithIdentifier(step, internalEntryId, topic);
    }

    void removeResourceWithReference(DbDto.Topic topic, Locale locale, String resourceReference, boolean forAllLocales) {
        List<Locale> affectedLocales = singletonList(locale);
        if (forAllLocales) {
            affectedLocales = Locale.valuesAsStream().collect(toList());
        }

        getChangeHelper().removeResourceValuesWithReference(topic, resourceReference, affectedLocales);
    }

    long addEntryForCurrentTopic() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), mainStageController.currentTopicProperty.getValue());

        return newEntry.getId();
    }

    long duplicateCurrentEntry() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().duplicateEntryWithIdentifier(
                mainStageController.currentEntryIndexProperty.getValue(),
                mainStageController.currentTopicProperty.getValue());

        return newEntry.getId();
    }

    void addLinkedEntry(String sourceEntryRef, Optional<String> targetEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(Optional.<String>empty(), targetTopic);
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(newEntry, sourceEntryRef, targetEntryRef);
    }

    void addResourceWithReference(DbDto.Topic topic, Locale locale, String newResourceReference, String newResourceValue) {
        requireNonNull(getGenHelper());
        getChangeHelper().addResourceValueWithReference(topic, locale, newResourceReference, newResourceValue);
    }

    String exportCurrentEntryAsLine() {
        List<String> values = getRawValuesFromCurrentEntry();

        final String line = String.join(DatabaseParser.VALUE_DELIMITER, values);
        return line.endsWith(DatabaseParser.VALUE_DELIMITER) ? line : line + DatabaseParser.VALUE_DELIMITER;
    }

    String exportCurrentEntryToPchValue() {
        List<String> values = getRawValuesFromCurrentEntry();
        Optional<String> potentialRef = getMiner().getContentEntryReferenceWithInternalIdentifier(
                mainStageController.currentEntryIndexProperty.getValue(),
                mainStageController.currentTopicProperty.getValue());

        return TdumtPatchConverter.getContentsValue(potentialRef, values);
    }

    boolean exportEntriesToPatchFile(DbDto.Topic currentTopic, List<String> entryReferences, List<String> entryFields, String patchFileLocation) throws IOException {

        return generatePatchObject(currentTopic, entryReferences, entryFields, mainStageController.getDatabaseObjects())

                .map((patchObject) -> {
                    try {
                        FilesHelper.writeJsonObjectToFile(patchObject, patchFileLocation);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                })

                .orElse(false);
    }

    Optional<String> importPatch(File patchFile) throws IOException, ReflectiveOperationException {
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, mainStageController.getDatabaseObjects());
        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);

        final PatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
    }

    void importPerformancePack(String packFile) throws ReflectiveOperationException {
        long currentEntryIndex = mainStageController.currentEntryIndexProperty.getValue();
        TdupeGateway gateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, mainStageController.getDatabaseObjects());
        gateway.applyPerformancePackToEntryWithIdentifier(currentEntryIndex, packFile);
    }

    private List<String> getRawValuesFromCurrentEntry() {
        ContentEntryDto currentEntry = getMiner().getContentEntryFromTopicWithInternalIdentifier(
                mainStageController.getCurrentEntryIndex(),
                mainStageController.getCurrentTopic()).get();
        return currentEntry.getItems().stream()

                .map(ContentItemDto::getRawValue)

                .collect(toList());
    }

    private static Optional<DbPatchDto> generatePatchObject(DbDto.Topic currentTopic, List<String> entryReferences, List<String> entryFields, List<DbDto> databaseObjects) {
        try {
            PatchGenerator patchGenerator = AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseObjects);

            final ItemRange refRange = entryReferences.isEmpty() ?
                    ALL :
                    fromCollection(entryReferences);
            final ItemRange fieldRange = entryFields.isEmpty() ?
                    ALL :
                    fromCollection(entryFields);

            return of(patchGenerator.makePatch(currentTopic, refRange, fieldRange));
        } catch (Exception e) {
            e.printStackTrace();
            return empty();
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
