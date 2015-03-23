package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class DatabasePatcherTest {

    private static final Class<DatabasePatcherTest> thisClass = DatabasePatcherTest.class;

    @Test(expected = NullPointerException.class)
    public void prepare_whenNullDatabaseObject_shouldThrowException() {
        // GIVEN-WHEN
        DatabasePatcher.prepare(null);

        // THEN: NPE
    }

    @Test
    public void prepare_shouldSetDatabaseObject() {
        // GIVEN
        List<DbDto> databaseObjects = createDefaultDatabaseObjects();

        // WHEN
        DatabasePatcher patcher = DatabasePatcher.prepare(databaseObjects);

        // THEN
        assertThat(patcher).isNotNull();
        assertThat(patcher.getDatabaseObjects()).isSameAs(databaseObjects);
    }

    @Test(expected = NullPointerException.class)
    public void apply_whenNullPatchObject_shouldThrowException() {
        // GIVEN-WHEN
        DatabasePatcher.prepare(createDefaultDatabaseObjects()).apply(null);

        // THEN: NPE
    }

    @Test
    public void apply_whenUpdateResourcesPatch_shouldAddAndUpdateEntries() throws IOException, URISyntaxException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = DatabasePatcher.prepare(asList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(asList(databaseObject));
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333", "Cindy");
    }

    @Test
    public void apply_whenUpdateResourcesPatch_forAllLocales_shouldAddAndUpdateEntries() throws IOException, URISyntaxException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources-all.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = DatabasePatcher.prepare(asList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(asList(databaseObject));
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333", "Cindy");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "54367256", "Brian Molko");
        assertResourceEntryPresentAndMatch(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY, "33333333", "Cindy");
    }

    @Test
    public void apply_whenDeleteResourcesPatch_shouldRemoveExistingEntry() throws IOException, URISyntaxException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = DatabasePatcher.prepare(asList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(asList(databaseObject));
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "60367256");
        assertResourceEntryMissing(databaseMiner, DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE, "33333333");
    }

    private static List<DbDto> createDefaultDatabaseObjects() {
        return asList(DbDto.builder().build());
    }

    private static <T> T readObjectFromResource(Class<T> objectClass, String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), objectClass);
    }

    private static void assertResourceEntryPresentAndMatch(BulkDatabaseMiner databaseMiner, DbDto.Topic topic, DbResourceDto.Locale locale, String ref, String value) {
        Optional<DbResourceDto.Entry> actualUpdatedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);
        assertThat(actualUpdatedEntry).isPresent();
        assertThat(actualUpdatedEntry.get().getValue()).isEqualTo(value);
    }

    private static void assertResourceEntryMissing(BulkDatabaseMiner databaseMiner, DbDto.Topic topic, DbResourceDto.Locale locale, String ref) {
        Optional<DbResourceDto.Entry> actualUpdatedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);
        assertThat(actualUpdatedEntry).isEmpty();
    }
}