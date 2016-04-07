package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DealerHelperTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    @Test
    public void getDealers() throws Exception {
        // GIVEN
        final String dealerReference = "REF1";
        final String nameResourceReference = "0000";
        final String nameResourceValue = "DEALER";

        DbResourceDto.Entry dealerNameResourceEntry = DbResourceDto.Entry.builder().forReference(nameResourceReference).build();
        dealerNameResourceEntry.setValue(nameResourceValue);
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(DbDataDto.Entry.builder()
                                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(dealerReference).build())
                                .addItem(DbDataDto.Item.builder().ofFieldRank(3).withRawValue(nameResourceReference).build())
                                .addItem(DbDataDto.Item.builder().ofFieldRank(4).withRawValue("SLOTREF1").build())
                                .addItem(DbDataDto.Item.builder().ofFieldRank(5).withRawValue("SLOTREF2").build())
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1.0")
                        .containingEntries(singletonList(dealerNameResourceEntry))
                        .build()
                )
                .build();
        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));
        DbDataDto.Entry carSlotEntry1 = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Entry carSlotEntry2 = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        when(minerMock.getContentEntryFromTopicWithReference("SLOTREF1", CAR_PHYSICS_DATA)).thenReturn(Optional.of(carSlotEntry1));
        when(minerMock.getContentEntryFromTopicWithReference("SLOTREF2", CAR_PHYSICS_DATA)).thenReturn(Optional.of(carSlotEntry2));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(nameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(nameResourceValue));


        // WHEN
        List<Dealer> dealers = DealerHelper.load(minerMock).getDealers();


        // THEN
        assertThat(dealers).extracting("ref").containsExactly(dealerReference);
        assertThat(dealers).extracting("displayedName").containsExactly(Resource.from(nameResourceReference, nameResourceValue));
        assertThat(dealers.get(0).getSlots()).hasSize(2);
        assertThat(dealers.get(0).getSlots()).extracting("rank").containsExactly(1,2);
        assertThat(dealers.get(0).getSlots().get(0).getVehicleSlot()).isPresent();
        assertThat(dealers.get(0).getSlots().get(1).getVehicleSlot()).isPresent();
    }
}
