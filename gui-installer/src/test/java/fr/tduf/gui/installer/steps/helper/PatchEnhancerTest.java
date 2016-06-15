package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.domain.javafx.DealerSlotData;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatchEnhancerTest {

    private static final String SLOT_REFERENCE = "30000000";
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

    private DatabaseContext databaseContext;
    private PatchProperties patchProperties;

    @Mock
    private VehicleSlotsHelper vehicleSlotsHelperMock;


    @Before
    public void setUp() throws IOException, URISyntaxException {
        Log.set(Log.LEVEL_DEBUG);

        patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOT_REFERENCE);

        databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
        databaseContext.getUserSelection().selectVehicleSlot(createVehicleSlot());
    }

    @Test(expected = InternalStepException.class)
    public void enhancePatchProperties_whenNoSlotReferenceProperty_andNoSlotSelected_shouldThrowException() {
        // GIVEN
        databaseContext.getUserSelection().resetVehicleSlot();
        patchProperties.clear();

        // WHEN
        createDefaultEnhancer().enhancePatchProperties(patchProperties);

        // THEN: IAE
    }

    @Test
    public void enhancePatchProperties_whenNoVehicleSlotSelected_shouldLoadFromDatabaseWithSlotProperty() {
        // GIVEN
        databaseContext.getUserSelection().resetVehicleSlot();

        final PatchEnhancer patchEnhancer = createDefaultEnhancer();
        patchEnhancer.overrideVehicleSlotsHelper(vehicleSlotsHelperMock);

        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(SLOT_REFERENCE)).thenReturn(of(createVehicleSlot()));


        // WHEN
        patchEnhancer.enhancePatchProperties(patchProperties);


        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(SLOT_REFERENCE);
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
    public void enhancePatchProperties_whenNoVehicleSlotSelected_andPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        databaseContext.getUserSelection().resetVehicleSlot();

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
        patchProperties.clear();
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
        final PatchEnhancer patchEnhancer = createDefaultEnhancer();
        patchEnhancer.overrideVehicleSlotsHelper(vehicleSlotsHelperMock);

        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(slotReference)).thenReturn(of(createVehicleSlot()));


        // WHEN
        patchEnhancer.enhancePatchProperties(patchProperties);


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
    public void enhancePatchProperties_whenNoDealerSlotSelected_shouldNotAddProperties() {
        // GIVEN-WHEN
        createDefaultEnhancer().enhancePatchProperties(patchProperties);

        // THEN
        assertThat(patchProperties.getDealerReference()).isEmpty();
        assertThat(patchProperties.getDealerSlot()).isEmpty();
    }

    @Test
    public void enhancePatchProperties_whenDealerSlotSelected_shouldAddProperties() {
        // GIVEN
        databaseContext.getUserSelection().selectDealerSlot(DealerSlotData.from(
                DealerSlotData.DealerDataItem.fromDealer(Dealer.builder()
                        .withRef("1111")
                        .withDisplayedName(Resource.from("", ""))
                        .withSlots(new ArrayList<>(0))
                        .build()),
                DealerSlotData.SlotDataItem.fromDealerSlot(Dealer.Slot.builder()
                        .withRank(1)
                        .build())));

        // WHEN
        createDefaultEnhancer().enhancePatchProperties(patchProperties);

        // THEN
        assertThat(patchProperties.getDealerReference()).contains("1111");
        assertThat(patchProperties.getDealerSlot()).contains(1);
    }

    @Test
    public void enhancePatchProperties_whenDealerSlotSelected_andPropertiesAlreadyExist_shouldKeepProperties() {
        // GIVEN
        patchProperties.setDealerReferenceIfNotExists("1111");
        patchProperties.setDealerSlotIfNotExists(1);
        databaseContext.getUserSelection().selectDealerSlot(DealerSlotData.from(
                DealerSlotData.DealerDataItem.fromDealer(Dealer.builder()
                        .withRef("2222")
                        .withDisplayedName(Resource.from("", ""))
                        .withSlots(new ArrayList<>(0))
                        .build()),
                DealerSlotData.SlotDataItem.fromDealerSlot(Dealer.Slot.builder()
                        .withRank(2)
                        .build())));

        // WHEN
        createDefaultEnhancer().enhancePatchProperties(patchProperties);

        // THEN
        assertThat(patchProperties.getDealerReference()).contains("1111");
        assertThat(patchProperties.getDealerSlot()).contains(1);
    }

    @Test(expected = InternalStepException.class)
    public void enhancePatchObject_withoutProperties_withoutSelectedSlot_shouldThrowException() {
        // GIVEN
        patchProperties.clear();
        databaseContext.getUserSelection().resetVehicleSlot();

        // WHEN
        createDefaultEnhancer().enhancePatchObject();

        // THEN: ISE
    }

    @Test
    public void enhancePatchObject_withoutProperties_shouldGenerateSlotRefFromSelectedSlot() {
        // GIVEN
        patchProperties.clear();

        // WHEN
        createDefaultEnhancer().enhancePatchObject();

        // THEN
        assertThat(patchProperties.getVehicleSlotReference()).contains(SLOT_REFERENCE);
    }

    @Test
    public void enhancePatchObject_withoutDealerProperties_shoulNotAddCarShops_updateInstruction() {
        // GIVEN-WHEN
        createDefaultEnhancer().enhancePatchObject();

        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).extracting("topic").doesNotContain(CAR_SHOPS);
    }

    @Test
    public void enhancePatchObjectWithPaintJobs_withPaintJobProperties_shouldAddCarColors_andInterior_updateInstructions() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        String mainColorId = "1111";
        String secColorId = "2222";
        String calColorId = "3333";
        String nameId = "4444";
        String name = "PJ name";
        String intId = "5555";
        String intManufacturerId = "62938337";
        String intNameId = "53365512";

        patchProperties.setExteriorMainColorIdIfNotExists(mainColorId, 1);

        final VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOT_REFERENCE)
                .addPaintJob(PaintJob.builder()
                        .withName(Resource.from(nameId, name))
                        .withColors(Resource.from(mainColorId, ""), Resource.from(secColorId, ""), Resource.from(calColorId, ""))
                        .addInteriorPattern(intId)
                        .build())
                .build();


        // WHEN
        createDefaultEnhancer().enhancePatchObjectWithPaintJobs(vehicleSlot);


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).hasSize(3);
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE, UPDATE_RES);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_COLORS, INTERIOR);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(null, nameId, intId);
        assertThat(patchObject.getChanges()).extracting("values").containsOnly(
                asList(
                        SLOT_REFERENCE,
                        "{COLORID.M.1}",
                        "{RES_COLORNAME.1}",
                        "{COLORID.S.1}",
                        "{CALLIPERSID.1}",
                        "0",
                        "0",
                        intId,
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636",
                        "11319636"
                ),
                null,
                asList(
                        intId,
                        intManufacturerId,
                        intNameId,
                        "{INTCOLORID.M.1}",
                        "{INTCOLORID.S.1}",
                        "{INTMATERIALID.1}",
                        "0"
                ));
        assertThat(patchObject.getChanges()).extracting("value").containsOnly(null, name);
    }


    @Test
    public void enhancePatchObjectWithRims_withRimProperties_shouldAddCarRims_andRims_updateInstructions() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        String rimId1 = "1111";
        String rimId2 = "2222";

        patchProperties.setRimsSlotReferenceIfNotExists(rimId1, 1);
        patchProperties.setRimsSlotReferenceIfNotExists(rimId2, 2);

        final RimSlot rimSlot1 = RimSlot.builder()
                .withRef(rimId1)
                .build();
        final RimSlot rimSlot2 = RimSlot.builder()
                .withRef(rimId2)
                .build();
        final VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOT_REFERENCE)
                .withDefaultRims(rimSlot1)
                .addRim(rimSlot2)
                .build();


        // WHEN
        createDefaultEnhancer().enhancePatchObjectWithRims(vehicleSlot);


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).hasSize(10); // 5 per rim set
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE, UPDATE_RES);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_RIMS, RIMS);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(
                null,
                rimId1,
                "{RES_RIMNAME.1}",
                "{RES_BANKNAME.FR.1}",
                "{RES_BANKNAME.RR.1}",
                rimId2,
                "{RES_RIMNAME.2}",
                "{RES_BANKNAME.FR.2}",
                "{RES_BANKNAME.RR.2}"
        );
        assertThat(patchObject.getChanges()).extracting("values").containsOnly(
                asList(SLOT_REFERENCE, rimId1),
                asList(
                        rimId1,
                        "{RIMBRANDREF.1}",
                        "54276512",
                        "{RES_RIMNAME.1}",
                        "{RIMWIDTH.FR.1}",
                        "{RIMHEIGHT.FR.1}",
                        "{RIMDIAM.FR.1}",
                        "{RIMWIDTH.RR.1}",
                        "{RIMHEIGHT.RR.1}",
                        "{RIMDIAM.RR.1}",
                        "0",
                        "0",
                        "{RIMBRANDREF.1}",
                        "{RES_BANKNAME.FR.1}",
                        "{RES_BANKNAME.RR.1}",
                        "0"),
                null,
                asList(SLOT_REFERENCE, rimId2),
                asList(
                        rimId2,
                        "{RIMBRANDREF.2}",
                        "54276512",
                        "{RES_RIMNAME.2}",
                        "{RIMWIDTH.FR.2}",
                        "{RIMHEIGHT.FR.2}",
                        "{RIMDIAM.FR.2}",
                        "{RIMWIDTH.RR.2}",
                        "{RIMHEIGHT.RR.2}",
                        "{RIMDIAM.RR.2}",
                        "0",
                        "0",
                        "{RIMBRANDREF.2}",
                        "{RES_BANKNAME.FR.2}",
                        "{RES_BANKNAME.RR.2}",
                        "0"));
        assertThat(patchObject.getChanges()).extracting("value").containsOnly(
                null,
                "{RIMNAME.1}",
                "{BANKNAME.FR.1}",
                "{BANKNAME.RR.1}",
                "{RIMNAME.2}",
                "{BANKNAME.FR.2}",
                "{BANKNAME.RR.2}");
    }

    @Test
    public void enhancePatchObjectWithLocationChange_withDealerProperties_shouldAddCarShops_updateInstruction() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN
        String dealerRef = "0000";
        patchProperties.setDealerReferenceIfNotExists(dealerRef);
        patchProperties.setDealerSlotIfNotExists(10);


        // WHEN
        createDefaultEnhancer().enhancePatchObjectWithLocationChange(SLOT_REFERENCE);


        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_SHOPS);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(dealerRef);
        assertThat(patchObject.getChanges()).extracting("partialValues").containsOnly(
                singletonList(DbFieldValueDto.fromCouple(13, SLOT_REFERENCE)));
    }

    @Test
    public void enhancePatchObjectWithInstallFlag_shouldAddCarPhysics_updateInstruction() throws URISyntaxException, IOException, ReflectiveOperationException, StepException {
        // GIVEN-WHEN
        createDefaultEnhancer().enhancePatchObjectWithInstallFlag(SLOT_REFERENCE);

        // THEN
        DbPatchDto patchObject = databaseContext.getPatchObject();
        assertThat(patchObject.getChanges()).extracting("type").containsOnly(UPDATE);
        assertThat(patchObject.getChanges()).extracting("topic").containsOnly(CAR_PHYSICS_DATA);
        assertThat(patchObject.getChanges()).extracting("ref").containsOnly(SLOT_REFERENCE);
        assertThat(patchObject.getChanges()).extracting("partialValues").containsOnly(
                singletonList(DbFieldValueDto.fromCouple(100, "100")));
    }

    private PatchEnhancer createDefaultEnhancer() {
        return new PatchEnhancer(databaseContext);
    }

    private static VehicleSlot createVehicleSlot() {
        return VehicleSlot.builder()
                .withRef(SLOT_REFERENCE)
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
