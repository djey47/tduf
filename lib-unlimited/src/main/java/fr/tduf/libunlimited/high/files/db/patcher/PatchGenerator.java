package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

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
    // TODO handle local resources
    // TODO handle remote resources
    // TODO handle remote contents ref
    // TODO reduce method size
    public DbPatchDto makePatch(DbDto.Topic topic, ReferenceRange range) {
        requireNonNull(topic, "A database topic is required.");
        requireNonNull(range, "A reference range is required.");

        this.topicObject = checkTopic(topic);

        List<DbPatchDto.DbChangeDto> changesObjects = new ArrayList<>();

        OptionalInt potentialRefFieldRank = BulkDatabaseMiner.getUidFieldRank(this.topicObject.getStructure().getFields());
        if (potentialRefFieldRank.isPresent()) {
            changesObjects = this.topicObject.getData().getEntries().stream()

                    .filter((entry) -> {
                        String entryRef = getEntryRef(entry, potentialRefFieldRank.getAsInt());
                        return range.accepts(entryRef);
                    })

                    .map((acceptedEntry) -> {

                        List<String> entryValues = acceptedEntry.getItems().stream()

                                .map(DbDataDto.Item::getRawValue)

                                .collect(toList());

                        return DbPatchDto.DbChangeDto.builder()

                                    .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)

                                    .forTopic(topic)

                                    .asReference(getEntryRef(acceptedEntry, potentialRefFieldRank.getAsInt()))

                                    .withEntryValues(entryValues)

                                    .build();
                    })

                    .collect(toList());
        }

        return DbPatchDto.builder()
                .addChanges(changesObjects)
                .build();
    }

    private DbDto checkTopic(DbDto.Topic topic) {
        Optional<DbDto> potentielTopicObject = databaseMiner.getDatabaseTopic(topic);

        if (!potentielTopicObject.isPresent()) {
            throw new IllegalArgumentException("Topic not found in provided database: " + topic);
        }

        return potentielTopicObject.get();
    }

    private static String getEntryRef(DbDataDto.Entry entry, int refFieldRank) {
        // TODO move to miner ?
        return entry.getItems().stream()

                .filter((item) -> item.getFieldRank() == refFieldRank)

                .findAny().get().getRawValue();
    }

    DbDto getTopicObject() {
        return topicObject;
    }
}