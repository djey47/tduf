package fr.tduf.gui.database.controllers;

import com.esotericsoftware.minlog.Log;
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
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.fromCollection;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to update database contents.
 */
public class MainStageChangeDataController {
    private static final String THIS_CLASS_NAME = MainStageChangeDataController.class.getSimpleName();

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
        requireNonNull(getChangeHelper());
        getChangeHelper().updateResourceWithReference(topic, locale, oldResourceReference, newResourceReference, newResourceValue);
    }

    void removeEntryWithIdentifier(long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    void moveEntryWithIdentifier(int step, long internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().moveEntryWithIdentifier(step, internalEntryId, topic);
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
        PatchProperties patchProperties = readPatchProperties(patchFile);

        final PatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

        return writePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
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

    private static PatchProperties readPatchProperties(File patchFile) throws IOException {
        String propertyFile = patchFile + ".properties";

        final PatchProperties patchProperties = new PatchProperties();
        final File propertyFileHandle = new File(propertyFile);
        if(propertyFileHandle.exists()) {

            Log.info(THIS_CLASS_NAME, "Using patch properties file: " + propertyFile);

            final InputStream inputStream = new FileInputStream(propertyFileHandle);
            patchProperties.load(inputStream);

        } else {

            Log.info(THIS_CLASS_NAME, "Patch properties file not provided: " + propertyFile);

        }

        return patchProperties;
    }

    private static Optional<String> writePatchProperties(PatchProperties patchProperties, String patchFile) throws IOException {
        if (patchProperties.isEmpty()) {
            return empty();
        }

        final Path patchPath = Paths.get(patchFile);
        Path patchParentPath = patchPath.getParent();
        String patchFileName = patchPath.getFileName().toString();
        final String targetFileName = "effective-" + patchFileName + ".properties";
        String targetPropertyFile = patchParentPath.resolve(targetFileName).toString();

        Log.info(THIS_CLASS_NAME, "Writing properties file: " + targetPropertyFile);

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);

        return of(targetPropertyFile);
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
