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

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
public class VehicleSlotsHelper extends CommonHelper {

    public static final int DEFAULT_VEHICLE_ID = 0;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        super(miner);
    }

    public enum BankFileType {EXTERIOR_MODEL, INTERIOR_MODEL, HUD, SOUND, FRONT_RIM, REAR_RIM}

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

        final Optional<DbDataDto.Entry> potentialPhysicsEntry = miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA);
        if (!potentialPhysicsEntry.isPresent()) {
            return empty();
        }

        final DbDataDto.Entry physicsEntry = potentialPhysicsEntry.get();

        Optional<Resource> brandName = getResourceFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_CAR_BRAND, BRANDS, DatabaseConstants.FIELD_RANK_MANUFACTURER_NAME);

        Optional<Resource> fileName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_FILE_NAME);
        Optional<Resource> realName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_REAL_NAME);
        Optional<Resource> modelName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME);
        Optional<Resource> versionName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME);

        Optional<Integer> carIdentifier = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_ID_CAR);

        final Optional<RimSlot> defaultRims = getDefaultRimEntryForVehicle(slotReference);

        return of(VehicleSlot.builder()
                .withRef(slotReference)
                .withCarIdentifier(carIdentifier.orElse(DEFAULT_VEHICLE_ID))
                .withFileName(fileName.orElse(null))
                .withRealName(realName.orElse(null))
                .withModelName(modelName.orElse(null))
                .withVersionName(versionName.orElse(null))
                .withDefaultRims(defaultRims.orElse(null))
                .withBrandName(brandName.orElse(Resource.from("", DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .build());
    }

    /**
     * @param bankFileType  : type of bank file to be resolved
     * @param withExtension : true to append appropriate extension, false otherwise
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
    public List<VehicleSlot> getDrivableVehicleSlots() {

        // TODO enhance criteria to express NOT condition and simplify call
        return miner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries().stream()

                .filter((slotEntry) -> {
                    final String groupRawValue = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_GROUP).get().getRawValue();
                    return !DatabaseConstants.RESOURCE_REF_GROUP_Z.equals(groupRawValue);
                })

                .map((drivableSlotEntry) -> drivableSlotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REF).get())

                .map((drivableSlotItem) -> getVehicleSlotFromReference(drivableSlotItem.getRawValue()))

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toList());
    }

    private Optional<RimSlot> getDefaultRimEntryForVehicle(String slotReference) {
        return miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)

                .flatMap((entry) -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))

                .map(DbDataDto.Item::getRawValue)

                .flatMap((rimSlotReference) -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS))

                .map(this::getRimSlotFromDatabaseEntry);
    }

    private RimSlot getRimSlotFromDatabaseEntry(DbDataDto.Entry rimEntry) {
        String defaultRimsReference = getStringValueFromDatabaseEntry(rimEntry, DatabaseConstants.FIELD_RANK_RIM_REF).get();
        Optional<Resource> defaulRimsParentDirectory = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_PATH);
        Optional<Resource> frontFileName = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT);
        Optional<Resource> rearFileName = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR);

        RimSlot.RimInfo frontInfo = RimSlot.RimInfo
                .builder()
                .withFileName(frontFileName.orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .build();
        RimSlot.RimInfo rearInfo = RimSlot.RimInfo
                .builder()
                .withFileName(rearFileName.orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .build();

        return RimSlot
                .builder()
                .withRef(defaultRimsReference)
                .withParentDirectoryName(defaulRimsParentDirectory.orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .withRimsInformation(frontInfo, rearInfo)
                .build();
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

    private static String getNameFromLocalResourceValue(Optional<String> potentialValue, String defaultValue) {
        return potentialValue

                .map((resourceValue) -> DatabaseConstants.RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                .orElse(defaultValue);
    }
}
