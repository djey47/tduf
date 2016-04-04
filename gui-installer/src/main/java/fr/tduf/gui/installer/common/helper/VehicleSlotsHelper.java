package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
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
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
// TODO Provide single method to load vehicle data into a domain object (VehicleSlot) and use it
public class VehicleSlotsHelper {

    public static final int DEFAULT_VEHICLE_ID = 0;

    private static final DbResourceDto.Locale DEFAULT_LOCALE = UNITED_STATES;

    private final BulkDatabaseMiner miner;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        this.miner = miner;
    }

    public enum BankFileType { EXTERIOR_MODEL, INTERIOR_MODEL, HUD, SOUND, FRONT_RIM, REAR_RIM}

    /**
     * @param miner : component to parse database
     * @return a new slot instance if it exists with given REF, or empty otherwise.
     */
    public static Optional<VehicleSlot> loadVehicleSlotFromReference(String slotReference, BulkDatabaseMiner miner) {
        requireNonNull(slotReference, "Slot reference is required.");
        requireNonNull(miner, "Database miner instance is required.");

        // TODO extract methods
        final Optional<DbDataDto.Entry> defaultRimEntry = getDefaultRimEntryForVehicle(slotReference, miner);
        if (!defaultRimEntry.isPresent()) {
            return empty();
        }

        Optional<String> defaultRimsReference = defaultRimEntry
                .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RIM_REF))
                .map(DbDataDto.Item::getRawValue);

        RimSlot defaultRims = null;
        if (defaultRimsReference.isPresent()) {
            Resource defaulRimsParentDirectory = defaultRimEntry
                    .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_PATH))
                    .map((item) -> {
                        String defaultValue = miner.getLocalizedResourceValueFromContentEntry(defaultRimEntry.get().getId(), DatabaseConstants.FIELD_RANK_RSC_PATH, RIMS, DEFAULT_LOCALE)
                                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                        return Resource.from(item.getRawValue(), defaultValue);
                    })
                    .orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT));

            Resource frontFileName = defaultRimEntry
                    .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT))
                    .map((item) -> {
                        String defaultValue = miner.getLocalizedResourceValueFromContentEntry(defaultRimEntry.get().getId(), DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT, RIMS, DEFAULT_LOCALE)
                                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                        return Resource.from(item.getRawValue(), defaultValue);
                    })
                    .orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT));
            Resource rearFileName = defaultRimEntry
                    .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR))
                    .map((item) -> {
                        String defaultValue = miner.getLocalizedResourceValueFromContentEntry(defaultRimEntry.get().getId(), DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR, RIMS, DEFAULT_LOCALE)
                                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                        return Resource.from(item.getRawValue(), defaultValue);
                    })
                    .orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT));
            RimSlot.RimInfo frontInfo = RimSlot.RimInfo
                    .builder()
                    .withFileName(frontFileName)
                    .build();
            RimSlot.RimInfo rearInfo = RimSlot.RimInfo
                    .builder()
                    .withFileName(rearFileName)
                    .build();

            defaultRims = RimSlot
                    .builder()
                    .withRef(defaultRimsReference.get())
                    .withParentDirectoryName(defaulRimsParentDirectory)
                    .withRimsInformation(frontInfo, rearInfo)
                    .build();
        }

        final Optional<DbDataDto.Entry> physicsEntry = miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA);
        Resource fileName = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_FILE_NAME))
                .map((item) -> {
                    String nameValue = miner.getLocalizedResourceValueFromContentEntry(physicsEntry.get().getId(), DatabaseConstants.FIELD_RANK_CAR_FILE_NAME, CAR_PHYSICS_DATA, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), nameValue);
                })
                .orElse(null);

        Resource realName = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REAL_NAME))
                .map((item) -> {
                    String nameValue = miner.getLocalizedResourceValueFromContentEntry(physicsEntry.get().getId(), DatabaseConstants.FIELD_RANK_CAR_REAL_NAME, CAR_PHYSICS_DATA, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), nameValue);
                })
                .orElse(null);

        int carIdentifier = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_ID_CAR))
                .map(DbDataDto.Item::getRawValue)
                .map(Integer::valueOf)
                .orElse(DEFAULT_VEHICLE_ID);

        Resource brandName = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_BRAND))
                .flatMap((item) -> miner.getContentEntryFromTopicWithReference(item.getRawValue(), BRANDS))
                .flatMap((brandsEntry) ->  miner.getLocalizedResourceValueFromContentEntry(brandsEntry.getId(), DatabaseConstants.FIELD_RANK_MANUFACTURER_NAME, BRANDS, DEFAULT_LOCALE))
                .map((value) -> Resource.from("", value))
                .orElse(Resource.from("", DatabaseConstants.RESOURCE_VALUE_DEFAULT));

        return of(VehicleSlot.builder()
                .withRef(slotReference)
                .withCarIdentifier(carIdentifier)
                .withFileName(fileName)
                .withRealName(realName)
                .withDefaultRims(defaultRims)
                .withBrandName(brandName)
                .build());
    }

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static VehicleSlotsHelper load(BulkDatabaseMiner miner) {
        return new VehicleSlotsHelper(requireNonNull(miner, "Database miner instance is required."));
    }

    /**
     * @param bankFileType  : type of bank file to be resolved
     * @return simple file name
     */
    public static String getBankFileName(VehicleSlot vehicleSlot, BankFileType bankFileType) {

        if (FRONT_RIM == bankFileType ||
                REAR_RIM == bankFileType) {
            return getDefaultRimBankFileName(vehicleSlot, bankFileType);
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

        return String.format("%s%s.%s", vehicleSlot.getFileName(), suffix, GenuineBnkGateway.EXTENSION_BANKS);
    }

    /**
     * @return in-game vehicle name, or N/A if unavailable.
     */
    public static String getVehicleName(VehicleSlot vehicleSlot) {

        final Resource realName = vehicleSlot.getRealName();
        if (realName != null) {
            return realName.getValue();
        }

        final String brandName = vehicleSlot.getBrandName().getValue();
        final String modelName = getNameFromLocalResourceValue(ofNullable(vehicleSlot.getModelName().getValue()), "");
        final String versionName = getNameFromLocalResourceValue(ofNullable(vehicleSlot.getVersionName().getValue()), "");

        return String.format("%s %s %s", brandName, modelName, versionName).trim();
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

    private static Optional<DbDataDto.Entry> getDefaultRimEntryForVehicle(String slotReference, BulkDatabaseMiner miner) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))

                .map(DbDataDto.Item::getRawValue)

                .flatMap((rimSlotReference) -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS));
    }

    private static String getDefaultRimBankFileName(VehicleSlot vehicleSlot, BankFileType rimBankFileType) {
        RimSlot.RimInfo rimInfo;
        if (FRONT_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims().getFrontRimInfo();
        } else if (REAR_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims().getRearRimInfo();
        } else {
            throw new IllegalArgumentException("Invalid bank file type: " + rimBankFileType);
        }

        return of(rimInfo.getFileName().getValue())
                .map((rimBankSimpleName) -> String.format("%s.%s", rimBankSimpleName, EXTENSION_BANKS))
                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
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
     * @return first brand directory resource reference for a vehicle rim
     */
    public String getDefaultRimDirectoryResource(String slotReference) {
        return getDefaultRimEntryForVehicle(slotReference)

                .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_PATH))

                .map(DbDataDto.Item::getRawValue)

                .orElse(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND);
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

                .orElse(DEFAULT_VEHICLE_ID);
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

                .flatMap((resourceRef) -> miner.getLocalizedResourceValueFromTopicAndReference(resourceRef, RIMS, DEFAULT_LOCALE))

                .map((rimBankSimpleName) -> String.format("%s.%s", rimBankSimpleName, EXTENSION_BANKS))

                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    // TODO move these methods to library when needed
    private String getNameFromRemoteEntryResources(long entryInternalIdentifier, DbDto.Topic localTopic, DbDto.Topic remoteTopic, int localFieldRank, int remoteFieldRank, DbResourceDto.Locale locale, String defaultValue) {
        final Optional<String> remoteResourceEntry = miner.getRemoteContentEntryWithInternalIdentifier(localTopic, localFieldRank, entryInternalIdentifier, remoteTopic)
                .flatMap((remoteEntry) -> miner.getLocalizedResourceValueFromContentEntry(remoteEntry.getId(), remoteFieldRank, remoteTopic, locale));
        return getNameFromLocalResourceValue(remoteResourceEntry, defaultValue);
    }

    private String getNameFromLocalResources(long entryInternalIdentifier, DbDto.Topic topic, int fieldRank, DbResourceDto.Locale locale, String defaultValue) {
        Optional<String> resourceValue = miner.getLocalizedResourceValueFromContentEntry(entryInternalIdentifier, fieldRank, topic, locale);
        return getNameFromLocalResourceValue(resourceValue, defaultValue);
    }

    private static String getNameFromLocalResourceValue(Optional<String> potentialValue, String defaultValue) {
        return potentialValue

                .map((resourceValue) -> DatabaseConstants.RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                .orElse(defaultValue);
    }
}
