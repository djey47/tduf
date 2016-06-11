package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.PER_VALUE_COUNT;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.SOURCE_TOPIC;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.common.game.domain.Locale.CHINA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseIntegrityCheckerTest {

    private static final String UID_NON_EXISTING = "000";
    private static final String UID_EXISTING = "001";

    @Before
    public void setUp() {}

    @Test
    public void checkAll_whenEmptyDatabaseObjects_shouldReturnSingleIntegrityError() throws ReflectiveOperationException {
        //GIVEN
        DatabaseIntegrityChecker checker = createChecker(new ArrayList<>());


        // WHEN
        Set<IntegrityError> integrityErrors = checker.checkAllContentsObjects();


        // THEN
        assertThat(integrityErrors).hasSize(1);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(INCOMPLETE_DATABASE);

        Set<DbDto.Topic> missingTopics = (Set<DbDto.Topic>) integrityErrors.stream()
                .findFirst().get()
                .getInformation().get(IntegrityError.ErrorInfoEnum.MISSING_TOPICS);
        assertThat(missingTopics)
                .containsOnly(DbDto.Topic.values());
    }

    @Test
    public void checkAll_whenNoError_shouldBuildIndexes_andReturnEmptyList() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithoutErrors();

        //WHEN
        DatabaseIntegrityChecker checker = createChecker(dbDtos);

        //THEN
        assertThat(checker.checkAllContentsObjects()).isEmpty();

        assertThat(checker.getTopicObjectsByReferences()).hasSize(18);
        assertThat(checker.getFieldsByRanksByTopicObjects()).hasSize(18);
    }

    @Test
    public void checkAll_whenMissingResourceInTopics_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalResource();

        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, IntegrityError.ErrorInfoEnum.REFERENCE, "200");
    }

    @Test
    public void checkAll_whenMissingResourceInTopics_andTypeH_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalResourceTypeH();

        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, IntegrityError.ErrorInfoEnum.REFERENCE, "400");
    }

    @Test
    public void checkAll_whenMissingResourceInRemoteTopic_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingForeignResource();

        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, IntegrityError.ErrorInfoEnum.REFERENCE, "300");
    }

    @Test
    public void checkAll_whenMissingResourceInLocalAndRemoteTopic_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalAndForeignResource();

        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(36);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
    }

    @Test
    public void checkAll_whenMissingContentsEntryInRemoteTopic_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingForeignEntry();

        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(CONTENTS_REFERENCE_NOT_FOUND);
    }

    @Test
    public void checkAll_whenGlobalResourceValuesNotIdentical_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithDifferentGlobalResourceValueForLocaleCH();


        //WHEN
        Set<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();


        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES);

        IntegrityError integrityError = integrityErrors.stream().findAny().get();
        assertThat(integrityError.getInformation())
                .containsKey(IntegrityError.ErrorInfoEnum.REFERENCE)
                .containsValue("100")
                .containsKey(SOURCE_TOPIC)
                .containsKey(PER_VALUE_COUNT);

        Map<String, Integer> valueCounter = (Map<String, Integer>) integrityError.getInformation().get(PER_VALUE_COUNT);
        assertThat(valueCounter).contains(MapEntry.entry("CENT", 7));
        assertThat(valueCounter).contains(MapEntry.entry("CENT_ALTERED", 1));
    }

    private List<DbDto> createAllDtosWithoutErrors() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceEnhancedNoEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceEnhancedOneLocalEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalAndForeignResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceEnhancedOneLocalAndOneForeignEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalResourceTypeH() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceAnotherLocalEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceOneForeignEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignEntry() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_NON_EXISTING);

        DbResourceDto resourceDto = createResourceEnhancedNoEntryMissing();

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithDifferentGlobalResourceValueForLocaleCH() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceEnhancedNoEntryMissing();
        resourceDto.getEntryByReference("100").get().setValueForLocale("CENT_ALTERED", CHINA);

        return createDtosForAllTopics(dataDto, resourceDto);
    }

    private List<DbDto> createDtosForAllTopics(DbDataDto dataDto, DbResourceDto resourceDto) {
        return DbDto.Topic.valuesAsStream()

                .map((topicEnum) -> {
                    DbStructureDto structureDto = createStructure(topicEnum);

                    return DbDto.builder()
                            .withStructure(structureDto)
                            .withData(dataDto)
                            .withResource(resourceDto)
                            .build();
                })

                .collect(toList());
    }

    private DbResourceDto createResourceEnhancedNoEntryMissing() {
        final DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference("100").setValue("CENT");            //Local 1
        resourceObject.addEntryByReference("200").setValue("DEUX CENTS");      //Local 2
        resourceObject.addEntryByReference("400").setValue("QUATRE CENTS");    //Local 3
        resourceObject.addEntryByReference("300").setValue("TROIS CENTS");     //Remote

        return resourceObject;
    }

    private DbResourceDto createResourceEnhancedOneLocalEntryMissing() {
        final DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference("100").setValue("CENT");            //Local 1
        resourceObject.addEntryByReference("400").setValue("QUATRE CENTS");    //Local 2
        resourceObject.addEntryByReference("300").setValue("TROIS CENTS");     //Remote

        return resourceObject;
    }

    private DbResourceDto createResourceEnhancedOneLocalAndOneForeignEntryMissing() {
        final DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference("100").setValue("CENT");            //Local 1
        resourceObject.addEntryByReference("400").setValue("QUATRE CENTS");    //Local 2

        return resourceObject;
    }

    private DbResourceDto createResourceOneForeignEntryMissing() {
        final DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference("100").setValue("CENT");            //Local 1
        resourceObject.addEntryByReference("200").setValue("DEUX CENTS");      //Local 2
        resourceObject.addEntryByReference("400").setValue("QUATRE CENTS");    //Local 3

        return resourceObject;
    }

    private DbResourceDto createResourceAnotherLocalEntryMissing() {
        final DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();
        resourceObject.addEntryByReference("100").setValue("CENT");            //Local 1
        resourceObject.addEntryByReference("200").setValue("DEUX CENTS");      //Local 2
        resourceObject.addEntryByReference("300").setValue("TROIS CENTS");      //Remote

        return resourceObject;
    }

    private DbResourceDto createDefaultResourceObjectEnhanced() {
        return DbResourceDto.builder()
                    .withCategoryCount(1)
                    .atVersion("1,0")
                    .build();
    }

    private DbDataDto createContentsOneEntryEightItems(String entryUniqueIdentifier) {
        return DbDataDto.builder()
                                .addEntry(DbDataDto.Entry.builder()
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue(entryUniqueIdentifier)
                                                .ofFieldRank(1)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("100")
                                                .ofFieldRank(2)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("100")
                                                .ofFieldRank(3)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("200")
                                                .ofFieldRank(4)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("300")
                                                .ofFieldRank(5)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("400")
                                                .ofFieldRank(6)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("001")
                                                .ofFieldRank(7)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("value1")
                                                .ofFieldRank(8)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .withRawValue("value2")
                                                .ofFieldRank(9)
                                                .build())
                                        .build())
                                .build();
    }

    private DbStructureDto createStructure(DbDto.Topic topicEnum) {
        // 5 columns = (uid, local resource ref, local resource ref, remote resource ref, remote contents ref)
        return DbStructureDto.builder()
                                .forTopic(topicEnum)
                .forReference(topicEnum.name() + "-topic")
                .addItem(DbStructureDto.Field.builder()
                        .forName("identifier")
                        .fromType(UID)
                        .ofRank(1)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef1")
                        .fromType(RESOURCE_CURRENT_GLOBALIZED)
                        .ofRank(2)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef1Bis")
                        .fromType(RESOURCE_CURRENT_GLOBALIZED)
                        .ofRank(3)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef2")
                        .fromType(RESOURCE_CURRENT_GLOBALIZED)
                        .ofRank(4)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef3")
                        .fromType(RESOURCE_REMOTE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .ofRank(5)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef4")
                        .fromType(RESOURCE_CURRENT_LOCALIZED)
                        .ofRank(6)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("contentsRef")
                        .fromType(REFERENCE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .ofRank(7)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("field")
                        .fromType(INTEGER)
                        .ofRank(8)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("field")
                        .fromType(INTEGER)
                        .ofRank(9)
                        .build())
                .build();
    }

    private static void assertAllIntegrityErrorsContainInformation(Set<IntegrityError> integrityErrors, IntegrityError.ErrorInfoEnum infoKey, Object infoValue) {
        for (IntegrityError integrityError : integrityErrors) {
            assertThat(integrityError.getInformation()).containsEntry(infoKey, infoValue);
        }
    }

    private static DatabaseIntegrityChecker createChecker(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, databaseObjects);
    }
}
