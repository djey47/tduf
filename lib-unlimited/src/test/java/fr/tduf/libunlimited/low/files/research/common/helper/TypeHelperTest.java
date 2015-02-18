package fr.tduf.libunlimited.low.files.research.common.helper;

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
    public void rawToFloatingPoint_when32BitValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToFloatingPoint(bytes)).isEqualTo(289.43762f);
    }

    @Test
    public void rawToFloatingPoint_when16BitValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x43, (byte)0x90 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToFloatingPoint(bytes)).isEqualTo(3.78125f);
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
    public void floatingPoint32ToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.floatingPoint32ToRaw(289.43762f)).isEqualTo(expectedBytes);
    }

    @Test
    public void floatingPoint16ToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x43, (byte)0x90 };

        // WHEN-THEN
        assertThat(TypeHelper.floatingPoint16ToRaw(3.78125f)).isEqualTo(expectedBytes);
    }

    @Test
    public void changeEndianType_shouldReverseBytes() {
        // GIVEN
        byte[] valueBytes = {(byte)0xF4, 0x01, 0x00, 0x00};
        byte[] expectedBytes = {0x00, 0x00, 0x01, (byte)0xF4};

        // WHEN-THEN
        assertThat(TypeHelper.changeEndianType(valueBytes)).isEqualTo(expectedBytes);
    }

    @Test
    public void changeEndianType_whenSingleByte_shouldReturnIt() {
        // GIVEN
        byte[] valueBytes = {(byte)0xF4};
        byte[] expectedBytes = {(byte)0xF4};

        // WHEN-THEN
        assertThat(TypeHelper.changeEndianType(valueBytes)).isEqualTo(expectedBytes);
    }

    @Test
    public void fitToSize_whenNullArray_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.fitToSize(null, null)).isNull();
    }

    @Test
    public void fitToSize_whenNullLength_shouldReturnNewIdenticalArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, null);

        // THEN
        assertThat(actualArray).isNotSameAs(byteArray);
        assertThat(actualArray).isEqualTo(byteArray);
    }

    @Test
    public void fitToSize_whenWantedItBigger_shouldReturnNewFilledArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};
        byte[] expectedByteArray = {0x1, 0x2, 0x3, 0x4, 0x0, 0x0};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, expectedByteArray.length);

        // THEN
        assertThat(actualArray).isEqualTo(expectedByteArray);
    }

    @Test
    public void fitToSize_whenWantedItSmaller_shouldReturnNewTruncatedArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};
        byte[] expectedByteArray = {0x1, 0x2};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, expectedByteArray.length);

        // THEN
        assertThat(actualArray).isEqualTo(expectedByteArray);
    }

    @Test
    public void fitToSize_whenWantedSameSize_shouldReturnClonedArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, byteArray.length);

        // THEN
        assertThat(actualArray).isNotSameAs(byteArray);
        assertThat(actualArray).isEqualTo(byteArray);
    }

    @Test
    public void byteArrayToHexRepresentation_whenNullArray_shouldReturnNull(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.byteArrayToHexRepresentation(null)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void byteArrayToHexRepresentation_whenEmptyArray_shouldThrowIllegalArgumentException(){
        // GIVEN-WHEN-THEN
        TypeHelper.byteArrayToHexRepresentation(new byte[0]);
    }

    @Test
    public void byteArrayToHexRepresentation_shouldReturnString(){
        // GIVEN
        byte[] byteArray = new byte[] { 0x0, (byte)0xAA, (byte)0xFF};

        // WHEN-THEN
        assertThat(TypeHelper.byteArrayToHexRepresentation(byteArray)).isEqualTo("0x[00 AA FF]");
    }

    @Test
    public void hexRepresentationToByteArray_whenNull_shouldReturnNull(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.hexRepresentationToByteArray(null)).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexRepresentationToByteArray_whenInvalid_shouldThrowIllegalArgumentException(){
        // GIVEN-WHEN-THEN
        TypeHelper.hexRepresentationToByteArray("xxx");
    }

    @Test
    public void hexRepresentationToByteArray_shouldReturnArray(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.hexRepresentationToByteArray("0x[00 AA ff]")).containsExactly((byte)0x0, (byte)0xAA, (byte)0xFF);
    }
}