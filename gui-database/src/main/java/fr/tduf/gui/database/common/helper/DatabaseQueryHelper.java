package fr.tduf.gui.database.common.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.List;

import static fr.tduf.gui.database.common.DisplayConstants.SEPARATOR_VALUES;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class to retrieve and format specific values from database.
 */
public class DatabaseQueryHelper {

    private DatabaseQueryHelper() {}

    /**
     * @return a string of all resource values from specified field ranks, using a particular locale.
     */
    public static String fetchResourceValuesWithEntryId(int entryId, DbDto.Topic topic, Locale locale, List<Integer> fieldRanks, BulkDatabaseMiner databaseMiner, EditorLayoutDto editorLayoutDto) {
        requireNonNull(databaseMiner, "A database miner must be provided.");
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return String.format(DisplayConstants.VALUE_UNKNOWN, "?");
        }

        List<DbStructureDto.Field> structureFields = databaseMiner.getDatabaseTopic(topic)
                .map(DbDto::getStructure)
                .map(DbStructureDto::getFields)
                .orElseThrow(() -> new IllegalStateException("No structure found for topic: " + topic));

        List<String> contents = fieldRanks.stream()
                .map(fieldRank -> resolveResourceValue(entryId, topic, locale, databaseMiner, editorLayoutDto, structureFields, fieldRank))
                .collect(toList());

        return join(SEPARATOR_VALUES, contents);
    }

    private static String resolveResourceValue(int entryId, DbDto.Topic topic, Locale locale, BulkDatabaseMiner databaseMiner, EditorLayoutDto editorLayoutDto, List<DbStructureDto.Field> structureFields, Integer fieldRank) {
        DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureFieldWithRank(fieldRank, structureFields);
        if (REFERENCE == structureField.getFieldType()) {
            return resolveValueForReferenceField(entryId, topic, locale, databaseMiner, editorLayoutDto, fieldRank, structureField);
        }
        return resolveValueForOtherField(entryId, topic, locale, databaseMiner, fieldRank);
    }

    private static String resolveValueForOtherField(int entryId, DbDto.Topic topic, Locale locale, BulkDatabaseMiner databaseMiner, Integer fieldRank) {
        return databaseMiner.getLocalizedResourceValueFromContentEntry(entryId, fieldRank, topic, locale)
                .orElseGet(() -> {
                    final String rawValue = databaseMiner.getContentItemWithEntryIdentifierAndFieldRank(topic, fieldRank, entryId)
                            .map(ContentItemDto::getRawValue)
                            .orElseThrow(() -> new IllegalStateException("No content item with identifier and field rank: (" + entryId + ":" + fieldRank + ")"));
                    return String.format(DisplayConstants.VALUE_UNKNOWN, rawValue);
                });
    }

    private static String resolveValueForReferenceField(int entryId, DbDto.Topic topic, Locale locale, BulkDatabaseMiner databaseMiner, EditorLayoutDto editorLayoutDto, Integer fieldRank, DbStructureDto.Field structureField) {
        final DbDto.Topic remoteTopic = databaseMiner.getDatabaseTopicFromReference(structureField.getTargetRef()).getTopic();
        return databaseMiner.getRemoteContentEntryWithInternalIdentifier(topic, fieldRank, entryId, remoteTopic)
                .map(ContentEntryDto::getId)
                .map(remoteEntryId -> {
                    final List<Integer> labelFieldRanks = EditorLayoutHelper.getAvailableProfileByTopic(remoteTopic, editorLayoutDto).getEntryLabelFieldRanks();
                    return fetchResourceValuesWithEntryId(remoteEntryId, remoteTopic, locale, labelFieldRanks, databaseMiner, editorLayoutDto);
                })
                .orElse(String.format(DisplayConstants.VALUE_UNKNOWN, ""));
    }
}
