package fr.tduf.gui.installer.common.helper;

import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.libunlimited.common.game.domain.Resource;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;

import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;

abstract class CommonHelper {

    private static final Locale DEFAULT_LOCALE = UNITED_STATES;

    protected final BulkDatabaseMiner miner;

    protected CommonHelper(BulkDatabaseMiner miner) {
        this.miner = requireNonNull(miner, "Database miner instance is required.");
    }

    protected Optional<Resource> getResourceFromDatabaseEntry(ContentEntryDto entry, DbDto.Topic topic, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(item -> {
                    String value = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), topic, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), value);
                });
    }

    protected static Optional<Integer> getIntValueFromDatabaseEntry(ContentEntryDto entry, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(ContentItemDto::getRawValue)
                .map(Integer::valueOf);
    }

    protected static Optional<Float> getFloatValueFromDatabaseEntry(ContentEntryDto entry, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(ContentItemDto::getRawValue)
                .map(Float::valueOf);
    }

    protected static Optional<String> getStringValueFromDatabaseEntry(ContentEntryDto entry, int fieldRank) {
        return entry.getItemAtRank(fieldRank)
                .map(ContentItemDto::getRawValue);
    }
}
