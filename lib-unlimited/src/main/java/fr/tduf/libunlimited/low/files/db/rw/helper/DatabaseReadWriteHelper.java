package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.low.files.common.crypto.helper.CryptoHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseParser;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseWriter;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing methods to manage Database read/write ops.
 */
public class DatabaseReadWriteHelper {

    public static final String EXTENSION_DB_CONTENTS = "db";
    public static final String EXTENSION_JSON = "json";

    private static final String ENCODING_UTF_8 = "UTF-8";
    private static final String ENCODING_UTF_16 = "UTF-16";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads all database contents (+resources) from specified topic into databaseDirectory.
     * @param topic             : topic to parse TDU contents from
     * @param databaseDirectory : location of database contents as db + fr,it,ge... files
     * @param integrityErrors    : list of database errors, encountered when parsing.  @return a global object for topic.
     * @return empty value if topic could not be read properly.
     * @throws FileNotFoundException
     */
    public static Optional<DbDto> readDatabaseTopic(DbDto.Topic topic, String databaseDirectory, Set<IntegrityError> integrityErrors) throws IOException {
        requireNonNull(integrityErrors, "A list (even empty) must be provided.");

        String contentsFileName = checkDatabaseContents(topic, databaseDirectory, integrityErrors);
        if (contentsFileName == null) {
            return Optional.empty();
        }
        contentsFileName = prepareClearContents(contentsFileName, integrityErrors);
        if (contentsFileName == null) {
            return Optional.empty();
        }

        List<String> contentLines = parseTopicContentsFromFile(contentsFileName);
        if(contentLines.isEmpty()) {
            return Optional.empty();
        }
        Map<DbResourceDto.Locale, List<String>> resourcesLines = parseTopicResourcesFromDirectoryAndCheck(topic, databaseDirectory, integrityErrors);

        DatabaseParser databaseParser = DatabaseParser.load(contentLines, resourcesLines);
        DbDto dbDto = databaseParser.parseAll();
        integrityErrors.addAll(databaseParser.getIntegrityErrors());

        return Optional.of(dbDto);
    }

    /**
     * Reads all database contents (+resources) in JSON format from specified topic into jsonDirectory.
     * @param topic         : topic to parse TDU contents from
     * @param jsonDirectory : location of json files
     * @return empty object if topic could not be read.
     */
    public static Optional<DbDto> readDatabaseTopicFromJson(DbDto.Topic topic, String jsonDirectory) throws IOException {

        File jsonFile = getJsonFileFromDirectory(topic, jsonDirectory);
        if (!jsonFile.exists()) {
            return Optional.<DbDto>empty();
        }

        return Optional.of(objectMapper.readValue(jsonFile, DbDto.class));
    }

    /**
     * Reads complete TDU database (contents + resources for all available topics) into jsonDirectory.
     * @param jsonDirectory : location of json files
     * @return a list of available database topic objects.
     */
    public static List<DbDto> readFullDatabaseFromJson(String jsonDirectory) {

        return DbDto.Topic.valuesAsStream()

                .parallel()

                .map((topic) -> {
                    try {
                        return readDatabaseTopicFromJson(topic, jsonDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Optional.<DbDto>empty();
                    }
                })

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toList());
    }

    /**
     * Writes all database contents (+resources) as TDU format from specified topic into outputDirectory.
     * @param dbDto             : topic contents to be written
     * @param outputDirectory   : location of generated files
     * @return a list of written TDU files
     * @throws FileNotFoundException
     */
    public static List<String> writeDatabaseTopic(DbDto dbDto, String outputDirectory) throws IOException {

        DatabaseWriter writer = DatabaseWriter.load(dbDto);
        List<String> writtenFileNames = writer.writeAll(outputDirectory);

        encryptContents(outputDirectory, writtenFileNames);

        return writtenFileNames;
    }

