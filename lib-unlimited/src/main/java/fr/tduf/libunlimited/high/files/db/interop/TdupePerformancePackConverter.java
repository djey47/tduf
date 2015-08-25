package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Utility class allowing to convert performance packs between TDUPE and TDUF systems.
 */
public class TdupePerformancePackConverter {

    private static final String REGEX_SEPARATOR_ITEMS = ";";

    /**
     * Converts a performance pack line (aka. CarPhysics entry)
     * @param carPhysicsDataLine
     * @param carPhysicsRef
     * @param carPhysicsTopicObject
     * @return
     */
    public static DbPatchDto tdupkToJson(String carPhysicsDataLine, Optional<String> carPhysicsRef, DbDto carPhysicsTopicObject) {
        requireNonNull(carPhysicsDataLine, "Line from Performance Pack is required.");
        requireNonNull(carPhysicsTopicObject, "CarPhysicsData topic object is required.");

        // TODO keep actual contents into account if available
        DbPatchDto.DbChangeDto changeObject = getChangeObjectForContentsUpdate(carPhysicsDataLine);

        return DbPatchDto.builder()
                .addChanges(singletonList(changeObject))
                .build();
    }

    // TODO move to Helper
    private static DbPatchDto.DbChangeDto getChangeObjectForContentsUpdate(String contentsEntry) {
        List<String> values = asList(contentsEntry.split(REGEX_SEPARATOR_ITEMS));

        return DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReference(values.get(0))
                .withEntryValues(values)
                .build();
    }
}
