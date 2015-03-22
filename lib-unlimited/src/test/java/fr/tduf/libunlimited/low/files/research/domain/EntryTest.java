package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EntryTest {
    @Test
    public void copy_shouldMakeFullEntryCopy() {
        // GIVEN
        Entry entry = new Entry(FileStructureDto.Type.GAP, new byte[] { 0x0 });

        // WHEN
        Entry actualCopy = entry.copy();

        // THEN
        assertThat(actualCopy).isEqualTo(entry);
        assertThat(actualCopy).isNotSameAs(entry);
        assertThat(actualCopy.getRawValue()).isNotSameAs(entry.getRawValue());
    }
}