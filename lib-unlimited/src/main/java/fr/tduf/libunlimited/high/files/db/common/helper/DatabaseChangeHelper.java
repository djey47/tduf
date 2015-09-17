package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
        requireNonNull(genHelper, "A generation helper instanceis required.");

        this.databaseMiner = databaseMiner;
        this.genHelper = genHelper;
    }

    /**
     * Adds a resource with given reference and value to resource entries from topic and locale
     * @param topic             : database topic where resource entry should be added
     * @param locale            : language to be affected
     * @param resourceReference : reference of new resource
     * @param resourceValue     : value of new resurce
     * @throws IllegalArgumentException when a resource entry with same reference already exists for topic and locale.
     */
    public void addResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, String resourceValue) {
        checkResourceDoesNotExistWithReference(topic, locale, resourceReference);

        List<DbResourceDto.Entry> resourceEntries = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get().getEntries();

        resourceEntries.add(DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue(resourceValue)
                .build());
    }

    /**
     * @param reference : reference of entry to create
     * @param topic     : database topic where content entry should be added
     * @return created content entry with items having default values
     * @throws java.util.NoSuchElementException when specified topic has no loaded content.
     */
    public DbDataDto.Entry addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {

        DbDto topicObject = databaseMiner.getDatabaseTopic(topic).get();

        DbDataDto dataDto = topicObject.getData();

        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(dataDto.getEntries().size())
                .addItems(genHelper.buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.getEntries().add(newEntry);

        return newEntry;
    }

    /**
     * Modifies existing resource having given reference, with new reference and new value
     * @param topic                 : database topic where resource entry should be changed
     * @param locale                : database language for which resource entry should be changed
     * @param oldResourceReference  : reference of resource to be updated. Must exist
     * @param newResourceReference  : new reference of resource if needed. Must not exist already
     * @param newResourceValue      : new value of resource
     * @throws IllegalArgumentException when source entry does not exist or target reference belongs to an already existing entry.
     */
    public void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        checkResourceExistsWithReference(topic, locale, oldResourceReference);

        if (!oldResourceReference.equals(newResourceReference)) {
            checkResourceDoesNotExistWithReference(topic, locale, newResourceReference);
        }

        DbResourceDto.Entry existingResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(oldResourceReference, topic, locale).get();

        existingResourceEntry.setReference(newResourceReference);
        existingResourceEntry.setValue(newResourceValue);
    }

    /**
     * Deletes content entry with given internal identifier in specified topic.
     * Following entries will have their ids updated (decreased by 1).
     * @param entryId   : internal identifier of entry to remove
     * @param topic     : database topic where entry should be changed
     * @throws java.util.NoSuchElementException when entry to delete does not exist.
     */
    public void removeEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(topic).get().getData().getEntries();
        AtomicBoolean removalResult = new AtomicBoolean(false);
        topicEntries.stream()

                .filter((entry) -> entry.getId() == entryId)

                .findAny()

                .ifPresent((entry) -> {
                    topicEntries.remove(entry);
                    removalResult.set(true);
                });

        // Fix identifiers of next entries
        if (removalResult.get()) {
            topicEntries.stream()

                    .filter((entry) -> entry.getId() > entryId)

                    .forEach(DbDataDto.Entry::shiftIdUp);
        }
    }

    /**
     * @param entryId   : internal identifier of entry to be copied
     * @param topic     : database topic where entry should be duplicated
     * @return a clone of entry with given identifier in specified topic and added to this topic.
     * If a REF field is present, a random, unique identifier will be generated.
     */
    public DbDataDto.Entry duplicateEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        DbDataDto.Entry sourceEntry = databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryId, topic).get();

        DbDto topicObject = databaseMiner.getDatabaseTopic(topic).get();
        List<DbDataDto.Entry> currentContentEntries = topicObject.getData().getEntries();

        long newIdentifier = currentContentEntries.size();
        List<DbDataDto.Item> clonedItems = cloneContentItems(sourceEntry);
        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(newIdentifier)
                .addItems(clonedItems)
                .build();

        DatabaseStructureQueryHelper.getUidFieldRank(topicObject.getStructure().getFields())
                .ifPresent((uidFieldRank) -> {
                    String newReference = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);
                    DbDataDto.Item uidContentItem = BulkDatabaseMiner.getContentItemFromEntryAtFieldRank(topic, newEntry, uidFieldRank).get();
                    uidContentItem.setRawValue(newReference);
                });

        currentContentEntries.add(newEntry);

        return newEntry;
    }

    /**
     * Deletes resource from specified topic, having given reference.
     * @param topic             : database topic where entry should be duplicated
     * @param resourceReference : reference of resource to be deleted
     * @param affectedLocales   : list of locales to be affected by deletion
     * @throws java.util.NoSuchElementException when such a resource entry does not exist in any of affected locales.
     */
    public void removeResourcesWithReference(DbDto.Topic topic, String resourceReference, List<DbResourceDto.Locale> affectedLocales) {
        affectedLocales.stream()

                .map((affectedLocale) -> databaseMiner.getResourceFromTopicAndLocale(topic, affectedLocale).get().getEntries())

                .forEach((resources) -> resources.stream()

                        .filter((resource) -> resource.getReference().equals(resourceReference))

                        .findAny()

                        .ifPresent(resources::remove));
    }

    /**
     * @param entry                     : database entry to be updated
     * @param sourceEntryRef            : reference of source entry (REF field for source topic)
     * @param potentialTargetEntryRef   : reference of target entry (REF field for target topic). Mandatory.
     */
    public static void updateAssociationEntryWithSourceAndTargetReferences(DbDataDto.Entry entry, String sourceEntryRef, Optional<String> potentialTargetEntryRef) {
        requireNonNull(entry, "A content entry is required.");

        List<DbDataDto.Item> entryItems = entry.getItems();

        // We assume source reference is first field ... target reference (if any) is second field  ...
        entryItems.get(0).setRawValue(sourceEntryRef);
        potentialTargetEntryRef.ifPresent((ref) -> entryItems.get(1).setRawValue(ref));
    }

    private void checkResourceDoesNotExistWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference) {
        databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceReference, topic, locale)
                .ifPresent((resourceEntry) -> {
                    throw new IllegalArgumentException("Resource already exists with reference: " + resourceReference);
                });
    }

    private void checkResourceExistsWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference) {
        databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceReference, topic, locale)
                .orElseGet(() -> {
                    throw new IllegalArgumentException("Resource does not exist with reference: " + resourceReference);
                });
    }

    private static List<DbDataDto.Item> cloneContentItems(DbDataDto.Entry entry) {
        return entry.getItems().stream()

                .map((contentItem) -> DbDataDto.Item.builder().fromExisting(contentItem).build())

                .collect(toList());
    }
}