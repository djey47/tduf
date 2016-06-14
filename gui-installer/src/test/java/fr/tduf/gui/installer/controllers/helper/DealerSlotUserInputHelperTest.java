package fr.tduf.gui.installer.controllers.helper;


import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

public class DealerSlotUserInputHelperTest {

    private static final String DEALERREF = "1111";

    @Test
    public void selectAndDefineDealerSlot_whenForcedDealerSlot_shouldNotSelectIt() throws Exception {
        // GIVEN
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setDealerReferenceIfNotExists(DEALERREF);
        patchProperties.setDealerSlotIfNotExists(1);
        DatabaseContext databaseContext = new DatabaseContext(new ArrayList<>(0), "");
        databaseContext.setPatch(DbPatchDto.builder().build(), patchProperties);

        // WHEN
        DealerSlotUserInputHelper.selectAndDefineDealerSlot(databaseContext, null);

        // THEN: no FX calls
    }
}
