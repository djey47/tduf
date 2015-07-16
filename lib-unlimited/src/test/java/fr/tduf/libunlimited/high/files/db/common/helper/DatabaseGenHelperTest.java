package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        DbDto topicObject = createTopicObjectOneIntegerField();

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

    private DbDto createTopicObjectOneUIDField() {
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

    private DbDto createTopicObjectOneIntegerField() {
        return DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(DbStructureDto.FieldType.INTEGER)
                                .ofRank(1)
                                .build())
                        .build())
                .build();
    }
}