    /**
     * Writes all database contents (+resources) as JSON format from specified topic into outputDirectory.
     * @param allTopicObjects   : topics contents to be written
     * @param outputDirectory   : location of generated file
     * @return names of correctlty written JSON files.
     */
    public static List<String> writeDatabaseTopicsToJson(List<DbDto> allTopicObjects, String outputDirectory) {
        return allTopicObjects.stream()

                .parallel()

                .map( (topicObject) -> writeDatabaseTopicToJson(topicObject, outputDirectory))

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toList());
    }

    /**
     * Writes database contents (+resources) as JSON format from specified topic into outputDirectory.
     * @param dbDto             : topic contents to be written
     * @param outputDirectory   : location of generated file
     * @return name of written JSON file if all went correctly, absent otherwise.
     */
    public static Optional<String> writeDatabaseTopicToJson(DbDto dbDto, String outputDirectory) {
        try {
            return Optional.ofNullable(
                    DatabaseWriter
                            .load(dbDto)
                            .writeAllAsJson(outputDirectory)
            );
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.<String>empty();
        }
    }

    /**
     * Creates a temporary directory.
     * @return full directory name
     * @throws IOException
     */
    public static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("libUnlimited-databaseRW").toString();
    }

    static List<String> parseTopicContentsFromFile(String contentsFileName) throws FileNotFoundException {
        return parseLinesInFile(contentsFileName, ENCODING_UTF_8);
    }

    static Map<DbResourceDto.Locale, List<String>> parseTopicResourcesFromDirectoryAndCheck(DbDto.Topic topic, String databaseDirectory, Set<IntegrityError> integrityErrors) throws FileNotFoundException {

        Map<DbResourceDto.Locale, List<String>> resourcesLinesByLocale = readLinesFromResourceFiles(databaseDirectory, topic);

        checkResourcesLines(resourcesLinesByLocale, topic, integrityErrors);

        return resourcesLinesByLocale;
    }

    private static File getJsonFileFromDirectory(DbDto.Topic topic, String jsonDirectory) {
        String jsonFileName = getDatabaseFileName(topic.getLabel(), jsonDirectory, EXTENSION_JSON);
        return new File(jsonFileName);
    }

    private static Map<DbResourceDto.Locale, List<String>> readLinesFromResourceFiles(String databaseDirectory, DbDto.Topic topic) throws FileNotFoundException {
        Map<DbResourceDto.Locale, List<String>> resourcesLinesByLocale = new ConcurrentHashMap<>();
        DbResourceDto.Locale.valuesAsStream()
                .forEach((currentLocale) -> {
                    String resourceFileName = getDatabaseFileName(topic.getLabel(), databaseDirectory, currentLocale.getCode());

                    List<String> readLines = null;
                    try {
                        readLines = parseLinesInFile(resourceFileName, ENCODING_UTF_16);
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                        throw new RuntimeException(fnfe);
                    }

                    resourcesLinesByLocale.put(currentLocale, readLines);
                });
        return resourcesLinesByLocale;
    }

    private static void checkResourcesLines(Map<DbResourceDto.Locale, List<String>> resourcesLinesByLocale, DbDto.Topic topic, Set<IntegrityError> integrityErrors) {
        requireNonNull(integrityErrors, "A list of integrity errors (even empty) is required.");

        integrityErrors.addAll(resourcesLinesByLocale.entrySet().stream()

                .filter((entry) -> entry.getValue().isEmpty())

                .map((entry) -> {
                    DbResourceDto.Locale locale = entry.getKey();
                    Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
                    info.put(SOURCE_TOPIC, topic);
                    info.put(FILE, String.format("%s.%s", topic.getLabel(), locale.getCode()));
                    info.put(LOCALE, locale);

                    return IntegrityError.builder()
                            .ofType(IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND)
                            .addInformations(info)
                            .build();
                })

                .collect(toSet()));
    }

    private static String checkDatabaseContents(DbDto.Topic topic, String databaseDirectory, Set<IntegrityError> integrityErrors) throws FileNotFoundException {
        String contentsFileName = getDatabaseFileName(topic.getLabel(), databaseDirectory, EXTENSION_DB_CONTENTS);
        File contentsFile = new File(contentsFileName);

        if (contentsFile.exists()) {
            return contentsFile.getAbsolutePath();
        }

        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
        info.put(SOURCE_TOPIC, topic);
        info.put(FILE, contentsFile.getAbsolutePath());
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
        String fileName = String.format("%s.%s", topicLabel, extension);
        return new File(databaseDirectory, fileName).getAbsolutePath();
    }

    private static String prepareClearContents(String contentsFileName, Set<IntegrityError> integrityErrors) throws IOException {
        File inputFile = new File(contentsFileName);
        File outputFile = new File(createTempDirectory(), inputFile.getName());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath()));

        try {
            ByteArrayOutputStream outputStream = CryptoHelper.decryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
            Files.write(outputFile.toPath(), outputStream.toByteArray(), StandardOpenOption.CREATE);
        } catch (Exception e) {

            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(FILE, contentsFileName);
            IntegrityError integrityError = IntegrityError.builder()
                    .ofType(IntegrityError.ErrorTypeEnum.CONTENTS_ENCRYPTION_NOT_SUPPORTED)
                    .addInformations(info)
                    .build();
            integrityErrors.add(integrityError);

            return null;
        }

        return outputFile.getPath();
    }

    private static void encryptContents(String outputDirectory, List<String> writtenFileNames) throws IOException {
        writtenFileNames.stream()

                .filter( (fileName) -> fileName.toUpperCase().endsWith(EXTENSION_DB_CONTENTS.toUpperCase()))

                .findFirst()

                .ifPresent( (contentsFileName) -> {
                    try {
                        File inputFile = new File(contentsFileName);
                        File outputFile = new File(createTempDirectory(), inputFile.getName());

                        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath()));
                        ByteArrayOutputStream outputStream = CryptoHelper.encryptXTEA(inputStream, CryptoHelper.EncryptionModeEnum.OTHER_AND_SPECIAL);
                        Files.write(outputFile.toPath(), outputStream.toByteArray(), StandardOpenOption.CREATE);

                        Path source = outputFile.toPath();
                        Path target = Paths.get(outputDirectory, source.getFileName().toString());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}