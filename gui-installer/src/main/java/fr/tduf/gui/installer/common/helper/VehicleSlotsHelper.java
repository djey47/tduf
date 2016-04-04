package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
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
     * @return a new helper instance.
     */
    public static VehicleSlotsHelper load(BulkDatabaseMiner miner) {
        return new VehicleSlotsHelper(requireNonNull(miner, "Database miner instance is required."));
    }

    /**
     * @return a new slot instance if it exists with given REF, or empty otherwise.
     */
    public Optional<VehicleSlot> getVehicleSlotFromReference(String slotReference) {
        requireNonNull(slotReference, "Slot reference is required.");
        requireNonNull(miner, "Database miner instance is required.");

        // TODO extract methods
        final Optional<DbDataDto.Entry> defaultRimEntry = getDefaultRimEntryForVehicle(slotReference);
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
                        String defaultValue = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), RIMS, DEFAULT_LOCALE)
                                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                        return Resource.from(item.getRawValue(), defaultValue);
                    })
                    .orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT));

            Resource frontFileName = defaultRimEntry
                    .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT))
                    .map((item) -> {
                        String defaultValue = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), RIMS, DEFAULT_LOCALE)
                                .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                        return Resource.from(item.getRawValue(), defaultValue);
                    })
                    .orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT));
            Resource rearFileName = defaultRimEntry
                    .flatMap((rimEntry) -> rimEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR))
                    .map((item) -> {
                        String defaultValue = miner.getLocalizedResourceValueFromTopicAndReference(item.getRawValue(), RIMS, DEFAULT_LOCALE)
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

        Resource modelName = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME))
                .map((item) -> {
                    String nameValue = miner.getLocalizedResourceValueFromContentEntry(physicsEntry.get().getId(), DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME, CAR_PHYSICS_DATA, DEFAULT_LOCALE)
                            .orElse(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
                    return Resource.from(item.getRawValue(), nameValue);
                })
                .orElse(null);

        Resource versionName = physicsEntry
                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME))
                .map((item) -> {
                    String nameValue = miner.getLocalizedResourceValueFromContentEntry(physicsEntry.get().getId(), DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME, CAR_PHYSICS_DATA, DEFAULT_LOCALE)
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
                .withModelName(modelName)
                .withVersionName(versionName)
                .withDefaultRims(defaultRims)
                .withBrandName(brandName)
                .build());
    }

    /**
     * @param bankFileType  : type of bank file to be resolved
     * @return simple file name
     */
    public static String getBankFileName(VehicleSlot vehicleSlot, BankFileType bankFileType, boolean withExtension) {

        final String extension = withExtension ? "." + GenuineBnkGateway.EXTENSION_BANKS : "";

        if (FRONT_RIM == bankFileType ||
                REAR_RIM == bankFileType) {
            return getDefaultRimBankFileName(vehicleSlot, bankFileType, extension);
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

        return String.format("%s%s%s", vehicleSlot.getFileName().getValue(), suffix, extension);
    }

    /**
     * @return in-game vehicle name.
     */
    public static String getVehicleName(VehicleSlot vehicleSlot) {

        final Resource realName = vehicleSlot.getRealName();
        if (realName != null && !realName.getRef().equals(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME)) {
            return realName.getValue();
        }

        final String brandName = ofNullable(vehicleSlot.getBrandName())
                .map(Resource::getValue)
                .orElse("");
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

    private Optional<DbDataDto.Entry> getDefaultRimEntryForVehicle(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))

                .map(DbDataDto.Item::getRawValue)

                .flatMap((rimSlotReference) -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS));
    }

    private static String getDefaultRimBankFileName(VehicleSlot vehicleSlot, BankFileType rimBankFileType, String extension) {
        RimSlot.RimInfo rimInfo;
        if (FRONT_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims().getFrontRimInfo();
        } else if (REAR_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims().getRearRimInfo();
        } else {
            throw new IllegalArgumentException("Invalid bank file type: " + rimBankFileType);
        }

        return of(rimInfo.getFileName().getValue())
                .map((rimBankSimpleName) -> String.format("%s%s", rimBankSimpleName, extension))
                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
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
