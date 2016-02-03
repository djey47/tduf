package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseGenHelperTest {

    private static final String ENTRY_REFERENCE = "11111111";
    private static final String RESOURCE_REFERENCE = "11111111";

    @Mock
    BulkDatabaseMiner minerMock;

    @Mock
    DatabaseChangeHelper changeHelperMock;

    @InjectMocks
    DatabaseGenHelper genHelper;

    @Test
    public void generateUniqueResourceEntryIdentifier_whenNullTopicObject_shouldReturnNull() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseGenHelper.generateUniqueResourceEntryIdentifier(null)).isNull();
    }

    @Test
    public void generateUniqueResourceEntryIdentifier_shouldReturnCorrectIdentifier() throws Exception {
        // GIVEN
        DbDto topicObject = DbDto.builder()
                .addResource(DbResourceDto.builder()
                        .withLocale(DbResourceDto.Locale.FRANCE)
                        .addEntry(DbResourceDto.Entry.builder()
                            .forReference(RESOURCE_REFERENCE)
                            .build())
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(DbResourceDto.Locale.ITALY)
                        .addEntry(DbResourceDto.Entry.builder()
                            .forReference(RESOURCE_REFERENCE)
                            .build())
                        .build())
                .build();

        // WHEN
        String actualResourceIdentifier = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);

        // THEN
        assertThat(actualResourceIdentifier).hasSize(8);
        assertThat(Integer.valueOf(actualResourceIdentifier))
                .isBetween(10000000, 99999999)
                .isNotEqualTo(11111111);
    }

    @Test
    public void generateUniqueContentsEntryIdentifier_whenNullTopicObject_shouldReturnNull() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseGenHelper.generateUniqueContentsEntryIdentifier(null)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateUniqueContentsEntryIdentifier_whenTopicObjectWithoutIdentifierField_shouldThrowIllegalArgumentException() throws Exception {
        // GIVEN-
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.INTEGER);

        // WHEN
        DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);

        // THEN: IllegalArgumentException
    }

    @Test
    public void generateUniqueContentsEntryIdentifier_whenTopicObjectWithIdentifierField_shouldReturnCorrectIdentifier() throws Exception {
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
    public void buildDefaultContentItems_whenOneField_andReference_shouldCreateOneItem() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneUIDField();

        // WHEN
        List<DbDataDto.Item> actualItems = genHelper.buildDefaultContentItems(Optional.of(ENTRY_REFERENCE), topicObject);

        // THEN
        assertThat(actualItems).hasSize(1);
        assertThat(actualItems).extracting("rawValue").containsExactly(ENTRY_REFERENCE);
    }

    @Test
    public void buildDefaultContentItems_whenOneField_andNoReference_shouldCreateOneItemWithDefaultRef() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneUIDField();

        // WHEN
        List<DbDataDto.Item> actualItems = genHelper.buildDefaultContentItems(Optional.empty(), topicObject);

        // THEN
        assertThat(actualItems).hasSize(1);
        assertThat(actualItems.get(0).getRawValue()).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void buildDefaultContentItem_whenNullFieldType_shouldThrowException() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(null);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.BITFIELD);

        // WHEN
        genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);

        // THEN
    }

    @Test
    public void buildDefaultContentItem_whenBitfield_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.BITFIELD);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.BITFIELD);

        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0");
    }

    @Test
    public void buildDefaultContentItem_whenFloatingPoint_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.FLOAT);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.FLOAT);

        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0.0");
    }

    @Test
    public void buildDefaultContentItem_whenInteger_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.INTEGER);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.INTEGER);

        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("0");
    }

    @Test
    public void buildDefaultContentItem_whenPercent_shouldCreateItem() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.PERCENT);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.PERCENT);

        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);

        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem.getRawValue()).isEqualTo("1");
    }

    @Test
    public void buildDefaultContentItem_whenReference_shouldCreateItem_andAddRemoteEntry() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteContent();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.REFERENCE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.UID);

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);


        // THEN
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        String newContentEntryRef = actualItem.getRawValue();

        verify(changeHelperMock).addContentsEntryWithDefaultItems(Optional.of(newContentEntryRef), BRANDS);
    }

    @Test
    public void buildDefaultContentItem_whenReference_andTargetGenerationDisabled_shouldNotAddRemoteEntry() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteContent();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.REFERENCE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.UID);

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, false);


        // THEN
        assertThat(actualItem).isNotNull();
        verify(changeHelperMock, never()).addContentsEntryWithDefaultItems(anyObject(), eq(BRANDS));
    }

    @Test
    public void buildDefaultContentItem_whenResource_shouldCreateItem_andResource() {
        // GIVEN
        DbStructureDto.Field field = createSingleStructureField(DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED);
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED);
        topicObject.getResources().add(DbResourceDto.builder().build());


        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);


        // THEN
        String actualResourceRef = actualItem.getRawValue();
        assertThat(actualResourceRef).isNotEmpty();

        assertThat(actualItem.getFieldRank()).isEqualTo(1);

        verify(changeHelperMock, times(8)).addResourceWithReference(eq(DbDto.Topic.ACHIEVEMENTS), any(DbResourceDto.Locale.class), eq(actualResourceRef), eq("??"));
    }

    @Test
    public void buildDefaultContentItem_whenRemoteResource_shouldCreateItem_andResource() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteResource();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        remoteTopicObject.getResources().add(DbResourceDto.builder().build());

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, true);


        // THEN
        String actualResourceRef = actualItem.getRawValue();
        assertThat(actualResourceRef).isNotEmpty();

        assertThat(actualItem.getFieldRank()).isEqualTo(1);

        verify(changeHelperMock, times(8)).addResourceWithReference(eq(BRANDS), any(DbResourceDto.Locale.class), eq(actualResourceRef), eq("??"));
    }

    @Test
    public void buildDefaultContentItem_whenRemoteResource_andTargetGenerationDisabled_shouldNotCreateResource() {
        // GIVEN
        DbStructureDto.Field field = createStructureFieldForRemoteResource();
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        DbDto remoteTopicObject = createRemoteTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_REMOTE);
        remoteTopicObject.getResources().add(DbResourceDto.builder().build());

        when(minerMock.getDatabaseTopicFromReference("TARGET_REF")).thenReturn(remoteTopicObject);


        // WHEN
        DbDataDto.Item actualItem = genHelper.buildDefaultContentItem(Optional.<String>empty(), field, topicObject, false);


        // THEN
        assertThat(actualItem).isNotNull();

        verify(changeHelperMock, never()).addResourceWithReference(eq(BRANDS), any(DbResourceDto.Locale.class), anyString(), eq("??"));
    }

    @Test
    public void generateDefaultResourceReference_whenDefaultResourceEntryExists_shouldReturnIt() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED);
        topicObject.getResources().add(DbResourceDto.builder()
                .addEntry(DbResourceDto.Entry.builder()
                        .forReference("12345")
                        .withValue("??")
                        .build())
                .build());


        // WHEN
        String actualResourceReference = genHelper.generateDefaultResourceReference(topicObject);


        // THEN
        assertThat(actualResourceReference).isEqualTo("12345");

        verifyZeroInteractions(changeHelperMock);
    }

    @Test
    public void generateDefaultResourceReference_whenDefaultResourceEntryDoesNotExist_shouldGenerateIt() {
        // GIVEN
        DbDto topicObject = createTopicObjectOneField(DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED);
        topicObject.getResources().add(DbResourceDto.builder().build());


        // WHEN
        String actualResourceReference = genHelper.generateDefaultResourceReference(topicObject);


        // THEN
        assertThat(actualResourceReference).isNotEmpty();

        verify(changeHelperMock, times(8)).addResourceWithReference(eq(DbDto.Topic.ACHIEVEMENTS), any(DbResourceDto.Locale.class), anyString(), eq("??"));
    }

    @Test
    public void generateUniqueIdentifier() {
        // GIVEN
        Set<String> existingValues = new HashSet<>(Arrays.asList("1", "2", "3"));

        // WHEN
        final String actual = DatabaseGenHelper.generateUniqueIdentifier(existingValues, Range.between(1, 10));

        // THEN
        assertThat(actual).isNotNull();
        assertThat(existingValues).doesNotContain(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateUniqueIdentifier_whenNoSpaceLeftInRange_shouldThrowException() {
        // GIVEN
        Set<String> existingValues = new HashSet<>(Arrays.asList("1", "2", "3"));

        // WHEN
        final String actual = DatabaseGenHelper.generateUniqueIdentifier(existingValues, Range.between(1, 3));

        // THEN
        assertThat(actual).isNotNull();
        assertThat(existingValues).doesNotContain(actual);
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
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(DbStructureDto.FieldType.UID)
                                .ofRank(1)
                                .build())
                        .build())
                .withData(DbDataDto.builder()
                        .addEntry(DbDataDto.Entry.builder()
                                .addItem(DbDataDto.Item.builder()
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
                .build();
    }
}