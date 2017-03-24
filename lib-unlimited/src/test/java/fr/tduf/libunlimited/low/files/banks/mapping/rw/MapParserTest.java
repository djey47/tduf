package fr.tduf.libunlimited.low.files.banks.mapping.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class MapParserTest {

    private static final Class<MapParserTest> thisClass = MapParserTest.class;

    @BeforeEach
    void setUp() throws IOException {
        Log.set(Log.LEVEL_DEBUG);
    }

    @Test
    void load_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        //GIVEN
        byte[] mapData = {0x0, 0x1, 0x2, 0x3};

        //WHEN
        MapParser mapParser = MapParser.load(mapData);

        //THEN
        assertThat(mapParser).isNotNull();
    }

    @Test
    void parse_whenRealFiles_shouldFillStore_andReadAllEntries() throws IOException, URISyntaxException {
        // GIVEN
        byte[] mapContents = FilesHelper.readBytesFromResourceFile("/banks/Bnk1.map");


        // WHEN
        MapParser mapParser = MapParser.load(mapContents);
        BankMap actualBankMap = mapParser.parse();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + mapParser.dump());


        // THEN
        byte[] separatorBytes = {0x0, (byte) 0xFE, 0x12, 0x0};

        assertThat(actualBankMap).isNotNull();
        assertThat(actualBankMap.getEntrySeparator()).isEqualTo(separatorBytes);
        assertThat(actualBankMap.getEntries()).hasSize(4);
        assertThat(actualBankMap.getEntries()).extracting("hash").containsAll(asList(858241L, 1507153L, 1521845L, 1572722L));
        assertThat(actualBankMap.getEntries()).extracting("size1").containsAll(asList(0L, 0L, 0L, 0L));
        assertThat(actualBankMap.getEntries()).extracting("size2").containsAll(asList(0L, 0L, 0L, 0L));

        assertThat(mapParser.getDataStore().size()).isEqualTo(16); // = 4*(Hash+Size1+Size2+End)
    }
}
