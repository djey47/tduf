package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.DELETE_RES;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Used to apply patchs to an existing database.
 */
// TODO WARNING! if using cache on Miner, reset caches after updates !
public class DatabasePatcher {

    private final List<DbDto> databaseObjects;

    private final BulkDatabaseMiner databaseMiner;

    private DatabasePatcher(List<DbDto> databaseObjects) {
        this.databaseObjects = databaseObjects;
        this.databaseMiner = BulkDatabaseMiner.load(databaseObjects);
    }

    /**
     * Unique entry point.
     * @param databaseObjects    : database topics to be patched.
     * @return a patcher instance.
     */
    public static DatabasePatcher prepare(List<DbDto> databaseObjects) {
        return new DatabasePatcher(requireNonNull(databaseObjects, "Database objects are required."));
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
        } else if (changeType == DELETE_RES) {
            deleteResources(changeObject);
        }
    }

    private void deleteResources(DbPatchDto.DbChangeDto changeObject) {
        Optional<DbResourceDto.Locale> potentialLocale = Optional.ofNullable(changeObject.getLocale());

        if (potentialLocale.isPresent()) {

            deleteResourcesForLocale(changeObject, potentialLocale.get());

        } else {

            asList(DbResourceDto.Locale.values())
                    .forEach((currentLocale) -> deleteResourcesForLocale(changeObject, currentLocale));

        }
    }

    private void deleteResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale) {
        DbDto.Topic topic = changeObject.getTopic();

        databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(changeObject.getRef(), topic, locale)
                .ifPresent((resourceEntry) -> {
                    DbResourceDto dbResourceDto = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
                    dbResourceDto.getEntries().remove(resourceEntry);
                });
    }

    private void addOrUpdateResources(DbPatchDto.DbChangeDto changeObject) {
        DbDto.Topic topic = changeObject.getTopic();
        Optional<DbResourceDto.Locale> locale = Optional.ofNullable(changeObject.getLocale());
        String ref = changeObject.getRef();
        String value = changeObject.getValue();

        if (locale.isPresent()) {

            addOrUpdateResourcesForLocale(topic, locale.get(), ref, value);

        } else {

            asList(DbResourceDto.Locale.values())
                    .forEach((currentLocale) -> addOrUpdateResourcesForLocale(topic, currentLocale, ref, value));

        }
    }

    // TODO reduce method size by passing patch objet as parameter
    private void addOrUpdateResourcesForLocale(DbDto.Topic topic, DbResourceDto.Locale locale, String ref, String value) {
        Optional<DbResourceDto.Entry> potentialResourceEntry =
                databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(ref, topic, locale);

        if (potentialResourceEntry.isPresent()) {

            potentialResourceEntry.get().setValue(value);

        } else {

            databaseMiner.getResourceFromTopicAndLocale(topic, locale)
                    .ifPresent((localeResources) -> localeResources.getEntries().add(DbResourceDto.Entry.builder()
                                                                                        .forReference(ref)
                                                                                        .withValue(value)
                                                                                        .build()));
        }
    }

    List<DbDto> getDatabaseObjects() {
        return databaseObjects;
    }
}