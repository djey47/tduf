package fr.tduf.libunlimited.low.files.research.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.low.files.research.domain.Type.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericWriterTest {

    private static final String DATA = "data";

    @Test
    void newWriter_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        // GIVEN-WHEN
        GenericWriter<String> actualWriter = createGenericWriter();

        // THEN
        assertThat(actualWriter.getData()).isEqualTo(DATA);
        assertThat(actualWriter.getFileStructure()).isNotNull();
    }

    @Test
    void newWriter_whenProvidedContents_andStructureAsFilePath_shouldReturnWriterInstance() throws Exception {
        // GIVEN-WHEN
        GenericWriter<String> actualWriter = createGenericWriterWithExternalStructure();

        // THEN
        assertThat(actualWriter.getData()).isEqualTo(DATA);
        assertThat(actualWriter.getFileStructure()).isNotNull();
    }

    @Test
    void write_whenProvidedFiles_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriter();

        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();
        // Uncomment below to regen output file
//        Files.write(Paths.get("src/test/resources/files/samples/TEST.bin"), actualOutputStream.toByteArray());

        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(47);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andModifiedTextLength_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterModifiedTextLength();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(47);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-modifiedTextLength.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andEncryptedContents_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterEncrypted();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(48);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-encrypted.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andHalfFloatContents_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterHalfFloat();

        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(6);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-halfFloat.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andVeryShortIntContents_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterVeryShortInt();

        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(3);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-veryShortInt.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andUnsignedLongValue_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterUnsignedLong();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(8);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-unsignedLong.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }    
    
    @Test
    void write_whenProvidedFiles_andConstantValues_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterConstants();

        
        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();
        // Uncomment below to regen output file
