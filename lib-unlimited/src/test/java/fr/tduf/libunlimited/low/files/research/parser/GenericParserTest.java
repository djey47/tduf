package fr.tduf.libunlimited.low.files.research.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GenericParserTest {
    private static final Class<GenericParserTest> thisClass = GenericParserTest.class;

    private static final String DATA = "data";

    @Test
    public void newParser_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();

        // WHEN
        GenericParser<String> actualParser = createGenericParser(inputStream);

        // THEN
        assertThat(actualParser.getInputStream()).isEqualTo(inputStream);
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    public void newParser_whenProvidedContents_andStructureAsFilePath_shouldReturnParserInstance() throws Exception {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();

        // WHEN
        GenericParser<String> actualParser = createGenericParserWithExternalStructure(inputStream);

        // THEN
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    public void parse_whenProvidedFiles_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();
        GenericParser<String> actualParser = createGenericParser(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    public void parse_whenProvidedFiles_andHalfFloatValues_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileHalfFloat();
        GenericParser<String> actualParser = createGenericParserHalfFloat(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    public void parse_whenProvidedFilesInLittleEndian_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileLittleEndian();
        GenericParser<String> actualParser = createGenericParserLittleEndian(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    public void parse_whenProvidedFiles_andSizeGivenByAnotherField_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileForFormulas();
        GenericParser<String> actualParser = createGenericParserForFormulas(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    public void dump_whenProvidedContents_andSizeGivenByAnotherField_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        String expectedDump = "sizeIndicator\t<INTEGER: 4 bytes>\t[0, 0, 0, 3]\t3\n" +
                "repeater\t<REPEATER>\t>>\n" +
                "repeater[0].number\t<INTEGER: 4 bytes>\t[0, 0, 0, 1]\t1\n" +
                "repeater[1].number\t<INTEGER: 4 bytes>\t[0, 0, 0, 2]\t2\n" +
                "repeater[2].number\t<INTEGER: 4 bytes>\t[0, 0, 0, 3]\t3\n" +
                "<< repeater\t<REPEATER: 3 items>\n" +
                "aValue\t<TEXT: 10 bytes>\t[65, 66, 67, 68, 69, 70, 71, 72, 73, 74]\t\"ABCDEFGHIJ\"\n";
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileForFormulas();
        GenericParser<String> actualParser = createGenericParserForFormulas(inputStream);
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        System.out.println("Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(expectedDump);
    }

    @Test
    public void dump_whenProvidedContents_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();
        GenericParser<String> actualParser = createGenericParser(inputStream);
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        System.out.println("Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDump());
    }

    @Test
    public void dump_whenProvidedContents_andHalfFloatValues_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileHalfFloat();
        GenericParser<String> actualParser = createGenericParserHalfFloat(inputStream);
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        System.out.println("Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpHalfFloat());
    }

    @Test
    public void dump_whenProvidedContentsInLittleEndian_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFileLittleEndian();
        GenericParser<String> actualParser = createGenericParserLittleEndian(inputStream);
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        System.out.println("Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDump());
    }

    private String getExpectedDump() {
        return "tag\t<TEXT: 10 bytes>\t[65, 66, 67, 68, 69, 70, 71, 72, 73, 74]\t\"ABCDEFGHIJ\"\n" +
                    "unknown\t<UNKNOWN: 5 bytes>\t[1, 2, 3, 4, 5]\t\n" +
                    "repeater\t<REPEATER>\t>>\n" +
                    "repeater[0].number\t<INTEGER: 4 bytes>\t[0, 0, 1, -12]\t500\n" +
                    "repeater[0].numberF\t<FPOINT: 4 bytes>\t[67, -128, -71, -48]\t257.45166\n" +
                    "repeater[0].gap\t<GAP: 2 bytes>\t[0, 0]\t\n" +
                    "repeater[0].text\t<TEXT: 4 bytes>\t[65, 66, 67, 68]\t\"ABCD\"\n" +
                    "repeater[0].delimiter\t<DELIMITER: 1 bytes>\t[10]\t\"\n\"\n" +
                    "repeater[1].number\t<INTEGER: 4 bytes>\t[0, 0, 3, -24]\t1000\n" +
                    "repeater[1].numberF\t<FPOINT: 4 bytes>\t[66, -83, 109, -34]\t86.714584\n" +
                    "repeater[1].gap\t<GAP: 2 bytes>\t[0, 0]\t\n" +
                    "repeater[1].text\t<TEXT: 4 bytes>\t[69, 70, 71, 72]\t\"EFGH\"\n" +
                    "repeater[1].delimiter\t<DELIMITER: 1 bytes>\t[11]\t\"\u000B\"\n" +
                    "<< repeater\t<REPEATER: 2 items>\n";
    }

    private String getExpectedDumpHalfFloat() {
        return "hf1\t<FPOINT: 2 bytes>\t[67, -112]\t3.78125\n" +
                    "hf2\t<FPOINT: 2 bytes>\t[68, -111]\t4.5664062\n" +
                    "hf3\t<FPOINT: 2 bytes>\t[69, -110]\t5.5703125\n";
    }

    private GenericParser<String> createGenericParser(final ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(10);

                // Field 1
                assertThat(getDataStore().getText("tag").get()).isEqualTo("ABCDEFGHIJ");

                // Field 2
                assertThat(getDataStore().getRawValue("unknown").get()).isEqualTo(new byte[]{0x1,0x2,0x3,0x4,0x5});

                // Field 3 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").get()).isEqualTo(500L);
                assertThat(getDataStore().getFloatingPoint("repeater[0].numberF").get()).isEqualTo(257.45166f);
                assertThat(getDataStore().getText("repeater[0].text").get()).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter").get()).isEqualTo(new byte[]{0xA});
                // Field 3 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").get()).isEqualTo(1000L);
                assertThat(getDataStore().getFloatingPoint("repeater[1].numberF").get()).isEqualTo(86.714584f);
                assertThat(getDataStore().getText("repeater[1].text").get()).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter").get()).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserHalfFloat(final ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getFloatingPoint("hf1").get()).isEqualTo(3.78125f);

                // Field 2
                assertThat(getDataStore().getFloatingPoint("hf2").get()).isEqualTo(4.5664062f);

                // Field 3
                assertThat(getDataStore().getFloatingPoint("hf3").get()).isEqualTo(5.5703125f);

                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-halfFloat-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserLittleEndian(final ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(10);

                // Field 1
                assertThat(getDataStore().getText("tag").get()).isEqualTo("ABCDEFGHIJ");

                // Field 2
                assertThat(getDataStore().getRawValue("unknown").get()).isEqualTo(new byte[]{0x1,0x2,0x3,0x4,0x5});

                // Field 3 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").get()).isEqualTo(500L);
                assertThat(getDataStore().getFloatingPoint("repeater[0].numberF").get()).isEqualTo(257.45166f);
                assertThat(getDataStore().getText("repeater[0].text").get()).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter").get()).isEqualTo(new byte[]{0xA});
                // Field 3 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").get()).isEqualTo(1000L);
                assertThat(getDataStore().getFloatingPoint("repeater[1].numberF").get()).isEqualTo(86.714584f);
                assertThat(getDataStore().getText("repeater[1].text").get()).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter").get()).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-littleEndian-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithExternalStructure(ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "./src/test/resources/files/structures/TEST-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserForFormulas(ByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(5);

                // Field 1
                assertThat(getDataStore().getInteger("sizeIndicator").get()).isEqualTo(3L);

                // Field 2 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").get()).isEqualTo(1L);
                // Field 2 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").get()).isEqualTo(2L);
                // Field 2 - item 2
                assertThat(getDataStore().getInteger("repeater[2].number").get()).isEqualTo(3L);

                // Field 3
                assertThat(getDataStore().getText("aValue").get()).isEqualTo("ABCDEFGHIJ");

                return DATA;
            }

            @Override
            protected String getStructureResource() {
                return "/files/structures/TEST-formulas-map.json";
            }
        };
    }

    private ByteArrayInputStream createInputStreamFromReferenceFile() throws IOException, URISyntaxException {
        URI fileURI = thisClass.getResource("/files/samples/TEST.bin").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(fileURI));
        return new ByteArrayInputStream(bytes);
    }

    private ByteArrayInputStream createInputStreamFromReferenceFileHalfFloat() throws IOException, URISyntaxException {
        URI fileURI = thisClass.getResource("/files/samples/TEST-halfFloat.bin").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(fileURI));
        return new ByteArrayInputStream(bytes);
    }

    private ByteArrayInputStream createInputStreamFromReferenceFileLittleEndian() throws IOException, URISyntaxException {
        URI fileURI = thisClass.getResource("/files/samples/TEST-littleEndian.bin").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(fileURI));
        return new ByteArrayInputStream(bytes);
    }

    private ByteArrayInputStream createInputStreamFromReferenceFileForFormulas() throws URISyntaxException, IOException {
        URI fileURI = thisClass.getResource("/files/samples/TEST-formulas.bin").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(fileURI));
        return new ByteArrayInputStream(bytes);
    }
}