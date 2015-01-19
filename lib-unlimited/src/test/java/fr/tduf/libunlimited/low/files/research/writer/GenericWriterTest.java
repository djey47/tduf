package fr.tduf.libunlimited.low.files.research.writer;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

public class GenericWriterTest {
    private static final Class<GenericWriterTest> thisClass = GenericWriterTest.class;

    private static final String DATA = "data";

    @Test
    public void newWriter_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        // GIVEN-WHEN
        GenericWriter<String> actualWriter = createGenericWriter();

        // THEN
        assertThat(actualWriter.getData()).isEqualTo(DATA);
        assertThat(actualWriter.getFileStructure()).isNotNull();
    }

    @Test
    public void newWriter_whenProvidedContents_andStructureAsFilePath_shouldReturnWriterInstance() throws Exception {
        // GIVEN-WHEN
        GenericWriter<String> actualWriter = createGenericWriterWithExternalStructure();

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
        assertThat(actualBytes).hasSize(45);

        URI referenceFileURI = thisClass.getResource("/files/samples/TEST.bin").toURI();
        byte[] expectedBytes = Files.readAllBytes(Paths.get(referenceFileURI));
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    public void write_whenProvidedFilesInLittleEndian_shouldReturnBytes() throws IOException, URISyntaxException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterLittleEndian();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(45);

        URI referenceFileURI = thisClass.getResource("/files/samples/TEST-littleEndian.bin").toURI();
        byte[] expectedBytes = Files.readAllBytes(Paths.get(referenceFileURI));
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    public void write_whenProvidedFiles_andSizeGivenByFormula_shouldReturnBytes() throws IOException, URISyntaxException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterForFormulas();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(26);

        URI referenceFileURI = thisClass.getResource("/files/samples/TEST-formulas.bin").toURI();
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
                getDataStore().addValue("unknown", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedIntegerValue("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPointValue("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedTextValue("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedIntegerValue("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPointValue("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedTextValue("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterWithExternalStructure() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {}

            @Override
            protected String getStructureResource() {
                return "./src/test/resources/files/structures/TEST-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterLittleEndian() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addText("tag", "ABCDEFGHIJ");

                // Field 2
                getDataStore().addValue("unknown", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedIntegerValue("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPointValue("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedTextValue("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedIntegerValue("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPointValue("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedTextValue("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedRawValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-littleEndian-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterForFormulas() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addInteger("sizeIndicator", 3);

                // Field 2 - sub items, rank 0
                getDataStore().addRepeatedIntegerValue("repeater", "number", 0, 1L);
                // Field 2 - sub items, rank 1
                getDataStore().addRepeatedIntegerValue("repeater", "number", 1, 2L);
                // Field 2 - sub items, rank 2
                getDataStore().addRepeatedIntegerValue("repeater", "number", 2, 3L);

                // Field 3
                getDataStore().addText("aValue", "ABCDEFGHIJ");
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-formulas-map.json";
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