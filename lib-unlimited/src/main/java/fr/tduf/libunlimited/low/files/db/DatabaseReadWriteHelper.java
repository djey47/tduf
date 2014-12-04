package fr.tduf.libunlimited.low.files.db;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;
import fr.tduf.libunlimited.low.files.db.writer.DbWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class providing methods to manage Database read/write ops.
 */
public class DatabaseReadWriteHelper {

    /**
     * Utility entry point - till a CLI comes
     */
    public static void main(String[] args) throws IOException {
        //TODO Set as method args
        String databaseFolderName = "D:\\Jeux\\Test Drive Unlimited\\Euro\\NoBnk\\DataBase";
        String outputFolderSuffix = "tdu-database-dump";

        File outputDirectory = new File(outputFolderSuffix);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();

        }
        Path outputPath = outputDirectory.toPath();

        for(DbDto.Topic currentTopic : DbDto.Topic.values()) {
            DbDto dbDto = readDatabase(currentTopic, databaseFolderName);

            writeDatabaseToJson(outputPath, currentTopic, dbDto);
        }
    }

    /**
     * Reads database contents from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db file
     * @return all lines in database file or empty list, if file for specified topic does not exist.
     */
    public static List<String> parseTopicContentsFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
        return parseLinesInFile(topic.getLabel(), databaseDirectory, "db", "UTF-8");
    }

    /**
     * Reads database resources from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU resources from
     * @param databaseDirectory : location of database resources as fr,it,ge... files
     * @return All existing resources or empty list, if file for specified topic does not exist.
     */
    public static List<List<String>> parseTopicResourcesFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
        List<List<String>> resources = new ArrayList<>();

        for (DbResourceDto.Locale currentLocale : DbResourceDto.Locale.values()) {
            resources.add(parseLinesInFile(topic.getLabel(), databaseDirectory, currentLocale.getCode(), "UTF-16"));
        }

        return resources;
    }

    private static DbDto readDatabase(DbDto.Topic currentTopic, String databaseFolderName) throws FileNotFoundException {
        List<String> contentLines = parseTopicContentsFromDirectory(currentTopic, databaseFolderName);
        if(contentLines.isEmpty()) {
            System.err.println("Database contents not found for topic: " + currentTopic);
            return null;
        }

        List<List<String>> resources = parseTopicResourcesFromDirectory(currentTopic, databaseFolderName);
        resources.stream()

                .filter(List::isEmpty)

                .forEach(resourceContents -> System.out.println("Some of database resources not found for topic: " + currentTopic));

        DbParser dbParser = DbParser.load(contentLines, resources);

        DbDto dbDto = dbParser.parseAll();

        System.out.println("Parsing done for topic: " + currentTopic);

        System.out.println("Content line count: " + dbParser.getContentLineCount());
        System.out.println("Resource count: " + dbParser.getResourceCount());
        System.out.println("Integrity errors: " + dbParser.getIntegrityErrors());

        return dbDto;
    }

    private static void writeDatabaseToJson(Path outputPath, DbDto.Topic currentTopic, DbDto dbDto) throws FileNotFoundException {
        DbWriter dbWriter = DbWriter.load(dbDto);

        dbWriter.writeAllAsJson(outputPath.toString());

        System.out.println("Writing done for topic: " + currentTopic);
        System.out.println("Location: " + outputPath + File.separator + currentTopic.getLabel() + ".json");
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