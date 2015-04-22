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
        ReferenceRange actualRange = new ReferenceRange(Optional.<String>empty(), Optional.<String>empty());

        //THEN
        assertThat(actualRange.isGlobal()).isTrue();
    }

    @Test
    public void isGlobal_whenMinEmpty_shouldReturnFalse() {
        // GIVEN-WHEN
        ReferenceRange actualRange = new ReferenceRange(Optional.<String>empty(), Optional.of("1000000"));

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

    @Test
    public void accepts_whenGlobalRange_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(Optional.<String>empty(), Optional.<String>empty());

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
    public void accepts_whenRefNotInList_shouldReturnTrue(){
        // GIVEN
        ReferenceRange actualRange = new ReferenceRange(new ArrayList<>());

        // WHEN-THEN
        assertThat(actualRange.accepts("12345678")).isFalse();
    }

}