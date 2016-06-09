package fr.tduf.gui.installer.controllers.helper;


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
public class DealerSlotUserInputHelperTest {

    private static final String DEALERREF = "1111";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Before
    public void setup() {}

    @Test
    public void createPatchPropertiesForDealerSlot_whenNoProperty_shouldSetValuesFromSelectedSlot() {
        // GIVEN
        int slotRank = 2;

        PatchProperties patchProperties = new PatchProperties();
        DealerSlotData.DealerDataItem dealerItem = new DealerSlotData.DealerDataItem();
        dealerItem.referenceProperty().setValue(DEALERREF);
        DealerSlotData.SlotDataItem slotItem = new DealerSlotData.SlotDataItem();
        slotItem.rankProperty().setValue(slotRank);
        DealerSlotData dealerSlotData = DealerSlotData.from(dealerItem, slotItem);


        // WHEN
        DealerSlotUserInputHelper.createPatchPropertiesForDealerSlot(dealerSlotData, patchProperties);


        // THEN
        assertThat(patchProperties.getDealerReference()).contains(DEALERREF);
        assertThat(patchProperties.getDealerSlot()).contains(slotRank);
    }

    @Test
    public void createPatchPropertiesForDealerSlot_whenPropertiesExist_shouldKeepCurrentValues() {
        // GIVEN
        int slotRank = 2;

        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setDealerReferenceIfNotExists(DEALERREF);
        patchProperties.setDealerSlotIfNotExists(slotRank);
        DealerSlotData.DealerDataItem dealerItem = new DealerSlotData.DealerDataItem();
        dealerItem.referenceProperty().setValue("2222");
        DealerSlotData.SlotDataItem slotItem = new DealerSlotData.SlotDataItem();
        slotItem.rankProperty().setValue(4);
        DealerSlotData dealerSlotData = DealerSlotData.from(dealerItem, slotItem);


        // WHEN
        DealerSlotUserInputHelper.createPatchPropertiesForDealerSlot(dealerSlotData, patchProperties);


        // THEN
        assertThat(patchProperties.getDealerReference()).contains(DEALERREF);
        assertThat(patchProperties.getDealerSlot()).contains(slotRank);
    }
}
