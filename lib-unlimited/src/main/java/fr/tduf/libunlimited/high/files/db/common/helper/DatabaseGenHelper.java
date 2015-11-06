package fr.tduf.libunlimited.high.files.db.common.helper;

import com.google.common.annotations.VisibleForTesting;
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
public class DatabaseGenHelper {

    public static final String RESOURCE_VALUE_DEFAULT = "??";

    private static final String BITFIELD_VALUE_DEFAULT = "0";

    private static final int IDENTIFIER_MIN = 10000000;
    private static final int IDENTIFIER_MAX = 99999999;

    private final BulkDatabaseMiner databaseMiner;

    private final DatabaseChangeHelper changeHelper;

    public DatabaseGenHelper(BulkDatabaseMiner databaseMiner) {
        this.databaseMiner = databaseMiner;
        this.changeHelper = new DatabaseChangeHelper(this, databaseMiner);
    }

    /** Only exists for injecting mocks in tests **/
    @VisibleForTesting
    private DatabaseGenHelper(BulkDatabaseMiner databaseMiner, DatabaseChangeHelper changeHelper) {
        this.databaseMiner = databaseMiner;
        this.changeHelper = changeHelper;
    }

    /**
     * @param reference     : unique reference of entry to create. If empty, a new one will be generated when necessary
     * @param topicObject   : database contents hosting items to be created
     * @return created items list with default values.
     */
    public List<DbDataDto.Item> buildDefaultContentItems(Optional<String> reference, DbDto topicObject) {

        return topicObject.getStructure().getFields().stream()

                .map((structureField) -> buildDefaultContentItem(reference, structureField, topicObject, false))

                .collect(toList());
    }

    /**
     * @param entryReference        : unique reference of entry to create. If empty, a new one will be generated when necessary
     * @param field                 : structure field to create item from
     * @param topicObject           : database contents hosting items to be created
     * @param createTargetEntries   : true to generate target resource and contents entries, false to leave raw values empty.
     * @return created item with default value.
     */
    public DbDataDto.Item buildDefaultContentItem(Optional<String> entryReference, DbStructureDto.Field field, DbDto topicObject, boolean createTargetEntries) {
        String rawValue;
        DbDto remoteTopicObject = databaseMiner.getDatabaseTopicFromReference(field.getTargetRef());

        DbStructureDto.FieldType fieldType = field.getFieldType();
        switch (fieldType) {
            case UID:
                if (entryReference.isPresent()) {
                    rawValue = entryReference.get();
                } else {
                    rawValue = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);
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
                rawValue = (createTargetEntries ? generateDefaultContentsReference(remoteTopicObject) : "");
                break;
            case RESOURCE_CURRENT_GLOBALIZED:
            case RESOURCE_CURRENT_LOCALIZED:
                rawValue = (createTargetEntries ? generateDefaultResourceReference(topicObject) : "");
                break;
            case RESOURCE_REMOTE:
                rawValue = (createTargetEntries ? generateDefaultResourceReference(remoteTopicObject) : "");
                break;
            default:
                throw new IllegalArgumentException("Unhandled field type: " + fieldType);
        }

        DbDto.Topic topic = topicObject.getTopic();
        return DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(field, topic)
                .withRawValue(rawValue)
                .build();
    }

    /**
     * @param topicObject   : database contents hosting resources
     * @return reference to default resource reference in current topic. May exist already, otherwise it will be generated.
     */
    public String generateDefaultResourceReference(DbDto topicObject) {
        Optional<DbResourceDto.Entry> potentialDefaultResourceEntry = findDefaultResourceEntry(topicObject);
        if (potentialDefaultResourceEntry.isPresent()) {
            return potentialDefaultResourceEntry.get().getReference();
        }

        String newResourceReference = generateUniqueResourceEntryIdentifier(topicObject);
        Stream.of(DbResourceDto.Locale.values()).forEach((locale) -> changeHelper.addResourceWithReference(topicObject.getTopic(), locale, newResourceReference, RESOURCE_VALUE_DEFAULT));
        return newResourceReference;
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

        Optional<DbStructureDto.Field> identifierField = DatabaseStructureQueryHelper.getUidField(topicObject.getStructure().getFields());
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

    private String generateDefaultContentsReference(DbDto topicObject) {
        String newContentsReference = generateUniqueContentsEntryIdentifier(topicObject);
        changeHelper.addContentsEntryWithDefaultItems(Optional.of(newContentsReference), topicObject.getTopic());
        return newContentsReference;
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
            generatedRef = generateEntryIdentifier(IDENTIFIER_MIN, IDENTIFIER_MAX);
        }
        return generatedRef;
    }

    private static String generateEntryIdentifier(int min, int max) {
        return Integer.valueOf((int) (Math.random() * (max - min) + min)).toString();
    }

    private static Optional<DbResourceDto.Entry> findDefaultResourceEntry(DbDto topicObject) {
        return topicObject.getResources().stream().findAny().get().getEntries().stream()

                .filter((anObject) -> RESOURCE_VALUE_DEFAULT.equals(anObject.getValue()))

                .findAny();
    }

    public DatabaseChangeHelper getChangeHelper() {
        return changeHelper;
    }
}