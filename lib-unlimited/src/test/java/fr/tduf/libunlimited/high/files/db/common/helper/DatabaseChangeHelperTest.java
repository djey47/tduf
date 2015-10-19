package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseChangeHelperTest {

    private static final String ENTRY_REFERENCE = "111111";
    private static final String ENTRY_REFERENCE_BIS = "222222";
    private static final String RESOURCE_REFERENCE = "000000";
    private static final String RESOURCE_VALUE = "TEST";
    private static final DbDto.Topic TOPIC = DbDto.Topic.CAR_PHYSICS_DATA;
    private static final DbResourceDto.Locale LOCALE = DbResourceDto.Locale.CHINA;
    private static final String CONTENT_ENTRY_NAME = "TEST";
    private static final String CONTENT_ENTRY_REF_NAME = "REF";

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
        DbStructureDto stuctureObject = createDefaultStructureObject();
        DbDto databaseObject = createDatabaseObject(dataObject, stuctureObject);
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

        DbDto topicObject = createDatabaseObject(dataObject, createDefaultStructureObject());

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
        changeHelper.removeEntryWithIdentifier(1, TOPIC);

        // THEN: NSEE
    }

    @Test
    public void duplicateEntryWithIdentifier_whenEntryExists_shouldDuplicateItAndAddItToTopic() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.getEntries().add(createDefaultContentEntry(0));

        DbDataDto.Entry defaultContentEntry = createDefaultContentEntry(1);
        defaultContentEntry.getItems().add(createDefaultEntryItem());
        dataObject.getEntries().add(defaultContentEntry);

        dataObject.getEntries().add(createDefaultContentEntry(2));

        DbStructureDto stuctureObject = createDefaultStructureObject();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(Optional.of(defaultContentEntry));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(2, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(4);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L, 1L, 2L, 3L);
        assertThat(dataObject.getEntries().get(3).getItems()).extracting("name").containsExactly(CONTENT_ENTRY_NAME);
        assertThat(dataObject.getEntries().get(3).getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    public void duplicateEntryWithIdentifier_whenUidField_shouldGenerateNewRefValueWithinBounds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();

        DbDataDto.Entry defaultContentEntry = createDefaultContentEntry(0);
        defaultContentEntry.getItems().add(createEntryItemForUidField());
        dataObject.getEntries().add(defaultContentEntry);

        DbStructureDto stuctureObject = createStructureObjectWithUidField();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC)).thenReturn(Optional.of(defaultContentEntry));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(Optional.of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(0, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L, 1L);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("name").containsExactly(CONTENT_ENTRY_REF_NAME);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("fieldRank").containsExactly(1);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("rawValue").doesNotContain(ENTRY_REFERENCE);

        Condition<String> betweenMinAndMaxRefValues = new Condition<>((Predicate<String>) o -> {
            int i = Integer.parseInt(o);
            return i >= 10000000 && i <= 99999999;
        }, "between 10000000 and 99999999 (inclusive)");
        assertThat(dataObject.getEntries().get(1).getItems().get(0).getRawValue()).is(betweenMinAndMaxRefValues);
    }

    @Test(expected = NoSuchElementException.class)
    public void duplicateEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.duplicateEntryWithIdentifier(2, TOPIC);

        //THEN
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    public void removeResourceWithReference_whenResourceEntryExists_andSameLocaleAffected_shouldDeleteIt() {
        // GIVEN
        DbResourceDto.Entry resourceEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        DbResourceDto resourceObject = DbResourceDto.builder().addEntry(resourceEntry).build();

        when(minerMock.getResourceFromTopicAndLocale(TOPIC, LOCALE)).thenReturn(Optional.of(resourceObject));


        // WHEN
        changeHelper.removeResourcesWithReference(TOPIC, RESOURCE_REFERENCE, singletonList(LOCALE));


        // THEN
        assertThat(resourceObject.getEntries()).isEmpty();
    }

    @Test
    public void removeResourceWithReference_whenResourceEntryExists_andTwoLocalesAffected_shouldDeleteThem() {
        // GIVEN
        DbResourceDto.Entry chineseResourceEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        DbResourceDto chineseResourceObject = DbResourceDto.builder().addEntry(chineseResourceEntry).build();

        DbResourceDto.Entry frenchResourceEntry = createDefaultResourceEntry(RESOURCE_REFERENCE);
        DbResourceDto frenchResourceObject = DbResourceDto.builder().addEntry(frenchResourceEntry).build();

        when(minerMock.getResourceFromTopicAndLocale(TOPIC, LOCALE)).thenReturn(Optional.of(chineseResourceObject));
        when(minerMock.getResourceFromTopicAndLocale(TOPIC, DbResourceDto.Locale.FRANCE)).thenReturn(Optional.of(frenchResourceObject));


        // WHEN
        changeHelper.removeResourcesWithReference(TOPIC, RESOURCE_REFERENCE, asList(LOCALE, DbResourceDto.Locale.FRANCE));


        // THEN
        assertThat(chineseResourceObject.getEntries()).isEmpty();
        assertThat(frenchResourceObject.getEntries()).isEmpty();
    }

    @Test(expected = NoSuchElementException.class)
    public void removeResourceWithReference_whenResourceEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getResourceFromTopicAndLocale(TOPIC, LOCALE)).thenReturn(Optional.empty());

        // WHEN
        changeHelper.removeResourcesWithReference(TOPIC, RESOURCE_REFERENCE, singletonList(LOCALE));

        // THEN
    }

    @Test
    public void updateAssociationEntryWithSourceAndTargetReferences_whenNoTargetReference_shouldOnlyApplySourceRef() {
        // GIVEN
        DbDataDto.Entry associationEntry = createDefaultContentEntry(0);
        associationEntry.getItems().add(DbDataDto.Item.builder().forName("REF").ofFieldRank(1).build());
        associationEntry.getItems().add(DbDataDto.Item.builder().forName("Other").ofFieldRank(2).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, Optional.empty());

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, null);
    }

    @Test
    public void updateAssociationEntryWithSourceAndTargetReferences_whenTargetReference_shouldApplySourceAndTargetRefs() {
        // GIVEN
        DbDataDto.Entry associationEntry = createDefaultContentEntry(0);
        associationEntry.getItems().add(DbDataDto.Item.builder().forName("REF").ofFieldRank(1).build());
        associationEntry.getItems().add(DbDataDto.Item.builder().forName("REF2").ofFieldRank(2).build());
        associationEntry.getItems().add(DbDataDto.Item.builder().forName("Other").ofFieldRank(3).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, Optional.of(ENTRY_REFERENCE_BIS));

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, ENTRY_REFERENCE_BIS, null);
    }

    @Test(expected = NullPointerException.class)
    public void updateAssociationEntryWithSourceAndTargetReferences_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(null, ENTRY_REFERENCE, Optional.of(ENTRY_REFERENCE_BIS));

        // THEN: NPE
    }

    private static DbDto createDatabaseObject(DbDataDto dataObject, DbStructureDto stuctureObject) {
        return DbDto.builder()
                .withData(dataObject)
                .withStructure(stuctureObject)
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

    private static DbDataDto.Item createDefaultEntryItem() {
        return DbDataDto.Item.builder()
                .ofFieldRank(1)
                .forName(CONTENT_ENTRY_NAME)
                .build();
    }

    private static DbDataDto.Item createEntryItemForUidField() {
        return DbDataDto.Item.builder()
                .ofFieldRank(1)
                .forName(CONTENT_ENTRY_REF_NAME)
                .withRawValue(ENTRY_REFERENCE)
                .build();

    }

    private static DbResourceDto.Entry createDefaultResourceEntry(String reference) {
        return DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue("")
                .build();
    }

    private static DbStructureDto createDefaultStructureObject() {
        return DbStructureDto.builder().build();
    }

    private static DbStructureDto createStructureObjectWithUidField() {
        return DbStructureDto.builder()
                .forTopic(TOPIC)
                .addItem(DbStructureDto.Field.builder()
                        .forName(CONTENT_ENTRY_REF_NAME)
                        .fromType(DbStructureDto.FieldType.UID)
                        .ofRank(1)
                        .build()
                )
                .build();
    }
}