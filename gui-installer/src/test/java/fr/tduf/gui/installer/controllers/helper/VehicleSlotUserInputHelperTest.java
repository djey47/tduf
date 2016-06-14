package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Test;

import java.util.ArrayList;

public class VehicleSlotUserInputHelperTest {

    private static final String SLOTREF = "30000000";

    @Test
    public void selectAndDefineVehicleSlot_whenForcedVehicleSlot_shouldNotSelectIt() throws Exception {
        // GIVEN
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(SLOTREF);
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);

        // WHEN
        VehicleSlotUserInputHelper.selectAndDefineVehicleSlot(databaseContext, null);

        // THEN: no FX calls
    }

}
