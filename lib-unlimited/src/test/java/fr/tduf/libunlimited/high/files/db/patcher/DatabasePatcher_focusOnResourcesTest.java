package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.helper.FilesHelper.readObjectFromJsonResourceFile;
import static fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher_commonTest.createPatcher;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BOTS;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DatabasePatcher_focusOnResourcesTest {
    @Test
     void apply_whenUpdateResourcesPatch_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);


        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, FRANCE)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).contains("Cindy");
    }

    @Test
     void apply_whenUpdateResourcesPatch_forAllLocales_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources-all.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, FRANCE)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).contains("Cindy");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, Locale.ITALY)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, Locale.ITALY)).contains("Cindy");
    }

    @Test
     void apply_whenUpdateResourcesPatch_forAllLocales_withDefaultLocale_shouldAddAndUpdateEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources-all-any.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, FRANCE)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).contains("Cindy");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, Locale.ITALY)).contains("Brian Molko");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, Locale.ITALY)).contains("Cindy");
    }

    @Test
     void apply_whenUpdateResourcesPatch_forAllLocales_andStrictMode_shouldOnlyAddEntry() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto updateResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources-all-strict.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(updateResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, FRANCE)).contains("Brian");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).contains("Cindy");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("54367256", BOTS, Locale.ITALY)).contains("Brian");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, Locale.ITALY)).contains("Cindy");
    }

    @Test
     void apply_whenDeleteResourcesPatch_shouldRemoveExistingItem() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteResources.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);
        databaseObject.getResource().getEntryByReference("60367256")
                .orElseThrow(() -> new IllegalStateException("Resource entry should exist"))
                .setValueForLocale("SpikeFR", FRANCE);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", BOTS, FRANCE)).contains("Spike");
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).isEmpty();
    }

    @Test
     void apply_whenDeleteResourcesPatch_forAllLocales_shouldRemoveExistingEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteResources-all.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", BOTS, FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", BOTS, Locale.ITALY)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, Locale.ITALY)).isEmpty();
    }

    @Test
     void apply_whenDeleteResourcesPatch_forAllLocales_withSpecialAny_shouldRemoveExistingEntries() throws IOException, URISyntaxException, ReflectiveOperationException {
        // GIVEN
        DbPatchDto deleteResourcesPatch = readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/deleteResources-all-any.mini.json");
        DbDto databaseObject = DatabaseHelper.createDatabaseTopicForReadOnly(BOTS);

        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));


        // WHEN
        patcher.apply(deleteResourcesPatch);


        // THEN
        BulkDatabaseMiner databaseMiner = BulkDatabaseMiner.load(singletonList(databaseObject));
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", BOTS, FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, FRANCE)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("60367256", BOTS, Locale.ITALY)).isEmpty();
        assertThat(databaseMiner.getLocalizedResourceValueFromTopicAndReference("33333333", BOTS, Locale.ITALY)).isEmpty();
    }
}
