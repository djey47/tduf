package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseStructureHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.common.game.domain.Locale.DEFAULT;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Used to generate patches from an existing database.
 */
public class PatchGenerator extends AbstractDatabaseHolder {

    private static final Set<DbDto.Topic> CAR_PHYSICS_ASSOCIATION_TOPICS = new HashSet<>(asList(CAR_RIMS, CAR_PACKS, CAR_COLORS));

    private DbDto topicObject;

    private DatabaseStructureHelper databaseStructureHelper = new DatabaseStructureHelper();

    /**
     * Generates a patch based on current database objects.
     *
     * @param topic             : database topic for which the patch should apply
     * @param entryRange        : range of reference values (REF support) or identifiers for concerned entries
     * @param fieldRange        : range of field ranks for partial generation
     * @return a patch object with all necessary instructions.
     */
    public DbPatchDto makePatch(DbDto.Topic topic, ItemRange entryRange, ItemRange fieldRange) {
        requireNonNull(topic, "A database topic is required.");
        requireNonNull(entryRange, "A reference range is required.");

        return DbPatchDto.builder()
                .addChanges(databaseStructureHelper.isRefSupportForTopic(topic) ?
                        makeChangesObjectsForTopicWithRef(topic, entryRange, fieldRange) :
                        makeChangesObjectsForTopicWithId(topic, entryRange, fieldRange))
                .build();
    }

