package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbDtoTest {

    @Test
    public void getTopicLabel_shouldReturnCorrectLabel() {
        // WHEN
        String actualLabel = DbDto.Topic.getLabel(DbDto.Topic.AFTER_MARKET_PACKS);

        //THEN
        assertThat(actualLabel).isEqualTo("TDU_AfterMarketPacks");
    }
}