package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DbStructureDtoTest {

    @Test
    public void itemTypeFromCode_whenCodeExists_shouldReturnProperType() throws Exception {
        //GIVEN
        DbStructureDto.Type expectedType = DbStructureDto.Type.RESOURCE_CURRENT;

        //WHEN
        DbStructureDto.Type actualType = DbStructureDto.Type.fromCode("u");

        //THEN
        assertThat(actualType).isEqualTo(expectedType);
    }

    @Test
    public void itemTypeFromCode_whenCodeDoesNotExist_shouldReturnNull() throws Exception {
        //GIVEN-WHEN
        DbStructureDto.Type actualType = DbStructureDto.Type.fromCode("z");

        //THEN
        assertThat(actualType).isNull();
    }
}