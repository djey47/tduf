package fr.tduf.libunlimited.low.files.bin.cameras.rw;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class CamerasWriterTest {

    @Test
    public void load_shouldReturnWriterInstance() throws IOException {
        // GIVEN
        DataStore dataStore = new DataStore(FileStructureDto.builder().build());

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(dataStore);

        // THEN
        assertThat(actualWriter).isNotNull();
        assertThat(actualWriter.getSourceStore()).isSameAs(dataStore);
    }

    @Test
    public void write_shouldReturnOriginalContentsBack() throws IOException, URISyntaxException {
        // GIVEN
        byte[] camerasContentsFromFile = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
        CamerasParser camerasParser = CamerasParser.load(new ByteArrayInputStream(camerasContentsFromFile));
        camerasParser.parse();

        // WHEN
        CamerasWriter actualWriter = CamerasWriter.load(camerasParser.getDataStore());
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(camerasContentsFromFile);
    }
}