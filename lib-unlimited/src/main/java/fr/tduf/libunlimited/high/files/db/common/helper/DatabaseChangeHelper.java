package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class to make changes on database contents and resources.
 */
public class DatabaseChangeHelper {

    private BulkDatabaseMiner databaseMiner;

    private DatabaseGenHelper genHelper;

    public DatabaseChangeHelper(DatabaseGenHelper genHelper, BulkDatabaseMiner databaseMiner) {
        requireNonNull(databaseMiner, "A database miner instance is required.");
        requireNonNull(genHelper, "A generation helper instance is required.");

        this.databaseMiner = databaseMiner;
        this.genHelper = genHelper;
    }

    public DatabaseChangeHelper(BulkDatabaseMiner databaseMiner) {
        this(new DatabaseGenHelper(databaseMiner), databaseMiner);
    }

    /**
     * Adds a resource with given reference and value to resource entries from topic and locale
     *
     * @param topic             : database topic where resource entry should be added
     * @param locale            : language to be affected
     * @param resourceReference : reference of new resource
     * @param resourceValue     : value of new resurce
     * @throws IllegalArgumentException when a resource entry with same reference already exists for topic and locale.
     */
    public void addResourceValueWithReference(DbDto.Topic topic, Locale locale, String resourceReference, String resourceValue) {
        checkResourceValueDoesNotExistWithReference(topic, locale, resourceReference);

        final DbResourceDto resourceEnhancedFromTopic = databaseMiner.getResourceEnhancedFromTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No resource for topic: " + topic));
        resourceEnhancedFromTopic.getEntryByReference(resourceReference)
                .orElseGet(() -> resourceEnhancedFromTopic.addEntryByReference(resourceReference))
                .setValueForLocale(resourceValue, locale);
    }

    /**
     * @param reference : reference of entry to create
     * @param topic     : database topic where content entry should be added
     * @return created content entry with items having default values
     * @throws java.util.NoSuchElementException when specified topic has no loaded content.
     */
    public ContentEntryDto addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {

        DbDto topicObject = databaseMiner.getDatabaseTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No object for topic: " + topic));

        DbDataDto dataDto = topicObject.getData();

        ContentEntryDto newEntry = ContentEntryDto.builder()
                .forId(dataDto.getEntries().size())
                .addItems(genHelper.buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.addEntry(newEntry);

        return newEntry;
    }

    /**
     * Changes raw value of existing item at specified index and field rank
     * @param topic         : database topic where content item should be changed
     * @param entryIndex    : index of entry in topic
     * @param fieldRank     : rank of item in entry
     * @param newRawValue   : value to apply
     * @return updated item if value has changed, empty otherwise.
     */
    public Optional<ContentItemDto> updateItemRawValueAtIndexAndFieldRank(DbDto.Topic topic, long entryIndex, int fieldRank, String newRawValue) {
        return databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryIndex, topic)
                .flatMap(entry -> entry.updateItemValueAtRank(newRawValue, fieldRank));
    }

    /**
     * Modifies existing resource item having given reference, with new reference and new value.
     *
     * @param topic                : database topic where resource entry should be changed
     * @param locale               : database language for which resource entry should be changed
     * @param oldResourceReference : reference of resource to be updated. Must exist
     * @param newResourceReference : new reference of resource if needed. Must not exist already
     * @param newResourceValue     : new value of resource
     * @throws IllegalArgumentException when source entry does not exist or target reference belongs to an already existing entry.
     */
    public void updateResourceItemWithReference(DbDto.Topic topic, Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        ResourceEntryDto existingEntry = checkResourceEntryExistsWithReference(topic, oldResourceReference);

        if (!oldResourceReference.equals(newResourceReference)) {
            checkResourceEntryDoesNotExistWithReference(topic, newResourceReference);
        }

        existingEntry.removeValueForLocale(locale);
        addResourceValueWithReference(topic, locale, newResourceReference, newResourceValue);
    }

    /**
     * Deletes content entry with given internal identifier in specified topic.
     * Following entries will have their ids updated (decreased by 1).
     *
     * @param entryId : internal identifier of entry to remove
     * @param topic   : database topic where entry should be removed
     * @throws java.util.NoSuchElementException when entry to delete does not exist.
     */
    public void removeEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        DbDataDto topicDataObject = databaseMiner.getDatabaseTopic(topic)
                .map(DbDto::getData)
                .orElseThrow(() -> new IllegalStateException("No data for topic: " + topic));

        ContentEntryDto entryToDelete = topicDataObject.getEntryWithInternalIdentifier(entryId)
                .orElseThrow(() -> new IllegalStateException("No entry for topic: " + topic + " at id: " + entryId));

