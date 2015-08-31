package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.interop.TdupePerformancePackConverter;
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

    private DatabasePatcher databasePatcher;

    /**
     * Loads a TDUPE Performance Pack File and applies contents to specified content entry
     * @param entryId               : internal identifier of entry to update
     * @param performancePackFile   : location of performance pack file (.tdupk) to be applied.
     */
    public void applyPerformancePackToEntryWithIdentifier(long entryId, String performancePackFile) {
        String packLine = readLineFromPerformancePack(performancePackFile);
        checkCarPhysicsDataLine(packLine);

        DbDto carPhysicsDataTopicObject = databaseMiner.getDatabaseTopic(CAR_PHYSICS_DATA).get();
        Optional<String> potentialCarPhysicsRef = databaseMiner.getContentEntryReferenceWithInternalIdentifier(entryId, CAR_PHYSICS_DATA);
        DbPatchDto patchObject = TdupePerformancePackConverter.tdupkToJson(packLine, potentialCarPhysicsRef, carPhysicsDataTopicObject);

        databasePatcher.apply(patchObject);
    }

    @Override
    protected void postPrepare() {
        try {
            databasePatcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseObjects());
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Unable to initialize database patcher for TDUPE Gateway.", roe);
        }
    }

    static void checkCarPhysicsDataLine(String carPhysicsDataLine) {
        Pattern linePattern = Pattern.compile("^([0-9\\-\\.,]*;){103}$");

        if (!linePattern.matcher(carPhysicsDataLine).matches()) {
            throw new RuntimeException("Unrecognized Car Physics line: " + carPhysicsDataLine);
        }
    }

    private static String readLineFromPerformancePack(String ppFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(ppFile));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to read performance pack file: " + ppFile, ioe);
        }

        return lines.get(0);
    }
}