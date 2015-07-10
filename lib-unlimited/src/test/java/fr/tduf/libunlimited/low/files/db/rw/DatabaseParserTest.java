package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.common.helper.DbHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.SOURCE_TOPIC;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class DatabaseParserTest {

    @Test
    public void load_whenProvidedContents_shouldReturnParserInstanceWithoutErrors() throws Exception {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<DbResourceDto.Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(DbResourceDto.Locale.FRANCE, createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);

        //THEN
        assertThat(databaseParser).isNotNull();
        assertThat(databaseParser.getContentLineCount()).isEqualTo(15);
        assertThat(databaseParser.getResourceCount()).isEqualTo(1);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();
    }

    @Test
    public void parseAll_whenProvidedContents_andIntegrityErrorsOnItemAndFieldCount_shouldReturnErrors() throws Exception {
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
        Map<DbResourceDto.Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(DbResourceDto.Locale.FRANCE, createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).hasSize(2);
        /** {@link fr.tduf.libunlimited.low.files.db.domain.IntegrityError#getError()} */
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsExactly("STRUCTURE_FIELDS_COUNT_MISMATCH", "CONTENT_ITEMS_COUNT_MISMATCH");
        assertThat(databaseParser.getIntegrityErrors().get(0).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
        assertThat(databaseParser.getIntegrityErrors().get(1).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
    }

    @Test
    public void parseAll_whenProvidedContents_andIntegrityErrorOnResourceCount_shouldReturnError() throws Exception {
        //GIVEN : fr resource count  != it resource
        List<String> dbLines = createValidContentsWithOneItem();
        Map<DbResourceDto.Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(DbResourceDto.Locale.FRANCE, createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));
        resourceLines.put(DbResourceDto.Locale.ITALY,
                asList(
                        "// TDU_Achievements.it",
                        "// version: 1,2",
                        "// categories: 6",
                        "// Explanation",
                        "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
                )
        );

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).hasSize(1);
        /** {@link fr.tduf.libunlimited.low.files.db.domain.IntegrityError#getError()} */
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsExactly("RESOURCE_ITEMS_COUNT_MISMATCH");
        assertThat(databaseParser.getIntegrityErrors().get(0).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
    }

    @Test
    public void parseAll_whenProvidedContents_andIntegrityErrorOnFieldCount_shouldReturnError() throws Exception {
        //GIVEN
        List<String> dbLines = createInvalidContentsWithOneItemAndUnconsistentFieldCount();
        Map<DbResourceDto.Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(DbResourceDto.Locale.FRANCE, createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));
        resourceLines.put(DbResourceDto.Locale.ITALY, createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.ITALY));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).hasSize(1);
        /** {@link fr.tduf.libunlimited.low.files.db.domain.IntegrityError#getError()} */
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsExactly("CONTENTS_FIELDS_COUNT_MISMATCH");
        assertThat(databaseParser.getIntegrityErrors().get(0).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
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
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(DbResourceDto.Locale.FRANCE, asList(
                "// TDU_CarPhysicsData.fr",
                "// version: 1,2",
                "// categories: 6",
                "// Explanation",
                "{??} 53410835,"
        ));
        DbStructureDto.Field expectedField =  DbStructureDto.Field.builder()
                .forName("Car_Brand")
                .fromType(DbStructureDto.FieldType.REFERENCE)
                .toTargetReference("1209165514")
                .ofRank(2)
                .build();

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb.getStructure().getFields()).hasSize(3);
        DbStructureDto.Field secondField = actualDb.getStructure().getFields().get(1);
        assertThat(secondField).isEqualTo(expectedField);
    }

    @Test
    public void parseAll_whenProvidedContents_andFloatNegativeValue_shouldReadAccordingly() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_CarPhysicsData.db",
                "// Version: 1,2,",
                "// Categories: 20",
                "// Fields: 1",
                "{TDU_CarPhysicsData} 1975083164",
                "{CX} f",
                "// items: 1",
                "-33,33;",
                "\0");
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(DbResourceDto.Locale.FRANCE, asList(
                "// TDU_CarPhysicsData.fr",
                "// version: 1,2",
                "// categories: 6",
                "// Explanation",
                "{??} 53410835,"
        ));


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();


        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb.getData().getEntries()).hasSize(1);
        assertThat(actualDb.getData().getEntries().get(0).getItems()).hasSize(1);
        DbDataDto.Item item = actualDb.getData().getEntries().get(0).getItems().get(0);
        assertThat(item.getName()).isEqualTo("CX");
        assertThat(item.getRawValue()).isEqualTo("-33,33");
    }

    @Test
    public void parseAll_whenProvidedContents_andEmptyValue_shouldReadAccordingly() throws Exception {
        //GIVEN
        List<String> dbLines = asList(
                "// TDU_CarPhysicsData.db",
                "// Version: 1,2,",
                "// Categories: 20",
                "// Fields: 3",
                "{TDU_CarPhysicsData} 1975083164",
                "{Pad1} i",
                "{Pad2} i",
                "{Pad3} i",
                "// items: 1",
                "1;;3;",
                "\0");
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.FRANCE,
                asList(
                        "// TDU_CarPhysicsData.fr",
                        "// version: 1,2",
                        "// categories: 6",
                        "// Explanation",
                        "{??} 53410835,"
                )
        );


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();


        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb.getData().getEntries()).hasSize(1);
        assertThat(actualDb.getData().getEntries().get(0).getItems()).hasSize(3);
        DbDataDto.Item item = actualDb.getData().getEntries().get(0).getItems().get(1);
        assertThat(item.getName()).isEqualTo("Pad2");
        assertThat(item.getRawValue()).isEqualTo("");
    }

    @Test
    public void parseAll_whenProvidedContents_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.FRANCE,
                createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));
        resourceLines.put(
                DbResourceDto.Locale.ITALY,
                createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.ITALY));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb).isNotNull();

        List<DbResourceDto> actualDbResources = actualDb.getResources();
        assertThat(actualDbResources).hasSize(2);
        assertThat(actualDbResources.get(0).getEntries()).hasSize(2);
        assertThat(actualDbResources.get(0).getEntries()).hasSameSizeAs(actualDbResources.get(1).getEntries());
    }

    @Test
    public void parseAll_whenProvidedContents_andBitfield_shouldReturnProperDto() throws Exception {
        //GIVEN
        List<String> dbLines = createValidContentsBitfieldOnlyWithOneItem();
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.FRANCE,
                createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();

        DbDataDto actualContents = actualDb.getData();
        List<DbDataDto.Entry> actualEntries = actualContents.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItems()).hasSize(1);

        List<DbDataDto.SwitchValue> actualSwitchValues = actualEntries.get(0).getItems().get(0).getSwitchValues();
        assertThat(actualSwitchValues).isNotNull();
        assertThat(actualSwitchValues).extracting("index").containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(actualSwitchValues).extracting("name").containsOnly("?", "?", "?", "?", "?", "Add-on key required", "Car Paint Luxe enabled");
        assertThat(actualSwitchValues).extracting("enabled").containsExactly(true, true, true, true, false, true, true);
    }

    @Test
    public void parseAll_whenProvidedContents_andMissingLocale_shouldReturnProperDto_withValidLocales() throws Exception {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.FRANCE,
                createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.FRANCE));
        resourceLines.put(
                DbResourceDto.Locale.GERMANY,
                new ArrayList<>());
        resourceLines.put(DbResourceDto.Locale.ITALY,
                createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale.ITALY)
        );


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();


        //THEN
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        List<DbResourceDto> actualDbResources = actualDb.getResources();
        assertThat(actualDbResources).hasSize(2);
        assertThat(actualDbResources.get(0).getLocale()).isEqualTo(DbResourceDto.Locale.FRANCE);
        assertThat(actualDbResources.get(1).getLocale()).isEqualTo(DbResourceDto.Locale.ITALY);
    }

    @Test
    public void parseAll_whenRealFiles_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = DbHelper.readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.FRANCE,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.fr").get(0));
        resourceLines.put(
                DbResourceDto.Locale.ITALY,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto db = databaseParser.parseAll();

        // JSON DISPLAY
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        // Uncomment below to fetch and actualize JSON result
//        System.out.println("JSON DISPLAY");
//        System.out.println(jsonResult);
        //


        //THEN
        assertThat(databaseParser.getContentLineCount()).isEqualTo(90);
        assertThat(databaseParser.getResourceCount()).isEqualTo(2);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/TDU_Achievements.json", FilesHelper.CHARSET_UNICODE_8);
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }

    @Test
    public void parseAll_whenRealFiles_andResourceMetaMismatch_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = DbHelper.readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        Map<DbResourceDto.Locale, List<String>> resourceLines = createResourceLinesForLocale(
                DbResourceDto.Locale.ITALY,
                DbHelper.readResourcesFromSamples("/db/res/special/TDU_Achievements.it").get(0));


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto db = databaseParser.parseAll();

        // JSON DISPLAY
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        // Uncomment below to fetch and actualize JSON result
        System.out.println("JSON DISPLAY");
        System.out.println(jsonResult);
        //


        //THEN
        assertThat(databaseParser.getContentLineCount()).isEqualTo(90);
        assertThat(databaseParser.getResourceCount()).isEqualTo(1);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/special/TDU_Achievements.json", FilesHelper.CHARSET_UNICODE_8);
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }

    @Test(expected = NullPointerException.class)
    public void prepareSwitchValues_whenNullValue_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseParser.prepareSwitchValues(null, DbDto.Topic.CAR_PHYSICS_DATA);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void prepareSwitchValues_whenNullTopic_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseParser.prepareSwitchValues("111", null);

        // THEN: NPE
    }

    @Test
    public void prepareSwitchValues_shouldReturnCorrectValues() {
        // GIVEN-WHEN
        List<DbDataDto.SwitchValue> actualValues = DatabaseParser.prepareSwitchValues("111", CAR_PHYSICS_DATA);

        // THEN
        assertThat(actualValues).hasSize(7);
        assertThat(actualValues).extracting("index").containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(actualValues).extracting("name").containsExactly("?", "?", "?", "?", "?", "Add-on key required", "Car Paint Luxe enabled");
        assertThat(actualValues).extracting("enabled").containsExactly(true, true, true, true, false, true, true);
    }

    @Test
    public void prepareSwitchValues_whenReferenceNotFound_shouldReturnEmptyList() {
        // GIVEN-WHEN
        List<DbDataDto.SwitchValue> actualValues = DatabaseParser.prepareSwitchValues("111", CAR_RIMS);

        // THEN
        assertThat(actualValues).isEmpty();
    }

    private List<String> createValidContentsWithOneItem() {
        return asList(
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
                "\0\0\0\0");
    }

    private List<String> createValidContentsBitfieldOnlyWithOneItem() {
        return asList(
                "// TDU_CarPhysicsData.db",
                "// Fields: 1",
                "{TDU_CarPhysicsData} 2442784645",
                "{Bitfield_} b",
                "// items: 1",
                "111;",
                "\0\0\0\0");
    }

    private List<String> createInvalidContentsWithOneItemAndUnconsistentFieldCount() {
        // 9 fields expected, 8 actual
        return asList(
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
                "55736935;5;20;54400734;54359455;54410835;561129540;5337472;",
                "\0");
    }

    private List<String> createValidResourcesWithTwoItemsForLocale(DbResourceDto.Locale locale) {
        return asList(
                "// TDU_Achievements." + locale.getCode(),
                "// version: 1,2",
                "// categories: 6",
                "// Explanation",
                "{??} 53410835",
                "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
        );
    }

    private Map<DbResourceDto.Locale, List<String>> createResourceLinesForLocale(DbResourceDto.Locale locale, List<String> resourceLines) {
        Map<DbResourceDto.Locale, List<String>> map = new HashMap<>();
        map.put(locale, resourceLines );
        return map;
    }
}