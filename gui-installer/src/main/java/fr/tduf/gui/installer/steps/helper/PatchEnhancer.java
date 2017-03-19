package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.BrandHelper;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

/**
 * Utility class to add instructions and properties over an existing install patch template
 */
public class PatchEnhancer {
    private static final String THIS_CLASS_NAME = PatchEnhancer.class.getSimpleName();

    private final DatabaseContext databaseContext;

    private VehicleSlotsHelper vehicleSlotsHelper;
    private BrandHelper brandHelper;

    /**
     * Unique way to get an instance.
     * @param databaseContext   : database information
     */
    public PatchEnhancer(DatabaseContext databaseContext) {
        this.databaseContext = requireNonNull(databaseContext, "Database context is required");
        this.vehicleSlotsHelper = VehicleSlotsHelper.load(databaseContext.getMiner());
        this.brandHelper = BrandHelper.load(databaseContext.getMiner());
    }

    /**
     * Put additional instructions to patch template.
     */
    public void enhancePatchObject() {
        DatabasePatchProperties patchProperties = requireNonNull(databaseContext.getPatchProperties(), "Patch properties are required.");

        enhancePatchProperties(patchProperties);

        final String vehicleSlotReference = patchProperties.getVehicleSlotReference()
                .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Selected vehicle slot not found in properties"));

        if(patchProperties.getDealerReference().isPresent()
                && patchProperties.getDealerSlot().isPresent()) {
            enhancePatchObjectWithLocationChange(vehicleSlotReference);
        }

        enhancePatchObjectWithInstallFlag(vehicleSlotReference);

        databaseContext.getUserSelection().getVehicleSlot()
                .ifPresent(vehicleSlot -> {
                    enhancePatchObjectWithPaintJobs(vehicleSlot, patchProperties);
                    enhancePatchObjectWithRims(vehicleSlot, patchProperties);
                });
    }

    void enhancePatchProperties(DatabasePatchProperties patchProperties) {
        VehicleSlot effectiveSlot = databaseContext.getUserSelection().getVehicleSlot()
                .orElseGet(() -> {
                    final String forcedSlotReference = patchProperties.getVehicleSlotReference()
                            .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Selected vehicle slot not found in properties"));
                    return vehicleSlotsHelper.getVehicleSlotFromReference(forcedSlotReference)
                            .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Selected vehicle slot not found in database: " + forcedSlotReference));
                });
        createPatchPropertiesForVehicleSlot(effectiveSlot, patchProperties);

        if (databaseContext.getUserSelection().getDealer().isPresent()) {
            createPatchPropertiesForDealerSlot(databaseContext.getUserSelection(), patchProperties);
        }

        if (patchProperties.getBrand().isPresent()) {
            createPatchPropertiesForBrand(patchProperties.getBrand().get(), patchProperties);
        } else if (!patchProperties.getBrandReference().isPresent()) {
            throw new IllegalArgumentException("BRAND or BRANDREF properties not found");
        }
    }

    void enhancePatchObjectWithPaintJobs(VehicleSlot vehicleSlot, DatabasePatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Adding paint jobs properties and changes to initial patch");

        if (vehicleSlot.getPaintJobs().isEmpty()) {
            return;
        }

        final List<String> availableInteriorRefs = vehicleSlot.getPaintJobs().get(0).getInteriorPatternRefs();
        List<String> effectiveInteriorRefs = getEffectiveInteriorReferences(availableInteriorRefs, patchProperties);

        enhancePatchObjectWithExteriors(vehicleSlot, effectiveInteriorRefs, patchProperties);
        enhancePatchObjectWithInteriors(availableInteriorRefs, patchProperties);
    }

