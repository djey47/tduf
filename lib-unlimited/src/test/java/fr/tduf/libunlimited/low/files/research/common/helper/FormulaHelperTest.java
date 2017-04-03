package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormulaHelperTest {

    @Test
    void resolveToInteger_whenNullFormula_shouldReturnNull() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger(null, null, null)).isNull();
    }

    @Test
    void resolveToInteger_whenSingleInteger_shouldReturnIt() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("100", null, null)).isEqualTo(100);
    }

    @Test
    void resolveToInteger_whenVerySimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=100", null, null)).isEqualTo(100);
    }

    @Test
    void resolveToInteger_whenSimpleFormula_shouldReturnValue() {
        // GIVEN-WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=1+1", null, null)).isEqualTo(2);
    }

    @Test
    void resolveToInteger_whenVerySimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("sizeIndicator", 500L);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", null, dataStore)).isEqualTo(500);
    }

    @Test
    void resolveToInteger_whenVerySimpleFormulaWithPointerFromRepeatedField_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("fileList[5].sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?", "fileList[5].", dataStore)).isEqualTo(500);
    }

    @Test
    void resolveToInteger_whenSimpleFormulaWithPointer_shouldReturnValue() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();
        dataStore.addInteger32("sizeIndicator", 500);

        // WHEN-THEN
        assertThat(FormulaHelper.resolveToInteger("=?sizeIndicator?*4", null, dataStore)).isEqualTo(2000);
    }

    @Test
    void resolveToInteger_whenFormulaWithPointerAndValueNotInStore_shouldThrowException() {
        // GIVEN
        DataStore dataStore = createDefaultDataStore();

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FormulaHelper.resolveToInteger("=?sizeIndicator?", null, dataStore));
    }

    @Test
    void resolveToInteger_whenFormulaWithPointerAndNoStore_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FormulaHelper.resolveToInteger("=?sizeIndicator?", null, null));
    }

    @Test
    void resolveToInteger_whenEmptyString_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FormulaHelper.resolveToInteger("", null, null));
    }

    @Test
    void resolveToInteger_whenNotAFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FormulaHelper.resolveToInteger("AZERTY", null, null));
    }

    @Test
    void resolveToInteger_whenIllegalFormula_shouldThrowIllegalArgumentException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> FormulaHelper.resolveToInteger("=AZERTY", null, null));
    }

    private DataStore createDefaultDataStore() {
        return new DataStore(FileStructureDto.builder().build());
    }
}
