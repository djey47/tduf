package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseStructureQueryHelperTest {

    @Test
    public void getIdentifierField_whenTopicObjectNull_shouldReturnNull() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getIdentifierField((DbDto)null)).isNull();
    }

    @Test
    public void getIdentifierField_whenNoIdentifierField_shouldReturnAbsent() throws Exception {
        // GIVEN
        DbDto topicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .addItem(createClassicField())
                        .build())
                .build();

        // WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getIdentifierField(topicObject)).isEmpty();
    }

    @Test
    public void getIdentifierField_whenFound_shouldReturnIt() throws Exception {
        // GIVEN
        DbStructureDto.Field identifierField = DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.UID)
                .build();

        DbDto topicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .addItem(identifierField)
                        .addItem(createClassicField())
                        .build())
                .build();


        // WHEN
        Optional<DbStructureDto.Field> actualField = DatabaseStructureQueryHelper.getIdentifierField(topicObject);

        // THEN
        assertThat(actualField).isPresent();
        assertThat(actualField.get()).isEqualTo(identifierField);
    }

    private static DbStructureDto.Field createClassicField() {
        return DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.INTEGER)
                .build();
    }
}