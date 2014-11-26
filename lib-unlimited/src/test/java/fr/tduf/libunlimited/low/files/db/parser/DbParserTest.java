package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.common.DbHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class DbParserTest {

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstanceWithoutErrors() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
                "// Fields: 9",
                "{TDU_Achievements} 2442784645",
                "{Achievement_Event_} u",
                "{TextIndex_} i",
                "{Nb_Achievement_Points_} i",
                "{Ach_Title_} h",
                "{Ach_Desc_} h",
                "{Explanation_} h",
                "{FailedExplain_} h",
                "{Reward_} u",
                "{Reward_Param_} i",
                "// items: 1",
                "55736935;5;20;54400734;54359455;54410835;561129540;5337472;211;",
                "\0");
        List<List<String>> resourceLines = asList(
                asList(
                        "// TDU_Achievements.fr",
                        "// version: 1,2",
                        "// categories: 6",
                        "// Explanation",
                        "{??} 53410835,",
                        "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
                )
        );

        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);

        //THEN
        assertThat(dbParser).isNotNull();
        assertThat(dbParser.getContentLineCount()).isEqualTo(15);
        assertThat(dbParser.getResourceCount()).isEqualTo(1);
        assertThat(dbParser.getIntegrityErrors()).isEmpty();
    }

    @Test
    public void parseAll_whenProvidedContents_andIntegrityErrors_shouldReturnErrors() throws Exception {
        //GIVEN : item count != actual item count
        // field count != actual field count
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
                "// Fields: 2",
                "{TDU_Achievements} 2442784645",
                "{Achievement_Event_} u",
                "// items: 10",
                "55736935;",
                "\0");
        List<List<String>> resourceLines = asList(
                asList(
                        "// TDU_Achievements.fr",
                        "// version: 1,2",
                        "// categories: 6",
                        "// Explanation",
                        "{??} 53410835,",
                        "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
                )
        );

        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);
        DbDto actualDb = dbParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(dbParser.getIntegrityErrors()).hasSize(2);
    }

    @Test
    public void parseAll_whenProvidedContents_andRemoteReference_shouldReadAccordingly() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_CarPhysicsData.db",
                "// Version: 1,2,",
                "// Categories: 20",
                "// Fields: 3",
                "{TDU_CarPhysicsData} 1975083164",
                "{REF} x",
                "{Car_Brand} r 1209165514",
                "{Car_Model} u",
                "// items: 1",
                "606298799;735;59938407;",
                "\0");
        List<List<String>> resourceLines = asList(
                asList(
                        "// TDU_CarPhysicsData.fr",
                        "// version: 1,2",
                        "// categories: 6",
                        "// Explanation",
                        "{??} 53410835,"
                )
        );
        DbStructureDto.Field expectedField =  DbStructureDto.Field.builder()
                .forName("Car_Brand")
                .fromType(DbStructureDto.FieldType.REFERENCE)
                .toTargetReference("1209165514")
                .build();

        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);
        DbDto actualDb = dbParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(dbParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb.getStructure().getFields()).hasSize(3);
        DbStructureDto.Field secondField = actualDb.getStructure().getFields().get(1);
        assertThat(secondField).isEqualTo(expectedField);
    }

    @Test
    public void parseAll_whenRealFiles_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = DbHelper.readContentsFromSample("/db/TDU_Achievements.db", "UTF-8", "\r\n");
        List<List<String>> resourceLines = DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.fr", "/db/res/TDU_Achievements.it");


        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);
        DbDto db = dbParser.parseAll();


        // JSON DISPLAY
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        // Uncomment below to fetch and actualize JSON result
//        System.out.println("JSON DISPLAY");
//        System.out.println(jsonResult);
        //


        //THEN
        assertThat(dbParser.getContentLineCount()).isEqualTo(90);
        assertThat(dbParser.getResourceCount()).isEqualTo(2);
        assertThat(dbParser.getIntegrityErrors()).isEmpty();

        String expectedJson = DbHelper.readTextFromSample("/db/TDU_Achievements.json", "UTF-8");
        assertJsonEquals(expectedJson, jsonResult);
    }
}