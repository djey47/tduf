package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
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
    public void take(String backupDirectory) throws IOException, ReflectiveOperationException, URISyntaxException {
        final PatchProperties effectiveProperties = requireNonNull(databaseContext.getPatchProperties(), "Patch properties are required.");

        PatchGenerator generator = AbstractDatabaseHolder.prepare(PatchGenerator.class, databaseContext.getTopicObjects());

        String vehicleSlotRef = effectiveProperties.getVehicleSlotReference()
                .orElseThrow(() -> new IllegalStateException("Vehicle slot reference not found in properties"));

        final DbPatchDto cleanPatchTemplate = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/gui-installer/templates/clean-slot.mini.json");
        List<DbPatchDto.DbChangeDto> cleaningOps = cleanPatchTemplate.getChanges();

        List<DbPatchDto.DbChangeDto> rawSnapshotOps = generator.makePatch(
                CAR_PHYSICS_DATA,
                ItemRange.fromCollection(Collections.singletonList(vehicleSlotRef)),
                ItemRange.ALL)
                .getChanges();

        List<DbPatchDto.DbChangeDto> snapshotOps = rawSnapshotOps.stream()
                .filter(op -> TOPICS_FOR_SNAPSHOT.contains(op.getTopic()))
                .collect(toList());

        // TODO dealer location...
        List<DbPatchDto.DbChangeDto> additionalOps = new ArrayList<>();

        DbPatchDto snapshotPatch = DbPatchDto.builder()
                .withComment("Vehicle raw snapshot for slot: " + vehicleSlotRef)
                .addChanges(cleaningOps)
                .addChanges(snapshotOps)
                .addChanges(additionalOps)
                .build();

        writeSnapshotPatch(Paths.get(backupDirectory), snapshotPatch);
    }

    private void writeSnapshotPatch(Path backupPath, DbPatchDto patchObject) throws IOException {
        String targetPatchFile = backupPath.resolve(InstallerConstants.FILE_NAME_SNAPSHOT_PATCH).toString();

        Log.info(THIS_CLASS_NAME, "->Writing snapshot patch to " + targetPatchFile + "...");

        FilesHelper.writeJsonObjectToFile(patchObject, targetPatchFile);
    }
}
