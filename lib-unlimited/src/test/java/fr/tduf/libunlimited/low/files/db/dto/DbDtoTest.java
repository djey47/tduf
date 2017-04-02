package fr.tduf.libunlimited.low.files.db.dto;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbDtoTest {

    @Test
    void fromTopicLabel_whenTopicExists_shouldReturnCorrectTopic() {
        // WHEN
        DbDto.Topic actualTopic = DbDto.Topic.fromLabel("TDU_AfterMarketPacks");

        //THEN
        assertThat(actualTopic).isEqualTo(DbDto.Topic.AFTER_MARKET_PACKS);
    }

    @Test
    void fromTopicLabel_whenTopicDoesNotExist_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> DbDto.Topic.fromLabel("TDU_Babes"));
    }
}