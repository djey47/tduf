package fr.tduf.libunlimited.low.files.banks.mapping.writer;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapWriterTest {

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
    public void write() {
        //TODO with mock
    }
}