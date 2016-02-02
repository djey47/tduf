package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
public class VehicleSlotsHelper {

    private static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    private final BulkDatabaseMiner miner;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    public enum BankFileType { EXTERIOR_MODEL, INTERIOR_MODEL, HUD, SOUND, FRONT_RIM, REAR_RIM}

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static VehicleSlotsHelper load(BulkDatabaseMiner miner) {
        return new VehicleSlotsHelper(requireNonNull(miner, "Database miner instance is required."));
    }

    /**
     * @param slotReference : vehicle slot reference
     * @return first brand directory found for a vehicle rim
     */
    public String getDefaultRimDirectoryForVehicle(String slotReference) {

        return getDefaultRimEntryForVehicle(slotReference)

                .map((rimEntry) -> getNameFromLocalResources(rimEntry.getId(), RIMS, DatabaseConstants.FIELD_RANK_RSC_PATH, DEFAULT_LOCALE, ""))

                .orElse("");
    }

    /**
     * @param slotReference : vehicle slot reference
     * @param bankFileType  : type of bank file to be resolved
     * @return simple file name
     */
    public String getBankFileName(String slotReference, BankFileType bankFileType) {

        if (FRONT_RIM == bankFileType ||
                REAR_RIM == bankFileType) {
            return getDefaultRimBankFileName(slotReference, bankFileType);
        }

        String suffix;
        switch (bankFileType) {
            case HUD:
            case EXTERIOR_MODEL:
                suffix = "";
                break;

            case INTERIOR_MODEL:
                suffix = FileConstants.SUFFIX_INTERIOR_BANK_FILE;
                break;

            case SOUND:
                suffix = FileConstants.SUFFIX_AUDIO_BANK_FILE;
                break;

            default:
                throw new IllegalArgumentException("Bank file type not handled: " + bankFileType);
        }

        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .map((entryInternalIdentifier) -> getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_FILE_NAME, DEFAULT_LOCALE, ""))

                .map((simpleName) -> String.format("%s%s.%s", simpleName, suffix, EXTENSION_BANKS))

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
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

    /**
     * @return value of ID_Car data for specified slot reference.
     */
    public String getVehicleIdentifier(String slotRerence) {
        return miner.getContentEntryFromTopicWithReference(slotRerence, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .flatMap((entryIdentifier) -> miner.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_ID_CAR, entryIdentifier))

                .map(DbDataDto.Item::getRawValue)

                .orElse("0");
    }

    /**
     * @return value of Car_FileName data for specified slot reference.
     */
    public String getFileName(String slotRerence) {
        return miner.getContentEntryFromTopicWithReference(slotRerence, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .flatMap((entryIdentifier) -> miner.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_FILE_NAME, entryIdentifier))

                .map(DbDataDto.Item::getRawValue)

                .orElse("");
    }

    /**
     * @return list of car physics entries concerning only drivable vehicles
     */
    public List<DbDataDto.Entry> getDrivableVehicleSlotEntries() {

        // TODO enhance criteria to express NOT condition and simplify call
        return miner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries().stream()

                .filter((slotEntry) -> {
                    final String groupRawValue = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_GROUP).get().getRawValue();
                    return !DatabaseConstants.RESOURCE_REF_GROUP_Z.equals(groupRawValue);
                })

                .collect(toList());
    }

    private Optional<DbDataDto.Entry> getDefaultRimEntryForVehicle(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .flatMap((entryId) -> miner.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_DEFAULT_RIMS, entryId))

                .map(DbDataDto.Item::getRawValue)

                .flatMap((rimSlotReference) -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS));
    }

    private String getDefaultRimBankFileName(String slotReference, BankFileType rimBankFileType) {

        return getDefaultRimEntryForVehicle(slotReference)

                .map(DbDataDto.Entry::getId)

                .map((rimEntryIdentifier) -> {
                    int fieldRank = 0;
                    if (FRONT_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT;
                    } else if (REAR_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR;
                    }

                    return getNameFromLocalResources(rimEntryIdentifier, RIMS, fieldRank, DEFAULT_LOCALE, "");
                })

                .map((rimBankSimpleName) -> String.format("%s.%s", rimBankSimpleName, EXTENSION_BANKS))

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
