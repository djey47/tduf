package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.gui.installer.domain.PaintJob;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

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
    private static final String RES_COLORNAME_1 = "4607167";
    private static final String COLORNAME_1 = "Nero";
    private static final String RES_COLORNAME_2 = "4607267";
    private static final String COLORNAME_2 = "Blau";
    private static final String INTREF_1 = "5000000001";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Before
    public void setup() {}

    @Test(expected = IllegalArgumentException.class)
    public void createPatchPropertiesForVehicleSlot_whenNoProperty_andInvalidSlotRef_shouldThrowException() {
        // GIVEN
        VehicleSlot vehicleSlot = VehicleSlot.builder().withRef(SLOTREF_INV).build();
        PatchProperties patchProperties = new PatchProperties();

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(vehicleSlot, patchProperties);

        // THEN: IAE
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenNoProperty_shouldSetValuesFromSlot() {
        // GIVEN
        VehicleSlot vehicleSlot = createVehicleSlot();
        PatchProperties patchProperties = new PatchProperties();

        // WHEN
        UserInputHelper.createPatchPropertiesForVehicleSlot(vehicleSlot, patchProperties);

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
        assertThat(patchProperties.getExteriorColorNameResource(1)).contains(RES_COLORNAME_1);
        assertThat(patchProperties.getExteriorColorNameResource(2)).contains(RES_COLORNAME_2);
    }

    @Test
    public void createPatchPropertiesForVehicleSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        VehicleSlot vehicleSlot = createVehicleSlot();
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
        UserInputHelper.createPatchPropertiesForVehicleSlot(vehicleSlot, patchProperties);

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

    private static VehicleSlot createVehicleSlot() {
        return VehicleSlot.builder()
                .withRef(SLOTREF)
                .withCarIdentifier(Integer.valueOf(CARID))
                .withFileName(Resource.from(RES_BANKNAME, BANKNAME))
                .withDefaultRims(RimSlot.builder()
                        .withRef(RIMREF_1)
                        .withParentDirectoryName(Resource.from(RES_RIMBRAND_1, RIMBRAND_1))
                        .withRimsInformation(RimSlot.RimInfo.builder()
                                        .withFileName(Resource.from(RES_BANKNAME_FR_1, BANKNAME_FR_1))
                                        .build(),
                                RimSlot.RimInfo.builder()
                                        .withFileName(Resource.from(RES_BANKNAME_RR_1, BANKNAME_RR_1))
                                        .build())
                        .build())
                .addPaintJob(PaintJob.builder()
                        .atRank(1)
                        .withName(Resource.from(RES_COLORNAME_1, COLORNAME_1))
                        .addInteriorPattern(INTREF_1)
                        .build())
                .addPaintJob(PaintJob.builder()
                        .atRank(2)
                        .withName(Resource.from(RES_COLORNAME_2, COLORNAME_2))
                        .addInteriorPattern(INTREF_1)
                        .build())
                .build();
    }
}
