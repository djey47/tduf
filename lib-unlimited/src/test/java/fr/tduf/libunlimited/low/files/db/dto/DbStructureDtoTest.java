package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DbStructureDtoTest {

    @Test
    public void itemTypeFromCode_whenCodeExists_shouldReturnProperType() throws Exception {
        //GIVEN
        DbStructureDto.FieldType expectedFieldType = DbStructureDto.FieldType.RESOURCE_CURRENT;

        //WHEN
        DbStructureDto.FieldType actualFieldType = DbStructureDto.FieldType.fromCode("u");

        //THEN
        assertThat(actualFieldType).isEqualTo(expectedFieldType);
    }

    @Test
    public void itemTypeFromCode_whenCodeDoesNotExist_shouldReturnNull() throws Exception {
        //GIVEN-WHEN
        DbStructureDto.FieldType actualFieldType = DbStructureDto.FieldType.fromCode("z");

        //THEN
        assertThat(actualFieldType).isNull();
    }
}