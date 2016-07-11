package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static org.assertj.core.api.StrictAssertions.assertThat;


public class RestoreSnapshotStepTest {
    private static final Class<RestoreSnapshotStepTest> THIS_CLASS = RestoreSnapshotStepTest.class;

    private static final String RESREF = "10101010";

    @Test
    public void perform_shouldApplySnapshot_withProperties() throws Exception {
        // GIVEN
        InstallerConfiguration installerConfiguration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory("")
                .build();
        DbPatchDto snapshotObject = new ObjectMapper().readValue(THIS_CLASS.getResource("/snapshots/SNAPSHOT-simple.mini.json"), DbPatchDto.class);
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("RESREF", RESREF);
        final DatabaseContext databaseContext = InstallerTestsHelper.createDatabaseContext();
        databaseContext.setPatch(snapshotObject, patchProperties);
        final GenericStep genericStep = GenericStep.starterStep(installerConfiguration, databaseContext)
                .nextStep(GenericStep.StepType.RESTORE_SNAPSHOT);

        // WHEN
        genericStep.perform();

        // THEN
        final Optional<ResourceEntryDto> potentialResourceEntry = databaseContext.getMiner().getResourceEntryFromTopicAndReference(CAR_PHYSICS_DATA, RESREF);
        assertThat(potentialResourceEntry).isPresent();
        assertThat(potentialResourceEntry.get().pickValue()).contains("NEW RESOURCE");
    }
}