package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class allowing to convert performance packs between TDUPE and TDUF systems.
 */
public class TdupePerformancePackConverter {

    private static final String REGEX_SEPARATOR_ITEMS = ";";

    private static final Set<Integer> FIELD_RANKS_NON_PHYSICAL = new HashSet<>(asList(1, 3, 4, 9, 10, 12, 100, 101));

    /**
     * Converts a performance pack line (aka. CarPhysics entry)
     * @param carPhysicsDataLine    : performance pack value
     * @param carPhysicsRef         : vehicle slot reference, not mandatory
     * @param carPhysicsTopicObject : actual car physics topic in database
     * @return a TDUF mini patch object.
     */
    public static DbPatchDto tdupkToJson(String carPhysicsDataLine, Optional<String> carPhysicsRef, DbDto carPhysicsTopicObject) {
        requireNonNull(carPhysicsDataLine, "Line from Performance Pack is required.");
        requireNonNull(carPhysicsTopicObject, "CarPhysicsData topic object is required.");

        DbPatchDto.DbChangeDto changeObject = getChangeObjectForContentsUpdate(carPhysicsDataLine, carPhysicsTopicObject, carPhysicsRef);

        return DbPatchDto.builder()
                .addChanges(singletonList(changeObject))
                .build();
    }

    private static DbPatchDto.DbChangeDto getChangeObjectForContentsUpdate(String contentsEntry, DbDto carPhysicsTopicObject, Optional<String> carPhysicsRef) {

        List<String> packValues = asList(contentsEntry.split(REGEX_SEPARATOR_ITEMS));
        String slotReference = carPhysicsRef.orElse(packValues.get(0));

        Optional<DbDataDto.Entry> carPhysicsEntry = BulkDatabaseMiner.load(singletonList(carPhysicsTopicObject))
                .getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA);

        List<String> itemValues = applyPhysicalChangesToPotentialEntry(packValues, carPhysicsEntry);
        itemValues.set(0, slotReference);

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReference(slotReference)
                .withEntryValues(itemValues)
                .build();
    }

    private static List<String> applyPhysicalChangesToPotentialEntry(List<String> packValues, Optional<DbDataDto.Entry> carPhysicsEntry) {
        if (carPhysicsEntry.isPresent()) {
            return carPhysicsEntry.get().getItems().stream()

                    .map((item) -> getProperRawValue(packValues, item))

                    .collect(toList());
        }

        return packValues;
    }

    private static String getProperRawValue(List<String> packValues, DbDataDto.Item item) {

        int fieldRank = item.getFieldRank();
        String rawValue = packValues.get(fieldRank - 1);

        if (FIELD_RANKS_NON_PHYSICAL.contains(fieldRank)) {
            rawValue = item.getRawValue();
        }

        return rawValue;
    }
}