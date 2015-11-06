package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.AFTER_MARKET_PACKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseIntegrityFixerTest {

    @Mock
    private static DatabaseGenHelper genHelperMock;

    @Before
    public void setUp() {
        BulkDatabaseMiner.clearAllCaches();
    }

    @Test(expected = NullPointerException.class)
    public void fixAllContentsObjects_whenNullErrors_shouldThrowNPE() throws Exception {
        //GIVEN-WHEN
        createFixer(new ArrayList<>()).fixAllContentsObjects(null);
        
        //THEN: NPE
    }

    @Test
    public void fixAllContentsObjects_whenNoError_shouldSetIntegrityErrors_andReturnEmptyList() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = new ArrayList<>();
        List<IntegrityError> integrityErrors = new ArrayList<>();


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);


        // THEN
        assertThat(integrityFixer.getIntegrityErrors()).isNotNull();
        assertThat(actualRemainingErrors).isEmpty();
        assertThat(integrityFixer.getDatabaseObjects()).isSameAs(dbDtos);
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorAutoFixed_shouldReturnEmptyList() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        List<IntegrityError> integrityErrors = singletonList(createIntegrityError_AutoFixed());

        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);

        // THEN
        assertThat(actualRemainingErrors).isEmpty();
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorNotHandled_shouldReturnErrorInList() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        List<IntegrityError> integrityErrors = singletonList(createIntegrityError_NotHandled());

        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);

        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors).isEqualTo(integrityErrors);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asLocalResourceReferenceNotFound_shouldInsertMissingResource() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, ACHIEVEMENTS);
        info.put(LOCALE, Locale.FRANCE);
        info.put(REFERENCE, "123456");
        List<IntegrityError> integrityErrors = singletonList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry createdEntry = searchResourceEntry("123456", ACHIEVEMENTS, Locale.FRANCE, fixedDatabaseObjects);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getValue()).isEqualTo("-FIXED BY TDUF-");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteResourceReferenceNotFound_shouldInsertMissingResource() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, AFTER_MARKET_PACKS);
        info.put(LOCALE, Locale.FRANCE);
        info.put(REFERENCE, "1234567");
        List<IntegrityError> integrityErrors = singletonList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry createdEntry = searchResourceEntry("1234567", AFTER_MARKET_PACKS, Locale.FRANCE, fixedDatabaseObjects);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getValue()).isEqualTo("-FIXED BY TDUF-");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteContentsReferenceNotFound_shouldInsertMissingContents() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, AFTER_MARKET_PACKS);
        info.put(REFERENCE, "11111111");
        List<IntegrityError> integrityErrors = singletonList(IntegrityError.builder().ofType(CONTENTS_REFERENCE_NOT_FOUND).addInformations(info).build());

        DbDto remoteTopicObject = dbDtos.get(1);
        List<DbStructureDto.Field> remoteStructureFields = remoteTopicObject.getStructure().getFields();
        DbDataDto.Item fixedItem1 = DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(0), AFTER_MARKET_PACKS)
                .withRawValue("11111111")
                .build();
        DbDataDto.Item fixedItem2 = DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(1), AFTER_MARKET_PACKS)
                .withRawValue("0")
                .build();
        DbDataDto.Item fixedItem3 = DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(2), AFTER_MARKET_PACKS)
                .withRawValue("REF")
                .build();
        List<DbDataDto.Item> fixedItems = asList(fixedItem1, fixedItem2, fixedItem3);
        when(genHelperMock.buildDefaultContentItems(Optional.of("11111111"), remoteTopicObject)).thenReturn(fixedItems);


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<DbDataDto.Entry> allEntries = searchContentsEntries(AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        DbDataDto.Entry createdEntry = allEntries.get(0);
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        DbDataDto.Item item1 = createdEntry.getItems().get(0);
        assertThat(item1.getName()).isEqualTo("ID");
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).isEqualTo("11111111");

        DbDataDto.Item item2 = createdEntry.getItems().get(1);
        assertThat(item2.getName()).isEqualTo("Val1");
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("0");

        DbDataDto.Item item3 = createdEntry.getItems().get(2);
        assertThat(item3.getName()).isEqualTo("RemoteRef");
        assertThat(item3.getFieldRank()).isEqualTo(3);
        assertThat(item3.getRawValue()).isEqualTo("REF");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asContentsFieldsCountMismatch_shouldInsertMissingField() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithDataEntryTwoFieldsMissing();

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(EXPECTED_COUNT, 3);
        info.put(ACTUAL_COUNT, 1);
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(ENTRY_ID, 0L);
        List<IntegrityError> integrityErrors = singletonList(IntegrityError.builder().ofType(CONTENTS_FIELDS_COUNT_MISMATCH).addInformations(info).build());

        DbDto topicObject = dbDtos.get(1);
        DbStructureDto.Field firstStructureField = topicObject.getStructure().getFields().get(0);
        DbStructureDto.Field thirdStructureField = topicObject.getStructure().getFields().get(2);
        DbDataDto.Item firstItem = DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(firstStructureField, AFTER_MARKET_PACKS)
                .withRawValue("11111111")
                .build();
        DbDataDto.Item thirdItem = DbDataDto.Item.builder()
                .fromStructureFieldAndTopic(thirdStructureField, AFTER_MARKET_PACKS)
                .withRawValue("100")
                .build();

        when(genHelperMock.buildDefaultContentItem(Optional.empty(), firstStructureField,  topicObject, true)).thenReturn(firstItem);
        when(genHelperMock.buildDefaultContentItem(Optional.empty(), thirdStructureField,  topicObject, true)).thenReturn(thirdItem);


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<DbDataDto.Entry> allEntries = searchContentsEntries(AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        DbDataDto.Entry createdEntry = allEntries.get(0);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        DbDataDto.Item item1 = createdEntry.getItems().get(0);
        assertThat(item1.getName()).isEqualTo("ID");
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).isEqualTo("11111111");

        DbDataDto.Item item2 = createdEntry.getItems().get(1);
        assertThat(item2.getName()).isEqualTo("Val1");
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("100");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceNotFound_shouldBuildMissingLocale() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(FILE, "./TDU_Achievements.fr");
        info.put(LOCALE, Locale.ITALY);

        List<IntegrityError> integrityErrors = singletonList(IntegrityError.builder().ofType(RESOURCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry actualResourceEntry = searchResourceEntry("000", ACHIEVEMENTS, Locale.ITALY, fixedDatabaseObjects);
        assertThat(actualResourceEntry.getValue()).isEqualTo("TDUF TEST");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceItemCountMismatch_shouldCompleteMissingLocale() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithUnconsistentResourceEntryCount();
        List<IntegrityError> integrityErrors = singletonList(createIntegrityError_ResourceItemsCountMismatch());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry actualResourceEntry = searchResourceEntry("000", AFTER_MARKET_PACKS, Locale.CHINA, fixedDatabaseObjects);
        assertThat(actualResourceEntry.getValue()).isEqualTo("TDUF TEST");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asValuesDiffentForGlobalizedResource_shouldApplyProperValue() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithDifferentResourceValuesGlobalized();
        List<IntegrityError> integrityErrors = singletonList(createIntegrityError_ResourceValuesDifferentGlobalized());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        Stream.of(Locale.FRANCE, Locale.UNITED_STATES)

                .forEach((locale) -> {
                    DbResourceDto.Entry actualResourceEntry = searchResourceEntry("000", AFTER_MARKET_PACKS, locale, fixedDatabaseObjects);
                    assertThat(actualResourceEntry.getValue()).isEqualTo("TDUF TEST");
                });
    }

    @Test
    public void fixAllContentsObjects_whenManyErrors() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithUnconsistentResourceEntryCount();
        List<IntegrityError> integrityErrors = asList(
                createIntegrityError_AutoFixed(),
                createIntegrityError_NotHandled(),
                createIntegrityError_ResourceItemsCountMismatch());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors.get(0).getErrorTypeEnum()).isEqualTo(CONTENTS_NOT_FOUND);

        assertThat(fixedDatabaseObjects).isNotEmpty();
    }

    private static DatabaseIntegrityFixer createFixer(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        DatabaseIntegrityFixer fixer = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
        fixer.setGenHelper(genHelperMock);
        return fixer;
    }

    private static List<DbDto> createDefaultDatabaseObjects() {
        List<DbDto> dbDtos = new ArrayList<>();
        dbDtos.add(createDefaultDatabaseObject());
        dbDtos.add(createDefaultDatabaseObject2());
        return dbDtos;
    }

    private static List<DbDto> createDatabaseObjectsWithDataEntryTwoFieldsMissing() {
        List<DbDto> dbDtos = new ArrayList<>();
        dbDtos.add(createDefaultDatabaseObject());
        dbDtos.add(createDatabaseObjectWithTwoContentsFieldsMissing());
        return dbDtos;
    }

    private static List<DbDto> createDatabaseObjectsWithUnconsistentResourceEntryCount() {
        List<DbDto> dbDtos = new ArrayList<>();
        dbDtos.add(createDefaultDatabaseObject());
        dbDtos.add(createDatabaseObjectWithUnconsistentResourceEntryCount());
        return dbDtos;
    }

    private static List<DbDto> createDatabaseObjectsWithDifferentResourceValuesGlobalized() {
        List<DbDto> dbDtos = new ArrayList<>();
        dbDtos.add(createDefaultDatabaseObject());
        dbDtos.add(createDatabaseObjectWithDifferentResourceValuesGlobalized());
        return dbDtos;
    }

    private static DbDto createDefaultDatabaseObject() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject())
                .withData(createDefaultContentsObject())
                .addResource(createResourceObjectWithOneResourceEntry(Locale.FRANCE))
                .addResource(createResourceObjectWithOneResourceEntry(Locale.UNITED_STATES))
                .build();
    }

    private static DbDto createDefaultDatabaseObject2() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .addResource(createResourceObjectWithOneResourceEntry(Locale.FRANCE))
                .build();
    }

    private static DbDto createDatabaseObjectWithTwoContentsFieldsMissing() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createContentsObjectWithTwoFieldsMissing())
                .addResource(createResourceObjectWithOneResourceEntry(Locale.FRANCE))
                .build();
    }

    private static DbDto createDatabaseObjectWithUnconsistentResourceEntryCount() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .addResource(createResourceObjectWithOneResourceEntry(Locale.FRANCE))
                .addResource(createResourceObjectWithOneResourceEntry(Locale.UNITED_STATES))
                .addResource(createDefaultResourceObject(Locale.CHINA))
                .build();
    }

    private static DbDto createDatabaseObjectWithDifferentResourceValuesGlobalized() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .addResource(createResourceObjectWithOneResourceEntry(Locale.FRANCE))
                .addResource(createResourceObjectWithAnotherResourceEntry(Locale.UNITED_STATES))
                .build();
    }

    private static DbStructureDto createDefaultStructureObject() {
        return DbStructureDto.builder()
                .forReference("1")
                .forTopic(ACHIEVEMENTS)
                .addItem(DbStructureDto.Field.builder()
                        .forName("ID")
                        .fromType(DbStructureDto.FieldType.UID)
                        .ofRank(1)
                        .build())
                .build();
    }

    private static DbStructureDto createDefaultStructureObject2() {
        return DbStructureDto.builder()
                .forReference("2")
                .forTopic(AFTER_MARKET_PACKS)
                .addItem(DbStructureDto.Field.builder()
                        .forName("ID")
                        .fromType(DbStructureDto.FieldType.UID)
                        .ofRank(1)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("Val1")
                        .fromType(DbStructureDto.FieldType.INTEGER)
                        .ofRank(2)
                        .build())
                .addItem(DbStructureDto.Field.builder()
                        .forName("RemoteRef")
                        .fromType(DbStructureDto.FieldType.REFERENCE)
                        .ofRank(3)
                        .toTargetReference("1")
                        .build())
                .build();
    }

    private static DbDataDto createDefaultContentsObject() {
        return DbDataDto.builder()
                .build();
    }

    private static DbDataDto createContentsObjectWithTwoFieldsMissing() {
        // Missing: Field 1 = ID, Field 3 = RemoteRef
        return DbDataDto.builder()
                .addEntry(DbDataDto.Entry.builder()
                        .addItem(DbDataDto.Item.builder()
                                .forName("Val1")
                                .ofFieldRank(1)
                                .withRawValue("100")
                                .build())
                        .build())
                .build();
    }

    private static DbResourceDto createDefaultResourceObject(Locale locale) {
        return DbResourceDto.builder()
                .withLocale(locale)
                .build();
    }

    private static DbResourceDto createResourceObjectWithOneResourceEntry(Locale locale) {
        return DbResourceDto.builder()
                .withLocale(locale)
                .addEntry(DbResourceDto.Entry.builder()
                        .forReference("000")
                        .withValue("TDUF TEST")
                        .build())
                .build();
    }

    private static DbResourceDto createResourceObjectWithAnotherResourceEntry(Locale locale) {
        return DbResourceDto.builder()
                .withLocale(locale)
                .addEntry(DbResourceDto.Entry.builder()
                        .forReference("000")
                        .withValue("TDUF TEST ALTERED")
                        .build())
                .build();
    }

    private static IntegrityError createIntegrityError_AutoFixed() {
        return IntegrityError.builder().ofType(CONTENT_ITEMS_COUNT_MISMATCH).addInformations(new HashMap<>()).build();
    }

    private IntegrityError createIntegrityError_NotHandled() {
        return IntegrityError.builder().ofType(CONTENTS_NOT_FOUND).addInformations(new HashMap<>()).build();
    }

    private IntegrityError createIntegrityError_ResourceItemsCountMismatch() {
        Map<Locale, Integer> perLocaleCountInfo = new HashMap<>();
        perLocaleCountInfo.put(Locale.CHINA, 0);
        perLocaleCountInfo.put(Locale.FRANCE, 1);
        perLocaleCountInfo.put(Locale.UNITED_STATES, 1);

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(PER_LOCALE_COUNT, perLocaleCountInfo);

        return IntegrityError.builder().ofType(RESOURCE_ITEMS_COUNT_MISMATCH).addInformations(info).build();
    }

    private IntegrityError createIntegrityError_ResourceValuesDifferentGlobalized() {
        Map<String, Integer> valueCounter = new HashMap<>();
        valueCounter.put("TDUF TEST", 7);
        valueCounter.put("TDUF TEST ALTERED", 1);

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(REFERENCE, "000");
        info.put(PER_VALUE_COUNT, valueCounter);

        return IntegrityError.builder().ofType(RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES).addInformations(info).build();
    }

    private static DbResourceDto.Entry searchResourceEntry(String reference, Topic topic, Locale locale, List<DbDto> databaseObjects) {
        return BulkDatabaseMiner.load(databaseObjects)
                .getResourceEntryFromTopicAndLocaleWithReference(reference, topic, locale).get();
    }

    private static List<DbDataDto.Entry> searchContentsEntries(Topic topic, List<DbDto> databaseObjects) {
        return BulkDatabaseMiner.load(databaseObjects)
                .getDatabaseTopic(topic)
                .get()
                .getData().getEntries();
    }
}