package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static fr.tduf.gui.installer.steps.GenericStep.StepType.UPDATE_DATABASE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

/**
 * Utility class to add instructions and properties over an existing install patch template
 */
public class PatchEnhancer {
    private static final String THIS_CLASS_NAME = PatchEnhancer.class.getSimpleName();

    private final DatabaseContext databaseContext;

    private VehicleSlotsHelper vehicleSlotsHelper;

    /**
     * Unique way to get an instance.
     * @param databaseContext   : database information
     */
    public PatchEnhancer(DatabaseContext databaseContext) {
        this.databaseContext = requireNonNull(databaseContext, "Database context is required");
        this.vehicleSlotsHelper = VehicleSlotsHelper.load(databaseContext.getMiner());
    }

    /**
     * Put additional instructions to patch template.
     */
    public void enhancePatchObject() {
        PatchProperties patchProperties = requireNonNull(databaseContext.getPatchProperties(), "Patch properties are required.");

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
                    enhancePatchObjectWithPaintJobs(vehicleSlot);
                    enhancePatchObjectWithRims(vehicleSlot);
                });
    }

    void enhancePatchProperties(PatchProperties patchProperties) {
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
    }

    void enhancePatchObjectWithPaintJobs(VehicleSlot vehicleSlot) {
        Log.info(THIS_CLASS_NAME, "->Adding paint jobs changes to initial patch");

        enhancePatchObjectWithExteriors(vehicleSlot);

        if (!vehicleSlot.getPaintJobs().isEmpty()) {
            enhancePatchObjectWithInteriors(vehicleSlot.getPaintJobs().get(0).getInteriorPatternRefs());
        }
    }

    // TODO see if bikes support more than 1 rim set
    void enhancePatchObjectWithRims(VehicleSlot vehicleSlot) {
        Log.info(THIS_CLASS_NAME, "->Adding rim changes to initial patch");

        AtomicInteger rimIndex = new AtomicInteger(0);
        List<DbPatchDto.DbChangeDto> changeObjectsForRims = vehicleSlot.getAllRimsSorted().stream()
                .flatMap(rimSlot -> createChangeObjectsForRims(vehicleSlot, rimSlot, rimIndex.getAndIncrement()))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForRims);
    }

    void enhancePatchObjectWithLocationChange(String vehicleSlotReference) {
        Log.info(THIS_CLASS_NAME, "->Adding dealer slot change to initial patch");

        PatchProperties patchProperties = databaseContext.getPatchProperties();
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

    private void createPatchPropertiesForVehicleSlot(VehicleSlot effectiveSlot, PatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with slot information");

        String slotReference = effectiveSlot.getRef();
        int selectedCarIdentifier = effectiveSlot.getCarIdentifier();
        if (VehicleSlotsHelper.DEFAULT_VEHICLE_ID == selectedCarIdentifier) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        String selectedBankName = VehicleSlotsHelper.getBankFileName(effectiveSlot, EXTERIOR_MODEL, false);
        String selectedResourceBankName = effectiveSlot.getFileName().getRef();
        List<String> values = asList(selectedBankName, selectedResourceBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, slotReference));
        }

        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(Integer.toString(selectedCarIdentifier));
        patchProperties.setBankNameIfNotExists(selectedBankName);
        patchProperties.setResourceBankNameIfNotExists(selectedResourceBankName);

        createPatchPropertiesForRims(effectiveSlot, patchProperties);

        createPatchPropertiesForPaintJobs(effectiveSlot, patchProperties);
    }

    private void createPatchPropertiesForPaintJobs(VehicleSlot vehicleSlot, PatchProperties patchProperties) {
        AtomicInteger paintJobIndex = new AtomicInteger(1);
        vehicleSlot.getPaintJobs()
                .forEach(paintJob -> {
                    String nameRef = paintJob.getName().getRef();
                    patchProperties.setExteriorColorNameResourceIfNotExists(nameRef, paintJobIndex.getAndIncrement());
                });

        searchFirstInteriorPatternReference(vehicleSlot).ifPresent(ref -> patchProperties.setInteriorReferenceIfNotExists(ref, 1));
    }

    private void createPatchPropertiesForRims(VehicleSlot vehicleSlot, PatchProperties patchProperties) {
        String selectedRimReference = vehicleSlot.getDefaultRims().getRef();
        String selectedResourceRimBrandReference = vehicleSlot.getDefaultRims().getParentDirectoryName().getRef();
        String selectedFrontRimBank = VehicleSlotsHelper.getBankFileName(vehicleSlot, FRONT_RIM, false);
        String selectedResourceFrontRimBankName = vehicleSlot.getDefaultRims().getFrontRimInfo().getFileName().getRef();
        String selectedRearRimBank = VehicleSlotsHelper.getBankFileName(vehicleSlot, REAR_RIM, false);
        String selectedResourceRearRimBankName = vehicleSlot.getDefaultRims().getRearRimInfo().getFileName().getRef();

        List<String> values = asList(selectedRimReference, selectedFrontRimBank, selectedRearRimBank, selectedResourceFrontRimBankName, selectedResourceRearRimBankName);
        if (values.contains(DisplayConstants.ITEM_UNAVAILABLE)) {
            throw new IllegalArgumentException(String.format(DisplayConstants.MESSAGE_FMT_INVALID_SLOT_INFO, vehicleSlot.getRef()));
        }

        patchProperties.setRimsSlotReferenceIfNotExists(selectedRimReference, 1);
        patchProperties.setResourceRimsBrandIfNotExists(selectedResourceRimBrandReference, 1);
        patchProperties.setFrontRimBankNameIfNotExists(selectedFrontRimBank, 1);
        patchProperties.setResourceFrontRimBankIfNotExists(selectedResourceFrontRimBankName, 1);
        patchProperties.setRearRimBankNameIfNotExists(selectedRearRimBank, 1);
        patchProperties.setResourceRearRimBankIfNotExists(selectedResourceRearRimBankName, 1);
    }

    private void createPatchPropertiesForDealerSlot(UserSelection userSelection, PatchProperties patchProperties) {
        Log.info(THIS_CLASS_NAME, "->Resolving missing properties with dealer slot information");

        patchProperties.setDealerReferenceIfNotExists(userSelection.getDealer()
                .map(Dealer::getRef)
                .orElseThrow(() -> new IllegalArgumentException("No dealer reference was selected!")));
        patchProperties.setDealerSlotIfNotExists(userSelection.getDealerSlotRank());
    }

    private Optional<String> searchFirstInteriorPatternReference(VehicleSlot vehicleSlot) {
        List<PaintJob> paintJobs = vehicleSlot.getPaintJobs();
        if (paintJobs.isEmpty()) {
            return empty();
        }

        return paintJobs.get(0).getInteriorPatternRefs().stream()
                .findFirst();
    }

    private void enhancePatchObjectWithExteriors(VehicleSlot vehicleSlot) {
        AtomicInteger exteriorIndex = new AtomicInteger(0);
        List<DbPatchDto.DbChangeDto> changeObjectsForPaintJobs = vehicleSlot.getPaintJobs().stream()
                .flatMap(paintJob -> createChangeObjectsForExterior(vehicleSlot, paintJob, exteriorIndex.getAndIncrement()))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForPaintJobs);
    }

    private void enhancePatchObjectWithInteriors(List<String> interiorPatternRefs) {
        AtomicInteger interiorIndex = new AtomicInteger(0);
        List<DbPatchDto.DbChangeDto> changeObjectsForInteriors = interiorPatternRefs.stream()
                .filter(intRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(intRef))
                .map(intRef -> createChangeObjectForInterior(intRef, interiorIndex.getAndIncrement()))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForInteriors);
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsForExterior(VehicleSlot vehicleSlot, PaintJob paintJob, int exteriorIndex) {
        if (!databaseContext.getPatchProperties().getExteriorMainColorId(paintJob.getRank() - 1).isPresent()) {
            return Stream.empty();
        }

        List<String> interiorRefs = new ArrayList<>(paintJob.getInteriorPatternRefs());
        try (IntStream stream = rangeClosed(interiorRefs.size(), DatabaseConstants.COUNT_INTERIORS)) {
            stream.forEach(i -> interiorRefs.add(DatabaseConstants.REF_NO_INTERIOR));
        }

        DbPatchDto.DbChangeDto entryUpdateChange = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_COLORS)
                .withEntryValues(asList(
                        vehicleSlot.getRef(),
                        PlaceholderConstants.getPlaceHolderForExteriorMainColor(exteriorIndex),
                        PlaceholderConstants.getPlaceHolderForExteriorNameResource(exteriorIndex),
                        PlaceholderConstants.getPlaceHolderForExteriorSecondaryColor(exteriorIndex),
                        PlaceholderConstants.getPlaceHolderForExteriorCalipersColor(exteriorIndex),
                        "0",
                        "0",
                        interiorRefs.get(0),
                        interiorRefs.get(1),
                        interiorRefs.get(2),
                        interiorRefs.get(3),
                        interiorRefs.get(4),
                        interiorRefs.get(5),
                        interiorRefs.get(6),
                        interiorRefs.get(7),
                        interiorRefs.get(8),
                        interiorRefs.get(9),
                        interiorRefs.get(10),
                        interiorRefs.get(11),
                        interiorRefs.get(12),
                        interiorRefs.get(13),
                        interiorRefs.get(14)
                ))
                .build();

        DbPatchDto.DbChangeDto resourceUpdateChange = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(CAR_COLORS)
                .asReference(paintJob.getName().getRef())
                .withValue(paintJob.getName().getValue())
                .build();

        return Stream.of(entryUpdateChange, resourceUpdateChange);
    }

    private DbPatchDto.DbChangeDto createChangeObjectForInterior(String intRef, int interiorIndex) {
        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(INTERIOR)
                .asReference(intRef)
                .withEntryValues(asList(
                        intRef,
                        DatabaseConstants.REF_DEFAULT_BRAND,
                        DatabaseConstants.RESOURCE_REF_NONE_INTERIOR_NAME,
                        PlaceholderConstants.getPlaceHolderForInteriorMainColor(interiorIndex),
                        PlaceholderConstants.getPlaceHolderForInteriorSecondaryColor(interiorIndex),
                        PlaceholderConstants.getPlaceHolderForInteriorMaterial(interiorIndex),
                        "0"
                ))
                .build();
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsForRims(VehicleSlot vehicleSlot, RimSlot rimSlot, int rimIndex) {
        final PatchProperties patchProperties = databaseContext.getPatchProperties();
        if (!patchProperties.getRimSlotReference(rimIndex).isPresent()) {
            return Stream.empty();
        }

        DbPatchDto.DbChangeDto associationEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_RIMS)
                .withEntryValues( asList(
                        vehicleSlot.getRef(),
                        patchProperties.getRimSlotReference(rimIndex)
                            .orElseThrow(() -> new InternalStepException(UPDATE_DATABASE, "Rim slot reference not found in properties: for set " + rimIndex))
                ))
                .build();

        DbPatchDto.DbChangeDto slotEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(RIMS)
                .asReference(rimSlot.getRef())
                .withEntryValues( asList(
                        rimSlot.getRef(),
                        PlaceholderConstants.getPlaceHolderForRimBrand(rimIndex),
                        DatabaseConstants.RESOURCE_REF_NO_RIM_NAME,
                        PlaceholderConstants.getPlaceHolderForRimNameResource(rimIndex),
                        PlaceholderConstants.getPlaceHolderForFrontRimWidth(rimIndex),
                        PlaceholderConstants.getPlaceHolderForFrontRimHeight(rimIndex),
                        PlaceholderConstants.getPlaceHolderForFrontRimDiameter(rimIndex),
                        PlaceholderConstants.getPlaceHolderForRearRimWidth(rimIndex),
                        PlaceholderConstants.getPlaceHolderForRearRimHeight(rimIndex),
                        PlaceholderConstants.getPlaceHolderForRearRimDiameter(rimIndex),
                        "0",
                        "0",
                        PlaceholderConstants.getPlaceHolderForRimBrand(rimIndex),
                        PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(rimIndex),
                        PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(rimIndex),
                        "0"
                ))
                .build();

        DbPatchDto.DbChangeDto slotNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRimNameResource(rimIndex))
                .withValue(PlaceholderConstants.getPlaceHolderForRimName(rimIndex))
                .build();

        DbPatchDto.DbChangeDto slotFrontFileNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(rimIndex))
                .withValue(PlaceholderConstants.getPlaceHolderForFrontRimFileName(rimIndex))
                .build();

        DbPatchDto.DbChangeDto slotRearNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(rimIndex))
                .withValue(PlaceholderConstants.getPlaceHolderForRearRimFileName(rimIndex))
                .build();

        return Stream.of(associationEntryUpdate, slotEntryUpdate, slotNameResourceUpdate, slotFrontFileNameResourceUpdate, slotRearNameResourceUpdate);
    }

    // For testing only
    void overrideVehicleSlotsHelper(VehicleSlotsHelper vehicleSlotsHelper) {
        this.vehicleSlotsHelper = vehicleSlotsHelper;
    }
}
