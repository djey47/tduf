package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseStructureQueryHelperTest {

    @Test(expected = NullPointerException.class)
    public void getIdentifierField_whenFieldListNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        assertThat(DatabaseStructureQueryHelper.getIdentifierField(null)).isNull();

        // THEN: NPE
    }

    @Test
    public void getIdentifierField_whenNoIdentifierField_shouldReturnAbsent() throws Exception {
        // GIVEN
        DbStructureDto structureObject = DbStructureDto.builder()
                .addItem(createClassicField())
                .build();

        // WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getIdentifierField(structureObject.getFields())).isEmpty();
    }

    @Test
    public void getIdentifierField_whenFound_shouldReturnIt() throws Exception {
        // GIVEN
        DbStructureDto.Field identifierField = DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.UID)
                .build();
        DbStructureDto structureObject = DbStructureDto.builder()
                .addItem(identifierField)
                .addItem(createClassicField())
                .build();

        // WHEN
        Optional<DbStructureDto.Field> actualField = DatabaseStructureQueryHelper.getIdentifierField(structureObject.getFields());

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