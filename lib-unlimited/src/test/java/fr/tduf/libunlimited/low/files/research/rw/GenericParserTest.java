package fr.tduf.libunlimited.low.files.research.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenericParserTest {

    private static final Class<GenericParserTest> thisClass = GenericParserTest.class;

    private static final String DATA = "data";
    private static final String FIELD_NAME = "fieldName";

    private enum TestingProps implements DataStoreProps {
        PROP;

        @Override
        public Optional<?> retrieveFrom(DataStore dataStore) {
            return empty();
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
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST.bin");

        // WHEN
        GenericParser<String> actualParser = createGenericParserWithExternalStructure(inputStream);

        // THEN
        assertThat(actualParser.getFileStructure()).isNotNull();
    }

    @Test
    void parse_whenProvidedFiles_shouldReturnDomainObject() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParser();

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    void parse_whenProvidedFiles_andEncryptedContents_shouldReturnDomainObject() throws IOException {
        // GIVEN
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-encrypted.bin");
        GenericParser<String> actualParser = createGenericParserEncrypted(inputStream);

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    void parse_whenProvidedFiles_andComplexRepeaterLevel2_shouldReturnDomainObject() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithLevel2RepeaterComplex();

        // WHEN
        String actualObject = actualParser.parse();

        // THEN
        assertThat(actualObject).isNotNull();
        assertThat(actualObject).isEqualTo(DATA);
    }

    @Test
    void dump_whenProvidedContents_andSizeGivenByAnotherField_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContents_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContents_andHalfFloatValues_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContents_andVeryShortValues_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContentsInLittleEndian_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContentsSigned_shouldReturnAllParsedData() throws IOException {
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
    void dump_whenProvidedContentsAsConstants_shouldReturnAllParsedData() throws IOException {
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
    void parse_whenProvidedContentsAsConstants_andNonMatchingValues_shouldThrowException() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithConstantsUnmatching();

        // WHEN-THEN
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                actualParser::parse);
        assertThat(actualException).hasMessage("Constant check failed for field: tag1 - expected: 0x[41 42 43 44 44 46 47 48 49 4A], read: 0x[41 42 43 44 45 46 47 48 49 4A]");
    }

    @Test
    void parse_whenProvidedContentsAsConstants_andNonMatchingValues_butCheckDisabled_shouldNotThrowException() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithConstantsUnmatchingAndCheckDisabled();

        // WHEN-THEN
        actualParser.parse();
    }

    @Test
    void parse_whenProvidedContentsAsGap_andMatchingValue_shouldNotThrowException() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithGap();

        // WHEN-THEN
        actualParser.parse();
    }

    @Test
    void parse_whenProvidedContentsAsGap_andNonMatchingValue_shouldThrowException() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithGapUnmatching();

        // WHEN-THEN
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                actualParser::parse);
        assertThat(actualException).hasMessage("Constant check failed for field: gap - expected: 0x[00 00 00 00 00 00 00 00 00 00], read: 0x[00 00 00 00 00 42 00 00 00 00]");
    }

    @Test
    void parse_whenProvidedContentsAsGap_andNonMatchingValues_butCheckDisabled_shouldNotThrowException() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithGapUnmatchingAndCheckDisabled();

        // WHEN-THEN
        actualParser.parse();
    }

    @Test
    void parse_whenProvidedContentsWithConditionSatisfied_shouldReturnParsedData() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithConditionSatisfied();

        // WHEN-THEN
        actualParser.parse();
    }

    @Test
    void parse_whenProvidedContentsWithConditionUnsatisfied_shouldReturnParsedData() throws IOException {
        // GIVEN
        GenericParser<String> actualParser = createGenericParserWithConditionUnsatisfied();

        // WHEN-THEN
        actualParser.parse();
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

    @Test
    void readRawValue_whenNegativeLength_shouldThrowException() throws IOException {
        // given
        GenericParser<String> actualParser = createGenericParser();

        // when
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> actualParser.readRawValue(-1));

        // then
        assertThat(actualException).hasMessage("Invalid raw value size supplied: -1");
    }

    @Test
    void readRawValue_whenEndOfStreamReached_shouldThrowException() throws IOException {
        // given
        GenericParser<String> actualParser = createGenericParserWithSimulatedEOS();

        // when
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> actualParser.readRawValue(5));

        // then
        assertThat(actualException).hasMessage("Cannot read raw value of size 5 - end of file was reached");
    }

    @Test
    void readRawValue_whenEndOfStreamReached_andAutomaticLength_shouldReturnEmptyByteArray() throws IOException {
        // given
        GenericParser<String> actualParser = createGenericParserWithSimulatedEOS();

        // when-then
        GenericParser.ReadResult actualResult = actualParser.readRawValue(null);

        // then
        assertThat(actualResult.getReadValueAsBytes()).isEmpty();
    }

    private String getExpectedDump() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-basicFields.txt");
    }

    private String getExpectedDumpSizeByField() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-sizeFromField.txt");
    }

    private String getExpectedDumpHalfFloat() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-halfFloat.txt");
    }

    private String getExpectedDumpVeryShortInt() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-veryShortInt.txt");
    }

    private String getExpectedDumpSignedInteger() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-signedInteger.txt");
    }

    private String getExpectedDumpForConstants() throws IOException {
        return FilesHelper.readTextFromResourceFile("/files/dumps/TEST-constants.txt");
    }

    private GenericParser<String> createGenericParser() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(10);

                // Field 1
                assertThat(getDataStore().getText("tag").orElse("NO TEXT")).isEqualTo("ABCDEFGHIJ");

                // Field 2
                assertThat(getDataStore().getRawValue("unknown").orElse(new byte[]{})).isEqualTo(new byte[]{0x1,0x2,0x3,0x4,0x5});

                // Field 3 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").orElse(0L)).isEqualTo(500L);
                assertThat(getDataStore().getFloatingPoint("repeater[0].numberF").orElse(0f)).isEqualTo(257.45166f);
                assertThat(getDataStore().getText("repeater[0].text").orElse("NO TEXT")).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter").orElse(new byte[]{})).isEqualTo(new byte[]{0xA});
                // Field 3 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").orElse(0L)).isEqualTo(1000L);
                assertThat(getDataStore().getFloatingPoint("repeater[1].numberF").orElse(0f)).isEqualTo(86.714584f);
                assertThat(getDataStore().getText("repeater[1].text").orElse("NO TEXT")).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter").orElse(new byte[]{})).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithLevel2RepeaterComplex() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-repeater-lvl2-complex.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(4);
                assertThat(getDataStore().getInteger("repeaterLvl1[0].number1Lvl2")).contains(101L);
                assertThat(getDataStore().getInteger("repeaterLvl1[0].number2Lvl2")).contains(102L);
                assertThat(getDataStore().getInteger("repeaterLvl1[1].number1Lvl2")).contains(201L);
                assertThat(getDataStore().getInteger("repeaterLvl1[1].number2Lvl2")).contains(202L);
                assertThat(getDataStore().getRepeatedValues("repeaterLvl2", "repeaterLvl1[0].")).hasSize(0);
                assertThat(getDataStore().getRepeatedValues("repeaterLvl2", "repeaterLvl1[1].")).hasSize(0);
                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-repeater-lvl2-complex-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserEncrypted(final XByteArrayInputStream inputStream) throws IOException {
        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(10);

                // Field 1
                assertThat(getDataStore().getText("tag").orElse("NO TEXT")).isEqualTo("ABCDEFGHIJ");

                // Field 2
                assertThat(getDataStore().getRawValue("unknown").orElse(null)).isEqualTo(new byte[]{0x1,0x2,0x3,0x4,0x5});

                // Field 3 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").orElse(null)).isEqualTo(500L);
                assertThat(getDataStore().getFloatingPoint("repeater[0].numberF").orElse(null)).isEqualTo(257.45166f);
                assertThat(getDataStore().getText("repeater[0].text").orElse("NO TEXT")).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter").orElse(null)).isEqualTo(new byte[]{0xA});
                // Field 3 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").orElse(null)).isEqualTo(1000L);
                assertThat(getDataStore().getFloatingPoint("repeater[1].numberF").orElse(null)).isEqualTo(86.714584f);
                assertThat(getDataStore().getText("repeater[1].text").orElse("NO TEXT")).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter").orElse(null)).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-encrypted-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserHalfFloat() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-halfFloat.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getFloatingPoint("hf1").orElse(null)).isEqualTo(3.78125f);

                // Field 2
                assertThat(getDataStore().getFloatingPoint("hf2").orElse(null)).isEqualTo(4.5664062f);

                // Field 3
                assertThat(getDataStore().getFloatingPoint("hf3").orElse(null)).isEqualTo(5.5703125f);

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-halfFloat-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserVeryShortInt() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-veryShortInt.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getInteger("vsi1").orElse(null)).isEqualTo(67);

                // Field 2
                assertThat(getDataStore().getInteger("vsi2").orElse(null)).isEqualTo(68);

                // Field 3
                assertThat(getDataStore().getInteger("vsi3").orElse(null)).isEqualTo(69);

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-veryShortInt-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserLittleEndian() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-littleEndian.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(10);

                // Field 1
                assertThat(getDataStore().getText("tag").orElse(null)).isEqualTo("ABCDEFGHIJ");

                // Field 2
                assertThat(getDataStore().getRawValue("unknown").orElse(null)).isEqualTo(new byte[]{0x1,0x2,0x3,0x4,0x5});

                // Field 3 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").orElse(null)).isEqualTo(500L);
                assertThat(getDataStore().getFloatingPoint("repeater[0].numberF").orElse(null)).isEqualTo(257.45166f);
                assertThat(getDataStore().getText("repeater[0].text").orElse("NO TEXT")).isEqualTo("ABCD");
                assertThat(getDataStore().getRawValue("repeater[0].delimiter").orElse(null)).isEqualTo(new byte[]{0xA});
                // Field 3 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").orElse(null)).isEqualTo(1000L);
                assertThat(getDataStore().getFloatingPoint("repeater[1].numberF").orElse(null)).isEqualTo(86.714584f);
                assertThat(getDataStore().getText("repeater[1].text").orElse("NO TEXT")).isEqualTo("EFGH");
                assertThat(getDataStore().getRawValue("repeater[1].delimiter").orElse(null)).isEqualTo(new byte[] {0xB});

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-littleEndian-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithExternalStructure(XByteArrayInputStream inputStream) throws IOException {
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

    private GenericParser<String> createGenericParserForFormulas() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-formulas.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {

                assertThat(getDataStore().size()).isEqualTo(5);

                // Field 1
                assertThat(getDataStore().getInteger("sizeIndicator").orElse(null)).isEqualTo(3L);

                // Field 2 - item 0
                assertThat(getDataStore().getInteger("repeater[0].number").orElse(null)).isEqualTo(1L);
                // Field 2 - item 1
                assertThat(getDataStore().getInteger("repeater[1].number").orElse(null)).isEqualTo(2L);
                // Field 2 - item 2
                assertThat(getDataStore().getInteger("repeater[2].number").orElse(null)).isEqualTo(3L);

                // Field 3
                assertThat(getDataStore().getText("aValue").orElse(null)).isEqualTo("ABCDEFGHIJ");

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-formulas-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserSigned() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-signedInteger.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().size()).isEqualTo(3);

                // Field 1
                assertThat(getDataStore().getInteger("si1").orElse(null)).isEqualTo(-60);

                // Field 2
                assertThat(getDataStore().getInteger("si2").orElse(null)).isEqualTo(0);

                // Field 3
                assertThat(getDataStore().getInteger("si3").orElse(null)).isEqualTo(60);

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-signedInteger-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConstants() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-constants.bin");

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

    private GenericParser<String> createGenericParserWithGap() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-gap.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-gap-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConstantsUnmatching() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-constants.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-constants-unmatching-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConstantsUnmatchingAndCheckDisabled() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-constants.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-constants-unmatching-nocheck-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithGapUnmatching() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-gap-mismatch.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-gap-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithGapUnmatchingAndCheckDisabled() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-gap-mismatch.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().isEmpty());

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-gap-nocheck-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConditionSatisfied() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-conditional-with.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().size()).isEqualTo(3);
                assertThat(getDataStore().getText("tag")).contains("ABCDE");
                assertThat(getDataStore().getInteger("flag")).contains(1L);
                assertThat(getDataStore().getRawValue("optional")).isPresent();

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-conditional-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithConditionUnsatisfied() throws IOException {
        XByteArrayInputStream inputStream = createInputStreamFromReferenceFile("/files/samples/TEST-conditional-without.bin");

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                assertThat(getDataStore().size()).isEqualTo(2);
                assertThat(getDataStore().getText("tag")).contains("ABCDE");
                assertThat(getDataStore().getInteger("flag")).contains(0L);
                assertThat(getDataStore().getRawValue("optional")).isEmpty();

                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-conditional-map.json";
            }
        };
    }

    private GenericParser<String> createGenericParserWithSimulatedEOS() throws IOException {
        XByteArrayInputStream inputStream = new XByteArrayInputStream(new byte[0]);

        return new GenericParser<String>(inputStream) {
            @Override
            protected String generate() {
                return DATA;
            }

            @Override
            public String getStructureResource() {
                return "/files/structures/TEST-map.json";
            }
        };
    }

    private XByteArrayInputStream createInputStreamFromReferenceFile(String referenceResource) throws IOException {
        return new XByteArrayInputStream(FilesHelper.readBytesFromResourceFile(referenceResource));
    }
}