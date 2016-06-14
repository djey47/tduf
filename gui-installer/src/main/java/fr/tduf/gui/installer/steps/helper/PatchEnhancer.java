package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.RIMS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

/**
 * Utility class to add instructions and properties over an existing install patch template
 */
public class PatchEnhancer {
    private static final String THIS_CLASS_NAME = PatchEnhancer.class.getSimpleName();

    private final DatabaseContext databaseContext;

    /**
     *
     * @param databaseContext
     */
    public PatchEnhancer(DatabaseContext databaseContext) {
        this.databaseContext = requireNonNull(databaseContext, "Database context is required");
    }

    /**
     *
     */
    public void enhancePatchObject() {
        PatchProperties patchProperties = databaseContext.getPatchProperties() ;

        if(patchProperties.getDealerReference().isPresent()
                && patchProperties.getDealerSlot().isPresent()) {
            enhancePatchObjectWithLocationChange();
        }

        enhancePatchObjectWithInstallFlag();

        databaseContext.getUserSelection().getVehicleSlot()
                .ifPresent(vehicleSlot -> {
                    enhancePatchObjectWithPaintJobs(vehicleSlot);
                    enhancePatchObjectWithRims(vehicleSlot);
                });
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

        AtomicInteger rimIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForRims = vehicleSlot.getRims().stream()
                .flatMap(rimSlot -> createChangeObjectsForRims(vehicleSlot, rimSlot, rimIndex))
                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForRims);
    }

    private void enhancePatchObjectWithLocationChange() {
        Log.info(THIS_CLASS_NAME, "->Adding dealer slot change to initial patch");

        PatchProperties patchProperties = databaseContext.getPatchProperties();
        int effectiveFieldRank = patchProperties.getDealerSlot().get() + 3;

        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_SHOPS)
                .withType(UPDATE)
                .asReference(patchProperties.getDealerReference().get())
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(effectiveFieldRank, patchProperties.getVehicleSlotReference().get())))
                .build();

        databaseContext.getPatchObject().getChanges().add(changeObject);
    }

    private void enhancePatchObjectWithInstallFlag() {
        Log.info(THIS_CLASS_NAME, "->Adding install flag change to initial patch");

        final String secuOneRawValue = SecurityOptions.INSTALLED.setScale(0, RoundingMode.UNNECESSARY).toString();
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(UPDATE)
                .asReference(databaseContext.getPatchProperties().getVehicleSlotReference().get())
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_SECU1, secuOneRawValue)))
                .build();

        databaseContext.getPatchObject().getChanges().add(changeObject);
    }

    private void enhancePatchObjectWithExteriors(VehicleSlot vehicleSlot) {
        AtomicInteger exteriorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForPaintJobs = vehicleSlot.getPaintJobs().stream()

                .flatMap(paintJob -> createChangeObjectsForExterior(vehicleSlot, paintJob, exteriorIndex))

                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForPaintJobs);
    }

    private void enhancePatchObjectWithInteriors(List<String> interiorPatternRefs) {
        AtomicInteger interiorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForInteriors = interiorPatternRefs.stream()

                .filter(intRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(intRef))

                .map(intRef -> createChangeObjectForInterior(intRef, interiorIndex))

                .collect(toList());

        databaseContext.getPatchObject().getChanges().addAll(changeObjectsForInteriors);
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsForExterior(VehicleSlot vehicleSlot, PaintJob paintJob, AtomicInteger exteriorIndex) {
        if (!databaseContext.getPatchProperties().getExteriorMainColorId(paintJob.getRank()).isPresent()) {
            return Stream.empty();
        }

        List<String> interiorRefs = new ArrayList<>(paintJob.getInteriorPatternRefs());
        try (IntStream stream = rangeClosed(interiorRefs.size(), 15)) {
            stream.forEach(index -> interiorRefs.add(DatabaseConstants.REF_NO_INTERIOR));
        }

        int index = exteriorIndex.getAndIncrement();
        DbPatchDto.DbChangeDto entryUpdateChange = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_COLORS)
                .withEntryValues(asList(
                        vehicleSlot.getRef(),
                        PlaceholderConstants.getPlaceHolderForExteriorMainColor(index),
                        PlaceholderConstants.getPlaceHolderForExteriorNameResource(index),
                        PlaceholderConstants.getPlaceHolderForExteriorSecondaryColor(index),
                        PlaceholderConstants.getPlaceHolderForExteriorCalipersColor(index),
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

    private DbPatchDto.DbChangeDto createChangeObjectForInterior(String intRef, AtomicInteger interiorIndex) {
        int index = interiorIndex.getAndIncrement();
        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(INTERIOR)
                .asReference(intRef)
                .withEntryValues(asList(
                        intRef,
                        DatabaseConstants.REF_DEFAULT_BRAND,
                        DatabaseConstants.RESOURCE_REF_NONE_INTERIOR_NAME,
                        PlaceholderConstants.getPlaceHolderForInteriorMainColor(index),
                        PlaceholderConstants.getPlaceHolderForInteriorSecondaryColor(index),
                        PlaceholderConstants.getPlaceHolderForInteriorMaterial(index),
                        "0"
                ))
                .build();
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsForRims(VehicleSlot vehicleSlot, RimSlot rimSlot, AtomicInteger rimIndex) {
        int index = rimIndex.getAndIncrement();
        final PatchProperties patchProperties = databaseContext.getPatchProperties();
        if (!patchProperties.getRimSlotReference(index).isPresent()) {
            return Stream.empty();
        }

        DbPatchDto.DbChangeDto associationEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_RIMS)
                .withEntryValues( asList(
                        vehicleSlot.getRef(),
                        patchProperties.getRimSlotReference(index).get()
                ))
                .build();

        DbPatchDto.DbChangeDto slotEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(RIMS)
                .asReference(rimSlot.getRef())
                .withEntryValues( asList(
                        rimSlot.getRef(),
                        PlaceholderConstants.getPlaceHolderForRimBrand(index),
                        DatabaseConstants.RESOURCE_REF_NO_RIM_NAME,
                        PlaceholderConstants.getPlaceHolderForRimNameResource(index),
                        PlaceholderConstants.getPlaceHolderForFrontRimWidth(index),
                        PlaceholderConstants.getPlaceHolderForFrontRimHeight(index),
                        PlaceholderConstants.getPlaceHolderForFrontRimDiameter(index),
                        PlaceholderConstants.getPlaceHolderForRearRimWidth(index),
                        PlaceholderConstants.getPlaceHolderForRearRimHeight(index),
                        PlaceholderConstants.getPlaceHolderForRearRimDiameter(index),
                        "0",
                        "0",
                        PlaceholderConstants.getPlaceHolderForRimBrand(index),
                        PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(index),
                        PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(index),
                        "0"
                ))
                .build();

        DbPatchDto.DbChangeDto slotNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRimNameResource(index))
                .withValue(PlaceholderConstants.getPlaceHolderForRimName(index))
                .build();

        DbPatchDto.DbChangeDto slotFrontFileNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForFrontRimFileNameResource(index))
                .withValue(PlaceholderConstants.getPlaceHolderForFrontRimFileName(index))
                .build();

        DbPatchDto.DbChangeDto slotRearNameResourceUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(RIMS)
                .asReference(PlaceholderConstants.getPlaceHolderForRearRimFileNameResource(index))
                .withValue(PlaceholderConstants.getPlaceHolderForRearRimFileName(index))
                .build();

        return Stream.of(associationEntryUpdate, slotEntryUpdate, slotNameResourceUpdate, slotFrontFileNameResourceUpdate, slotRearNameResourceUpdate);
    }

}
