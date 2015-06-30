package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseGenHelperTest {

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
                            .forReference("11111111")
                            .build())
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(DbResourceDto.Locale.ITALY)
                        .addEntry(DbResourceDto.Entry.builder()
                            .forReference("11111111")
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
        DbDto topicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .addItem(DbStructureDto.Field.builder()
                                .fromType(DbStructureDto.FieldType.INTEGER)
                                .ofRank(1)
                                .build())
                        .build())
                .build();

        // WHEN
        DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);

        // THEN: IllegalArgumentException
    }

    @Test
    public void generateUniqueContentsEntryIdentifier_whenTopicObjectWithIdentifierField_shouldReturnCorrectIdentifier() throws Exception {
        // GIVEN-
        DbDto topicObject = DbDto.builder()
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
                                        .withRawValue("11111111")
                                        .build())
                                .build())
                        .build())
                .build();

        // WHEN
        String actualEntryIdentifier = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject);

        // THEN
        assertThat(actualEntryIdentifier).hasSize(8);
        assertThat(Integer.valueOf(actualEntryIdentifier))
                .isBetween(10000000, 99999999)
                .isNotEqualTo(11111111);
    }

    @Test
    public void generateUniqueResourceEntryIdentifier() throws Exception {

    }
}