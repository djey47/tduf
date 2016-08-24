package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceItemDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.game.domain.Locale.ITALY;
import static java.util.Arrays.asList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DbResourceMapperTest {

    private static final Class<DbResourceMapperTest> thisClass = DbResourceMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @Before
    public void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    public void serialize_shouldWriteProperJson() throws IOException, JSONException, URISyntaxException {
        //GIVEN
        ResourceItemDto item1 = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VAL1_FR")
                .build();
        ResourceItemDto item2 = ResourceItemDto.builder()
                .withLocale(ITALY)
                .withValue("VAL1_IT")
                .build();
        ResourceEntryDto entry1 = ResourceEntryDto.builder()
                .forReference("53410835")
                .withItems(asList(item1, item2))
                .build();
        ResourceItemDto item3 = ResourceItemDto.builder()
                .withLocale(FRANCE)
                .withValue("VAL2_FR")
                .build();
        ResourceItemDto item4 = ResourceItemDto.builder()
                .withLocale(ITALY)
                .withValue("VAL2_IT")
                .build();
        ResourceEntryDto entry2 = ResourceEntryDto.builder()
                .forReference("54410835")
                .withItems(asList(item3, item4))
                .build();
        DbResourceDto dbResourceDto = DbResourceDto.builder()
                .withCategoryCount(1)
                .atVersion("1,0")
                .containingEntries(asList(entry1, entry2))
                .build();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/mapper/topicObject.resources.json");


        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbResourceDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);


        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}
