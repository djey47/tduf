package fr.tduf.cli.tools;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libtesting.common.helper.ConsoleHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static fr.tduf.tests.IntegTestsConstants.RESOURCES_PATH;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DatabaseToolIntegTest {
    private static final Path PATH_PATCHER = RESOURCES_PATH.resolve("patcher");
    private static final Path PATH_DATABASE_BANKS = RESOURCES_PATH.resolve("banks").resolve("db");

    private static final String DIRECTORY_DATABASE_BANKS = PATH_DATABASE_BANKS.toString();
    private static final String DIRECTORY_DATABASE_BANKS_OUTPUT = PATH_DATABASE_BANKS.resolve("out").toString();
    private static final String DIRECTORY_PATCH = PATH_PATCHER.toString();
    private static final String DIRECTORY_PATCH_OUTPUT = PATH_PATCHER.resolve("out").toString();
    private static final String DIRECTORY_PATCHED_DATABASE = RESOURCES_PATH.resolve("db-patched").toString();
    private static final String DIRECTORY_JSON_DATABASE = RESOURCES_PATH.resolve("db-json").toString();
    private static final String DIRECTORY_ERR_JSON_DATABASE = RESOURCES_PATH.resolve("db-json-errors").toString();
    private static final String DIRECTORY_DIFF_JSON_DATABASE = RESOURCES_PATH.resolve("db-json-diff").toString();

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private DatabaseTool databaseTool;  // Used for bank testing only. Do not use twice in a same test method!

    @BeforeAll
    static void globalSetUp() {
        Log.set(Log.LEVEL_INFO);
    }
    
    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);

        FileUtils.deleteDirectory(new File(DIRECTORY_PATCH_OUTPUT));
        FileUtils.deleteDirectory(new File(DIRECTORY_JSON_DATABASE));
    }

    @AfterAll
    static void globalTearDown() {
        ConsoleHelper.restoreOutput();
    }

    @Test
    void applyPatchGenPatch() throws IOException, JSONException {
        // GIVEN
        String inputPatchFile = Paths.get(DIRECTORY_PATCH, "mini.json").toString();
        String inputPatchWithPartialChangesFile = Paths.get(DIRECTORY_PATCH, "mini-partialUpdate.json").toString();
        String outputPatchFile = Paths.get(DIRECTORY_PATCH_OUTPUT, "mini-gen.json").toString();
        String referencePatchFile = Paths.get(DIRECTORY_PATCH, "mini-gen.json").toString();

        // WHEN: applyPatch
        System.out.println("-> ApplyPatch!");
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", DIRECTORY_ERR_JSON_DATABASE, "-o", DIRECTORY_JSON_DATABASE, "-p", inputPatchFile});
        System.out.println("-> ApplyPatch! (partial changes)");
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", DIRECTORY_JSON_DATABASE, "-o", DIRECTORY_PATCHED_DATABASE, "-p", inputPatchWithPartialChangesFile});

        // THEN: files must exist
        long jsonFilesCount = getTopicFileCount(DIRECTORY_PATCHED_DATABASE, EXTENSION_JSON);
        assertThat(jsonFilesCount).isEqualTo(18);

        // THEN: contents must be updated
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_PATCHED_DATABASE);
        BulkDatabaseMiner miner = BulkDatabaseMiner.load(actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue("632098801", 103, "8900", "bitfield patched to 8900", miner);
        assertCarPhysicsEntryWithRefHasFieldValue("70033960", 103, "8901", "bitfield patched to 8901", miner);
        assertResourceWithRefHasValue(BOTS, "33333333", FRANCE, "Cindy", "new resource created", miner);
        assertResourceWithRefHasValue(BOTS, "58867256", GERMANY, "Gordon Freeman", "existing resource updated", miner);

        // THEN: resource must not be updated in strict mode
        assertResourceWithRefHasValue(BOTS, "54367256", FRANCE, "Brian", "existing resource not updated in strict mode", miner);

        // WHEN: genPatch from patched database
        System.out.println("-> GenPatch!");
        DatabaseTool.main(new String[]{"gen-patch", "-n", "-j", DIRECTORY_PATCHED_DATABASE, "-p", outputPatchFile, "-t", CAR_PHYSICS_DATA.name(), "-r", "606298799,632098801,637314272,70033960"});

        // THEN: patch file must exist
        AssertionsHelper.assertFileExistAndGet(outputPatchFile);
        AssertionsHelper.assertJsonFilesMatch(referencePatchFile, outputPatchFile);
    }

    @Test
    void applyPatch_withTemplateAndProperties() throws IOException {
        // GIVEN
        Files.deleteIfExists(PATH_PATCHER.resolve("effective-mini-template.json.properties"));
        String inputPatchFile = Paths.get(DIRECTORY_PATCH, "mini-template.json").toString();


        // WHEN: applyPatch
        System.out.println("-> ApplyPatch! (template and properties)");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", DIRECTORY_ERR_JSON_DATABASE, "-o", DIRECTORY_PATCHED_DATABASE, "-p", inputPatchFile});


        // THEN: Normalized output contents
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 2);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 54);
        final String effectivePatchPropertyFile = rootJsonNode.get("effectivePatchPropertyFile").asText();
        assertThat(effectivePatchPropertyFile).isNotNull();


        // THEN: contents must be updated
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_PATCHED_DATABASE);
        BulkDatabaseMiner miner = BulkDatabaseMiner.load(actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue("000003000", 9, "3000567", "File name set to 3000567 at #9", miner);
        assertCarPhysicsEntryWithRefHasFieldValue("000003000", 10, "000030001", "Default rim set to 000030001 at #10", miner);
        assertCarPhysicsEntryWithRefHasFieldValue("000003000", 102, "3000", "Id car set to 3000 at #102", miner);
        assertResourceWithRefHasValue(CAR_PHYSICS_DATA, "3000567", UNITED_STATES, "TDUCP_3000", "Created resource value #3000567: TDUCP_3000", miner);
        assertResourceWithRefHasValue(CAR_PHYSICS_DATA, "3000567", FRANCE, "TDUCP_3000", "Created resource value #3000567: TDUCP_3000", miner);
        assertCarPhysicsEntryWithRefHasFieldValue("63518960", 103, "8900", "bitfield patched to 8900", miner);


        // THEN: car rims must be deleted for a particular slot
        DbFieldValueDto criteria = DbFieldValueDto.fromCouple(1, "63518960");
        List<ContentEntryDto> carRims = miner.getContentEntriesMatchingSimpleCondition(criteria, CAR_RIMS);
        assertThat(carRims).isEmpty();


        // THEN: effective property file must exist with right contents
        final DatabasePatchProperties effectiveProperties = new DatabasePatchProperties();
        final File handle = new File(effectivePatchPropertyFile);
        assertThat(handle).exists();
        effectiveProperties.load(new FileInputStream(handle));
        assertThat(effectiveProperties.size()).isEqualTo(24);
    }

    @Test
    void applyPatches() throws IOException {
        // GIVEN
        String inputPatchesDirectory = Paths.get(DIRECTORY_PATCH, "batch").toString();


        // WHEN: applyPatches
        System.out.println("-> ApplyPatches!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        DatabaseTool.main(new String[]{"apply-patches", "-n", "-j", DIRECTORY_ERR_JSON_DATABASE, "-o", DIRECTORY_PATCHED_DATABASE, "-p", inputPatchesDirectory});


        // THEN: Normalized output contents
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 1);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 54);


        // THEN: contents must be updated
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_PATCHED_DATABASE);
        BulkDatabaseMiner miner = BulkDatabaseMiner.load(actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue("632098801", 103, "8900", "bitfield patched to 8900", miner);
        assertCarPhysicsEntryWithRefHasFieldValue("70033960", 103, "8901", "bitfield patched to 8901", miner);
    }

    @Test
    void genPatch_withPartialContents() throws IOException, JSONException {
        // GIVEN
        String outputPatchFile = Paths.get(DIRECTORY_PATCH_OUTPUT, "mini-partialUpdate-gen.json").toString();
        String referencePatchFile = Paths.get(DIRECTORY_PATCH, "mini-partialUpdate-gen.json").toString();

        // WHEN: genPatch
        System.out.println("-> GenPatch!");
        DatabaseTool.main(new String[]{"gen-patch", "-n", "-j", DIRECTORY_ERR_JSON_DATABASE, "-p", outputPatchFile, "-t", CAR_PHYSICS_DATA.name(), "-r", "606298799,632098801", "-f", "103"});

        // THEN: patch file must exist
        AssertionsHelper.assertFileExistAndGet(outputPatchFile);
        AssertionsHelper.assertJsonFilesMatch(referencePatchFile, outputPatchFile);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void unpackAllRepackAll_shouldCallGateway() throws IOException {
        // GIVEN
        String unpackJsonDirectory = Paths.get(DIRECTORY_DATABASE_BANKS_OUTPUT, "json").toString();

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all
        System.out.println("-> UnpackAll!");
        databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", DIRECTORY_DATABASE_BANKS, "-j", unpackJsonDirectory});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).extractAll(anyString(), anyString());


        // GIVEN
        databaseTool = new DatabaseTool();
        databaseTool.setBankSupport(bankSupportMock);

        doAnswer(DatabaseToolIntegTest::fakeAndAssertPackAll)
                .when(bankSupportMock).packAll(anyString(), anyString());


        // WHEN repack-all
        System.out.println("-> RepackAll!");
        databaseTool.doMain(new String[]{"repack-all", "-n", "-j", unpackJsonDirectory, "-o", DIRECTORY_DATABASE_BANKS_OUTPUT,});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void unpackAll_simple() throws IOException {
        // GIVEN
        String unpackJsonDirectory = Paths.get(DIRECTORY_DATABASE_BANKS_OUTPUT, "json").toString();

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all (with fix only)
        System.out.println("-> UnpackAll!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", DIRECTORY_DATABASE_BANKS, "-j", unpackJsonDirectory});


        // THEN
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 6);

        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);

        assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
        assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
        assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void unpackAll_withFix() throws IOException {
        // GIVEN
        String unpackJsonDirectory = Paths.get(DIRECTORY_DATABASE_BANKS_OUTPUT, "json").toString();

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all (with fix only)
        System.out.println("-> UnpackAll!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", DIRECTORY_DATABASE_BANKS, "-j", unpackJsonDirectory, "-m"});


        // THEN
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 7);

        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "remainingIntegrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);

        assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
        assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
        assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void unpackAll_withDeepCheck_andFix() throws IOException {
        // GIVEN
        String unpackJsonDirectory = Paths.get(DIRECTORY_DATABASE_BANKS_OUTPUT, "json").toString();

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all (with deep-check and fix)
        System.out.println("-> UnpackAll!");

        // TODO [2.0] See to implement assertions
//        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
//        exitRule.checkAssertionAfterwards(() -> {
//            String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
//            JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);
//
//            AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 7);
//
//            AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
//            AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 19);
//            AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "remainingIntegrityErrors", 19);
//            AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);
//
//            assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
//            assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
//            assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
//        });

        databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", DIRECTORY_DATABASE_BANKS, "-j", unpackJsonDirectory, "-x", "-m"});
    }

    @Test
    void convertPatch_fromAndBackPchFile() throws IOException {
        // GIVEN
        String tdumtPatchDirectory = Paths.get(DIRECTORY_PATCH, "tdumt").toString();
        String inputPatchFile = Paths.get(tdumtPatchDirectory, "install_community_patch.pch").toString();

        Path outJsonPath = Paths.get(tdumtPatchDirectory, "out-json");
        Path inputPath = Paths.get(inputPatchFile);

        String inputPchPatchFile =  Paths.get(outJsonPath.toString(), "install_community_patch.pch").toString();
        String outputJsonPatchFile =  Paths.get(outJsonPath.toString(), "install_community_patch.json").toString();

        FileUtils.deleteDirectory(outJsonPath.toFile());
        Files.createDirectories(outJsonPath);

        Files.copy(inputPath, Paths.get(inputPchPatchFile));


        // WHEN: convert-patch
        System.out.println("-> ConvertPatch! pch=>json");
        DatabaseTool.main(new String[]{"convert-patch", "-n", "-p", inputPchPatchFile});


        // THEN: output file exists
        assertThat(new File(outputJsonPatchFile)).exists();


        // GIVEN
        Path outPchPath = Paths.get(tdumtPatchDirectory, "out-pch");
        String inputJsonPatchFile =  Paths.get(outPchPath.toString(), "install_community_patch.json").toString();
        String outputPchPatchFile =  Paths.get(outPchPath.toString(), "install_community_patch.pch").toString();

        FileUtils.deleteDirectory(outPchPath.toFile());
        Files.createDirectories(outPchPath);

        Files.copy(Paths.get(outputJsonPatchFile), Paths.get(inputJsonPatchFile));


        // WHEN: convert-patch
        System.out.println("-> ConvertPatch! json=>pch");
        DatabaseTool.main(new String[]{"convert-patch", "-n", "-p", inputJsonPatchFile});


        // THEN: output file exists
        assertThat(new File(outputPchPatchFile)).exists();
    }

    @Test
    void applyTdupk_withExistingSlotRef_shouldAlterCarPhysicsContents() throws IOException {
        // GIVEN
        String inputPerformancePackFile = Paths.get(DIRECTORY_PATCH, "tdupe", "F150.tdupk").toString();
        String vehicleSlotReference = "606298799";


        // WHEN: apply TDUPE performance pack
        System.out.println("-> ApplyTdupk!");
        DatabaseTool.main(new String[]{"apply-tdupk", "-n", "-j", DIRECTORY_ERR_JSON_DATABASE, "-p", inputPerformancePackFile, "-o", DIRECTORY_JSON_DATABASE, "-r", vehicleSlotReference});


        // THEN
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_JSON_DATABASE);
        BulkDatabaseMiner miner = BulkDatabaseMiner.load(actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 2, "735", "non-physical contents NOT patched at field rank 2", miner);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 5, "78900265", "physical contents patched at field rank 5", miner);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 6, "1", "physical contents patched at field rank 6", miner);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 8, "59368917", "physical contents patched at field rank 8", miner);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 99, "238", "physical contents patched at field rank 99", miner);
    }

    @Test
    void diffPatches_shouldGenerateMiniPatchFiles() throws IOException, JSONException {
        // GIVEN-WHEN: compute diff between 2 database files and reference
        System.out.println("-> DiffPatches!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        DatabaseTool.main(new String[]{"diff-patches", "-n", "-j", DIRECTORY_DIFF_JSON_DATABASE, "-J", DIRECTORY_ERR_JSON_DATABASE, "-p", DIRECTORY_PATCH_OUTPUT});


        // THEN: normalized output contents
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.elements(), 1);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 2);


        // THEN: output files exist
        Path actualCarRimsPatchPath = Paths.get(DIRECTORY_PATCH_OUTPUT, "CAR_RIMS.mini.json");
        Path actualRimsPatchPath = Paths.get(DIRECTORY_PATCH_OUTPUT, "RIMS.mini.json");
        assertThat(actualCarRimsPatchPath).exists();
        assertThat(actualRimsPatchPath).exists();


        // THEN: change objects have right contents
        Path referenceDiffPatchesPath = PATH_PATCHER.resolve("diff");
        AssertionsHelper.assertJsonFilesMatch(referenceDiffPatchesPath.resolve("CAR_RIMS.mini.json").toString(), actualCarRimsPatchPath.toString());
        AssertionsHelper.assertJsonFilesMatch(referenceDiffPatchesPath.resolve("RIMS.mini.json").toString(), actualRimsPatchPath.toString());
    }

    private static Object fakeAndAssertExtractAll(InvocationOnMock invocation) throws IOException {
        String bankFileName = (String) invocation.getArguments()[0];
        String outputDirectory = (String) invocation.getArguments()[1];

        String shortBankFileName = Paths.get(bankFileName).getFileName().toString();

        assertThat(shortBankFileName).startsWith("DB").endsWith(".bnk");

        Path outputPath = Paths.get(outputDirectory);
        assertThat(Files.exists(outputPath));

        Path originalBankPath = outputPath.resolve("original-" + shortBankFileName);
        Files.createFile(originalBankPath);

        return null;
    }

    private static Object fakeAndAssertPackAll(InvocationOnMock invocation) {
        String inputDirectory = (String) invocation.getArguments()[0];
        String outputBankFileName = (String) invocation.getArguments()[1];

        String shortBankFileName = Paths.get(outputBankFileName).getFileName().toString();

        assertThat(shortBankFileName).startsWith("DB").endsWith(".bnk");
        assertThat(new File(inputDirectory)).exists();

        return null;
    }

    /**
     * Only check for .data.json files (ignore structure and resources)
     */
    private static long getTopicFileCount(String jsonDirectory, String extension) {
        Map<String, Boolean> jsonFileResult = DbDto.Topic.valuesAsStream()

                .map((topic) -> String.format("%s.data.%s", topic.getLabel(), extension))

                .collect(toMap((fileName) -> fileName, (fileName) -> new File(jsonDirectory, fileName).exists()));

        return jsonFileResult.values().stream()

                .filter((existingFile) -> existingFile)

                .count();
    }

    private static void assertCarPhysicsEntryWithRefHasFieldValue(String ref, int fieldRank, String expectedValue, String label, BulkDatabaseMiner miner) {
        Optional<ContentEntryDto> potentialEntry = miner.getContentEntryFromTopicWithReference(ref, CAR_PHYSICS_DATA);

        assertThat(potentialEntry)
                .isPresent()
                .has(new Condition<>(
                        entry -> expectedValue.equals(entry.get().getItemAtRank(fieldRank).get().getRawValue()),
                        label));
    }

    private static void assertResourceWithRefHasValue(DbDto.Topic topic, String ref, Locale locale, String expectedValue, String label, BulkDatabaseMiner miner) {
        Optional<String> potentialValue = miner.getLocalizedResourceValueFromTopicAndReference(ref, topic, locale);

        assertThat(potentialValue)
                .contains(expectedValue)
                .as(label);
    }
}
