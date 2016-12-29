package fr.tduf.libunlimited.low.files.db.dto.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbDataDtoTest {

    private DbDataDto dataObjectWithoutREFSupport;
    private DbDataDto dataObjectWithREFSupport;

    @BeforeEach
    void setUp() {
        dataObjectWithoutREFSupport = createDataWithoutRefSupport();
        dataObjectWithREFSupport = createDataWithRefSupport();
    }

    @Test
    void buildItem_whenNullRawValue_andBitfield_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue(null)
                .bitFieldForTopic(true, CAR_PHYSICS_DATA)
                .build());
    }

    @Test
    void buildItem_whenNullTopic_andBitfield_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue("111")
                .bitFieldForTopic(true, null)
                .build());
    }

    @Test
    void buildItem_whenNullRawValueAndNullTopic_andNoBitfield_shouldNotSetSwitchValues() {
        // GIVEN-WHEN
        ContentItemDto actualItem = ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue(null)
                .build();

        // THEN
        assertThat(actualItem.getSwitchValues()).isNull();
    }

    @Test
    void buildItem_whenBitfield_shouldReturnCorrectSwitchValues() {
        // GIVEN-WHEN
        List<SwitchValueDto> actualValues = ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue("111")
                .bitFieldForTopic(true, CAR_PHYSICS_DATA)
                .build()
                .getSwitchValues();

        // THEN
        assertThat(actualValues).hasSize(7);
        assertThat(actualValues).extracting("index").containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(actualValues).extracting("name").containsExactly("Vehicle slot enabled", "?", "?", "?", "?", "Add-on key required", "Car Paint Luxe enabled");
        assertThat(actualValues).extracting("enabled").containsExactly(true, true, true, true, false, true, true);
    }

    @Test
    void prepareSwitchValues_whenBitfield_andReferenceNotFound_shouldReturnEmptyList() {
        // GIVEN-WHEN
        List<SwitchValueDto> actualValues = ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue("111")
                .bitFieldForTopic(true, CAR_RIMS)
                .build()
                .getSwitchValues();

        // THEN
        assertThat(actualValues).isEmpty();
    }

    @Test
    void getEntries_shouldReturnNewReadOnlyList() {
        // GIVEN
        final ContentEntryDto contentEntry = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry);

        // WHEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();

        // THEN
        assertThat(actualEntries).containsExactly(contentEntry);
        assertThrows(UnsupportedOperationException.class,
                () -> actualEntries.add(contentEntry));
    }

    @Test
    void getEntryId_whenUnattachedEntry_shouldReturnMinusOne() {
        // GIVEN
        ContentEntryDto contentEntry = ContentEntryDto.builder().build();

        // WHEN-THEN
        assertThat(dataObjectWithoutREFSupport.getEntryId(contentEntry)).isEqualTo(-1);
    }

    @Test
    void getEntryId_whenAattachedEntries_shouldReturnListRanks() {
        // GIVEN
        ContentEntryDto contentEntry1 = createContentEntryWithReference("1");
        ContentEntryDto contentEntry2 = createContentEntryWithReference("2");
        dataObjectWithREFSupport.addEntry(contentEntry1);
        dataObjectWithREFSupport.addEntry(contentEntry2);

        // WHEN-THEN
        assertThat(dataObjectWithREFSupport.getEntryId(contentEntry1)).isEqualTo(0);
        assertThat(dataObjectWithREFSupport.getEntryId(contentEntry2)).isEqualTo(1);
    }

    @Test
    void getEntryWithInternalIdentifier_whenUnknownId_shouldReturnEmpty() {
        // GIVEN-WHEN-THEN
        assertThat(dataObjectWithoutREFSupport.getEntryWithInternalIdentifier(560)).isEmpty();
    }

    @Test
    void getEntryWithInternalIdentifier_whenExistingEntry_shouldReturnIt() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntryWithPseudoReference("1", "1");
        dataObjectWithoutREFSupport.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObjectWithoutREFSupport.getEntryWithInternalIdentifier(0);

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    void getEntryWithReference_whenNoReferenceIndex_andUnknownEntry_shouldReturnEmpty() {
        // GIVEN-WHEN-THEN
        assertThat(dataObjectWithoutREFSupport.getEntryWithReference("REF")).isEmpty();
    }

    @Test
    void getEntryWithReference_whenPseudoRef_shouldCheckTwoFirstFields() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntryWithPseudoReference("100", "101");
        dataObjectWithoutREFSupport.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObjectWithoutREFSupport.getEntryWithReference("100|101");

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    void getEntryWithReference_whenReferenceIndex_andUnknownEntry_shouldReturnEmpty() {
        // GIVEN
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();

        // WHEN-THEN
        assertThat(dataObjectWithRefSupport.getEntryWithReference("REF")).isEmpty();
    }

    @Test
    void getEntryWithReference_whenReferenceIndex_andKnownEntry_shouldReturnIt() {
        // GIVEN
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();
        ContentEntryDto contentEntry = createContentEntry();
        dataObjectWithRefSupport.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObjectWithRefSupport.getEntryWithReference("REF");

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    void setEntries_withoutRefSupport_shouldInitProperties() {
        // GIVEN
        final ContentEntryDto contentEntry = createContentEntryWithPseudoReference("1", "1");

        // WHEN
        dataObjectWithoutREFSupport.setEntries(singletonList(contentEntry));

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithoutREFSupport.getEntries();
        assertThat(actualEntries).hasSize(1);
        final ContentEntryDto actualEntry = actualEntries.get(0);
        assertThat(actualEntry.getDataHost()).isSameAs(dataObjectWithoutREFSupport);
        assertThat(actualEntry.getValuesHash()).isNotZero();
    }

    @Test
    void setEntries_withoutRefSupport_shouldCreateIndexAsWell() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntryWithPseudoReference("1", "1");
        final ContentEntryDto contentEntry2 = createContentEntryWithPseudoReference("1", "2");
        final ContentEntryDto contentEntry3 = createContentEntryWithPseudoReference("2", "1");
        final ContentEntryDto contentEntry4 = createContentEntryWithPseudoReference("2", "2");
        final List<ContentEntryDto> entries = asList(contentEntry1, contentEntry2, contentEntry3, contentEntry4);

        // WHEN
        dataObjectWithoutREFSupport.setEntries(entries);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithoutREFSupport.getEntries();
        assertThat(actualEntries).hasSize(4);
        assertThat(dataObjectWithoutREFSupport.getEntriesByReference()).hasSize(4);
        assertThat(dataObjectWithoutREFSupport.getEntriesByReference().keySet()).contains("1|1", "1|2", "2|1", "2|2");
    }

    @Test
    void setEntries_withRefSupport_shouldCreateIndex() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntryWithReference("REF1");
        final ContentEntryDto contentEntry2 = createContentEntryWithReference("REF2");
        final ContentEntryDto contentEntry3 = createContentEntryWithReference("REF3");
        final ContentEntryDto contentEntry4 = createContentEntryWithReference("REF4");
        final List<ContentEntryDto> entries = asList(contentEntry1, contentEntry2, contentEntry3, contentEntry4);

        // WHEN
        dataObjectWithREFSupport.setEntries(entries);

        // THEN
        final Map<String, ContentEntryDto> actualIndex = dataObjectWithREFSupport.getEntriesByReference();
        assertThat(actualIndex).hasSize(4);
        assertThat(actualIndex.keySet()).contains("REF1", "REF2", "REF3", "REF4");
    }

    @Test
    void addEntry_shouldUpdateContext() {
        // GIVEN
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();
        ContentEntryDto contentEntry = createContentEntry();

        // WHEN
        dataObjectWithRefSupport.addEntry(contentEntry);

        // THEN
        assertThat(contentEntry.getDataHost()).isSameAs(dataObjectWithRefSupport);
        assertThat(dataObjectWithRefSupport.getEntries()).containsExactly(contentEntry);
        assertThat(dataObjectWithRefSupport.getEntriesByReference())
                .containsOnlyKeys("REF")
                .containsValues(contentEntry);
    }

    @Test
    void addEntryWithItems() {
        // GIVEN
        List<ContentItemDto> items = singletonList(ContentItemDto.builder().ofFieldRank(1).withRawValue("1").build());
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();

        // WHEN
        dataObjectWithRefSupport.addEntryWithItems(items);

        // THEN
        List<ContentEntryDto> actualEntries = dataObjectWithRefSupport.getEntries();
        assertThat(actualEntries).hasSize(1);
        List<ContentItemDto> actualItems = actualEntries.get(0).getItems();
        assertThat(actualItems).isEqualTo(items);
    }

    @Test
    void removeEntry() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry);

        // WHEN
        dataObjectWithREFSupport.removeEntry(contentEntry);

        // THEN
        assertThat(dataObjectWithREFSupport.getEntries()).isEmpty();
    }

    @Test
    void removeEntry_withRefSupport_shouldRemoveFromIndex() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry);

        // WHEN
        dataObjectWithREFSupport.removeEntry(contentEntry);

        // THEN
        assertThat(dataObjectWithREFSupport.getEntriesByReference()).isEmpty();
    }

    @Test
    void removeEntry_withoutRefSupport_shouldRemoveFromIndex() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntryWithPseudoReference("1", "1");
        dataObjectWithoutREFSupport.addEntry(contentEntry);

        // WHEN
        dataObjectWithoutREFSupport.removeEntry(contentEntry);

        // THEN
        assertThat(dataObjectWithoutREFSupport.getEntriesByReference()).isEmpty();
    }

    @Test
    void removeEntries_shouldRemoveExisting() {
        // GIVEN
        ContentEntryDto contentEntry1 = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF1").build())
                .build();
        ContentEntryDto contentEntry2 = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF2").build())
                .build();
        ContentEntryDto contentEntry3 = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF3").build())
                .build();
        dataObjectWithREFSupport.addEntry(contentEntry1);
        dataObjectWithREFSupport.addEntry(contentEntry2);

        // WHEN
        dataObjectWithREFSupport.removeEntries(asList(contentEntry2, contentEntry3));

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItems()).extracting("rawValue").containsExactly("REF1");
    }

    @Test
    void moveEntryUp_whenUnattachedEntry_shouldDoNothing() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        final ContentEntryDto contentEntry4 = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry1);
        dataObjectWithREFSupport.addEntry(contentEntry2);
        dataObjectWithREFSupport.addEntry(contentEntry3);

        // WHEN
        dataObjectWithREFSupport.moveEntryUp(contentEntry4);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry2);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry3);
    }

    @Test
    void moveEntryUp_whenEntryCanBeMoved() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry1);
        dataObjectWithREFSupport.addEntry(contentEntry2);
        dataObjectWithREFSupport.addEntry(contentEntry3);

        // WHEN
        dataObjectWithREFSupport.moveEntryUp(contentEntry2);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry2);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry3);
        assertThat(actualEntries).extracting("id").containsExactly(0, 1, 2);
    }

    @Test
    void moveEntryDown_whenEntryCannotBeMoved_shouldDoNothing() {
        // GIVEN
        final ContentEntryDto contentEntry = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry);

        // WHEN
        dataObjectWithREFSupport.moveEntryDown(contentEntry);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry);
        assertThat(actualEntries).extracting("id").containsExactly(0);
    }

    @Test
    void moveEntryDown_whenEntryCanBeMoved() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        dataObjectWithREFSupport.addEntry(contentEntry1);
        dataObjectWithREFSupport.addEntry(contentEntry2);
        dataObjectWithREFSupport.addEntry(contentEntry3);

        // WHEN
        dataObjectWithREFSupport.moveEntryDown(contentEntry2);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObjectWithREFSupport.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry3);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry2);
        assertThat(actualEntries).extracting("id").containsExactly(0, 1, 2);
    }

    private DbDataDto createDataWithRefSupport() {
        return DbDataDto.builder()
                    .forTopic(CAR_PHYSICS_DATA)
                    .build();
    }

    private DbDataDto createDataWithoutRefSupport() {
        return DbDataDto.builder()
                    .forTopic(CAR_RIMS)
                    .build();
    }

    private ContentEntryDto createContentEntry() {
        return ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF").build())
                .build();
    }

    private ContentEntryDto createContentEntryWithReference(String ref) {
        return ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(ref).build())
                .build();
    }

    private ContentEntryDto createContentEntryWithPseudoReference(String ref1, String ref2) {
        return ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(ref1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(ref2).build())
                .build();
    }
}
