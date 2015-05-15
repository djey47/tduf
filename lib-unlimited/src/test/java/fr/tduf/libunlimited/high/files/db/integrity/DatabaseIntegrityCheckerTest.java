package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseIntegrityCheckerTest {

    private static final String UID_NON_EXISTING = "000";
    private static final String UID_EXISTING = "001";

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
        List<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

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
        List<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

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
        List<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, IntegrityError.ErrorInfoEnum.REMOTE_TOPIC, ACHIEVEMENTS);
        assertAllIntegrityErrorsContainInformation(integrityErrors, IntegrityError.ErrorInfoEnum.REFERENCE, "300" );
    }

    @Test
    public void checkAll_whenMissingResourceInLocalAndRemoteTopic_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalAndForeignResource();

        //WHEN
        List<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(36);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(RESOURCE_REFERENCE_NOT_FOUND);
    }

    @Test
    public void checkAll_whenMissingContentsEntryInRemoteTopic_shouldReturnIntegrityErrors() throws ReflectiveOperationException {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingForeignEntry();

        //WHEN
        List<IntegrityError> integrityErrors = createChecker(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(CONTENTS_REFERENCE_NOT_FOUND);
    }

    private List<DbDto> createAllDtosWithoutErrors() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceNoEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceOneLocalEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalResourceTypeH() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceAnotherLocalEntryMissing();
        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceOneForeignEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalAndForeignResource() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_EXISTING);

        DbResourceDto resourceDto = createResourceOneLocalAndOneForeignEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignEntry() {
        DbDataDto dataDto = createContentsOneEntryEightItems(UID_NON_EXISTING);

        DbResourceDto resourceDto = createResourceNoEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtos(DbDataDto dataDto, DbResourceDto resourceDto) {
        return Stream.of(DbDto.Topic.values())

                .map((topicEnum) -> {
                    DbStructureDto structureDto = createStructure(topicEnum);

                    return DbDto.builder()
                            .withStructure(structureDto)
                            .withData(dataDto)
                            .addResource(resourceDto)
                            .build();
                })

                .collect(toList());
    }

    private DbResourceDto createResourceNoEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry2())
                                .addEntry(createLocalResourceEntry3())
                                .addEntry(createRemoteResourceEntry())
                                .build();
    }

    private DbResourceDto createResourceOneForeignEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry2())
                                .addEntry(createLocalResourceEntry3())
                                .build();
    }

    private DbResourceDto createResourceOneLocalEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry3())
                                .addEntry(createRemoteResourceEntry())
                                .build();
    }

    private DbResourceDto createResourceAnotherLocalEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry2())
                                .addEntry(createRemoteResourceEntry())
                                .build();
    }

    private DbResourceDto createResourceOneLocalAndOneForeignEntryMissing() {
        return DbResourceDto.builder()
                .withLocale(FRANCE)
                .addEntry(createLocalResourceEntry1())
                .addEntry(createLocalResourceEntry3())
                .build();
    }

    private DbDataDto createContentsOneEntryEightItems(String entryUniqueIdentifier) {
        return DbDataDto.builder()
                                .addEntry(DbDataDto.Entry.builder()
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("identifier")
                                                .withRawValue(entryUniqueIdentifier)
                                                .ofFieldRank(1)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef1")
                                                .withRawValue("100")
                                                .ofFieldRank(2)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef2")
                                                .withRawValue("200")
                                                .ofFieldRank(3)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef3")
                                                .withRawValue("300")
                                                .ofFieldRank(4)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef4")
                                                .withRawValue("400")
                                                .ofFieldRank(5)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("contentsRef")
                                                .withRawValue("001")
                                                .ofFieldRank(6)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("field")
                                                .withRawValue("value1")
                                                .ofFieldRank(7)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("field")
                                                .withRawValue("value2")
                                                .ofFieldRank(8)
                                                .build())
                                        .build())
                                .build();
    }

    private DbResourceDto.Entry createLocalResourceEntry3() {
        return DbResourceDto.Entry.builder()
                .forReference("400")
                .withValue("QUATRE CENTS")
                .build();
    }

    private DbResourceDto.Entry createLocalResourceEntry2() {
        return DbResourceDto.Entry.builder()
                .forReference("200")
                .withValue("DEUX CENTS")
                .build();
    }

    private DbResourceDto.Entry createLocalResourceEntry1() {
        return DbResourceDto.Entry.builder()
                .forReference("100")
                .withValue("CENT")
                .build();
    }

    private DbResourceDto.Entry createRemoteResourceEntry() {
        return DbResourceDto.Entry.builder()
                .forReference("300")
                .withValue("TROIS CENTS")
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
                        .forName("resourceRef2")
                        .fromType(RESOURCE_CURRENT_GLOBALIZED)
                        .ofRank(3)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef3")
                        .fromType(RESOURCE_REMOTE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .ofRank(4)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef4")
                        .fromType(RESOURCE_CURRENT_LOCALIZED)
                        .ofRank(5)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("contentsRef")
                        .fromType(REFERENCE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .ofRank(6)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("field")
                        .fromType(INTEGER)
                        .ofRank(7)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("field")
                        .fromType(INTEGER)
                        .ofRank(8)
                        .build())
                .build();
    }

    private static void assertAllIntegrityErrorsContainInformation(List<IntegrityError> integrityErrors, IntegrityError.ErrorInfoEnum infoKey, Object infoValue) {
        for (IntegrityError integrityError : integrityErrors) {
            assertThat(integrityError.getInformation()).containsEntry(infoKey, infoValue);
        }
    }

    private static DatabaseIntegrityChecker createChecker(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, databaseObjects);
    }
}