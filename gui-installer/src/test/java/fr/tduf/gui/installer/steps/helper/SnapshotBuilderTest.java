package fr.tduf.gui.installer.steps.helper;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libtesting.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapshotBuilderTest {
    private static final String SLOTREF = "606298799";

    @Test
    public void take_shouldWritePatchObject() throws Exception {
        // GIVEN
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOTREF);
        DatabaseContext databaseContext = InstallerTestsHelper.createJsonDatabase();
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        final SnapshotBuilder snapshotBuilder = new SnapshotBuilder(databaseContext);
        String backupDirectory = FilesHelper.createTempDirectoryForInstaller();

        // WHEN
        snapshotBuilder.take(backupDirectory);

        // THEN
        // TODO assert contents
        assertThat(new File(backupDirectory, "before.mini.json")).exists();
    }
}