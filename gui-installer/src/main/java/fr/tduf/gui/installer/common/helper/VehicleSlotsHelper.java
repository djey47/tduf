package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;

/**
 * Component to get advanced information on vehicle slots.
 */
public class VehicleSlotsHelper {

    private  static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    private final BulkDatabaseMiner miner;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static VehicleSlotsHelper load(BulkDatabaseMiner miner) {
        return new VehicleSlotsHelper(requireNonNull(miner, "Database miner instance is required."));
    }

    /**
     * @param slotReference : reference (REF value) of entry
     * @return in-game vehicle name, or N/A if unavailable.
     */
    public String getVehicleName(String slotReference) {

        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .map((entryInternalIdentifier) -> {
                    final String realName = getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_REAL_NAME, DEFAULT_LOCALE, "");
                    if (!"".equals(realName)) {
                        return realName;
                    }

                    final String brandName = getNameFromRemoteEntryResources(entryInternalIdentifier, CAR_PHYSICS_DATA, BRANDS, DatabaseConstants.FIELD_RANK_CAR_BRAND, DatabaseConstants.FIELD_RANK_MANUFACTURER_NAME, DEFAULT_LOCALE, "");
                    final String modelName = getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME, DEFAULT_LOCALE, "");
                    final String versionName = getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME, DEFAULT_LOCALE, "");

                    return String.format("%s %s %s", brandName, modelName, versionName).trim();
                })

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    // TODO move these methods to library when needed
    private String getNameFromRemoteEntryResources(long entryInternalIdentifier, DbDto.Topic localTopic, DbDto.Topic remoteTopic, int localFieldRank, int remoteFieldRank, DbResourceDto.Locale locale, String defaultValue) {
        final Optional<DbResourceDto.Entry> remoteResourceEntry = miner.getRemoteContentEntryWithInternalIdentifier(localTopic, localFieldRank, entryInternalIdentifier, remoteTopic)

                .flatMap((remoteEntry) -> miner.getResourceEntryWithContentEntryInternalIdentifier(remoteTopic, remoteFieldRank, remoteEntry.getId(), locale));

        return getNameFromLocalResources(remoteResourceEntry, defaultValue);
    }

    private String getNameFromLocalResources(long entryInternalIdentifier, DbDto.Topic topic, int fieldRank, DbResourceDto.Locale locale, String defaultValue) {
        final Optional<DbResourceDto.Entry> resourceEntry = miner.getResourceEntryWithContentEntryInternalIdentifier(topic, fieldRank, entryInternalIdentifier, locale);

        return getNameFromLocalResources(resourceEntry, defaultValue);
    }

    private String getNameFromLocalResources(Optional<DbResourceDto.Entry> potentialResourceEntry, String defaultValue) {
        return potentialResourceEntry

                .map(DbResourceDto.Entry::getValue)

                .map((resourceValue) -> DatabaseConstants.RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                .orElse(defaultValue);
    }
}
