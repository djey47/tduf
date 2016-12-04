package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.ACHIEVEMENTS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.AFTER_MARKET_PACKS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.tduf.libunlimited.common.game.domain.Locale;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseIntegrityFixerTest {

    @Mock
    private static DatabaseGenHelper genHelperMock;

    @Before
    public void setUp() {}

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
        Set<IntegrityError> integrityErrors = new HashSet<>();


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);


        // THEN
        assertThat(integrityFixer.getIntegrityErrors()).isNotNull();
        assertThat(actualRemainingErrors).isEmpty();
        assertThat(integrityFixer.getDatabaseObjects()).isSameAs(dbDtos);
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorAutoFixed_shouldReturnEmptyList() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(createIntegrityError_AutoFixed()));

        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);

        // THEN
        assertThat(actualRemainingErrors).isEmpty();
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorNotHandled_shouldReturnErrorInList() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(createIntegrityError_NotHandled()));

        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);

        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors).isEqualTo(integrityErrors);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asLocalResourceReferenceNotFound_shouldInsertMissingResource() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, ACHIEVEMENTS);
        info.put(LOCALE, FRANCE);
        info.put(REFERENCE, "123456");
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        assertResourceExistsWithValue("123456", "-FIXED BY TDUF-", ACHIEVEMENTS, FRANCE, fixedDatabaseObjects);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteResourceReferenceNotFound_shouldInsertMissingResource() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, AFTER_MARKET_PACKS);
        info.put(LOCALE, FRANCE);
        info.put(REFERENCE, "1234567");
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        assertResourceExistsWithValue("1234567", "-FIXED BY TDUF-", AFTER_MARKET_PACKS, FRANCE, fixedDatabaseObjects);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteContentsReferenceNotFound_shouldInsertMissingContents() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, AFTER_MARKET_PACKS);
        info.put(REFERENCE, "11111111");
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(IntegrityError.builder().ofType(CONTENTS_REFERENCE_NOT_FOUND).addInformations(info).build()));

        DbDto remoteTopicObject = dbDtos.get(1);
        List<DbStructureDto.Field> remoteStructureFields = remoteTopicObject.getStructure().getFields();
        ContentItemDto fixedItem1 = ContentItemDto.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(0), AFTER_MARKET_PACKS)
                .withRawValue("11111111")
                .build();
        ContentItemDto fixedItem2 = ContentItemDto.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(1), AFTER_MARKET_PACKS)
                .withRawValue("0")
                .build();
        ContentItemDto fixedItem3 = ContentItemDto.builder()
                .fromStructureFieldAndTopic(remoteStructureFields.get(2), AFTER_MARKET_PACKS)
                .withRawValue("REF")
                .build();
        List<ContentItemDto> fixedItems = asList(fixedItem1, fixedItem2, fixedItem3);
        when(genHelperMock.buildDefaultContentItems(Optional.of("11111111"), remoteTopicObject)).thenReturn(fixedItems);


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<ContentEntryDto> allEntries = searchContentsEntries(AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        ContentEntryDto createdEntry = allEntries.get(0);
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        ContentItemDto item1 = createdEntry.getItems().get(0);
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).isEqualTo("11111111");

        ContentItemDto item2 = createdEntry.getItems().get(1);
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("0");

        ContentItemDto item3 = createdEntry.getItems().get(2);
        assertThat(item3.getFieldRank()).isEqualTo(3);
        assertThat(item3.getRawValue()).isEqualTo("REF");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asContentsFieldsCountMismatch_shouldInsertMissingFields() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithDataEntryTwoFieldsMissing();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(EXPECTED_COUNT, 3);
        info.put(ACTUAL_COUNT, 1);
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(ENTRY_ID, 0);
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(IntegrityError.builder().ofType(CONTENTS_FIELDS_COUNT_MISMATCH).addInformations(info).build()));

        DbDto topicObject = dbDtos.get(1);
        DbStructureDto.Field firstStructureField = topicObject.getStructure().getFields().get(0);
        DbStructureDto.Field thirdStructureField = topicObject.getStructure().getFields().get(2);
        ContentItemDto firstItem = ContentItemDto.builder()
                .fromStructureFieldAndTopic(firstStructureField, AFTER_MARKET_PACKS)
                .withRawValue("11111111")
                .build();
        ContentItemDto thirdItem = ContentItemDto.builder()
                .fromStructureFieldAndTopic(thirdStructureField, AFTER_MARKET_PACKS)
                .withRawValue("200")
                .build();

        when(genHelperMock.buildDefaultContentItem(Optional.empty(), firstStructureField,  topicObject)).thenReturn(firstItem);
        when(genHelperMock.buildDefaultContentItem(Optional.empty(), thirdStructureField,  topicObject)).thenReturn(thirdItem);


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<ContentEntryDto> allEntries = searchContentsEntries(AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        ContentEntryDto createdEntry = allEntries.get(0);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        ContentItemDto item1 = createdEntry.getItems().get(0);
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).isEqualTo("11111111");

        ContentItemDto item2 = createdEntry.getItems().get(1);
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("100");

        ContentItemDto item3 = createdEntry.getItems().get(2);
        assertThat(item3.getFieldRank()).isEqualTo(3);
        assertThat(item3.getRawValue()).isEqualTo("200");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceNotFound_shouldBuildMissingLocale() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, ACHIEVEMENTS);
        info.put(FILE, "./TDU_Achievements.fr");
        info.put(LOCALE, ITALY);

        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(IntegrityError.builder().ofType(RESOURCE_NOT_FOUND).addInformations(info).build()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        assertResourceExistsWithValue("000", "TDUF TEST", ACHIEVEMENTS, ITALY, fixedDatabaseObjects);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceItemCountMismatch_shouldCompleteMissingLocale() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithUnconsistentResourceEntryCount();
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(createIntegrityError_ResourceReferenceMissingForOneLocale()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        assertResourceExistsWithValue("000", "TDUF TEST", AFTER_MARKET_PACKS, CHINA, fixedDatabaseObjects);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asDifferentValuesForGlobalizedResource_shouldApplyProperValue() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithDifferentResourceValuesGlobalized();
        Set<IntegrityError> integrityErrors = new HashSet<>(singletonList(createIntegrityError_ResourceValuesDifferentGlobalized()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        Stream.of(FRANCE, UNITED_STATES)
                .forEach((locale) -> assertResourceExistsWithValue("000", "TDUF TEST", AFTER_MARKET_PACKS, locale, fixedDatabaseObjects));
    }

    @Test
    public void fixAllContentsObjects_whenManyErrors() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithUnconsistentResourceEntryCount();
        Set<IntegrityError> integrityErrors = new HashSet<>(asList(
                createIntegrityError_AutoFixed(),
                createIntegrityError_NotHandled(),
                createIntegrityError_ResourceReferenceMissingForOneLocale()));


        // WHEN
        DatabaseIntegrityFixer integrityFixer = createFixer(dbDtos);
        Set<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = integrityFixer.getDatabaseObjects();


        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors.stream().findFirst().get().getErrorTypeEnum()).isEqualTo(CONTENTS_NOT_FOUND);

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
                .withResource(createResourceObjectEnhancedWithOneEntry())
                .build();
    }

    private static DbDto createDefaultDatabaseObject2() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .withResource(createResourceObjectEnhancedWithOneEntryOneItem())
                .build();
    }

    private static DbDto createDatabaseObjectWithTwoContentsFieldsMissing() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createContentsObjectWithTwoFieldsMissing())
                .withResource(createResourceObjectEnhancedWithOneEntryOneItem())
                .build();
    }

    private static DbDto createDatabaseObjectWithUnconsistentResourceEntryCount() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .withResource(createResourceObjectEnhancedWithOneEntryOneMissingValueForLocale())
                .build();
    }

    private static DbResourceDto createDefaultResourceObjectEnhanced() {
        return DbResourceDto.builder()
                .atVersion("1,0")
                .withCategoryCount(1)
                .build();
    }

    private static DbDto createDatabaseObjectWithDifferentResourceValuesGlobalized() {
        return DbDto.builder()
                .withStructure(createDefaultStructureObject2())
                .withData(createDefaultContentsObject())
                .withResource(createResourceObjectEnhancedWithOneEntryOneDifferentValueForLocale())
                .build();
    }

    private static DbResourceDto createResourceObjectEnhancedWithOneEntryOneItem() {
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();

        resourceObject.addEntryByReference("000")
                .setValueForLocale("TDUF TEST", FRANCE);

        return resourceObject;
    }

    private static DbResourceDto createResourceObjectEnhancedWithOneEntry() {
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();

        ResourceEntryDto resourceEntryDto = resourceObject
                .addEntryByReference("000");
        setAllResourceValues(resourceEntryDto, "TDUF TEST");

        return resourceObject;
    }

    private static DbResourceDto createResourceObjectEnhancedWithOneEntryOneMissingValueForLocale() {
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();

        ResourceEntryDto resourceEntryDto = resourceObject
                .addEntryByReference("000");
        setAllResourceValues(resourceEntryDto, "TDUF TEST");
        resourceEntryDto.removeValueForLocale(CHINA);

        return resourceObject;
    }

    private static DbResourceDto createResourceObjectEnhancedWithOneEntryOneDifferentValueForLocale() {
        DbResourceDto resourceObject = createDefaultResourceObjectEnhanced();

        ResourceEntryDto resourceEntryDto = resourceObject.addEntryByReference("000");
        setAllResourceValues(resourceEntryDto, "TDUF TEST");
        resourceEntryDto.setValueForLocale("TDUF TEST ALTERED", UNITED_STATES);

        return resourceObject;
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
                .addEntry(ContentEntryDto.builder()
                        .addItem(ContentItemDto.builder()
                                .ofFieldRank(2)
                                .withRawValue("100")
                                .build())
                        .build())
                .build();
    }

    private static IntegrityError createIntegrityError_AutoFixed() {
        return IntegrityError.builder().ofType(CONTENT_ITEMS_COUNT_MISMATCH).addInformations(new HashMap<>()).build();
    }

    private IntegrityError createIntegrityError_NotHandled() {
        return IntegrityError.builder().ofType(CONTENTS_NOT_FOUND).addInformations(new HashMap<>()).build();
    }

    private IntegrityError createIntegrityError_ResourceReferenceMissingForOneLocale() {
        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(REFERENCE, "000");
        info.put(MISSING_LOCALES, new HashSet<>(singletonList(CHINA)));

        return IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build();
    }

    private IntegrityError createIntegrityError_ResourceValuesDifferentGlobalized() {
        Map<String, Integer> valueCounter = new HashMap<>();
        valueCounter.put("TDUF TEST", 7);
        valueCounter.put("TDUF TEST ALTERED", 1);

        Map<IntegrityError.ErrorInfoEnum, Object> info = new EnumMap<>(IntegrityError.ErrorInfoEnum.class);
        info.put(SOURCE_TOPIC, AFTER_MARKET_PACKS);
        info.put(REFERENCE, "000");
        info.put(PER_VALUE_COUNT, valueCounter);

        return IntegrityError.builder().ofType(RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES).addInformations(info).build();
    }

    private static void assertResourceExistsWithValue(String reference, String value, Topic topic, Locale locale, List<DbDto> databaseObjects) {
        Optional<String> potentialValue = BulkDatabaseMiner.load(databaseObjects)
                .getLocalizedResourceValueFromTopicAndReference(reference, topic, locale);
        assertThat(potentialValue)
                .isPresent()
                .contains(value);
    }

    private static List<ContentEntryDto> searchContentsEntries(Topic topic, List<DbDto> databaseObjects) {
        return BulkDatabaseMiner.load(databaseObjects)
                .getDatabaseTopic(topic)
                .get()
                .getData().getEntries();
    }

    private static void setAllResourceValues(ResourceEntryDto resourceEntryDto, String value) {
        Locale.valuesAsStream()
                .forEach(locale -> resourceEntryDto.setValueForLocale(value, locale));
    }
}
