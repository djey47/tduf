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
    public void dump_gen_check() throws IOException {

        String jsonDirectory = "integ-tests/db-json";
        String generatedDirectory = "integ-tests/db-generated";

        // WHEN: dump
        System.out.println("-> Dump!");
        DatabaseTool.main(new String[]{"dump", "-d", "integ-tests/db-encrypted", "-j", jsonDirectory});

        // THEN: written json files
        Map<String, Boolean> jsonFileResult = asList(DbDto.Topic.values()).stream()

                .map((topic) -> topic.getLabel() + ".json")

                .collect(toMap((fileName) -> fileName, (fileName) -> new File(jsonDirectory, fileName).exists()));

        long existingFilesCount = jsonFileResult.values().stream()

                .filter((existingFile) -> true)

                .count();

        assertThat(existingFilesCount).isEqualTo(18);


        // WHEN: gen
        System.out.println("-> Gen!");
        DatabaseTool.main(new String[]{"gen", "-d", generatedDirectory, "-j", jsonDirectory});

        // THEN: written TDU files
        Map<String, Boolean> dbFileResult = asList(DbDto.Topic.values()).stream()

                .map((topic) -> topic.getLabel() + ".db")

                .collect(toMap((fileName) -> fileName, (fileName) -> new File(generatedDirectory, fileName).exists()));

        existingFilesCount = dbFileResult.values().stream()

                .filter((existingFile) -> true)

                .count();

        assertThat(existingFilesCount).isEqualTo(18);

        asList(DbResourceDto.Locale.values()).stream()

                .forEach( (locale) -> {

                    Map<String, Boolean> resFileResult = asList(DbDto.Topic.values()).stream()

                            .map((topic) -> topic.getLabel() + "." + locale.getCode())

                            .collect(toMap((fileName) -> fileName, (fileName) -> new File(generatedDirectory, fileName).exists()));

                    long existingResourceFilesCount = resFileResult.values().stream()

                            .filter((existingFile) -> true)

                            .count();

                    assertThat(existingResourceFilesCount).isEqualTo(18);
                });


        // WHEN: check
        System.out.println("-> Check!");
        DatabaseTool.main(new String[]{"check", "-d", generatedDirectory});
    }
}