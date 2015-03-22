package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Used to apply patchs to an existing database.
 */
// TODO handle List of dtos instead of single one
// TODO implement all locales updates and deletions
public class DatabasePatcher {

    private final DbDto databaseObject;

    private final BulkDatabaseMiner databaseMiner;

    private DatabasePatcher(DbDto databaseObject) {
        this.databaseObject = databaseObject;
        this.databaseMiner = BulkDatabaseMiner.load(asList(databaseObject));
    }

    /**
     * Unique entry point.
     * @param databaseObject    : database to be patched.
     * @return a patcher instance.
     */
    public static DatabasePatcher prepare(DbDto databaseObject) {
        return new DatabasePatcher(requireNonNull(databaseObject, "A database object is required."));
    }

    /**
     * Execute provided patch onto current database.
     */
    public void apply(DbPatchDto patchObject) {
        requireNonNull(patchObject, "A patch object is required.");

        patchObject.getChanges()

                .forEach(this::applyChange);
    }

    private void applyChange(DbPatchDto.DbChangeDto changeObject) {

        DbPatchDto.DbChangeDto.ChangeTypeEnum changeType = changeObject.getType();

        if (changeType == UPDATE_RES) {
            addOrUpdateResources(changeObject);
        }
    }

    private void addOrUpdateResources(DbPatchDto.DbChangeDto changeObject) {
        DbDto.Topic topic = changeObject.getTopic();
        DbResourceDto.Locale locale = changeObject.getLocale();
        String ref = changeObject.getRef();
        String value = changeObject.getValue();

        Optional<DbResourceDto.Entry> potentialResourceEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);

        if (potentialResourceEntry.isPresent()) {
            potentialResourceEntry.get().setValue(value);
        } else {
            DbResourceDto localeResources = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
            localeResources.getEntries().add(DbResourceDto.Entry.builder()
                    .forReference(ref)
                    .withValue(value)
                    .build());
        }
    }

    DbDto getDatabaseObject() {
        return databaseObject;
    }
}