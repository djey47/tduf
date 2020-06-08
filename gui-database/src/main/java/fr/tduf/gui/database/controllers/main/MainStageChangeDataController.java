package fr.tduf.gui.database.controllers.main;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.gui.common.javafx.helper.TableViewHelper;
import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import fr.tduf.gui.database.dto.TopicLinkDto;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.interop.TdumtPatchConverter;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseParser;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.database.common.SupportConstants.LOG_TARGET_PROFILE_NAME;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.fromCollection;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

/**
 * Specialized controller to update database contents.
 */
public class MainStageChangeDataController extends AbstractMainStageSubController {
    private static final String THIS_CLASS_NAME = MainStageChangeDataController.class.getSimpleName();

    private DatabaseGenHelper databaseGenHelper;

    MainStageChangeDataController(MainStageController mainStageController) {
        super(mainStageController);
    }

    /**
     * Only use when unit testing
     * @return a special instance
     */
    public static MainStageChangeDataController testingInstance() {
        return new MainStageChangeDataController(new MainStageController());
    }

    public void updateContentItem(DbDto.Topic topic, int fieldRank, String newRawValue) {
        requireNonNull(getChangeHelper());

        final int currentEntryIndex = currentEntryIndexProperty().getValue();
        getChangeHelper().updateItemRawValueAtIndexAndFieldRank(topic, currentEntryIndex, fieldRank, newRawValue)
                .ifPresent(updatedItem -> {
                    getViewDataController().updateItemProperties(updatedItem);
                    getViewDataController().updateBrowsableEntryLabel(currentEntryIndex);
                    getViewDataController().updateCurrentEntryLabelProperty();
                });
    }

    public EventHandler<ActionEvent> handleAddLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, String targetProfileName, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleAddLinkedEntryButton clicked, targetTopic:" + targetTopic + LOG_TARGET_PROFILE_NAME + targetProfileName);

