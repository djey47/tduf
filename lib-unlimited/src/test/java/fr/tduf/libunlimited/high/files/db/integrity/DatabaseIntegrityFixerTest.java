package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

// TODO add test with multiple errors (fixable, autofixed, unfixable, fix failure)
public class DatabaseIntegrityFixerTest {

    @Test(expected = NullPointerException.class)
    public void load_whenNullDtos_shouldThrowNPE() throws Exception {
        //GIVEN-WHEN-THEN
        DatabaseIntegrityFixer.load(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void load_whenNullErrors_shouldThrowNPE() throws Exception {
        //GIVEN-WHEN-THEN
        DatabaseIntegrityFixer.load(new ArrayList<>(), null);
    }

    @Test
    public void load_whenProvidedContents_shouldGetProvidedData() {
        // GIVEN
        List<DbDto> dbDtos = new ArrayList<>();
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);

        // THEN
        assertThat(integrityFixer.getDbDtos()).isEqualTo(dbDtos);
        assertThat(integrityFixer.getIntegrityErrors()).isEqualTo(integrityErrors);
    }

    @Test
    public void fixAllContentsObjects_whenNoError_shouldReturnEmptyList() {
        // GIVEN
        List<DbDto> dbDtos = new ArrayList<>();
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();

        // THEN
        assertThat(actualRemainingErrors).isEmpty();
        assertThat(integrityFixer.getFixedDbDtos()).isSameAs(dbDtos);
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorAutoFixed_shouldReturnEmptyList() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(CONTENT_ITEMS_COUNT_MISMATCH).addInformations(new HashMap<>()).build());

        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();

        // THEN
        assertThat(actualRemainingErrors).isEmpty();
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorNotHandled_shouldReturnErrorInList() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(CONTENTS_NOT_FOUND).addInformations(new HashMap<>()).build());

        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();

        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors).isEqualTo(integrityErrors);
    }

    @Test
    public void fixAllContentsObjects_whenOneErrorHandledButNotFixed_shouldReturnErrorInList() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(CONTENTS_NOT_FOUND).addInformations(new HashMap<>()).build());

        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();

        // THEN
        assertThat(actualRemainingErrors).hasSize(1);
        assertThat(actualRemainingErrors).isEqualTo(integrityErrors);
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asLocalResourceReferenceNotFound_shouldInsertMissingResource() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, Topic.ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, Topic.ACHIEVEMENTS);
        info.put(LOCALE, Locale.FRANCE);
        info.put(REFERENCE, "123456");
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry createdEntry = searchResourceEntry("123456", Topic.ACHIEVEMENTS, Locale.FRANCE, fixedDatabaseObjects);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getValue()).isEqualTo("-FIXED BY TDUF-");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteResourceReferenceNotFound_shouldInsertMissingResource() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, Topic.ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, Topic.AFTER_MARKET_PACKS);
        info.put(LOCALE, Locale.FRANCE);
        info.put(REFERENCE, "1234567");
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(RESOURCE_REFERENCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry createdEntry = searchResourceEntry("1234567", Topic.AFTER_MARKET_PACKS, Locale.FRANCE, fixedDatabaseObjects);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getValue()).isEqualTo("-FIXED BY TDUF-");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asRemoteContentsReferenceNotFound_shouldInsertMissingContents() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, Topic.ACHIEVEMENTS);
        info.put(REMOTE_TOPIC, Topic.AFTER_MARKET_PACKS);
        info.put(REFERENCE, "001");
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(CONTENTS_REFERENCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<DbDataDto.Entry> allEntries = searchContentsEntries(Topic.AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        DbDataDto.Entry createdEntry = allEntries.get(0);
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        DbDataDto.Item item1 = createdEntry.getItems().get(0);
        assertThat(item1.getName()).isEqualTo("ID");
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).hasSize(8);

        DbDataDto.Item item2 = createdEntry.getItems().get(1);
        assertThat(item2.getName()).isEqualTo("Val1");
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("0");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asContentsFieldsCountMismatch_shouldInsertMissingField() {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithDataEntryTwoFieldsMissing();

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(EXPECTED_COUNT, 3);
        info.put(ACTUAL_COUNT, 1);
        info.put(SOURCE_TOPIC, Topic.AFTER_MARKET_PACKS);
        info.put(ENTRY_ID, 0L);
        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(CONTENTS_FIELDS_COUNT_MISMATCH).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        List<DbDataDto.Entry> allEntries = searchContentsEntries(Topic.AFTER_MARKET_PACKS, fixedDatabaseObjects);
        assertThat(allEntries).hasSize(1);
        DbDataDto.Entry createdEntry = allEntries.get(0);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getId()).isEqualTo(0);

        assertThat(createdEntry.getItems()).hasSize(3);

        DbDataDto.Item item1 = createdEntry.getItems().get(0);
        assertThat(item1.getName()).isEqualTo("ID");
        assertThat(item1.getFieldRank()).isEqualTo(1);
        assertThat(item1.getRawValue()).hasSize(8);

        DbDataDto.Item item2 = createdEntry.getItems().get(1);
        assertThat(item2.getName()).isEqualTo("Val1");
        assertThat(item2.getFieldRank()).isEqualTo(2);
        assertThat(item2.getRawValue()).isEqualTo("100");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceNotFound_shouldBuildMissingLocale() {
        // GIVEN
        List<DbDto> dbDtos = createDefaultDatabaseObjects();

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, Topic.ACHIEVEMENTS);
        info.put(FILE, "./TDU_Achievements.fr");
        info.put(LOCALE, Locale.ITALY);

        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(RESOURCE_NOT_FOUND).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry actualResourceEntry = searchResourceEntry("000", Topic.ACHIEVEMENTS, Locale.ITALY, fixedDatabaseObjects);
        assertThat(actualResourceEntry.getValue()).isEqualTo("TDUF TEST");
    }

    @Test
    public void fixAllContentsObjects_whenOneError_asResourceItemCountMismatch_shouldCompleteMissingLocale() {
        // GIVEN
        List<DbDto> dbDtos = createDatabaseObjectsWithUnconsistentResourceEntryCount();

        Map<Locale, Integer> perLocaleCountInfo = new HashMap<>();
        perLocaleCountInfo.put(Locale.CHINA, 0);
        perLocaleCountInfo.put(Locale.FRANCE, 1);
        perLocaleCountInfo.put(Locale.UNITED_STATES, 1);

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, Topic.AFTER_MARKET_PACKS);
        info.put(PER_LOCALE_COUNT, perLocaleCountInfo);

        List<IntegrityError> integrityErrors = asList(IntegrityError.builder().ofType(RESOURCE_ITEMS_COUNT_MISMATCH).addInformations(info).build());


        // WHEN
        DatabaseIntegrityFixer integrityFixer = DatabaseIntegrityFixer.load(dbDtos, integrityErrors);
        List<IntegrityError> actualRemainingErrors = integrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = integrityFixer.getFixedDbDtos();


        // THEN
        assertThat(actualRemainingErrors).isEmpty();

        assertThat(fixedDatabaseObjects).isNotEmpty();

        DbResourceDto.Entry actualResourceEntry = searchResourceEntry("000", Topic.AFTER_MARKET_PACKS, Locale.CHINA, fixedDatabaseObjects);
        assertThat(actualResourceEntry.getValue()).isEqualTo("TDUF TEST");
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

    private static DbStructureDto createDefaultStructureObject() {
        return DbStructureDto.builder()
                .forReference("1")
                .forTopic(Topic.ACHIEVEMENTS)
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
                .forTopic(Topic.AFTER_MARKET_PACKS)
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

    private static DbResourceDto.Entry searchResourceEntry(String reference, Topic topic, Locale locale, List<DbDto> databaseObjects) {
        return databaseObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findFirst().get().getResources().stream()

                .filter((resourceObject) -> resourceObject.getLocale() == locale)

                .findFirst().get().getEntries().stream()

                .filter((entry) -> entry.getReference().equals(reference))

                .findFirst().orElse(null);
    }

    private static List<DbDataDto.Entry> searchContentsEntries(Topic topic, List<DbDto> databaseObjects) {
        return databaseObjects.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findFirst().get().getData().getEntries();
   }
}