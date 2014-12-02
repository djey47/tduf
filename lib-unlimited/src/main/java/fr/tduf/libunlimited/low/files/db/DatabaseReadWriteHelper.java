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
        String databaseFolderName = "D:\\Jeux\\Test Drive Unlimited\\Euro\\NoBnk\\Database";
        String outputFolderSuffix = "tdu-database-dump";

        File outputDirectory = new File(outputFolderSuffix);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();

        }
        Path outputPath = outputDirectory.toPath();

        for(DbDto.Topic currentTopic : DbDto.Topic.values()) {
            List<String> contentLines = parseTopicContentsFromDirectory(currentTopic, databaseFolderName);

            List<List<String>> resources = parseTopicResourcesFromDirectory(currentTopic, databaseFolderName);

            DbParser dbParser = DbParser.load(contentLines, resources);

            DbDto dbDto = dbParser.parseAll();

            System.out.println("Parsing done for topic: " + currentTopic);
            System.out.println("Content line count: " + dbParser.getContentLineCount());
            System.out.println("Resource count: " + dbParser.getResourceCount());
            System.out.println("Integrity errors: " + dbParser.getIntegrityErrors());

            DbWriter dbWriter = DbWriter.load(dbDto);

            dbWriter.writeAllAsJson(outputPath.toString());

            System.out.println("Writing done for topic: " + currentTopic);
            System.out.println("Location: " + outputPath + File.pathSeparator + currentTopic.getLabel() + ".*");
        }
    }

    /**
     * Reads database contents from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db file
     * @return all lines in database file.
     */
    public static List<String> parseTopicContentsFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
        return parseLinesInFile(topic.getLabel(), databaseDirectory, "db", "UTF-8");
    }

    /**
     * Reads database resources from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU resources from
     * @param databaseDirectory : location of database resources as fr,it,ge... files
     * @return All existing resources.
     */
    public static List<List<String>> parseTopicResourcesFromDirectory(DbDto.Topic topic, String databaseDirectory) throws FileNotFoundException {
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
            // TODO handle non-existent file
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