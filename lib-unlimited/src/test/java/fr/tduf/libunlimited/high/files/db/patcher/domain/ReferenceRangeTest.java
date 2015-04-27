package fr.tduf.libunlimited.high.files.db.patcher.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceRangeTest {

    @Test
    public void isGlobal_whenBothMinAndMaxEmpty_shouldReturnTrue() {
        // GIVEN-WHEN
        ReferenceRange actualRange = new ReferenceRange(Optional.<Long>empty(), Optional.<Long>empty());

        //THEN
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    public void isGlobal_whenMinEmpty_shouldReturnFalse() {
        // GIVEN-WHEN
        ReferenceRange actualRange = new ReferenceRange(Optional.<Long>empty(), Optional.of(1000000L));

        //THEN
        assertThat(actualRange.isGlobal()).isFalse();
    }

    @Test
    public void isGlobal_whenEmptyList_shouldReturnFalse() {
        // GIVEN-WHEN
        ReferenceRange actualRange = new ReferenceRange(new ArrayList<>());

        //THEN
        assertThat(actualRange.isGlobal()).isFalse();
    }

    @Test
    public void fromCliOption_whenAbsentValue_shouldCreateGlobalRange() {
        // GIVEN-WHEN
        ReferenceRange actualRange = ReferenceRange.fromCliOption(Optional.<String>empty());

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromCliOption_whenIllegalRangeValue_shouldThrowException() {
        // GIVEN-WHEN
        ReferenceRange.fromCliOption(Optional.of("azertyuiop"));

        // THEN: IAE
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromCliOption_whenValueWithIllegalBounds_shouldThrowException() {
        // GIVEN-WHEN
        ReferenceRange.fromCliOption(Optional.of("1..0"));

        // THEN: IAE
    }

    @Test
    public void fromCliOption_whenValueWithValidBounds_shouldReturnRange() {
        // GIVEN-WHEN
        ReferenceRange actualRange = ReferenceRange.fromCliOption(Optional.of("10..20"));

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.getMinRef().get()).isEqualTo(10L);
        assertThat(actualRange.getMaxRef().get()).isEqualTo(20L);
        assertThat(actualRange.getRefs()).isNull();
    }

    @Test
    public void fromCliOption_whenValueWithEnumeration_shouldReturnRange() {
        // GIVEN-WHEN
        ReferenceRange actualRange = ReferenceRange.fromCliOption(Optional.of("10,20,30,40"));

        // THEN
        assertThat(actualRange).isNotNull();
        assertThat(actualRange.getMinRef()).isEmpty();
        assertThat(actualRange.getMaxRef()).isEmpty();
        assertThat(actualRange.getRefs()).containsExactly("10", "20", "30", "40");
    }

    @Test(expected = NullPointerException.class)
    public void fromList_whenNullArgument_shouldThrowException() {
        // GIVEN-WHEN
        ReferenceRange.fromList(null);

        // THEN: NPE
    }

    @Test
    public void fromList_whenEmptyList_shouldReturnGlobalRange() {
        // GIVEN-WHEN
        ReferenceRange actualRange = ReferenceRange.fromList(new ArrayList<>());

        // THEN
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    public void fromList_whenNonEmptyList_shouldReturnRange() {
        // GIVEN-WHEN
        ReferenceRange actualRange = ReferenceRange.fromList(asList("1", "2", "3"));

        // THEN
        assertThat(actualRange.isGlobal()).isFalse();
        assertThat(actualRange.getRefs()).containsExactly("1", "2", "3");
    }

    @Test
    public void accepts_whenGlobalRange_shouldReturnTrue(){
        // Long
        ReferenceRange actualRange = new ReferenceRange(Optional.<Long>empty(), Optional.<Long>empty());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    public void accepts_whenRefInList_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(asList("11111111", "12345678"));

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    public void accepts_whenRefNotInList_shouldReturnFalse(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(new ArrayList<>());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isFalse();
    }

    @Test
    public void accepts_whenRefInRange_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(Optional.of(1L), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("500")).isTrue();
    }

    @Test
    public void accepts_whenLowerUnbounded_andRefInRange_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(Optional.empty(), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("500")).isTrue();
    }

    @Test
    public void accepts_whenUpperUnbounded_andRefInRange_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(Optional.of(1L), Optional.empty());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isTrue();
    }

    @Test
    public void accepts_whenRefNotInRange_shouldReturnFalse(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(Optional.of(1L), Optional.of(1000L));

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isFalse();
    }
}