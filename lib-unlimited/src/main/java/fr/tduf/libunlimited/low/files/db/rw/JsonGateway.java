package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Class to convert extracted tdu database to json and vice-versa.
 * Relies on {@link DatabaseReadWriteHelper} class for lower level ops.
 */
public class JsonGateway {

    /**
     * Converts extracted TDU database to JSON files.
     * @param sourceDatabaseDirectory   : directory where extracted TDU database files are located
     * @param targetJsonDirectory       : directory where JSON files will be created
     * @param withClearContents         : if true, TDU database files are in an encrypted state, false otherwise
     * @param missingTopicContents      : a list which will contain topic whose contents can't be found
     * @param integrityErrors           : a list which will contain database aprsing errors.
     * @return list of written file names.
     * @throws IOException
     */
    public static List<String> dump(String sourceDatabaseDirectory, String targetJsonDirectory, boolean withClearContents, List<DbDto.Topic> missingTopicContents, Set<IntegrityError> integrityErrors) throws IOException {
        requireNonNull(missingTopicContents, "A list for missing topics is required.");
        requireNonNull(integrityErrors, "A list for integrity errors is required.");

        List<String> writtenFileNames = new ArrayList<>();

        Stream.of(DbDto.Topic.values())

                .parallel()

                .forEach((topic) -> {
                    try {
                        Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(topic, sourceDatabaseDirectory, withClearContents, integrityErrors);
                        if (potentialDbDto.isPresent()) {
                            DatabaseReadWriteHelper.writeDatabaseTopicToJson(potentialDbDto.get(), targetJsonDirectory)

                                    .ifPresent(writtenFileNames::add);
                        } else {
                            missingTopicContents.add(topic);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to dump database topic: " + topic);
                    }
                });

        return writtenFileNames;
    }

    /**
     * Converts JSON database to TDU extracted files.
     * @param sourceJsonDirectory       : directory where JSON files are located
     * @param targetDatabaseDirectory   : directory where TDU database files will be created
     * @param withClearContents         : if true, TDU database files will be encrypted, false otherwise
     * @param missingTopicContents      : a list which will contain topic whose contents can't be found.
     * @throws IOException
     */
    public static List<String> gen(String sourceJsonDirectory, String targetDatabaseDirectory, boolean withClearContents, List<DbDto.Topic> missingTopicContents) throws IOException {
        requireNonNull(missingTopicContents, "A list for missing topics is requried.");

        List<String> writtenFileNames = new ArrayList<>();
        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {

            Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(currentTopic, sourceJsonDirectory);
            if (!potentialDbDto.isPresent()) {
                missingTopicContents.add(currentTopic);
                continue;
            }

            writtenFileNames.addAll(DatabaseReadWriteHelper.writeDatabaseTopic(potentialDbDto.get(), targetDatabaseDirectory, withClearContents));
        }

        return writtenFileNames;
    }
}
