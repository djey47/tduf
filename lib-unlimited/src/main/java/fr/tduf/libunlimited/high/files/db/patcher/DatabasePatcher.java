package fr.tduf.libunlimited.high.files.db.patcher;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.DirectionEnum.UP;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patches to an existing database.
 */
// TODO resolve placeholders the soonest possible (parse all objects value(s), partialValues etc)
public class DatabasePatcher extends AbstractDatabaseHolder {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{(.+)\\}");

    private DatabaseChangeHelper databaseChangeHelper;

    /**
     * Execute provided patch onto current database
     * @return effective properties.
     */
    public PatchProperties apply(DbPatchDto patchObject) {
        return applyWithProperties(patchObject, new PatchProperties());
    }

    /**
     * Execute provided patch onto current database, taking properties into account.
     * @return effective properties.
     */
    public PatchProperties applyWithProperties(DbPatchDto patchObject, PatchProperties patchProperties) {
        requireNonNull(patchObject, "A patch object is required.");
        requireNonNull(patchProperties, "Patch properties are required.");

        PatchProperties effectiveProperties = patchProperties.makeCopy();
        patchObject.getChanges()

                .forEach((changeObject) -> applyChange(changeObject, effectiveProperties));

        return effectiveProperties;
    }

    @Override
    protected void postPrepare() {
        databaseChangeHelper = new DatabaseChangeHelper(databaseMiner);
    }

