package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dirty class to convert V1 resources to V2
 */
public class ResourcesV1ToV2Converter {

    private static final String JSON_DIR = "/media/sf_DevStore/GIT/tduf/lib-unlimited/src/test/resources/db/json/miner/TDU_Clothes_FAKE.json";

    public static void main(String... args) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        Files.walk(Paths.get(JSON_DIR), 1)

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

                        DbResourceEnhancedDto tempObjectV2 = DbResourceEnhancedDto.builder()
                                .atVersion("")
                                .withCategoryCount(0)
                                .build();
                        dbDto.getResources().forEach(resourceObjectV1 -> {

                            DbResourceEnhancedDto.Locale currentLocale = resourceObjectV1.getLocale();

                            version.set(resourceObjectV1.getVersion());
                            categoryCount.set(resourceObjectV1.getCategoryCount());

                            resourceObjectV1.getEntries().forEach(entry -> {

                                String reference = entry.getReference();
                                DbResourceEnhancedDto.Entry entryV2 = tempObjectV2.getEntryByReference(reference)
                                        .orElseGet(() -> tempObjectV2.addEntryByReference(reference));

                                entryV2.setValueForLocale(entry.getValue(), currentLocale);

                            });

                        });

                        DbResourceEnhancedDto resourceObjectV2 = DbResourceEnhancedDto.builder()
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
}
