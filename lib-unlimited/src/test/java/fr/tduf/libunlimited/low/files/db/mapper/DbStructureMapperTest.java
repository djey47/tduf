package fr.tduf.libunlimited.low.files.db.mapper;

import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

public class DbStructureMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException {
        //GIVEN
        DbStructureDto.Item item1 = DbStructureDto.Item.builder()
                .withId(1L)
                .fromType(DbStructureDto.Type.UID)
                .withName("ID")
                .build();
        DbStructureDto.Item item2 = DbStructureDto.Item.builder()
                .withId(2L)
                .fromType(DbStructureDto.Type.REFERENCE)
                .withName("REF")
                .forTarget("TARGET")
                .build();
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .addItem(item1)
                .addItem(item2)
                .build();
        String expectedJson = "{\n" +
                "  \"items\" : [ {\n" +
                "    \"id\" : 1,\n" +
                "    \"name\" : \"ID\",\n" +
                "    \"type\" : \"UID\",\n" +
                "    \"target\" : null\n" +
                "  }, {\n" +
                "    \"id\" : 2,\n" +
                "    \"name\" : \"REF\",\n" +
                "    \"type\" : \"REFERENCE\",\n" +
                "    \"target\" : \"TARGET\"\n" +
                "  } ]\n" +
                "}";

        //WHEN
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dbStructureDto);
        System.out.println("Actual JSON:" + jsonResult);

        //THEN
        JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }
}