package fr.tduf.libunlimited.low.files.research.writer;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericWriterTest {
    private static final Class<GenericWriterTest> thisClass = GenericWriterTest.class;

    private static final String DATA = "data";

    @Test
    public void newWriter_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        // GIVEN

        // WHEN
        GenericWriter<String> actualWriter = createGenericWriter();

        // THEN
        assertThat(actualWriter.getData()).isEqualTo(DATA);
        assertThat(actualWriter.getFileStructure()).isNotNull();
    }

    @Test
    public void write_whenProvidedFiles_shouldReturnBytes() throws IOException, URISyntaxException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriter();

        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();
        // Uncomment below to regen output file
//        Files.write(Paths.get("src/test/resources/files/samples/TEST.bin"), actualOutputStream.toByteArray());

        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(37);

        URI referenceFileURI = thisClass.getResource("/files/samples/TEST.bin").toURI();
        byte[] expectedBytes = Files.readAllBytes(Paths.get(referenceFileURI));
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test(expected = NoSuchElementException.class)
    public void write_whenProvidedFiles_andMissingValue_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterWithMissingValues();

        // WHEN
        actualWriter.write();

        // THEN: exception
    }

    private GenericWriter<String> createGenericWriter() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addText("tag", "ABCDEFGHIJ");

                // Field 2
                getDataStore().addRawValue("unknown", new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedNumericValue("repeater", "number", 0, 500L);
                getDataStore().addRepeatedTextValue("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedNumericValue("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedTextValue("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 1, new byte[]{0xB});
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterWithMissingValues() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {}

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }
}