package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


class DatabaseChangeHelperTest {

    private static final String ENTRY_REFERENCE = "111111";
    private static final String ENTRY_BITFIELD = "79";
    private static final String ENTRY_REFERENCE_BIS = "222222";
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final String CONTENT_ENTRY_REF_NAME = "REF";
    private static final String CONTENT_ENTRY_BIFIELD_NAME = "BITFIELD";

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
    void addContentsEntryWithDefaultItems_whenTopicObjectAvailable_shouldCreateAndReturnIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        DbStructureDto stuctureObject = createDefaultStructureObject();
        DbDto databaseObject = createDatabaseObject(dataObject, stuctureObject);
        List<ContentItemDto> contentItems = new ArrayList<>();
        contentItems.add(ContentItemDto.builder()
                .ofFieldRank(1)
                .build());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(databaseObject));
        when(genHelperMock.buildDefaultContentItems(of(ENTRY_REFERENCE), databaseObject)).thenReturn(contentItems);


        // WHEN
        ContentEntryDto actualEntry = changeHelper.addContentsEntryWithDefaultItems(ENTRY_REFERENCE, TOPIC);


        // THEN
        assertThat(actualEntry).isNotNull();
        assertThat(actualEntry.getId()).isZero();
        assertThat(actualEntry.getItems()).hasSize(1);
        assertThat(actualEntry.getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    void addContentsEntryWithDefaultItems_whenTopicObjectUnavailable_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // GIVEN-WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.addContentsEntryWithDefaultItems(ENTRY_REFERENCE, TOPIC));
        verifyZeroInteractions(genHelperMock);
    }

    @Test
    void removeEntryWithIdentifier_whenEntryExists_shouldDeleteIt_andUpdateIds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto entry1 = createDefaultContentEntry();
        entry1.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry1);
        ContentEntryDto entry2 = createDefaultContentEntry();
        entry2.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry2);
        ContentEntryDto entry3 = createDefaultContentEntry();
        entry3.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry3);

        DbDto topicObject = createDatabaseObject(dataObject, createDefaultStructureObject());

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.removeEntryWithIdentifier(2, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0, 1);
    }

    @Test
    void removeEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.removeEntryWithIdentifier(1, TOPIC));
    }

    @Test
    void removeEntryWithReference_whenEntryExists_shouldDeleteIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto contentEntryWithUidItem = createContentEntryWithUidItem();
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
    void removeEntryWithReference_whenEntryDoesNotExist_shouldDoNothing() {
        // GIVEN
        when(minerMock.getContentEntryFromTopicWithReference("111111", TOPIC)).thenReturn(empty());

        // WHEN
        changeHelper.removeEntryWithReference("111111", TOPIC);

        // THEN
    }

    @Test
    void removeEntriesMatchingCriteria_whenOneEntryMatches_shouldDeleteIt() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto contentEntryWithUidItem = createContentEntryWithUidItem();
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
    void removeEntriesMatchingCriteria_whenNoEntryMatches_shouldDoNothing() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto contentEntryWithUidItem = createContentEntryWithUidItem();
        dataObject.addEntry(contentEntryWithUidItem);

        List<DbFieldValueDto> criteria = singletonList(DbFieldValueDto.fromCouple(1, "111111"));

        when(minerMock.getContentEntryStreamMatchingCriteria(eq(criteria), eq(TOPIC))).thenReturn(Stream.empty());


        // WHEN
        changeHelper.removeEntriesMatchingCriteria(criteria, TOPIC);


        // THEN
        assertThat(dataObject.getEntries()).hasSize(1);
    }

    @Test
    void duplicateEntryWithIdentifier_whenEntryExists_shouldDuplicateItAndAddItToTopic() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto entry0 = createDefaultContentEntry();
        entry0.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry0);

        ContentEntryDto defaultContentEntry = createDefaultContentEntry();
        defaultContentEntry.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(defaultContentEntry);

        ContentEntryDto entryToBeCloned = createDefaultContentEntry();
        entryToBeCloned.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entryToBeCloned);

        DbStructureDto stuctureObject = createDefaultStructureObject();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(2, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(4);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0, 1, 2, 3);
        assertThat(dataObject.getEntries().get(3).getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    void duplicateEntryWithIdentifier_whenEntryExists_withSingleBitfieldItem_shouldDuplicateItAndAddItToTopic() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();

        ContentEntryDto defaultContentEntry = createDefaultContentEntry();
        defaultContentEntry.appendItem(createEntryItemForBitField());
        dataObject.addEntry(defaultContentEntry);

        DbStructureDto stuctureObject = createStructureObjectWithBitField();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        changeHelper.duplicateEntryWithIdentifier(0, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0, 1);
        assertThat(dataObject.getEntries().get(1).getItems()).extracting("fieldRank").containsExactly(1);
    }

    @Test
    void duplicateEntryWithIdentifier_whenUidField_shouldGenerateNewRefValueWithinBounds() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();

        ContentEntryDto defaultContentEntry = createDefaultContentEntry();
        defaultContentEntry.appendItem(createEntryItemForUidField());
        dataObject.addEntry(defaultContentEntry);
        ContentEntryDto cloneContentEntry = createDefaultContentEntry();
        cloneContentEntry.appendItem(createEntryItemForUidField());

        DbStructureDto stuctureObject = createStructureObjectWithUidField();

        DbDto topicObject = createDatabaseObject(dataObject, stuctureObject);

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));


        // WHEN
        ContentEntryDto actualCloneEntry = changeHelper.duplicateEntryWithIdentifier(0, TOPIC);


        //THEN
        assertThat(dataObject.getEntries()).hasSize(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0, 1);

        String cloneEntryReference = actualCloneEntry.getItemAtRank(1).get().getRawValue();
        Condition<String> betweenMinAndMaxRefValues = new Condition<>(o -> {
            int i = Integer.parseInt(o);
            return i >= 10000000 && i <= 99999999;
        }, "between 10000000 and 99999999 (inclusive)");

        assertThat(cloneEntryReference).is(betweenMinAndMaxRefValues);

        assertThat(dataObject.getEntryWithReference(ENTRY_REFERENCE)).contains(defaultContentEntry);
        assertThat(dataObject.getEntryWithReference(cloneEntryReference)).contains(actualCloneEntry);

        assertThat(actualCloneEntry.getItems()).extracting("fieldRank").containsExactly(1);
        assertThat(actualCloneEntry.getItems()).extracting("rawValue").containsExactly(cloneEntryReference);
    }

    @Test
    void duplicateEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(createDatabaseObject(createDefaultDataObject(), createDefaultStructureObject())));

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.duplicateEntryWithIdentifier(2, TOPIC));
        verify(minerMock).getDatabaseTopic(any(DbDto.Topic.class));
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    void moveEntryWithIdentifier_whenEntryExists_andStepNotInRange_shouldDoNothing() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto entry0 = createDefaultContentEntry();
        entry0.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry0);
        ContentEntryDto entry1 = createDefaultContentEntry();
        entry1.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry1);
        final ContentEntryDto movedEntry = createDefaultContentEntry();
        movedEntry.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(movedEntry);


        // WHEN
        changeHelper.moveEntryWithIdentifier(-3, 2, TOPIC);


        // THEN
        assertThat(movedEntry.getId()).isEqualTo(2);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0,1,2);
    }

    @Test
    void moveEntryWithIdentifier_whenEntryExists_andStepInRange_shouldUpdateEntryRank() {
        // GIVEN
        DbDataDto dataObject = createDefaultDataObject();
        ContentEntryDto entry0 = createDefaultContentEntry();
        entry0.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry0);
        ContentEntryDto entry1 = createDefaultContentEntry();
        entry1.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(entry1);
        final ContentEntryDto movedEntry = createDefaultContentEntry();
        movedEntry.appendItem(createEntryItemAtRank(1));
        dataObject.addEntry(movedEntry);
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(of(movedEntry));


        // WHEN
        changeHelper.moveEntryWithIdentifier(-2, 2, TOPIC);


        // THEN
        assertThat(movedEntry.getId()).isEqualTo(0);
        assertThat(dataObject.getEntries()).extracting("id").containsExactly(0, 1, 2);
    }

    @Test
    void moveEntryWithIdentifier_whenTopicDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(empty());

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.moveEntryWithIdentifier(1, 2, TOPIC));
        verify(minerMock.getDatabaseTopic(any(DbDto.Topic.class)));
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    void moveEntryWithIdentifier_whenEntryDoesNotExist_shouldThrowException() {
        // GIVEN
        DbDto topicObject = DbDto.builder().withData(createDefaultDataObject()).build();

        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(topicObject));
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(2, TOPIC)).thenReturn(empty());


        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> changeHelper.moveEntryWithIdentifier(-2, 2, TOPIC));
        verify(minerMock).getDatabaseTopic(any(DbDto.Topic.class));
        verify(minerMock).getContentEntryFromTopicWithInternalIdentifier(anyInt(), any(DbDto.Topic.class));
        verifyNoMoreInteractions(minerMock);
    }

    @Test
    void updateAssociationEntryWithSourceAndTargetReferences_whenNoTargetReference_shouldOnlyApplySourceRef() {
        // GIVEN
        ContentEntryDto associationEntry = createDefaultContentEntry();
        associationEntry.appendItem(ContentItemDto.builder().ofFieldRank(1).build());
        associationEntry.appendItem(ContentItemDto.builder().ofFieldRank(2).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, empty());

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, null);
    }

    @Test
    void updateAssociationEntryWithSourceAndTargetReferences_whenTargetReference_shouldApplySourceAndTargetRefs() {
        // GIVEN
        ContentEntryDto associationEntry = createDefaultContentEntry();
        associationEntry.appendItem(ContentItemDto.builder().ofFieldRank(1).build());
        associationEntry.appendItem(ContentItemDto.builder().ofFieldRank(2).build());
        associationEntry.appendItem(ContentItemDto.builder().ofFieldRank(3).build());

        // WHEN
        DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(associationEntry, ENTRY_REFERENCE, of(ENTRY_REFERENCE_BIS));

        // THEN
        assertThat(associationEntry.getItems()).extracting("rawValue").containsExactly(ENTRY_REFERENCE, ENTRY_REFERENCE_BIS, null);
    }

    @Test
    void updateAssociationEntryWithSourceAndTargetReferences_whenNullEntry_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> DatabaseChangeHelper.updateAssociationEntryWithSourceAndTargetReferences(null, ENTRY_REFERENCE, of(ENTRY_REFERENCE_BIS)));
    }

    @Test
    void updateItemRawValueAtIndexAndFieldRank_whenRawValueUnchanged_shouldReturnEmpty() {
        // GIVEN
        ContentItemDto item = createEntryItemForBitField();
        ContentEntryDto entry = createDefaultContentEntry();
        entry.appendItem(item);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1, TOPIC)).thenReturn(of(entry));

        // WHEN
        Optional<ContentItemDto> updatedItem = changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, ENTRY_BITFIELD);

        // THEN
        assertThat(updatedItem).isEmpty();
    }

    @Test
    void updateItemRawValueAtIndexAndFieldRank_whenRawValueChanged_shouldReturnUpdatedItem() {
        // GIVEN
        ContentItemDto item = createEntryItemForBitField();
        ContentEntryDto entry = createDefaultContentEntry();
        entry.appendItem(item);

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1, TOPIC)).thenReturn(of(entry));


        // WHEN
        Optional<ContentItemDto> updatedItem = changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, "80");


        // THEN
        assertThat(updatedItem).isPresent();
        assertThat(updatedItem.get().getRawValue()).isEqualTo("80");
    }

    @Test
    void updateItemRawValueAtIndexAndFieldRank_whenItemDoesNotExist_shouldThrowException() {
        // GIVEN
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(1, TOPIC)).thenReturn(of(createDefaultContentEntry()));

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> changeHelper.updateItemRawValueAtIndexAndFieldRank(TOPIC, 1, 1, "NEW_VALUE"));
    }

    private static DbDto createDatabaseObject(DbDataDto dataObject, DbStructureDto stuctureObject) {
        return DbDto.builder()
                .withData(dataObject)
                .withStructure(stuctureObject)
                .build();
    }

    private static DbDataDto createDefaultDataObject() {
        return DbDataDto.builder()
                .forTopic(TOPIC)
                .build();
    }

    private static ContentEntryDto createDefaultContentEntry() {
        return ContentEntryDto.builder().build();
    }

    private static ContentEntryDto createContentEntryWithUidItem() {
        return ContentEntryDto.builder()
                .addItem(createEntryItemForUidField())
                .build();
    }

    private static ContentItemDto createEntryItemAtRank(int rank) {
        return ContentItemDto.builder()
                .ofFieldRank(rank)
                .build();
    }

    private static ContentItemDto createEntryItemForUidField() {
        return ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue(ENTRY_REFERENCE)
                .build();
    }

    private static ContentItemDto createEntryItemForBitField() {
        return ContentItemDto.builder()
                .ofFieldRank(1)
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
