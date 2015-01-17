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
    public void rawToInteger_whenArrayHasCorrectSize_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes)).isEqualTo(858241L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rawToInteger_whenArrayHasIncorrectSize_shouldThrowException() {
        //GIVEN
        byte[] bytes = { 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        TypeHelper.rawToInteger(bytes);
    }

    @Test
    public void rawToFloatingPoint_whenArrayHasCorrectSize_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToFloatingPoint(bytes)).isEqualTo(289.43762f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rawToFloatingPoint_whenArrayHasIncorrectSize_shouldThrowException() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x04, (byte)0xB8, (byte)0x90, 0x43 };

        // WHEN-THEN
        TypeHelper.rawToFloatingPoint(bytes);
    }

    @Test
    public void textToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x4d, 0x41, 0x50, 0x34,  0x00};

        // WHEN-THEN
        assertThat(TypeHelper.textToRaw("MAP4\0")).isEqualTo(expectedBytes);
    }

    @Test
    public void integerToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.integerToRaw(858241L)).isEqualTo(expectedBytes);
    }

    @Test
    public void floatingPointToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.floatingPointToRaw(289.43762f)).isEqualTo(expectedBytes);
    }
}