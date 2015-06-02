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

    private static final String BITFIELD_VALUE_DEFAULT = "00000000";

    private final BulkDatabaseMiner databaseMiner;

    public DatabaseHelper(BulkDatabaseMiner databaseMiner) {
        this.databaseMiner = databaseMiner;
    }

    /**
     *
     * @param reference
     * @param topicObject
     * @return
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
     * @return
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
                rawValue = DatabaseHelper.generateUniqueContentsEntryIdentifier(remoteTopicObject);
                addContentsEntryWithDefaultItems(Optional.of(rawValue), remoteTopicObject.getTopic());
                break;
            case RESOURCE_CURRENT_GLOBALIZED:
            case RESOURCE_CURRENT_LOCALIZED:
                rawValue = DatabaseHelper.generateUniqueResourceEntryIdentifier(topicObject);
                addDefaultResourceReferenceForAllLocales(rawValue, topicObject);
                break;
            case RESOURCE_REMOTE:
                rawValue = DatabaseHelper.generateUniqueResourceEntryIdentifier(remoteTopicObject);
                addDefaultResourceReferenceForAllLocales(rawValue, remoteTopicObject);
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
     * @param reference
     * @param topic
     */
    public void addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {

        DbDto topicObject = databaseMiner.getDatabaseTopic(topic).get();

        DbDataDto dataDto = topicObject.getData();

        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(dataDto.getEntries().size())
                .addItems(buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.getEntries().add(newEntry);
    }

    /**
     *
     * @param resourceReference
     * @param topicObject
     */
    public void addDefaultResourceReferenceForAllLocales(String resourceReference, DbDto topicObject) {
        Stream.of(DbResourceDto.Locale.values())

                .forEach((locale) -> addDefaultResourceEntry(resourceReference, topicObject.getTopic(), locale));
    }

    /**
     *
     * @param reference
     * @param topic
     * @param locale
     */
    public void addDefaultResourceEntry(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
        DbResourceDto.Entry newEntry = DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue(RESOURCE_VALUE_DEFAULT)
                .build();

        DbResourceDto resourceDto = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
        resourceDto.getEntries().add(newEntry);
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