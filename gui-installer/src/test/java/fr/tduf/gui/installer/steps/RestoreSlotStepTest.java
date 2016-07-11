package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.common.helper.DealerHelper;
import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class RestoreSlotStepTest {
    @Mock
    private DatabasePatcher databasePatcher;

    @Captor
    private ArgumentCaptor<PatchProperties> patchPropertiesCaptor;

    private  DatabaseContext context;
    private InstallerConfiguration configuration;

    @Before
    public void setUp() throws IOException {
        context = InstallerTestsHelper.createDatabaseContext();
        configuration = InstallerConfiguration.builder().withTestDriveUnlimitedDirectory("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void perform_whenNoSlotSelected_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        GenericStep step = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);

        // WHEN
        step.perform();

        // THEN: ISE
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_whenNotTDUCPSlotSelected_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("12345678")
                .build();
        context.getUserSelection().selectVehicleSlot(vehicleSlot);
        GenericStep step = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);

        // WHEN
        step.perform();

        // THEN: IAE
    }

    @Test
    public void perform_shouldApplyPatchesWithProperties_andRemoveFromDealer() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        String vehicleSlotRef = "300000000";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(vehicleSlotRef)
                .build();
        context.getUserSelection().selectVehicleSlot(vehicleSlot);
        DatabaseChangeHelper changeHelper = new DatabaseChangeHelper(context.getMiner());
        changeHelper.updateItemRawValueAtIndexAndFieldRank(DbDto.Topic.CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 1, vehicleSlotRef);
        changeHelper.updateItemRawValueAtIndexAndFieldRank(DbDto.Topic.CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 2, vehicleSlotRef);
        changeHelper.updateItemRawValueAtIndexAndFieldRank(DbDto.Topic.CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 3, vehicleSlotRef);
        RestoreSlotStep step = (RestoreSlotStep) GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);
        step.setPatcherComponent(databasePatcher);

        // WHEN
        step.perform();

        // THEN
        verify(databasePatcher).applyWithProperties(any(DbPatchDto.class), patchPropertiesCaptor.capture());
        final PatchProperties actualProperties = patchPropertiesCaptor.getValue();
        assertThat(actualProperties.getVehicleSlotReference()).contains(vehicleSlotRef);
        assertThat(DealerHelper.load(context.getMiner()).searchForVehicleSlot(vehicleSlotRef)).isEmpty();
    }
}
