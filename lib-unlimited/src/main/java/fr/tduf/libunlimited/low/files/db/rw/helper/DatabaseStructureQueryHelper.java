package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.Optional;

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
        if (topicObject == null) {
            return null;
        }

        return topicObject.getStructure().getFields().stream()

                .filter((field) -> field.getFieldType() == DbStructureDto.FieldType.UID)

                .findAny();
    }
}