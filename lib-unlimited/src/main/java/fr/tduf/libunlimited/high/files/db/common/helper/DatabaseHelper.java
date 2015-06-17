package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing methods to generate database contents and resources.
 */
// TODO rename to DatabaseGenHelper
// TODO unit tests
// TODO see to use this class from DatabaseIntegrityFixer
public class DatabaseHelper {

    public static final String RESOURCE_VALUE_DEFAULT = "??";

    private static final String ENTRY_REF_DEFAULT = "00000000";

    private static final String BITFIELD_VALUE_DEFAULT = "0";

    private final BulkDatabaseMiner databaseMiner;

    public DatabaseHelper(BulkDatabaseMiner databaseMiner) {
        this.databaseMiner = databaseMiner;
    }

    /**
     *
     * @param reference
     * @param topicObject
     * @return created items list
     */
    public List<DbDataDto.Item> buildDefaultContentItems(Optional<String> reference, DbDto topicObject) {

        return topicObject.getStructure().getFields().stream()

                .map((structureField) -> buildDefaultContentItem(reference, structureField, topicObject))

                .collect(toList());
    }

    /**
     *
     * @param entryReference
     * @param field
     * @param topicObject
     * @return created item
     */
    public DbDataDto.Item buildDefaultContentItem(Optional<String> entryReference, DbStructureDto.Field field, DbDto topicObject) {
        String rawValue;
        DbDto remoteTopicObject = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef());

        DbStructureDto.FieldType fieldType = field.getFieldType();
        switch (fieldType) {
            case UID:
                if (entryReference.isPresent()) {
                    rawValue = entryReference.get();
                } else {
                    rawValue = DatabaseHelper.generateUniqueContentsEntryIdentifier(topicObject);
                }
                break;
            case BITFIELD:
                rawValue = BITFIELD_VALUE_DEFAULT;
                break;
            case FLOAT:
                rawValue = "0.0";
                break;
            case INTEGER:
                rawValue = "0";
                break;
            case PERCENT:
                rawValue = "1";
                break;
            case REFERENCE:
                rawValue = ENTRY_REF_DEFAULT;
                break;
            case RESOURCE_CURRENT_GLOBALIZED:
            case RESOURCE_CURRENT_LOCALIZED:
                rawValue = getDefaultResourceReference(topicObject);
                break;
            case RESOURCE_REMOTE:
                rawValue = getDefaultResourceReference(remoteTopicObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled field type: " + fieldType);
        }

        return DbDataDto.Item.builder()
                .fromStructureField(field)
                .withRawValue(rawValue)
                .build();
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
     *
     * @param topicObject
     * @return
     */
    public String getDefaultResourceReference(DbDto topicObject) {
        Optional<DbResourceDto.Entry> potentialDefaultResourceEntry = topicObject.getResources().stream().findAny().get().getEntries().stream()

                .filter((anObject) -> RESOURCE_VALUE_DEFAULT.equals(anObject.getValue()))

                .findAny();

        if (potentialDefaultResourceEntry.isPresent()) {
            return potentialDefaultResourceEntry.get().getReference();
        }

        String newResourceReference = generateUniqueResourceEntryIdentifier(topicObject);
        Stream.of(DbResourceDto.Locale.values()).forEach((locale) -> addResourceWithReference(topicObject.getTopic(), locale, newResourceReference, RESOURCE_VALUE_DEFAULT));
        return newResourceReference;
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
                .addItems(buildDefaultContentItems(reference, topicObject))
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
        topicEntries.stream()

                .filter((entry) -> entry.getId() == entryId)

                .findAny()

                .ifPresent(topicEntries::remove);
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
     * Produces a random, unique identifier of content entry.
     * @param topicObject   : database topic to provide structure and contents
     * @return null if topicObject is null.
     * @throws java.lang.IllegalArgumentException if current topic does not have an identifier field
     */
    public static String generateUniqueContentsEntryIdentifier(DbDto topicObject) {
        if (topicObject == null) {
            return null;
        }

        Optional<DbStructureDto.Field> identifierField = DatabaseStructureQueryHelper.getIdentifierField(topicObject.getStructure().getFields());
        if (!identifierField.isPresent()) {
            throw new IllegalArgumentException("Provided contents object has no identifier field described in its structure.");
        }

        Set<String> existingEntryRefs = extractContentEntryReferences(identifierField.get(), topicObject);
        return generateUniqueEntryReference(existingEntryRefs);
    }

    /**
     * Produces a random, unique identifier of content entry.
     * @param topicObject   : database topic to provide resources
     * @return null if topicObject is null.
     */
    public static String generateUniqueResourceEntryIdentifier(DbDto topicObject) {
        if (topicObject == null) {
            return null;
        }

        Set<String> existingResourceRefs = extractResourceEntryReferences(topicObject);
        return generateUniqueEntryReference(existingResourceRefs);
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

    private static Set<String> extractContentEntryReferences(DbStructureDto.Field identifierField, DbDto topicObject) {
        return topicObject.getData().getEntries().stream()

                .map((entry) -> entry.getItems().stream()

                        .filter((item) -> item.getFieldRank() == identifierField.getRank())

                        .findAny().get().getRawValue())

                .collect(toSet());
    }

    private static Set<String> extractResourceEntryReferences(DbDto topicObject) {
        return topicObject.getResources().stream()

                .flatMap((resource -> resource.getEntries().stream()

                        .map(DbResourceDto.Entry::getReference)))

                .collect(toSet());
    }

    private static String generateUniqueEntryReference(Set<String> existingEntryRefs) {
        String generatedRef = null;
        while(generatedRef == null || existingEntryRefs.contains(generatedRef)) {
            generatedRef = generateEntryIdentifier(10000000, 99999999);
        }
        return generatedRef;
    }

    private static String generateEntryIdentifier(int min, int max) {
        return Integer.valueOf((int) (Math.random() * (max - min) + min)).toString();
    }
}