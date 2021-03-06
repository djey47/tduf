package fr.tduf.libunlimited.low.files.db.rw;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.common.helper.DbHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.content.SwitchValueDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceItemDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.util.*;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.MISSING_LOCALES;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.REFERENCE;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.SOURCE_TOPIC;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class DatabaseParserTest {

    private static final Class<DatabaseParserTest> thisClass = DatabaseParserTest.class;
    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    @BeforeAll
    static void setUp() {
        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void load_whenProvidedContents_shouldReturnParserInstanceWithoutErrors() {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<fr.tduf.libunlimited.common.game.domain.Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(FRANCE, createValidResourcesWithTwoItemsForLocale(FRANCE));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);

        //THEN
        assertThat(databaseParser).isNotNull();
        assertThat(databaseParser.getContentLineCount()).isEqualTo(15);
        assertThat(databaseParser.getResourceCount()).isEqualTo(1);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();
    }

    @Test
    void parseAll_whenProvidedContents_andIntegrityErrorsOnItemAndFieldCount_shouldReturnErrors() {
        //GIVEN : item count != actual item count
        // field count != actual field count
        List<String> dbLines = asList(
                "// TDU_Achievements.db",
                "// Fields: 1",
                "{TDU_Achievements} 2442784645",
                "{Achievement_Event_} u",
                "{Misc} u",
                "// items: 10",
                "55736935;5;",
                "\0");
        Map<Locale, List<String>> resourceLines = createValidResourcesForAllLocales();

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).hasSize(2);
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsExactly("STRUCTURE_FIELDS_COUNT_MISMATCH", "CONTENT_ITEMS_COUNT_MISMATCH");
        assertThat(databaseParser.getIntegrityErrors().get(0).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
        assertThat(databaseParser.getIntegrityErrors().get(1).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
    }

    @Test
    void parseAll_whenProvidedContents_andIntegrityErrorOnResourceCount_shouldReturnError() {
        //GIVEN : fr resource count  != it resource
        List<String> dbLines = createValidContentsWithOneItem();
        Map<Locale, List<String>> resourceLines = new HashMap<>();

        Locale.valuesAsStream()
                .filter(locale -> locale != ITALY)
                .forEach(locale -> resourceLines.put(locale, createValidResourcesWithTwoItemsForLocale(locale)));
        resourceLines.put(ITALY,
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
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsOnly("RESOURCE_REFERENCE_NOT_FOUND");
        Map<IntegrityError.ErrorInfoEnum, Object> actualInfo = databaseParser.getIntegrityErrors().get(0).getInformation();
        assertThat(actualInfo.get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
        assertThat(actualInfo.get(MISSING_LOCALES)).isEqualTo(new HashSet<>(singletonList(ITALY)));
        assertThat(actualInfo.get(REFERENCE)).isEqualTo("53410835");
    }

    @Test
    void parseAll_whenProvidedContents_andDuplicateResourceReference_shouldNotReturnError() {
        //GIVEN : duplicate ref
        List<String> dbLines = createValidContentsWithOneItem();
        Map<Locale, List<String>> resourceLines = new HashMap<>();
        Locale.valuesAsStream()
                .forEach(locale -> resourceLines.put(locale, createInvalidResourcesWithReferenceDuplicate(locale)));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();
    }

    @Test
    void parseAll_whenProvidedContents_andIntegrityErrorOnFieldCount_shouldReturnError() {
        //GIVEN
        List<String> dbLines = createInvalidContentsWithOneItemAndUnconsistentFieldCount();
        Map<Locale, List<String>> resourceLines = createValidResourcesForAllLocales();

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();
        assertThat(databaseParser.getIntegrityErrors()).hasSize(1);
        assertThat(databaseParser.getIntegrityErrors()).extracting("error").containsExactly("CONTENTS_FIELDS_COUNT_MISMATCH");
        assertThat(databaseParser.getIntegrityErrors().get(0).getInformation().get(SOURCE_TOPIC)).isEqualTo(ACHIEVEMENTS);
    }

    @Test
    void parseAll_whenProvidedContents_andRemoteReference_shouldReadAccordingly() {
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
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(FRANCE, asList(
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
    void parseAll_whenProvidedContents_andFloatNegativeValue_shouldReadAccordingly() {
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
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(FRANCE, asList(
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
        ContentItemDto item = actualDb.getData().getEntries().get(0).getItems().get(0);
        assertThat(item.getFieldRank()).isEqualTo(1);
        assertThat(item.getRawValue()).isEqualTo("-33,33");
    }

    @Test
    void parseAll_whenProvidedContents_andEmptyValue_shouldReadAccordingly() {
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
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(
                FRANCE,
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
        ContentItemDto item = actualDb.getData().getEntries().get(0).getItems().get(1);
        assertThat(item.getFieldRank()).isEqualTo(2);
        assertThat(item.getRawValue()).isEqualTo("");
    }

    @Test
    void parseAll_whenProvidedContentsAsGlobalResources_shouldReturnProperDto() {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<Locale, List<String>> resourceLines = createValidResourcesForAllLocales();

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        assertThat(actualDb).isNotNull();

        DbResourceDto actualDbResource = actualDb.getResource();
        assertThat(actualDbResource.getCategoryCount()).isEqualTo(6);
        assertThat(actualDbResource.getVersion()).isEqualTo("1,2");

        final Collection<ResourceEntryDto> actualEntries = actualDbResource.getEntries();
        assertThat(actualEntries).hasSize(2);
        assertThat(actualEntries).extracting("reference").containsOnly("53410835", "70410835");
        Set<ResourceItemDto> globalItem1 = new HashSet<>(singletonList(
                ResourceItemDto.builder().withGlobalValue("??").build()));
        Set<ResourceItemDto> globalItem2 = new HashSet<>(singletonList(
                ResourceItemDto.builder().withGlobalValue("Bravo ! Vous recevez §NB_PTS§ points.").build()));
        assertThat(actualEntries).extracting("items").contains(globalItem1, globalItem2);
    }

    @Test
    void parseAll_whenProvidedContents_andBitfield_shouldReturnProperDto() {
        //GIVEN
        List<String> dbLines = createValidContentsBitfieldOnlyWithOneItem();
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(
                FRANCE,
                createValidResourcesWithTwoItemsForLocale(FRANCE));

        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();

        //THEN
        assertThat(actualDb).isNotNull();

        DbDataDto actualContents = actualDb.getData();
        List<ContentEntryDto> actualEntries = actualContents.getEntries();
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItems()).hasSize(1);

        List<SwitchValueDto> actualSwitchValues = actualEntries.get(0).getItems().get(0).getSwitchValues();
        assertThat(actualSwitchValues).isNotNull();
        assertThat(actualSwitchValues).extracting("index").containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(actualSwitchValues).extracting("name").containsOnly("Vehicle slot enabled", "?", "?", "?", "?", "Add-on key required", "Car Paint Luxe enabled");
        assertThat(actualSwitchValues).extracting("enabled").containsExactly(true, true, true, true, false, true, true);
    }

    @Test
    void parseAll_whenProvidedContents_andMissingLocale_shouldReturnProperDto_withValidLocales() {
        //GIVEN
        List<String> dbLines = createValidContentsWithOneItem();
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(
                FRANCE,
                createValidResourcesWithTwoItemsForLocale(FRANCE));
        resourceLines.put(
                Locale.GERMANY,
                new ArrayList<>());
        resourceLines.put(ITALY,
                createValidResourcesWithTwoItemsForLocale(ITALY)
        );


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto actualDb = databaseParser.parseAll();


        //THEN
        assertThat(databaseParser.getIntegrityErrors()).hasSize(2);

        DbResourceDto actualResource = actualDb.getResource();
        assertThat(actualResource.getEntries()).hasSize(2);
        final Optional<ResourceEntryDto> entry1 = actualResource.getEntryByReference("53410835");
        assertThat(entry1).isPresent();
        assertThat(entry1.get().getItemCount()).isEqualTo(2);
        assertThat(entry1.get().getItemForLocale(FRANCE)).isPresent();
        assertThat(entry1.get().getItemForLocale(ITALY)).isPresent();
        final Optional<ResourceEntryDto> entry2 = actualResource.getEntryByReference("70410835");
        assertThat(entry2).isPresent();
        assertThat(entry2.get().getItemCount()).isEqualTo(2);
        assertThat(entry2.get().getItemForLocale(FRANCE)).isPresent();
        assertThat(entry2.get().getItemForLocale(ITALY)).isPresent();
    }

    @Test
    void parseAll_whenRealFiles_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = DbHelper.readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        Map<Locale, List<String>> resourceLines = createResourceLinesForAllLocalesFromResourceFiles();


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto db = databaseParser.parseAll();

        // JSON DISPLAY
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);


        //THEN
        assertThat(databaseParser.getContentLineCount()).isEqualTo(90);
        assertThat(databaseParser.getResourceCount()).isEqualTo(8);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/parsing/TDU_Achievements.json", FilesHelper.CHARSET_UNICODE_8);
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
    }

    @Test
    void parseAll_whenRealFiles_andResourceMetaMismatch_shouldReturnProperDto_andParserWithoutError() throws Exception {
        //GIVEN
        List<String> dbLines = DbHelper.readContentsFromSample("/db/TDU_Achievements.db", "UTF-8");
        Map<Locale, List<String>> resourceLines = createResourceLinesForAllLocalesFromResourceFiles();


        //WHEN
        DatabaseParser databaseParser = DatabaseParser.load(dbLines, resourceLines);
        DbDto db = databaseParser.parseAll();

        // JSON DISPLAY
        assertThat(objectWriter.canSerialize(db.getClass())).isTrue();

        String jsonResult = objectWriter.writeValueAsString(db);

        Log.debug(thisClass.getSimpleName(), "Actual JSON:" + jsonResult);


        //THEN
        assertThat(databaseParser.getContentLineCount()).isEqualTo(90);
        assertThat(databaseParser.getResourceCount()).isEqualTo(8);
        assertThat(databaseParser.getIntegrityErrors()).isEmpty();

        String expectedJson = FilesHelper.readTextFromResourceFile("/db/json/special/TDU_Achievements.json", FilesHelper.CHARSET_UNICODE_8);
        assertEquals(expectedJson, jsonResult, JSONCompareMode.STRICT);
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

    private Map<Locale, List<String>> createValidResourcesForAllLocales() {
        Map<Locale, List<String>> resourceLines = new HashMap<>();
        resourceLines.put(FRANCE, createValidResourcesWithTwoItemsForLocale(FRANCE));
        resourceLines.put(ITALY, createValidResourcesWithTwoItemsForLocale(ITALY));
        resourceLines.put(GERMANY, createValidResourcesWithTwoItemsForLocale(GERMANY));
        resourceLines.put(SPAIN, createValidResourcesWithTwoItemsForLocale(SPAIN));
        resourceLines.put(CHINA, createValidResourcesWithTwoItemsForLocale(CHINA));
        resourceLines.put(KOREA, createValidResourcesWithTwoItemsForLocale(KOREA));
        resourceLines.put(JAPAN, createValidResourcesWithTwoItemsForLocale(JAPAN));
        resourceLines.put(UNITED_STATES, createValidResourcesWithTwoItemsForLocale(UNITED_STATES));
        return resourceLines;
    }

    private List<String> createValidResourcesWithTwoItemsForLocale(Locale locale) {
        return asList(
                "// TDU_Achievements." + locale.getCode(),
                "// version: 1,2",
                "// categories: 6",
                "// Explanation",
                "{??} 53410835",
                "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
        );
    }

    private List<String> createInvalidResourcesWithReferenceDuplicate(Locale locale) {
        return asList(
                "// TDU_Achievements." + locale.getCode(),
                "// version: 1,2",
                "// categories: 6",
                "// Explanation",
                "{??} 53410835",
                "{Bravo ! Vous recevez §NB_PTS§ points.} 70410835",
                "{(clone)Bravo ! Vous recevez §NB_PTS§ points.} 70410835"
        );
    }

    private Map<Locale, List<String>> createResourceLinesForLocale(Locale locale, List<String> resourceLines) {
        Map<Locale, List<String>> map = new HashMap<>();
        map.put(locale, resourceLines );
        return map;
    }

    private Map<Locale, List<String>> createResourceLinesForAllLocalesFromResourceFiles() throws IOException {
        Map<Locale, List<String>> resourceLines = createResourceLinesForLocale(
                FRANCE,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.fr").get(0));
        resourceLines.put(
                ITALY,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                JAPAN,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                KOREA,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                CHINA,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                SPAIN,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                GERMANY,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        resourceLines.put(
                UNITED_STATES,
                DbHelper.readResourcesFromSamples("/db/res/TDU_Achievements.it").get(0));
        return resourceLines;
    }
}
