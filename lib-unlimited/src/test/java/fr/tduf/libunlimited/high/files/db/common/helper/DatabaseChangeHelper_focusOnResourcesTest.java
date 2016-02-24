package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.NoSuchElementException;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto.Locale.FRANCE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseChangeHelper_focusOnResourcesTest {

    private static final String RESOURCE_REFERENCE = "000000";
    private static final String RESOURCE_VALUE = "TEST";
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final DbResourceEnhancedDto.Locale LOCALE = DbResourceEnhancedDto.Locale.CHINA;

    @Mock
    DatabaseGenHelper genHelperMock;

    @Mock
    BulkDatabaseMiner minerMock;

    @InjectMocks
    DatabaseChangeHelper changeHelper;

    @After
    public void tearDown() {
        BulkDatabaseMiner.clearAllCaches();
    }

    @Test
    public void addResourceWithReference_andNonExistingEntry_shouldCreateNewResourceEntry() throws Exception {
        // GIVEN
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
    }

    @Test
    public void addResourceWithReference_andExistingEntry_shouldCreateNewResourceItem() throws Exception {
        // GIVEN
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference(RESOURCE_REFERENCE)
                .setValueForLocale("", FRANCE);

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
        final DbResourceEnhancedDto.Entry actualEntry = resourceObject.getEntryByReference(RESOURCE_REFERENCE).get();
        assertThat(actualEntry.getItemCount()).isEqualTo(2);
        assertThat(actualEntry.getValueForLocale(LOCALE)).contains(RESOURCE_VALUE);
        assertThat(actualEntry.getValueForLocale(FRANCE)).contains("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addResourceWithReference_andExisting_shouldThrowException() throws Exception {
        // GIVEN
        String resourceValue = "TEST2";
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(of(resourceValue));

        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected = NoSuchElementException.class)
    public void addResourceValueWithReference_andNoResource_shouldThrowException() throws Exception {
        // GIVEN
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: NSEE
    }

    @Test
    public void updateResourceItemWithReference_whenExistingEntry_shouldReplaceReferenceAndValue() {
        // GIVEN
        String initialReference = "0";
        String initialValue = "";
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();
        DbResourceEnhancedDto.Entry resourceEntry = createDefaultResourceEntryEnhanced(initialReference);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(resourceEntry));
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(empty());
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(initialReference, TOPIC, LOCALE)).thenReturn(of(initialValue));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntryByReference(initialReference)).isEmpty();
        final Optional<DbResourceEnhancedDto.Entry> potentialEntry = resourceObject.getEntryByReference(RESOURCE_REFERENCE);
        assertThat(potentialEntry).isPresent();
        assertThat(potentialEntry.get().getValueForLocale(LOCALE)).contains(RESOURCE_VALUE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateResourceItemWithReference_whenNonexistingEntry_shouldThrowException() {
        // GIVEN
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(empty());

        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateResourceItemWithReference_whenEntryExistsWithNewReference_shouldThrowException() {
        // GIVEN
        String initialReference = "0";
        String existingValue = "e";

        DbResourceEnhancedDto.Entry existingEntry = createDefaultResourceEntryEnhanced(initialReference);
        DbResourceEnhancedDto.Entry existingEntry2 = createDefaultResourceEntryEnhanced(initialReference);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(existingEntry));
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(existingEntry2));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(of(existingValue));


        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    public void removeResourceWithReference_whenResourceEntryExists_shouldDeleteIt() {
        // GIVEN
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference(RESOURCE_REFERENCE);

        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.removeResourceWithReference(TOPIC, RESOURCE_REFERENCE);


        // THEN
        assertThat(resourceObject.getEntryByReference(RESOURCE_REFERENCE)).isEmpty();
    }

    @Test
    public void removeResourceValuesWithReference_whenResourceEntryExists_andSameLocaleAffected_shouldDeleteLocalizedValue() {
        // GIVEN
        DbResourceEnhancedDto.Entry resourceEntry = createDefaultResourceEntryEnhanced(RESOURCE_REFERENCE);
        resourceEntry.setValue(RESOURCE_VALUE);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(resourceEntry));


        // WHEN
        changeHelper.removeResourceValuesWithReference(TOPIC, RESOURCE_REFERENCE, singletonList(LOCALE));


        // THEN
        assertThat(resourceEntry.getItemCount()).isEqualTo(7);
        assertThat(resourceEntry.getItemForLocale(LOCALE)).isEmpty();
    }

    @Test
    public void removeResourceValuesWithReference_whenResourceEntryExists_andTwoLocalesAffected_shouldDeleteThem() {
        // GIVEN
        DbResourceEnhancedDto.Entry resourceEntry = createDefaultResourceEntryEnhanced(RESOURCE_REFERENCE);
        resourceEntry.setValue(RESOURCE_VALUE);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(resourceEntry));


        // WHEN
        changeHelper.removeResourceValuesWithReference(TOPIC, RESOURCE_REFERENCE, asList(LOCALE, FRANCE));


        // THEN
        assertThat(resourceEntry.getItemCount()).isEqualTo(6);
        assertThat(resourceEntry.getItemForLocale(LOCALE)).isEmpty();
        assertThat(resourceEntry.getItemForLocale(FRANCE)).isEmpty();
    }

    @Test
    public void removeResourceValuesWithReference_whenResourceEntryExists_andAllLocalesAffected_shouldDeleteEntry() {
        // GIVEN
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();
        DbResourceEnhancedDto.Entry resourceEntry = resourceObject.addEntryByReference(RESOURCE_REFERENCE)
                .setValue(RESOURCE_VALUE);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(resourceEntry));
        when(minerMock.getResourceEnhancedFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.removeResourceValuesWithReference(TOPIC, RESOURCE_REFERENCE, DbResourceEnhancedDto.Locale.valuesAsStream().collect(toList()));


        // THEN
        assertThat(resourceObject.getEntryByReference(RESOURCE_REFERENCE)).isEmpty();
    }

    @Test
    public void removeResourceValuesWithReference_whenResourceEntryDoesNotExist_shouldNotThrowException() {
        // GIVEN
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(empty());

        // WHEN
        changeHelper.removeResourceValuesWithReference(TOPIC, RESOURCE_REFERENCE, singletonList(LOCALE));

        // THEN: no exception
    }

    private DbResourceEnhancedDto createDefaultResourceObjectEnhanced() {
        return DbResourceEnhancedDto.builder()
                .atVersion("1,0")
                .withCategoryCount(1)
                .build();
    }

    private static DbResourceEnhancedDto.Entry createDefaultResourceEntryEnhanced(String reference) {
        return DbResourceEnhancedDto.Entry.builder()
                .forReference(reference)
                .build()
                .setValueForLocale("", LOCALE);
    }
}