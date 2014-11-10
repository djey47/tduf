package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

public class DbMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbDto.Item item11 = DbDto.Item.builder()
                .forName("ID")
                .withRawValue("111111")
                .build();
        item11.setValue("0000000");
        DbDto.Item item12 = DbDto.Item.builder()
                .forName("Speed")
                .withRawValue("111")
                .build();
        item12.setValue("111");
        DbDto.Item item21 = DbDto.Item.builder()
                .forName("ID")
                .withRawValue("222222")
                .build();
        item21.setValue("0000000");
        DbDto.Item item22 = DbDto.Item.builder()
                .forName("Speed")
                .withRawValue("222")
                .build();
        item22.setValue("222");
        DbDto.Entry entry1 = DbDto.Entry.builder()
                .withId(1L)
                .addItem(item11)
                .addItem(item12)
                .build();
        DbDto.Entry entry2 = DbDto.Entry.builder()
                .withId(1L)
                .addItem(item21)
                .addItem(item22)
                .build();
        DbDto dbDto = DbDto.builder()
                .addEntry(entry1)
                .addEntry(entry2)
                .build();
        String expectedJson = "{\n" +
                "  \"entries\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"items\" : [ {\n" +
                "      \"name\" : \"ID\",\n" +
                "      \"value\" : \"0000000\",\n" +
                "      \"rawValue\" : \"111111\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Speed\",\n" +
                "      \"value\" : \"111\",\n" +
                "      \"rawValue\" : \"111\"\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"id\" : 1,\n" +
                "    \"items\" : [ {\n" +
                "      \"name\" : \"ID\",\n" +
                "      \"value\" : \"0000000\",\n" +
                "      \"rawValue\" : \"222222\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Speed\",\n" +
                "      \"value\" : \"222\",\n" +
                "      \"rawValue\" : \"222\"\n" +
                "    } ]\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }
}