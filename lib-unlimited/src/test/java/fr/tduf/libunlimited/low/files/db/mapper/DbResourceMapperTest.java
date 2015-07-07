package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DbResourceMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbResourceDto.Entry entry1 = DbResourceDto.Entry.builder()
                .forReference("53410835")
                .withValue("VAL1")
                .build();
        DbResourceDto.Entry entry2 = DbResourceDto.Entry.builder()
                .forReference("54410835")
                .withValue("VAL2")
                .build();
        DbResourceDto dbResourceDto = DbResourceDto.builder()
                .atVersion("1,2")
                .withLocale(FRANCE)
                .withCategoryCount(6)
                .addEntry(entry1)
                .addEntry(entry2)
                .build();
        String expectedJson = "{\n" +
                "  \"locale\" : \"FRANCE\",\n" +
                "  \"version\" : \"1,2\",\n" +
                "  \"categoryCount\" : 6,\n" +
                "  \"entries\" : [ {\n" +
                "    \"ref\" : \"53410835\",\n" +
                "    \"value\" : \"VAL1\"\n" +
                "  }, {\n" +
                "    \"ref\" : \"54410835\",\n" +
                "    \"value\" : \"VAL2\"\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbResourceDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}