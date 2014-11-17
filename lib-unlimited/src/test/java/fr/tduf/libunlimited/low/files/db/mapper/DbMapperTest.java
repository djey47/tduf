package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;

import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbStructureDto dbStructureDto = DbStructureDto.builder().build();
        DbDataDto dbDataDto = DbDataDto.builder().build();
        DbResourceDto dbResourcesDto = DbResourceDto.builder().build();
        DbDto dbTopicDto = DbDto.builder()
                .forName("TDU_Achievements")
                .forRef("2442784645")
                .withStructure(dbStructureDto)
                .withData(dbDataDto)
                .withResources(dbResourcesDto)
                .build();
        String expectedJson = "{\n" +
                "  \"name\" : \"TDU_Achievements\",\n" +
                "  \"ref\" : \"2442784645\",\n" +
                "  \"structure\" : {\n" +
                "    \"items\" : [ ]\n" +
                "  },\n" +
                "  \"data\" : {\n" +
                "    \"entries\" : [ ]\n" +
                "  },\n" +
                "  \"resources\" : {\n" +
                "    \"entries\" : [ ]\n" +
                "  }\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbTopicDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}