package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static net.sf.json.test.JSONAssert.assertJsonEquals;

public class DbStructureMapperTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Test
    public void serialize_shouldWriteProperJson() throws IOException {
        //GIVEN
        DbStructureDto.Field field1 = DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.UID)
                .forName("ID")
                .build();
        DbStructureDto.Field field2 = DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.REFERENCE)
                .forName("REF")
                .toTargetReference("2442784646")
                .build();
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .forTopic(ACHIEVEMENTS)
                .addItem(field1)
                .addItem(field2)
                .build();
        String expectedJson = "{\n" +
                "  \"topic\" : \"ACHIEVEMENTS\",\n" +
                "  \"ref\" : \"2442784645\",\n" +
                "  \"fields\" : [ {\n" +
                "    \"name\" : \"ID\",\n" +
                "    \"type\" : \"UID\"\n" +
                "  }, {\n" +
                "    \"name\" : \"REF\",\n" +
                "    \"type\" : \"REFERENCE\",\n" +
                "    \"targetRef\" : \"2442784646\"\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbStructureDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        assertJsonEquals(expectedJson, jsonResult);
    }
}