package fr.tduf.cli.tools;

import com.esotericsoftware.minlog.Log;
import fr.tduf.cli.common.helper.ConsoleHelper;
import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseToolIntegTest {
    private static final Path PATH_INTEG_TESTS = Paths.get("integ-tests");
    private static final Path PATH_PATCHER = PATH_INTEG_TESTS.resolve("patcher");
    private static final Path PATH_DATABASE_BANKS = PATH_INTEG_TESTS.resolve("banks").resolve("db");

    private static final String DIRECTORY_DATABASE_BANKS = PATH_DATABASE_BANKS.toString();
    private static final String DIRECTORY_DATABASE_BANKS_OUTPUT = PATH_DATABASE_BANKS.resolve("out").toString();
    private static final String DIRECTORY_PATCH = PATH_PATCHER.toString();
    private static final String DIRECTORY_PATCH_OUTPUT = PATH_PATCHER.resolve("out").toString();
    private static final String DIRECTORY_ENCRYPTED_DATABASE = PATH_INTEG_TESTS.resolve("db-encrypted").toString();
    private static final String DIRECTORY_PATCHED_DATABASE = PATH_INTEG_TESTS.resolve("db-patched").toString();
    private static final String DIRECTORY_FIXED_DATABASE = PATH_INTEG_TESTS.resolve("db-fixed").toString();
    private static final String DIRECTORY_JSON_DATABASE = PATH_INTEG_TESTS.resolve("db-json").toString();
    private static final String DIRECTORY_ERR_JSON_DATABASE = PATH_INTEG_TESTS.resolve("db-json-errors").toString();
    private static final String DIRECTORY_GENERATED_DATABASE = PATH_INTEG_TESTS.resolve("db-generated").toString();
    private static final String DIRECTORY_ERR_GENERATED_DATABASE = PATH_INTEG_TESTS.resolve("db-generated-errors").toString();

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private DatabaseTool databaseTool;  // Used for bank testing only. Do not use twice in a same test method!

    @Before
    public void setUp() throws IOException {
        Log.set(Log.LEVEL_INFO);

        FileUtils.deleteDirectory(new File(DIRECTORY_PATCH_OUTPUT));
        FileUtils.deleteDirectory(new File(DIRECTORY_JSON_DATABASE));
        FileUtils.deleteDirectory(new File(DIRECTORY_FIXED_DATABASE));
        FileUtils.deleteDirectory(new File(DIRECTORY_ERR_GENERATED_DATABASE));
        FileUtils.deleteDirectory(new File(DIRECTORY_GENERATED_DATABASE));
    }

    @After
    public void tearDown() {
        BulkDatabaseMiner.clearAllCaches();

        ConsoleHelper.restoreStandardOutput();
    }

    @Test
    public void dumpGenCheck_shouldNotThrowError() throws IOException {
        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-n", "-d", DIRECTORY_ENCRYPTED_DATABASE, "-j", DIRECTORY_JSON_DATABASE});

        // THEN: written json files
        long jsonFilesCount = getTopicFileCount(DIRECTORY_JSON_DATABASE, EXTENSION_JSON);
        assertThat(jsonFilesCount).isEqualTo(18);


        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-n", "-d", DIRECTORY_GENERATED_DATABASE, "-j", DIRECTORY_JSON_DATABASE});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(DIRECTORY_GENERATED_DATABASE);


        // WHEN: check
        System.out.println("-> Check!");
        DatabaseTool.main(new String[]{"check", "-n", "-d", DIRECTORY_GENERATED_DATABASE});

        // THEN: should not exit with status code 1
    }

    @Test
    public void genFix_shouldNotThrowError() throws IOException {
        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-n", "-d", DIRECTORY_ERR_GENERATED_DATABASE, "-j", DIRECTORY_ERR_JSON_DATABASE});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(DIRECTORY_ERR_GENERATED_DATABASE);


        // WHEN: fix
        System.out.println("-> Fix!");
        DatabaseTool.main(new String[]{"fix", "-n", "-d", DIRECTORY_ERR_GENERATED_DATABASE, "-o", DIRECTORY_FIXED_DATABASE});

        // THEN: written fixed TDU files
        assertDatabaseFilesArePresent(DIRECTORY_FIXED_DATABASE);
    }

    @Test
    public void dumpApplyPatchGenPatch() throws IOException, JSONException {
        // GIVEN
        String inputPatchFile = Paths.get(DIRECTORY_PATCH, "mini.json").toString();
        String inputPatchWithPartialChangesFile = Paths.get(DIRECTORY_PATCH, "mini-partialUpdate.json").toString();
        String outputPatchFile = Paths.get(DIRECTORY_PATCH_OUTPUT, "mini-gen.json").toString();
        String referencePatchFile = Paths.get(DIRECTORY_PATCH, "mini-gen.json").toString();

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-n", "-d", DIRECTORY_ENCRYPTED_DATABASE, "-j", DIRECTORY_JSON_DATABASE});

        // WHEN: applyPatch
        System.out.println("-> ApplyPatch!");
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", DIRECTORY_JSON_DATABASE, "-o", DIRECTORY_JSON_DATABASE, "-p", inputPatchFile});
        System.out.println("-> ApplyPatch! (partial changes)");
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", DIRECTORY_JSON_DATABASE, "-o", DIRECTORY_PATCHED_DATABASE, "-p", inputPatchWithPartialChangesFile});

        // THEN: files must exist
        long jsonFilesCount = getTopicFileCount(DIRECTORY_PATCHED_DATABASE, EXTENSION_JSON);
        assertThat(jsonFilesCount).isEqualTo(18);

        // THEN: contents must be updated
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_PATCHED_DATABASE);
        assertCarPhysicsEntryWithRefHasFieldValue("632098801", 103, "8900", "bitfield patched to 8900", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue("70033960", 103, "8901", "bitfield patched to 8901", actualDatabaseObjects);

        // WHEN: genPatch from patched database
        System.out.println("-> GenPatch!");
        DatabaseTool.main(new String[]{"gen-patch", "-n", "-j", DIRECTORY_PATCHED_DATABASE, "-p", outputPatchFile, "-t", CAR_PHYSICS_DATA.name(), "-r", "606298799,632098801,637314272,70033960"});

        // THEN: patch file must exist
        AssertionsHelper.assertFileExistAndGet(outputPatchFile);
        AssertionsHelper.assertJsonFilesMatch(referencePatchFile, outputPatchFile);
    }

    @Test
    public void genPatch_withPartialContents() throws IOException, JSONException {
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
    public void unpackAllRepackAll_shouldCallGateway() throws IOException {
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
        databaseTool.doMain(new String[]{"repack-all", /*"-n",*/ "-j", unpackJsonDirectory, "-o", DIRECTORY_DATABASE_BANKS_OUTPUT,});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
    }

    @Test
    public void unpackAll_simple() throws IOException {
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

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.getElements(), 6);

        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);

        assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
        assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
        assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
    }

    @Test
    public void unpackAll_withFix() throws IOException {
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

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.getElements(), 7);

        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "remainingIntegrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);

        assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
        assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
        assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
    }

    @Test
    public void unpackAll_withDeepCheck_andFix() throws IOException {
        // GIVEN
        String unpackJsonDirectory = Paths.get(DIRECTORY_DATABASE_BANKS_OUTPUT, "json").toString();

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all (with deep-check and fix)
        System.out.println("-> UnpackAll!");
        OutputStream outputStream = ConsoleHelper.hijackStandardOutput();
        databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", DIRECTORY_DATABASE_BANKS, "-j", unpackJsonDirectory, "-x", "-m"});


        // THEN
        String jsonContents = ConsoleHelper.finalizeAndGetContents(outputStream);
        JsonNode rootJsonNode = new ObjectMapper().readTree(jsonContents);

        AssertionsHelper.assertJsonNodeIteratorHasItems(rootJsonNode.getElements(), 7);

        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "missingTopicContents", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "integrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "remainingIntegrityErrors", 18);
        AssertionsHelper.assertJsonChildArrayHasSize(rootJsonNode, "writtenFiles", 0);

        assertThat(rootJsonNode.get("sourceDatabaseDirectory").asText()).endsWith(DIRECTORY_DATABASE_BANKS);
        assertThat(rootJsonNode.get("jsonDatabaseDirectory").asText()).endsWith(unpackJsonDirectory);
        assertThat(rootJsonNode.get("temporaryDirectory").asText()).startsWith("/tmp/");
    }

    @Test
    public void convertPatch_fromAndBackPchFile() throws IOException {
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
    public void dumpApplyTdupk_withExistingSlotRef_shouldAlterCarPhysicsContents() throws IOException {
        // GIVEN
        String inputPerformancePackFile = Paths.get(DIRECTORY_PATCH, "tdupe", "F150.tdupk").toString();
        String vehicleSlotReference = "606298799";


        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-n", "-d", DIRECTORY_ENCRYPTED_DATABASE, "-j", DIRECTORY_JSON_DATABASE});


        // WHEN: apply TDUPE performance pack
        System.out.println("-> ApplyTdupk!");
        DatabaseTool.main(new String[]{"apply-tdupk", "-n", "-j", DIRECTORY_JSON_DATABASE, "-p", inputPerformancePackFile, "-o", DIRECTORY_JSON_DATABASE, "-r", vehicleSlotReference});


        // THEN
        List<DbDto> actualDatabaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(DIRECTORY_JSON_DATABASE);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 2, "77061", "physical contents patched at field rank 2", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 5, "78900265", "physical contents patched at field rank 5", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 6, "1", "physical contents patched at field rank 6", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 7, "43055", "physical contents patched at field rank 7", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 8, "59368917", "physical contents patched at field rank 8", actualDatabaseObjects);
        assertCarPhysicsEntryWithRefHasFieldValue(vehicleSlotReference, 99, "238", "physical contents patched at field rank 99", actualDatabaseObjects);
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

    private static long getTopicFileCount(String jsonDirectory, String extension) {
        Map<String, Boolean> jsonFileResult = Stream.of(DbDto.Topic.values())

                .map((topic) -> topic.getLabel() + "." + extension)

                .collect(toMap((fileName) -> fileName, (fileName) -> new File(jsonDirectory, fileName).exists()));

        return jsonFileResult.values().stream()

                .filter((existingFile) -> true)

                .count();
    }

    private static void assertDatabaseFilesArePresent(String directory) {
        long dbFilesCount = getTopicFileCount(directory, "db");
        assertThat(dbFilesCount).isEqualTo(18);

        Stream.of(DbResourceDto.Locale.values())

                .forEach((locale) -> {
                    long resFilesCount = getTopicFileCount(directory, locale.getCode());
                    assertThat(resFilesCount).isEqualTo(18);
                });
    }

    private static void assertCarPhysicsEntryWithRefHasFieldValue(String ref, int fieldRank, String expectedValue, String label, List<DbDto> actualDatabaseObjects) {
        Optional<DbDataDto.Entry> potentialEntry = BulkDatabaseMiner.load(actualDatabaseObjects).getContentEntryFromTopicWithReference(ref, CAR_PHYSICS_DATA);

        assertThat(potentialEntry)
                .isPresent()
                .has(new Condition<>(
                        entry -> expectedValue.equals(entry.get().getItemAtRank(fieldRank).get().getRawValue()),
                        label));
    }
}
