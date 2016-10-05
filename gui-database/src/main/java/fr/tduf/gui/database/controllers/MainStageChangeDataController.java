package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.interop.TdumtPatchConverter;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
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
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to update database contents.
 */
class MainStageChangeDataController extends AbstractMainStageSubController {
    private static final String THIS_CLASS_NAME = MainStageChangeDataController.class.getSimpleName();

    private DatabaseGenHelper databaseGenHelper;

    MainStageChangeDataController(MainStageController mainStageController) {
        super(mainStageController);
    }

    void updateContentItem(DbDto.Topic topic, int fieldRank, String newRawValue) {
        requireNonNull(getChangeHelper());

        final int currentEntryIndex = currentEntryIndexProperty().getValue();
        getChangeHelper().updateItemRawValueAtIndexAndFieldRank(topic, currentEntryIndex, fieldRank, newRawValue)
                .ifPresent(updatedItem -> {
                    getViewDataController().updateItemProperties(updatedItem);
                    getViewDataController().updateBrowsableEntryLabel(currentEntryIndex);
                    getViewDataController().updateCurrentEntryLabelProperty();
                });
    }

    void updateResourceWithReference(DbDto.Topic topic, Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        requireNonNull(getChangeHelper());

        if (newResourceReference.equals(oldResourceReference)) {
            // TODO create separate methods
            getChangeHelper().updateResourceItemWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
        } else {
            // TODO create separate methods
            getChangeHelper().updateResourceItemWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
        }
    }

    void removeEntryWithIdentifier(int internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    void moveEntryWithIdentifier(int step, int internalEntryId, DbDto.Topic topic) {
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

    int addEntryForCurrentTopic() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(empty(), currentTopicProperty().getValue());

        return newEntry.getId();
    }

    int duplicateCurrentEntry() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().duplicateEntryWithIdentifier(
                currentEntryIndexProperty().getValue(),
                currentTopicProperty().getValue());

        return newEntry.getId();
    }

    void addLinkedEntryWithTargetRef(DbDto.Topic targetTopic, ContentEntryDataItem linkedEntry) {
        String sourceEntryRef = getMiner().getContentEntryReferenceWithInternalIdentifier(currentEntryIndexProperty().getValue(), currentTopicProperty().getValue()).get();
        Optional<String> targetEntryRef = ofNullable(linkedEntry)
                .map(entry -> entry.referenceProperty().get());

        addLinkedEntry(sourceEntryRef, targetEntryRef, targetTopic);
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
                currentEntryIndexProperty().getValue(),
                currentTopicProperty().getValue());

        return TdumtPatchConverter.getContentsValue(potentialRef, values);
    }

    boolean exportEntriesToPatchFile(DbDto.Topic currentTopic, List<String> entryReferences, List<String> entryFields, String patchFileLocation) throws IOException {
        return generatePatchObject(currentTopic, entryReferences, entryFields, getDatabaseObjects())
                .map(patchObject -> {
                    try {
                        FilesHelper.writeJsonObjectToFile(patchObject, patchFileLocation);
                    } catch (IOException ioe) {
                        Log.warn(THIS_CLASS_NAME, "Unable to write patch object to file: " + patchFileLocation, ioe);
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    Optional<String> importPatch(File patchFile) throws IOException, ReflectiveOperationException {
        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseObjects());
        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);

        final PatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
    }

    void importPerformancePack(String packFile) throws ReflectiveOperationException {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        TdupeGateway gateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, getDatabaseObjects());
        gateway.applyPerformancePackToEntryWithIdentifier(currentEntryIndex, packFile);
    }

    private List<String> getRawValuesFromCurrentEntry() {
        final DbDto.Topic currentTopic = currentTopicProperty().getValue();
        final int currentEntryIndex = getCurrentEntryIndex();
        ContentEntryDto currentEntry = getMiner().getContentEntryFromTopicWithInternalIdentifier(
                currentEntryIndex,
                currentTopic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No content entry for topic: " + currentTopic + " at id: " + currentEntryIndex));
        return currentEntry.getItems().stream()

                .map(ContentItemDto::getRawValue)

                .collect(toList());
    }

    private void addLinkedEntry(String sourceEntryRef, Optional<String> targetEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(empty(), targetTopic);
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(newEntry, sourceEntryRef, targetEntryRef);
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
            Log.warn(THIS_CLASS_NAME, "Unable to generate patch object", e);
            return empty();
        }
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
