package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Helper class to generate db files (contents+structure) from database instances.
 */
public class DatabaseWriter {

    private static final String COMMENT_PATTERN = "// %s";
    private static final String COMMENT_INFO_PATTERN = "// %s: %s";
    private static final String ENTRY_PATTERN = "{%s} %s";
    private static final String ENTRY_REF_PATTERN = "{%s} %s %s";
    private static final String RESOURCE_ENTRY_PATTERN = "{%s} %s";


    private final DbDto databaseDto;

    private DatabaseWriter(DbDto dbDto) {
        this.databaseDto = dbDto;
    }

    /**
     * Single entry point for this writer.
     * @param dbDto full database information
     * @return writer instance.
     */
    public static DatabaseWriter load(DbDto dbDto) {
        checkPrerequisites(dbDto);

        return new DatabaseWriter(dbDto);
    }

    /**
     * Writes all TDU database files to given path (must exist).
     * @param path location to write db files
     * @return a list of written file names
     */
    public List<String> writeAll(String path) throws IOException {
        checkPrerequisites(this.databaseDto);

        List<String> writtenFilenames = new ArrayList<>();

        writtenFilenames.add(writeStructureAndContents(path));
        writtenFilenames.addAll(writeResources(path));

        return writtenFilenames;
    }

    /**
     * Writes all contents to given path as JSON file .
     * @param path  : location to write db files
     * @return names of JSON files that have been written.
     */
    public List<String> writeAllAsJson(String path) throws IOException {
        checkPrerequisites(databaseDto);

        final String topicLabel = databaseDto.getTopic().getLabel();
        String dataOutputFileName = String.format(DatabaseReadWriteHelper.FMT_JSON_DATA_FILE_NAME, topicLabel);
        String structureOutputFileName = String.format(DatabaseReadWriteHelper.FMT_JSON_STRUCTURE_FILE_NAME, topicLabel);
        String resourceOutputFileName = String.format(DatabaseReadWriteHelper.FMT_JSON_RESOURCES_FILE_NAME, topicLabel);

        Path dataOutputFilePath = Paths.get(path, dataOutputFileName);
        FilesHelper.writeJsonObjectToFile(databaseDto.getData(), dataOutputFilePath.toString());

        Path structureOutputFilePath = Paths.get(path, structureOutputFileName);
        FilesHelper.writeJsonObjectToFile(databaseDto.getStructure(), structureOutputFilePath.toString());

        Path resourceOutputFilePath = Paths.get(path, resourceOutputFileName);
        FilesHelper.writeJsonObjectToFile(databaseDto.getResource(), resourceOutputFilePath.toString());

        return asList(
                dataOutputFilePath.toAbsolutePath().toString(),
                structureOutputFilePath.toAbsolutePath().toString(),
                resourceOutputFilePath.toAbsolutePath().toString());
    }

    private String writeStructureAndContents(String directoryPath) throws IOException {

        DbStructureDto dbStructureDto = this.databaseDto.getStructure();
        DbDataDto dbDataDto = this.databaseDto.getData();

        DbDto.Topic currentTopic = dbStructureDto.getTopic();

        String topicLabel = currentTopic.getLabel();
        String contentsFileName = format("%s.db", topicLabel);

        Path contentsFilePath = Paths.get(directoryPath, contentsFileName);
        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(contentsFilePath, StandardCharsets.UTF_8)) {
            long writtenSize = writeMetaContents(dbStructureDto, contentsFileName, bufferedWriter);

            writtenSize += writeStructureContents(dbStructureDto, topicLabel, bufferedWriter);

            writtenSize += writeItemContents(dbDataDto, bufferedWriter);

            // Required for later encryption
            writePaddingForSizeMultipleOfEight(bufferedWriter, writtenSize);
        }

