package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.interop.TdupePerformancePackConverter;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
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

    void importPerformancePack(String packFile) {
        requireNonNull(getMiner());
        // TODO extract to helper and share with CLI-importTdupk
        String packLine = readLineFromPerformancePack(packFile);
        checkCarPhysicsDataLine(packLine);

        DbDto carPhysicsDataTopicObject = getMiner().getDatabaseTopic(CAR_PHYSICS_DATA).get();
        Optional<String> potentialCarPhysicsRef = getMiner().getContentEntryReferenceWithInternalIdentifier(mainStageController.currentEntryIndexProperty.getValue(), CAR_PHYSICS_DATA);
        DbPatchDto patchObject = TdupePerformancePackConverter.tdupkToJson(packLine, potentialCarPhysicsRef, carPhysicsDataTopicObject);

        try {
            AbstractDatabaseHolder.prepare(DatabasePatcher.class, mainStageController.getDatabaseObjects()).apply(patchObject);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Unable to apply patch.", roe);
        }
    }

    static void checkCarPhysicsDataLine(String carPhysicsDataLine) {
        Pattern linePattern = Pattern.compile("^([0-9\\-\\.,]*;){103}$");

        if (!linePattern.matcher(carPhysicsDataLine).matches()) {
            throw new RuntimeException("Unrecognized Car Physics line: " + carPhysicsDataLine);
        }
    }

    private static String readLineFromPerformancePack(String ppFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(ppFile));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to read performance pack file: " + ppFile, ioe);
        }

        return lines.get(0);
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