package fr.tduf.gui.installer.common.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.libunlimited.common.game.domain.*;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static fr.tduf.gui.installer.common.DisplayConstants.*;
import static fr.tduf.libunlimited.common.game.domain.Resource.from;
import static fr.tduf.libunlimited.common.game.FileConstants.SUFFIX_AUDIO_BANK_FILE;
import static fr.tduf.libunlimited.common.game.FileConstants.SUFFIX_INTERIOR_BANK_FILE;
import static fr.tduf.libunlimited.high.files.db.common.DatabaseConstants.*;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.FRONT_RIMS_3D;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.REAR_RIMS_3D;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toCollection;
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

    private BrandHelper brandHelper;

    private VehicleSlotsHelper(BulkDatabaseMiner miner) {
        super(miner);
        brandHelper = BrandHelper.load(miner);
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
        TDUCP(ITEM_SLOT_KIND_TDUCP),
        TDUCP_NEW(ITEM_SLOT_KIND_TDUCP_NEW);
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

        final Optional<ContentEntryDto> potentialPhysicsEntry = miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA);
        if (!potentialPhysicsEntry.isPresent()) {
            return empty();
        }

        final ContentEntryDto physicsEntry = potentialPhysicsEntry.get();

        Optional<Resource> fileName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_FILE_NAME);
        Optional<Resource> realName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_REAL_NAME);
        Optional<Resource> modelName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_MODEL_NAME);
        Optional<Resource> versionName = getResourceFromDatabaseEntry(physicsEntry, CAR_PHYSICS_DATA, DatabaseConstants.FIELD_RANK_CAR_VERSION_NAME);

        Optional<Integer> carIdentifier = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_ID_CAR);
        Optional<Integer> cameraIdentifier = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_ID_CAM);

        Optional<Float> secuOptionOne = getFloatValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_SECU1);
        Optional<Integer> secuOptionTwo = getIntValueFromDatabaseEntry(physicsEntry, DatabaseConstants.FIELD_RANK_SECU2);

        List<PaintJob> paintJobs = getPaintJobsForVehicle(slotReference);

        List<RimSlot> rimOptions = getAllRimOptionsForVehicle(slotReference);
        List<RimSlot> rimCandidates = getAllRimCandidatesForVehicle(slotReference, rimOptions);

        Brand brand = physicsEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_BRAND)
                .map(ContentItemDto::getRawValue)
                .flatMap(rawValue -> brandHelper.getBrandFromReference(rawValue))
                .orElseGet(VehicleSlotsHelper::createDefaultBrand);

        return of(VehicleSlot.builder()
                .withRef(slotReference)
                .withCarIdentifier(carIdentifier.orElse(DEFAULT_VEHICLE_ID))
                .withBrand(brand)
                .withFileName(fileName.orElse(null))
                .withRealName(realName.orElse(null))
                .withModelName(modelName.orElse(null))
                .withVersionName(versionName.orElse(null))
                .withCameraIdentifier(cameraIdentifier.orElse(DEFAULT_CAM_ID))
                .withSecurityOptions(secuOptionOne.orElse(SecurityOptions.ONE_DEFAULT), secuOptionTwo.orElse(SecurityOptions.TWO_DEFAULT))
                .addPaintJobs(paintJobs)
                .addRimOptions(rimOptions)
                .addRimCandidates(rimCandidates)
                .build());
    }

    /**
     * @param bankFileType  : type of bank file to be resolved
     * @param withExtension : true to append appropriate extension, false otherwise
     * @return simple file name
     */
    public static String getBankFileName(VehicleSlot vehicleSlot, MappedFileKind bankFileType, boolean withExtension) {

        final String extension = withExtension ?
                "." + GenuineBnkGateway.EXTENSION_BANKS : "";

        String suffix;
        switch (bankFileType) {
            case HUD:
            case EXT_3D:
                suffix = "";
                break;

            case INT_3D:
                suffix = SUFFIX_INTERIOR_BANK_FILE;
                break;

            case SOUND:
                suffix = SUFFIX_AUDIO_BANK_FILE;
                break;

            default:
                throw new IllegalArgumentException("Bank file type not handled: " + bankFileType);
        }

        return String.format("%s%s%s", vehicleSlot.getFileName().getValue(), suffix, extension);
    }


    /**
     * @param rimBankFileType : type of bank file to be resolved
     * @return simple file name
     */
    public static String getRimBankFileName(VehicleSlot vehicleSlot, MappedFileKind rimBankFileType, int rimRank, boolean withExtension) {
        String extension = withExtension ?
                "." + GenuineBnkGateway.EXTENSION_BANKS : "";
        if (FRONT_RIMS_3D != rimBankFileType &&
                REAR_RIMS_3D != rimBankFileType) {
            throw new IllegalArgumentException("Not a valid rim bank type: " + rimBankFileType);
        }

        final RimSlot rimSlot = vehicleSlot.getRimAtRank(rimRank)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle slot hasn't required rim at rank: " + rimRank));
        RimSlot.RimInfo rimInfo = FRONT_RIMS_3D == rimBankFileType ?
                rimSlot.getFrontRimInfo() : rimSlot.getRearRimInfo();

        return of(rimInfo.getFileName().getValue())
                .map(rimBankSimpleName -> String.format("%s%s", rimBankSimpleName, extension))
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

        final String brandName = getNameFromLocalResourceValue(vehicleSlot.getBrand().getDisplayedName().getValue());
        final String modelName = getNameFromLocalResourceValue(vehicleSlot.getModelName().getValue());
        final String versionName = getNameFromLocalResourceValue(vehicleSlot.getVersionName().getValue());

        return String.format("%s %s %s", brandName, modelName, versionName).trim();
    }

    /**
     * @return true if specified slot reference is for a TDUCP new car, false otherwise.
     */
    public static boolean isTDUCPNewCarSlot(String slotReference) {
        return tducpCarSlotPattern.matcher(slotReference).matches();
    }

    /**
     * @return true if specified slot reference is for a TDUCP new bike, false otherwise.
     */
    public static boolean isTDUCPNewBikeSlot(String slotReference) {
        return tducpBikeSlotPattern.matcher(slotReference).matches();
    }

    /**
     * @return list of car physics entries following criteria
     */
    public List<VehicleSlot> getVehicleSlots(SlotKind slotKind, VehicleKind vehicleKind) {
        return miner.getDatabaseTopic(CAR_PHYSICS_DATA)
                .orElseThrow(() -> new IllegalStateException("Database topic not found: CAR_PHYSICS_DATA"))
                .getData().getEntries().stream()
                .filter(slotEntry -> byVehicleKind(slotEntry, vehicleKind))
                .filter(slotEntry -> bySlotKind(slotEntry, slotKind))
                .map(drivableSlotEntry -> drivableSlotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REF)
                        .orElseThrow(() -> new IllegalStateException("No item at rank 1: slot reference")))
                .map(drivableSlotItem -> getVehicleSlotFromReference(drivableSlotItem.getRawValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private List<PaintJob> getPaintJobsForVehicle(String slotReference) {
        AtomicInteger paintJobIndex = new AtomicInteger(1);
        return miner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference), CAR_COLORS)

                .map(entry -> {
                    final Optional<Resource> nameResource = getResourceFromDatabaseEntry(entry, CAR_COLORS, DatabaseConstants.FIELD_RANK_COLOR_NAME);
                    final PaintJob.PaintJobBuilder paintJobBuilder = PaintJob.builder()
                            .atRank(paintJobIndex.getAndIncrement())
                            .withName(nameResource.orElse(null));

                    IntStream.rangeClosed(DatabaseConstants.FIELD_RANK_INTERIOR_1, DatabaseConstants.FIELD_RANK_INTERIOR_15)
                            .mapToObj(rank -> entry.getItemAtRank(rank)
                                    .orElseThrow(() -> new IllegalStateException("No INTERIOR item at rank: " + rank))
                                    .getRawValue())
                            .filter(interiorPatternRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(interiorPatternRef))
                            .forEach(paintJobBuilder::addInteriorPattern);

                    return paintJobBuilder.build();
                })

                .collect(toList());
    }

    private List<RimSlot> getAllRimOptionsForVehicle(String slotReference) {
        final Optional<String> defaultRimsReference = miner.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)
                .flatMap(entry -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_DEFAULT_RIMS))
                .map(ContentItemDto::getRawValue);

        AtomicInteger rimRank = new AtomicInteger(1);
        return miner.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotReference), CAR_RIMS)
                .map(entry -> entry.getItemAtRank(DatabaseConstants.FIELD_RANK_RIM_ASSO_REF)
                        .orElseThrow(() -> new IllegalStateException("No CAR_RIMS item at rank 2")))
                .map(ContentItemDto::getRawValue)
                .map(rimSlotReference -> miner.getContentEntryFromTopicWithReference(rimSlotReference, RIMS)
                        .orElseThrow(() -> new IllegalStateException("No RIMS entry at ref: " + rimSlotReference)))
                .map(rimEntry -> getRimSlotFromDatabaseEntry(rimEntry, rimRank.getAndIncrement(), defaultRimsReference.orElse(null)))
                .collect(toList());
    }

    private List<RimSlot> getAllRimCandidatesForVehicle(String slotReference, List<RimSlot> rimOptions) {
        if (!isTDUCPNewVehicleSlot(slotReference)) {
            return new ArrayList<>(0);
        }

        final RimSlot defaultRims = rimOptions.stream()
                .filter(RimSlot::isDefault)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No default rims for slot ref: " + slotReference));
        final String defaultRimsReference = defaultRims.getRef();

        List<RimSlot> rimCandidates = new ArrayList<>(9);
        rimCandidates.add(defaultRims);

        AtomicInteger rimRank = new AtomicInteger(2);
        return IntStream.rangeClosed(2, 9)
                .mapToObj(index -> defaultRimsReference.substring(0, 8) + Integer.toString(index))
                .map(potentialRimRef -> miner.getContentEntryFromTopicWithReference(potentialRimRef, RIMS))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rimEntry -> getRimSlotFromDatabaseEntry(rimEntry, rimRank.getAndIncrement(), defaultRimsReference))
                .collect(toCollection(() -> rimCandidates));
    }

    private RimSlot getRimSlotFromDatabaseEntry(ContentEntryDto rimEntry, int rimRank, String defaultRimsReference) {
        String rimsReference = getStringValueFromDatabaseEntry(rimEntry, DatabaseConstants.FIELD_RANK_RIM_REF)
                .orElseThrow(() -> new IllegalStateException("No RIMS entry ref at rank 1"));
        Optional<Resource> defaulRimsParentDirectory = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_PATH);
        Optional<Resource> frontFileName = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_FRONT);
        Optional<Resource> rearFileName = getResourceFromDatabaseEntry(rimEntry, RIMS, DatabaseConstants.FIELD_RANK_RSC_FILE_NAME_REAR);

        RimSlot.RimInfo frontInfo = RimSlot.RimInfo
                .builder()
                .withFileName(frontFileName.orElse(from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .build();
        RimSlot.RimInfo rearInfo = RimSlot.RimInfo
                .builder()
                .withFileName(rearFileName.orElse(from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .build();

        boolean defaultRims = ofNullable(defaultRimsReference)
                .map(ref -> ref.equals(rimsReference))
                .orElse(false);

        return RimSlot
                .builder()
                .withRef(rimsReference)
                .atRank(rimRank)
                .withParentDirectoryName(defaulRimsParentDirectory.orElse(from(DatabaseConstants.RESOURCE_REF_DEFAULT_RIM_BRAND, DatabaseConstants.RESOURCE_VALUE_DEFAULT)))
                .withRimsInformation(frontInfo, rearInfo)
                .setDefaultRims(defaultRims)
                .build();
    }

    private static Brand createDefaultBrand() {
        return Brand.builder()
                .withReference(DisplayConstants.ITEM_UNAVAILABLE)
                .withIdentifier(from(DatabaseConstants.RESOURCE_REF_DEFAULT, DatabaseConstants.RESOURCE_VALUE_DEFAULT))
                .withDisplayedName(from(DatabaseConstants.RESOURCE_REF_DEFAULT, DatabaseConstants.RESOURCE_VALUE_DEFAULT))
                .build();
    }

    private static String getNameFromLocalResourceValue(String potentialValue) {
        return ofNullable(potentialValue)
                .map(resourceValue -> DatabaseConstants.RESOURCE_VALUE_NONE.equals(resourceValue) ? null : resourceValue)
                .orElse("");
    }

    private static boolean byVehicleKind(ContentEntryDto slotEntry, VehicleKind vehicleKind) {
        final String groupRawValue = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_GROUP)
                .orElseThrow(() -> new IllegalStateException("No CAR_PHYSICS_DATA entry item at rank 5"))
                .getRawValue();
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

    private static boolean bySlotKind(ContentEntryDto slotEntry, SlotKind slotKind) {
        final String slotReference = slotEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_CAR_REF)
                .orElseThrow(() -> new IllegalStateException("No CAR_PHYSICS_DATA entry REF at rank 1"))
                .getRawValue();
        switch (slotKind) {
            case ALL:
                return true;
            case GENUINE:
                return !isTDUCPVehicleSlot(slotReference);
            case TDUCP:
                return isTDUCPVehicleSlot(slotReference);
            case TDUCP_NEW:
                return isTDUCPNewVehicleSlot(slotReference);
            default:
                return false;
        }
    }

    private static boolean isTDUCPVehicleSlot(String slotReference) {
        return tducpCarSlotPattern.matcher(slotReference).matches()
                || tducpBikeSlotPattern.matcher(slotReference).matches()
                || tducpUnlockedSlotRefs.contains(slotReference);
    }

    private static boolean isTDUCPNewVehicleSlot(String slotReference) {
        return tducpCarSlotPattern.matcher(slotReference).matches()
                || tducpBikeSlotPattern.matcher(slotReference).matches();
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

    // For testing use
    void setBrandHelper(BrandHelper brandHelper) {
        this.brandHelper = brandHelper;
    }
}
