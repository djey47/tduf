package fr.tduf.libunlimited.low.files.db;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;
import fr.tduf.libunlimited.low.files.db.writer.DbWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Class providing methods to manage Database read/write ops.
 */
public class DatabaseReadWriteHelper {

    /**
     * Reads all database contents (+resources) from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db + fr,it,ge... files
     * @param integrityErrors   : list of database errors, encountered when parsing.
     * @return a global object for topic.
     * @throws FileNotFoundException
     */
    public static DbDto readDatabase(DbDto.Topic topic, String databaseDirectory, List<IntegrityError> integrityErrors) throws FileNotFoundException {
        Objects.requireNonNull(integrityErrors);

        List<String> contentLines = parseTopicContentsFromDirectory(topic, databaseDirectory);
        if(contentLines.isEmpty()) {
            return null;
        }

        List<List<String>> resources = parseTopicResourcesFromDirectory(topic, databaseDirectory);
        resources.stream()

                .filter(List::isEmpty)

                .forEach(resourceContents -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("Topic", topic);

                    IntegrityError integrityError = IntegrityError.builder()
                            .ofType(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND)
                            .addInformations(info)
                            .build();

                    integrityErrors.add(integrityError);
                });

        DbParser dbParser = DbParser.load(contentLines, resources);

        DbDto dbDto = dbParser.parseAll();

        integrityErrors.addAll(dbParser.getIntegrityErrors());

        return dbDto;
    }

    /**
     * Writes all database contents (+resources) from specified topic into outputDirectory.
     * @param dbDto             : topic contents to be written
     * @param outputDirectory   : location of generated files
     * @throws FileNotFoundException
     */
    public static void writeDatabaseToJson(DbDto dbDto, String outputDirectory) throws IOException {
        DbWriter dbWriter = DbWriter.load(dbDto);

        dbWriter.writeAllAsJson(outputDirectory);
    }

    static List<String> parseTopicContentsFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
        return parseLinesInFile(topic.getLabel(), databaseDirectory, "db", "UTF-8");
    }

    static List<List<String>> parseTopicResourcesFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
        List<List<String>> resources = new ArrayList<>();

        for (DbResourceDto.Locale currentLocale : DbResourceDto.Locale.values()) {
            resources.add(parseLinesInFile(topic.getLabel(), databaseDirectory, currentLocale.getCode(), "UTF-16"));
        }

        return resources;
    }

    private static List<String> parseLinesInFile(String topicLabel, String databaseDirectory, String extension, String encoding) throws FileNotFoundException {
        List<String> resourceLines = new ArrayList<>() ;

        String inputFileName = String.format("%s%s%s.%s", databaseDirectory, File.separator, topicLabel, extension);
        File inputFile = new File(inputFileName);

        if (!inputFile.exists()) {
            // Returns empty contents so far
            return resourceLines;
        }

        Scanner scanner = new Scanner(inputFile, encoding) ;
        scanner.useDelimiter("\r\n");

        while(scanner.hasNext()) {
            resourceLines.add(scanner.next());
        }
        return resourceLines;
    }
}