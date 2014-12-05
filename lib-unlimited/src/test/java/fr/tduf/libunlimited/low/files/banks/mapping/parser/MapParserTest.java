package fr.tduf.libunlimited.low.files.banks.mapping.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MapParserTest {

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        //GIVEN
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(new byte[]{ 0x0, 0x1, 0x2, 0x3 });

        //WHEN
        MapParser mapParser = MapParser.load(mapInputStream);

        //THEN
        assertThat(mapParser).isNotNull();
        assertThat(mapParser.getMapInputStream()).isSameAs(mapInputStream);
    }
}