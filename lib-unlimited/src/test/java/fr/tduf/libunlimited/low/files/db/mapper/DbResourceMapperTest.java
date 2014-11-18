package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbResourceMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbResourceDto.LocalizedValue localizedValue1 = DbResourceDto.LocalizedValue.builder()
                .withLocale(DbResourceDto.Locale.FRANCE)
                .withValue("FR??")
                .build();
        DbResourceDto.LocalizedValue localizedValue2 = DbResourceDto.LocalizedValue.builder()
                .withLocale(DbResourceDto.Locale.ITALY)
                .withValue("IT??")
                .build();
        DbResourceDto.Entry entry = DbResourceDto.Entry.builder()
                .forReference("53410835")
                .fromCategory("Explanation")
                .addLocalizedValue(localizedValue1)
                .addLocalizedValue(localizedValue2)
                .build();
        DbResourceDto dbResourceDto = DbResourceDto.builder()
                .atVersion("1,2")
                .withCategoryCount(6)
                .addEntry(entry)
                .build();
        String expectedJson = "{\n" +
                "  \"version\" : \"1,2\",\n" +
                "  \"categoryCount\" : 6,\n" +
                "  \"entries\" : [ {\n" +
                "    \"ref\" : \"53410835\",\n" +
                "    \"category\" : \"Explanation\",\n" +
                "    \"localizedValues\" : [ {\n" +
                "      \"locale\" : \"FRANCE\",\n" +
                "      \"value\" : \"FR??\"\n" +
                "    }, {\n" +
                "      \"locale\" : \"ITALY\",\n" +
                "      \"value\" : \"IT??\"\n" +
                "    } ]\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbResourceDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}