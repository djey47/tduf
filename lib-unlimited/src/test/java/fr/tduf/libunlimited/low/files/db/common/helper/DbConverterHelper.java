package fr.tduf.libunlimited.low.files.db.common.helper;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Helps with data migration
 */
@Disabled
class DbConverterHelper {

    /**
     * Converts to 3 JSON files per topic instead of single one
     */
    @Test
    void splitJsonFiles() {

        String jsonDirectory = "/opt/workspaces/perso-git/tduf/lib-unlimited/src/test/resources/db/json";

        final List<DbDto> dbdtos = DbDto.Topic.valuesAsStream()
                .map(topic -> {
                    try {
                        return DatabaseReadWriteHelper.readGenuineDatabaseTopicFromJson(topic, jsonDirectory)
                                .orElse(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(dbdtos, jsonDirectory);
    }

    @Test
    void resourceModelV1ToV2() throws IOException {
        final String jsonDirectory = "/media/sf_DevStore/GIT/tduf/lib-unlimited/src/test/resources/db/json/miner/TDU_Clothes_FAKE.json";

        ObjectMapper objectMapper = new ObjectMapper();

        Files.walk(Paths.get(jsonDirectory), 1)

                .filter(Files::isRegularFile)

                .filter(path -> path.getFileName().toString().startsWith("TDU_"))

                .filter(path -> path.getFileName().toString().endsWith(".json"))

                .map(Path::toFile)

                .forEach(file -> {

                    try {
                        System.out.println("->" + file.getAbsolutePath());
                        DbDto dbDto = objectMapper.readValue(file, DbDto.class);

                        AtomicReference<String> version = new AtomicReference<>();
                        AtomicInteger categoryCount = new AtomicInteger();

                        DbResourceDto tempObjectV2 = DbResourceDto.builder()
                                .atVersion("")
                                .withCategoryCount(0)
                                .build();
//                        dbDto.getResources().forEach(resourceObjectV1 -> {
//
//                            DbResourceEnhancedDto.Locale currentLocale = resourceObjectV1.getLocale();
//
//                            version.set(resourceObjectV1.getVersion());
//                            categoryCount.set(resourceObjectV1.getCategoryCount());
//
//                            resourceObjectV1.getEntries().forEach(entry -> {
//
//                                String reference = entry.getReference();
//                                DbResourceEnhancedDto.Entry entryV2 = tempObjectV2.getEntryByReference(reference)
//                                        .orElseGet(() -> tempObjectV2.addEntryByReference(reference));
//
//                                entryV2.setValueForLocale(entry.getValue(), currentLocale);
//
//                            });
//
//                        });

                        DbResourceDto resourceObjectV2 = DbResourceDto.builder()
                                .atVersion(version.get())
                                .withCategoryCount(categoryCount.get())
                                .containingEntries(tempObjectV2.getEntries())
                                .build();

                        DbDto dbDtoEnhanced = DbDto.builder()
                                .withData(dbDto.getData())
                                .withStructure(dbDto.getStructure())
                                .withResource(resourceObjectV2)
                                .build();

                        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dbDtoEnhanced);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void globalizedResources() {
        String jsonDirectory = "/media/sf_DevStore/GIT/tduf/lib-unlimited/src/test/resources/db/json/ref";

        List<DbDto> dbDtos = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

        dbDtos.stream()
                .map(DbDto::getResource)
                .forEach(r -> {
                    List<ResourceEntryDto> currentEntries = new ArrayList<>(r.getEntries());
//                    r.setEntries(reduceGlobalResources(currentEntries));
                });

        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(dbDtos, jsonDirectory);
    }

    private static List<ResourceEntryDto> reduceGlobalResources(List<ResourceEntryDto> readEntries) {
        return readEntries.stream()
                .map(readEntry -> {
                    Set<String> values = Locale.valuesAsStream()
                            .map(locale -> readEntry.getValueForLocale(locale).orElse(""))
                            .collect(toSet());

                    if (1 == values.size()) {
                        return ResourceEntryDto.builder()
                                .forReference(readEntry.getReference())
                                .withDefaultItem(values.stream().findAny().get())
                                .build();
                    } else {
                        return readEntry;
                    }
                })
                .collect(toList());
    }

}
