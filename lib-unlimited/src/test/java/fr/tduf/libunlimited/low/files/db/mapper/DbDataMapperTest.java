package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbDataMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbDataDto.Item item11 = DbDataDto.Item.builder()
                .forName("ID")
                .withRawValue("111111")
                .build();
        item11.setValue("0000000");
        DbDataDto.Item item12 = DbDataDto.Item.builder()
                .forName("Speed")
                .withRawValue("111")
                .build();
        item12.setValue("111");
        DbDataDto.Item item21 = DbDataDto.Item.builder()
                .forName("ID")
                .withRawValue("222222")
                .build();
        item21.setValue("0000000");
        DbDataDto.Item item22 = DbDataDto.Item.builder()
                .forName("Speed")
                .withRawValue("222")
                .build();
        item22.setValue("222");
        DbDataDto.Entry entry1 = DbDataDto.Entry.builder()
                .forId(1L)
                .addItem(item11)
                .addItem(item12)
                .build();
        DbDataDto.Entry entry2 = DbDataDto.Entry.builder()
                .forId(1L)
                .addItem(item21)
                .addItem(item22)
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
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbDataDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}