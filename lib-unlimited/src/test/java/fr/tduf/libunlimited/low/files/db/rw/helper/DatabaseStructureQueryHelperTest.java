package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.NoSuchElementException;
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
        DbStructureDto.Field identifierField = createIdentifierField();
        DbStructureDto structureObject = createStructureWithTwoFields(identifierField);

        // WHEN
        Optional<DbStructureDto.Field> actualField = DatabaseStructureQueryHelper.getIdentifierField(structureObject.getFields());

        // THEN
        assertThat(actualField).contains(identifierField);
    }

    @Test(expected = NullPointerException.class)
    public void getStructureField_whenNullArguments_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseStructureQueryHelper.getStructureField(null, null);

        // THEN: NPE
    }

    @Test(expected = NoSuchElementException.class)
    public void getStructureField_whenNotFound_shouldThrowException() throws Exception {
        // GIVEN
        DbDataDto.Item contentsItem = DbDataDto.Item.builder()
                .ofFieldRank(2)
                .build();
        DbStructureDto structureObject = createStructureWithTwoFields(createIdentifierField());

        // WHEN
        DatabaseStructureQueryHelper.getStructureField(contentsItem, structureObject.getFields());

        // THEN: NSEE
    }

    @Test
    public void getStructureField_whenFound_shouldReturnIt() throws Exception {
        // GIVEN
        DbDataDto.Item contentsItem = DbDataDto.Item.builder()
                .ofFieldRank(0)
                .build();
        DbStructureDto.Field identifierField = createIdentifierField();
        DbStructureDto structureObject = createStructureWithTwoFields(identifierField);

        // WHEN
        DbStructureDto.Field actualField = DatabaseStructureQueryHelper.getStructureField(contentsItem, structureObject.getFields());

        // THEN
        assertThat(actualField).isNotNull();
        assertThat(actualField).isEqualTo(identifierField);
    }

    private DbStructureDto createStructureWithTwoFields(DbStructureDto.Field identifierField) {
        return DbStructureDto.builder()
                .addItem(identifierField)
                .addItem(createClassicField())
                .build();
    }

    private static DbStructureDto.Field createIdentifierField() {
        return DbStructureDto.Field.builder()
                .ofRank(0)
                .fromType(DbStructureDto.FieldType.UID)
                .build();
    }

    private static DbStructureDto.Field createClassicField() {
        return DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.INTEGER)
                .build();
    }
}