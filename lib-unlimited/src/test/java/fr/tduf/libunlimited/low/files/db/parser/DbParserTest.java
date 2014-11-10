package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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

        //WHEN
        DbParser dbParser = DbParser.load(dbLines);

        //THEN
        assertThat(dbParser).isNotNull();
        assertThat(dbParser.getLineCount()).isEqualTo(4);
    }

    @Test
    public void parseContents_whenAny_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = readLinesFromSample();

        //WHEN
        DbDto dbContentsDto = DbParser.load(dbLines).parseContents();

        //THEN
        assertThat(dbContentsDto).isNotNull();
        assertThat(dbContentsDto.getEntries().size()).isEqualTo(74);
        //TODO check entry
    }

    @Test
    public void parseStructure_whenAny_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = readLinesFromSample();

        //WHEN
        DbStructureDto dbStructureDto = DbParser.load(dbLines).parseStructure();

        //THEN
        assertThat(dbStructureDto).isNotNull();
        assertThat(dbStructureDto.getRef()).isEqualTo("2442784645");
        assertThat(dbStructureDto.getItems().size()).isEqualTo(9);
        //TODO check item
    }

    private List<String> readLinesFromSample() throws IOException {
        List<String> lines = newArrayList();

        InputStream resourceAsStream = getClass().getResourceAsStream("/db/TDU_achievements.db");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));

        String line;
        while((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }
}