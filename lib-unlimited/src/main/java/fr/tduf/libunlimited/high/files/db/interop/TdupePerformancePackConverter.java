package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Utility class allowing to convert performance packs between TDUPE and TDUF systems.
 */
public class TdupePerformancePackConverter {

    /**
     *
     * @param carPhysicsDataLine
     * @param carPhysicsRef
     * @param carPhysicsTopicObject
     * @return
     */
    public static DbPatchDto tdupkToJson(String carPhysicsDataLine, Optional<String> carPhysicsRef, DbDto carPhysicsTopicObject) {
        requireNonNull(carPhysicsDataLine, "Line from Performance Pack is required.");
        requireNonNull(carPhysicsTopicObject, "CarPhysicsData topic object is required.");

        return null;
    }
}
