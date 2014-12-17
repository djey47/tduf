package fr.tduf.libunlimited.low.files.research.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class TypeHelperTest {

    @Test
    public void rawToText_shouldReturnText() {
        //GIVEN
        byte[] bytes = { 0x4d, 0x41, 0x50, 0x34,  0x00};

        // WHEN-THEN
        assertThat(TypeHelper.rawToText(bytes)).isEqualTo("MAP4\0");
    }

    @Test
    public void rawToNumeric_whenArrayHasCorrectSize_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToNumeric(bytes)).isEqualTo(858241L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rawToNumeric_whenArrayHasIncorrectSize_shouldThrowException() {
        //GIVEN
        byte[] bytes = { 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToNumeric(bytes)).isEqualTo(858241L);
    }
}