        return contentsFilePath.toAbsolutePath().toString();
    }

    private long writeItemContents(DbDataDto dbDataDto, BufferedWriter bufferedWriter) throws IOException {
        long writtenSize = writeAndEndWithCRLF(
                format(COMMENT_INFO_PATTERN, "items", dbDataDto.getEntries().size()), bufferedWriter);
        for (ContentEntryDto entry : dbDataDto.getEntries()) {

            for (ContentItemDto item : entry.getItems()) {
                bufferedWriter.write(item.getRawValue());
                bufferedWriter.write(DatabaseParser.VALUE_DELIMITER);

                writtenSize += (item.getRawValue().length() + 1);
            }

            writtenSize += writeAndEndWithCRLF("", bufferedWriter);

        }
        return writtenSize;
    }

    private long writeStructureContents(DbStructureDto dbStructureDto, String topicLabel, BufferedWriter bufferedWriter) throws IOException {
        long writtenSize = writeAndEndWithCRLF(
                format(COMMENT_INFO_PATTERN, "Fields", dbStructureDto.getFields().size()), bufferedWriter);
        writtenSize += writeAndEndWithCRLF(
                format(ENTRY_PATTERN, topicLabel, dbStructureDto.getRef()), bufferedWriter);
        for (DbStructureDto.Field field : dbStructureDto.getFields()) {
            String fieldDescription;
            if(field.getTargetRef() == null) {
                fieldDescription = format(ENTRY_PATTERN, field.getName(), field.getFieldType().getCode());
            } else {
                fieldDescription = format(ENTRY_REF_PATTERN, field.getName(), field.getFieldType().getCode(), field.getTargetRef());
            }
            writtenSize += writeAndEndWithCRLF(fieldDescription, bufferedWriter);
        }
        return writtenSize;
    }

    private long writeMetaContents(DbStructureDto dbStructureDto, String contentsFileName, BufferedWriter bufferedWriter) throws IOException {
        long writtenSize = writeAndEndWithCRLF(
                format(COMMENT_PATTERN, contentsFileName), bufferedWriter );
        writtenSize += writeAndEndWithCRLF(
                format(COMMENT_INFO_PATTERN, "Version", dbStructureDto.getVersion()), bufferedWriter);
        writtenSize += writeAndEndWithCRLF(
                format(COMMENT_INFO_PATTERN, "Categories", dbStructureDto.getCategoryCount()), bufferedWriter);
        return writtenSize;
    }

    private List<String> writeResources(String directoryPath) throws IOException {
        DbResourceDto dbResourceDto = databaseDto.getResource();
        String topicLabel = databaseDto.getTopic().getLabel();
        return Locale.valuesAsStream()
                .map(locale -> writeResourcesForLocale(locale, topicLabel, dbResourceDto, directoryPath))
                .collect(toList());
    }

    private static void checkPrerequisites(DbDto dbDto) {
        requireNonNull(dbDto, "Full database information is required");
        requireNonNull(dbDto.getStructure(), "Database structure information is required");
        requireNonNull(dbDto.getData(), "Database contents are required");
    }

    private static String writeResourcesForLocale(Locale locale, String topicLabel, DbResourceDto resourceObject, String directoryPath) {
        String localeCode = locale.getCode();
        String resourceFileName = format("%s.%s", topicLabel, localeCode);
        File resourceFile = new File(directoryPath, resourceFileName);

        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(resourceFile.toPath(), StandardCharsets.UTF_16LE)) {

            // Encoding
            bufferedWriter.write("\uFEFF");

            // Meta
            writeAndEndWithCRLF(
                    format(COMMENT_PATTERN, resourceFileName), bufferedWriter);
            writeAndEndWithCRLF(
                    format(COMMENT_INFO_PATTERN, "version", resourceObject.getVersion()), bufferedWriter);
            writeAndEndWithCRLF(
                    format(COMMENT_INFO_PATTERN, "categories", resourceObject.getCategoryCount()), bufferedWriter);

            writeResourceLines(locale, resourceObject, bufferedWriter);

        } catch (IOException ioe) {
            throw new RuntimeException("Error occured while writing database", ioe);
        }

        return resourceFile.getAbsolutePath();
    }

    private static long writeAndEndWithCRLF(String text, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(text);
        bufferedWriter.write("\r\n");

        return text.length() + 2;
    }

    private static void writeResourceLines(Locale locale, DbResourceDto resourceObject, BufferedWriter bufferedWriter) {
        resourceObject.getEntries()
                // TODO replace by getItemValueForLocale
                .forEach((entry) -> entry.getItemForLocale(locale)
                        .ifPresent((item) -> {
                            try {
                                writeAndEndWithCRLF(
                                        format(RESOURCE_ENTRY_PATTERN, item.getValue(), entry.getReference()), bufferedWriter);
                            } catch (IOException ioe) {
                                throw new RuntimeException("Error occured while writing resource entry", ioe);
                            }
                        }));
    }

    private static long writePaddingForSizeMultipleOfEight(BufferedWriter bufferedWriter, long actualSize) throws IOException {

        if (actualSize % 8 == 0) {
            return 0;
        }

        long paddingSize = 8  - (actualSize % 8);
        for (int i = 0 ; i < paddingSize ; i++) {
            bufferedWriter.write("\0");
        }

        return paddingSize;
    }
}
