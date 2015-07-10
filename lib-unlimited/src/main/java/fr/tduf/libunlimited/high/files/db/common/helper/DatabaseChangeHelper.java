package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class to make changes on database contents and resources.
 */
// TODO Unit tests
// TODO Javadoc
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
     *
     * @param topic
     * @param locale
     * @param resourceReference
     * @param resourceValue
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
     *  @param reference
     * @param topic
     * @return created entry
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
     *
     * @param topic
     * @param locale
     * @param oldResourceReference
     * @param newResourceReference
     * @param newResourceValue
     */
    public void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        checkResourceExistsWithReference(topic, locale, newResourceReference);

        DbResourceDto.Entry existingResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(oldResourceReference, topic, locale).get();

        existingResourceEntry.setReference(newResourceReference);
        existingResourceEntry.setValue(newResourceValue);
    }

    /**
     *
     * @param entryId
     * @param topic
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
     *
     * @param entryId
     * @param topic
     * @return
     */
    public DbDataDto.Entry duplicateEntryWithIdentifier(long entryId, DbDto.Topic topic) {
        DbDataDto.Entry sourceEntry = databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryId, topic).get();

        List<DbDataDto.Entry> currentContentEntries = databaseMiner.getDatabaseTopic(topic).get().getData().getEntries();

        long newIdentifier = currentContentEntries.size();
        List<DbDataDto.Item> clonedItems = cloneContentItems(sourceEntry);
        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(newIdentifier)
                .addItems(clonedItems)
                .build();

        currentContentEntries.add(newEntry);

        return newEntry;
    }

    /**
     *
     * @param topic
     * @param locale
     * @param resourceReference
     * @param affectedLocales
     */
    public void removeResourcesWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, List<DbResourceDto.Locale> affectedLocales) {
        affectedLocales.stream()

                .map((affectedLocale) -> databaseMiner.getResourceFromTopicAndLocale(topic, locale).get().getEntries())

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