package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseChangeHelperTest {

    private static final String ENTRY_REFERENCE = "111111";
    private static final String RESOURCE_REFERENCE = "000000";
    private static final String RESOURCE_VALUE = "TEST";
    private static final DbDto.Topic TOPIC = DbDto.Topic.CAR_PHYSICS_DATA;
    private static final DbResourceDto.Locale LOCALE = DbResourceDto.Locale.CHINA;

    @Mock
    DatabaseGenHelper genHelperMock;

    @Mock
    BulkDatabaseMiner minerMock;

    @InjectMocks
    DatabaseChangeHelper changeHelper;

    @Test
    public void addResourceWithReference_andNonExisting_shouldCreateNewResourceEntry() throws Exception {
        // GIVEN
        DbResourceDto resourceObject = DbResourceDto.builder().build();

        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(Optional.empty());
        when(minerMock.getResourceFromTopicAndLocale(TOPIC, LOCALE)).thenReturn(Optional.of(resourceObject));


        // WHEN
        changeHelper.addResourceWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
        assertThat(resourceObject.getEntries()).extracting("value").containsExactly(RESOURCE_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addResourceWithReference_andExisting_shouldThrowException() throws Exception {
        // GIVEN
        Optional<DbResourceDto.Entry> entry = Optional.of(DbResourceDto.Entry.builder().build());

        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(entry);


        // WHEN
        changeHelper.addResourceWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected = NoSuchElementException.class)
    public void addResourceWithReference_andNoResourceEntries_shouldThrowException() throws Exception {
        // GIVEN
        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(Optional.empty());
        when(minerMock.getResourceFromTopicAndLocale(TOPIC, LOCALE)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.addResourceWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: NSEE
    }

    @Test
    public void addContentsEntryWithDefaultItems_whenTopicObjectAvailable_shouldCreateAndReturnIt() {
        // GIVEN
        DbDataDto dataObject = DbDataDto.builder()
                .build();
        DbDto databaseObject = DbDto.builder()
                .withData(dataObject)
                .build();
        List<DbDataDto.Item> contentItems = new ArrayList<>();
        contentItems.add(DbDataDto.Item.builder()
                .ofFieldRank(1)
                .build());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.of(databaseObject));
        when(genHelperMock.buildDefaultContentItems(Optional.of(ENTRY_REFERENCE), databaseObject)).thenReturn(contentItems);


        // WHEN
        DbDataDto.Entry actualEntry = changeHelper.addContentsEntryWithDefaultItems(Optional.of(ENTRY_REFERENCE), TOPIC);


        // THEN
        assertThat(actualEntry).isNotNull();
        assertThat(actualEntry.getId()).isZero();
        assertThat(actualEntry.getItems()).hasSize(1);
        assertThat(actualEntry.getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void addContentsEntryWithDefaultItems_whenTopicObjectUnavailable_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.addContentsEntryWithDefaultItems(Optional.of(ENTRY_REFERENCE), TOPIC);

        // THEN: NSEE
        verifyZeroInteractions(genHelperMock);
    }
}