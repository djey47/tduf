package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;

public abstract class CommonHelper {

    private static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    protected final BulkDatabaseMiner miner;

    protected CommonHelper(BulkDatabaseMiner miner) {
        this.miner = requireNonNull(miner, "Database miner instance is required.");
    }

    protected Optional<Resource> getResourceFromDatabaseEntry(DbDataDto.Entry entry, DbDto.Topic topic, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map((item) -> {
                    String value = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), topic, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), value);
                });
    }

    protected Optional<Resource> getResourceFromDatabaseEntry(DbDataDto.Entry entry, int sourceFieldRank, DbDto.Topic targetTopic, int targetFieldRank) {
        return entry.getItemAtRank(sourceFieldRank)
                .flatMap((sourceItem) -> miner.getContentEntryFromTopicWithReference(sourceItem.getRawValue(), targetTopic))
                .flatMap((targetEntry) -> miner.getLocalizedResourceValueFromContentEntry(targetEntry.getId(), targetFieldRank, targetTopic, DEFAULT_LOCALE))
                .map((value) -> Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT, value));
    }

    protected static Optional<Integer> getIntValueFromDatabaseEntry(DbDataDto.Entry entry, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(DbDataDto.Item::getRawValue)
                .map(Integer::valueOf);
    }

    protected static Optional<String> getStringValueFromDatabaseEntry(DbDataDto.Entry entry, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(DbDataDto.Item::getRawValue);
    }
}
