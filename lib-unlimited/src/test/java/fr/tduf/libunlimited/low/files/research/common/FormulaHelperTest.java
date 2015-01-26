package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class FormulaHelperTest {

    @Test
    public void resolveToInteger_whenNullFormula_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger(null, null, null)).isNull();
    }

    @Test
    public void resolveToInteger_whenSingleInteger_shouldReturnIt() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("100", null, null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=100", null, null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenSimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=1+1", null, null)).isEqualTo(2);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = new DataStore();
        dataStore.addInteger("sizeIndicator", 500L);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", null, dataStore)).isEqualTo(500);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormulaWithPointerFromRepeatedField_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = new DataStore();
        dataStore.addInteger("fileList[5].sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", "fileList[5].", dataStore)).isEqualTo(500);
    }

    @Test
    public void resolveToInteger_whenSimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = new DataStore();
        dataStore.addInteger("sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?*4", null, dataStore)).isEqualTo(2000);
    }

    @Test(expected = NoSuchElementException.class)
    public void resolveToInteger_whenFormulaWithPointerAndValueNotInStore_shouldThrowException() {
        // GIVEN
        DataStore dataStore = new DataStore();

        // WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", null, dataStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenFormulaWithPointerAndNoStore_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenEmptyString_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenNotAFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("AZERTY", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenIllegalFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=AZERTY", null, null);
    }
}