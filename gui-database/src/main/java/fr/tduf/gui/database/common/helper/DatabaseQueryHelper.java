package fr.tduf.gui.database.common.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class to retrieve and format specific values from database.
 */
public class DatabaseQueryHelper {

    /**
     * @return a string of all resource values from specified field ranks, using a particular locale.
     */
    public static String fetchResourceValuesWithEntryId(long entryId, DbDto.Topic topic, DbResourceDto.Locale locale, List<Integer> fieldRanks, BulkDatabaseMiner databaseMiner) {
        requireNonNull(databaseMiner, "A database miner must be provided.");
        requireNonNull(fieldRanks, "A list of field ranks (even empty) must be provided.");

        if (fieldRanks.isEmpty()) {
            return String.format(DisplayConstants.VALUE_UNKNOWN, "?");
        }

        List<String> contents = fieldRanks.stream()

                .map((fieldRank) -> databaseMiner.getLocalizedResourceValueFromContentEntry(entryId, fieldRank, topic, locale)
                        .orElseGet(() -> {
                            final String rawValue = databaseMiner.getContentItemWithEntryIdentifierAndFieldRank(topic, fieldRank, entryId)
                                    .map(DbDataDto.Item::getRawValue)
                                    .get();
                            return String.format(DisplayConstants.VALUE_UNKNOWN, rawValue);
                        }))

                .collect(toList());

        return String.join(DisplayConstants.SEPARATOR_VALUES, contents);
    }
}
