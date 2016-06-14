package fr.tduf.gui.installer.steps.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class PatchEnhancerTest {

    private static final String SLOT_REFERENCE = "30000000";

    private DatabaseContext databaseContext;
    private PatchProperties patchProperties;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Log.set(Log.LEVEL_DEBUG);

        patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOT_REFERENCE);

        databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);
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
        new PatchEnhancer(databaseContext).enhancePatchObjectWithPaintJobs(vehicleSlot);


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
        new PatchEnhancer(databaseContext).enhancePatchObjectWithRims(vehicleSlot);


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
}
