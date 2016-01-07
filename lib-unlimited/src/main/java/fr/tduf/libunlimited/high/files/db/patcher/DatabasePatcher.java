package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patches to an existing database.
 */
public class DatabasePatcher extends AbstractDatabaseHolder {

    private DatabaseChangeHelper databaseChangeHelper;

    /**
     * Execute provided patch onto current database.
     */
    public void apply(DbPatchDto patchObject) {
        requireNonNull(patchObject, "A patch object is required.");

        patchObject.getChanges()

                .forEach(this::applyChange);
    }

    @Override
    protected void postPrepare() {
        databaseChangeHelper = new DatabaseChangeHelper(databaseMiner);
    }

    private void applyChange(DbPatchDto.DbChangeDto changeObject) {

        DbPatchDto.DbChangeDto.ChangeTypeEnum changeType = changeObject.getType();

        switch (changeType) {
            case UPDATE_RES:
                addOrUpdateResources(changeObject);
                break;
            case DELETE_RES:
                deleteResources(changeObject);
                break;
            case UPDATE:
                addOrUpdateContents(changeObject);
                break;
            case DELETE:
                deleteContents(changeObject);
                break;
        }

        BulkDatabaseMiner.clearAllCaches();
    }

    private void deleteContents(DbPatchDto.DbChangeDto changeObject) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        Optional<String> potentialRef = Optional.ofNullable(changeObject.getRef());