        topicDataObject.removeEntry(entryToDelete);
    }

    /**
     * Deletes content entry with given reference in specified topic.
     * Following entries will have their ids updated (decreased by 1).
     *
     * @param entryRef : ref value of entry to remove
     * @param topic    : database topic where entry should be removed.
     */
    public void removeEntryWithReference(String entryRef, DbDto.Topic topic) {
        databaseMiner.getContentEntryFromTopicWithReference(entryRef, topic)
                .ifPresent(entry -> removeEntryWithIdentifier(entry.getId(), topic));
    }

    /**
     * Deletes content entries satisfying all conditions.
     * Following entries will have their ids updated (decreased by 1).
     *
     * @param criteria : list of conditions to select content entries
     * @param topic    : database topic where entry should be removed
     */
    public void removeEntriesMatchingCriteria(List<DbFieldValueDto> criteria, DbDto.Topic topic) {
        databaseMiner.getContentEntryStreamMatchingCriteria(criteria, topic)
                .forEach((entry) -> removeEntryWithIdentifier(entry.getId(), topic));
    }

    /**
     * @param entryId : internal identifier of entry to be copied
     * @param topic   : database topic where entry should be duplicated
     * @return a clone of entry with given identifier in specified topic and added to this topic.
     * If a REF field is present, a random, unique identifier will be generated.
     */
    public ContentEntryDto duplicateEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        DbDto topicObject = databaseMiner.getDatabaseTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No data for topic: " + topic));

        DbDataDto topicDataObject = topicObject.getData();
        ContentEntryDto sourceEntry = topicDataObject.getEntryWithInternalIdentifier(entryId)
                .orElseThrow(() -> new IllegalStateException("No source entry found at id: " + entryId));
        long newIdentifier = topicDataObject.getEntries().size();

        List<ContentItemDto> clonedItems = cloneContentItems(sourceEntry, topic);
        ContentEntryDto newEntry = ContentEntryDto.builder()
                .forId(newIdentifier)
                .addItems(clonedItems)
                .build();

        DatabaseStructureQueryHelper.getUidFieldRank(topicObject.getStructure().getFields())
                .ifPresent(uidFieldRank -> {
                    String newReference = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);
                    newEntry.updateItemValueAtRank(newReference, uidFieldRank);
                });


        topicDataObject.addEntry(newEntry);

        return newEntry;
    }

    /**
     * Changes rank of entry with given identifier and updated ids of sourrounding ones
     *
     * @param step    : number of moves to perform
     * @param entryId : internal identifier of entry to be moved
     * @param topic   : database topic where entry should be moved
     */
    public void moveEntryWithIdentifier(int step, long entryId, DbDto.Topic topic) {
        final DbDataDto dataObject = databaseMiner.getDatabaseTopic(topic)
                .map(DbDto::getData)
                .orElseThrow(() -> new IllegalStateException("No data for topic: " + topic));

        int absoluteSteps = Math.abs(step);
        if (step == 0
                || step < 0 && entryId - absoluteSteps < 0
                || step > 0 && entryId + absoluteSteps > dataObject.getEntries().size() - 1) {
            return;
        }

        ContentEntryDto entry = databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryId, topic)
                .orElseThrow(() -> new IllegalStateException("No content entry for topic: " + topic + " at id: " + entryId));
        for (int i = 0; i < absoluteSteps; i++) {
            if (step < 0) {
                dataObject.moveEntryUp(entry);
            } else {
                dataObject.moveEntryDown(entry);
            }
        }
    }

    /**
     * Deletes values from specified topic, having given reference.
     *
     * @param topic             : database topic where entry should be duplicated
     * @param resourceReference : reference of resource to be deleted
     * @param affectedLocales   : list of locales to be affected by deletion
     * @throws java.util.NoSuchElementException when such a resource entry does not exist in any of affected locales.
     */
    public void removeResourceValuesWithReference(DbDto.Topic topic, String resourceReference, List<Locale> affectedLocales) {
        databaseMiner.getResourceEntryFromTopicAndReference(topic, resourceReference)
                .ifPresent(entry -> {
                    affectedLocales.forEach(entry::removeValueForLocale);

                    if (entry.getItemCount() == 0) {
                        databaseMiner.getResourceEnhancedFromTopic(topic)
                                .orElseThrow(() -> new IllegalStateException("No resource for topic: " + topic))
                                .removeEntryByReference(resourceReference);
                    }
                });
    }

    /**
     * Deletes all localized values and parent entry.
     *
     * @param topic             : database topic where resource entry should be removed from
     * @param resourceReference : identifier of resource entry to be deleted
     */
    public void removeResourceWithReference(DbDto.Topic topic, String resourceReference) {
        databaseMiner.getResourceEnhancedFromTopic(topic)
                .ifPresent(resource -> resource.removeEntryByReference(resourceReference));
    }

    /**
     * @param entry                   : database entry to be updated
     * @param sourceEntryRef          : reference of source entry (REF field for source topic)
     * @param potentialTargetEntryRef : reference of target entry (REF field for target topic). Mandatory.
     */
    public static void updateAssociationEntryWithSourceAndTargetReferences(ContentEntryDto entry, String sourceEntryRef, Optional<String> potentialTargetEntryRef) {
        requireNonNull(entry, "A content entry is required.");

        // We assume source reference is first field ... target reference (if any) is second field  ...
        entry.updateItemValueAtRank(sourceEntryRef, 1);
        potentialTargetEntryRef
                .ifPresent(ref -> entry.updateItemValueAtRank(ref, 2));
    }

    private void checkResourceValueDoesNotExistWithReference(DbDto.Topic topic, Locale locale, String resourceReference) {
        databaseMiner.getLocalizedResourceValueFromTopicAndReference(resourceReference, topic, locale)
                .ifPresent((value) -> {
                    throw new IllegalArgumentException("Resource value already exists with reference: " + resourceReference + ", for locale: " + locale);
                });
    }

    private ResourceEntryDto checkResourceEntryExistsWithReference(DbDto.Topic topic, String resourceReference) {
        return databaseMiner.getResourceEntryFromTopicAndReference(topic, resourceReference)
                .orElseThrow(() -> new IllegalArgumentException("Resource does not exist with reference: " + resourceReference));
    }

    private void checkResourceEntryDoesNotExistWithReference(DbDto.Topic topic, String resourceReference) {
        databaseMiner.getResourceEntryFromTopicAndReference(topic, resourceReference)
                .ifPresent((entry) -> {
                    throw new IllegalArgumentException("Resource already exists with reference: " + resourceReference);
                });
    }

    private static List<ContentItemDto> cloneContentItems(ContentEntryDto entry, DbDto.Topic topic) {
        return entry.getItems().stream()
                .map((contentItem) -> ContentItemDto.builder().fromExisting(contentItem, topic).build())
                .collect(toList());
    }
}
