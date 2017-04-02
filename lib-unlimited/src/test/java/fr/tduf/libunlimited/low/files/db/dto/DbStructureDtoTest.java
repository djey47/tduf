package fr.tduf.libunlimited.low.files.db.dto;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DbStructureDtoTest {

    @Test
    void itemTypeFromCode_whenCodeExists_shouldReturnProperType() throws Exception {
        //GIVEN
        DbStructureDto.FieldType expectedFieldType = DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED;

        //WHEN
        DbStructureDto.FieldType actualFieldType = DbStructureDto.FieldType.fromCode("u");

        //THEN
        assertThat(actualFieldType).isEqualTo(expectedFieldType);
    }

    @Test
    void itemTypeFromCode_whenCodeDoesNotExist_shouldReturnNull() throws Exception {
        //GIVEN-WHEN
        DbStructureDto.FieldType actualFieldType = DbStructureDto.FieldType.fromCode("z");

        //THEN
        assertThat(actualFieldType).isNull();
    }

    @Test
    void isAResourceField_whenUIDField_shouldReturnFalse() {
        // GIVEN
        DbStructureDto.Field field = DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.UID)
                .build();

        // WHEN-THEN
        assertThat(field.isAResourceField()).isFalse();
    }

    @Test
    void isAResourceField_whenResourceField_shouldReturnTrue() {
        // GIVEN
        DbStructureDto.Field field1 = DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED)
                .build();
        DbStructureDto.Field field2 = DbStructureDto.Field.builder()
                .ofRank(2)
                .fromType(DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED)
                .build();
        DbStructureDto.Field field3 = DbStructureDto.Field.builder()
                .ofRank(3)
                .fromType(DbStructureDto.FieldType.RESOURCE_REMOTE)
                .build();

        // WHEN-THEN
        assertThat(field1.isAResourceField()).isTrue();
        assertThat(field2.isAResourceField()).isTrue();
        assertThat(field3.isAResourceField()).isTrue();
    }
}
