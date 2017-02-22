package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FormulaHelperTest {

    @Test
    public void resolveToInteger_whenNullFormula_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger(null, Optional.empty(), null)).isNull();
    }

    @Test
    public void resolveToInteger_whenSingleInteger_shouldReturnIt() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("100", Optional.empty(), null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=100", Optional.empty(), null)).isEqualTo(100);
    }

    @Test
    public void resolveToInteger_whenSimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=1+1", Optional.empty(), null)).isEqualTo(2);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("sizeIndicator", 500L);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", Optional.empty(), dataStore)).isEqualTo(500);
    }

    @Test
    public void resolveToInteger_whenVerySimpleFormulaWithPointerFromRepeatedField_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("fileList[5].sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", Optional.of("fileList[5]."), dataStore)).isEqualTo(500);
    }

    @Test
    public void resolveToInteger_whenSimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?*4", Optional.empty(), dataStore)).isEqualTo(2000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenFormulaWithPointerAndValueNotInStore_shouldThrowException() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();

        // WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", Optional.empty(), dataStore);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenFormulaWithPointerAndNoStore_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=?sizeIndicator?", Optional.empty(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenEmptyString_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("", Optional.empty(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenNotAFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("AZERTY", Optional.empty(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveToInteger_whenIllegalFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        FormulaHelper.resolveToInteger("=AZERTY", Optional.empty(), null);
    }

    private DataStore createDefaultDataStore() {
        return new DataStore(FileStructureDto.builder().build());
    }
}
