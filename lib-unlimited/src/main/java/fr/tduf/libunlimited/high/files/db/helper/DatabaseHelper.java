package fr.tduf.libunlimited.high.files.db.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Class providing methods to manipulate databases.
 */
public class DatabaseHelper {

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

        Optional<DbStructureDto.Field> identifierField = DatabaseStructureQueryHelper.getIdentifierField(topicObject);
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
        // FIXME issue when field rank broken (missing fields) - do not rely on field rank for identifier (use a boolean attribute on entry item)
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