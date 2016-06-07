package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.PaintJob;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.SecurityOptions;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.io.Files.getFileExtension;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
class UpdateDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = UpdateDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        enhancePatchObject();

        applyMiniPatch();

        applyPerformancePackage(getDatabaseContext().getPatchProperties().getVehicleSlotReference().get());
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

        getDatabaseContext().getPatchObject().getChanges().addAll(changeObjectsForRims);
    }

    private void enhancePatchObject() {
        PatchProperties patchProperties = getDatabaseContext().getPatchProperties() ;

        if(patchProperties.getDealerReference().isPresent()
                && patchProperties.getDealerSlot().isPresent()) {
            enhancePatchObjectWithLocationChange();
        }

        enhancePatchObjectWithInstallFlag();

        getDatabaseContext().getUserSelection().getVehicleSlot()
                .ifPresent(vehicleSlot -> {
                    enhancePatchObjectWithPaintJobs(vehicleSlot);
                    enhancePatchObjectWithRims(vehicleSlot);
                });
    }

    private void enhancePatchObjectWithLocationChange() {
        Log.info(THIS_CLASS_NAME, "->Adding dealer slot change to initial patch");

        PatchProperties patchProperties = getDatabaseContext().getPatchProperties();
        int effectiveFieldRank = patchProperties.getDealerSlot().get() + 3;

        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_SHOPS)
                .withType(UPDATE)
                .asReference(patchProperties.getDealerReference().get())
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(effectiveFieldRank, patchProperties.getVehicleSlotReference().get())))
                .build();

        getDatabaseContext().getPatchObject().getChanges().add(changeObject);
    }

    private void enhancePatchObjectWithInstallFlag() {
        Log.info(THIS_CLASS_NAME, "->Adding install flag change to initial patch");

        final String secuOneRawValue = SecurityOptions.INSTALLED.setScale(0, RoundingMode.UNNECESSARY).toString();
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(UPDATE)
                .asReference(getDatabaseContext().getPatchProperties().getVehicleSlotReference().get())
                .withPartialEntryValues(singletonList(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_SECU1, secuOneRawValue)))
                .build();

        getDatabaseContext().getPatchObject().getChanges().add(changeObject);
    }

    private void enhancePatchObjectWithExteriors(VehicleSlot vehicleSlot) {
        AtomicInteger exteriorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForPaintJobs = vehicleSlot.getPaintJobs().stream()

                .flatMap(paintJob -> createChangeObjectsForExterior(vehicleSlot, paintJob, exteriorIndex))

                .collect(toList());

        getDatabaseContext().getPatchObject().getChanges().addAll(changeObjectsForPaintJobs);
    }

    private void enhancePatchObjectWithInteriors(List<String> interiorPatternRefs) {
        AtomicInteger interiorIndex = new AtomicInteger(1);
        List<DbPatchDto.DbChangeDto> changeObjectsForInteriors = interiorPatternRefs.stream()

                .filter(intRef -> !DatabaseConstants.REF_NO_INTERIOR.equals(intRef))

                .map(intRef -> createChangeObjectForInterior(intRef, interiorIndex))

                .collect(toList());

        getDatabaseContext().getPatchObject().getChanges().addAll(changeObjectsForInteriors);
    }

    private Stream<DbPatchDto.DbChangeDto> createChangeObjectsForExterior(VehicleSlot vehicleSlot, PaintJob paintJob, AtomicInteger exteriorIndex) {
        if (!getDatabaseContext().getPatchProperties().getExteriorMainColorId(paintJob.getRank()).isPresent()) {
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
        if (!getDatabaseContext().getPatchProperties().getRimSlotReference(index).isPresent()) {
            return Stream.empty();
        }

        DbPatchDto.DbChangeDto associationEntryUpdate = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_RIMS)
                .withEntryValues( asList(
                        vehicleSlot.getRef(),
                        getDatabaseContext().getPatchProperties().getRimSlotReference(index).get()
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
                        PlaceholderConstants.getPlaceHolderForFrontRimFileName(index),
                        PlaceholderConstants.getPlaceHolderForRearRimFileName(index),
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

    private void applyMiniPatch() throws ReflectiveOperationException, IOException {
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());

        Log.info(THIS_CLASS_NAME, "->Applying TDUF mini patch...");
        Path backupPath = Paths.get(getInstallerConfiguration().getBackupDirectory());
        writeEffectivePatch(backupPath, getDatabaseContext().getPatchObject());

        PatchProperties effectiveProperties = patcher.applyWithProperties(getDatabaseContext().getPatchObject(), getDatabaseContext().getPatchProperties());

        writeEffectiveProperties(backupPath, effectiveProperties);
    }

    private void applyPerformancePackage(String slotRef) throws ReflectiveOperationException, IOException {
        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);
        Optional<Path> potentialPPFilePath;
        try (Stream<Path> assetStream = Files.walk(assetPath, 1)) {
            potentialPPFilePath = assetStream

                    .filter(Files::isRegularFile)

                    .filter(path -> TdupeGateway.EXTENSION_PERFORMANCE_PACK.equalsIgnoreCase(getFileExtension(path.toString())))

                    .findFirst();
        }

        if (!potentialPPFilePath.isPresent()) {
            Log.info(THIS_CLASS_NAME, "->No TDUPE performance package to apply");
            return;
        }

        final TdupeGateway tdupeGateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, getDatabaseContext().getTopicObjects());

        String ppFilePath = potentialPPFilePath.get().toString();
        Log.info(THIS_CLASS_NAME, "->Applying TDUPE Performance pack: " + ppFilePath + "...");
        tdupeGateway.applyPerformancePackToEntryWithReference(Optional.of(slotRef), ppFilePath);
    }

    private void writeEffectivePatch(Path backupPath, DbPatchDto patchObject) throws IOException {
        String targetPatchFile = backupPath.resolve(FileConstants.FILE_NAME_EFFECTIVE_PATCH).toString();

        Log.info(THIS_CLASS_NAME, "->Writing effective patch to " + targetPatchFile + "...");

        FilesHelper.writeJsonObjectToFile(patchObject, targetPatchFile);
    }

    private void writeEffectiveProperties(Path backupPath, PatchProperties patchProperties) throws IOException {
        String targetPropertyFile = backupPath.resolve(FileConstants.FILE_NAME_EFFECTIVE_PROPERTIES).toString();

        Log.info(THIS_CLASS_NAME, "->Writing effective properties to " + targetPropertyFile + "...");

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);
    }
}
