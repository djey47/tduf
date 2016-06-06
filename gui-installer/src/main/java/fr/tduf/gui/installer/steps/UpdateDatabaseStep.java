package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.SecurityOptions;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

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
import java.util.stream.Stream;

import static com.google.common.io.Files.getFileExtension;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
class UpdateDatabaseStep extends GenericStep {
    private static final String THIS_CLASS_NAME = UpdateDatabaseStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        PatchProperties patchProperties = getDatabaseContext().getPatchProperties();

        if(patchProperties.getDealerReference().isPresent()
                && patchProperties.getDealerSlot().isPresent()) {
            enhancePatchObjectWithLocationChange();
        }

        enhancePatchObjectWithInstallFlag();

        getDatabaseContext().getUserSelection().getVehicleSlot()
                .ifPresent(this::enhancePatchObjectWithPaintJobs);

        final List<DbDto> topicObjects = getDatabaseContext().getTopicObjects();
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, topicObjects);

        Log.info(THIS_CLASS_NAME, "->Applying TDUF mini patch...");
        PatchProperties effectiveProperties = patcher.applyWithProperties(getDatabaseContext().getPatchObject(), patchProperties);
        writeEffectiveProperties(effectiveProperties);

        String slotRef = patchProperties.getVehicleSlotReference().get();
        applyPerformancePackage(topicObjects, slotRef);
    }

    private void writeEffectiveProperties(PatchProperties patchProperties) throws IOException {
        Path backupPath = Paths.get(getInstallerConfiguration().getBackupDirectory());
        String targetPropertyFile = backupPath.resolve(FileConstants.FILE_NAME_EFFECTIVE_PROPERTIES).toString();

        Log.info(THIS_CLASS_NAME, "->Writing effective properties to " + targetPropertyFile + "...");

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);
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

    private void enhancePatchObjectWithPaintJobs(VehicleSlot vehicleSlot) {
        Log.info(THIS_CLASS_NAME, "->Adding paint jobs changes to initial patch");

        enhancePatchObjectWithExteriors(vehicleSlot);

        enhancePatchObjectWithInteriors();
    }

    private void enhancePatchObjectWithExteriors(VehicleSlot vehicleSlot) {
        List<DbPatchDto.DbChangeDto> changeObjectsForPaintJobs = vehicleSlot.getPaintJobs().stream()

                .flatMap(paintJob -> {
                    final Optional<String> exteriorColorId = getDatabaseContext().getPatchProperties().getExteriorMainColorId(paintJob.getRank());
                    if (!exteriorColorId.isPresent()) {
                        return Stream.of((DbPatchDto.DbChangeDto)null);
                    }

                    List<DbPatchDto.DbChangeDto> changes = new ArrayList<>();
                    // Ext
                    changes.add(DbPatchDto.DbChangeDto.builder()
                            .withType(UPDATE)
                            .forTopic(CAR_COLORS)
                            .withEntryValues(asList(
                                    vehicleSlot.getRef(),
                                    paintJob.getMainColor().getRef(),
                                    paintJob.getName().getRef(),
                                    paintJob.getSecondaryColor().getRef(),
                                    paintJob.getCalipersColor().getRef(),
                                    Long.toString(paintJob.getPriceDollar()),
                                    "0",
                                    paintJob.getInteriorPatternRefs().get(0),
                                    paintJob.getInteriorPatternRefs().get(1),
                                    paintJob.getInteriorPatternRefs().get(2),
                                    paintJob.getInteriorPatternRefs().get(3),
                                    paintJob.getInteriorPatternRefs().get(4),
                                    paintJob.getInteriorPatternRefs().get(5),
                                    paintJob.getInteriorPatternRefs().get(6),
                                    paintJob.getInteriorPatternRefs().get(7),
                                    paintJob.getInteriorPatternRefs().get(8),
                                    paintJob.getInteriorPatternRefs().get(9),
                                    paintJob.getInteriorPatternRefs().get(10),
                                    paintJob.getInteriorPatternRefs().get(11),
                                    paintJob.getInteriorPatternRefs().get(12),
                                    paintJob.getInteriorPatternRefs().get(13),
                                    paintJob.getInteriorPatternRefs().get(14)
                            ))
                            .build());
                    // Ext resources
                    changes.add(DbPatchDto.DbChangeDto.builder()
                            .withType(UPDATE_RES)
                            .forTopic(CAR_COLORS)
                            .asReference(paintJob.getName().getRef())
                            .withValue(paintJob.getName().getValue())
                            .build());
                    return changes.stream();
                })

                .filter(changeObject -> changeObject != null)

                .collect(toList());

        getDatabaseContext().getPatchObject().getChanges().addAll(changeObjectsForPaintJobs);
    }

    private void enhancePatchObjectWithInteriors() {
        List<DbPatchDto.DbChangeDto> changeObjectsForInteriors = new ArrayList<>();
        getDatabaseContext().getPatchObject().getChanges().addAll(changeObjectsForInteriors);
    }

    private void applyPerformancePackage(List<DbDto> topicObjects, String slotRef) throws ReflectiveOperationException, IOException {
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

        final TdupeGateway tdupeGateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, topicObjects);

        String ppFilePath = potentialPPFilePath.get().toString();
        Log.info(THIS_CLASS_NAME, "->Applying TDUPE Performance pack: " + ppFilePath + "...");
        tdupeGateway.applyPerformancePackToEntryWithReference(Optional.of(slotRef), ppFilePath);
    }
}
