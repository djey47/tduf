package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patchs to an existing database.
 */
// TODO WARNING! if using cache on Miner, reset caches after updates !
public class DatabasePatcher extends AbstractDatabaseHolder {

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

        switch (changeType) {
            case UPDATE_RES:
                addOrUpdateResources(changeObject);
                break;
            case DELETE_RES:
                deleteResources(changeObject);
                break;
            case UPDATE:
                addOrUpdateContents(changeObject);
                break;
            case DELETE:
                deleteContents(changeObject);
                break;
        }
    }

    private void deleteContents(DbPatchDto.DbChangeDto changeObject) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        databaseMiner.getContentEntryFromTopicWithReference(changeObject.getRef(), changedTopic)
                .ifPresent((contentEntry) -> {

                    List<DbDataDto.Entry> topicEntries = databaseMiner.getDatabaseTopic(changedTopic).get().getData().getEntries();
                    topicEntries.remove(contentEntry);

                });
    }

    private void addOrUpdateContents(DbPatchDto.DbChangeDto changeObject) {

        DbDto.Topic changedTopic = changeObject.getTopic();
        databaseMiner.getDatabaseTopic(changedTopic)
                .ifPresent((topicObject) -> {

                    List<DbDataDto.Entry> topicEntries = topicObject.getData().getEntries();
                    List<DbDataDto.Item> modifiedItems = createEntryItemsWithValues(topicObject.getStructure().getFields(), changeObject.getValues());

                    Optional<String> potentialRef = Optional.ofNullable(changeObject.getRef());
                    if (potentialRef.isPresent()) {

                        addOrUpdateEntryWithReference(potentialRef.get(), changedTopic, topicEntries, modifiedItems);

                    } else {

                        addEntryToTopic(modifiedItems, topicEntries);

                    }
                });
    }

    private void addOrUpdateEntryWithReference(String ref, DbDto.Topic changedTopic, List<DbDataDto.Entry> topicEntries, List<DbDataDto.Item> modifiedItems) {
        Optional<DbDataDto.Entry> potentialEntry = databaseMiner.getContentEntryFromTopicWithReference(ref, changedTopic);

        if (potentialEntry.isPresent()) {

            potentialEntry.get().setItems(modifiedItems);

        } else {

            addEntryToTopic(modifiedItems, topicEntries);

        }
    }

    private void deleteResources(DbPatchDto.DbChangeDto changeObject) {
        Optional<DbResourceDto.Locale> potentialLocale = Optional.ofNullable(changeObject.getLocale());

        if (potentialLocale.isPresent()) {

            deleteResourcesForLocale(changeObject, potentialLocale.get());

        } else {

            Stream.of(DbResourceDto.Locale.values())

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
        Optional<DbResourceDto.Locale> locale = Optional.ofNullable(changeObject.getLocale());

        if (locale.isPresent()) {

            addOrUpdateResourcesForLocale(changeObject, locale.get());

        } else {

            Stream.of(DbResourceDto.Locale.values())

                    .forEach((currentLocale) -> addOrUpdateResourcesForLocale(changeObject, currentLocale));

        }
    }

    private void addOrUpdateResourcesForLocale(DbPatchDto.DbChangeDto changeObject, DbResourceDto.Locale locale) {
        String ref = changeObject.getRef();
        DbDto.Topic topic = changeObject.getTopic();
        String value = changeObject.getValue();

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

    private static void checkValueCount(List<String> allValues, List<DbStructureDto.Field> structureFields) {
        int structureFieldsSize = structureFields.size();
        if (allValues.size() != structureFieldsSize) {
            throw new IllegalArgumentException("Values count in current patch does not match topic structure: " + allValues.size() + " VS " + structureFieldsSize);
        }
    }

    private static void addEntryToTopic(List<DbDataDto.Item> modifiedItems, List<DbDataDto.Entry> topicEntries) {
        topicEntries.add(DbDataDto.Entry.builder()
                .forId(topicEntries.size())
                .addItems(modifiedItems)
                .build());
    }

    private static List<DbDataDto.Item> createEntryItemsWithValues(List<DbStructureDto.Field> structureFields, List<String> allValues) {
        checkValueCount(allValues, structureFields);

        AtomicInteger fieldIndex = new AtomicInteger();
        return allValues.stream()

                .map((value) -> {
                    DbStructureDto.Field structureField = structureFields.get(fieldIndex.getAndIncrement());
                    return DbDataDto.Item.builder()
                            .fromStructureField(structureField)
                            .withRawValue(value)
                            .build();
                })

                .collect(toList());
    }
}