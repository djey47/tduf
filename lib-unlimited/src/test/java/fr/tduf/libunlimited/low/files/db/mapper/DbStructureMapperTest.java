package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DbStructureMapperTest {

    private static final Class<DbStructureMapperTest> thisClass = DbStructureMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @BeforeEach
    public void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbStructureDto.Field field1 = DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.UID)
                .forName("ID")
                .ofRank(1)
                .build();
        DbStructureDto.Field field2 = DbStructureDto.Field.builder()
                .fromType(DbStructureDto.FieldType.REFERENCE)
                .forName("REF")
                .toTargetReference("2442784646")
                .ofRank(2)
                .build();
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .forTopic(ACHIEVEMENTS)
                .atVersion("1,0")
                .withCategoryCount(5)
                .addItem(field1)
                .addItem(field2)
                .build();
        String expectedJson = "{\n" +
                "  \"topic\" : \"ACHIEVEMENTS\",\n" +
                "  \"ref\" : \"2442784645\",\n" +
                "  \"version\" : \"1,0\",\n" +
                "  \"categoryCount\" : 5,\n" +
                "  \"fields\" : [ {\n" +
                "    \"name\" : \"ID\",\n" +
                "    \"type\" : \"UID\",\n" +
                "    \"rank\" : 1" +
                "  }, {\n" +
                "    \"name\" : \"REF\",\n" +
                "    \"type\" : \"REFERENCE\",\n" +
                "    \"targetRef\" : \"2442784646\",\n" +
                "    \"rank\" : 2" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbStructureDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);

        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}