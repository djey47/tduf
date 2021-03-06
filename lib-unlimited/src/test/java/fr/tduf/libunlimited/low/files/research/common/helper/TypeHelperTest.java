package fr.tduf.libunlimited.low.files.research.common.helper;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeHelperTest {

    @Test
    void rawToText_shouldReturnText() {
        //GIVEN
        byte[] bytes = {(byte) 0xcd, 0x4d, 0x41, 0x50, 0x34,  0x00};

        // WHEN-THEN
        assertThat(TypeHelper.rawToText(bytes)).isEqualTo("\u00cdMAP4\0");
    }

    @Test
    void rawToText_whenGreaterLength_shouldReturnTextFilledWithZeros() {
        //GIVEN
        byte[] bytes = { 0x4d, 0x41, 0x50, 0x34 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToText(bytes, 5)).isEqualTo("MAP4\0");
    }

    @Test
    void rawToText_whenLowerLength_shouldReturnTruncatedText() {
        //GIVEN
        byte[] bytes = { 0x4d, 0x41, 0x50, 0x34, 0x00 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToText(bytes, 4)).isEqualTo("MAP4");
    }

    @Test
    void rawToInteger_whenUnsignedValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, false, 4)).isEqualTo(858241L);
    }

    @Test
    void rawToInteger_whenSignedIntegerValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, (byte)0xFF, (byte)0xF2, (byte)0xE7, 0x7F };

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, true, 4)).isEqualTo(-858241L);
    }

    @Test
    void rawToInteger_whenSignedNegativeShortValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xE7, 0x7F };

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, true, 2)).isEqualTo(-6273);
    }

    @Test
    void rawToInteger_whenSignedPositiveShortValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE5};

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, true, 2)).isEqualTo(229);
    }

    @Test
    void rawToInteger_whenSignedByteValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xFF };

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, true, 1)).isEqualTo(-1L);
    }

    @Test
    void rawToInteger_whenZeroValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = new byte[8];

        // WHEN-THEN
        assertThat(TypeHelper.rawToInteger(bytes, true, 8)).isZero();
    }

    @Test
    void rawToInteger_whenArrayHasIncorrectSize_shouldThrowException() {
        //GIVEN
        byte[] bytes = { 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> TypeHelper.rawToInteger(bytes, false, 4));
    }

    @Test
    void rawToFloatingPoint_when32BitValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToFloatingPoint(bytes)).isEqualTo(289.43762f);
    }

    @Test
    void rawToFloatingPoint_when16BitValue_shouldReturnNumeric() {
        //GIVEN
        byte[] bytes = { 0x43, (byte)0x90 };

        // WHEN-THEN
        assertThat(TypeHelper.rawToFloatingPoint(bytes)).isEqualTo(3.78125f);
    }

    @Test
    void rawToFloatingPoint_whenArrayHasIncorrectSize_shouldThrowException() {
        //GIVEN
        byte[] bytes = { 0x00, 0x00, 0x00, 0x00, 0x04, (byte)0xB8, (byte)0x90, 0x43 };

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> TypeHelper.rawToFloatingPoint(bytes));
    }

    @Test
    void textToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x4d, 0x41, 0x50, 0x34,  0x00};

        // WHEN-THEN
        assertThat(TypeHelper.textToRaw("MAP4\0", 5)).isEqualTo(expectedBytes);
    }

    @Test
    void textToRaw_whenExtendedCharacter_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = {(byte) 0xcd, 0x4d, 0x41, 0x50, 0x34,  0x00};

        // WHEN-THEN
        assertThat(TypeHelper.textToRaw("\u00cdMAP4\0", 6)).isEqualTo(expectedBytes);
    }

    @Test
    void textToRaw_whenGreaterLength_shouldReturnByteArrayFilledByZeros() {
        //GIVEN
        byte[] expectedBytes = { 0x4d, 0x41, 0x50, 0x34,  0x00, 0x00, 0x00, 0x00};

        // WHEN-THEN
        assertThat(TypeHelper.textToRaw("MAP4", 8)).isEqualTo(expectedBytes);
    }

    @Test
    void textToRaw_whenLowerLength_shouldReturnTruncatedByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x4d, 0x41, 0x50, 0x34};

        // WHEN-THEN
        assertThat(TypeHelper.textToRaw("MAP4\0", 4)).isEqualTo(expectedBytes);
    }

    @Test
    void integerToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d, 0x18, (byte)0x81 };

        // WHEN-THEN
        assertThat(TypeHelper.integerToRaw(858241L)).isEqualTo(expectedBytes);
    }

    @Test
    void floatingPoint32ToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x43, (byte)0x90, (byte)0xb8, 0x04 };

        // WHEN-THEN
        assertThat(TypeHelper.floatingPoint32ToRaw(289.43762f)).isEqualTo(expectedBytes);
    }

    @Test
    void floatingPoint16ToRaw_shouldReturnByteArray() {
        //GIVEN
        byte[] expectedBytes = { 0x43, (byte)0x90 };

        // WHEN-THEN
        assertThat(TypeHelper.floatingPoint16ToRaw(3.78125f)).isEqualTo(expectedBytes);
    }

    @Test
    void changeEndianType_shouldReverseBytes() {
        // GIVEN
        byte[] valueBytes = {(byte)0xF4, 0x01, 0x00, 0x00};
        byte[] expectedBytes = {0x00, 0x00, 0x01, (byte)0xF4};

        // WHEN-THEN
        assertThat(TypeHelper.changeEndianType(valueBytes)).isEqualTo(expectedBytes);
    }

    @Test
    void changeEndianType_whenSingleByte_shouldReturnIt() {
        // GIVEN
        byte[] valueBytes = {(byte)0xF4};
        byte[] expectedBytes = {(byte)0xF4};

        // WHEN-THEN
        assertThat(TypeHelper.changeEndianType(valueBytes)).isEqualTo(expectedBytes);
    }

    @Test
    void fitToSize_whenNullArray_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.fitToSize(null, null)).isNull();
    }

    @Test
    void fitToSize_whenNullLength_shouldReturnNewIdenticalArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, null);

        // THEN
        assertThat(actualArray).isNotSameAs(byteArray);
        assertThat(actualArray).isEqualTo(byteArray);
    }

    @Test
    void fitToSize_whenWantedItBigger_shouldReturnNewFilledArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};
        byte[] expectedByteArray = {0x1, 0x2, 0x3, 0x4, 0x0, 0x0};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, expectedByteArray.length);

        // THEN
        assertThat(actualArray).isEqualTo(expectedByteArray);
    }

    @Test
    void fitToSize_whenWantedItSmaller_shouldReturnNewTruncatedArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};
        byte[] expectedByteArray = {0x1, 0x2};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, expectedByteArray.length);

        // THEN
        assertThat(actualArray).isEqualTo(expectedByteArray);
    }

    @Test
    void fitToSize_whenWantedSameSize_shouldReturnClonedArray() {
        // GIVEN
        byte[] byteArray = {0x1, 0x2, 0x3, 0x4};

        // WHEN
        byte[] actualArray = TypeHelper.fitToSize(byteArray, byteArray.length);

        // THEN
        assertThat(actualArray).isNotSameAs(byteArray);
        assertThat(actualArray).isEqualTo(byteArray);
    }

    @Test
    void byteArrayToHexRepresentation_whenNullArray_shouldReturnNull(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.byteArrayToHexRepresentation(null)).isNull();
    }

    @Test
    void byteArrayToHexRepresentation_whenEmptyArray_shouldReturnString(){
        // GIVEN
        byte[] byteArray = new byte[0];

        // WHEN-THEN
        assertThat(TypeHelper.byteArrayToHexRepresentation(byteArray)).isEqualTo("0x[]");
    }

    @Test
    void byteArrayToHexRepresentation_shouldReturnString(){
        // GIVEN
        byte[] byteArray = new byte[] { 0x0, (byte)0xAA, (byte)0xFF};

        // WHEN-THEN
        assertThat(TypeHelper.byteArrayToHexRepresentation(byteArray)).isEqualTo("0x[00 AA FF]");
    }

    @Test
    void hexRepresentationToByteArray_whenNull_shouldReturnNull(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.hexRepresentationToByteArray(null)).isNull();
    }

    @Test
    void hexRepresentationToByteArray_whenInvalid_shouldThrowIllegalArgumentException(){
        // GIVEN-WHEN-THEN
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> TypeHelper.hexRepresentationToByteArray("xxx"));
        assertThat(actualException).hasMessage("Provided hexadecimal representation is invalid.");
    }

    @Test
    void hexRepresentationToByteArray_whenInvalidAsWell_shouldThrowIllegalArgumentException(){
        // GIVEN-WHEN-THEN
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> TypeHelper.hexRepresentationToByteArray("0x[IN VA LI D0]"));
        assertThat(actualException).hasMessage("Provided hexadecimal representation is invalid.");
    }

    @Test
    void hexRepresentationToByteArray_whenEmpty_shouldReturnEmptyArray(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.hexRepresentationToByteArray("0x[]")).isEmpty();
    }

    @Test
    void hexRepresentationToByteArray_shouldReturnArray(){
        // GIVEN-WHEN-THEN
        assertThat(TypeHelper.hexRepresentationToByteArray("0x[00 AA ff]")).containsExactly((byte)0x0, (byte)0xAA, (byte)0xFF);
    }

    @Test
    void hexRepresentationToByteArray_whenLargeString_shouldReturnArray() throws IOException {
        // given
        String representation = FilesHelper.readTextFromResourceFile("/files/dumps/EXTRACT-largeRepresentation.txt");

        // when
        byte[] actualBytes = TypeHelper.hexRepresentationToByteArray(representation);

        // then
        assertThat(actualBytes).hasSize(6288);
    }
}