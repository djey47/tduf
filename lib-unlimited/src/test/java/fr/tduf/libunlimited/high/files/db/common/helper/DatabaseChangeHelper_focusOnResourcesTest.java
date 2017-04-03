package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


class DatabaseChangeHelper_focusOnResourcesTest {

    private static final String RESOURCE_REFERENCE = "000000";
    private static final String RESOURCE_VALUE = "TEST";
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final Locale LOCALE = Locale.CHINA;

    // Kept for automatic injection in changeHelper
    @Mock
    private DatabaseGenHelper genHelperMock;

    @Mock
    private BulkDatabaseMiner minerMock;

    @InjectMocks
    private DatabaseChangeHelper changeHelper;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void addResourceWithReference_andNonExistingEntry_shouldCreateNewResourceEntry() throws Exception {
        // GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntries()).hasSize(1);
        assertThat(resourceObject.getEntries()).extracting("reference").containsExactly(RESOURCE_REFERENCE);
    }

    @Test
    void addResourceWithReference_andExistingEntry_shouldCreateNewResourceItem() throws Exception {
        // GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();
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

    @Test
    void addResourceWithReference_andExisting_shouldThrowException() throws Exception {
        // GIVEN
        String resourceValue = "TEST2";
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(of(resourceValue));

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE));
        verify(minerMock).getLocalizedResourceValueFromTopicAndReference(anyString(), any(DbDto.Topic.class), any(Locale.class));
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    void addResourceValueWithReference_andNoResource_shouldThrowException() throws Exception {
        // GIVEN
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.addResourceValueWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE));
    }

    @Test
    void updateResourceItemWithReference_whenNonexistingEntry_shouldThrowException() {
        // GIVEN
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE));
        verify(minerMock).getResourceEntryFromTopicAndReference(any(DbDto.Topic.class), anyString());
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    void updateResourceItemWithReference_whenExistingEntry_shouldChangeValue() {
        // GIVEN
        ResourceEntryDto resourceEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REFERENCE)).thenReturn(of(resourceEntry));

        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN
        assertThat(resourceEntry.getValueForLocale(LOCALE)).contains(RESOURCE_VALUE);
    }

    @Test
    void updateResourceEntryWithReference_whenExistingEntry_shouldReplaceReferenceAndValue() {
        // GIVEN
        String initialReference = "0";
        DbResourceDto resourceObject = createDefaultResourceObject();
        ResourceEntryDto resourceEntry = createDefaultResourceEntry(initialReference);

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(resourceEntry));
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.updateResourceEntryWithReference(TOPIC, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN
        assertThat(resourceObject.getEntryByReference(initialReference)).isEmpty();
        final Optional<ResourceEntryDto> potentialEntry = resourceObject.getEntryByReference(RESOURCE_REFERENCE);
        assertThat(potentialEntry
                .flatMap(e -> e.getValueForLocale(LOCALE)))
                .contains(RESOURCE_VALUE);
    }

    @Test
    void updateResourceEntryWithReference_whenEntryExistsWithNewReference_shouldThrowException_andKeepOriginalResource() {
        // GIVEN
        String initialReference = "1";

        ResourceEntryDto existingEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        ResourceEntryDto existingEntry2 = createDefaultResourceEntry(initialReference);
        DbResourceDto resourceObject = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(asList(existingEntry, existingEntry2)).build();

        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(existingEntry));
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> changeHelper.updateResourceEntryWithReference(TOPIC, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE));
        assertThat(existingEntry.pickValue()).isPresent();
    }

    @Test
    void removeResourceEntryWithReference_whenResourceEntryExists_shouldDeleteIt() {
        // GIVEN
        ResourceEntryDto resourceEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        setValuesForAllLocales(resourceEntry);

        DbResourceDto resourceObject = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(singletonList(resourceEntry))
                .build();
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(resourceObject));


        // WHEN
        changeHelper.removeResourceEntryWithReference(TOPIC, RESOURCE_REFERENCE);


        // THEN
        assertThat(resourceObject.getEntries()).isEmpty();
    }

    @Test
    void removeResourceValuesWithReference_whenResourceEntryDoesNotExist_shouldNotThrowException() {
        // GIVEN
        when(minerMock.getResourcesFromTopic(TOPIC)).thenReturn(of(createDefaultResourceObject()));

        // WHEN
        changeHelper.removeResourceEntryWithReference(TOPIC, RESOURCE_REFERENCE);

        // THEN: no exception
    }

    private DbResourceDto createDefaultResourceObject() {
        return DbResourceDto.builder()
                .atVersion("1,0")
                .withCategoryCount(1)
                .build();
    }

    private static ResourceEntryDto createDefaultResourceEntry(String reference) {
        return ResourceEntryDto.builder()
                .forReference(reference)
                .build()
                .setValueForLocale("", LOCALE);
    }

    private static void setValuesForAllLocales(ResourceEntryDto resourceEntry) {
        resourceEntry.setValueForLocale("VAL1", LOCALE);
        resourceEntry.setValueForLocale("VAL2", FRANCE);
        resourceEntry.setValueForLocale("VAL3", GERMANY);
        resourceEntry.setValueForLocale("VAL4", ITALY);
        resourceEntry.setValueForLocale("VAL5", JAPAN);
        resourceEntry.setValueForLocale("VAL6", KOREA);
        resourceEntry.setValueForLocale("VAL7", SPAIN);
        resourceEntry.setValueForLocale("VAL8", UNITED_STATES);
    }
}
