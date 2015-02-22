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

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

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

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
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

        HashMap<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
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

    private static List<DbDto> createDefaultDatabaseObjects() {
        List<DbDto> dbDtos = new ArrayList<>();
        dbDtos.add(DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(Topic.ACHIEVEMENTS)
                        .build())
                .withData(DbDataDto.builder()
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(Locale.FRANCE)
                        .build())
                .build());
        dbDtos.add(DbDto.builder()
                .withStructure(DbStructureDto.builder()
                        .forTopic(Topic.AFTER_MARKET_PACKS)
                        .build())
                .withData(DbDataDto.builder()
                        .build())
                .addResource(DbResourceDto.builder()
                        .withLocale(Locale.FRANCE)
                        .build())
                .build());
        return dbDtos;
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
}