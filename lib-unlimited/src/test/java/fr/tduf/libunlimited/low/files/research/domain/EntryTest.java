package fr.tduf.libunlimited.low.files.research.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EntryTest {
    @Test
    public void copy_shouldMakeFullEntryCopy() {
        // GIVEN
        Entry entry = new Entry(Type.GAP, true, 1, new byte[] { 0x0 });

        // WHEN
        Entry actualCopy = entry.copy();

        // THEN
        assertThat(actualCopy).isEqualTo(entry);
        assertThat(actualCopy).isNotSameAs(entry);
        assertThat(actualCopy.getRawValue()).isNotSameAs(entry.getRawValue());
    }
}