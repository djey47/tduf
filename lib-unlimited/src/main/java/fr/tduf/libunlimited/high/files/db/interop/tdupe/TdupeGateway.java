package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;

/**
 * Component to interact with TDUPE features.
 */
public class TdupeGateway extends AbstractDatabaseHolder {

    public static final String EXTENSION_PERFORMANCE_PACK = "tdupk";

    private DatabasePatcher databasePatcher;

    /**
     * Loads a TDUPE Performance Pack File and applies contents to specified content entry
     * @param entryId               : internal identifier of entry to update
     * @param performancePackFile   : location of performance pack file (.tdupk) to be applied.
     */
    public void applyPerformancePackToEntryWithIdentifier(int entryId, String performancePackFile) {
        Optional<String> potentialCarPhysicsRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(entryId, CAR_PHYSICS_DATA);
        applyPerformancePackToEntryWithReference(potentialCarPhysicsRef, performancePackFile);
    }

    /**
     * Loads a TDUPE Performance Pack File and applies contents
     * @param potentialCarPhysicsRef    : reference of entry to update, if absent first item of pack will be used instead
     * @param performancePackFile       : location of performance pack file (.tdupk) to be applied.
     */
    public void applyPerformancePackToEntryWithReference(Optional<String> potentialCarPhysicsRef, String performancePackFile) {
        String packLine = readLineFromPerformancePack(performancePackFile);
        checkCarPhysicsDataLine(packLine);

        DbDto carPhysicsDataTopicObject = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("Car physics topic absent from database"));
        DbPatchDto patchObject = TdupePerformancePackConverter.tdupkToJson(packLine, potentialCarPhysicsRef, carPhysicsDataTopicObject);

        databasePatcher.apply(patchObject);
    }

    @Override
    protected void postPrepare() {
        try {
            databasePatcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseObjects());
        } catch (ReflectiveOperationException roe) {
            throw new IllegalStateException("Unable to initialize database patcher for TDUPE Gateway.", roe);
        }
    }

    static void checkCarPhysicsDataLine(String carPhysicsDataLine) {
        Pattern linePattern = Pattern.compile("^([0-9\\-.,]*;){103}$");

        if (!linePattern.matcher(carPhysicsDataLine).matches()) {
            throw new IllegalArgumentException("Unrecognized Car Physics line: " + carPhysicsDataLine);
        }
    }

    private static String readLineFromPerformancePack(String ppFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(ppFile));
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to read performance pack file: " + ppFile, ioe);
        }

        return lines.get(0);
    }
}
