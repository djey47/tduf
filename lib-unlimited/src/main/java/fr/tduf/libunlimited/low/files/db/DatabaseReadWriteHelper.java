package fr.tduf.libunlimited.low.files.db;

import fr.tduf.libunlimited.low.files.common.crypto.CryptoHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;
import fr.tduf.libunlimited.low.files.db.writer.DbWriter;

import java.io.*;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.util.*;

/**
 * Class providing methods to manage Database read/write ops.
 */
public class DatabaseReadWriteHelper {

    private static final String EXTENSION_DB_CONTENTS = "db";

    /**
     * Reads all database contents (+resources) from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db + fr,it,ge... files
     * @param withClearContents : true indicates contents do not need to be decrypted before processing, false otherwise.
     *@param integrityErrors    : list of database errors, encountered when parsing.  @return a global object for topic.
     * @throws FileNotFoundException
     */
    public static DbDto readDatabase(DbDto.Topic topic, String databaseDirectory, boolean withClearContents, List<IntegrityError> integrityErrors) throws IOException {
        Objects.requireNonNull(integrityErrors);

        //TODO Retrieve file name, Handle topic contents file not found and return null
        String databaseContentsDirectory = databaseDirectory;
        if (!withClearContents) {
            databaseContentsDirectory = prepareClearContents(topic.getLabel(), databaseContentsDirectory);
        }

        List<String> contentLines = parseTopicContentsFromDirectory(topic, databaseContentsDirectory);
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
        return parseLinesInFile(topic.getLabel(), databaseDirectory, EXTENSION_DB_CONTENTS, "UTF-8");
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

        File inputFile = new File(getDatabaseContentsFileName(topicLabel, databaseDirectory, extension));

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

    private static String getDatabaseContentsFileName(String topicLabel, String databaseDirectory, String extension) {
        return String.format("%s%s%s.%s", databaseDirectory, File.separator, topicLabel, extension);
    }

    private static String prepareClearContents(String topicLabel, String databaseDirectory) throws IOException {
        Path tempDirectoryPath = Files.createTempDirectory("libUnlimited-databaseRW");

        File inputFile = new File(getDatabaseContentsFileName(topicLabel, databaseDirectory, EXTENSION_DB_CONTENTS));
        File outputFile = new File(tempDirectoryPath.toString(), inputFile.getName());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath()));
        ByteArrayOutputStream outputStream;
        try {
            outputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
        } catch (InvalidKeyException e) {
            throw new IOException("Should never occur.", e);
        }

        Files.write(outputFile.toPath(), outputStream.toByteArray(), StandardOpenOption.CREATE);

        return tempDirectoryPath.toString();
    }
}