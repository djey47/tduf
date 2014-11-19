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
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.ITALY;
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
    }

    @Test
    public void parseAll_whenRealFiles_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        List<List<String>> resourceLines = readResourcesFromSamples("/db/res/TDU_Achievements.fr", "/db/res/TDU_Achievements.it");


        //WHEN
        DbParser dbParser = DbParser.load(dbLines, resourceLines);
        DbDto dbDto = dbParser.parseAll();


        //THEN
        assertThat(dbParser.getContentLineCount()).isEqualTo(90);
        assertThat(dbParser.getResourceCount()).isEqualTo(2);

        assertThat(dbDto).isNotNull();
        assertThat(dbDto.getRef()).isEqualTo("2442784645");
//        assertThat(dbDto.getTopic()).isEqualTo(ACHIEVEMENTS);

        DbDataDto data = dbDto.getData();
        assertThat(data).isNotNull();
        assertThat(data.getEntries()).hasSize(74);

        DbStructureDto structure = dbDto.getStructure();
        assertThat(structure).isNotNull();
        assertThat(structure.getFields()).hasSize(9);

        List<DbResourceDto> resources = dbDto.getResources();
        assertThat(resources).isNotNull();
        assertThat(resources).extracting("version").containsExactly("1,2", "1,2");
        assertThat(resources).extracting("categoryCount").containsExactly(6, 6);
        assertThat(resources).extracting("locale").containsExactly(FRANCE, ITALY);
        assertThat(resources.get(0).getEntries()).hasSize(237);
        assertThat(resources.get(1).getEntries()).hasSize(236); //FIXME Should be 237 as well.... why ?
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