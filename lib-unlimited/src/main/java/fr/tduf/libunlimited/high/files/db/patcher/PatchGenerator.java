package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseStructureQueryHelper;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Used to generate patches from an existing database.
 */
public class PatchGenerator extends AbstractDatabaseHolder {

    private DbDto topicObject;

    /**
     * Generates a patch based on current database objects.
     * @param topic : database topic for which the patch should apply
     * @param range : range of reference values for concerned entries.
     * @return a patch object with all necessary instructions.
     */
    // TODO handle remote contents ref
    public DbPatchDto makePatch(DbDto.Topic topic, ReferenceRange range) {
        requireNonNull(topic, "A database topic is required.");
        requireNonNull(range, "A reference range is required.");

        this.topicObject = checkTopic(topic);

        final Map<DbDto.Topic, List<String>> requiredResourceReferences = new HashMap<>();
        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();
        List<DbStructureDto.Field> structureFields = this.topicObject.getStructure().getFields();
        BulkDatabaseMiner.getUidFieldRank(structureFields)

                .ifPresent((refFieldRank -> changesObjects.addAll(

                        makeChangesObjectsForContents(refFieldRank, structureFields, range, requiredResourceReferences)

                )));

        changesObjects.addAll(makeChangesObjectsForResources(topic, requiredResourceReferences));

        return DbPatchDto.builder()
                .addChanges(changesObjects)
                .build();
    }

    private List<DbPatchDto.DbChangeDto> makeChangesObjectsForContents(int refFieldRank, List<DbStructureDto.Field> structureFields, ReferenceRange range, Map<DbDto.Topic, List<String>> requiredLocalResourceReferences) {
        DbDto.Topic topic = this.topicObject.getStructure().getTopic();

        return this.topicObject.getData().getEntries().stream()

            .filter((entry) -> isInRange(entry, refFieldRank, range))

            .map((acceptedEntry) -> createChangeObjectForEntry(topic, acceptedEntry, refFieldRank, structureFields, requiredLocalResourceReferences))

            .collect(toList());
    }

    // TODO extract methods!
    // TODO when all locales have same values for a given ref, generate a single instruction for all locales (reduce instruction count)
    private Set<DbPatchDto.DbChangeDto> makeChangesObjectsForResources(DbDto.Topic topic, Map<DbDto.Topic, List<String>> resourceReferences) {
        return resourceReferences.entrySet().stream()

                .flatMap((resourceEntry) -> Stream.of(DbResourceDto.Locale.values())

                        .flatMap((locale) -> resourceEntry.getValue().stream()

                                    .map( (resourceRef) -> {

                                        String resourceValue = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceRef, resourceEntry.getKey(), locale).get().getValue();
                                        return DbPatchDto.DbChangeDto.builder()
                                                .forTopic(topic)
                                                .forLocale(locale)
                                                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES)
                                                .asReference(resourceRef)
                                                .withValue(resourceValue)
                                                .build();
                                    })
                        )
                )

                .collect(toSet());
    }

    private DbDto checkTopic(DbDto.Topic topic) {
        Optional<DbDto> potentielTopicObject = databaseMiner.getDatabaseTopic(topic);

        if (!potentielTopicObject.isPresent()) {
            throw new IllegalArgumentException("Topic not found in provided database: " + topic);
        }

        return potentielTopicObject.get();
    }

    private static boolean isInRange(DbDataDto.Entry entry, int refFieldRank, ReferenceRange range) {
        String entryRef = BulkDatabaseMiner.getEntryReference(entry, refFieldRank);
        return range.accepts(entryRef);
    }

    // TODO handle case of RESOURCE_REMOTE
    private static DbPatchDto.DbChangeDto createChangeObjectForEntry(DbDto.Topic topic, DbDataDto.Entry entry, int refFieldRank, List<DbStructureDto.Field> structureFields, Map<DbDto.Topic, List<String>> requiredResourceReferences) {
        List<String> entryValues = entry.getItems().stream()

                .map((item) -> {

                    DbStructureDto.Field structureField = DatabaseStructureQueryHelper.getStructureField(item, structureFields);
                    if (DbStructureDto.FieldType.RESOURCE_CURRENT == structureField.getFieldType()
                            || DbStructureDto.FieldType.RESOURCE_CURRENT_AGAIN == structureField.getFieldType()) {

                        if (!requiredResourceReferences.containsKey(topic)) {
                            requiredResourceReferences.put(topic, new ArrayList<>());
                        }
                        requiredResourceReferences.get(topic).add(item.getRawValue());
                    }

                    return item.getRawValue();
                })

                .collect(toList());

        return DbPatchDto.DbChangeDto.builder()
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)
                .forTopic(topic)
                .asReference(BulkDatabaseMiner.getEntryReference(entry, refFieldRank))
                .withEntryValues(entryValues)
                .build();
    }

    DbDto getTopicObject() {
        return topicObject;
    }
}