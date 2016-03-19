package fr.tduf.gui.database.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.primitives.Ints.asList;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseQueryHelperTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    @Test
    public void fetchResourceValuesWithEntryId_whenNoFieldRanks_shouldReturnUnavailable() throws Exception {
        // GIVEN - WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, new ArrayList<>(), minerMock);

        // THEN
        assertThat(actualLabel).isEqualTo("<?>");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenLocalResourcesAvailable() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(3, 4);
        String resValue1 = "RES1";
        String resValue2 = "RES2";

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, CAR_PHYSICS_DATA, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, CAR_PHYSICS_DATA,  FRANCE)).thenReturn(of(resValue2));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, fieldRanks, minerMock);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - RES2");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenLocalResourceUnavailable_shouldReturnItemRawValue() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(3, 4);
        String resValue1 = "RES1";
        DbDataDto.Item item2 = DbDataDto.Item.builder()
                .ofFieldRank(4)
                .withRawValue("85467580")
                .build();

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, CAR_PHYSICS_DATA, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, CAR_PHYSICS_DATA, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 4, 1)).thenReturn(of(item2));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, fieldRanks, minerMock);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - <85467580>");
    }

    @Test(expected = NoSuchElementException.class)
    public void fetchResourceValuesWithEntryId_whenLocalResourceUnavailable_andItemUnavailable_shouldThrowException() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = singletonList(3);

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, CAR_PHYSICS_DATA, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 3, 1)).thenReturn(empty());


        // WHEN
        DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, fieldRanks, minerMock);


        // THEN: NSEE
    }
}
