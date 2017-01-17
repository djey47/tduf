package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseStructureHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.UID;
import static java.util.Objects.requireNonNull;

/**
 * Class providing methods to query database structure.
 */
public class DatabaseStructureQueryHelper {
    private static final String MESSAGE_ERR_FIELDS = "A list of fields is required.";

    private static final DatabaseStructureHelper structureHelper = new DatabaseStructureHelper();

    private DatabaseStructureQueryHelper() {}

    /**
     * Finds structure field used for entry identification.
     * @param structureFields   : list of topic fields to search for such a field
     * @return searched field, or empty if it does not exist.
     */
    public static Optional<DbStructureDto.Field> getUidField(List<DbStructureDto.Field> structureFields) {
        return requireNonNull(structureFields, MESSAGE_ERR_FIELDS).stream()
                .filter(field -> UID == field.getFieldType())
                .findAny();
    }

    /**
     * @param structureFields   : list of structure fields for a topic
     * @return rank of UID field in structure if such a field exists, empty otherwise.
     */
    public static OptionalInt getUidFieldRank(List<DbStructureDto.Field> structureFields) {
        Optional<DbStructureDto.Field> potentialUidField = DatabaseStructureQueryHelper.getUidField(requireNonNull(structureFields, MESSAGE_ERR_FIELDS));
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
    public static DbStructureDto.Field getStructureField(ContentItemDto item, List<DbStructureDto.Field> structureFields) {
        requireNonNull(item, "A content entry item is required.");

        return getStructureFieldWithRank(item.getFieldRank(), structureFields);
    }

    /**
     * @param fieldRank         : rank of field
     * @param structureFields   : list of topic fields to search for such a field
     * @return corresponding structure field.
     */
    public static DbStructureDto.Field getStructureFieldWithRank(int fieldRank, List<DbStructureDto.Field> structureFields) {
        return requireNonNull(structureFields, MESSAGE_ERR_FIELDS).stream()
                .filter(field -> field.getRank() == fieldRank)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No structure field for item at rank: " + fieldRank));
    }

    /**
     * @param topic : database topic
     * @return true if specified topic supports entry UID feature (REF)
     */
    public static boolean isUidSupportForTopic(DbDto.Topic topic) {
        return structureHelper.isRefSupportForTopic(topic);
    }
}
