package fr.tduf.libunlimited.low.files.db.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;

/**
 * Class to convert extracted tdu database to json and vice-versa.
 * Relies on {@link DatabaseReadWriteHelper} class for lower level ops.
 */
public class JsonGateway {
    private static final String THIS_CLASS_NAME = JsonGateway.class.getSimpleName();

    private JsonGateway() {}

    /**
     * Converts extracted TDU database to JSON files.
     * @param sourceDatabaseDirectory   : directory where extracted TDU database files are located
     * @param targetJsonDirectory       : directory where JSON files will be created
     * @param missingTopicContents      : a list which will contain topic whose contents can't be found
     * @param integrityErrors           : a list which will contain database aprsing errors.
     * @return list of written file names.
     */
    public static List<String> dump(String sourceDatabaseDirectory, String targetJsonDirectory, List<DbDto.Topic> missingTopicContents, Set<IntegrityError> integrityErrors) throws IOException {
        requireNonNull(missingTopicContents, "A list for missing topics is required.");
        requireNonNull(integrityErrors, "A list for integrity errors is required.");

        List<String> writtenFileNames = synchronizedList(new ArrayList<>());
        List<DbDto.Topic> missingTopicContentsWhileProcessing = synchronizedList(new ArrayList<>());
        Set<IntegrityError> integrityErrorsWhileProcessing = Collections.synchronizedSet(new HashSet<>());

        DbDto.Topic.valuesAsStream()

                .parallel()

                .forEach(topic -> {
                    try {
                        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(topic, sourceDatabaseDirectory, integrityErrorsWhileProcessing);
                        if (potentialDbDto.isPresent()) {
                            writtenFileNames.addAll(
                                    DatabaseReadWriteHelper.writeDatabaseTopicToJson(potentialDbDto.get(), targetJsonDirectory));
                        } else {
                            missingTopicContentsWhileProcessing.add(topic);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to dump database topic: " + topic);
                    }
                });

        missingTopicContents.addAll(missingTopicContentsWhileProcessing);
        integrityErrors.addAll(integrityErrorsWhileProcessing);

        return new ArrayList<>(writtenFileNames);
    }

    /**
     * Converts JSON database to TDU extracted files.
     * @param sourceJsonDirectory       : directory where JSON files are located
     * @param targetDatabaseDirectory   : directory where TDU database files will be created
     * @param missingTopicContents      : a list which will contain topic whose contents can't be found.
     */
    public static List<String> gen(String sourceJsonDirectory, String targetDatabaseDirectory, List<DbDto.Topic> missingTopicContents) throws IOException {
        requireNonNull(missingTopicContents, "A list for missing topics is requried.");

        List<String> writtenFileNames = synchronizedList(new ArrayList<>());
        List<DbDto.Topic> missingTopicContentsWhileProcessing = synchronizedList(new ArrayList<>());
        DbDto.Topic.valuesAsStream()

                .parallel()

                .forEach(topic -> {
                    try {
                        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(topic, sourceJsonDirectory);
                        if (potentialDbDto.isPresent()) {
                            writtenFileNames.addAll(DatabaseReadWriteHelper.writeDatabaseTopic(potentialDbDto.get(), targetDatabaseDirectory));
                        } else {
                            missingTopicContentsWhileProcessing.add(topic);
                        }
                    } catch (IOException ioe) {
                        Log.error(THIS_CLASS_NAME, ExceptionUtils.getStackTrace(ioe));
                    }
                });

        missingTopicContents.addAll(missingTopicContentsWhileProcessing);

        return new ArrayList<>(writtenFileNames);
    }
}

