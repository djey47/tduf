package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserInputHelperTest {

    private  static final String SLOTREF = "30000000";
    private static final String CARID = "3000";
    private static final String BANKNAME = "TDUCP_3000";
    private static final String RES_BANKNAME = "30000567";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Before
    public void setup() {
        when(minerMock.getContentEntryFromTopicWithReference(SLOTREF, CAR_PHYSICS_DATA)).thenReturn(of(createSlotContentEntry()));
        when(minerMock.getLocalizedResourceValueFromContentEntry(eq(0L), eq(9), eq(CAR_PHYSICS_DATA), any(DbResourceDto.Locale.class))).thenReturn(of(BANKNAME));
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenNoProperty_shouldSetValuesFromSlot() {
        // GIVEN
        PatchProperties patchProperties = new PatchProperties();

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOTREF, patchProperties, minerMock);

        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(SLOTREF);
        assertThat(patchProperties.getCarIdentifier()).contains(CARID);
        assertThat(patchProperties.getBankFileName()).contains(BANKNAME);
        assertThat(patchProperties.getBankFileNameResource()).contains(RES_BANKNAME);
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        final String slotReference = "1979";
        final String carIdentifier = "197";
        final String bankName = "A3_V6";
        final String bankResource = "12345567";
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);
        patchProperties.setBankNameIfNotExists(bankName);
        patchProperties.setResourceBankNameIfNotExists(bankResource);

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOTREF, patchProperties, minerMock);

        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(slotReference);
        assertThat(patchProperties.getCarIdentifier()).contains(carIdentifier);
        assertThat(patchProperties.getBankFileName()).contains(bankName);
        assertThat(patchProperties.getBankFileNameResource()).contains(bankResource);
    }

    private static DbDataDto.Entry createSlotContentEntry() {
        return DbDataDto.Entry.builder()
                    .forId(0)
                    .addItem(DbDataDto.Item.builder().withRawValue(RES_BANKNAME).ofFieldRank(9).build())
                    .addItem(DbDataDto.Item.builder().withRawValue(CARID).ofFieldRank(102).build())
                    .build();
    }
}
