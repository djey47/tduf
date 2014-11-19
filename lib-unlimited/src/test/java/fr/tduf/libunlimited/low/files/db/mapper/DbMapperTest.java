package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .forTopic(ACHIEVEMENTS)
                .build();
        DbDto dbTopicDto = DbDto.builder()
                .withStructure(dbStructureDto)
                .withData(DbDataDto.builder().build())
                .addResource(DbResourceDto.builder().build())
                .addResource(DbResourceDto.builder().build())
                .build();

        String expectedJson = "{\n" +
                "  \"topic\" : \"ACHIEVEMENTS\",\n" +
                "  \"ref\" : \"2442784645\",\n" +
                "  \"structure\" : {\n" +
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
        System.out.println("Actual JSON:" + jsonResult);


        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}