            List<DbStructureDto.Field> structureFields = getMiner().getDatabaseTopic(targetTopic)
                    .orElseThrow(() -> new IllegalStateException("Database object not found for topic: " + targetTopic))
                    .getStructure().getFields();
            if (DatabaseStructureQueryHelper.getUidFieldRank(structureFields).isPresent()) {
                // Association topic -> browse remote entries in target topic
                getEntriesStageController().initAndShowModalDialog(targetTopic, targetProfileName)
                        .ifPresent(selectedEntry -> addLinkedEntryWithTargetRefAndUpdateStage(tableViewSelectionModel, topicLinkObject.getTopic(), selectedEntry, topicLinkObject));
            } else {
                // Direct topic link -> add default entry in target topic
                addLinkedEntryWithoutTargetRefAndUpdateStage(tableViewSelectionModel, targetTopic, topicLinkObject);
            }
        };
    }

    public EventHandler<ActionEvent> handleRemoveLinkedEntryButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleRemoveLinkedEntryButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> removeLinkedEntryAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryUpButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryUpButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> moveLinkedEntryUpAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public EventHandler<ActionEvent> handleMoveLinkedEntryDownButtonMouseClick(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        return actionEvent -> {
            Log.trace(THIS_CLASS_NAME, "->handleMoveLinkedEntryDownButton clicked");

            ofNullable(tableViewSelectionModel.getSelectedItem())
                    .ifPresent(selectedItem -> moveLinkedEntryDownAndUpdateStage(tableViewSelectionModel, topicLinkObject));
        };
    }

    public ChangeListener<Boolean> handleTextFieldFocusChange(int fieldRank, StringProperty textFieldValueProperty) {
        return (observable, oldFocusState, newFocusState) -> {
            Log.trace(THIS_CLASS_NAME, "->handleTextFieldFocusChange, focused=" + newFocusState + ", fieldRank=" + fieldRank + ", fieldValue=" + textFieldValueProperty.get());

            if (oldFocusState && !newFocusState) {
                updateContentItem(getCurrentTopic(), fieldRank, textFieldValueProperty.get());
            }
        };
    }

    public void updateResourceWithReferenceForLocale(DbDto.Topic topic, Locale locale, String resourceReference, String newResourceValue) {
        requireNonNull(getChangeHelper());

        getChangeHelper().updateResourceItemWithReference(topic, locale, resourceReference, newResourceValue);
    }

    public void updateResourceWithReferenceForAllLocales(DbDto.Topic topic, String oldResourceReference, String newResourceReference, String newResourceValue) {
        requireNonNull(getChangeHelper());

        getChangeHelper().updateResourceEntryWithReference(topic, oldResourceReference, newResourceReference, newResourceValue);
    }

    public void updateResourceWithReferenceForAllLocales(DbDto.Topic topic, String resourceReference, String newResourceValue) {
        Locale.valuesAsStream()
                .forEach(affectedLocale -> updateResourceWithReferenceForLocale(topic, affectedLocale, resourceReference, newResourceValue));
    }

    void removeEntryWithIdentifier(int internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().removeEntryWithIdentifier(internalEntryId, topic);
    }

    public void removeResourceWithReference(DbDto.Topic topic, String resourceReference) {
        requireNonNull(getChangeHelper());
        getChangeHelper().removeResourceEntryWithReference(topic, resourceReference);
    }

    int addEntryForCurrentTopic() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(currentTopicProperty().getValue());

        return newEntry.getId();
    }

    int duplicateCurrentEntry() {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().duplicateEntryWithIdentifier(
                currentEntryIndexProperty().getValue(),
                currentTopicProperty().getValue());

        return newEntry.getId();
    }

    public void addResourceWithReference(DbDto.Topic topic, Locale locale, String newResourceReference, String newResourceValue) {
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

        return TdumtPatchConverter.getContentsValue(potentialRef.orElse(null), values);
    }

    boolean exportEntriesToPatchFile(DbDto.Topic currentTopic, List<String> entryReferences, List<String> entryFields, String patchFileLocation) {
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
        DatabasePatchProperties patchProperties = PatchPropertiesReadWriteHelper.readDatabasePatchProperties(patchFile);

        final DatabasePatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
    }

    void importPerformancePack(String packFile) throws ReflectiveOperationException {
        int currentEntryIndex = currentEntryIndexProperty().getValue();
        TdupeGateway gateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, getDatabaseObjects());
        gateway.applyPerformancePackToEntryWithIdentifier(currentEntryIndex, packFile);
    }

    void importLegacyPatch(String patchFile) throws IOException, ReflectiveOperationException {
        Document patchDocument = FilesHelper.readXMLDocumentFromFile(patchFile);

        final DbPatchDto patchObject = TdumtPatchConverter.pchToJson(patchDocument);
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseObjects());
        patcher.apply(patchObject);
    }

    private void moveEntryWithIdentifier(int step, int internalEntryId, DbDto.Topic topic) {
        requireNonNull(getChangeHelper());
        getChangeHelper().moveEntryWithIdentifier(step, internalEntryId, topic);
    }

    private List<String> getRawValuesFromCurrentEntry() {
        final DbDto.Topic currentTopic = currentTopicProperty().getValue();
        final int currentEntryIndex = getCurrentEntryIndex();
        ContentEntryDto currentEntry = getMiner().getContentEntryFromTopicWithInternalIdentifier(
                currentEntryIndex,
                currentTopic)
                .orElseThrow(() -> new IllegalStateException("No content entry for topic: " + currentTopic + " at id: " + currentEntryIndex));
        return currentEntry.getItems().stream()

                .map(ContentItemDto::getRawValue)

                .collect(toList());
    }

    private void addLinkedEntryWithTargetRef(DbDto.Topic targetTopic, ContentEntryDataItem linkedEntry) {
        int entryIdentifier = currentEntryIndexProperty().getValue();
        String sourceEntryRef = getMiner().getContentEntryReferenceWithInternalIdentifier(entryIdentifier, currentTopicProperty().getValue())
                .orElseThrow(() -> new IllegalStateException("No content entry ref with identifier: " + entryIdentifier));
        String targetEntryRef = linkedEntry == null ? null : linkedEntry.referenceProperty().get();

        addLinkedEntry(sourceEntryRef, targetEntryRef, targetTopic);
    }

    private void addLinkedEntryWithTargetRefAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, ContentEntryDataItem linkedEntry, TopicLinkDto topicLinkObject) {
        addLinkedEntryWithTargetRef(targetTopic, linkedEntry);
        getViewDataController().updateAllLinkProperties(topicLinkObject);
        TableViewHelper.selectLastRowAndScroll(tableViewSelectionModel.getTableView());
    }


    private void addLinkedEntryWithoutTargetRefAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, DbDto.Topic targetTopic, TopicLinkDto topicLinkObject) {
        addLinkedEntryWithTargetRefAndUpdateStage(tableViewSelectionModel, targetTopic, null, topicLinkObject);
    }

    private void addLinkedEntry(String sourceEntryRef, String targetEntryRef, DbDto.Topic targetTopic) {
        requireNonNull(getChangeHelper());
        ContentEntryDto newEntry = getChangeHelper().addContentsEntryWithDefaultItems(targetTopic);
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(newEntry, sourceEntryRef, targetEntryRef);
    }

    private void removeLinkedEntryAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        ContentEntryDataItem selectedItem = tableViewSelectionModel.getSelectedItem();
        removeEntryWithIdentifier(selectedItem.internalEntryIdProperty().get(), topicLinkObject.getTopic());
        getViewDataController().updateAllLinkProperties(topicLinkObject);
        TableViewHelper.selectRowAndScroll(tableViewSelectionModel.getSelectedIndex(), tableViewSelectionModel.getTableView());
    }


    private void moveLinkedEntryUpAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == 0) {
            return;
        }

        moveEntryWithIdentifier(-1, tableViewSelectionModel.getSelectedItem().internalEntryIdProperty().get(), topicLinkObject.getTopic());
        getViewDataController().updateAllLinkProperties(topicLinkObject);
        TableViewHelper.selectRowAndScroll(initialRowIndex - 1, tableView);
    }

    private void moveLinkedEntryDownAndUpdateStage(TableView.TableViewSelectionModel<ContentEntryDataItem> tableViewSelectionModel, TopicLinkDto topicLinkObject) {
        final TableView<ContentEntryDataItem> tableView = tableViewSelectionModel.getTableView();
        int initialRowIndex = tableViewSelectionModel.getSelectedIndex();
        if (initialRowIndex == tableView.getItems().size() - 1) {
            return;
        }

        moveEntryWithIdentifier(1, tableViewSelectionModel.getSelectedItem().internalEntryIdProperty().get(), topicLinkObject.getTopic());
        getViewDataController().updateAllLinkProperties(topicLinkObject);
        TableViewHelper.selectRowAndScroll(initialRowIndex + 1, tableView);
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

    void setGenHelper(DatabaseGenHelper genHelper) {
        databaseGenHelper = genHelper;
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