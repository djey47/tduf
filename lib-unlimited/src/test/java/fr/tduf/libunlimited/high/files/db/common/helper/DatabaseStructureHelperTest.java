package fr.tduf.libunlimited.high.files.db.common.helper;


import org.junit.jupiter.api.Test;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseStructureHelperTest {
    private final DatabaseStructureHelper helper = new DatabaseStructureHelper();

    @Test
    void isRefSupportForTopic_whenTopicDoesNotSupport_shouldReturnFalse() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(helper.isRefSupportForTopic(CAR_RIMS)).isFalse();
    }

    @Test
    void isRefSupportForTopic_whenTopicDoesSupport_shouldReturnTrue() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(helper.isRefSupportForTopic(BRANDS)).isTrue();
    }
}
