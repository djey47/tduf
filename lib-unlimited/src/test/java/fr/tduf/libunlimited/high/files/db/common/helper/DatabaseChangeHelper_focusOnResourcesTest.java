package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
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
    private static final Locale LOCALE = Locale.CHINA;

    @Mock
    DatabaseGenHelper genHelperMock;

    @Mock
    BulkDatabaseMiner minerMock;

    @InjectMocks
    DatabaseChangeHelper changeHelper;

    @After
    public void tearDown() {}

    @Test
    public void addResourceWithReference_andNonExistingEntry_shouldCreateNewResourceEntry() throws Exception {
        // GIVEN
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
    }

    @Test
    public void addResourceWithReference_andExistingEntry_shouldCreateNewResourceItem() throws Exception {
        // GIVEN
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference(RESOURCE_REFERENCE)
                .setValueForLocale("", FRANCE);

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
        final ResourceEntryDto actualEntry = resourceObject.getEntryByReference(RESOURCE_REFERENCE).get();
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

    @Test(expected = IllegalStateException.class)
    public void addResourceValueWithReference_andNoResource_shouldThrowException() throws Exception {
        // GIVEN
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: NSEE
    }

    @Test
    public void updateResourceItemWithReference_whenExistingEntry_shouldReplaceReferenceAndValue() {
        // GIVEN
        String initialReference = "0";
        String initialValue = "";
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        ResourceEntryDto resourceEntry = createDefaultResourceEntryEnhanced(initialReference);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(resourceEntry));
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(empty());
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(initialReference, TOPIC, LOCALE)).thenReturn(of(initialValue));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntryByReference(initialReference)).isEmpty();
        final Optional<ResourceEntryDto> potentialEntry = resourceObject.getEntryByReference(RESOURCE_REFERENCE);
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
    public void updateResourceItemWithReference_whenEntryExistsWithNewReference_shouldThrowException_andKeepOriginalResource() {
        // GIVEN
        String initialReference = "0";
        String existingValue = "e";

        ResourceEntryDto existingEntry = createDefaultResourceEntryEnhanced(initialReference);
        ResourceEntryDto existingEntry2 = createDefaultResourceEntryEnhanced(initialReference);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(existingEntry));
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(existingEntry2));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(of(existingValue));


        // WHEN-THEN
        try {
            changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);
        } catch (IllegalArgumentException iae) {
            assertThat(existingEntry.pickValue()).isPresent();
            throw iae;
        }
    }

    @Test
    public void removeResourceValuesWithReference_whenResourceEntryExists_andSameLocaleAffected_shouldDeleteLocalizedValue() {
        // GIVEN
        ResourceEntryDto resourceEntry = createDefaultResourceEntryEnhanced(RESOURCE_REFERENCE);
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
        ResourceEntryDto resourceEntry = createDefaultResourceEntryEnhanced(RESOURCE_REFERENCE);
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
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        ResourceEntryDto resourceEntry = resourceObject.addEntryByReference(RESOURCE_REFERENCE)
                .setValue(RESOURCE_VALUE);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(resourceEntry));
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.removeResourceValuesWithReference(TOPIC, RESOURCE_REFERENCE, Locale.valuesAsStream().collect(toList()));


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

    private DbResourceDto createDefaultResourceObjectEnhanced() {
        return DbResourceDto.builder()
                .atVersion("1,0")
                .withCategoryCount(1)
                .build();
    }

    private static ResourceEntryDto createDefaultResourceEntryEnhanced(String reference) {
        return ResourceEntryDto.builder()
                .forReference(reference)
                .build()
                .setValueForLocale("", LOCALE);
    }
}
