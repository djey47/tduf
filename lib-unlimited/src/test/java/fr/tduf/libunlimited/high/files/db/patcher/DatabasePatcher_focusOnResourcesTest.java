package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher_commonTest.createPatcher;
import static fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher_commonTest.readObjectFromResource;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.StrictAssertions.assertThat;

public class DatabasePatcher_focusOnResourcesTest {

    @Before
    public void setUp() throws ReflectiveOperationException {
        BulkDatabaseMiner.clearAllCaches();
    }

    @Test
    public void apply_whenUpdateResourcesPatch_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).contains("Cindy");
    }

    @Test
    public void apply_whenUpdateResourcesPatch_forAllLocales_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/updateResources-all.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).contains("Cindy");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY)).contains("Cindy");
    }

    @Test
    public void apply_whenDeleteResourcesPatch_shouldRemoveExistingEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteResources.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).isEmpty();
    }

    @Test
    public void apply_whenDeleteResourcesPatch_forAllLocales_shouldRemoveExistingEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromResource(DbPatchDto.class, "/db/patch/deleteResources-all.mini.json");
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", DbDto.Topic.BOTS, DbResourceDto.Locale.ITALY)).isEmpty();
    }
}
