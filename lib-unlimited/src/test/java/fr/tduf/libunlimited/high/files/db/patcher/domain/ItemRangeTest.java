package fr.tduf.libunlimited.high.files.db.patcher.domain;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemRangeTest {

    @Test
    void isGlobal_whenBothMinAndMaxEmpty_shouldReturnTrue() {
        // GIVEN-WHEN
        ItemRange actualRange = new ItemRange(empty(), empty());

        //THEN
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    void isGlobal_whenMinEmpty_shouldReturnFalse() {
        // GIVEN-WHEN
        ItemRange actualRange = new ItemRange(empty(), Optional.of(1000000L));

        //THEN
        assertThat(actualRange.isGlobal()).isFalse();
    }

    @Test
    void isGlobal_whenEmptyList_shouldReturnFalse() {
        // GIVEN-WHEN
        ItemRange actualRange = new ItemRange(new ArrayList<>());

        //THEN
        assertThat(actualRange.isGlobal()).isFalse();
    }

    @Test
    void fromCliOption_whenAbsentValue_shouldCreateGlobalRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromCliOption(empty());

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    void fromCliOption_whenIllegalRangeValue_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> ItemRange.fromCliOption(Optional.of("azertyuiop")));
    }

    @Test
    void fromCliOption_whenValueWithIllegalBounds_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> ItemRange.fromCliOption(Optional.of("1..0")));
    }

    @Test
    void fromCliOption_whenValueWithValidBounds_shouldReturnRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromCliOption(Optional.of("10..20"));

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.fetchLowerBound().get()).isEqualTo(10L);
        assertThat(actualRange.fetchUpperBound().get()).isEqualTo(20L);
        assertThat(actualRange.getEnumeratedItems()).isNull();
    }

    @Test
    void fromCliOption_whenValueWithEnumeration_shouldReturnRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromCliOption(Optional.of("10,20,30,40"));

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.fetchLowerBound()).isEmpty();
        assertThat(actualRange.fetchUpperBound()).isEmpty();
        assertThat(actualRange.getEnumeratedItems()).containsExactly("10", "20", "30", "40");
    }

    @Test
    void fromCollection_whenNullArgument_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> ItemRange.fromCollection(null));
    }

    @Test
    void fromCollection_whenEmptyList_shouldReturnGlobalRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromCollection(new ArrayList<>());

        // THEN
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    void fromCollection_whenNonEmptyList_shouldReturnRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromCollection(asList("1", "2", "3"));

        // THEN
        assertThat(actualRange.isGlobal()).isFalse();
        assertThat(actualRange.getEnumeratedItems()).containsExactly("1", "2", "3");
    }

    @Test
    void fromSingleValue_shouldReturnRange() {
        // GIVEN-WHEN
        ItemRange actualRange = ItemRange.fromSingleValue("1");

        // THEN
        assertThat(actualRange.isGlobal()).isFalse();
        assertThat(actualRange.getEnumeratedItems()).containsExactly("1");
    }

    @Test
    void fromSingleValue_whenNullArgument_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> ItemRange.fromSingleValue(null));
    }

    @Test
    void accepts_whenGlobalRange_shouldReturnTrue(){
        // Long
        ItemRange actualRange = new ItemRange(empty(), empty());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    void accepts_whenRefInList_shouldReturnTrue(){
        // GIVEN
        ItemRange actualRange = new ItemRange(asList("11111111", "12345678"));

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    void accepts_whenRefNotInList_shouldReturnFalse(){
        // GIVEN
        ItemRange actualRange = new ItemRange(new ArrayList<>());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isFalse();
    }

    @Test
    void accepts_whenRefInRange_shouldReturnTrue(){
        // GIVEN
        ItemRange actualRange = new ItemRange(Optional.of(1L), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("500")).isTrue();
    }

    @Test
    void accepts_whenLowerUnbounded_andRefInRange_shouldReturnTrue(){
        // GIVEN
        ItemRange actualRange = new ItemRange(empty(), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("500")).isTrue();
    }

    @Test
    void accepts_whenUpperUnbounded_andRefInRange_shouldReturnTrue(){
        // GIVEN
        ItemRange actualRange = new ItemRange(Optional.of(1L), empty());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    void accepts_whenRefNotInRange_shouldReturnFalse(){
        // GIVEN
        ItemRange actualRange = new ItemRange(Optional.of(1L), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isFalse();
    }
}
