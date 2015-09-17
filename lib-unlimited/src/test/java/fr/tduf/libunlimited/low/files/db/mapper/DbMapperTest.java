package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DbMapperTest {

    private static final Class<DbMapperTest> thisClass = DbMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .forTopic(ACHIEVEMENTS)
                .atVersion("1,0")
                .withCategoryCount(5)
                .build();
        DbDto dbTopicDto = DbDto.builder()
                .withStructure(dbStructureDto)
                .withData(DbDataDto.builder().build())
                .addResource(DbResourceDto.builder().build())
                .addResource(DbResourceDto.builder().build())
                .build();

        String expectedJson = "{\n" +
                "  \"structure\" : {\n" +
                "    \"ref\" : \"2442784645\",\n" +
                "    \"topic\" : \"ACHIEVEMENTS\",\n" +
                "    \"version\" : \"1,0\",\n" +
                "    \"categoryCount\" :  5,\n" +
                "    \"fields\" : [ ]\n" +
                "  },\n" +
                "  \"data\" : {\n" +
                "    \"entries\" : [ ]\n" +
                "  },\n" +
                "  \"resources\" : [ {\n" +
                "    \"entries\" : [ ],\n" +
                "    \"categoryCount\" : 0\n" +
                "  }, {\n" +
                "    \"entries\" : [ ],\n" +
                "    \"categoryCount\" : 0\n" +
                "  } ]\n" +
                "}";


        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbTopicDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);


        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}