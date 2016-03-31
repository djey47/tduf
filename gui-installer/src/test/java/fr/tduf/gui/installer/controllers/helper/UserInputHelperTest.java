package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserInputHelperTest {

    private  static final String SLOT_REF = "30000000";
    private static final String CAR_ID = "3000";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Test
    public void createPatchPropertiesForVehicleSlot_whenNoProperty_shouldSetValuesFromSlot() {
        // GIVEN
        DbDataDto.Entry contentEntry = createSlotContentEntry();
        PatchProperties patchProperties = new PatchProperties();

        when(minerMock.getContentEntryFromTopicWithReference(SLOT_REF, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));


        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOT_REF, patchProperties, minerMock);


        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(SLOT_REF);
        assertThat(patchProperties.getCarIdentifier()).contains(CAR_ID);
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        DbDataDto.Entry contentEntry = createSlotContentEntry();
        PatchProperties patchProperties = new PatchProperties();

        final String slotReference = "1979";
        final String carIdentifier = "197";
        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);

        when(minerMock.getContentEntryFromTopicWithReference(SLOT_REF, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));


        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOT_REF, patchProperties, minerMock);


        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(slotReference);
        assertThat(patchProperties.getCarIdentifier()).contains(carIdentifier);
    }

    private static DbDataDto.Entry createSlotContentEntry() {
        return DbDataDto.Entry.builder()
                    .addItem(DbDataDto.Item.builder().withRawValue(CAR_ID).ofFieldRank(102).build())
                    .build();
    }
}
