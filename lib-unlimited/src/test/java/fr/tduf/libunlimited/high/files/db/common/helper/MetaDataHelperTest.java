package fr.tduf.libunlimited.high.files.db.common.helper;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class MetaDataHelperTest {
    @Test
    public void newHelper_shouldLoadDatabaseMetadata() throws IOException, URISyntaxException {
        // GIVEN
        new MetaDataHelper() {};

        // WHEN-THEN
        assertThat(MetaDataHelper.databaseMetadataObject).isNotNull();
    }
}
