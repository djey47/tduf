package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class MetaDataHelperTest {
    @Test
    void newHelper_shouldLoadDatabaseMetadata() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        new MetaDataHelper() {};

        // THEN
        DbMetadataDto actualObject = MetaDataHelper.databaseMetadataObject;
        assertThat(actualObject).isNotNull();
        assertThat(actualObject.getCameras()).isNotEmpty();
        assertThat(actualObject.getIKs()).isNotEmpty();
        assertThat(actualObject.getDealers()).isNotEmpty();
        assertThat(actualObject.getTopics()).isNotEmpty();
    }
}
