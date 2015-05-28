package fr.tduf.gui.database.common.helper;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

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
            return DisplayConstants.VALUE_UNKNOWN;
        }

        List<String> contents = fieldRanks.stream()

                .map((fieldRank) -> {
                    Optional<DbResourceDto.Entry> potentialRemoteResourceEntry = databaseMiner.getResourceEntryWithInternalIdentifier(topic, fieldRank, entryId, locale);
                    if (potentialRemoteResourceEntry.isPresent()) {
                        return potentialRemoteResourceEntry.get().getValue();
                    }

                    return databaseMiner.getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, entryId).get().getRawValue();
                })

                .collect(toList());

        return String.join(DisplayConstants.SEPARATOR_VALUES, contents);
    }
}