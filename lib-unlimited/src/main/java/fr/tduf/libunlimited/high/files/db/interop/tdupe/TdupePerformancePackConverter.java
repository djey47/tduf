package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert performance packs between TDUPE and TDUF systems.
 */
public class TdupePerformancePackConverter {
    private static final String REGEX_SEPARATOR_ITEMS = ";";

    private static final Set<Integer> FIELD_RANKS_NON_PHYSICAL = new HashSet<>(asList(1, 2, 3, 4, 7, 9, 10, 11, 12, 13, 14, 100, 101, 102, 103));

    private TdupePerformancePackConverter() {}

    /**
     * Converts a performance pack line (aka. CarPhysics entry)
     * @param carPhysicsDataLine    : performance pack value
     * @param carPhysicsRef         : vehicle slot reference, can be null
     * @param carPhysicsTopicObject : actual car physics topic in database
     * @return a TDUF mini patch object.
     */
    public static DbPatchDto tdupkToJson(String carPhysicsDataLine, String carPhysicsRef, DbDto carPhysicsTopicObject) {
        requireNonNull(carPhysicsDataLine, "Line from Performance Pack is required.");
        requireNonNull(carPhysicsTopicObject, "CarPhysicsData topic object is required.");

        DbPatchDto.DbChangeDto changeObject = getChangeObjectForContentsUpdate(carPhysicsDataLine, carPhysicsTopicObject, carPhysicsRef);

        return DbPatchDto.builder()
                .addChanges(singletonList(changeObject))
                .build();
    }

    private static DbPatchDto.DbChangeDto getChangeObjectForContentsUpdate(String contentsEntry, DbDto carPhysicsTopicObject, String carPhysicsRef) {

        List<String> packValues = asList(contentsEntry.split(REGEX_SEPARATOR_ITEMS));
        String slotReference = ofNullable(carPhysicsRef).orElse(packValues.get(0));

        Optional<ContentEntryDto> carPhysicsEntry = BulkDatabaseMiner.load(singletonList(carPhysicsTopicObject))
                .getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA);

        if (carPhysicsEntry.isPresent()) {
            List<DbFieldValueDto> physicsValues = getPartialChangesFromPack(packValues);
            return DbPatchDto.DbChangeDto.builder()
                    .withType(UPDATE)
                    .forTopic(CAR_PHYSICS_DATA)
                    .asReference(slotReference)
                    .withPartialEntryValues(physicsValues)
                    .build();
        }

        packValues.set(0, slotReference);
        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReference(slotReference)
                .withEntryValues(packValues)
                .build();
    }

    private static List<DbFieldValueDto> getPartialChangesFromPack(List<String> packValues) {
        AtomicInteger rank = new AtomicInteger(0);
        return packValues.stream()

                .filter(packValue -> !FIELD_RANKS_NON_PHYSICAL.contains(rank.incrementAndGet()))

                .map(physicsValue -> DbFieldValueDto.fromCouple(rank.get(), physicsValue))

                .collect(toList());
    }
}
