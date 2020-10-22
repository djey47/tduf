package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class DatabaseGenHelperTest {

    private static final String ENTRY_REFERENCE = "11111111";
    private static final String RESOURCE_REFERENCE = "11111111";

    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private DatabaseChangeHelper changeHelperMock;

    @InjectMocks
    private DatabaseGenHelper genHelper;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void generateUniqueResourceEntryIdentifier_whenNullTopicObject_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseGenHelper.generateUniqueResourceEntryIdentifier(null)).isNull();
    }

    @Test
    void generateUniqueResourceEntryIdentifier_shouldReturnCorrectIdentifier() {
        // GIVEN
        DbDto topicObject = DbDto.builder()
                .withResource(DbResourceDto.builder()
                        .withCategoryCount(1)
                        .atVersion("1,0")
                        .build())
                .build();
        topicObject.getResource().addEntryByReference(RESOURCE_REFERENCE).setDefaultValue("VAL");

        // WHEN
        String actualResourceIdentifier = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);

        // THEN
        assertThat(actualResourceIdentifier).hasSize(8);
        assertThat(Integer.valueOf(actualResourceIdentifier))
                .isBetween(10000000, 99999999)
                .isNotEqualTo(11111111);
    }

    @Test
    void generateUniqueContentsEntryIdentifier_whenNullTopicObject_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseGenHelper.generateUniqueContentsEntryIdentifier(null)).isNull();
    }

    @Test
    void generateUniqueContentsEntryIdentifier_whenTopicObjectWithoutIdentifierField_shouldThrowIllegalArgumentException() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.INTEGER);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject));
    }

    @Test
    void generateUniqueContentsEntryIdentifier_whenTopicObjectWithIdentifierField_shouldReturnCorrectIdentifier() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneUIDField();

        // WHEN
        String actualEntryIdentifier = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);

        // THEN
        assertThat(actualEntryIdentifier).hasSize(8);
        assertThat(Integer.valueOf(actualEntryIdentifier))
                .isBetween(10000000, 99999999)
                .isNotEqualTo(11111111);
    }

    @Test
    void buildDefaultContentItems_whenOneField_andReference_shouldCreateOneItem() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneUIDField();

        // WHEN
        List<ContentItemDto> actualItems = genHelper.buildDefaultContentItems(ENTRY_REFERENCE, topicObject);

        // THEN
        assertThat(actualItems).hasSize(1);
        assertThat(actualItems).extracting("rawValue").containsExactly(ENTRY_REFERENCE);
    }

    @Test
    void buildDefaultContentItems_whenOneField_andNoReference_shouldCreateOneItemWithDefaultRef() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneUIDField();

        // WHEN
        List<ContentItemDto> actualItems = genHelper.buildDefaultContentItems(null, topicObject);

        // THEN
        assertThat(actualItems).hasSize(1);
        assertThat(actualItems.get(0).getRawValue()).isNotEmpty();
    }

    @Test
    void buildDefaultContentItem_whenNullFieldType_shouldThrowException() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(null);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.BITFIELD);

        // WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> genHelper.buildDefaultContentItem(null, field, topicObject));
    }

    @Test
    void buildDefaultContentItem_whenBitfield_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.BITFIELD);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.BITFIELD);

        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0");
    }

    @Test
    void buildDefaultContentItem_whenFloatingPoint_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.FLOAT);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.FLOAT);

        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0.0");
    }

    @Test
    void buildDefaultContentItem_whenInteger_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.INTEGER);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.INTEGER);

        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0");
    }

    @Test
    void buildDefaultContentItem_whenPercent_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.PERCENT);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.PERCENT);

        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("1");
    }

    @Test
    void buildDefaultContentItem_whenReference_shouldCreateItem_andAddRemoteEntry() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteContent();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.REFERENCE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.UID);

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);


        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        String newContentEntryRef = actualItem.getRawValue();

        verify(changeHelperMock).addContentsEntryWithDefaultItems(newContentEntryRef, BRANDS);
    }

    @Test
    void buildDefaultContentItem_whenResource_shouldCreateItem_andResource() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED);


        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);


        // THEN
        String actualResourceRef = actualItem.getRawValue();
        assertThat(actualResourceRef).isNotEmpty();

        assertThat(actualItem.getFieldRank()).isEqualTo(1);

        assertResourceExistsWithDefaultItem(topicObject);
    }

    @Test
    void buildDefaultContentItem_whenRemoteResource_shouldCreateItem_andResource() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteResource();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);


        // THEN
        String actualResourceRef = actualItem.getRawValue();
        assertThat(actualResourceRef).isNotEmpty();

        assertThat(actualItem.getFieldRank()).isEqualTo(1);

        assertResourceExistsWithDefaultItem(remoteTopicObject);
    }

    @Test
    void buildDefaultContentItem_whenRemoteResource_andTargetGenerationDisabled_shouldNotCreateResource() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteResource();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        ContentItemDto actualItem = genHelper.buildDefaultContentItem(null, field, topicObject);


        // THEN
        assertThat(actualItem).isNotNull();

        verify(changeHelperMock, never()).addResourceValueWithReference(eq(BRANDS), any(fr.tduf.libunlimited.common.game.domain.Locale.class), anyString(), eq("??"));
    }

    @Test
    void generateDefaultResourceReference_whenDefaultResourceEntryExists_shouldReturnIt() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED);
        topicObject.getResource().addEntryByReference("12345").setDefaultValue("??");


        // WHEN
        String actualResourceReference = genHelper.generateDefaultResourceReference(topicObject);


        // THEN
        assertThat(actualResourceReference).isEqualTo("12345");

        verifyNoMoreInteractions(changeHelperMock);
    }

    @Test
    void generateDefaultResourceReference_whenDefaultResourceEntryDoesNotExist_shouldGenerateIt() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED);

        // WHEN
        String actualResourceReference = genHelper.generateDefaultResourceReference(topicObject);

        // THEN
        assertThat(actualResourceReference).isNotEmpty();
        assertResourceExistsWithDefaultItem(topicObject);
    }

    @Test
    void generateUniqueIdentifier() {
        // GIVEN
        Set<String> existingValues = new HashSet<>(Arrays.asList("1", "2", "3"));

        // WHEN
        final String actual = DatabaseGenHelper.generateUniqueIdentifier(existingValues, Range.between(1, 10));

        // THEN
        assertThat(actual).isNotNull();
        assertThat(existingValues).doesNotContain(actual);
    }

    @Test
    void generateUniqueIdentifier_whenNoSpaceLeftInRange_shouldThrowException() {
        // GIVEN
        Set<String> existingValues = new HashSet<>(Arrays.asList("1", "2", "3"));

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseGenHelper.generateUniqueIdentifier(existingValues, Range.between(1, 3)));
    }

    private static DbStructureDto.Field createSingleStructureField(DbStructureDto.FieldType fieldType) {
        return DbStructureDto.Field.builder()
                    .fromType(fieldType)
                    .ofRank(1)
                    .build();
    }

    private static DbStructureDto.Field createStructureFieldForRemoteResource() {
        return DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.RESOURCE_REMOTE)
                .ofRank(1)
                .toTargetReference("TARGET_REF")
                .build();
    }

    private static DbStructureDto.Field createStructureFieldForRemoteContent() {
        return DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.REFERENCE)
                .ofRank(1)
                .toTargetReference("TARGET_REF")
                .build();
    }

    private static DbDto createTopicObjectOneUIDField() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(CAR_PHYSICS_DATA)
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(DbStructureDto.FieldType.UID)
                                .ofRank(1)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .forTopic(CAR_PHYSICS_DATA)
                        .addEntry(ContentEntryDto.builder()
                                .addItem(ContentItemDto.builder()
                                        .ofFieldRank(1)
                                        .withRawValue(ENTRY_REFERENCE)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private static DbDto createTopicObjectOneField(DbStructureDto.FieldType fieldType) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(DbDto.Topic.ACHIEVEMENTS)
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(fieldType)
                                .ofRank(1)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .withResource(DbResourceDto.builder().atVersion("1,0").withCategoryCount(1).build())
                .build();
    }

    private static DbDto createRemoteTopicObjectOneField(DbStructureDto.FieldType fieldType) {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(BRANDS)
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(fieldType)
                                .ofRank(1)
                                .build())
                        .build())
                .withData(DbDataDto.builder().build())
                .withResource(DbResourceDto.builder().atVersion("1,0").withCategoryCount(1).build())
                .build();
    }

    private static void assertResourceExistsWithDefaultItem(DbDto topicObject) {
        final Collection<ResourceEntryDto> actualEntries = topicObject.getResource().getEntries();
        assertThat(actualEntries).hasSize(1);
        final ResourceEntryDto uniqueEntry = actualEntries.stream()
                .findAny()
                .orElseThrow(() -> new IllegalStateException("entry should exist"));
        assertThat(uniqueEntry.getItemCount()).isEqualTo(8);
        assertThat(uniqueEntry.pickValue()).contains("??");
    }
}
