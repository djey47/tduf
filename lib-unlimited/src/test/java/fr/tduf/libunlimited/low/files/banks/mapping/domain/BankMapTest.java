package fr.tduf.libunlimited.low.files.banks.mapping.domain;

import org.assertj.core.groups.Tuple;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class BankMapTest {

    @Test
    public void magifyAll_shouldMagifyAllEntries() throws Exception {
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
}
