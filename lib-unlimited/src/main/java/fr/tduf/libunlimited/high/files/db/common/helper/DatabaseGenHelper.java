package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;
import org.apache.commons.lang3.Range;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing methods to generate database contents and resources.
 */
public class DatabaseGenHelper {

    public static final String RESOURCE_VALUE_DEFAULT = "??";

    private static final String BITFIELD_VALUE_DEFAULT = "0";

    private static final int IDENTIFIER_MIN = 10000000;
    private static final int IDENTIFIER_MAX = 99999999;

    private final BulkDatabaseMiner databaseMiner;

    private final DatabaseChangeHelper changeHelper;

    /**
     * Main constructor
     */
    public DatabaseGenHelper(BulkDatabaseMiner databaseMiner) {
        this.databaseMiner = databaseMiner;
        this.changeHelper = new DatabaseChangeHelper(this, databaseMiner);
    }

    /**
     * Only exists for injecting mocks in tests. Do not remove!
     **/
    private DatabaseGenHelper(BulkDatabaseMiner databaseMiner, DatabaseChangeHelper changeHelper) {
        this.databaseMiner = databaseMiner;
        this.changeHelper = changeHelper;
    }

    /**
     * @param reference   : unique reference of entry to create. If null, a new one will be generated when necessary
     * @param topicObject : database contents hosting items to be created
     * @return created items list with default values.
     */
    public List<ContentItemDto> buildDefaultContentItems(String reference, DbDto topicObject) {
        return topicObject.getStructure().getFields().stream()
                .map(structureField -> buildDefaultContentItem(reference, structureField, topicObject))
                .collect(toList());
    }

    /**
     * @param entryReference      : unique reference of entry to create. If null, a new one will be generated when necessary
     * @param field               : structure field to create item from
     * @param topicObject         : database contents hosting items to be created
     * @return created item with default value.
     */
    public ContentItemDto buildDefaultContentItem(String entryReference, DbStructureDto.Field field, DbDto topicObject) {
        String rawValue;
        DbDto remoteTopicObject = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef());

        DbStructureDto.FieldType fieldType = field.getFieldType();
        switch (fieldType) {
            case UID:
                rawValue = ofNullable(entryReference)
                        .orElseGet(() -> DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject));
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
                rawValue = generateDefaultContentsReference(remoteTopicObject);
                break;
            case RESOURCE_CURRENT_GLOBALIZED:
            case RESOURCE_CURRENT_LOCALIZED:
                rawValue = generateDefaultResourceReference(topicObject);
                break;
            case RESOURCE_REMOTE:
                rawValue = generateDefaultResourceReference(remoteTopicObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled field type: " + fieldType);
        }

        DbDto.Topic topic = topicObject.getTopic();
        return ContentItemDto.builder()
                .fromStructureFieldAndTopic(field, topic)
                .withRawValue(rawValue)
                .build();
    }

    /**
     * Produces a random, unique identifier of content entry.
     *
     * @param topicObject : database topic to provide structure and contents
     * @return null if topicObject is null.
     * @throws java.lang.IllegalArgumentException if current topic does not have an identifier field
     */
    public static String generateUniqueContentsEntryIdentifier(DbDto topicObject) {
        if (topicObject == null) {
            return null;
        }

        Optional<DbStructureDto.Field> identifierField = DatabaseStructureQueryHelper.getUidField(topicObject.getStructure().getFields());
        if (!identifierField.isPresent()) {
            throw new IllegalArgumentException("Provided contents object has no identifier field described in its structure.");
        }

        Set<String> existingEntryRefs = extractContentEntryReferences(identifierField.get(), topicObject);
        return generateUniqueIdentifier(existingEntryRefs, Range.between(IDENTIFIER_MIN, IDENTIFIER_MAX));
    }

    /**
     * Produces a random, unique identifier of content entry.
     *
     * @param topicObject : database topic to provide resources
     * @return null if topicObject is null.
     */
    public static String generateUniqueResourceEntryIdentifier(DbDto topicObject) {
        if (topicObject == null) {
            return null;
        }

        Set<String> existingResourceRefs = extractResourceEntryReferences(topicObject);
        return generateUniqueIdentifier(existingResourceRefs, Range.between(IDENTIFIER_MIN, IDENTIFIER_MAX));
    }

    /**
     * @param existingIdentifiers : set of identifiers which already exist
     * @param range               : range the generated value will be into
     * @return a random, unique identifier in provided range.
     */
    public static String generateUniqueIdentifier(Set<String> existingIdentifiers, Range<Integer> range) {
        if (existingIdentifiers.size() >= range.getMaximum() - range.getMinimum()) {
            throw new IllegalArgumentException("No space left to generate value in provided range: " + range);
        }

        String generatedId = null;
        while (generatedId == null || existingIdentifiers.contains(generatedId)) {
            generatedId = generateIdentifier(range);
        }
        return generatedId;
    }

    /**
     * V2
     *
     * @param topicObject : database contents hosting resources
     * @return reference to default resource reference in current topic. May exist already, otherwise it will be generated.
     */
    String generateDefaultResourceReference(DbDto topicObject) {
        return findDefaultResourceEntry(topicObject)
                .map(ResourceEntryDto::getReference)
                .orElseGet(() -> {
                    String newResourceReference = generateUniqueResourceEntryIdentifier(topicObject);
                    final ResourceEntryDto newEntry = topicObject.getResource().addEntryByReference(newResourceReference);

                    Locale.valuesAsStream().forEach(locale -> newEntry.setValueForLocale(RESOURCE_VALUE_DEFAULT, locale));

                    return newResourceReference;
                });
    }

    private String generateDefaultContentsReference(DbDto topicObject) {
        String newContentsReference = generateUniqueContentsEntryIdentifier(topicObject);
        changeHelper.addContentsEntryWithDefaultItems(newContentsReference, topicObject.getTopic());
        return newContentsReference;
    }

    private static Set<String> extractContentEntryReferences(DbStructureDto.Field identifierField, DbDto topicObject) {
        return topicObject.getData().getEntries().stream()
                .map(entry -> entry.getItems().stream()
                        .filter(item -> item.getFieldRank() == identifierField.getRank())
                        .findAny()
                        .map(ContentItemDto::getRawValue)
                        .orElseThrow(() -> new IllegalStateException("No identifier field for topic object: " + topicObject.getTopic())))

                .collect(toSet());
    }

    private static Set<String> extractResourceEntryReferences(DbDto topicObject) {
        final Collection<ResourceEntryDto> entries = topicObject.getResource().getEntries();
        if(entries == null) {
            return new HashSet<>();
        }

        return entries.stream()
                .map(ResourceEntryDto::getReference)
                .collect(toSet());
    }

    private static String generateIdentifier(Range<Integer> range) {
        final int identifier = new Random().nextInt(range.getMaximum() - range.getMinimum()) + range.getMinimum();
        return Integer.toString(identifier);
    }

    private static Optional<ResourceEntryDto> findDefaultResourceEntry(DbDto topicObject) {
        final Collection<ResourceEntryDto> entries = topicObject.getResource().getEntries();
        if (entries == null) {
            return Optional.empty();
        }

        return entries.stream()
                .filter(entry -> RESOURCE_VALUE_DEFAULT.equals(entry.pickValue()
                        .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No resource value for entry at ref: " + entry.getReference()))))
                .findAny();
    }

    public DatabaseChangeHelper getChangeHelper() {
        return changeHelper;
    }
}
