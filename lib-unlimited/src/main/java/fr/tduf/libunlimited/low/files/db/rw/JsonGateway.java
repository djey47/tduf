package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * @param missingTopicContents      : a list which will contain topic whose contents can't be found.
     * @return list of written file names.
     */
    public static List<String> dump(String sourceDatabaseDirectory, String targetJsonDirectory, boolean withClearContents, List<DbDto.Topic> missingTopicContents) throws IOException {
        requireNonNull(missingTopicContents, "A list for missing topics is requried.");

        List<String> writtenFileNames = new ArrayList<>();
        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {

            Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(currentTopic, sourceDatabaseDirectory, withClearContents, new ArrayList<>());
            if (!potentialDbDto.isPresent()) {
                missingTopicContents.add(currentTopic);
                continue;
            }

            DatabaseReadWriteHelper.writeDatabaseTopicToJson(potentialDbDto.get(), targetJsonDirectory)

                    .ifPresent(writtenFileNames::add);
        }

        return writtenFileNames;
    }
}