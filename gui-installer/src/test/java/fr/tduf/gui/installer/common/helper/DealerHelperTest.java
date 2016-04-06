package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DealerHelperTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    @Test
    public void getDealers() throws Exception {
        // GIVEN
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(DbDataDto.Entry.builder()
                                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue("REF1").build())
                                .build())
                        .build())
                .build();
        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));

        // WHEN
        List<Dealer> dealers = DealerHelper.load(minerMock).getDealers();

        // THEN
        assertThat(dealers).extracting("ref").containsExactly("REF1");

    }
}