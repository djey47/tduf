package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbResourceMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbResourceDto.Entry commentEntry = DbResourceDto.Entry.builder()
                .withId(1L)
                .withComment("TDU_Achievements.fr")
                .build();
        DbResourceDto.LocalizedValue localizedValue1 = DbResourceDto.LocalizedValue.builder()
                .withLocale(DbResourceDto.Locale.FRANCE)
                .withValue("FR??")
                .build();
        DbResourceDto.LocalizedValue localizedValue2 = DbResourceDto.LocalizedValue.builder()
                .withLocale(DbResourceDto.Locale.ITALY)
                .withValue("IT??")
                .build();
        DbResourceDto.Entry normalEntry = DbResourceDto.Entry.builder()
                .withId(2L)
                .withReference("53410835")
                .addLocalizedValue(localizedValue1)
                .addLocalizedValue(localizedValue2)
                .build();
        DbResourceDto dbResourceDto = DbResourceDto.builder()
                .addEntry(commentEntry)
                .addEntry(normalEntry)
                .build();
        String expectedJson = "{\n" +
                "  \"entries\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"comment\" : true,\n" +
                "    \"localizedValues\" : [ ]\n" +
                "  }, {\n" +
                "    \"id\" : 2,\n" +
                "    \"reference\" : \"53410835\",\n" +
                "    \"comment\" : false,\n" +
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
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbResourceDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}