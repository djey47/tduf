package fr.tduf.gui.installer.steps;

import fr.tduf.gui.installer.common.helper.InstallerTestsHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libunlimited.common.game.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseChangeHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;


class RestoreSlotStepTest {
    @Mock
    private DatabasePatcher databasePatcher;

    @Captor
    private ArgumentCaptor<DbPatchDto> patchCaptor;

    @Captor
    private ArgumentCaptor<DatabasePatchProperties> patchPropertiesCaptor;

    private  DatabaseContext context;
    private InstallerConfiguration configuration;

    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);
        
        context = InstallerTestsHelper.createDatabaseContext();
        configuration = InstallerConfiguration.builder().withTestDriveUnlimitedDirectory("").build();
    }

    @Test
    void perform_whenNoSlotSelected_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        GenericStep step = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);
        
        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                step::perform);
    }

    @Test
    void perform_whenNotTDUCPSlotSelected_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("12345678")
                .build();
        context.getUserSelection().selectVehicleSlot(vehicleSlot);
        GenericStep step = GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                step::perform);
    }

    @Test
    void perform_shouldApplyPatchesWithProperties_andRemoveFromDealer() throws ReflectiveOperationException, IOException, URISyntaxException, StepException {
        // GIVEN
        String vehicleSlotRef = "300000000";
        String dealerRef = "550413704";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(vehicleSlotRef)
                .build();
        context.getUserSelection().selectVehicleSlot(vehicleSlot);
        DatabaseChangeHelper changeHelper = new DatabaseChangeHelper(context.getMiner());
        changeHelper.updateItemRawValueAtIndexAndFieldRank(CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 1, vehicleSlotRef);
        changeHelper.updateItemRawValueAtIndexAndFieldRank(CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 2, vehicleSlotRef);
        changeHelper.updateItemRawValueAtIndexAndFieldRank(CAR_SHOPS, 0, DatabaseConstants.DELTA_RANK_DEALER_SLOTS + 3, vehicleSlotRef);
        RestoreSlotStep step = (RestoreSlotStep) GenericStep.starterStep(configuration, context)
                .nextStep(GenericStep.StepType.RESTORE_SLOT);
        step.setPatcherComponent(databasePatcher);

        // WHEN
        step.perform();

        // THEN
        verify(databasePatcher).applyWithProperties(patchCaptor.capture(), patchPropertiesCaptor.capture());
        final DatabasePatchProperties actualProperties = patchPropertiesCaptor.getValue();
        assertThat(actualProperties.getVehicleSlotReference()).contains(vehicleSlotRef);

        final DbPatchDto actualPatchObject = patchCaptor.getValue();
        assertThat(actualPatchObject.getChanges()).hasSize(79);

        final Optional<DbPatchDto.DbChangeDto> carShopsChangeObject = actualPatchObject.getChanges().stream()
                .filter(changeObject -> CAR_SHOPS == changeObject.getTopic())
                .findAny();
        assertThat(carShopsChangeObject).isPresent();
        final DbPatchDto.DbChangeDto actualDealerChangeObject = carShopsChangeObject.get();
        final List<DbFieldValueDto> actualPartialValues = actualDealerChangeObject.getPartialValues();
        assertThat(actualDealerChangeObject.getRef()).isEqualTo(dealerRef);
        assertThat(actualPartialValues).hasSize(3);
        assertThat(actualPartialValues)
                .extracting("rank").containsOnly(4, 5, 6);
        assertThat(actualPartialValues)
                .extracting("value").containsOnly("61085282");

    }
}
