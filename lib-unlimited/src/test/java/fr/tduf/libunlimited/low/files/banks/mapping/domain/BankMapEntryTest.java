package fr.tduf.libunlimited.low.files.banks.mapping.domain;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BankMapEntryTest {

    private BankMap bankMap;

    @Before
    public void setUp() {
        bankMap = new BankMap();
    }

    @Test
    public void magify_shouldSetEntrySizesTo0() throws Exception {
        // GIVEN
        BankMap.Entry entry = bankMap.addEntry(1589L, 150L, 150L);

        // WHEN
        entry.magify();

        // THEN
        assertThat(entry.getSize1()).isEqualTo(0);
        assertThat(entry.getSize2()).isEqualTo(0);
    }

    @Test
    public void isMagic_whenAllSizes0_shouldReturnTrue() {
        // GIVEN
        BankMap.Entry entry = bankMap.addEntry(1589L, 0L, 0L);

        // WHEN-THEN
        assertThat(entry.isMagic()).isTrue();
    }

    @Test
    public void isMagic_whenAllSizesNot0_shouldReturnFalse() {
        // GIVEN
        BankMap.Entry entry = bankMap.addEntry(1589L, 150L, 150L);

        // WHEN-THEN
        assertThat(entry.isMagic()).isFalse();
    }

    @Test
    public void isMagic_whenOneSizeNot0_shouldReturnFalse() {
        // GIVEN
        BankMap.Entry entry = bankMap.addEntry(1589L, 0L, 150L);

        // WHEN-THEN
        assertThat(entry.isMagic()).isFalse();
    }
}
