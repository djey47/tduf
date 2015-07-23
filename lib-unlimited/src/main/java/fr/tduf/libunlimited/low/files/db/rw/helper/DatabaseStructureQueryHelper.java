package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

/**
 * Class providing methods to query database structure.
 */
public class DatabaseStructureQueryHelper {

    // TODO rename to getUidField
    /**
     * Finds structure field used for entry identification.
     * @param structureFields   : list of topic fields to search for such a field
     * @return searched field, or empty if it does not exist.
     */
    public static Optional<DbStructureDto.Field> getIdentifierField(List<DbStructureDto.Field> structureFields) {
        requireNonNull(structureFields, "A list of fields is required.");

        return structureFields.stream()

                .filter((field) -> field.getFieldType() == DbStructureDto.FieldType.UID)

                .findAny();
    }

    /**
     * @param structureFields   : list of structure fields for a topic
     * @return rank of UID field in structure if such a field exists, empty otherwise.
     */
    public static OptionalInt getUidFieldRank(List<DbStructureDto.Field> structureFields) {
        requireNonNull(structureFields, "A list of fields is required.");

        Optional<DbStructureDto.Field> potentialUidField = DatabaseStructureQueryHelper.getIdentifierField(structureFields);
        if (potentialUidField.isPresent()) {
            return OptionalInt.of(potentialUidField.get().getRank());
        }
        return OptionalInt.empty();
    }

    /**
     * @param item              : a contents entry item
     * @param structureFields   : list of topic fields to search for such a field
     * @return corresponding structure field.
     */
    public static DbStructureDto.Field getStructureField(DbDataDto.Item item, List<DbStructureDto.Field> structureFields) {
        requireNonNull(item, "A content entry item is required.");
        requireNonNull(structureFields, "A list of fields is required.");

        return structureFields.stream()

                .filter((field) -> field.getRank() == item.getFieldRank())

                .findAny().get();
    }
}