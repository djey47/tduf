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
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

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
        String jsonDirectory = "integ-tests/banks/db/out";

        // WHEN unpack-all
        System.out.println("-> UnpackAll!");
        DatabaseTool.main(new String[]{"unpack-all", "-d", databaseDirectory, "-j", jsonDirectory});

        // THEN
        // TODO
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