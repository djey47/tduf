package fr.tduf.libunlimited.low.files.db.dto.content;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentEntryDtoTest {

    private ContentEntryDto contentEntry;

    @BeforeEach
    void setUp() {
        contentEntry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("V1").build())
                .build();
    }

    @Test
    void addItemAtRank_shouldUpdateItemRank() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(3).build();

        // WHEN
        contentEntry.addItemAtRank(1, item);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(2);
        final ContentItemDto actualItem = contentEntry.getItems().get(0);
        assertThat(actualItem.getFieldRank()).isEqualTo(1);
        assertThat(actualItem).isSameAs(item);
    }

    @Test
    void addItemAtRank_shouldRecomputeValuesHash() {
        // GIVEN
        ContentItemDto item2 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V2").build();
        ContentItemDto item3 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V3").build();
        contentEntry.appendItem(item2);
        int hash = contentEntry.getValuesHash();

        // WHEN
        contentEntry.addItemAtRank(2, item3);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(3);
        assertThat(contentEntry.getValuesHash()).isNotEqualTo(hash);
        final ContentItemDto actualItem2 = contentEntry.getItems().get(1);
        assertThat(actualItem2.getFieldRank()).isEqualTo(2);
        assertThat(actualItem2).isSameAs(item3);
        final ContentItemDto actualItem3 = contentEntry.getItems().get(2);
        assertThat(actualItem3.getFieldRank()).isEqualTo(3);
        assertThat(actualItem3).isSameAs(item2);
    }

    @Test
    void appendItem_shouldUpdateItemRank() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(3).build();

        // WHEN
        contentEntry.appendItem(item);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(2);
        final ContentItemDto actualItem = contentEntry.getItems().get(1);
        assertThat(actualItem.getFieldRank()).isEqualTo(2);
        assertThat(actualItem).isSameAs(item);
    }

    @Test
    void replaceItems_shouldRecomputeValuesHash() {
        // GIVEN
        ContentItemDto item1 = ContentItemDto.builder().ofFieldRank(1).withRawValue("V1").build();
        contentEntry.appendItem(item1);
        ContentItemDto item2 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V2").build();
        ContentItemDto item3 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V3").build();
        List<ContentItemDto> newItems = asList(item2, item3);
        int hash = contentEntry.getValuesHash();

        // WHEN
        contentEntry.replaceItems(newItems);

        // THEN
        assertThat(contentEntry.getItems()).hasSize(2);
        assertThat(contentEntry.getValuesHash()).isNotEqualTo(hash);
        final ContentItemDto actualItem1 = contentEntry.getItems().get(0);
        assertThat(actualItem1).isSameAs(item2);
        final ContentItemDto actualItem2 = contentEntry.getItems().get(1);
        assertThat(actualItem2).isSameAs(item3);
    }

    @Test
    void getItems_shouldReturnReadOnlyList(){
        // GIVEN-WHEN
        List<ContentItemDto> actualItems = contentEntry.getItems();

        // THEN
        assertThat(actualItems).hasSize(1);
        assertThrows(UnsupportedOperationException.class,
                () -> actualItems.add(ContentItemDto.builder().ofFieldRank(2).build()));
    }

    @Test
    void getItemAtRank_whenNoItemAtThisRank_shouldReturnEmpty() {
        // GIVEN-WHEN
        Optional<ContentItemDto> actualItem = contentEntry.getItemAtRank(2);

        // THEN
        assertThat(actualItem).isEmpty();
    }

    @Test
    void getItemAtRank_whenItemExists_shouldReturnIt() {
        // GIVEN
        ContentItemDto item2 = ContentItemDto.builder().ofFieldRank(2).withRawValue("V2").build();
        ContentItemDto item3 = ContentItemDto.builder().ofFieldRank(3).withRawValue("V3").build();
        contentEntry.appendItem(item2);
        contentEntry.appendItem(item3);

        // WHEN
        Optional<ContentItemDto> actualItem = contentEntry.getItemAtRank(2);

        // THEN
        assertThat(actualItem).contains(item2);
    }

    @Test
    void updateItemValueAtRank_whenNoItemAtThisRank_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> contentEntry.updateItemValueAtRank("V", 2));
    }

    @Test
    void updateItemValueAtRank_whenItemAtThisRank_andValueAlreadyChanged_shouldReturnEmpty() {
        // GIVEN-WHEN
        Optional<ContentItemDto> actualResult = contentEntry.updateItemValueAtRank("V1", 1);

        // THEN
        assertThat(actualResult).isEmpty();
    }

    @Test
    void updateItemValueAtRank_whenItemAtThisRank_shouldReturnModifiedItem_andRecomputeValuesHash() {
        // GIVEN
        int hash = contentEntry.getValuesHash();

        // WHEN
        Optional<ContentItemDto> actualResult = contentEntry.updateItemValueAtRank("V2", 1);

        // THEN
        assertThat(actualResult.get().getRawValue()).isEqualTo("V2");
        assertThat(contentEntry.getValuesHash()).isNotEqualTo(hash);
    }

    @Test
    void getId_whenUnattachedEntry_shouldReturnMinusOne() {
        // GIVEN-WHEN-THEN
        assertThat(contentEntry.getId()).isEqualTo(-1);
    }

    @Test
    void getId_whenAttachedEntry_shouldReturnIndexInList() {
        // GIVEN
        DbDataDto.builder().forTopic(CAR_PHYSICS_DATA).addEntry(contentEntry).build();

        // WHEN
        int actualId = contentEntry.getId();

        // THEN
        assertThat(actualId).isEqualTo(0);
    }

    @Test
    void getValuesHash_whenNoItems_shouldReturnOne() {
        // GIVEN-WHEN-THEN
        assertThat(createEntryWithoutItems().getValuesHash()).isEqualTo(1);
    }

    private ContentEntryDto createEntryWithoutItems() {
        return ContentEntryDto.builder().build();
    }

    @Test
    void getValuesHash_whenItems_shouldReturnNorZeroNorOne() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(1).withRawValue("V1").build();
        contentEntry.appendItem(item);

        // WHEN-THEN
        assertThat(contentEntry.getValuesHash())
                .isNotZero()
                .isNotEqualTo(1);
    }

    @Test
    void getFirstItemValue_whenNoItem_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> createEntryWithoutItems().getNativeRef());
    }

    @Test
    void getFirstItemValue_whenSingleItem_shouldReturnIt() {
        // GIVEN
        ContentItemDto item = ContentItemDto.builder().ofFieldRank(1).withRawValue("V1").build();
        contentEntry.appendItem(item);

        // WHEN
        String actualItemValue = contentEntry.getNativeRef();

        // THEN
        assertThat(actualItemValue).isEqualTo("V1");
    }       
    
    @Test
    void getPseudoRef_whenSingleItem_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> contentEntry.getPseudoRef());
    }
    
    @Test
    void getPseudoRef_whenTwoItems_shouldReturnIt() {
        // GIVEN
        contentEntry.appendItem(ContentItemDto.builder().ofFieldRank(2).withRawValue("V2").build());

        // WHEN
        String actualItemValue = contentEntry.getPseudoRef();

        // THEN
        assertThat(actualItemValue).isEqualTo("V1|V2");
    }
}
