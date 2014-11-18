package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DbParserTest {

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstance() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
                "{TDU_Achievements} 2442784645",
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
        assertThat(dbParser.getContentLineCount()).isEqualTo(4);
        assertThat(dbParser.getResourceCount()).isEqualTo(1);
        assertThat(dbParser.getResourceLinesCount()).isEqualTo(6);
    }

    @Test
    public void parseAll_whenRealFiles_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = readContentsFromSample("/db/TDU_achievements.db", "UTF-8");
        List<List<String>> resourceLines = readResourcesFromSamples("/db/res/TDU_achievements.fr", "/db/res/TDU_achievements.it");


        //WHEN
        DbDto dbDto = DbParser.load(dbLines, resourceLines).parseAll();


        //THEN
        assertThat(dbDto).isNotNull();
        assertThat(dbDto.getRef()).isEqualTo("2442784645");
//        assertThat(dbDto.getName()).isEqualTo("Achievements");

        DbDataDto data = dbDto.getData();
        assertThat(data).isNotNull();
        assertThat(data.getEntries()).hasSize(74);

        DbStructureDto structure = dbDto.getStructure();
        assertThat(structure).isNotNull();
        assertThat(structure.getFields()).hasSize(9);

        DbResourceDto resources = dbDto.getResources();
        assertThat(resources).isNotNull();
        assertThat(resources.getVersion()).isEqualTo("1,2");
        assertThat(resources.getCategoryCount()).isEqualTo(6);
        assertThat(resources.getEntries()).hasSize(237);
        assertThat(resources.getEntries().get(0).getLocalizedValues()).hasSize(1);
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
}