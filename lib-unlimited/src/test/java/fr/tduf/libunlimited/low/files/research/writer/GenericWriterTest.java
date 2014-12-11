package fr.tduf.libunlimited.low.files.research.writer;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericWriterTest {

    @Test
    public void newWriter_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        // GIVEN
        String data = "data";

        // WHEN
        GenericWriter<String> actualWriter = new GenericWriter<String>(data) {
            @Override
            protected void fillStore() {}

            @Override
            protected String getStructureResource() {
                return "/files/structures/MAP4-map.json";
            }
        };

        // THEN
        assertThat(actualWriter.getData()).isEqualTo(data);
        assertThat(actualWriter.getFileStructure()).isNotNull();
    }

    @Test
    public void write_whenProvidedFiles_shouldReturnBytes() throws IOException {
        // TODO test with light sample
        // GIVEN

        // WHEN

        // THEN
    }
}