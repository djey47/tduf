package fr.tduf.libunlimited.low.files.db.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DbDtoTest {

    @Test
    public void fromTopicLabel_whenTopicExists_shouldReturnCorrectTopic() {
        // WHEN
        DbDto.Topic actualTopic = DbDto.Topic.fromLabel("TDU_AfterMarketPacks");

        //THEN
        assertThat(actualTopic).isEqualTo(DbDto.Topic.AFTER_MARKET_PACKS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromTopicLabel_whenTopicDoesNotExist_shouldThrowException() {
        // WHEN-THEN
        DbDto.Topic.fromLabel("TDU_Babes");
    }
}