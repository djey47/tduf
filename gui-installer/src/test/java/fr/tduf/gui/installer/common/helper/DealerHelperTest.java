package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.Dealer;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_SHOPS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DealerHelperTest {
    private static final String SLOTREF_1 = "SLOTREF1";
    private static final String SLOTREF_2 = "SLOTREF2";
    private static final String DEALERREF = "541293706";
    private static final String DEALERREF_NOMETA = "541293707";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private VehicleSlotsHelper vehicleSlotsHelperMock;

    @InjectMocks
    private DealerHelper dealerHelper;

    @Before
    public void setUp() {
        mockVehicleSlots();
    }

    @Test
    public void getDealers_whenMetaDataUnavailable() throws Exception {
        // GIVEN
        final String nameResourceReference = "0000";
        final String nameResourceValue = "DEALER";
        final String fileNameResourceReference = "1111";
        final String fileNameResourceValue = "ECD_B028";

        ResourceEntryDto dealerNameResourceEntry = ResourceEntryDto.builder().forReference(nameResourceReference).build();
        dealerNameResourceEntry.setValue(nameResourceValue);
        ResourceEntryDto dealerFileNameResourceEntry = ResourceEntryDto.builder().forReference(fileNameResourceReference).build();
        dealerFileNameResourceEntry.setValue(fileNameResourceValue);
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(DEALERREF_NOMETA).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(fileNameResourceReference).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(nameResourceReference).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(4).withRawValue(SLOTREF_1).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(5).withRawValue(SLOTREF_2).build())
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1.0")
                        .containingEntries(asList(dealerNameResourceEntry, dealerFileNameResourceEntry))
                        .build()
                )
                .build();
        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(nameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(nameResourceValue));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(fileNameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(fileNameResourceValue));


        // WHEN
        List<Dealer> dealers = dealerHelper.getDealers(DealerHelper.DealerKind.ALL);


        // THEN
        assertThat(dealers).extracting("ref").containsExactly(DEALERREF_NOMETA);
        assertThat(dealers).extracting("displayedName").containsExactly(Resource.from(nameResourceReference, nameResourceValue));
        assertThat(dealers).extracting("location").containsExactly("???");
        assertThat(dealers.get(0).getSlots()).hasSize(2);
        assertThat(dealers.get(0).getSlots()).extracting("rank").containsExactly(1,2);
        assertThat(dealers.get(0).getSlots().get(0).getVehicleSlot()).isPresent();
        assertThat(dealers.get(0).getSlots().get(1).getVehicleSlot()).isPresent();
    }

    @Test
    public void getDealers_whenMetaDataAvailable_shouldUseIt() throws Exception {
        // GIVEN
        final String nameResourceReference = "0000";
        final String nameResourceValue = "DEALER";

        ResourceEntryDto dealerNameResourceEntry = ResourceEntryDto.builder().forReference(nameResourceReference).build();
        dealerNameResourceEntry.setValue(nameResourceValue);
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(DEALERREF).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(nameResourceReference).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(4).withRawValue(SLOTREF_1).build())
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1.0")
                        .containingEntries(singletonList(dealerNameResourceEntry))
                        .build()
                )
                .build();
        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(nameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(nameResourceValue));


        // WHEN
        List<Dealer> dealers = dealerHelper.getDealers(DealerHelper.DealerKind.ALL);


        // THEN
        assertThat(dealers).extracting("location").containsExactly("Honolulu Downtown: Kapalama");
    }

    @Test
    public void searchForVehicleSlot_whenSameVehicleLocatedThreeTimesInSingleDealer_shouldReturnOneEntryWithThreeSlots() {
        // GIVEN
        final String nameResourceReference = "0000";
        final String nameResourceValue = "DEALER";

        ResourceEntryDto dealerNameResourceEntry = ResourceEntryDto.builder().forReference(nameResourceReference).build();
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(DEALERREF).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(nameResourceReference).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(4).withRawValue(SLOTREF_1).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(5).withRawValue(SLOTREF_1).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(6).withRawValue(SLOTREF_1).build())
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1.0")
                        .containingEntries(singletonList(dealerNameResourceEntry))
                        .build()
                )
                .build();

        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(nameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(nameResourceValue));


        // WHEN
        final Map<String, List<Integer>> actualSlots = dealerHelper.searchForVehicleSlot(SLOTREF_1);


        // THEN
        assertThat(actualSlots).hasSize(1);
        final String actualRef = actualSlots.keySet().stream().findAny().get();
        assertThat(actualRef).isEqualTo(DEALERREF);
        final List<Integer> actualList = actualSlots.get(DEALERREF);
        assertThat(actualList).hasSize(3);
        assertThat(actualList).contains(1, 2, 3);
    }

    @Test
    public void searchForVehicleSlot_whenUnlocatedVehicle_shouldReturnEmptyMap() {
        // GIVEN
        final String nameResourceReference = "0000";
        final String nameResourceValue = "DEALER";

        ResourceEntryDto dealerNameResourceEntry = ResourceEntryDto.builder().forReference(nameResourceReference).build();
        DbDto carShopsTopicObject = DbDto.builder()
                .withData(DbDataDto.builder()
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(DEALERREF).build())
                                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(nameResourceReference).build())
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1.0")
                        .containingEntries(singletonList(dealerNameResourceEntry))
                        .build()
                )
                .build();

        when(minerMock.getDatabaseTopic(CAR_SHOPS)).thenReturn(Optional.of(carShopsTopicObject));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(nameResourceReference, CAR_SHOPS, UNITED_STATES)).thenReturn(Optional.of(nameResourceValue));


        // WHEN
        final Map<String, List<Integer>> actualSlots = dealerHelper.searchForVehicleSlot(SLOTREF_1);


        // THEN
        assertThat(actualSlots).isEmpty();
    }

    private void mockVehicleSlots() {
        dealerHelper.setVehicleSlotsHelper(vehicleSlotsHelperMock);

        VehicleSlot vehicleSlot1 = VehicleSlot.builder()
                .withRef(SLOTREF_1)
                .build();
        VehicleSlot vehicleSlot2 = VehicleSlot.builder()
                .withRef(SLOTREF_2)
                .build();
        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(SLOTREF_1)).thenReturn(Optional.of(vehicleSlot1));
        when(vehicleSlotsHelperMock.getVehicleSlotFromReference(SLOTREF_2)).thenReturn(Optional.of(vehicleSlot2));
    }
}
