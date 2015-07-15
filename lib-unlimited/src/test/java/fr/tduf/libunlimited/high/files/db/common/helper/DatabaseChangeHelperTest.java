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
        DbDataDto dataObject = createDefaultDataObject();
        DbDto databaseObject = createDatabaseObject(dataObject);
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

    @Test
    public void updateResourceWithReference_whenExistingEntry_shouldReplaceReferenceAndValue() {
        // GIVEN
        String initialReference = "0";
        DbResourceDto.Entry resourceEntry = createDefaultResourceEntry(initialReference);

        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(initialReference, TOPIC, LOCALE)).thenReturn(Optional.of(resourceEntry));
        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(Optional.empty());


        // WHEN
        changeHelper.updateResourceWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceEntry.getReference()).isEqualTo(RESOURCE_REFERENCE);
        assertThat(resourceEntry.getValue()).isEqualTo(RESOURCE_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateResourceWithReference_whenNonexistingEntry_shouldThrowException() {
        // GIVEN
        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.updateResourceWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateResourceWithReference_whenEntryExistsWithNewReference_shouldThrowException() {
        // GIVEN
        String initialReference = "0";
        DbResourceDto.Entry resourceEntry = createDefaultResourceEntry(initialReference);

        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(initialReference, TOPIC, LOCALE)).thenReturn(Optional.of(resourceEntry));
        when(minerMock.getResourceEntryFromTopicAndLocaleWithReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(Optional.of(resourceEntry));


        // WHEN
        changeHelper.updateResourceWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    public void removeEntryWithIdentifier_whenEntryExists_shouldDeleteIt_andUpdateIds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.getEntries().add(createDefaultContentEntry(1));
        dataObject.getEntries().add(createDefaultContentEntry(2));
        dataObject.getEntries().add(createDefaultContentEntry(3));

        DbDto topicObject = createDatabaseObject(dataObject);

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.of(topicObject));


        // WHEN
        changeHelper.removeEntryWithIdentifier(2, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(1L, 2L);
    }

    @Test(expected = NoSuchElementException.class)
    public void removeEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.removeEntryWithIdentifier(1, TOPIC );

        // THEN: NSEE
    }

    private static DbDto createDatabaseObject(DbDataDto dataObject) {
        return DbDto.builder()
                .withData(dataObject)
                .build();
    }

    private static DbDataDto createDefaultDataObject() {
        return DbDataDto.builder()
                .build();
    }

    private static DbDataDto.Entry createDefaultContentEntry(long internalId) {
        return DbDataDto.Entry.builder()
                .forId(internalId)
                .build();
    }

    private static DbResourceDto.Entry createDefaultResourceEntry(String reference) {
        return DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue("")
                .build();
    }
}