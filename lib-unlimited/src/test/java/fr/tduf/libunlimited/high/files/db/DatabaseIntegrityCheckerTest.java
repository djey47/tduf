package fr.tduf.libunlimited.high.files.db;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseIntegrityCheckerTest {

    @Test(expected = NullPointerException.class)
    public void load_whenNullDtos_shouldThrowNPE() throws Exception {
        //GIVEN-WHEN-THEN
        DatabaseIntegrityChecker.load(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void load_whenIncompleteDtoList_shouldThrowIllegalArgumentException() throws Exception {
        //GIVEN-WHEN-THEN
        DatabaseIntegrityChecker.load(new ArrayList<>());
    }

    @Test
    public void load_whenCompleteDtoList_shouldBuildIndexes() throws Exception {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalResource();

        //WHEN
        DatabaseIntegrityChecker databaseIntegrityChecker = DatabaseIntegrityChecker.load(dbDtos);

        //THEN
        assertThat(databaseIntegrityChecker.getTopicObjectsByReferences()).hasSize(18);

        assertThat(databaseIntegrityChecker.getFieldsByNamesByTopicObjects()).hasSize(18);
    }

    @Test
    public void checkAll_whenNoError_shouldReturnEmptyList() {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithoutErrors();

        //WHEN-THEN
        assertThat(DatabaseIntegrityChecker.load(dbDtos).checkAllContentsObjects()).isEmpty();
    }

    @Test
    public void checkAll_whenMissingResourceInTopics_shouldReturnIntegrityErrors() {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalResource();

        //WHEN
        List<IntegrityError> integrityErrors = DatabaseIntegrityChecker.load(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, "Reference", "200");
    }

    @Test
    public void checkAll_whenMissingResourceInRemoteTopic_shouldReturnIntegrityErrors() {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingForeignResource();

        //WHEN
        List<IntegrityError> integrityErrors = DatabaseIntegrityChecker.load(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND);
        assertAllIntegrityErrorsContainInformation(integrityErrors, "Remote Topic", DbDto.Topic.ACHIEVEMENTS);
        assertAllIntegrityErrorsContainInformation(integrityErrors, "Reference", "300" );
    }

    @Test
    public void checkAll_whenMissingResourceInLocalAndRemoteTopic_shouldReturnIntegrityErrors() {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingLocalAndForeignResource();

        //WHEN
        List<IntegrityError> integrityErrors = DatabaseIntegrityChecker.load(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(36);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND);
    }

    @Test
    public void checkAll_whenMissingContentsEntryInRemoteTopic_shouldReturnIntegrityErrors() {
        //GIVEN
        List<DbDto> dbDtos = createAllDtosWithMissingForeignEntry();

        //WHEN
        List<IntegrityError> integrityErrors = DatabaseIntegrityChecker.load(dbDtos).checkAllContentsObjects();

        //THEN
        assertThat(integrityErrors).hasSize(18);
        assertThat(integrityErrors).extracting("errorTypeEnum").containsOnly(IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND);
    }

    private List<DbDto> createAllDtosWithoutErrors() {
        DbDataDto dataDto = createContentsOneEntryFiveItems("001");

        DbResourceDto resourceDto = createResourceNoEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalResource() {
        DbDataDto dataDto = createContentsOneEntryFiveItems("001");

        DbResourceDto resourceDto = createResourceOneLocalEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignResource() {
        DbDataDto dataDto = createContentsOneEntryFiveItems("001");

        DbResourceDto resourceDto = createResourceOneForeignEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingLocalAndForeignResource() {
        DbDataDto dataDto = createContentsOneEntryFiveItems("001");

        DbResourceDto resourceDto = createResourceOneLocalAndOneForeignEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtosWithMissingForeignEntry() {
        DbDataDto dataDto = createContentsOneEntryFiveItems("000");

        DbResourceDto resourceDto = createResourceNoEntryMissing();

        return createAllDtos(dataDto, resourceDto);
    }

    private List<DbDto> createAllDtos(DbDataDto dataDto, DbResourceDto resourceDto) {
        return asList(DbDto.Topic.values()).stream()

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
                                .withLocale(DbResourceDto.Locale.FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry2())
                                .addEntry(createRemoteResourceEntry())
                                .build();
    }

    private DbResourceDto createResourceOneForeignEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(DbResourceDto.Locale.FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createLocalResourceEntry2())
                                .build();
    }

    private DbResourceDto createResourceOneLocalEntryMissing() {
        return DbResourceDto.builder()
                                .withLocale(DbResourceDto.Locale.FRANCE)
                                .addEntry(createLocalResourceEntry1())
                                .addEntry(createRemoteResourceEntry())
                                .build();
    }

    private DbResourceDto createResourceOneLocalAndOneForeignEntryMissing() {
        return DbResourceDto.builder()
                .withLocale(DbResourceDto.Locale.FRANCE)
                .addEntry(createLocalResourceEntry1())
                .build();
    }

    private DbDataDto createContentsOneEntryFiveItems(String entryUniqueIdentifier) {
        return DbDataDto.builder()
                                .addEntry(DbDataDto.Entry.builder()
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("identifier")
                                                .withRawValue(entryUniqueIdentifier)
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef1")
                                                .withRawValue("100")
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef2")
                                                .withRawValue("200")
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("resourceRef3")
                                                .withRawValue("300")
                                                .build())
                                        .addItem(DbDataDto.Item.builder()
                                                .forName("contentsRef")
                                                .withRawValue("001")
                                                .build())
                                        .build())
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
                        .fromType(DbStructureDto.FieldType.UID)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef1")
                        .fromType(DbStructureDto.FieldType.RESOURCE_CURRENT)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef2")
                        .fromType(DbStructureDto.FieldType.RESOURCE_CURRENT)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("resourceRef3")
                        .fromType(DbStructureDto.FieldType.RESOURCE_REMOTE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("contentsRef")
                        .fromType(DbStructureDto.FieldType.REFERENCE)
                        .toTargetReference("ACHIEVEMENTS-topic")
                        .build())
                .build();
    }

    private static void assertAllIntegrityErrorsContainInformation(List<IntegrityError> integrityErrors, String infoKey, Object infoValue) {
        for (IntegrityError integrityError : integrityErrors) {
            assertThat(integrityError.getInformation()).containsEntry(infoKey, infoValue);
        }
    }
}