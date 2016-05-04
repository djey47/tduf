package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.io.Files.getFileExtension;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Objects.requireNonNull;

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

        final List<DbDto> topicObjects = getDatabaseContext().getTopicObjects();
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, topicObjects);

        Log.info(THIS_CLASS_NAME, "->Applying TDUF mini patch...");
        patcher.applyWithProperties(getDatabaseContext().getPatchObject(), patchProperties);

        String slotRef = patchProperties.getVehicleSlotReference().get();
        applyPerformancePackage(topicObjects, slotRef);
    }

    private void enhancePatchObjectWithLocationChange() {
        Log.info(THIS_CLASS_NAME, "->Adding dealer slot change to initial patch");

        PatchProperties patchProperties = getDatabaseContext().getPatchProperties();
        int effectiveFieldRank = patchProperties.getDealerSlot().get() + 3;

        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_SHOPS)
                .withType(UPDATE)
                .asReference(patchProperties.getDealerReference().get())
                .withPartialEntryValues(Collections.singletonList(DbFieldValueDto.fromCouple(effectiveFieldRank, patchProperties.getVehicleSlotReference().get())))
                .build();

        getDatabaseContext().getPatchObject().getChanges().add(changeObject);
    }

    private void applyPerformancePackage(List<DbDto> topicObjects, String slotRef) throws ReflectiveOperationException, IOException {
        Path assetPath = Paths.get(getInstallerConfiguration().getAssetsDirectory(), InstallerConstants.DIRECTORY_DATABASE);
        final Optional<Path> potentialPPFilePath = Files.walk(assetPath, 1)

                .filter(Files::isRegularFile)

                .filter(path -> TdupeGateway.EXTENSION_PERFORMANCE_PACK.equalsIgnoreCase(getFileExtension(path.toString())))

                .findFirst();

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
