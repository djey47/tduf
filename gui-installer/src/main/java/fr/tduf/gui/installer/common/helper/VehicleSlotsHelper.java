package fr.tduf.gui.installer.common.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static fr.tduf.gui.installer.common.DatabaseConstants.*;
import static fr.tduf.gui.installer.common.DisplayConstants.*;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.FRONT_RIM;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.REAR_RIM;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle slots.
 */
public class VehicleSlotsHelper extends CommonHelper {
    private static final String THIS_CLASS_NAME = VehicleSlotsHelper.class.getSimpleName();
    private static final Class<VehicleSlotsHelper> thisClass = VehicleSlotsHelper.class;

    public static final int DEFAULT_VEHICLE_ID = 0;

    private static final int DEFAULT_CAM_ID = 0;

    private static final List<String> RESOURCE_REFS_CAR_GROUPS = asList(RESOURCE_REF_GROUP_A, RESOURCE_REF_GROUP_B, RESOURCE_REF_GROUP_C, RESOURCE_REF_GROUP_D, RESOURCE_REF_GROUP_E, RESOURCE_REF_GROUP_F, RESOURCE_REF_GROUP_G);
    private static final List<String> RESOURCE_REFS_BIKE_GROUPS = asList(RESOURCE_REF_GROUP_MA, RESOURCE_REF_GROUP_MB);

    private static List<String> tducpUnlockedSlotRefs;
    private static Pattern tducpCarSlotPattern;
    private static Pattern tducpBikeSlotPattern;

