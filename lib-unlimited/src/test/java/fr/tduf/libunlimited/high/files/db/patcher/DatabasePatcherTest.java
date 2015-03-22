package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
        DbDto databaseObject = createDefaultDatabaseObject();

        // WHEN
        DatabasePatcher patcher = DatabasePatcher.prepare(databaseObject);

        // THEN
        assertThat(patcher).isNotNull();
        assertThat(patcher.getDatabaseObject()).isSameAs(databaseObject);
    }

    @Test(expected = NullPointerException.class)
    public void apply_whenNullPatchObject_shouldThrowException() {
        // GIVEN-WHEN
        DatabasePatcher.prepare(createDefaultDatabaseObject()).apply(null);

        // THEN: NPE
    }

    @Test
    public void apply_whenUpdateResourcesPatch_shouldAddAndUpdateEntries() throws IOException, URISyntaxException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/dumped/TDU_Bots.json");
        DatabasePatcher patcher = DatabasePatcher.prepare(databaseObject);


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(asList(databaseObject));

        Optional<DbResourceDto.Entry> actualUpdatedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference("54367256", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);
        assertThat(actualUpdatedEntry).isPresent();
        assertThat(actualUpdatedEntry.get().getValue()).isEqualTo("Brian Molko");

        Optional<DbResourceDto.Entry> actualAddedEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE);
        assertThat(actualAddedEntry).isPresent();
        assertThat(actualAddedEntry.get().getValue()).isEqualTo("Cindy");
    }

    private static DbDto createDefaultDatabaseObject() {
        return DbDto.builder().build();
    }

    private static <T> T readObjectFromResource(Class<T> objectClass, String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), objectClass);
    }
}