//        Files.write(Paths.get("src/test/resources/files/samples/TEST-constants.bin"), actualOutputStream.toByteArray());

        
        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(16);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-constants.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andLastFieldAutoSize_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterLastFieldAutoSize();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(51);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-auto.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andFormatAsLittleEndian_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterLittleEndian();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(47);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-littleEndian.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andSizeGivenByFormula_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterForFormulas();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(26);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-formulas.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andConditionedField_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterForCondition();


        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();


        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(6);

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-conditional-without.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
    }

    @Test
    void write_whenProvidedFiles_andMissingValue_shouldThrowException() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterWithMissingValues();

        // WHEN-THEN
        assertThrows(NoSuchElementException.class,
                actualWriter::write);
    }

    @Test
    void write_whenProvidedFiles_withLevel2Repeater_shouldReturnBytes() throws IOException {
        // GIVEN
        GenericWriter<String> actualWriter = createGenericWriterWithLevel2Repeater();

        // WHEN
        ByteArrayOutputStream actualOutputStream = actualWriter.write();
        // Uncomment below to regen output file
//        Files.write(Paths.get("src/test/resources/files/samples/TEST-repeater-lvl2.bin"), actualOutputStream.toByteArray());

        // THEN
        assertThat(actualOutputStream).isNotNull();

        byte[] actualBytes = actualOutputStream.toByteArray();
        assertThat(actualBytes).hasSize(2*2*4); // 2*2*4 bytes

        byte[] expectedBytes = FilesHelper.readBytesFromResourceFile("/files/samples/TEST-repeater-lvl2.bin");
        assertThat(actualBytes).isEqualTo(expectedBytes);
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
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedText("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedText("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterWithLevel2Repeater() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1 - sub items (lvl2), ranks 0-0
                getDataStore().addRepeatedInteger32("repeaterLvl1[0].repeaterLvl2", "number", 0, 500L);
                // Field 2 - sub items (lvl2), ranks 0-1
                getDataStore().addRepeatedInteger32("repeaterLvl1[0].repeaterLvl2", "number", 1, 501L);
                // Field 3 - sub items (lvl2), ranks 1-0
                getDataStore().addRepeatedInteger32("repeaterLvl1[1].repeaterLvl2", "number", 0, 502L);
                // Field 4 - sub items (lvl2), ranks 1-1
                getDataStore().addRepeatedInteger32("repeaterLvl1[1].repeaterLvl2", "number", 1, 503L);
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-repeater-lvl2-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterModifiedTextLength() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1: text length 10 to 16 (will be truncated to fit)
                getDataStore().addText("tag", "ABCDEFGHIJKLMNOP");

                // Field 2
                getDataStore().addValue("unknown", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 0, 257.45166f);
                // text length 4 to 2 (will be filled to fit)
                getDataStore().addRepeatedText("repeater", "text", 0, "AB");
                getDataStore().addRepeatedValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedText("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterEncrypted() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addText("tag", "ABCDEFGHIJ");

                // Field 2
                getDataStore().addValue("unknown", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedText("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedText("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-encrypted-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterHalfFloat() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addHalfFloatingPoint("hf1", 3.78125f);

                // Field 2
                getDataStore().addHalfFloatingPoint("hf2", 4.5664062f);

                // Field 3
                getDataStore().addHalfFloatingPoint("hf3", 5.5703125f);
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-halfFloat-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterVeryShortInt() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addInteger32("vsi1", 67);

                // Field 2
                getDataStore().addInteger32("vsi2", 68);

                // Field 3
                getDataStore().addInteger32("vsi3", 69);
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-veryShortInt-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterUnsignedLong() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                getDataStore().addInteger32("my_int_field", 1000);
                getDataStore().addInteger32("my_long_field", 4125000000L);
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-unsignedLong-map.json";
            }
        };
    }    
    
    private GenericWriter<String> createGenericWriterConstants() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {}

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-constants-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterLastFieldAutoSize() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addText("tag", "ABCDEFGHIJ");

                // Field 2
                getDataStore().addValue("unknown", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5});

                // Field 3 - sub items, rank 0
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedText("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedText("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedValue("repeater", "delimiter", 1, new byte[] {0xB});

                // Field 4 - size not specified in structure
                getDataStore().addValue("unknown2", UNKNOWN, new byte[]{0x1, 0x2, 0x3, 0x4, 0x5, 0x6});
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-auto-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterWithExternalStructure() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {}

            @Override
            public String getStructureResource() {
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
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 500L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 0, 257.45166f);
                getDataStore().addRepeatedText("repeater", "text", 0, "ABCD");
                getDataStore().addRepeatedValue("repeater", "delimiter", 0, new byte[] {0xA});

                // Field 3 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 1000L);
                getDataStore().addRepeatedFloatingPoint("repeater", "numberF", 1, 86.714584f);
                getDataStore().addRepeatedText("repeater", "text", 1, "EFGH");
                getDataStore().addRepeatedValue("repeater", "delimiter", 1, new byte[] {0xB});
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-littleEndian-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterForFormulas() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addInteger32("sizeIndicator", 3);

                // Field 2 - sub items, rank 0
                getDataStore().addRepeatedInteger32("repeater", "number", 0, 1L);
                // Field 2 - sub items, rank 1
                getDataStore().addRepeatedInteger32("repeater", "number", 1, 2L);
                // Field 2 - sub items, rank 2
                getDataStore().addRepeatedInteger32("repeater", "number", 2, 3L);

                // Field 3
                getDataStore().addText("aValue", "ABCDEFGHIJ");
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-formulas-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterForCondition() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {
                // Field 1
                getDataStore().addText("tag", "ABCDE");

                // Field 2
                getDataStore().addInteger("flag", 0, 1);

                // Field 3 - is omitted as unsatisfied condition when parsing
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-conditional-map.json";
            }
        };
    }

    private GenericWriter<String> createGenericWriterWithMissingValues() throws IOException {
        return new GenericWriter<String>(DATA) {
            @Override
            protected void fillStore() {}

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }
}
