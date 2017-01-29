package fr.tduf.gui.installer.steps.helper;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.DELETE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotBuilderTest {
    private static final String SLOTREF = "606298799";
    private static final String DEALERREF = "550413704";
    private static final int DEALERSLOT = 2;

    @Test
    public void take_shouldWritePatchObject() throws Exception {
        // GIVEN
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOTREF);
        patchProperties.setDealerReferenceIfNotExists(DEALERREF);
        patchProperties.setDealerSlotIfNotExists(DEALERSLOT);
        DatabaseContext databaseContext = InstallerTestsHelper.createDatabaseContext();
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        final SnapshotBuilder snapshotBuilder = new SnapshotBuilder(databaseContext);
        String backupDirectory = FilesHelper.createTempDirectoryForInstaller();


        // WHEN
        snapshotBuilder.take(backupDirectory);


        // THEN
        final File actualSnapshot = new File(backupDirectory, "SNAPSHOT.mini.json");
        assertThat(actualSnapshot).exists();

        final DbPatchDto actualSnapshotPatch = new ObjectMapper().readValue(actualSnapshot, DbPatchDto.class);
        final List<DbPatchDto.DbChangeDto> actualChanges = actualSnapshotPatch.getChanges();

        assertThat(actualChanges).hasSize(88); // Clean (3) + Snapshot (84) + Additional (1)
        assertThat(actualChanges.stream()
                .filter(change -> DELETE == change.getType())
                .count())
                .isEqualTo(3);
        assertChangesForTopicHaveCount(actualChanges, AFTER_MARKET_PACKS, 0);
        assertChangesForTopicHaveCount(actualChanges, BRANDS, 0);
        assertChangesForTopicHaveCount(actualChanges, CAR_COLORS, 14);
        assertChangesForTopicHaveCount(actualChanges, CAR_PACKS, 2);
        assertChangesForTopicHaveCount(actualChanges, CAR_PHYSICS_DATA, 61);
        assertChangesForTopicHaveCount(actualChanges, CAR_RIMS, 2);
        assertChangesForTopicHaveCount(actualChanges, CAR_SHOPS, 1);
        assertChangesForTopicHaveCount(actualChanges, INTERIOR, 3);
        assertChangesForTopicHaveCount(actualChanges, RIMS, 5);
    }

    private static void assertChangesForTopicHaveCount(List<DbPatchDto.DbChangeDto> actualChanges, DbDto.Topic topic, int expectedCount) {
        assertThat(actualChanges.stream()
                .filter(change -> topic == change.getTopic())
                .count())
                .isEqualTo(expectedCount);
    }
}