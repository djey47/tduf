package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static net.sf.json.test.JSONAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class DbParserTest {

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstanceWithoutErrors() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
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
        assertThat(dbParser.getContentLineCount()).isEqualTo(14);
        assertThat(dbParser.getResourceCount()).isEqualTo(1);
        assertThat(dbParser.getIntegrityErrors()).isEmpty();
    }

    @Test
    public void parseAll_whenProvidedContents_andIntegrityError_shouldReturnError() throws Exception {
        //GIVEN : item count != actual item count
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
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
        assertThat(dbParser.getIntegrityErrors()).hasSize(1);
    }

    @Test
    public void parseAll_whenRealFiles_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        List<List<String>> resourceLines = readResourcesFromSamples("/db/res/TDU_Achievements.fr", "/db/res/TDU_Achievements.it");


        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);
        DbDto db = dbParser.parseAll();


        // JSON DISPLAY
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        // Uncomment to fetch and actualize JSON result
//        System.out.println("JSON DISPLAY");
//        System.out.println(jsonResult);
        //


        //THEN
        assertThat(dbParser.getContentLineCount()).isEqualTo(90);
        assertThat(dbParser.getResourceCount()).isEqualTo(2);
        assertThat(dbParser.getIntegrityErrors()).isEmpty();

        String expectedJson = readTextFromSample("/db/TDU_Achievements.json", "UTF-8");
        assertJsonEquals(expectedJson, jsonResult);
    }

    private List<List<String>> readResourcesFromSamples(String... sampleFiles) throws IOException{
        List<List<String>> resourceLines = newArrayList();

        for (String sampleFile : sampleFiles) {
            resourceLines.add(readContentsFromSample(sampleFile, "UTF-16"));
        }

        return resourceLines;
    }

    private List<String> readContentsFromSample(String sampleFile, String encoding) throws IOException {
        List<String> lines = newArrayList();

        InputStream resourceAsStream = getClass().getResourceAsStream(sampleFile);

        Scanner scanner = new Scanner(resourceAsStream, encoding) ;
        scanner.useDelimiter("\r\n");

        while(scanner.hasNext()) {
            lines.add(scanner.next());
        }

        return lines;
    }

    private String readTextFromSample(String sampleFile, String charsetName) throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getResource(sampleFile).toURI());
        byte[] encoded = Files.readAllBytes(path);

        return new String(encoded, Charset.forName(charsetName));
    }
}