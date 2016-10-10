package fr.tduf.libunlimited.low.files.db.dto.content;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DbDataDtoTest {

    private DbDataDto dataObject;

    @Before
    public void setUp() {
        dataObject = DbDataDto.builder()
                .forTopic(CAR_RIMS)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void buildItem_whenNullRawValue_andBitfield_shouldThrowException() {
        // GIVEN-WHEN
        ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue(null)
                .bitFieldForTopic(true, CAR_PHYSICS_DATA)
                .build();

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void buildItem_whenNullTopic_andBitfield_shouldThrowException() {
        // GIVEN-WHEN
        ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue("111")
                .bitFieldForTopic(true, null)
                .build();

        // THEN: NPE
    }

    @Test
    public void buildItem_whenNullRawValueAndNullTopic_andNoBitfield_shouldNotSetSwitchValues() {
        // GIVEN-WHEN
        ContentItemDto actualItem = ContentItemDto.builder()
                .ofFieldRank(101)
                .withRawValue(null)
                .build();

        // THEN
        assertThat(actualItem.getSwitchValues()).isNull();
    }

    @Test
    public void buildItem_whenBitfield_shouldReturnCorrectSwitchValues() {
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
    public void prepareSwitchValues_whenBitfield_andReferenceNotFound_shouldReturnEmptyList() {
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

    @Test(expected = UnsupportedOperationException.class)
    public void getEntries_shouldReturnNewReadOnlyList() {
        // GIVEN
        final ContentEntryDto contentEntry = ContentEntryDto.builder().build();
        dataObject.addEntry(contentEntry);

        // WHEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();

        // THEN
        assertThat(actualEntries).containsExactly(contentEntry);
        actualEntries.add(contentEntry);
    }

    @Test
    public void getEntryId_whenUnattachedEntry_shouldReturnMinusOne() {
        // GIVEN
        ContentEntryDto contentEntry = ContentEntryDto.builder().build();

        // WHEN-THEN
        assertThat(dataObject.getEntryId(contentEntry)).isEqualTo(-1);
    }

    @Test
    public void getEntryId_whenAattachedEntries_shouldReturnListRanks() {
        // GIVEN
        ContentEntryDto contentEntry1 = ContentEntryDto.builder().build();
        ContentEntryDto contentEntry2 = ContentEntryDto.builder().build();
        dataObject.addEntry(contentEntry1);
        dataObject.addEntry(contentEntry2);

        // WHEN-THEN
        assertThat(dataObject.getEntryId(contentEntry1)).isEqualTo(0);
        assertThat(dataObject.getEntryId(contentEntry2)).isEqualTo(1);
    }

    @Test
    public void getEntryWithInternalIdentifier_whenUnknownId_shouldReturnEmpty() {
        // GIVEN-WHEN-THEN
        assertThat(dataObject.getEntryWithInternalIdentifier(560)).isEmpty();
    }

    @Test
    public void getEntryWithInternalIdentifier_whenExistingEntry_shouldReturnIt() {
        // GIVEN
        ContentEntryDto contentEntry = ContentEntryDto.builder().build();
        dataObject.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObject.getEntryWithInternalIdentifier(0);

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    public void getEntryWithReference_whenNoReferenceIndex_andUnknownEntry_shouldReturnEmpty() {
        // GIVEN-WHEN-THEN
        assertThat(dataObject.getEntryWithReference("REF")).isEmpty();
    }

    @Test
    public void getEntryWithReference_whenNoReferenceIndex_shouldCheckFirstField() {
        // GIVEN
        ContentEntryDto contentEntry = createContentEntry();
        dataObject.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObject.getEntryWithReference("REF");

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    public void getEntryWithReference_whenReferenceIndex_andUnknownEntry_shouldReturnEmpty() {
        // GIVEN
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();

        // WHEN-THEN
        assertThat(dataObjectWithRefSupport.getEntryWithReference("REF")).isEmpty();
    }

    @Test
    public void getEntryWithReference_whenReferenceIndex_andKnownEntry_shouldReturnIt() {
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
    public void setEntries_withoutRefSupport_shouldInitProperties() {
        // GIVEN
        final ContentEntryDto contentEntry = createContentEntry();

        // WHEN
        dataObject.setEntries(singletonList(contentEntry));

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(1);
        final ContentEntryDto actualEntry = actualEntries.get(0);
        assertThat(actualEntry.getDataHost()).isSameAs(dataObject);
        assertThat(actualEntry.getValuesHash()).isNotZero();
    }

    @Test
    public void setEntries_withoutRefSupport_shouldNotCreateIndex() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        final ContentEntryDto contentEntry4 = createContentEntry();
        final List<ContentEntryDto> entries = asList(contentEntry1, contentEntry2, contentEntry3, contentEntry4);

        // WHEN
        dataObject.setEntries(entries);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(4);
        assertThat(dataObject.getEntriesByReference()).isNull();
    }

    @Test
    public void setEntries_withRefSupport_shouldCreateIndex() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntryWithReference("REF1");
        final ContentEntryDto contentEntry2 = createContentEntryWithReference("REF2");
        final ContentEntryDto contentEntry3 = createContentEntryWithReference("REF3");
        final ContentEntryDto contentEntry4 = createContentEntryWithReference("REF4");
        final List<ContentEntryDto> entries = asList(contentEntry1, contentEntry2, contentEntry3, contentEntry4);
        final DbDataDto dataWithRefSupport = createDataWithRefSupport();

        // WHEN
        dataWithRefSupport.setEntries(entries);

        // THEN
        final Map<String, ContentEntryDto> actualIndex = dataWithRefSupport.getEntriesByReference();
        assertThat(actualIndex).hasSize(4);
        assertThat(actualIndex.keySet()).contains("REF1", "REF2", "REF3", "REF4");
    }

    @Test
    public void addEntry_shouldUpdateContext() {
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
    public void addEntryWithItems() {
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
    public void removeEntry() {
        // GIVEN
        ContentEntryDto contentEntry = ContentEntryDto.builder().build();
        dataObject.addEntry(contentEntry);

        // WHEN
        dataObject.removeEntry(contentEntry);

        // THEN
        assertThat(dataObject.getEntries()).isEmpty();
    }

    @Test
    public void removeEntry_withRefSupport_shouldAlsoRemoveFromIndex() {
        // GIVEN
        DbDataDto dataWithRefSupport = createDataWithRefSupport();
        ContentEntryDto contentEntry = createContentEntry();
        dataWithRefSupport.addEntry(contentEntry);

        // WHEN
        dataWithRefSupport.removeEntry(contentEntry);

        // THEN
        assertThat(dataWithRefSupport.getEntriesByReference()).doesNotContainKey("REF");
    }

    @Test
    public void removeEntries_shouldRemoveExisting() {
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
        dataObject.addEntry(contentEntry1);
        dataObject.addEntry(contentEntry2);

        // WHEN
        dataObject.removeEntries(asList(contentEntry2, contentEntry3));

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItems()).extracting("rawValue").containsExactly("REF1");
    }

    @Test
    public void moveEntryUp_whenUnattachedEntry_shouldDoNothing() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        final ContentEntryDto contentEntry4 = createContentEntry();
        dataObject.addEntry(contentEntry1);
        dataObject.addEntry(contentEntry2);
        dataObject.addEntry(contentEntry3);

        // WHEN
        dataObject.moveEntryUp(contentEntry4);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry2);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry3);
    }

    @Test
    public void moveEntryUp_whenEntryCanBeMoved() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        dataObject.addEntry(contentEntry1);
        dataObject.addEntry(contentEntry2);
        dataObject.addEntry(contentEntry3);

        // WHEN
        dataObject.moveEntryUp(contentEntry2);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry2);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry3);
        assertThat(actualEntries).extracting("id").containsExactly(0, 1, 2);
    }

    @Test
    public void moveEntryDown_whenEntryCannotBeMoved_shouldDoNothing() {
        // GIVEN
        final ContentEntryDto contentEntry = createContentEntry();
        dataObject.addEntry(contentEntry);

        // WHEN
        dataObject.moveEntryDown(contentEntry);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry);
        assertThat(actualEntries).extracting("id").containsExactly(0);
    }

    @Test
    public void moveEntryDown_whenEntryCanBeMoved() {
        // GIVEN
        final ContentEntryDto contentEntry1 = createContentEntry();
        final ContentEntryDto contentEntry2 = createContentEntry();
        final ContentEntryDto contentEntry3 = createContentEntry();
        dataObject.addEntry(contentEntry1);
        dataObject.addEntry(contentEntry2);
        dataObject.addEntry(contentEntry3);

        // WHEN
        dataObject.moveEntryDown(contentEntry2);

        // THEN
        final List<ContentEntryDto> actualEntries = dataObject.getEntries();
        assertThat(actualEntries).hasSize(3);
        assertThat(actualEntries.get(0)).isSameAs(contentEntry1);
        assertThat(actualEntries.get(1)).isSameAs(contentEntry3);
        assertThat(actualEntries.get(2)).isSameAs(contentEntry2);
        assertThat(actualEntries).extracting("id").containsExactly(0, 1, 2);
    }

    private DbDataDto createDataWithRefSupport() {
        return DbDataDto.builder()
                    .forTopic(CAR_PHYSICS_DATA)
                    .supportingReferenceIndex(true)
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
}