package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbDataMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbDataDto.Item item11 = DbDataDto.Item.builder()
                .forName("ID")
                .withSingleValue("111111")
                .build();
        DbDataDto.Item item12 = DbDataDto.Item.builder()
                .forName("Name")
                .withReferenceValue("111", "NAME1")
                .build();
        List<DbDataDto.Item.BitfieldSwitch> bitfield1 = asList(DbDataDto.Item.BitfieldSwitch.builder()
                .forName("isEnabled")
                .status(true)
                .build());
        DbDataDto.Item item13 = DbDataDto.Item.builder()
                .forName("Switchs")
                .withBitfieldSwitches("1", bitfield1)
                .build();
        DbDataDto.Item item21 = DbDataDto.Item.builder()
                .forName("ID")
                .withSingleValue("222222")
                .build();
        DbDataDto.Item item22 = DbDataDto.Item.builder()
                .forName("Name")
                .withReferenceValue("222", "NAME2")
                .build();
        List<DbDataDto.Item.BitfieldSwitch> bitfield2 = asList(DbDataDto.Item.BitfieldSwitch.builder()
                .forName("isEnabled")
                .status(false)
                .build());
        DbDataDto.Item item23 = DbDataDto.Item.builder()
                .forName("Switchs")
                .withBitfieldSwitches("0", bitfield2)
                .build();
        DbDataDto.Entry entry1 = DbDataDto.Entry.builder()
                .forId(1L)
                .addItem(item11)
                .addItem(item12)
                .addItem(item13)
                .build();
        DbDataDto.Entry entry2 = DbDataDto.Entry.builder()
                .forId(1L)
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
                "      \"value\" : \"111111\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Name\",\n" +
                "      \"value\" : \"NAME1\",\n" +
                "      \"rawValue\" : \"111\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Switchs\",\n" +
                "      \"bitfield\" : [ {\n" +
                "        \"name\" : \"isEnabled\",\n" +
                "        \"status\" : true\n" +
                "      } ],\n" +
                "      \"rawValue\" : \"1\"\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"id\" : 1,\n" +
                "    \"items\" : [ {\n" +
                "      \"name\" : \"ID\",\n" +
                "      \"value\" : \"222222\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Name\",\n" +
                "      \"value\" : \"NAME2\",\n" +
                "      \"rawValue\" : \"222\"\n" +
                "    }, {\n" +
                "      \"name\" : \"Switchs\",\n" +
                "      \"bitfield\" : [ {\n" +
                "        \"name\" : \"isEnabled\",\n" +
                "        \"status\" : false\n" +
                "      } ],\n" +
                "      \"rawValue\" : \"0\"\n" +
                "    } ]\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbDataDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}