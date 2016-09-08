package fr.tduf.libunlimited.low.files.db.dto.content;

import com.sun.javafx.UnmodifiableArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
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
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF").build())
                .build();
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
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF").build())
                .build();
        dataObjectWithRefSupport.addEntry(contentEntry);

        // WHEN
        final Optional<ContentEntryDto> actualEntry = dataObjectWithRefSupport.getEntryWithReference("REF");

        // THEN
        assertThat(actualEntry).contains(contentEntry);
    }

    @Test
    public void addEntry_shouldUpdateContext() {
        // GIVEN
        DbDataDto dataObjectWithRefSupport = createDataWithRefSupport();
        ContentEntryDto contentEntry = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue("REF").build())
                .build();

        // WHEN
        dataObjectWithRefSupport.addEntry(contentEntry);

        // THEN
        assertThat(contentEntry.getDataHost()).isSameAs(dataObjectWithRefSupport);
        assertThat(dataObjectWithRefSupport.getEntries()).containsExactly(contentEntry);
        assertThat(dataObjectWithRefSupport.getEntriesByReference())
                .containsOnlyKeys("REF")
                .containsValues(contentEntry);
    }

    private DbDataDto createDataWithRefSupport() {
        return DbDataDto.builder()
                    .forTopic(CAR_PHYSICS_DATA)
                    .supportingReferenceIndex(true)
                    .build();
    }
}
