package fr.tduf.libunlimited.low.files.db.dto.content;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentEntryDtoTest {

    private ContentEntryDto contentEntry;

    @Before
    public void setUp() {
        contentEntry = ContentEntryDto.builder().build();
    }

    @Test
    public void addItemAtRank_shouldUpdateItemRank() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(3).build();

        // WHEN
        contentEntry.addItemAtRank(1, item);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(1);
        final ContentItemDto actualItem = contentEntry.getItems().get(0);
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem).isSameAs(item);
    }

    @Test
    public void addItemAtRank_shouldRecomputeValuesHash() {
        // GIVEN
        ContentItemDto item1 = ContentItemDto.builder().ofFieldRank(1).withRawValue("V1").build();
        ContentItemDto item2 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V2").build();
        ContentItemDto item3 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V3").build();
        contentEntry.appendItem(item1);
        contentEntry.appendItem(item2);
        int hash = contentEntry.getValuesHash();

        // WHEN
        contentEntry.addItemAtRank(2, item3);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(3);
        assertThat(contentEntry.getValuesHash()).isNotEqualTo(hash);
        final ContentItemDto actualItem1 = contentEntry.getItems().get(0);
        assertThat(actualItem1.getFieldRank()).isEqualTo(1);
        assertThat(actualItem1).isSameAs(item1);
        final ContentItemDto actualItem2 = contentEntry.getItems().get(1);
        assertThat(actualItem2.getFieldRank()).isEqualTo(2);
        assertThat(actualItem2).isSameAs(item3);
        final ContentItemDto actualItem3 = contentEntry.getItems().get(2);
        assertThat(actualItem3.getFieldRank()).isEqualTo(3);
        assertThat(actualItem3).isSameAs(item2);
    }

    @Test
    public void appendItem_shouldUpdateItemRank() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(3).build();

        // WHEN
        contentEntry.appendItem(item);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(1);
        final ContentItemDto actualItem = contentEntry.getItems().get(0);
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem).isSameAs(item);
    }
}
