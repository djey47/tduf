package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceItemDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static java.util.Collections.singletonList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class DbMapperTest {

    private static final Class<DbMapperTest> thisClass = DbMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void serialize_shouldWriteProperJson() throws IOException, JSONException, URISyntaxException {
        //GIVEN
        DbStructureDto dbStructureDto = DbStructureDto.builder()
                .forReference("2442784645")
                .forTopic(ACHIEVEMENTS)
                .atVersion("1,0")
                .withCategoryCount(5)
                .build();
        DbDto dbTopicDto = DbDto.builder()
                .withStructure(dbStructureDto)
                .withData(DbDataDto.builder().build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1,2")
                        .withCategoryCount(6)
                        .containingEntries(singletonList(ResourceEntryDto.builder()
                                .forReference("REF")
                                .withItems(singletonList(ResourceItemDto.builder()
                                        .withLocale(Locale.FRANCE)
                                        .withValue("VAL")
                                        .build()))
                                .build()))
                        .build())
                .build();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/mapper/topicObject.json");


        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbTopicDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);


        //THEN
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}