    // TODO introduce notion of simple placeholder vs content Ref vs resource Ref
    static String resolvePlaceholder(String value, PatchProperties patchProperties, Optional<DbDto> topicObject) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if(matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseGet(() -> {
                        if (topicObject.isPresent()) {
                            // TODO take newly generated values into account for unicity
                            final String generatedValue = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject.get());
                            patchProperties.register(placeholderName, generatedValue);
                            return generatedValue;
                        }

                        return value;
                    });
        }

        return value;
    }

    static String resolveResourcePlaceholder(String value, PatchProperties patchProperties, Optional<DbDto> topicObject) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if(matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseGet(() -> {
                        if (topicObject.isPresent()) {
                            // TODO take newly generated values into account for unicity
                            final String generatedValue = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject.get());
                            patchProperties.register(placeholderName, generatedValue);
                            return generatedValue;
                        }

                        return value;
                    });
        }

        return value;
    }

    private void applyChange(DbPatchDto.DbChangeDto changeObject, PatchProperties patchProperties) {

        DbPatchDto.DbChangeDto.ChangeTypeEnum changeType = changeObject.getType();

        switch (changeType) {
            case UPDATE_RES:
                addOrUpdateResources(changeObject, patchProperties);
                break;
            case DELETE_RES:
                deleteResources(changeObject, patchProperties);
                break;
            case UPDATE:
                addOrUpdateContents(changeObject, patchProperties);
                break;
            case DELETE:
                deleteContents(changeObject, patchProperties);
                break;
            case MOVE:
                moveContents(changeObject);
                break;
        }

        BulkDatabaseMiner.clearAllCaches();
    }

    private void moveContents(DbPatchDto.DbChangeDto changeObject) {

        final DbDto.Topic changedTopic = changeObject.getTopic();
        final List<DbFieldValueDto> filterCompounds = changeObject.getFilterCompounds();

        final Optional<Long> potentialIdentifier = databaseMiner.getContentEntryStreamMatchingCriteria(filterCompounds, changedTopic)

                .findFirst()

                .map(DbDataDto.Entry::getId);

        if (potentialIdentifier.isPresent()) {

            final DbPatchDto.DbChangeDto.DirectionEnum moveDirection = requireNonNull(changeObject.getDirection(), "Direction is required for MOVE patch");
            final int steps = ofNullable(changeObject.getSteps())
                    .orElse(1);
            final int actualSteps = (UP == moveDirection ?
                    steps * -1 :
                    steps);

            databaseChangeHelper.moveEntryWithIdentifier(actualSteps, potentialIdentifier.get(), changedTopic);

        } else {

            Log.warn("No entry to be moved, using filter: " + filterCompounds);

        }
    }

    private void deleteContents(DbPatchDto.DbChangeDto changeObject, PatchProperties patchProperties) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        Optional<String> potentialRef = Optional.ofNullable(changeObject.getRef());

        if (potentialRef.isPresent()) {
            String effectiveRef = resolvePlaceholder(potentialRef.get(), patchProperties, empty());
            databaseChangeHelper.removeEntryWithReference(effectiveRef, changedTopic);
        } else {
            requireNonNull(changeObject.getFilterCompounds(), "As no REF is provided, filter attribute is mandatory.");
            databaseChangeHelper.removeEntriesMatchingCriteria(changeObject.getFilterCompounds(), changedTopic);
        }
    }

    private void addOrUpdateContents(DbPatchDto.DbChangeDto changeObject, PatchProperties patchProperties) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        databaseMiner.getDatabaseTopic(changedTopic)
                .ifPresent((topicObject) -> {

                    Optional<DbDataDto.Entry> potentialEntry = retrieveExistingEntry(changeObject, changedTopic, patchProperties);

                    if (changeObject.isPartialChange()) {

                        List<DbFieldValueDto> partialValues = changeObject.getPartialValues();
                        if (potentialEntry.isPresent()) {
                            updateEntryWithPartialChanges(potentialEntry.get(), topicObject.getStructure(), partialValues, patchProperties);
                        } else {
                            updateEntriesMatchingCriteriaWithPartialChanges(changeObject, changedTopic, topicObject, partialValues, patchProperties);
                        }

                    } else {

                        addOrUpdateEntryWithFullChanges(potentialEntry, topicObject, changeObject.getValues(), patchProperties);

                    }
                });
    }

    private Optional<DbDataDto.Entry> retrieveExistingEntry(DbPatchDto.DbChangeDto changeObject, DbDto.Topic changedTopic, PatchProperties patchProperties) {
        final Optional<String> potentialReference = ofNullable(changeObject.getRef());
        if (potentialReference.isPresent()) {
            String effectiveReference = resolvePlaceholder(potentialReference.get(), patchProperties, empty());
            return databaseMiner.getContentEntryFromTopicWithReference(effectiveReference, changedTopic);
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

    private void addOrUpdateEntryWithFullChanges(Optional<DbDataDto.Entry> existingEntry, DbDto topicObject, List<String> allValues, PatchProperties patchProperties) {
        List<DbDataDto.Item> modifiedItems = createEntryItemsWithValues(topicObject, allValues, patchProperties);

        if (existingEntry.isPresent()) {

            existingEntry.get().replaceItems(modifiedItems);

        } else {

            topicObject.getData().addEntryWithItems(modifiedItems);

        }
    }

    private void updateEntryWithPartialChanges(DbDataDto.Entry existingEntry, DbStructureDto structureObject, List<DbFieldValueDto> partialValues, PatchProperties patchProperties) {
        List<DbDataDto.Item> modifiedItems = createEntryItemsWithPartialValues(structureObject, existingEntry, partialValues, patchProperties);
        existingEntry.replaceItems(modifiedItems);
    }

    private void updateEntriesMatchingCriteriaWithPartialChanges(DbPatchDto.DbChangeDto changeObject, DbDto.Topic changedTopic, DbDto topicObject, List<DbFieldValueDto> partialValues, PatchProperties patchProperties) {
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

        entries.forEach((entry) -> updateEntryWithPartialChanges(entry, topicObject.getStructure(), partialValues, patchProperties));
    }

    private void deleteResources(DbPatchDto.DbChangeDto changeObject, PatchProperties patchProperties) {
        Optional<DbResourceDto.Locale> potentialLocale = ofNullable(changeObject.getLocale());

        if (potentialLocale.isPresent()) {

            deleteResourcesForLocale(changeObject, potentialLocale.get(), patchProperties);

        } else {

            Stream.of(DbResourceDto.Locale.values())

                    .forEach((currentLocale) -> deleteResourcesForLocale(changeObject, currentLocale, patchProperties));

        }
    }

    private void deleteResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale, PatchProperties patchProperties) {
        final DbDto.Topic topic = changeObject.getTopic();
        final String effectiveRef = resolvePlaceholder(changeObject.getRef(), patchProperties, empty());

        databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(effectiveRef, topic, locale)

                .ifPresent((resourceEntry) -> {
                    DbResourceDto dbResourceDto = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
                    dbResourceDto.getEntries().remove(resourceEntry);
                });
    }

    private void addOrUpdateResources(DbPatchDto.DbChangeDto changeObject, PatchProperties patchProperties) {
        Optional<DbResourceDto.Locale> locale = ofNullable(changeObject.getLocale());

        if (locale.isPresent()) {

            addOrUpdateResourcesForLocale(changeObject, locale.get(), patchProperties);

        } else {

            Stream.of(DbResourceDto.Locale.values())

                    .forEach((currentLocale) -> addOrUpdateResourcesForLocale(changeObject, currentLocale, patchProperties));

        }
    }

    private void addOrUpdateResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale, PatchProperties patchProperties) {
        String ref = changeObject.getRef();
        DbDto.Topic topic = changeObject.getTopic();
        String value = changeObject.getValue();

        Optional<DbDto> topicObject = databaseMiner.getDatabaseTopic(topic);
        String effectiveRef = resolveResourcePlaceholder(ref, patchProperties, topicObject);
        Optional<DbResourceDto.Entry> potentialResourceEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(effectiveRef, topic, locale);

        String effectiveValue = resolveResourcePlaceholder(value, patchProperties, empty());
        if (potentialResourceEntry.isPresent()) {

            potentialResourceEntry.get().setValue(effectiveValue);

        } else {

            databaseMiner.getResourceFromTopicAndLocale(topic, locale)
                    .ifPresent((localeResources) -> localeResources.getEntries().add(DbResourceDto.Entry.builder()
                                                                                        .forReference(effectiveRef)
                                                                                        .withValue(effectiveValue)
                                                                                        .build()));
        }
    }

    private static List<DbDataDto.Item> createEntryItemsWithValues(DbDto topicObject, List<String> allValues, PatchProperties patchProperties) {
        List<DbStructureDto.Field> structureFields = topicObject.getStructure().getFields();

        int structureFieldsSize = structureFields.size();
        int patchValuesCount = allValues.size();
        if (patchValuesCount != structureFieldsSize) {
            throw new IllegalArgumentException("Values count in current patch does not match topic structure: " + patchValuesCount + " VS " + structureFieldsSize);
        }

        AtomicInteger fieldIndex = new AtomicInteger();
        return allValues.stream()

                .map((value) -> {
                    DbStructureDto.Field structureField = structureFields.get(fieldIndex.getAndIncrement());
                    String effectiveValue = resolvePlaceholder(value, patchProperties, Optional.of(topicObject));
                    return DbDataDto.Item.builder()
                            .fromStructureFieldAndTopic(structureField, topicObject.getTopic())
                            .withRawValue(effectiveValue)
                            .build();
                })

                .collect(toList());
    }

    private static List<DbDataDto.Item> createEntryItemsWithPartialValues(DbStructureDto structureObject, DbDataDto.Entry existingEntry, List<DbFieldValueDto> partialValues, PatchProperties patchProperties) {
        return existingEntry.getItems().stream()

                .map( (item) -> {

                    Optional<DbFieldValueDto> partialValue = partialValues.stream()

                            .filter((value) -> value.getRank() == item.getFieldRank())

                            .findAny();

                    if (partialValue.isPresent()) {

                        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, structureObject.getFields());
                        final String effectiveValue = resolvePlaceholder(partialValue.get().getValue(), patchProperties, empty());
                        return DbDataDto.Item.builder()
                                .fromStructureFieldAndTopic(structureField, structureObject.getTopic())
                                .withRawValue(effectiveValue)
                                .build();
                    }

                    return item;
                })

                .collect(toList());
    }
}
