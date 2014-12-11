package fr.tduf.libunlimited.low.files.banks.mapping.writer;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.parser.MapParser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class MapWriterTest {

    private static Class<MapWriterTest> thisClass = MapWriterTest.class;

    @Test
    public void load_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        //GIVEN
        BankMap bankMap = new BankMap();

        //WHEN
        MapWriter mapWriter = MapWriter.load(bankMap);

        //THEN
        assertThat(mapWriter).isNotNull();
    }

    @Test
    public void write_whenRealFilesLoaded_shouldReturnSameAsInitialContents() throws URISyntaxException, IOException {
        //GIVEN
        URI uri = thisClass.getResource("/banks/Bnk1.map").toURI();
        byte[] mapContents = Files.readAllBytes(Paths.get(uri));
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(mapContents);
        BankMap bankMap = MapParser.load(mapInputStream).parse();

        //WHEN
        MapWriter actualMapWriter = MapWriter.load(bankMap);
        ByteArrayOutputStream actualBytes = actualMapWriter.write();

        //THEN
        assertThat(actualBytes.toByteArray()).isEqualTo(mapContents);
    }
}