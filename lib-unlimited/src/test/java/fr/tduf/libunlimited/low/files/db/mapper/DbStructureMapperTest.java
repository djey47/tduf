package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;

import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbStructureMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbStructureDto.Item item1 = DbStructureDto.Item.builder()
                .withId(1L)
                .fromType(DbStructureDto.Type.UID)
                .forName("ID")
                .build();
        DbStructureDto.Item item2 = DbStructureDto.Item.builder()
                .withId(2L)
                .fromType(DbStructureDto.Type.REFERENCE)
                .forName("REF")
                .build();
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .addItem(item1)
                .addItem(item2)
                .build();
        String expectedJson = "{\n" +
                "  \"ref\" : \"2442784645\",\n" +
                "  \"fields\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"name\" : \"ID\",\n" +
                "    \"type\" : \"UID\"\n" +
                "  }, {\n" +
                "    \"id\" : 2,\n" +
                "    \"name\" : \"REF\",\n" +
                "    \"type\" : \"REFERENCE\"\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbStructureDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}