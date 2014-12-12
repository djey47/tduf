package fr.tduf.libunlimited.low.files.banks.mapping.parser;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MapParserTest {

    private static Class<MapParserTest> thisClass = MapParserTest.class;

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        //GIVEN
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(new byte[]{ 0x0, 0x1, 0x2, 0x3 });

        //WHEN
        MapParser mapParser = MapParser.load(mapInputStream);

        //THEN
        assertThat(mapParser).isNotNull();
    }

    @Test
    public void parse_whenRealFiles_shouldFillStore_andReadAllEntries() throws IOException, URISyntaxException {
        // GIVEN
        URI uri = thisClass.getResource("/banks/Bnk1.map").toURI();
        byte[] mapContents = Files.readAllBytes(Paths.get(uri));
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(mapContents);


        // WHEN
        MapParser mapParser = MapParser.load(mapInputStream);
        BankMap actualBankMap = mapParser.parse();


        // THEN
        byte[] separatorBytes = {0x0, (byte) 0xFE, 0x12, 0x0};

        assertThat(actualBankMap).isNotNull();
        assertThat(actualBankMap.getEntrySeparator()).isEqualTo(separatorBytes);
        assertThat(actualBankMap.getEntries()).hasSize(4);
        assertThat(actualBankMap.getEntries()).extracting("hash").containsAll(asList(858241L, 1507153L, 1521845L, 1572722L));
        assertThat(actualBankMap.getEntries()).extracting("size1").containsAll(asList(0L, 0L, 0L, 0L));
        assertThat(actualBankMap.getEntries()).extracting("size2").containsAll(asList(0L, 0L, 0L, 0L));

        assertThat(mapParser.getDataStore().size()).isEqualTo(17); // = Tag + 4*(Hash+Size1+Size2+End)
        assertThat(mapParser.getDataStore().getText("tag")).isEqualTo("MAP4\0");
    }
}