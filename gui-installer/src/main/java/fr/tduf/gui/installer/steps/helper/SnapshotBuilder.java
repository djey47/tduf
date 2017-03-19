package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class to take database snapshot before applying mini patch.
 */
public class SnapshotBuilder {
    private static final String THIS_CLASS_NAME = SnapshotBuilder.class.getSimpleName();

    private static final Set<DbDto.Topic> TOPICS_FOR_SNAPSHOT = new HashSet<>(asList(
            CAR_COLORS, CAR_PACKS, CAR_PHYSICS_DATA, CAR_RIMS, INTERIOR, RIMS
    ));

    private final DatabaseContext databaseContext;

    /**
     * Unique way to get an instance.
     * @param databaseContext   : database information
     */
    public SnapshotBuilder(DatabaseContext databaseContext) {
        this.databaseContext = databaseContext;
    }

    /**
     * Takes snapshot of entries which will be modified
     * @param backupDirectory   : directory where to create snapshot file. Must exist.
     */
    public void take(String backupDirectory) throws IOException {
        final DatabasePatchProperties effectiveProperties = requireNonNull(databaseContext.getPatchProperties(), "Patch properties are required.");
        String vehicleSlotRef = effectiveProperties.getVehicleSlotReference()
                .orElseThrow(() -> new IllegalStateException("Vehicle slot reference not found in properties"));

        List<DbPatchDto.DbChangeDto> cleaningOps = generateCleaningOperationsTemplates();
        List<DbPatchDto.DbChangeDto> snapshotOps = generateSnapshotOperationsForVehicleSlot(vehicleSlotRef);
        List<DbPatchDto.DbChangeDto> additionalOps = generateAdditionalOperationsFromProperties(effectiveProperties);

        DbPatchDto snapshotPatch = DbPatchDto.builder()
                .withComment("Vehicle raw snapshot for slot: " + vehicleSlotRef)
                .addChanges(cleaningOps)
                .addChanges(snapshotOps)
                .addChanges(additionalOps)
                .build();

        writeSnapshotPatch(Paths.get(backupDirectory), snapshotPatch);
    }

    private List<DbPatchDto.DbChangeDto> generateAdditionalOperationsFromProperties(DatabasePatchProperties effectiveProperties) {
        List<DbPatchDto.DbChangeDto> additionalOps = new ArrayList<>();
        addDealerLocationOperationsIfNecessary(effectiveProperties, additionalOps);
        return additionalOps;
    }

    private List<DbPatchDto.DbChangeDto> generateCleaningOperationsTemplates() throws IOException {
        try {
            return FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, InstallerConstants.RESOURCE_NAME_CLEAN_PATCH).getChanges();
        } catch (URISyntaxException use) {
            throw new IOException("Unable to generate cleaning operations", use);
        }
    }

    private List<DbPatchDto.DbChangeDto> generateSnapshotOperationsForVehicleSlot(String vehicleSlotRef) throws IOException {
        try {
            PatchGenerator generator = AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseContext.getTopicObjects());
            List<DbPatchDto.DbChangeDto> rawSnapshotOps = generator.makePatch(
                    CAR_PHYSICS_DATA,
                    ItemRange.fromSingleValue(vehicleSlotRef),
                    ItemRange.ALL)
                    .getChanges();

            return rawSnapshotOps.stream()
                    .filter(op -> TOPICS_FOR_SNAPSHOT.contains(op.getTopic()))
                    .collect(toList());
        } catch (ReflectiveOperationException roe) {
            throw new IOException("Unable to generate snapshot operations", roe);
        }
    }

    private void addDealerLocationOperationsIfNecessary(DatabasePatchProperties effectiveProperties, List<DbPatchDto.DbChangeDto> additionalOps) {
        effectiveProperties.getDealerReference()
                .ifPresent(dealerRef -> {
                    final int slotRank = DatabaseConstants.FIELD_RANK_DEALER_SLOT_1
                            + effectiveProperties.getDealerSlot()
                            .orElseThrow(() -> new IllegalStateException("No dealer slot rank provided."))
                            - 1;
                    final String currentVehicleRef = databaseContext.getMiner().getContentEntryFromTopicWithReference(dealerRef, CAR_SHOPS)
                            .flatMap(entry -> entry.getItemAtRank(slotRank))
                            .map(ContentItemDto::getRawValue)
                            .orElseThrow(() -> new IllegalStateException("No dealer at ref: " + dealerRef));
                    DbFieldValueDto partialValue = DbFieldValueDto.fromCouple(slotRank, currentVehicleRef);
                    final DbPatchDto.DbChangeDto dbChangeDto = DbPatchDto.DbChangeDto.builder()
                            .forTopic(CAR_SHOPS)
                            .withType(UPDATE)
                            .asReference(dealerRef)
                            .withPartialEntryValues(singletonList(partialValue))
                            .build();
                    additionalOps.add(dbChangeDto);
                });
    }

    private void writeSnapshotPatch(Path backupPath, DbPatchDto patchObject) throws IOException {
        String targetPatchFile = backupPath.resolve(InstallerConstants.FILE_NAME_SNAPSHOT_PATCH).toString();

        Log.info(THIS_CLASS_NAME, "->Writing snapshot patch to " + targetPatchFile + "...");

        FilesHelper.writeJsonObjectToFile(patchObject, targetPatchFile);
    }
}
