package fr.tduf.cli.tools.dto;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseIntegrityErrorDtoTest {

    @Test
    void toAttributeCase_whenNullText_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseIntegrityErrorDto.toAttributeCase(null)).isNull();
    }

    @Test
    void toAttributeCase_whenEmptyText_shouldReturnEmptyString() {
        // GIVEN-WHEN-THEN
        assertThat(DatabaseIntegrityErrorDto.toAttributeCase("")).isEmpty();
    }

    @Test
    void toAttributeCase_whenComplexText_shouldReturnTextWithAttributeCase() {
        // GIVEN
        String text = "Abcd Efgh_Ijklm-Nopq";

        // WHEN
        String actualText = DatabaseIntegrityErrorDto.toAttributeCase(text);

        // THEN
        assertThat(actualText).isEqualTo("abcdEfghIjklmNopq");
    }
}