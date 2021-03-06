package fr.tduf.libunlimited.low.files.banks.mapping.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MapWriterTest {

    @Test
    void load_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        //GIVEN
        BankMap bankMap = new BankMap();

        //WHEN
        MapWriter mapWriter = MapWriter.load(bankMap);

        //THEN
        assertThat(mapWriter).isNotNull();
    }

    @Test
    void write_whenRealFilesLoaded_shouldReturnSameAsInitialContents() throws IOException {
        //GIVEN
        byte[] mapData = FilesHelper.readBytesFromResourceFile("/banks/Bnk1.map");
        BankMap bankMap = MapParser.load(mapData).parse();

        //WHEN
        MapWriter actualMapWriter = MapWriter.load(bankMap);
        ByteArrayOutputStream actualBytes = actualMapWriter.write();

        //THEN
        assertThat(actualBytes.toByteArray()).isEqualTo(mapData);
    }
}
