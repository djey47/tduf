package fr.tduf.libunlimited.low.files.db.dto;

import junit.framework.TestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbResourceDtoTest {

    @Test
    public void isComment_whenEntryIsComment_shouldReturnTrue() {
        //GIVEN
        DbResourceDto.Entry entry = DbResourceDto.Entry.builder()
                .withId(1L)
                .withComment("This is a comment")
                .build();

        //WHEN-THEN
        assertThat(entry.isComment()).isTrue();
    }

    @Test
    public void isComment_whenEntryHasLocalizedValue_shouldReturnFalse() {
        //GIVEN
        DbResourceDto.LocalizedValue localizedValue = DbResourceDto.LocalizedValue.builder()
                .withLocale(DbResourceDto.Locale.JAPAN)
                .withValue("Value")
                .build();
        DbResourceDto.Entry entry = DbResourceDto.Entry.builder()
                .withId(1L)
                .withReference("53410835")
                .addLocalizedValue(localizedValue)
                .build();

        //WHEN-THEN
        assertThat(entry.isComment()).isFalse();
    }
}