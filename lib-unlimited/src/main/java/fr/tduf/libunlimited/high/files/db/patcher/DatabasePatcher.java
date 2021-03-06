package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.DatabasePlaceholderResolver;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.DirectionEnum.UP;
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
     * Execute provided patch onto current database
     *
     * @return effective properties.
     */
    public DatabasePatchProperties apply(DbPatchDto patchObject) {
        return applyWithProperties(patchObject, new DatabasePatchProperties());
    }

    /**
     * Execute provided patch onto current database, taking properties into account.
     *
     * @return effective properties.
     */
    public DatabasePatchProperties applyWithProperties(DbPatchDto patchObject, DatabasePatchProperties patchProperties) {
        requireNonNull(patchObject, "A patch object is required.");
        requireNonNull(patchProperties, "Patch properties are required.");

        DatabasePatchProperties effectiveProperties = patchProperties.makeCopy();

        DatabasePlaceholderResolver
                .load(patchObject, effectiveProperties, databaseMiner)
                .resolveAllPlaceholders();

        patchObject.getChanges()
                .forEach(this::applyChange);

        return effectiveProperties;
    }

    /**
     * Execute provided patches onto current database, taking properties into account.
     */
    public void batchApplyWithProperties(Map<DbPatchDto, DatabasePatchProperties> patchObjectsAndProperties) {
        requireNonNull(patchObjectsAndProperties, "A list of patch objects and associated properties are required.")
                .forEach(this::applyWithProperties);
    }

    @Override
    protected void postPrepare() {
        databaseChangeHelper = new DatabaseChangeHelper(databaseMiner);
    }

    /**
     * Execute provided patches onto current database
     */
    void batchApply(List<DbPatchDto> patchObjects) {
        requireNonNull(patchObjects, "A list of patch objects is required.")
                .forEach(this::apply);
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
            case MOVE:
                moveContents(changeObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled change type: " + changeType);
        }
    }

    private void moveContents(DbPatchDto.DbChangeDto changeObject) {

        final DbDto.Topic changedTopic = changeObject.getTopic();
        final List<DbFieldValueDto> filterCompounds = changeObject.getFilterCompounds();

        final Optional<Integer> potentialIdentifier = databaseMiner.getContentEntryStreamMatchingCriteria(filterCompounds, changedTopic)

                .findFirst()

                .map(ContentEntryDto::getId);

        if (potentialIdentifier.isPresent()) {

            final DbPatchDto.DbChangeDto.DirectionEnum moveDirection = requireNonNull(changeObject.getDirection(), "Direction is required for MOVE patch");
            final int steps = ofNullable(changeObject.getSteps())
                    .orElse(1);
            final int actualSteps =
                    UP == moveDirection ?
                    steps * -1 :
                    steps;

            databaseChangeHelper.moveEntryWithIdentifier(actualSteps, potentialIdentifier.get(), changedTopic);

        } else {

            Log.warn("No entry to be moved, using filter: " + filterCompounds);

        }
    }

    private void deleteContents(DbPatchDto.DbChangeDto changeObject) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        Optional<String> potentialRef = Optional.ofNullable(changeObject.getRef());

        if (potentialRef.isPresent()) {
            String effectiveRef = potentialRef.get();
            databaseChangeHelper.removeEntryWithReference(effectiveRef, changedTopic);
        } else {
            requireNonNull(changeObject.getFilterCompounds(), "As no REF is provided, filter attribute is mandatory.");
            databaseChangeHelper.removeEntriesMatchingCriteria(changeObject.getFilterCompounds(), changedTopic);
        }
    }

    private void addOrUpdateContents(DbPatchDto.DbChangeDto changeObject) {
        DbDto.Topic changedTopic = changeObject.getTopic();
        databaseMiner.getDatabaseTopic(changedTopic)
                .ifPresent(topicObject -> addOrUpdateContentsForTopic(changeObject, topicObject));
    }

    private void addOrUpdateContentsForTopic(DbPatchDto.DbChangeDto changeObject, DbDto topicObject) {
        DbDto.Topic changedTopic = changeObject.getTopic();
        Optional<ContentEntryDto> potentialEntry = retrieveExistingEntry(changeObject, changedTopic);
        if (changeObject.isPartialChange()) {

            List<DbFieldValueDto> partialValues = changeObject.getPartialValues();
            if (potentialEntry.isPresent()) {
                updateEntryWithPartialChanges(potentialEntry.get(), topicObject.getStructure(), partialValues);
            } else {
                updateEntriesMatchingCriteriaWithPartialChanges(changeObject, topicObject);
            }

        } else {

            if (potentialEntry.isPresent()) {
                updateEntryWithFullChanges(potentialEntry.get(), topicObject, changeObject);
            } else {
                addEntryWithFullChanges(topicObject, changeObject.getValues());
            }

        }
    }

    private Optional<ContentEntryDto> retrieveExistingEntry(DbPatchDto.DbChangeDto changeObject, DbDto.Topic changedTopic) {
        final Optional<String> potentialReference = ofNullable(changeObject.getRef());
        if (potentialReference.isPresent()) {
            String effectiveReference = potentialReference.get();
            return databaseMiner.getContentEntryFromTopicWithReference(effectiveReference, changedTopic);
        }

        if (changeObject.isPartialChange() || changeObject.getValues() == null) {
            return empty();
        }

        return databaseMiner.getContentEntryFromTopicWithItemValues(changeObject.getValues(), changedTopic);
    }

    private void addEntryWithFullChanges(DbDto topicObject, List<String> values) {
        List<ContentItemDto> modifiedItems = createEntryItemsWithValues(topicObject, values);
        topicObject.getData().addEntryWithItems(modifiedItems);
    }

    private void updateEntryWithFullChanges(ContentEntryDto existingEntry, DbDto topicObject, DbPatchDto.DbChangeDto changeObject) {
        if (changeObject.isStrictMode()) {
            return;
        }

        List<ContentItemDto> modifiedItems = createEntryItemsWithValues(topicObject, changeObject.getValues());
        existingEntry.replaceItems(modifiedItems);
    }

    private void updateEntryWithPartialChanges(ContentEntryDto existingEntry, DbStructureDto structureObject, List<DbFieldValueDto> partialValues) {
        List<ContentItemDto> modifiedItems = createEntryItemsWithPartialValues(structureObject, existingEntry, partialValues);
        existingEntry.replaceItems(modifiedItems);
    }

    private void updateEntriesMatchingCriteriaWithPartialChanges(DbPatchDto.DbChangeDto changeObject, DbDto topicObject) {
        List<DbFieldValueDto> partialValues = changeObject.getPartialValues();
        List<DbFieldValueDto> filterCompounds = changeObject.getFilterCompounds();
        if (filterCompounds == null) {
            Log.warn("No entry to be updated with partial values: " + partialValues + ", using no filter.");
            return;
        }

        List<ContentEntryDto> entries = databaseMiner.getContentEntriesMatchingCriteria(filterCompounds, changeObject.getTopic());
        if (entries.isEmpty()) {
            Log.warn("No entry to be updated with partial values: " + partialValues + ", using filter: " + filterCompounds);
            return;
        }

        entries.forEach(entry -> updateEntryWithPartialChanges(entry, topicObject.getStructure(), partialValues));
    }

    private void deleteResources(DbPatchDto.DbChangeDto changeObject) {
        String ref = changeObject.getRef();
        final DbDto.Topic topic = changeObject.getTopic();
        databaseMiner.getResourceEntryFromTopicAndReference(topic, ref)
                .ifPresent(entry -> {
                    Locale selectedLocale = changeObject.getLocale();
                    if (selectedLocale == null
                            || DEFAULT == selectedLocale) {
                        databaseMiner.getResourcesFromTopic(topic)
                                .orElseThrow(() -> new IllegalStateException("No resource object for topic: " + topic))
                                .removeEntryByReference(ref);
                    } else {
                        entry.removeValueForLocale(selectedLocale);
                    }
                });
    }

    private void addOrUpdateResources(DbPatchDto.DbChangeDto changeObject) {
        String ref = changeObject.getRef();
        DbDto.Topic topic = changeObject.getTopic();

        final Optional<ResourceEntryDto> potentialResourceEntry = databaseMiner.getResourceEntryFromTopicAndReference(topic, ref);
        if (potentialResourceEntry.isPresent() && changeObject.isStrictMode()) {
            return;
        }

        DbResourceDto resources = databaseMiner.getResourcesFromTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No resource object for topic: " + topic));

        ResourceEntryDto resourceEntry = potentialResourceEntry
                .orElseGet(() -> resources.addEntryByReference(ref));

        String value = changeObject.getValue();
        Locale selectedLocale = changeObject.getLocale();
        if (selectedLocale == null
                || DEFAULT == selectedLocale) {
            resources.removeEntryByReference(ref);
            resources.addDefaultEntryByReference(ref, value);
        } else {
            resourceEntry.setValueForLocale(value, selectedLocale);
        }
    }

    private static List<ContentItemDto> createEntryItemsWithValues(DbDto topicObject, List<String> allValues) {
        List<DbStructureDto.Field> structureFields = topicObject.getStructure().getFields();

        int structureFieldsSize = structureFields.size();
        int patchValuesCount = allValues.size();
        if (patchValuesCount != structureFieldsSize) {
            throw new IllegalArgumentException("Values count in current patch does not match topic structure: " + patchValuesCount + " VS " + structureFieldsSize);
        }

        AtomicInteger fieldIndex = new AtomicInteger();
        return allValues.stream()

                .map(value -> {
                    DbStructureDto.Field structureField = structureFields.get(fieldIndex.getAndIncrement());
                    return ContentItemDto.builder()
                            .fromStructureFieldAndTopic(structureField, topicObject.getTopic())
                            .withRawValue(value)
                            .build();
                })

                .collect(toList());
    }

    private static List<ContentItemDto> createEntryItemsWithPartialValues(DbStructureDto structureObject, ContentEntryDto existingEntry, List<DbFieldValueDto> partialValues) {
        return existingEntry.getItems().stream()

                .map(item -> {

                    Optional<DbFieldValueDto> partialValue = partialValues.stream()
                            .filter(value -> value.getRank() == item.getFieldRank())
                            .findAny();

                    if (partialValue.isPresent()) {

                        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, structureObject.getFields());
                        final String effectiveValue = partialValue.get().getValue();
                        return ContentItemDto.builder()
                                .fromStructureFieldAndTopic(structureField, structureObject.getTopic())
                                .withRawValue(effectiveValue)
                                .build();
                    }

                    return item;
                })

                .collect(toList());
    }
}
