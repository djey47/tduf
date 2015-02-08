package fr.tduf.libunlimited.low.files.db;

import fr.tduf.libunlimited.low.files.common.crypto.CryptoHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;
import fr.tduf.libunlimited.low.files.db.writer.DbWriter;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Class providing methods to manage Database read/write ops.
 */
public class DatabaseReadWriteHelper {

    private static final String EXTENSION_DB_CONTENTS = "db";
    private static final String EXTENSION_JSON = "json";

    private static final String ENCODING_UTF_8 = "UTF-8";
    private static final String ENCODING_UTF_16 = "UTF-16";

    /**
     * Reads all database contents (+resources) from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db + fr,it,ge... files
     * @param withClearContents : true indicates contents do not need to be decrypted before processing, false otherwise.
     * @param integrityErrors    : list of database errors, encountered when parsing.  @return a global object for topic.
     * @throws FileNotFoundException
     */
    public static DbDto readDatabase(DbDto.Topic topic, String databaseDirectory, boolean withClearContents, List<IntegrityError> integrityErrors) throws IOException {
        Objects.requireNonNull(integrityErrors, "A list (even empty) must be provided.");

        String contentsFileName = checkDatabaseContents(topic, databaseDirectory, integrityErrors);
        if (contentsFileName == null) {
            return null;
        }
        contentsFileName = prepareClearContentsIfNecessary(contentsFileName, withClearContents, integrityErrors);
        if (contentsFileName == null) {
            return null;
        }

        List<String> contentLines = parseTopicContentsFromFile(contentsFileName);
        if(contentLines.isEmpty()) {
            return null;
        }
        List<List<String>> resourcesLines = parseTopicResourcesFromDirectoryAndCheck(topic, databaseDirectory, integrityErrors);

        DbParser dbParser = DbParser.load(contentLines, resourcesLines);
        DbDto dbDto = dbParser.parseAll();
        integrityErrors.addAll(dbParser.getIntegrityErrors());

        return dbDto;
    }

    /**
     * Reads all database contents (+resources) in JSON format from specified topic into jsonDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param jsonDirectory : location of json files
     */
    public static DbDto readDatabaseFromJson(DbDto.Topic topic, String jsonDirectory) throws IOException {

        String jsonFileName = getDatabaseFileName(topic.getLabel(), jsonDirectory, EXTENSION_JSON);
        File jsonFile = new File(jsonFileName);

        if (!jsonFile.exists()) {
            return null;
        }

        return new ObjectMapper().readValue(jsonFile, DbDto.class);
    }

    /**
     * Writes all database contents (+resources) as TDU format from specified topic into outputDirectory.
     * @param dbDto             : topic contents to be written
     * @param outputDirectory   : location of generated files
     * @return a list of written TDU files
     * @throws FileNotFoundException
     */
    public static List<String> writeDatabase(DbDto dbDto, String outputDirectory, boolean withClearContents) throws IOException {

        DbWriter writer = DbWriter.load(dbDto);
        List<String> writtenFileNames = writer.writeAll(outputDirectory);

        if (withClearContents) {
            return writtenFileNames;
        }

        // TODO Ensure file to be encrypted is contents and not resource !!
        String contentsFileName = writtenFileNames.get(0);
        String tempDirectory = Files.createTempDirectory("libUnlimited-databaseRW").toString();

        File inputFile = new File(contentsFileName);
        File outputFile = new File(tempDirectory, inputFile.getName());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath()));
        ByteArrayOutputStream outputStream = CryptoHelper.encryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
        Files.write(outputFile.toPath(), outputStream.toByteArray(), StandardOpenOption.CREATE);

        Path source = outputFile.toPath();
        Path target = Paths.get(outputDirectory, source.getFileName().toString());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        return writtenFileNames;
    }

    /**
     * Writes all database contents (+resources) as JSON format from specified topic into outputDirectory.
     * @param dbDto             : topic contents to be written
     * @param outputDirectory   : location of generated files
     * @return name of written JSON file
     * @throws FileNotFoundException
     */
    public static String writeDatabaseToJson(DbDto dbDto, String outputDirectory) throws IOException {
        DbWriter dbWriter = DbWriter.load(dbDto);

        return dbWriter.writeAllAsJson(outputDirectory);
    }

    static List<String> parseTopicContentsFromFile(String contentsFileName) throws FileNotFoundException {
        return parseLinesInFile(contentsFileName, ENCODING_UTF_8);
    }

    static List<List<String>> parseTopicResourcesFromDirectoryAndCheck(DbDto.Topic topic, String databaseDirectory, List<IntegrityError> integrityErrors) throws FileNotFoundException {
        List<List<String>> resourcesLines = new ArrayList<>();

        for (DbResourceDto.Locale currentLocale : DbResourceDto.Locale.values()) {
            String resourceFileName = getDatabaseFileName(topic.getLabel(), databaseDirectory, currentLocale.getCode());
            resourcesLines.add(parseLinesInFile(resourceFileName, ENCODING_UTF_16));
        }

        resourcesLines.stream()

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


        return resourcesLines;
    }

    private static String checkDatabaseContents(DbDto.Topic topic, String databaseDirectory, List<IntegrityError> integrityErrors) throws FileNotFoundException {
        String contentsFileName = getDatabaseFileName(topic.getLabel(), databaseDirectory, EXTENSION_DB_CONTENTS);

        if (new File(contentsFileName).exists()) {
            return contentsFileName;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("Topic", topic);
        IntegrityError integrityError = IntegrityError.builder()
                .ofType(IntegrityError.ErrorTypeEnum.CONTENTS_NOT_FOUND)
                .addInformations(info)
                .build();
        integrityErrors.add(integrityError);

        return null;
    }

    private static List<String> parseLinesInFile(String fileName, String encoding) throws FileNotFoundException {
        List<String> resourceLines = new ArrayList<>() ;

        File inputFile = new File(fileName);

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

    private static String getDatabaseFileName(String topicLabel, String databaseDirectory, String extension) {
        return String.format("%s%s%s.%s", databaseDirectory, File.separator, topicLabel, extension);
    }

    private static String prepareClearContentsIfNecessary(String contentsFileName, boolean withClearContents, List<IntegrityError> integrityErrors) throws IOException {
        if (withClearContents) {
            return contentsFileName;
        }

        Path tempDirectoryPath = Files.createTempDirectory("libUnlimited-databaseRW");

        File inputFile = new File(contentsFileName);
        File outputFile = new File(tempDirectoryPath.toString(), inputFile.getName());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath()));

        try {
            ByteArrayOutputStream outputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
            Files.write(outputFile.toPath(), outputStream.toByteArray(), StandardOpenOption.CREATE);
        } catch (Exception e) {

            Map<String, Object> info = new HashMap<>();
            info.put("File", contentsFileName);
            IntegrityError integrityError = IntegrityError.builder()
                    .ofType(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED)
                    .addInformations(info)
                    .build();
            integrityErrors.add(integrityError);

            return null;
        }

        return outputFile.getPath();
    }
}