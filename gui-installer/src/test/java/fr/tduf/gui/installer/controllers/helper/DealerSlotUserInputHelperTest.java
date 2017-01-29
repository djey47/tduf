package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Test;

import java.util.ArrayList;

public class DealerSlotUserInputHelperTest {

    private static final String DEALERREF = "1111";

    @Test
    public void selectAndDefineDealerSlot_whenForcedDealerSlot_shouldNotSelectIt() throws Exception {
        // GIVEN
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.setDealerReferenceIfNotExists(DEALERREF);
        patchProperties.setDealerSlotIfNotExists(1);
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);

        // WHEN
        DealerSlotUserInputHelper.selectAndDefineDealerSlot(databaseContext, null);

        // THEN: no FX calls
    }
}
