package fr.tduf.libunlimited.low.files.db.dto;

import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.SwitchValueDto;
import org.junit.Test;

import java.util.List;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static org.assertj.core.api.Assertions.assertThat;

public class DbDataDtoTest {

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
}
