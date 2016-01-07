package fr.tduf.cli.tools.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseIntegrityErrorDtoTest {

    @Test
    public void toAttributeCase_whenNullText_shouldReturnNull() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseIntegrityErrorDto.toAttributeCase(null)).isNull();
    }

    @Test
    public void toAttributeCase_whenEmptyText_shouldReturnEmptyString() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseIntegrityErrorDto.toAttributeCase("")).isEmpty();
    }

    @Test
    public void toAttributeCase_whenComplexText_shouldReturnTextWithAttributeCase() throws Exception {
        // GIVEN
        String text = "Abcd Efgh_Ijklm-Nopq";

        // WHEN
        String actualText = DatabaseIntegrityErrorDto.toAttributeCase(text);

        // THEN
        assertThat(actualText).isEqualTo("abcdEfghIjklmNopq");
    }
}