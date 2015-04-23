package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Class providing methods to query database structure.
 */
public class DatabaseStructureQueryHelper {

    /**
     * Finds structure field used for entry identification.
     * @param topicObject   : database topic to search for such a field
     * @return searched field, or empty if it does not exist.
     */
    public static Optional<DbStructureDto.Field> getIdentifierField(DbDto topicObject) {
        // TODO throw exception (requirenonnull)
        if (topicObject == null) {
            return null;
        }

        return getIdentifierField(topicObject.getStructure().getFields());
    }

    /**
     * Finds structure field used for entry identification.
     * @param structureFields   : database topic to search for such a field
     * @return searched field, or empty if it does not exist.
     */
    public static Optional<DbStructureDto.Field> getIdentifierField(List<DbStructureDto.Field> structureFields) {
        requireNonNull(structureFields, "A list of fields is required.");

        return structureFields.stream()

                .filter((field) -> field.getFieldType() == DbStructureDto.FieldType.UID)

                .findAny();
    }
}