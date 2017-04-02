package fr.tduf.libunlimited.low.files.banks.mapping.domain;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BankMapTest {

    @Test
    void magifyAll_shouldMagifyAllEntries() throws Exception {
        // GIVEN
        BankMap bankMap = new BankMap();
        bankMap.addEntry(1589L, 150L, 150L);
        bankMap.addMagicEntry(1590L);

        // WHEN
        bankMap.magifyAll();

        // THEN
        assertThat(bankMap.getEntries())
                .extracting(BankMap.Entry::getSize1, BankMap.Entry::getSize2)
                .containsOnly(new Tuple(0L, 0L));
    }

    @Test
    void isMagic_whenNoEntry_shouldReturnFalse() {
        // GIVEN
        BankMap bankMap = new BankMap();

        // WHEN-THEN
        assertThat(bankMap.isMagic()).isFalse();
    }

    @Test
    void isMagic_whenAllStandardEntries_shouldReturnFalse() {
        // GIVEN
        BankMap bankMap = new BankMap();
        bankMap.addEntry(1589L, 150L, 150L);
        bankMap.addEntry(1590L, 160L, 160L);

        // WHEN-THEN
        assertThat(bankMap.isMagic()).isFalse();
    }

    @Test
    void isMagic_whenOneEntryMagic_shouldReturnFalse() {
        // GIVEN
        BankMap bankMap = new BankMap();
        bankMap.addEntry(1589L, 150L, 150L);
        bankMap.addEntry(1589L, 0L, 150L);
        bankMap.addMagicEntry(1590L);

        // WHEN-THEN
        assertThat(bankMap.isMagic()).isFalse();
    }

    @Test
    void isMagic_whenAllEntriesMagic_shouldReturnTrue() {
        // GIVEN
        BankMap bankMap = new BankMap();
        bankMap.addMagicEntry(1589L);
        bankMap.addMagicEntry(1590L);

        // WHEN-THEN
        assertThat(bankMap.isMagic()).isTrue();
    }


}
