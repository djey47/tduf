package fr.tduf.cli.tools;

import fr.tduf.libtesting.common.helper.AssertionsHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseToolIntegTest {

    private static final String DIRECTORY_PATCH = Paths.get("integ-tests", "patcher").toString();
    private static final String DIRECTORY_ENCRYPTED_DATABASE = Paths.get("integ-tests", "db-encrypted").toString();
    private static final String DIRECTORY_JSON_DATABASE = Paths.get("integ-tests", "db-json").toString();
    // TODO add missing directories to prevent duplication

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private DatabaseTool databaseTool;  // Used for bank testing only. Do not use twice in a same test method!

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File("integ-tests/db/out"));
        FileUtils.deleteDirectory(new File(DIRECTORY_PATCH, "out"));
        FileUtils.deleteDirectory(new File(DIRECTORY_JSON_DATABASE));
    }

    @Test
    public void dumpGenCheck_shouldNotThrowError() throws IOException {

        String sourceDirectory = "integ-tests/db-encrypted";
        String jsonDirectory = "integ-tests/db-json";
        String generatedDirectory = "integ-tests/db-generated";

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-n", "-d", sourceDirectory, "-j", jsonDirectory});

        // THEN: written json files
        long jsonFilesCount = getTopicFileCount(jsonDirectory, "json");
        assertThat(jsonFilesCount).isEqualTo(18);


        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-n", "-d", generatedDirectory, "-j", jsonDirectory});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(generatedDirectory);


        // WHEN: check
        System.out.println("-> Check!");
        DatabaseTool.main(new String[]{"check", "-n", "-d", generatedDirectory});

        // THEN: should not exit with status code 1
    }

    @Test
    public void genFix_shouldNotThrowError() throws IOException {

        String jsonErrorsDirectory = "integ-tests/db-json-errors";
        String generatedErrorsDirectory = "integ-tests/db-generated-errors";
        String fixedDirectory = "integ-tests/db-fixed";

        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-n", "-d", generatedErrorsDirectory, "-j", jsonErrorsDirectory});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(generatedErrorsDirectory);


        // WHEN: fix
        System.out.println("-> Fix!");
        DatabaseTool.main(new String[]{"fix", "-n", "-d", generatedErrorsDirectory, "-o", fixedDirectory});

        // THEN: written fixed TDU files
        assertDatabaseFilesArePresent(fixedDirectory);
    }

    @Test
    public void dumpApplyPatchGenPatch() throws IOException, JSONException {
        // GIVEN
        String sourceDirectory = "integ-tests/db-encrypted";
        String jsonDirectory = "integ-tests/db-json";
        String patchedDirectory = "integ-tests/db-patched";
        String inputPatchFile = Paths.get(DIRECTORY_PATCH, "mini.json").toString();
        String outputPatchFile = Paths.get(DIRECTORY_PATCH, "mini-gen.json").toString();
        String referencePatchFile = Paths.get(DIRECTORY_PATCH, "mini-gen.json").toString();

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-n", "-d", sourceDirectory, "-j", jsonDirectory});

        // WHEN: applyPatch
        System.out.println("-> ApplyPatch!");
        DatabaseTool.main(new String[]{"apply-patch", "-n", "-j", jsonDirectory, "-o", patchedDirectory, "-p", inputPatchFile});

        // THEN: files must exist
        long jsonFilesCount = getTopicFileCount(patchedDirectory, "json");
        assertThat(jsonFilesCount).isEqualTo(18);

        // WHEN: genPatch
        System.out.println("-> GenPatch!");
        DatabaseTool.main(new String[]{"gen-patch", "-n", "-j", jsonDirectory, "-p", outputPatchFile, "-t", CAR_PHYSICS_DATA.name(), "-r", "606298799,632098801"});

        // THEN: patch file must exist
        AssertionsHelper.assertFileExistAndGet(outputPatchFile);
        AssertionsHelper.assertJsonFilesMatch(referencePatchFile, outputPatchFile);
    }

    @Test
    public void unpackAllRepackAll_shouldCallGateway() throws IOException {
        // GIVEN
        String databaseDirectory = "integ-tests/banks/db";
        String unpackJsonDirectory = "integ-tests/banks/db/out/json";
        String outputDirectory = "integ-tests/banks/db/out";
        String repackJsonDirectory = "integ-tests/db-json-errors";

        doAnswer(DatabaseToolIntegTest::fakeAndAssertExtractAll)
                .when(bankSupportMock).extractAll(anyString(), anyString());


        // WHEN unpack-all
        System.out.println("-> UnpackAll!");
        this.databaseTool.doMain(new String[]{"unpack-all", "-n", "-d", databaseDirectory, "-j", unpackJsonDirectory});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).extractAll(anyString(), anyString());


        // GIVEN
        this.databaseTool = new DatabaseTool();
        this.databaseTool.setBankSupport(bankSupportMock);

        doAnswer(DatabaseToolIntegTest::fakeAndAssertPreparePackAll)
                .when(bankSupportMock).preparePackAll(anyString(), anyString());
        doAnswer(DatabaseToolIntegTest::fakeAndAssertPackAll)
                .when(bankSupportMock).packAll(anyString(), anyString());


        // WHEN repack-all
        System.out.println("-> RepackAll!");
        this.databaseTool.doMain(new String[]{"repack-all", /*"-n",*/ "-j", repackJsonDirectory, "-o", outputDirectory,});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).preparePackAll(anyString(), anyString());
        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
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
        Optional<DbDataDto.Entry> potentialEntry = BulkDatabaseMiner.load(actualDatabaseObjects).getContentEntryFromTopicWithReference(vehicleSlotReference, CAR_PHYSICS_DATA);

        assertThat(potentialEntry)
                .isPresent()
                .has(new Condition<>(

                        entry -> {
                            List<String> actualEntryItemValues = entry.get().getItems().stream()

                                    .map(DbDataDto.Item::getRawValue)

                                    .collect(toList());

                            return "77061".equals(actualEntryItemValues.get(1))
                                    && "78900265".equals(actualEntryItemValues.get(4))
                                    && "1".equals(actualEntryItemValues.get(5))
                                    && "43055".equals(actualEntryItemValues.get(6))
                                    && "59368917".equals(actualEntryItemValues.get(7))
                                    && "238".equals(actualEntryItemValues.get(98));
                        }
                        , "physical contents patched"));
    }

    private static Object fakeAndAssertExtractAll(InvocationOnMock invocation) throws IOException {

        String bankFileName = (String) invocation.getArguments()[0];
        String outputDirectory = (String) invocation.getArguments()[1];

        String shortBankFileName = Paths.get(bankFileName).getFileName().toString();

        assertThat(shortBankFileName).startsWith("DB").endsWith(".bnk");
        assertThat(new File(outputDirectory)).exists();

        Files.createDirectories(Paths.get(outputDirectory, shortBankFileName));

        return null;
    }

    private static Object fakeAndAssertPreparePackAll(InvocationOnMock invocation) {
        String sourceDirectory = (String) invocation.getArguments()[0];
        String targetBankFileName = (String) invocation.getArguments()[1];

        String shortBankFileName = Paths.get(targetBankFileName).getFileName().toString();

        assertThat(shortBankFileName).startsWith("DB").endsWith(".bnk");
        assertThat(new File(sourceDirectory)).exists();

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
}