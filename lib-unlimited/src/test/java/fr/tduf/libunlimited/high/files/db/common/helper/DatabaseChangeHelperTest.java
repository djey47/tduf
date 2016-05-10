package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
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
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class DatabaseChangeHelperTest {

    private static final String ENTRY_REFERENCE = "111111";
    private static final String ENTRY_BITFIELD = "79";
    private static final String ENTRY_REFERENCE_BIS = "222222";
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
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
    public void tearDown() {}

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
