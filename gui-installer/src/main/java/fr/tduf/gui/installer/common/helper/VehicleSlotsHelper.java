package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;
import static fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto.fromCouple;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
// TODO tests
public class VehicleSlotsHelper {

    private static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    private final BulkDatabaseMiner miner;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    public enum BankFileType { EXTERIOR_MODEL, INTERIOR_MODEL, HUD, SOUND, RIM }

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
    public String getRimDirectoryForVehicle(String slotReference) {

        List<DbFieldValueDto> criteria = singletonList(fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference));
        return miner.getContentEntryStreamMatchingCriteria(criteria, CAR_RIMS)

                .findAny()

                .flatMap((carRimEntry) -> miner.getContentEntryFromTopicWithInternalIdentifier(carRimEntry.getId(), RIMS))

                .map((rimEntry) -> getNameFromLocalResources(rimEntry.getId(), RIMS, DatabaseConstants.FIELD_RANK_RSC_PATH, DEFAULT_LOCALE, ""))

                .orElse("");
    }

    /**
     *
     * @param slotReference
     * @param bankFileType
     * @return
     */
    public String getBankFileName(String slotReference, BankFileType bankFileType) {

        if (BankFileType.RIM == bankFileType) {
            return getRimBankFileName(slotReference);
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
                suffix =  FileConstants.SUFFIX_AUDIO_BANK_FILE;
                break;

            default:
                throw new IllegalArgumentException("Bank file type not handled: " + bankFileType);
        }

        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .map(DbDataDto.Entry::getId)

                .map((entryInternalIdentifier) -> {
                    final String simpleName = getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_FILE_NAME, DEFAULT_LOCALE, "");
                    return String.format("%s%s.%s", simpleName, suffix, EXTENSION_BANKS);
                })

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);

    }

    private String getRimBankFileName(String slotReference) {
        return "";
//        case RIM:
//        suffix = "_%s_%s";
//        break;
//
//        default:
//        throw new IllegalArgumentException("Bank file type not handled: " + bankFileType);
//    }
//
//    return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)
//
//            .map(DbDataDto.Entry::getId)
//
//    .map((entryInternalIdentifier) -> {
//        final String simpleName = getNameFromLocalResources(entryInternalIdentifier, CAR_PHYSICS_DATA, fieldRank, DEFAULT_LOCALE, "");
//        return String.format("%s%s%s.%s", parentDirectory, simpleName, suffix, EXTENSION_BANKS);
//    })
//
//            .orElse(DisplayConstants.ITEM_UNAVAILABLE);        return null;
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
