package fr.tduf.cli.tools;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseToolIntegTest {

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
        long dbFilesCount = getTopicFileCount(generatedDirectory, "db");
        assertThat(dbFilesCount).isEqualTo(18);

        asList(DbResourceDto.Locale.values()).stream()

                .forEach( (locale) -> {
                    long resFilesCount = getTopicFileCount(generatedDirectory, locale.getCode());
                    assertThat(resFilesCount).isEqualTo(18);
                });


        // WHEN: check
        System.out.println("-> Check!");
        DatabaseTool.main(new String[]{"check", "-d", generatedDirectory});

        // THEN: should not exit with status code 1
    }

    private static long getTopicFileCount(String jsonDirectory, String extension) {
        Map<String, Boolean> jsonFileResult = asList(DbDto.Topic.values()).stream()

                .map((topic) -> topic.getLabel() + "." + extension)

                .collect(toMap((fileName) -> fileName, (fileName) -> new File(jsonDirectory, fileName).exists()));

        return jsonFileResult.values().stream()

                .filter((existingFile) -> true)

                .count();
    }
}