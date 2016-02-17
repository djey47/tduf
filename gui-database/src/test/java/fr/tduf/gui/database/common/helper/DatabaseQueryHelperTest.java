package fr.tduf.gui.database.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.primitives.Ints.asList;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto.Locale.FRANCE;
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

        DbResourceDto.Entry resEntry1 = DbResourceDto.Entry.builder()
                .forReference("")
                .withValue("RES1")
                .build();
        DbResourceDto.Entry resEntry2 = DbResourceDto.Entry.builder()
                .forReference("")
                .withValue("RES2")
                .build();

        when(minerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 3, 1, FRANCE)).thenReturn(of(resEntry1));
        when(minerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 4, 1, FRANCE)).thenReturn(of(resEntry2));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, fieldRanks, minerMock);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - RES2");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenLocalResourceUnavailable_shouldReturnItemRawValue() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(3, 4);

        DbResourceDto.Entry resEntry1 = DbResourceDto.Entry.builder()
                .forReference("")
                .withValue("RES1")
                .build();
        DbDataDto.Item item2 = DbDataDto.Item.builder()
                .ofFieldRank(4)
                .withRawValue("85467580")
                .build();

        when(minerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 3, 1, FRANCE)).thenReturn(of(resEntry1));
        when(minerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 4, 1, FRANCE)).thenReturn(empty());
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

        when(minerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 3, 1, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 3, 1)).thenReturn(empty());


        // WHEN
        DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, CAR_PHYSICS_DATA, FRANCE, fieldRanks, minerMock);


        // THEN: NSEE
    }
}
