package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.AssertionsHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.json.JSONException;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseToolIntegTest {

    @Mock
    private BankSupport bankSupportMock;

    @InjectMocks
    private DatabaseTool databaseTool;  // Used for bank testing only. Do not use twice in a same test method!

    @Test
    public void dumpGenCheck_shouldNotThrowError() throws IOException {

        String sourceDirectory = "integ-tests/db-encrypted";
        String jsonDirectory = "integ-tests/db-json";
        String generatedDirectory = "integ-tests/db-generated";

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-d", sourceDirectory, "-j", jsonDirectory});

        // THEN: written json files
        long jsonFilesCount = getTopicFileCount(jsonDirectory, "json");
        assertThat(jsonFilesCount).isEqualTo(18);


        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-d", generatedDirectory, "-j", jsonDirectory});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(generatedDirectory);


        // WHEN: check
        System.out.println("-> Check!");
        DatabaseTool.main(new String[]{"check", "-d", generatedDirectory});

        // THEN: should not exit with status code 1
    }

    @Test
    public void genFix_shouldNotThrowError() throws IOException {

        String jsonErrorsDirectory = "integ-tests/db-json-errors";
        String generatedErrorsDirectory = "integ-tests/db-generated-errors";
        String fixedDirectory = "integ-tests/db-fixed";

        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-d", generatedErrorsDirectory, "-j", jsonErrorsDirectory});

        // THEN: written TDU files
        assertDatabaseFilesArePresent(generatedErrorsDirectory);


        // WHEN: fix
        System.out.println("-> Fix!");
        DatabaseTool.main(new String[]{"fix", "-d", generatedErrorsDirectory, "-o", fixedDirectory});

        // THEN: written fixed TDU files
        assertDatabaseFilesArePresent(fixedDirectory);
    }

    @Test
    public void dumpApplyPatchGenPatch() throws IOException, JSONException {
        // GIVEN
        String sourceDirectory = "integ-tests/db-encrypted";
        String jsonDirectory = "integ-tests/db-json";
        String patchedDirectory = "integ-tests/db-patched";
        String inputPatchFile = "integ-tests/patcher/mini.json";
        String outputPatchFile = "integ-tests/patcher/out/mini-gen.json";
        String referencePatchFile = "integ-tests/patcher/mini-gen.json";

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-d", sourceDirectory, "-j", jsonDirectory});

        // WHEN: applyPatch
        System.out.println("-> ApplyPatch!");
        DatabaseTool.main(new String[]{"apply-patch", "-j", jsonDirectory, "-o", patchedDirectory, "-p", inputPatchFile});

        // THEN: files must exist
        long jsonFilesCount = getTopicFileCount(patchedDirectory, "json");
        assertThat(jsonFilesCount).isEqualTo(18);

        // WHEN: genPatch
        System.out.println("-> GenPatch!");
        DatabaseTool.main(new String[]{"gen-patch", "-j", jsonDirectory, "-p", outputPatchFile, "-t", DbDto.Topic.CAR_PHYSICS_DATA.name(), "-r", "606298799,632098801"});

        // THEN: patch file must exist
        AssertionsHelper.assertFileExistAndGet(outputPatchFile);
        AssertionsHelper.assertJsonFilesMatch(outputPatchFile, referencePatchFile);
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
        this.databaseTool.doMain(new String[]{"unpack-all", "-d", databaseDirectory, "-j", unpackJsonDirectory});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).extractAll(anyString(), anyString());


        // GIVEN
        this.databaseTool = new DatabaseTool();
        this.databaseTool.setBankSupport(bankSupportMock);

        doAnswer(DatabaseToolIntegTest::fakeAndAssertPrepareFilesToBeRepacked)
                .when(bankSupportMock).prepareFilesToBeRepacked(anyString(), anyListOf(Path.class), anyString(), anyString());
        doAnswer(DatabaseToolIntegTest::fakeAndAssertPackAll)
                .when(bankSupportMock).packAll(anyString(), anyString());


        // WHEN repack-all
        System.out.println("-> RepackAll!");
        this.databaseTool.doMain(new String[]{"repack-all", "-j", repackJsonDirectory, "-o", outputDirectory,});


        // THEN: gateway was correctly called
        verify(bankSupportMock, times(9)).prepareFilesToBeRepacked(anyString(), anyListOf(Path.class), anyString(), anyString());
        verify(bankSupportMock, times(9)).packAll(anyString(), anyString());
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

    private static Object fakeAndAssertPrepareFilesToBeRepacked(InvocationOnMock invocation) {
        String sourceDirectory = (String) invocation.getArguments()[0];
        List<Path> repackedPaths = (List<Path>) invocation.getArguments()[1];
        String targetBankFileName = (String) invocation.getArguments()[2];
        String targetDirectory = (String) invocation.getArguments()[3];

        String shortBankFileName = Paths.get(targetBankFileName).getFileName().toString();

        assertThat(shortBankFileName).startsWith("DB").endsWith(".bnk");
        assertThat(repackedPaths).hasSize(18);
        assertThat(new File(sourceDirectory)).exists();
        assertThat(new File(targetDirectory)).exists();

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