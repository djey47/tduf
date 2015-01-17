package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class FormulaHelperTest {

    @Test
    public void resolveToInteger_whenNullFormula_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger(null, null)).isNull();
    }

    @Test
    public void resolveToInteger_whenSingleInteger_shouldReturnIt() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("100", null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenSimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=100", null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = new DataStore();
        dataStore.addRawValue("sizeIndicator", TypeHelper.integerToRaw(500L));

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", dataStore)).isEqualTo(500);
    }

    @Test(expected = NoSuchElementException.class)
    public void resolveToInteger_whenFormulaWithPointerAndValueNotInStore_shouldThrowException() {
        // GIVEN
        DataStore dataStore = new DataStore();

        // WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", dataStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenFormulaWithPointerAndNoStore_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenEmptyString_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenNotAFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("AZERTY", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenIllegalFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=AZERTY", null);
    }
}