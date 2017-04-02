package fr.tduf.libunlimited.low.files.db.mapper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;


class DbDataMapperTest {

    private static final Class<DbDataMapperTest> thisClass = DbDataMapperTest.class;

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @BeforeEach
    void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void serialize_shouldWriteProperJson() throws IOException, JSONException, URISyntaxException {
        //GIVEN
        ContentItemDto item11 = ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue("111111")
                .build();
        ContentItemDto item12 = ContentItemDto.builder()
                .ofFieldRank(2)
                .withRawValue("111")
                .build();
        ContentItemDto item13 = ContentItemDto.builder()
                .ofFieldRank(3)
                .withRawValue("1")
                .build();
        ContentItemDto item21 = ContentItemDto.builder()
                .ofFieldRank(1)
                .withRawValue("222222")
                .build();
        ContentItemDto item22 = ContentItemDto.builder()
                .ofFieldRank(2)
                .withRawValue("222")
                .build();
        ContentItemDto item23 = ContentItemDto.builder()
                .ofFieldRank(3)
                .withRawValue("0")
                .build();
        ContentEntryDto entry1 = ContentEntryDto.builder()
                .addItem(item11)
                .addItem(item12)
                .addItem(item13)
                .build();
        ContentEntryDto entry2 = ContentEntryDto.builder()
                .addItem(item21)
                .addItem(item22)
                .addItem(item23)
                .build();
        DbDataDto dbDataDto = DbDataDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .addEntry(entry1)
                .addEntry(entry2)
                .build();

        //WHEN
        String jsonResult = objectWriter.writeValueAsString(dbDataDto);
        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);

        //THEN
        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/mapper/topicObject.data.json");
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }
}