    void enhancePatchObjectWithRims(VehicleSlot vehicleSlot, DatabasePatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Adding rim properties and changes to initial patch");

        final List<RimSlot> rims = vehicleSlot.getAllRimCandidatesSorted().isEmpty() ?
                vehicleSlot.getAllRimOptionsSorted() : vehicleSlot.getAllRimCandidatesSorted();

        // TODO handle rims at index 0??
        AtomicInteger rimIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForRims = rims.stream()
                .flatMap(rimSlot -> createChangeObjectsAndPropertiesForRims(vehicleSlot, rimSlot, rimIndex.getAndIncrement(), patchProperties))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForRims);
    }

    void enhancePatchObjectWithLocationChange(String vehicleSlotReference) {
        Log.info(THIS_CLASS_NAME, "->Adding dealer slot change to initial patch");

        DatabasePatchProperties patchProperties = databaseContext.getPatchProperties();
        int effectiveFieldRank = patchProperties.getDealerSlot()
                .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Selected dealer slot index not found in properties"))
                + DatabaseConstants.DELTA_RANK_DEALER_SLOTS;

        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_SHOPS)
                .withType(UPDATE)
                .asReference(patchProperties.getDealerReference()
                        .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Selected dealer reference not found in properties")))
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(effectiveFieldRank, vehicleSlotReference)))
                .build();

        databaseContext.getPatchObject().getChanges().add(changeObject);
    }

    void enhancePatchObjectWithInstallFlag(String vehicleSlotReference) {
        Log.info(THIS_CLASS_NAME, "->Adding install flag change to initial patch");

        final String secuOneRawValue = SecurityOptions.INSTALLED.setScale(0, RoundingMode.UNNECESSARY).toString();
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(UPDATE)
                .asReference(vehicleSlotReference)
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_SECU1, secuOneRawValue)))
                .build();

        databaseContext.getPatchObject().getChanges().add(changeObject);
    }

    private void createPatchPropertiesForVehicleSlot(VehicleSlot effectiveSlot, DatabasePatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with slot information");

        String slotReference = effectiveSlot.getRef();
        int selectedCarIdentifier = effectiveSlot.getCarIdentifier();
        if (VehicleSlotsHelper.DEFAULT_VEHICLE_ID == selectedCarIdentifier) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        String selectedBankName = VehicleSlotsHelper.getBankFileName(effectiveSlot, EXT_3D, false);
        String selectedResourceBankName = effectiveSlot.getFileName().getRef();
        List<String> values = asList(selectedBankName, selectedResourceBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.toString(selectedCarIdentifier));
        patchProperties.setBankNameIfNotExists(selectedBankName);
        patchProperties.setResourceBankNameIfNotExists(selectedResourceBankName);
    }

    private void createPatchPropertiesForDealerSlot(UserSelection userSelection, DatabasePatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with dealer slot information");

        patchProperties.setDealerReferenceIfNotExists(userSelection.getDealer()
                .map(Dealer::getRef)
                .orElseThrow(() -> new IllegalArgumentException("No dealer reference was selected!")));
        patchProperties.setDealerSlotIfNotExists(userSelection.getDealerSlotRank());
    }

    private void createPatchPropertiesForBrand(String brand, DatabasePatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with brand information");

        String brandReference = brandHelper.getBrandFromIdentifierOrName(brand)
                .map(Brand::getRef)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with identifier or name: " + brand));

        patchProperties.setBrandReferenceIfNotExists(brandReference);
    }

    private void enhancePatchObjectWithExteriors(VehicleSlot vehicleSlot, List<String> interiorPatternRefs, DatabasePatchProperties patchProperties) {
        // TODO handle pj at index 0??
        AtomicInteger exteriorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForPaintJobs = vehicleSlot.getPaintJobs().stream()
                .flatMap(paintJob -> createChangeObjectsAndPropertiesForExterior(paintJob, exteriorIndex.getAndIncrement(), interiorPatternRefs, patchProperties))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForPaintJobs);
    }

    private void enhancePatchObjectWithInteriors(List<String> interiorPatternRefs, DatabasePatchProperties patchProperties) {
        // TODO handle int at index 0??
        AtomicInteger interiorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForInteriors = interiorPatternRefs.stream()
                .filter(intRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(intRef))
                .flatMap(intRef -> createChangeObjectAndPropertiesForInterior(intRef, interiorIndex.getAndIncrement(), patchProperties))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForInteriors);
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsAndPropertiesForExterior(PaintJob paintJob, int exteriorRank, List<String> interiorPatternRefs, DatabasePatchProperties patchProperties) {
        if (!patchProperties.getExteriorColorName(exteriorRank).isPresent()) {
            return Stream.empty();
        }

        createPatchPropertiesForPaintJobAtRank(paintJob, exteriorRank, patchProperties);

        DbPatchDto.DbChangeDto entryUpdateChange = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_COLORS)
                .withEntryValues(asList(
                        PlaceholderConstants.getPlaceHolderForVehicleSlotReference(),
                        PlaceholderConstants.getPlaceHolderForExteriorMainColor(exteriorRank),
                        PlaceholderConstants.getPlaceHolderForExteriorNameResource(exteriorRank),
                        PlaceholderConstants.getPlaceHolderForExteriorSecondaryColor(exteriorRank),
                        PlaceholderConstants.getPlaceHolderForExteriorCalipersColor(exteriorRank),
                        "0",
                        "0",
                        interiorPatternRefs.get(0),
                        interiorPatternRefs.get(1),
                        interiorPatternRefs.get(2),
                        interiorPatternRefs.get(3),
                        interiorPatternRefs.get(4),
                        interiorPatternRefs.get(5),
                        interiorPatternRefs.get(6),
                        interiorPatternRefs.get(7),
                        interiorPatternRefs.get(8),
                        interiorPatternRefs.get(9),
                        interiorPatternRefs.get(10),
                        interiorPatternRefs.get(11),
                        interiorPatternRefs.get(12),
                        interiorPatternRefs.get(13),
                        interiorPatternRefs.get(14)
                ))
                .build();

        DbPatchDto.DbChangeDto resourceUpdateChange = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(CAR_COLORS)
                .asReference(PlaceholderConstants.getPlaceHolderForExteriorNameResource(exteriorRank))
                .withValue(PlaceholderConstants.getPlaceHolderForExteriorName(exteriorRank))
                .build();

        return Stream.of(entryUpdateChange, resourceUpdateChange);
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectAndPropertiesForInterior(String intRef, int interiorRank, DatabasePatchProperties patchProperties) {
        if (!patchProperties.getInteriorMainColorId(interiorRank).isPresent()) {
            return Stream.empty();
        }

        createPatchPropertiesForInteriorAtRank(intRef, interiorRank, patchProperties);

        return Stream.of(DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(INTERIOR)
                .asReference(PlaceholderConstants.getPlaceHolderForInteriorReference(interiorRank))
                .withEntryValues(asList(
                        PlaceholderConstants.getPlaceHolderForInteriorReference(interiorRank),
                        DatabaseConstants.REF_DEFAULT_BRAND,
                        DatabaseConstants.RESOURCE_REF_NONE_INTERIOR_NAME,
                        PlaceholderConstants.getPlaceHolderForInteriorMainColor(interiorRank),
                        PlaceholderConstants.getPlaceHolderForInteriorSecondaryColor(interiorRank),
                        PlaceholderConstants.getPlaceHolderForInteriorMaterial(interiorRank),
                        "0"
                ))
                .build());
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsAndPropertiesForRims(VehicleSlot vehicleSlot, RimSlot rimSlot, int rimRank, DatabasePatchProperties patchProperties) {
        if (!patchProperties.getRimName(rimRank).isPresent()) {
            return Stream.empty();
        }

        createPatchPropertiesForRimSetAtRank(vehicleSlot, rimSlot, rimRank, patchProperties);

        DbPatchDto.DbChangeDto associationEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_RIMS)
                .withEntryValues( asList(
                        vehicleSlot.getRef(),
                        patchProperties.getRimSlotReference(rimRank)
                            .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Rim slot reference not found in properties: for set " + rimRank))
                ))
                .build();

        DbPatchDto.DbChangeDto slotEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRimSlotReference(rimRank))
                .withEntryValues( asList(
                        PlaceholderConstants.getPlaceHolderForRimSlotReference(rimRank),
                        PlaceholderConstants.getPlaceHolderForRimBrand(rimRank),
                        DatabaseConstants.RESOURCE_REF_NO_RIM_NAME,
                        PlaceholderConstants.getPlaceHolderForRimNameResource(rimRank),
                        PlaceholderConstants.getPlaceHolderForFrontRimWidth(rimRank),
                        PlaceholderConstants.getPlaceHolderForFrontRimHeight(rimRank),
                        PlaceholderConstants.getPlaceHolderForFrontRimDiameter(rimRank),
                        PlaceholderConstants.getPlaceHolderForRearRimWidth(rimRank),
                        PlaceholderConstants.getPlaceHolderForRearRimHeight(rimRank),
                        PlaceholderConstants.getPlaceHolderForRearRimDiameter(rimRank),
                        "0",
                        "0",
                        PlaceholderConstants.getPlaceHolderForRimBrand(rimRank),
                        PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(rimRank),
                        PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(rimRank),
                        "0"
                ))
                .build();

        DbPatchDto.DbChangeDto slotNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRimNameResource(rimRank))
                .withValue(PlaceholderConstants.getPlaceHolderForRimName(rimRank))
                .build();

        DbPatchDto.DbChangeDto slotFrontFileNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(rimRank))
                .withValue(PlaceholderConstants.getPlaceHolderForFrontRimFileName(rimRank))
                .build();

        DbPatchDto.DbChangeDto slotRearNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(rimRank))
                .withValue(PlaceholderConstants.getPlaceHolderForRearRimFileName(rimRank))
                .build();

        return Stream.of(associationEntryUpdate, slotEntryUpdate, slotNameResourceUpdate, slotFrontFileNameResourceUpdate, slotRearNameResourceUpdate);
    }

    private void createPatchPropertiesForPaintJobAtRank(PaintJob paintJob, int paintJobRank, DatabasePatchProperties patchProperties) {
        String nameRef = paintJob.getName().getRef();

        patchProperties.setExteriorColorNameResourceIfNotExists(nameRef, paintJobRank);
    }

    private void createPatchPropertiesForInteriorAtRank(String intRef, int interiorRank, DatabasePatchProperties patchProperties) {
        patchProperties.setInteriorReferenceIfNotExists(intRef, interiorRank);
    }

    private void createPatchPropertiesForRimSetAtRank(VehicleSlot vehicleSlot, RimSlot rimSlot, int rank, DatabasePatchProperties patchProperties) {
        String selectedRimReference = rimSlot.getRef();
        String selectedResourceRimBrandReference = rimSlot.getParentDirectoryName().getRef();
        String selectedFrontRimBank = VehicleSlotsHelper.getRimBankFileName(vehicleSlot, FRONT_RIMS_3D, rimSlot.getRank(), false);
        String selectedResourceFrontRimBankName = rimSlot.getFrontRimInfo().getFileName().getRef();
        String selectedRearRimBank = VehicleSlotsHelper.getRimBankFileName(vehicleSlot, REAR_RIMS_3D, rimSlot.getRank(), false);
        String selectedResourceRearRimBankName = rimSlot.getRearRimInfo().getFileName().getRef();

        List<String> values = asList(selectedRimReference, selectedFrontRimBank, selectedRearRimBank, selectedResourceFrontRimBankName, selectedResourceRearRimBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, vehicleSlot.getRef()));
        }

        patchProperties.setRimsSlotReferenceIfNotExists(selectedRimReference, rank);
        patchProperties.setResourceRimsBrandIfNotExists(selectedResourceRimBrandReference, rank);
        patchProperties.setFrontRimBankNameIfNotExists(selectedFrontRimBank, rank);
        patchProperties.setResourceFrontRimBankIfNotExists(selectedResourceFrontRimBankName, rank);
        patchProperties.setRearRimBankNameIfNotExists(selectedRearRimBank, rank);
        patchProperties.setResourceRearRimBankIfNotExists(selectedResourceRearRimBankName, rank);
    }

    static List<String> getEffectiveInteriorReferences(List<String> interiorRefs, DatabasePatchProperties patchProperties) {
        AtomicInteger interiorRank = new AtomicInteger(1);
        List<String> interiorPatternRefs = interiorRefs.stream()
                .map(ref -> {
                    int rank = interiorRank.getAndIncrement();
                    return patchProperties.getInteriorMainColorId(rank).isPresent() ?
                            PlaceholderConstants.getPlaceHolderForInteriorReference(rank) : DatabaseConstants.REF_NO_INTERIOR;
                })
                .collect(toList());

        try (IntStream stream = range(interiorPatternRefs.size(), DatabaseConstants.COUNT_INTERIORS)) {
            stream.forEach(i -> interiorPatternRefs.add(DatabaseConstants.REF_NO_INTERIOR));
        }
        
        return interiorPatternRefs;
    }


    // For testing only
    void overrideVehicleSlotsHelper(VehicleSlotsHelper vehicleSlotsHelper) {
        this.vehicleSlotsHelper = vehicleSlotsHelper;
    }

    void overrideBrandHelper(BrandHelper brandHelper) {
        this.brandHelper = brandHelper;
    }
}