    static {
        loadProperties();
    }

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        super(miner);
    }

    /**
     * All handled bank file types
     */
    public enum BankFileType {
        EXTERIOR_MODEL, INTERIOR_MODEL, HUD, SOUND, FRONT_RIM, REAR_RIM
    }

    /**
     * Criteria for slot lookups by vehicle type
     */
    public enum VehicleKind {
        DRIVABLE(ITEM_VEHICLE_KIND_DRIVABLE),
        CAR(ITEM_VEHICLE_KIND_CAR),
        BIKE(ITEM_VEHICLE_KIND_BIKE);
        private final String label;

        VehicleKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

    }

    /**
     * Criteria for slot lookups by slot type
     */
    public enum SlotKind {
        ALL(ITEM_SLOT_KIND_ALL),
        GENUINE(ITEM_SLOT_KIND_GENUINE),
        TDUCP(ITEM_SLOT_KIND_TDUCP);
        private final String label;

        SlotKind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

    }

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
        Optional<Integer> cameraIdentifier = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_ID_CAM);

        Optional<Float> secuOptionOne = getFloatValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_SECU1);
        Optional<Integer> secuOptionTwo = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_SECU2);

        List<PaintJob> paintJobs = getPaintJobsForVehicle(slotReference);

        List<RimSlot> rims = getAllRimsForVehicle(slotReference);

        return of(VehicleSlot.builder()
                .withRef(slotReference)
                .withCarIdentifier(carIdentifier.orElse(DEFAULT_VEHICLE_ID))
                .withFileName(fileName.orElse(null))
                .withRealName(realName.orElse(null))
                .withModelName(modelName.orElse(null))
                .withVersionName(versionName.orElse(null))
                .withBrandName(brandName.orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .withCameraIdentifier(cameraIdentifier.orElse(DEFAULT_CAM_ID))
                .withSecurityOptions(secuOptionOne.orElse(SecurityOptions.ONE_DEFAULT), secuOptionTwo.orElse(SecurityOptions.TWO_DEFAULT))
                .addPaintJobs(paintJobs)
                .addRims(rims)
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
     * @param rimBankFileType  : type of bank file to be resolved
     * @return simple file name
     */
    public static String getRimBankFileName(VehicleSlot vehicleSlot, BankFileType rimBankFileType, int rimRank) {
        if (FRONT_RIM != rimBankFileType &&
                REAR_RIM != rimBankFileType) {
            throw new IllegalArgumentException("Not a valid rim bank type: " + rimBankFileType);
        }

        final RimSlot rimSlot = vehicleSlot.getRimAtRank(rimRank)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle slot hasn't required rim at rank: " + rimRank));
        RimSlot.RimInfo rimInfo = FRONT_RIM == rimBankFileType ?
                rimSlot.getFrontRimInfo() : rimSlot.getRearRimInfo();

        return of(rimInfo.getFileName().getValue())
                .map(rimBankSimpleName -> String.format("%s.%s", rimBankSimpleName, GenuineBnkGateway.EXTENSION_BANKS))
                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    /**
     * @return in-game vehicle name.
     */
    public static String getVehicleName(VehicleSlot vehicleSlot) {

        final Resource realName = vehicleSlot.getRealName();
        if (realName != null && !realName.getRef().equals(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME)) {
            return realName.getValue();
        }

        final String brandName = getNameFromLocalResourceValue(ofNullable(vehicleSlot.getBrandName().getValue()), "");
        final String modelName = getNameFromLocalResourceValue(ofNullable(vehicleSlot.getModelName().getValue()), "");
        final String versionName = getNameFromLocalResourceValue(ofNullable(vehicleSlot.getVersionName().getValue()), "");

        return String.format("%s %s %s", brandName, modelName, versionName).trim();
    }

    /**
     * @return list of car physics entries following criteria
     */
    public List<VehicleSlot> getVehicleSlots(SlotKind slotKind, VehicleKind vehicleKind) {
        return miner.getDatabaseTopic(CAR_PHYSICS_DATA).get().getData().getEntries().stream()

                .filter(slotEntry -> byVehicleKind(slotEntry, vehicleKind))

                .filter(slotEntry -> bySlotKind(slotEntry, slotKind))

                .map(drivableSlotEntry -> drivableSlotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REF).get())

                .map(drivableSlotItem -> getVehicleSlotFromReference(drivableSlotItem.getRawValue()))

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toList());
    }

    private List<PaintJob> getPaintJobsForVehicle(String slotReference) {
        AtomicInteger paintJobIndex = new AtomicInteger(1);
        return miner.getContentEntriesMatchingCriteria(singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference)), CAR_COLORS).stream()

                .map(entry -> {
                    final Optional<Resource> nameResource = getResourceFromDatabaseEntry(entry, CAR_COLORS, DatabaseConstants.FIELD_RANK_COLOR_NAME);
                    final PaintJob.PaintJobBuilder paintJobBuilder = PaintJob.builder()
                            .atRank(paintJobIndex.getAndIncrement())
                            .withName(nameResource.orElse(null));

                    IntStream.rangeClosed(DatabaseConstants.FIELD_RANK_INTERIOR_1, DatabaseConstants.FIELD_RANK_INTERIOR_15)
                            .mapToObj(rank -> entry.getItemAtRank(rank).get().getRawValue())
                            .filter(interiorPatternRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(interiorPatternRef))
                            .forEach(paintJobBuilder::addInteriorPattern);

                    return paintJobBuilder.build();
                })

                .collect(toList());
    }

    private List<RimSlot> getAllRimsForVehicle(String slotReference) {
        final Optional<String> defaultRimsReference = miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)
                .flatMap(entry -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))
                .map(DbDataDto.Item::getRawValue);

        AtomicInteger rimRank = new AtomicInteger(1);
        return miner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference), CAR_RIMS)
                .map(entry -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_RIM_ASSO_REF).get())
                .map(DbDataDto.Item::getRawValue)
                .map(rimSlotReference -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS).get())
                .map(rimEntry -> getRimSlotFromDatabaseEntry(rimEntry, rimRank.getAndIncrement(), defaultRimsReference.orElse(null)))
                .collect(toList());
    }

    private RimSlot getRimSlotFromDatabaseEntry(DbDataDto.Entry rimEntry, int rimRank, String defaultRimsReference) {
        String rimsReference = getStringValueFromDatabaseEntry(rimEntry, DatabaseConstants.FIELD_RANK_RIM_REF).get();
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

        boolean defaultRims = ofNullable(defaultRimsReference)
                .map(ref -> ref.equals(rimsReference))
                .orElse(false);

        return RimSlot
                .builder()
                .withRef(rimsReference)
                .atRank(rimRank)
                .withParentDirectoryName(defaulRimsParentDirectory.orElse(Resource.from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .withRimsInformation(frontInfo, rearInfo)
                .setDefaultRims(defaultRims)
                .build();
    }

    private static String getDefaultRimBankFileName(VehicleSlot vehicleSlot, BankFileType rimBankFileType, String extension) {
        Optional<RimSlot.RimInfo> rimInfo;
        if (FRONT_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims()
                    .map(RimSlot::getFrontRimInfo);
        } else if (REAR_RIM == rimBankFileType) {
            rimInfo = vehicleSlot.getDefaultRims()
                    .map(RimSlot::getRearRimInfo);
        } else {
            throw new IllegalArgumentException("Invalid bank file type: " + rimBankFileType);
        }

        return rimInfo
                .map(info -> info.getFileName().getValue())
                .map(rimBankSimpleName -> String.format("%s%s", rimBankSimpleName, extension))
                .orElse(DisplayConstants.ITEM_UNAVAILABLE);
    }

    private static String getNameFromLocalResourceValue(Optional<String> potentialValue, String defaultValue) {
        return potentialValue

                .map(resourceValue -> DatabaseConstants.RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)

                .orElse(defaultValue);
    }

    private static boolean byVehicleKind(DbDataDto.Entry slotEntry, VehicleKind vehicleKind) {
        final String groupRawValue = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_GROUP).get().getRawValue();
        switch (vehicleKind) {
            case DRIVABLE:
                return !DatabaseConstants.RESOURCE_REF_GROUP_Z.equals(groupRawValue);
            case CAR:
                return RESOURCE_REFS_CAR_GROUPS.contains(groupRawValue);
            case BIKE:
                return RESOURCE_REFS_BIKE_GROUPS.contains(groupRawValue);
            default:
                return false;
        }
    }

    private static boolean bySlotKind(DbDataDto.Entry slotEntry, SlotKind slotKind) {
        final String slotReference = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REF).get().getRawValue();
        switch (slotKind) {
            case ALL:
                return true;
            case GENUINE:
                return !isTDUCPVehicleSlot(slotReference);
            case TDUCP:
                return isTDUCPVehicleSlot(slotReference);
            default:
                return false;
        }
    }

    private static boolean isTDUCPVehicleSlot(String slotReference) {
        return tducpCarSlotPattern.matcher(slotReference).matches()
                || tducpBikeSlotPattern.matcher(slotReference).matches()
                || tducpUnlockedSlotRefs.contains(slotReference);
    }

    private static void loadProperties() {
        Properties vehicleSlotsProperties = new Properties();

        String[] refs = new String[0];
        String tducpBikeSlotRegex = "";
        String tducpCarSlotRegex = "";
        try (InputStream resourceAsStream = thisClass.getResourceAsStream("/gui-installer/conf/vehicleSlots.properties")) {
            vehicleSlotsProperties.load(resourceAsStream);

            refs = vehicleSlotsProperties.getProperty("tducp.slots.unlocked.refs", "").split(",");
            tducpBikeSlotRegex = vehicleSlotsProperties.getProperty("tducp.slots.new.bikes.pattern", "");
            tducpCarSlotRegex = vehicleSlotsProperties.getProperty("tducp.slots.new.cars.pattern", "");
        } catch (IOException e) {
            Log.error(THIS_CLASS_NAME, e);
        }

        tducpUnlockedSlotRefs = Collections.unmodifiableList(asList(refs));
        tducpBikeSlotPattern = Pattern.compile(tducpBikeSlotRegex);
        tducpCarSlotPattern = Pattern.compile(tducpCarSlotRegex);
    }

    static List<String> getTducpUnlockedSlotRefs() {
        return tducpUnlockedSlotRefs;
    }

    static Pattern getTducpCarSlotPattern() {
        return tducpCarSlotPattern;
    }

    static Pattern getTducpBikeSlotPattern() {
        return tducpBikeSlotPattern;
    }
}