        if (potentialRef.isPresent()) {
            databaseChangeHelper.removeEntryWithReference(potentialRef.get(), changedTopic);
        } else {
            requireNonNull(changeObject.getFilterCompounds(), "As no REF is provided, filter attribute is mandatory.");
            databaseChangeHelper.removeEntriesMatchingCriteria(changeObject.getFilterCompounds(), changedTopic);
        }
    }

    private void addOrUpdateContents(DbPatchDto.DbChangeDto changeObject) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        databaseMiner.getDatabaseTopic(changedTopic)
                .ifPresent((topicObject) -> {

                    Optional<DbDataDto.Entry> potentialEntry = retrieveExistingEntry(changeObject, changedTopic);

                    if (changeObject.isPartialChange()) {

                        List<DbFieldValueDto> partialValues = changeObject.getPartialValues();
                        if (potentialEntry.isPresent()) {
                            updateEntryWithPartialChanges(potentialEntry.get(), topicObject.getStructure(), partialValues);
                        } else {
                            updateEntriesMatchingCriteriaWithPartialChanges(changeObject, changedTopic, topicObject, partialValues);
                        }

                    } else {

                        addOrUpdateEntryWithFullChanges(potentialEntry, topicObject, changeObject.getValues());

                    }
                });
    }

    private Optional<DbDataDto.Entry> retrieveExistingEntry(DbPatchDto.DbChangeDto changeObject, DbDto.Topic changedTopic) {
        final Optional<String> potentialReference = ofNullable(changeObject.getRef());
        if (potentialReference.isPresent()) {
            return databaseMiner.getContentEntryFromTopicWithReference(potentialReference.get(), changedTopic);
        }

        if (changeObject.isPartialChange() || changeObject.getValues() == null) {
            return empty();
        }

        AtomicInteger fieldRank = new AtomicInteger(1);
        List<DbFieldValueDto> fullCriteria = changeObject.getValues().stream()

                .map( (rawValue) -> DbFieldValueDto.fromCouple(fieldRank.getAndIncrement(), rawValue))

                .collect(toList());

        return databaseMiner.getContentEntriesMatchingCriteria(fullCriteria, changedTopic).stream().findAny();
    }

    private void addOrUpdateEntryWithFullChanges(Optional<DbDataDto.Entry> existingEntry, DbDto topicObject, List<String> allValues) {
        List<DbDataDto.Item> modifiedItems = createEntryItemsWithValues(topicObject.getStructure(), allValues);

        if (existingEntry.isPresent()) {

            existingEntry.get().replaceItems(modifiedItems);

        } else {

            topicObject.getData().addEntryWithItems(modifiedItems);

        }
    }

    private void updateEntryWithPartialChanges(DbDataDto.Entry existingEntry, DbStructureDto structureObject, List<DbFieldValueDto> partialValues) {
        List<DbDataDto.Item> modifiedItems = createEntryItemsWithPartialValues(structureObject, existingEntry, partialValues);
        existingEntry.replaceItems(modifiedItems);
    }

    private void updateEntriesMatchingCriteriaWithPartialChanges(DbPatchDto.DbChangeDto changeObject, DbDto.Topic changedTopic, DbDto topicObject, List<DbFieldValueDto> partialValues) {
        List<DbFieldValueDto> filterCompounds = changeObject.getFilterCompounds();
        if (filterCompounds == null) {
            Log.warn("No entry to be updated with partial values: " + partialValues + ", using no filter.");
            return;
        }

        List<DbDataDto.Entry> entries = databaseMiner.getContentEntriesMatchingCriteria(filterCompounds, changedTopic);
        if (entries.isEmpty()) {
            Log.warn("No entry to be updated with partial values: " + partialValues + ", using filter: " + filterCompounds);
            return;
        }

        entries.forEach((entry) -> updateEntryWithPartialChanges(entry, topicObject.getStructure(), partialValues));
    }

    private void deleteResources(DbPatchDto.DbChangeDto changeObject) {
        Optional<DbResourceDto.Locale> potentialLocale = ofNullable(changeObject.getLocale());

        if (potentialLocale.isPresent()) {

            deleteResourcesForLocale(changeObject, potentialLocale.get());

        } else {

            Stream.of(DbResourceDto.Locale.values())

                    .forEach((currentLocale) -> deleteResourcesForLocale(changeObject, currentLocale));

        }
    }

    private void deleteResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale) {
        DbDto.Topic topic = changeObject.getTopic();

        databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(changeObject.getRef(), topic, locale)

                .ifPresent((resourceEntry) -> {
                    DbResourceDto dbResourceDto = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
                    dbResourceDto.getEntries().remove(resourceEntry);
                });
    }

    private void addOrUpdateResources(DbPatchDto.DbChangeDto changeObject) {
        Optional<DbResourceDto.Locale> locale = ofNullable(changeObject.getLocale());

        if (locale.isPresent()) {

            addOrUpdateResourcesForLocale(changeObject, locale.get());

        } else {

            Stream.of(DbResourceDto.Locale.values())

                    .forEach((currentLocale) -> addOrUpdateResourcesForLocale(changeObject, currentLocale));

        }
    }

    private void addOrUpdateResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale) {
        String ref = changeObject.getRef();
        DbDto.Topic topic = changeObject.getTopic();
        String value = changeObject.getValue();

        Optional<DbResourceDto.Entry> potentialResourceEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);

        if (potentialResourceEntry.isPresent()) {

            potentialResourceEntry.get().setValue(value);

        } else {

            databaseMiner.getResourceFromTopicAndLocale(topic, locale)
                    .ifPresent((localeResources) -> localeResources.getEntries().add(DbResourceDto.Entry.builder()
                                                                                        .forReference(ref)
                                                                                        .withValue(value)
                                                                                        .build()));
        }
    }

    private static List<DbDataDto.Item> createEntryItemsWithValues(DbStructureDto structureObject, List<String> allValues) {
        List<DbStructureDto.Field> structureFields = structureObject.getFields();

        int structureFieldsSize = structureFields.size();
        int patchValuesCount = allValues.size();
        if (patchValuesCount != structureFieldsSize) {
            throw new IllegalArgumentException("Values count in current patch does not match topic structure: " + patchValuesCount + " VS " + structureFieldsSize);
        }

        AtomicInteger fieldIndex = new AtomicInteger();
        return allValues.stream()

                .map((value) -> {
                    DbStructureDto.Field structureField = structureFields.get(fieldIndex.getAndIncrement());
                    return DbDataDto.Item.builder()
                            .fromStructureFieldAndTopic(structureField, structureObject.getTopic())
                            .withRawValue(value)
                            .build();
                })

                .collect(toList());
    }

    private static List<DbDataDto.Item> createEntryItemsWithPartialValues(DbStructureDto structureObject, DbDataDto.Entry existingEntry, List<DbFieldValueDto> partialValues) {
        return existingEntry.getItems().stream()

                .map( (item) -> {

                    Optional<DbFieldValueDto> partialValue = partialValues.stream()

                            .filter((value) -> value.getRank() == item.getFieldRank())

                            .findAny();

                    if (partialValue.isPresent()) {

                        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, structureObject.getFields());
                        return DbDataDto.Item.builder()
                                .fromStructureFieldAndTopic(structureField, structureObject.getTopic())
                                .withRawValue(partialValue.get().getValue())
                                .build();

                    }

                    return item;
                })

                .collect(toList());
    }
}