    @Override
    protected void postPrepare() {
        // No processing necessary for now
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForTopicWithRef(DbDto.Topic topic, ItemRange refRange, ItemRange fieldRange) {
        defineCurrentTopicObject(topic);

        RequiredReferences requiredReferences = new RequiredReferences();
        Set<DbPatchDto.DbChangeDto> changesObjects = new LinkedHashSet<>();
        List<DbStructureDto.Field> structureFields = topicObject.getStructure().getFields();

        changesObjects.addAll(makeChangesObjectsForContentsWithRef(structureFields, refRange, fieldRange, requiredReferences));

        return addChangeObjectsForRequiredItems(topic, requiredReferences, changesObjects);
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForTopicWithId(DbDto.Topic topic, ItemRange idRange, ItemRange fieldRange) {
        defineCurrentTopicObject(topic);

        RequiredReferences requiredReferences = new RequiredReferences();
        Set<DbPatchDto.DbChangeDto> changesObjects = new LinkedHashSet<>();
        List<DbStructureDto.Field> structureFields = topicObject.getStructure().getFields();

        changesObjects.addAll(makeChangesObjectsForContentsWithId(structureFields, idRange, fieldRange, requiredReferences));

        return addChangeObjectsForRequiredItems(topic, requiredReferences, changesObjects);
    }

    private void defineCurrentTopicObject(DbDto.Topic topic) {
        topicObject = databaseMiner.getDatabaseTopic(topic)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found in provided database: " + topic));
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForContentsWithRef(List<DbStructureDto.Field> structureFields, ItemRange range, ItemRange fieldRange, RequiredReferences requiredReferences) {
        int refFieldRank = DatabaseStructureQueryHelper.getUidFieldRank(structureFields)
                .orElseThrow(() -> new IllegalStateException("Topic should contain REF field: " + structureFields));

        return topicObject.getData().getEntries().stream()
                .filter(entry -> isInRefRange(entry, refFieldRank, range))
                .map(acceptedEntry -> makeChangeObjectForEntry(topicObject.getTopic(), acceptedEntry, structureFields, fieldRange, requiredReferences))
                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForContentsWithId(List<DbStructureDto.Field> structureFields, ItemRange range, ItemRange fieldRange, RequiredReferences requiredReferences) {
        return topicObject.getData().getEntries().stream()
                .filter(entry -> isInIdRange(entry, range))
                .map(acceptedEntry -> makeChangeObjectForEntry(topicObject.getTopic(), acceptedEntry, structureFields, fieldRange, requiredReferences))
                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> addChangeObjectsForRequiredItems(DbDto.Topic topic, RequiredReferences requiredReferences, Set<DbPatchDto.DbChangeDto> changesObjects) {
        changesObjects.addAll(makeChangesObjectsForRequiredResources(requiredReferences));
        changesObjects.addAll(makeChangesObjectsForRequiredContents(topic, requiredReferences));
        return changesObjects;
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredContents(DbDto.Topic topic, RequiredReferences requiredReferences) {
        // To prevent recursivity issues
        DbDto.Topic ignoredTopic = CAR_PHYSICS_ASSOCIATION_TOPICS.contains(topic) ?
                CAR_PHYSICS_DATA : null;

        return requiredReferences.requiredContentsIds.entrySet().stream()
                .filter(entry -> entry.getKey() != ignoredTopic)
                .flatMap(topicEntry -> {
                    Set<String> ids = topicEntry.getValue().stream()
                            .map(entryId -> Integer.toString(entryId))
                            .collect(toSet());

                    return makeChangesObjectsForTopicWithId(topicEntry.getKey(), ItemRange.fromCollection(ids), ItemRange.ALL).stream();
                })
                .collect(toSet());
    }

    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForRequiredResources(RequiredReferences requiredReferences) {
        return requiredReferences.requiredResourceReferences.entrySet().stream()
                .flatMap(resourceEntry -> makeChangesObjectsForResourcesInTopic(resourceEntry.getKey(), resourceEntry.getValue()))
                .collect(toSet());
    }

    private Stream<DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesInTopic(DbDto.Topic topic, Set<String> resources) {
        DbResourceDto resourceFromTopic = databaseMiner.getResourcesFromTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No resource object found for topic: " + topic));

        Set<String> globalizedResourceRefs = new HashSet<>();
        Set<String> localizedResourceRefs = new HashSet<>();
        identifyGlobalizedAndLocalizedResourceReferences(resources, resourceFromTopic, globalizedResourceRefs, localizedResourceRefs);

        Stream<DbPatchDto.DbChangeDto> changesObjectsForGlobalizedResources = makeChangesObjectsForResourcesWithLocale(topic, null, globalizedResourceRefs);
        Stream<DbPatchDto.DbChangeDto> changesObjectsForLocalizedResources = Locale.valuesAsStream()
                .flatMap(locale -> makeChangesObjectsForResourcesWithLocale(topic, locale, localizedResourceRefs));

        return Stream.concat(changesObjectsForGlobalizedResources, changesObjectsForLocalizedResources);
    }

    private Stream<DbPatchDto.DbChangeDto> makeChangesObjectsForResourcesWithLocale(DbDto.Topic topic, Locale potentialLocale, Set<String> topicResources) {
        return topicResources.stream()
                .map(resourceRef -> makeChangeObjectForResource(topic, potentialLocale, resourceRef));
    }

    private DbPatchDto.DbChangeDto makeChangeObjectForResource(DbDto.Topic topic, Locale locale, String resourceRef) {
        Optional<Locale> potentialLocale = ofNullable(locale);
        String resourceValue = databaseMiner.getLocalizedResourceValueFromTopicAndReference(resourceRef, topic, potentialLocale.orElse(Locale.FRANCE))
                .orElse(DatabaseGenHelper.RESOURCE_VALUE_DEFAULT);

        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .forLocale(potentialLocale.orElse(DEFAULT))
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES)
                .asReference(resourceRef)
                .withValue(resourceValue)
                .build();
    }

    private DbPatchDto.DbChangeDto makeChangeObjectForEntry(DbDto.Topic topic, ContentEntryDto entry, List<DbStructureDto.Field> structureFields, ItemRange fieldRange, RequiredReferences requiredReferences) {
        String entryReference = entry.getEffectiveRef();
        List<ContentItemDto> items = entry.getItems();
        if (fieldRange.isGlobal()) {
            return makeGlobalChangeObject(entryReference, topic, items, structureFields, requiredReferences);
        } else {
            return makePartialChangeObject(entryReference, topic, items, structureFields, fieldRange, requiredReferences);
        }
    }

    private DbPatchDto.DbChangeDto makeGlobalChangeObject(String entryReference, DbDto.Topic topic, List<ContentItemDto> entryItems, List<DbStructureDto.Field> structureFields, RequiredReferences requiredReferences) {
        if (CAR_PHYSICS_DATA == topic) {
            addCarPhysicsAssociatedEntriesToRequiredContents(entryReference, requiredReferences);
        }
        if (CAR_COLORS == topic) {
            addRelatedInteriorEntriesToRequiredContents(entryItems, structureFields.get(7), requiredReferences);
        }

        List<String> entryValues = entryItems.stream()
                .map(entryItem -> {
                    DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(entryItem, structureFields);
                    return fetchItemValue(topic, structureField, entryItem, requiredReferences);
                })
                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(topic)
                .asReference(entryReference)
                .withEntryValues(entryValues)
                .build();
    }

    private DbPatchDto.DbChangeDto makePartialChangeObject(String entryReference, DbDto.Topic topic, List<ContentItemDto> entryItems, List<DbStructureDto.Field> structureFields, ItemRange fieldRange, RequiredReferences requiredReferences) {
        requireNonNull(entryReference, "Entry reference is required for partial change object.");

        List<DbFieldValueDto> partialValues = entryItems.stream()
                .filter(entryItem -> fieldRange.accepts(Integer.toString(entryItem.getFieldRank())))
                .map(acceptedItem -> {
                    DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(acceptedItem, structureFields);
                    String itemValue = fetchItemValue(topic, structureField, acceptedItem, requiredReferences);

                    return DbFieldValueDto.fromCouple(acceptedItem.getFieldRank(), itemValue);
                })
                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(topic)
                .asReference(entryReference)
                .withPartialEntryValues(partialValues)
                .build();
    }

    private void addCarPhysicsAssociatedEntriesToRequiredContents(String entryReference, RequiredReferences requiredReferences) {
        CAR_PHYSICS_ASSOCIATION_TOPICS
                .forEach(topic -> databaseMiner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(1, entryReference), topic)
                        .forEach(associationEntry -> requiredReferences.updateRequiredContentsIds(topic, associationEntry.getId()))
        );
    }

    private void addRelatedInteriorEntriesToRequiredContents(List<ContentItemDto> carColorsItems, DbStructureDto.Field structureField, RequiredReferences requiredReferences) {
        IntStream.rangeClosed(1, 15)
                .forEach(interiorIndex -> {
                    String interiorReference = carColorsItems.get(6 + interiorIndex).getRawValue();
                    updateRequiredRemoteReferences(interiorReference, structureField, requiredReferences);
                });
    }

    private String fetchItemValue(DbDto.Topic topic, DbStructureDto.Field structureField, ContentItemDto entryItem, RequiredReferences requiredReferences) {
        DbStructureDto.FieldType fieldType = structureField.getFieldType();
        if (RESOURCE_CURRENT_GLOBALIZED == fieldType
                || RESOURCE_CURRENT_LOCALIZED == fieldType) {

            requiredReferences.updateRequiredResourceReferences(topic, entryItem.getRawValue());
        } else if (RESOURCE_REMOTE == fieldType
                || REFERENCE == fieldType) {

            updateRequiredRemoteReferences(entryItem.getRawValue(), structureField, requiredReferences);
        }

        return entryItem.getRawValue();
    }

    private void updateRequiredRemoteReferences(String reference, DbStructureDto.Field structureField, RequiredReferences requiredReferences) {
        DbDto remoteTopicObject = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef());
        DbDto.Topic remoteTopic = remoteTopicObject.getTopic();

        DbStructureDto.FieldType fieldType = structureField.getFieldType();

        if (RESOURCE_REMOTE == fieldType) {
            requiredReferences.updateRequiredResourceReferences(remoteTopic, reference);
        } else {
            int entryId = databaseMiner.getContentEntryInternalIdentifierWithReference(reference, remoteTopic)
                    .orElseThrow(() -> new IllegalStateException("No entry id at ref: " + reference));
            requiredReferences.updateRequiredContentsIds(remoteTopic, entryId);
        }
    }

    private static boolean isInRefRange(ContentEntryDto entry, int refFieldRank, ItemRange refRange) {
        String entryRef = BulkDatabaseMiner.getContentEntryReference(entry, refFieldRank);
        return refRange.accepts(entryRef);
    }

    private static boolean isInIdRange(ContentEntryDto entry, ItemRange idRange) {
        String entryId = Integer.toString(entry.getId());
        return idRange.accepts(entryId);
    }

    private static void identifyGlobalizedAndLocalizedResourceReferences(Set<String> resourceReferences, DbResourceDto resourceObject, Set<String> globalizedResourceRefs, Set<String> localizedResourceRefs) {
        resourceReferences
                .forEach(resourceReference -> {
                    Set<String> resourceValuesForCurrentRef = BulkDatabaseMiner.getAllResourceValuesForReference(resourceReference, resourceObject);
                    if (resourceValuesForCurrentRef.size() == 1) {
                        globalizedResourceRefs.add(resourceReference);
                    } else {
                        localizedResourceRefs.add(resourceReference);
                    }
        });
    }

    DbDto getTopicObject() {
        return topicObject;
    }

    private static class RequiredReferences {
        private Map<DbDto.Topic, Set<Integer>> requiredContentsIds = new EnumMap<>(DbDto.Topic.class);
        private Map<DbDto.Topic, Set<String>> requiredResourceReferences = new EnumMap<>(DbDto.Topic.class);

        private void updateRequiredContentsIds(DbDto.Topic topic, int id) {
            requiredContentsIds.putIfAbsent(topic, new HashSet<>());
            requiredContentsIds.get(topic).add(id);
        }

        private void updateRequiredResourceReferences(DbDto.Topic topic, String reference) {
            requiredResourceReferences.putIfAbsent(topic, new HashSet<>());
            requiredResourceReferences.get(topic).add(reference);
        }
    }
}
