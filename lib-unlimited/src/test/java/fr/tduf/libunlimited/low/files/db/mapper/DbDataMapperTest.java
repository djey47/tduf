package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;


public class DbDataMapperTest {

    private static final Class<DbDataMapperTest> thisClass = DbDataMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbDataDto.Item item11 = DbDataDto.Item.builder()
                .ofFieldRank(1)
                .forName("ID")
                .withRawValue("111111")
                .build();
        DbDataDto.Item item12 = DbDataDto.Item.builder()
                .ofFieldRank(2)
                .forName("Name")
                .withRawValue("111")
                .build();
        DbDataDto.Item item13 = DbDataDto.Item.builder()
                .ofFieldRank(3)
                .forName("Switchs")
                .withRawValue("1")
                .build();
        DbDataDto.Item item21 = DbDataDto.Item.builder()
                .ofFieldRank(1)
                .forName("ID")
                .withRawValue("222222")
                .build();
        DbDataDto.Item item22 = DbDataDto.Item.builder()
                .ofFieldRank(2)
                .forName("Name")
                .withRawValue("222")
                .build();
        DbDataDto.Item item23 = DbDataDto.Item.builder()
                .ofFieldRank(3)
                .forName("Switchs")
                .withRawValue("0")
                .build();
        DbDataDto.Entry entry1 = DbDataDto.Entry.builder()
                .forId(1L)
                .addItem(item11)
                .addItem(item12)
                .addItem(item13)
                .build();
        DbDataDto.Entry entry2 = DbDataDto.Entry.builder()
                .forId(2L)
                .addItem(item21)
                .addItem(item22)
                .addItem(item23)
                .build();
        DbDataDto dbDataDto = DbDataDto.builder()
                .addEntry(entry1)
                .addEntry(entry2)
                .build();
        String expectedJson = "{\n" +
                "  \"entries\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"items\" : [ {\n" +
                "      \"name\" : \"ID\",\n" +
                "      \"rawValue\" : \"111111\",\n" +
                "      \"fieldRank\" : 1\n" +
                "    }, {\n" +
                "      \"name\" : \"Name\",\n" +
                "      \"rawValue\" : \"111\",\n" +
                "      \"fieldRank\" : 2\n" +
                "    }, {\n" +
                "      \"name\" : \"Switchs\",\n" +
                "      \"rawValue\" : \"1\",\n" +
                "      \"fieldRank\" : 3\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"id\" : 2,\n" +
                "    \"items\" : [ {\n" +
                "      \"name\" : \"ID\",\n" +
                "      \"rawValue\" : \"222222\",\n" +
                "      \"fieldRank\" : 1\n" +
                "    }, {\n" +
                "      \"name\" : \"Name\",\n" +
                "      \"rawValue\" : \"222\",\n" +
                "      \"fieldRank\" : 2\n" +
                "    }, {\n" +
                "      \"name\" : \"Switchs\",\n" +
                "      \"rawValue\" : \"0\",\n" +
                "      \"fieldRank\" : 3\n" +
                "    } ]\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbDataDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);

        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}