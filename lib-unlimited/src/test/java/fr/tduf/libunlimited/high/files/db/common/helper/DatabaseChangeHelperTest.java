package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.*;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto.Locale.FRANCE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
// TODO create dedicated test class for resources
public class DatabaseChangeHelperTest {

    private static final String ENTRY_REFERENCE = "111111";
    private static final String ENTRY_BITFIELD = "79";
    private static final String ENTRY_REFERENCE_BIS = "222222";
    private static final String RESOURCE_REFERENCE = "000000";
    private static final String RESOURCE_VALUE = "TEST";
    private static final DbDto.Topic TOPIC = DbDto.Topic.CAR_PHYSICS_DATA;
    private static final DbResourceEnhancedDto.Locale LOCALE = DbResourceEnhancedDto.Locale.CHINA;
    private static final String CONTENT_ENTRY_NAME = "TEST";
    private static final String CONTENT_ENTRY_REF_NAME = "REF";
    private static final String CONTENT_ENTRY_BIFIELD_NAME = "BITFIELD";

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
    public void addContentsEntryWithDefaultItems_whenTopicObjectAvailable_shouldCreateAndReturnIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        DbStructureDto stuctureObject = createDefaultStructureObject();
        DbDto databaseObject = createDatabaseObject(dataObject, stuctureObject);
        List<DbDataDto.Item> contentItems = new ArrayList<>();
        contentItems.add(DbDataDto.Item.builder()
                .ofFieldRank(1)
                .build());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(databaseObject));
        when(genHelperMock.buildDefaultContentItems(of(ENTRY_REFERENCE), databaseObject)).thenReturn(contentItems);


        // WHEN
        DbDataDto.Entry actualEntry = changeHelper.addContentsEntryWithDefaultItems(of(ENTRY_REFERENCE), TOPIC);


        // THEN
        assertThat(actualEntry).isNotNull();
        assertThat(actualEntry.getId()).isZero();
        assertThat(actualEntry.getItems()).hasSize(1);
        assertThat(actualEntry.getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void addContentsEntryWithDefaultItems_whenTopicObjectUnavailable_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.addContentsEntryWithDefaultItems(of(ENTRY_REFERENCE), TOPIC);

        // THEN: NSEE
        verifyZeroInteractions(genHelperMock);
    }

    @Test
    public void updateResourceItemWithReference_whenExistingEntry_shouldReplaceReferenceAndValue() {
        // GIVEN
        String initialReference = "0";
        String initialValue = "";
        DbResourceEnhancedDto resourceObject = createDefaultResourceObjectEnhanced();
        DbResourceEnhancedDto.Entry resourceEntry = createDefaultResourceEntryEnhanced(initialReference);

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(initialReference, TOPIC, LOCALE)).thenReturn(of(initialValue));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());
        when(minerMock.getResourceEntryFromTopicAndReference(TOPIC, initialReference)).thenReturn(of(resourceEntry));
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
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(empty());

        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, RESOURCE_REFERENCE, RESOURCE_REFERENCE, RESOURCE_VALUE);

        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateResourceItemWithReference_whenEntryExistsWithNewReference_shouldThrowException() {
        // GIVEN
        String initialReference = "0";
        String initialValue = "i";
        String existingValue = "e";

        when(minerMock.getLocalizedResourceValueFromTopicAndReference(initialReference, TOPIC, LOCALE)).thenReturn(of(initialValue));
        when(minerMock.getLocalizedResourceValueFromTopicAndReference(RESOURCE_REFERENCE, TOPIC, LOCALE)).thenReturn(of(existingValue));


        // WHEN
        changeHelper.updateResourceItemWithReference(TOPIC, LOCALE, initialReference, RESOURCE_REFERENCE, RESOURCE_VALUE);


        // THEN: IAE
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    public void removeEntryWithIdentifier_whenEntryExists_shouldDeleteIt_andUpdateIds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.addEntry(createDefaultContentEntry(1));
        dataObject.addEntry(createDefaultContentEntry(2));
        dataObject.addEntry(createDefaultContentEntry(3));

        DbDto topicObject = createDatabaseObject(dataObject, createDefaultStructureObject());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.removeEntryWithIdentifier(2, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(1L, 2L);
    }

    @Test(expected = NoSuchElementException.class)
    public void removeEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.removeEntryWithIdentifier(1, TOPIC);

        // THEN: NSEE
    }

    @Test
    public void removeEntryWithReference_whenEntryExists_shouldDeleteIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        DbDataDto.Entry contentEntryWithUidItem = createContentEntryWithUidItem(1);
        dataObject.addEntry(contentEntryWithUidItem);

        DbDto topicObject = createDatabaseObject(dataObject, createDefaultStructureObject());

        when(minerMock.getContentEntryFromTopicWithReference("111111", TOPIC)).thenReturn(of(contentEntryWithUidItem));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.removeEntryWithReference("111111", TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).isEmpty();
    }

    @Test
    public void removeEntryWithReference_whenEntryDoesNotExist_shouldDoNothing() {
        // GIVEN
        when(minerMock.getContentEntryFromTopicWithReference("111111", TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.removeEntryWithReference("111111", TOPIC);

        // THEN
    }

    @Test
    public void removeEntriesMatchingCriteria_whenOneEntryMatches_shouldDeleteIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        DbDataDto.Entry contentEntryWithUidItem = createContentEntryWithUidItem(1);
        dataObject.addEntry(contentEntryWithUidItem);

        DbDto topicObject = createDatabaseObject(dataObject, createDefaultStructureObject());

        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(1, "111111"));

        when(minerMock.getContentEntryStreamMatchingCriteria(eq(criteria), eq(TOPIC))).thenReturn(Stream.of(contentEntryWithUidItem));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.removeEntriesMatchingCriteria(criteria, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).isEmpty();
    }

    @Test
    public void removeEntriesMatchingCriteria_whenNoEntryMatches_shouldDoNothing() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        DbDataDto.Entry contentEntryWithUidItem = createContentEntryWithUidItem(1);
        dataObject.addEntry(contentEntryWithUidItem);

        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(1, "111111"));

        when(minerMock.getContentEntryStreamMatchingCriteria(eq(criteria), eq(TOPIC))).thenReturn(Stream.empty());


        // WHEN
        changeHelper.removeEntriesMatchingCriteria(criteria, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).hasSize(1);
    }

    @Test
    public void duplicateEntryWithIdentifier_whenEntryExists_shouldDuplicateItAndAddItToTopic() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.addEntry(createDefaultContentEntry(0));

        DbDataDto.Entry defaultContentEntry = createDefaultContentEntry(1);
        defaultContentEntry.appendItem(createDefaultEntryItem());
        dataObject.addEntry(defaultContentEntry);

        dataObject.addEntry(createDefaultContentEntry(2));

        DbStructureDto stuctureObject = createDefaultStructureObject();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(of(defaultContentEntry));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(2, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(4);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L, 1L, 2L, 3L);
        assertThat(dataObject.getEntries().get(3).getItems()).extracting("name").containsExactly(CONTENT_ENTRY_NAME);
        assertThat(dataObject.getEntries().get(3).getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    public void duplicateEntryWithIdentifier_whenEntryExists_withSignleBitfieldItem_shouldDuplicateItAndAddItToTopic() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();

        DbDataDto.Entry defaultContentEntry = createDefaultContentEntry(0);
        defaultContentEntry.appendItem(createEntryItemForBitField());
        dataObject.addEntry(defaultContentEntry);

        DbStructureDto stuctureObject = createStructureObjectWithBitField();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC)).thenReturn(of(defaultContentEntry));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(0, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L, 1L);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("name").containsExactly(CONTENT_ENTRY_BIFIELD_NAME);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    public void duplicateEntryWithIdentifier_whenUidField_shouldGenerateNewRefValueWithinBounds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();

        DbDataDto.Entry defaultContentEntry = createDefaultContentEntry(0);
        defaultContentEntry.appendItem(createEntryItemForUidField());
        dataObject.addEntry(defaultContentEntry);

        DbStructureDto stuctureObject = createStructureObjectWithUidField();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, TOPIC)).thenReturn(of(defaultContentEntry));
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


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
        assertThat(dataObject.getEntries().get(1).getItemAtRank(1).get().getRawValue()).is(betweenMinAndMaxRefValues);
    }

    @Test(expected = NoSuchElementException.class)
    public void duplicateEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.duplicateEntryWithIdentifier(2, TOPIC);

        // THEN
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    public void moveEntryWithIdentifier_whenEntryExists_andStepNotInRange_shouldDoNothing() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.addEntry(createDefaultContentEntry(0));
        dataObject.addEntry(createDefaultContentEntry(1));
        final DbDataDto.Entry movedEntry = createDefaultContentEntry(2);
        dataObject.addEntry(movedEntry);
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.moveEntryWithIdentifier(-3, 2, TOPIC);


        // THEN
        assertThat(movedEntry.getId()).isEqualTo(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L,1L,2L);
    }

    @Test
    public void moveEntryWithIdentifier_whenEntryExists_andStepInRange_shouldUpdateEntryRank() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        dataObject.addEntry(createDefaultContentEntry(0));
        dataObject.addEntry(createDefaultContentEntry(1));
        final DbDataDto.Entry movedEntry = createDefaultContentEntry(2);
        dataObject.addEntry(movedEntry);
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(of(movedEntry));


        // WHEN
        changeHelper.moveEntryWithIdentifier(-2, 2, TOPIC);


        // THEN
        assertThat(movedEntry.getId()).isEqualTo(0);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0L, 1L, 2L);
    }

    @Test(expected = NoSuchElementException.class)
    public void moveEntryWithIdentifier_whenTopicDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.moveEntryWithIdentifier(0, 2, TOPIC);

        // THEN
        verifyNoMoreInteractions(minerMock);
    }

    @Test(expected = NoSuchElementException.class)
    public void moveEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        DbDto topicObject = DbDto.builder().withData(createDefaultDataObject()).build();

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(empty());


        // WHEN
        changeHelper.moveEntryWithIdentifier(-2, 2, TOPIC);


        // THEN
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

    @Test
    public void updateAssociationEntryWithSourceAndTargetReferences_whenNoTargetReference_shouldOnlyApplySourceRef() {
        // GIVEN
        DbDataDto.Entry associationEntry = createDefaultContentEntry(0);
        associationEntry.appendItem(DbDataDto.Item.builder().forName("REF").ofFieldRank(1).build());
        associationEntry.appendItem(DbDataDto.Item.builder().forName("Other").ofFieldRank(2).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, empty());

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, null);
    }

    @Test
    public void updateAssociationEntryWithSourceAndTargetReferences_whenTargetReference_shouldApplySourceAndTargetRefs() {
        // GIVEN
        DbDataDto.Entry associationEntry = createDefaultContentEntry(0);
        associationEntry.appendItem(DbDataDto.Item.builder().forName("REF").ofFieldRank(1).build());
        associationEntry.appendItem(DbDataDto.Item.builder().forName("REF2").ofFieldRank(2).build());
        associationEntry.appendItem(DbDataDto.Item.builder().forName("Other").ofFieldRank(3).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, of(ENTRY_REFERENCE_BIS));

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, ENTRY_REFERENCE_BIS, null);
    }

    @Test(expected = NullPointerException.class)
    public void updateAssociationEntryWithSourceAndTargetReferences_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(null, ENTRY_REFERENCE, of(ENTRY_REFERENCE_BIS));

        // THEN: NPE
    }

    @Test
    public void updateItemRawValueAtIndexAndFieldRank_whenRawValueUnchanged_shouldReturnEmpty() {
        // GIVEN
        DbDataDto.Item item = createEntryItemForBitField();
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC, 1, 1)).thenReturn(of(item));

        // WHEN
        Optional<DbDataDto.Item> updatedItem = changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, ENTRY_BITFIELD);

        // THEN
        assertThat(updatedItem).isEmpty();
    }

    @Test
    public void updateItemRawValueAtIndexAndFieldRank_whenRawValueChanged_shouldReturnUpdatedItem() {
        // GIVEN
        DbDataDto.Item item = createEntryItemForBitField();
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC, 1, 1)).thenReturn(of(item));

        // WHEN
        Optional<DbDataDto.Item> updatedItem = changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, "NEW_VALUE");

        // THEN
        assertThat(updatedItem).isPresent();
        assertThat(updatedItem.get().getRawValue()).isEqualTo("NEW_VALUE");
    }

    @Test(expected = NoSuchElementException.class)
    public void updateItemRawValueAtIndexAndFieldRank_whenItemDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC, 1, 1)).thenReturn(empty());

        // WHEN
        changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, "NEW_VALUE");

        // THEN: NSE
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

    private static DbDataDto.Entry createContentEntryWithUidItem(long internalId) {
        return DbDataDto.Entry.builder()
                .forId(internalId)
                .addItem(createEntryItemForUidField())
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

    private static DbDataDto.Item createEntryItemForBitField() {
        return DbDataDto.Item.builder()
                .ofFieldRank(1)
                .forName(CONTENT_ENTRY_BIFIELD_NAME)
                .withRawValue(ENTRY_BITFIELD)
                .bitFieldForTopic(true, TOPIC)
                .build();
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

    private static DbStructureDto createStructureObjectWithBitField() {
        return DbStructureDto.builder()
                .forTopic(TOPIC)
                .addItem(DbStructureDto.Field.builder()
                        .forName(CONTENT_ENTRY_BIFIELD_NAME)
                        .fromType(DbStructureDto.FieldType.BITFIELD)
                        .ofRank(1)
                        .build()
                )
                .build();
    }
}
