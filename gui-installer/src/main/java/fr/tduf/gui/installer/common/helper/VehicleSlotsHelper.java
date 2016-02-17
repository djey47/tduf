package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto.Locale.UNITED_STATES;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
// TODO Provide single method to load vehicle data into a domain object (VehicleSlot) and use it
public class VehicleSlotsHelper {

    private static final DbResourceEnhancedDto.Locale DEFAULT_LOCALE = UNITED_STATES;

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

                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
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
    public int getVehicleIdentifier(String slotRerence) {
        return miner.getContentEntryFromTopicWithReference(slotRerence, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_ID_CAR))

                .map(DbDataDto.Item::getRawValue)

                .map(Integer::valueOf)

                .orElse(0);
    }

    /**
     * @return value of Car_FileName data for specified slot reference.
     */
    public String getCarFileNameReference(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_FILE_NAME))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    /**
     * @return value of Car_Model data for specified slot reference.
     */
    public String getModelNameReference(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME);
    }

    /**
     * @return value of Car_Version data for specified slot reference.
     */
    public String getVersionNameReference(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME);
    }

    /**
     * @return value of Color_Name data (CAR_COLORS) at exteriorIndex for specified slot reference
     */
    public String getColorNameReference(String slotReference, int exteriorIndex) {
        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference));
        final List<DbDataDto.Entry> exteriorEntries = miner.getContentEntriesMatchingCriteria(criteria, CAR_COLORS);

        if (exteriorEntries.size() >= exteriorIndex) {
            int exteriorFieldRank = exteriorIndex - 1;
            return exteriorEntries.get(exteriorFieldRank)
                    .getItemAtRank(DatabaseConstants.FIELD_RANK_COLOR_NAME).get()
                    .getRawValue();
        }

        return DatabaseConstants.RESOURCE_REF_UNKNOWN_COLOR_NAME;
    }

    /**
     * @return value of Interior_ data (CAR_COLORS) at exteriorIndex and interiorIndex for specified slot reference
     */
    public String getInteriorReference(String slotReference, int exteriorIndex, int interiorIndex) {
        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference));
        final List<DbDataDto.Entry> exteriorEntries = miner.getContentEntriesMatchingCriteria(criteria, CAR_COLORS);

        if (exteriorEntries.size() >= exteriorIndex) {
            int interiorFieldRank = DatabaseConstants.FIELD_RANK_INTERIOR_1 + interiorIndex - 1;
            return exteriorEntries.get(exteriorIndex - 1)
                    .getItemAtRank(interiorFieldRank).get()
                    .getRawValue();
        }

        return DisplayConstants.ITEM_UNAVAILABLE;
    }

    /**
     * @return value of REF data (INTERIOR) at exteriorIndex and interiorIndex for specified slot reference
     */
    public String getInteriorNameReference(String slotReference, int exteriorIndex, int interiorIndex) {
        String interiorReference = getInteriorReference(slotReference, exteriorIndex, interiorIndex);
        return miner.getContentEntryFromTopicWithReference(interiorReference, INTERIOR)

                .flatMap((interiorEntry) -> interiorEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_INTERIOR_NAME))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DatabaseConstants.RESOURCE_REF_NONE_INTERIOR_NAME);
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

    /**
     * @return value of REF data (RIMS) for default rims for specified slot reference
     */
    public String getDefaultRimIdentifier(String slotReference) {
        return getDefaultRimEntryForVehicle(slotReference)

                .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RIM_REF))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    /**
     * @return value of Rsc_File_Name_Front or Rsc_File_Name_Rear
     */
    public String getDefaultRimFileNameReference(String slotReference, BankFileType rimBankFileType) {

        return getDefaultRimEntryForVehicle(slotReference)

                .flatMap((rimEntry) -> {
                    int fieldRank;
                    if (FRONT_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT;
                    } else if (REAR_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR;
                    } else {
                        throw new IllegalArgumentException("Invalid bank file type: " + rimBankFileType);
                    }

                    return rimEntry.getItemAtRank(fieldRank);
                })

                .map(DbDataDto.Item::getRawValue)

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    private Optional<DbDataDto.Entry> getDefaultRimEntryForVehicle(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))

                .map(DbDataDto.Item::getRawValue)

                .flatMap((rimSlotReference) -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS));
    }

    private String getDefaultRimBankFileName(String slotReference, BankFileType rimBankFileType) {

        return getDefaultRimEntryForVehicle(slotReference)

                .flatMap((rimEntry) -> {
                    int fieldRank;
                    if (FRONT_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT;
                    } else if (REAR_RIM == rimBankFileType) {
                        fieldRank = DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR;
                    } else {
                        throw new IllegalArgumentException("Invalid bank file type: " + rimBankFileType);
                    }

                    return rimEntry.getItemAtRank(fieldRank);
                })

                .map(DbDataDto.Item::getRawValue)

                .flatMap((resourceRef) -> miner.getResourceEntryFromTopicAndLocaleWithReference(resourceRef, RIMS, DEFAULT_LOCALE))

                .map(DbResourceDto.Entry::getValue)

                .map((rimBankSimpleName) -> String.format("%s.%s", rimBankSimpleName, EXTENSION_BANKS))

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    // TODO move these methods to library when needed
    private String getNameFromRemoteEntryResources(long entryInternalIdentifier, DbDto.Topic localTopic, DbDto.Topic remoteTopic, int localFieldRank, int remoteFieldRank, DbResourceEnhancedDto.Locale locale, String defaultValue) {
        final Optional<DbResourceDto.Entry> remoteResourceEntry = miner.getRemoteContentEntryWithInternalIdentifier(localTopic, localFieldRank, entryInternalIdentifier, remoteTopic)

                .flatMap((remoteEntry) -> miner.getResourceEntryWithContentEntryInternalIdentifier(remoteTopic, remoteFieldRank, remoteEntry.getId(), locale));

        return getNameFromLocalResources(remoteResourceEntry, defaultValue);
    }

    private String getNameFromLocalResources(long entryInternalIdentifier, DbDto.Topic topic, int fieldRank, DbResourceEnhancedDto.Locale locale, String defaultValue) {
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
