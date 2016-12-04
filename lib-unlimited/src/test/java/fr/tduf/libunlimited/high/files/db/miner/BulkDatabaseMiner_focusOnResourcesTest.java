package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.PNJ;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BulkDatabaseMiner_focusOnResourcesTest {

    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final Locale LOCALE = UNITED_STATES;
    private static final String ENTRY_REF = "33333333";
    private static final String RESOURCE_REF = "00000000";
    private static final String RESOURCE_VALUE = "VALUE";
    private static final String TOPIC_REF = "11111111";
    private static final String TOPIC_REF2 = "22222222";

    @Test
    void getResourceEnhancedFromTopic_whenTopicExists_shouldReturnIt() {
        //GIVEN
        DbDto topicObject1 = createDefaultTopicObject(TOPIC);
        DbDto topicObject2 = createDefaultTopicObject(PNJ);
        List<DbDto> topicObjects = asList(topicObject1, topicObject2);

        //WHEN
        final Optional<DbResourceDto> potentialResource = BulkDatabaseMiner.load(topicObjects).getResourcesFromTopic(TOPIC);

        //THEN
        assertThat(potentialResource)
                .isPresent()
                .contains(topicObject1.getResource());
    }

    @Test
    void getResourceEnhancedFromTopic_whenTopicDoesNotExist_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<DbResourceDto> potentialResource = BulkDatabaseMiner.load(topicObjects).getResourcesFromTopic(PNJ);

        //THEN
        assertThat(potentialResource).isEmpty();
    }

    @Test
    void getResourceEntryFromTopicAndReference_whenTopicDoesNotExist_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<ResourceEntryDto> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(PNJ, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry).isEmpty();
    }

    @Test
    void getResourceEntryFromTopicAndReference_whenTopicExists_butEntryDoesNot_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<ResourceEntryDto> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry).isEmpty();
    }

    @Test
    void getResourceEntryFromTopicAndReference_whenEntryExists_shouldReturnIt() {
        //GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        final ResourceEntryDto entry = topicObject.getResource().addEntryByReference(RESOURCE_REF);
        List<DbDto> topicObjects = singletonList(topicObject);

        //WHEN
        final Optional<ResourceEntryDto> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry)
                .isPresent()
                .contains(entry);
    }

    @Test
    void getLocalizedResourceValueFromTopicAndReference_whenTopicDoesNotExist_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromTopicAndReference(RESOURCE_REF, PNJ, LOCALE);

        //THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromTopicAndReference_whenTopicExists_butEntryDoesNot_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromTopicAndReference(RESOURCE_REF, TOPIC, LOCALE);

        //THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromTopicAndReference_whenTopicExists_entryExists_butValueDoesNot_shouldReturnEmpty() {
        //GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        topicObject.getResource().addEntryByReference(RESOURCE_REF);
        List<DbDto> topicObjects = singletonList(topicObject);

        //WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromTopicAndReference(RESOURCE_REF, TOPIC, LOCALE);

        //THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromTopicAndReference_whenTopicExists_entryExists_andValueExistsForLocale_shouldReturnIt() {
        //GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        topicObject.getResource()
                .addEntryByReference(RESOURCE_REF)
                .setDefaultValue("")
                .setValueForLocale(RESOURCE_VALUE, LOCALE);
        List<DbDto> topicObjects = singletonList(topicObject);

        //WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromTopicAndReference(RESOURCE_REF, TOPIC, LOCALE);

        //THEN
        assertThat(potentialValue)
                .isPresent()
                .contains(RESOURCE_VALUE);
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryDoesNotExist_shouldReturnEmpty() {
        // GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(1000, 1, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_butFieldRankDoesNot_shouldThrowException() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 0, TOPIC, LOCALE));
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExists_butNotAResourceField_shouldReturnEmpty() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 1, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsResourceField_butResourceEntryDoesNotExist_shouldReturnEmpty() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 2, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsResourceField_andResourceEntryExists_shouldReturnValue() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        topicObject.getResource()
                .addEntryByReference(RESOURCE_REF)
                .setDefaultValue("")
                .setValueForLocale(RESOURCE_VALUE, LOCALE);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 3, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue)
                .isPresent()
                .contains(RESOURCE_VALUE);
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsRemoteResourceField_butRemoteTopicDoesNotExist_shouldThrowException() {
        // GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        // WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 4, TOPIC, LOCALE));
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsRemoteResourceField_butResourceEntryDoesNotExist_shouldReturnEmpty() {
        // GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 5, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsRemoteResourceField_andResourceEntryExists_shouldReturnValue() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        topicObject.getResource()
                .addEntryByReference(RESOURCE_REF)
                .setDefaultValue("")
                .setValueForLocale(RESOURCE_VALUE, LOCALE);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 5, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue)
                .isPresent()
                .contains(RESOURCE_VALUE);
    }

    @Test
    void getLocalizedResourceValueFromContentEntry_whenContentEntryExists_fieldRankExistsAsRemoteEntry_shouldReturnEmpty() {
        // GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        List<DbDto> topicObjects = singletonList(topicObject);

        // WHEN
        final Optional<String> potentialValue = BulkDatabaseMiner.load(topicObjects).getLocalizedResourceValueFromContentEntry(0, 6, TOPIC, LOCALE);

        // THEN
        assertThat(potentialValue).isEmpty();
    }

    @Test
    void getAllResourceValuesForReference_whenEntryDoesNotExist_shouldReturnEmptySet() {
        //GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();

        //WHEN
        final Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference(RESOURCE_REF, resourceObject);

        //THEN
        assertThat(actualValues).isEmpty();
    }

    @Test
    void getAllResourceValuesForReference_whenEntryExists_withSameValueForItems_shouldReturnValueOnce() {
        //GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();
        resourceObject.addEntryByReference(RESOURCE_REF).setDefaultValue(RESOURCE_VALUE);

        //WHEN
        final Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference(RESOURCE_REF, resourceObject);

        //THEN
        assertThat(actualValues)
                .hasSize(1)
                .containsOnly(RESOURCE_VALUE);
    }

    @Test
    void getAllResourceValuesForReference_whenEntryExists_withGlobalizedValue_shouldReturnIt() {
        //GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();
        resourceObject.addDefaultEntryByReference(RESOURCE_REF, RESOURCE_VALUE);

        //WHEN
        final Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference(RESOURCE_REF, resourceObject);

        //THEN
        assertThat(actualValues)
                .hasSize(1)
                .containsOnly(RESOURCE_VALUE);
    }

    @Test
    void getAllResourceValuesForReference_whenEntryExists_withDifferentValuesForItems_shouldReturnValues() {
        //GIVEN
        DbResourceDto resourceObject = createDefaultResourceObject();
        ResourceEntryDto resourceEntryDto = resourceObject.addEntryByReference(RESOURCE_REF);
        setAllResourceValues(resourceEntryDto, RESOURCE_VALUE);
        resourceEntryDto
                .setValueForLocale("VALUE2", FRANCE)
                .setValueForLocale("VALUE3", UNITED_STATES);

        //WHEN
        final Set<String> actualValues = BulkDatabaseMiner.getAllResourceValuesForReference(RESOURCE_REF, resourceObject);

        //THEN
        assertThat(actualValues)
                .hasSize(3)
                .containsOnly(RESOURCE_VALUE, "VALUE2", "VALUE3");
    }

    private static DbDto createDefaultTopicObject(DbDto.Topic topic) {
        DbResourceDto resourceObject = createDefaultResourceObject();
        DbStructureDto structureObject = DbStructureDto.builder()
                .forTopic(topic)
                .forReference(TOPIC_REF)
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(1)
                        .fromType(BITFIELD)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(2)
                        .fromType(RESOURCE_CURRENT_LOCALIZED)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(3)
                        .fromType(RESOURCE_CURRENT_GLOBALIZED)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(4)
                        .fromType(RESOURCE_REMOTE)
                        .toTargetReference(TOPIC_REF2)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(5)
                        .fromType(RESOURCE_REMOTE)
                        .toTargetReference(TOPIC_REF)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .ofRank(6)
                        .fromType(DbStructureDto.FieldType.REFERENCE)
                        .toTargetReference(TOPIC_REF)
                        .build())
                .build();
        DbDataDto dataObject = DbDataDto.builder().build();
        dataObject.addEntryWithItems(asList(
                ContentItemDto.builder()
                        .ofFieldRank(1)
                        .withRawValue("108")
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(2)
                        .withRawValue(RESOURCE_REF)
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(3)
                        .withRawValue(RESOURCE_REF)
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(4)
                        .withRawValue(RESOURCE_REF)
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(5)
                        .withRawValue(RESOURCE_REF)
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(6)
                        .withRawValue(ENTRY_REF)
                        .build()));

        return DbDto.builder()
                .withData(dataObject)
                .withStructure(structureObject)
                .withResource(resourceObject)
                .build();
    }

    private static DbResourceDto createDefaultResourceObject() {
        return DbResourceDto.builder()
                    .withCategoryCount(1)
                    .atVersion("1,0")
                    .build();
    }

    private static void setAllResourceValues(ResourceEntryDto resourceEntryDto, String resourceValue) {
        Locale.valuesAsStream()
                .forEach(locale -> resourceEntryDto.setValueForLocale(resourceValue, locale));
    }
}
