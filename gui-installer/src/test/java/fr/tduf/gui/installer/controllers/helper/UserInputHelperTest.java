package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
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
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.RIMS;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserInputHelperTest {

    private static final String SLOTREF = "30000000";
    private static final String SLOTREF_INV = "40000000";
    private static final String CARID = "3000";
    private static final String BANKNAME = "TDUCP_3000";
    private static final String RES_BANKNAME = "30000567";
    private static final String BANKNAME_FR_1 = "TDUCP_3000_F_01";
    private static final String BANKNAME_RR_1 = "TDUCP_3000_R_01";
    private static final String RES_BANKNAME_FR_1 = "3000000010";
    private static final String RES_BANKNAME_RR_1 = "3000000011";
    private static final String RIMREF_1 = "3000000001";
    private static final String RES_RIMBRAND_1 = "654857";
    private static final String RIMBRAND_1 = "Default";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Before
    public void setup() {
        // Valid slot
        when(minerMock.getContentEntryFromTopicWithReference(SLOTREF, CAR_PHYSICS_DATA)).thenReturn(of(createCarPhysicsContentEntry()));
        when(minerMock.getContentEntryFromTopicWithReference(RIMREF_1, RIMS)).thenReturn(of(createRimsContentEntry()));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(eq(RES_BANKNAME), eq(CAR_PHYSICS_DATA), any(DbResourceDto.Locale.class))).thenReturn(of(BANKNAME));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(eq(RES_BANKNAME_FR_1), eq(RIMS), any(DbResourceDto.Locale.class))).thenReturn(of(BANKNAME_FR_1));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(eq(RES_BANKNAME_RR_1), eq(RIMS), any(DbResourceDto.Locale.class))).thenReturn(of(BANKNAME_RR_1));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(eq(RES_RIMBRAND_1), eq(RIMS), any(DbResourceDto.Locale.class))).thenReturn(of(RIMBRAND_1));

        // Invalid slot
        when(minerMock.getContentEntryFromTopicWithReference(SLOTREF_INV, CAR_PHYSICS_DATA)).thenReturn(empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPatchPropertiesForVehicleSlot_whenNoProperty_andInvalidSlotRef_shouldThrowException() {
        // GIVEN
        PatchProperties patchProperties = new PatchProperties();

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOTREF_INV, patchProperties, minerMock);

        // THEN: IAE
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
        assertThat(patchProperties.getRimSlotReference(1)).contains(RIMREF_1);
        assertThat(patchProperties.getRimBrandNameResource(1)).contains(RES_RIMBRAND_1);
        assertThat(patchProperties.getFrontRimBankFileName(1)).contains(BANKNAME_FR_1);
        assertThat(patchProperties.getRearRimBankFileName(1)).contains(BANKNAME_RR_1);
        assertThat(patchProperties.getFrontRimBankFileNameResource(1)).contains(RES_BANKNAME_FR_1);
        assertThat(patchProperties.getRearRimBankFileNameResource(1)).contains(RES_BANKNAME_RR_1);
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        final String slotReference = "1979";
        final String carIdentifier = "197";
        final String bankName = "A3_V6";
        final String bankResource = "12345567";
        final String frontRimBankName = "A3_V6_F_01";
        final String rimSlotReference = "1111111";
        final String rearRimBankName = "A3_V6_R_01";
        final String frontRimResource = "12345568";
        final String rearRimResource = "12345569";
        final String rimBrandReference = "664857";
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);
        patchProperties.setBankNameIfNotExists(bankName);
        patchProperties.setRimsSlotReferenceIfNotExists(rimSlotReference, 1);
        patchProperties.setResourceRimsBrandIfNotExists(rimBrandReference, 1);
        patchProperties.setResourceBankNameIfNotExists(bankResource);
        patchProperties.setFrontRimBankNameIfNotExists(frontRimBankName, 1);
        patchProperties.setResourceFrontRimBankIfNotExists(frontRimResource, 1);
        patchProperties.setRearRimBankNameIfNotExists(rearRimBankName, 1);
        patchProperties.setResourceRearRimBankIfNotExists(rearRimResource, 1);

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(SLOTREF, patchProperties, minerMock);

        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(slotReference);
        assertThat(patchProperties.getCarIdentifier()).contains(carIdentifier);
        assertThat(patchProperties.getBankFileName()).contains(bankName);
        assertThat(patchProperties.getBankFileNameResource()).contains(bankResource);
        assertThat(patchProperties.getRimSlotReference(1)).contains(rimSlotReference);
        assertThat(patchProperties.getRimBrandNameResource(1)).contains(rimBrandReference);
        assertThat(patchProperties.getFrontRimBankFileName(1)).contains(frontRimBankName);
        assertThat(patchProperties.getRearRimBankFileName(1)).contains(rearRimBankName);
        assertThat(patchProperties.getFrontRimBankFileNameResource(1)).contains(frontRimResource);
        assertThat(patchProperties.getRearRimBankFileNameResource(1)).contains(rearRimResource);
    }

    @Test
    public void createPatchPropertiesForDealerSlot_whenNoProperty_shouldSetValuesFromSelectedSlot() {
        // GIVEN
        String dealerRef = "1111";
        int slotRank = 2;

        PatchProperties patchProperties = new PatchProperties();
        DealerSlotData.DealerDataItem dealerItem = new DealerSlotData.DealerDataItem();
        dealerItem.referenceProperty().setValue(dealerRef);
        DealerSlotData.SlotDataItem slotItem = new DealerSlotData.SlotDataItem();
        slotItem.rankProperty().setValue(slotRank);
        DealerSlotData dealerSlotData = DealerSlotData.from(dealerItem, slotItem);


        // WHEN
        UserInputHelper.createPatchPropertiesForDealerSlot(dealerSlotData, patchProperties);


        // THEN
        assertThat(patchProperties.getDealerReference()).contains(dealerRef);
        assertThat(patchProperties.getDealerSlot()).contains(slotRank);
    }

    @Test
    public void createPatchPropertiesForDealerSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        String dealerRef = "1111";
        int slotRank = 2;

        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setDealerReferenceIfNotExists(dealerRef);
        patchProperties.setDealerSlotIfNotExists(slotRank);
        DealerSlotData.DealerDataItem dealerItem = new DealerSlotData.DealerDataItem();
        dealerItem.referenceProperty().setValue("2222");
        DealerSlotData.SlotDataItem slotItem = new DealerSlotData.SlotDataItem();
        slotItem.rankProperty().setValue(4);
        DealerSlotData dealerSlotData = DealerSlotData.from(dealerItem, slotItem);


        // WHEN
        UserInputHelper.createPatchPropertiesForDealerSlot(dealerSlotData, patchProperties);


        // THEN
        assertThat(patchProperties.getDealerReference()).contains(dealerRef);
        assertThat(patchProperties.getDealerSlot()).contains(slotRank);
    }

    private static DbDataDto.Entry createCarPhysicsContentEntry() {
        return DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().withRawValue(RES_BANKNAME).ofFieldRank(9).build())
                .addItem(DbDataDto.Item.builder().withRawValue(RIMREF_1).ofFieldRank(10).build())
                .addItem(DbDataDto.Item.builder().withRawValue(CARID).ofFieldRank(102).build())
                .build();
    }

    private static DbDataDto.Entry createRimsContentEntry() {
        return DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().withRawValue(RIMREF_1).ofFieldRank(1).build())
                .addItem(DbDataDto.Item.builder().withRawValue(RES_RIMBRAND_1).ofFieldRank(13).build())
                .addItem(DbDataDto.Item.builder().withRawValue(RES_BANKNAME_FR_1).ofFieldRank(14).build())
                .addItem(DbDataDto.Item.builder().withRawValue(RES_BANKNAME_RR_1).ofFieldRank(15).build())
                .build();
    }
}
