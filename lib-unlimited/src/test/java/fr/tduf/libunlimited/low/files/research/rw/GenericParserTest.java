package fr.tduf.libunlimited.low.files.research.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenericParserTest {

    private static Class<GenericParserTest> thisClass = GenericParserTest.class;

    private static final String DATA = "data";
    private static final String FIELD_NAME = "fieldName";

    private enum TestingProps implements DataStoreProps {
        PROP;

        @Override
        public Optional<?> retrieveFrom(DataStore dataStore) {
            return null;
        }

        @Override
        public String getStoreFieldName() {
            return FIELD_NAME;
        }
    }

    @BeforeAll
    static void setUp() {
        Log.set(Log.LEVEL_INFO);

        MockitoAnnotations.initMocks(thisClass);
    }

    @Test
    void newParser_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        // GIVEN-WHEN
        GenericParser<String> actualParser = createGenericParser();

        // THEN
        assertThat(actualParser.getInputStream()).isNotNull();
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    void newParser_whenProvidedContents_andStructureAsFilePath_shouldReturnParserInstance() throws Exception {
        // GIVEN
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();

        // WHEN
        GenericParser<String> actualParser = createGenericParserWithExternalStructure(inputStream);

        // THEN
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    void parse_whenProvidedFiles_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParser();

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    void parse_whenProvidedFiles_andEncryptedContents_shouldReturnDomainObject() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-encrypted.bin"));
        GenericParser<String> actualParser = createGenericParserEncrypted(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    void dump_whenProvidedContents_andSizeGivenByAnotherField_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserForFormulas();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpSizeByField());
    }

    @Test
    void dump_whenProvidedContents_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParser();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDump());
    }

    @Test
    void dump_whenProvidedContents_andHalfFloatValues_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserHalfFloat();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpHalfFloat());
    }

    @Test
    void dump_whenProvidedContents_andVeryShortValues_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserVeryShortInt();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpVeryShortInt());
    }

    @Test
    void dump_whenProvidedContentsInLittleEndian_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserLittleEndian();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDump());
    }

    @Test
    void dump_whenProvidedContentsSigned_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserSigned();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpSignedInteger());
    }

    @Test
    void dump_whenProvidedContentsAsConstants_shouldReturnAllParsedData() throws IOException, URISyntaxException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithConstants();
        actualParser.parse();

        // WHEN
        String actualDump = actualParser.dump();
        Log.debug(thisClass.getSimpleName(), "Dumped contents:\n" + actualDump);

        // THEN
        assertThat(actualDump).isEqualTo(getExpectedDumpForConstants());
    }

    @Test
    void getNumeric_whenValueExistInStore_shouldReturnIt() {
        // GIVEN
        DataStore viewStoreMock = mock(DataStore.class);
        when(viewStoreMock.getInteger(FIELD_NAME)).thenReturn(of(100L));

        // WHEN
        Optional<Long> actualValue = GenericParser.getNumeric(viewStoreMock, TestingProps.PROP);

        // THEN
        assertThat(actualValue).contains(100L);
    }

    @Test
    void getNumeric_whenValueDoesNotExistInStore_shouldReturnEmpty() {
        // GIVEN
        DataStore viewStoreMock = mock(DataStore.class);
        when(viewStoreMock.getInteger(FIELD_NAME)).thenReturn(empty());

        // WHEN
        Optional<Long> actualValue = GenericParser.getNumeric(viewStoreMock, TestingProps.PROP);

        // THEN
        assertThat(actualValue).isEmpty();
    }

    private String getExpectedDump() throws IOException, URISyntaxException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-basicFields.txt");
    }

    private String getExpectedDumpSizeByField() throws URISyntaxException, IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-sizeFromField.txt");
    }

    private String getExpectedDumpHalfFloat() throws IOException, URISyntaxException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-halfFloat.txt");
    }

    private String getExpectedDumpVeryShortInt() throws IOException, URISyntaxException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-veryShortInt.txt");
    }

    private String getExpectedDumpSignedInteger() throws IOException, URISyntaxException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-signedInteger.txt");
    }

    private String getExpectedDumpForConstants() throws IOException, URISyntaxException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-constants.txt");
    }

    private GenericParser<String> createGenericParser() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = createInputStreamFromReferenceFile();

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
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserEncrypted(final ByteArrayInputStream inputStream) throws IOException {
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
            public String getStructureResource() {
                return "/files/structures/TEST-encrypted-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserHalfFloat() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-halfFloat.bin"));

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
            public String getStructureResource() {
                return "/files/structures/TEST-halfFloat-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserVeryShortInt() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-veryShortInt.bin"));

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getInteger("vsi1").get()).isEqualTo(67);

                // Field 2
                assertThat(getDataStore().getInteger("vsi2").get()).isEqualTo(68);

                // Field 3
                assertThat(getDataStore().getInteger("vsi3").get()).isEqualTo(69);

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-veryShortInt-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserLittleEndian() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-littleEndian.bin"));

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
            public String getStructureResource() {
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
            public String getStructureResource() {
                return "./src/test/resources/files/structures/TEST-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserForFormulas() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-formulas.bin"));

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
            public String getStructureResource() {
                return "/files/structures/TEST-formulas-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserSigned() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-signedInteger.bin"));

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getInteger("si1").get()).isEqualTo(-60);

                // Field 2
                assertThat(getDataStore().getInteger("si2").get()).isEqualTo(0);

                // Field 3
                assertThat(getDataStore().getInteger("si3").get()).isEqualTo(60);

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-signedInteger-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConstants() throws IOException, URISyntaxException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST-constants.bin"));

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-constants-map.json";
            }
        };
    }

    private ByteArrayInputStream createInputStreamFromReferenceFile() throws IOException, URISyntaxException {
        return new ByteArrayInputStream(FilesHelper.readBytesFromResourceFile("/files/samples/TEST.bin"));
    }
}