package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
     * Writes all contents to given path (must exist) as JSON file .
     * @param path  : location to write db files
     * @return name of JSON file that has been written.
     */
    public String writeAllAsJson(String path) throws IOException {
        checkPrerequisites(this.databaseDto);

        String outputFileName = String.format("%s.%s", this.databaseDto.getTopic().getLabel(), EXTENSION_JSON);

        Path outputFilePath = Paths.get(path, outputFileName);
        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
            new ObjectMapper().writer().writeValue(bufferedWriter, this.databaseDto);
        }

        return outputFilePath.toAbsolutePath().toString();
    }

    private static void checkPrerequisites(DbDto dbDto) {
        requireNonNull(dbDto, "Full database information is required");
        requireNonNull(dbDto.getStructure(), "Database structure information is required");
        requireNonNull(dbDto.getData(), "Database contents are required");
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
        for (DbDataDto.Entry entry : dbDataDto.getEntries()) {

            for (DbDataDto.Item item : entry.getItems()) {
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

        List<String> writtenPaths = new ArrayList<>();
        List<DbResourceDto> dbResourceDtos = this.databaseDto.getResources();
        String topicLabel = this.databaseDto.getTopic().getLabel();

        for(DbResourceDto dbResourceDto : dbResourceDtos) {

            String localeCode = dbResourceDto.getLocale().getCode();
            String resourceFileName = format("%s.%s", topicLabel, localeCode);

            File resourceFile = new File(directoryPath, resourceFileName);
            try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(resourceFile.toPath(), StandardCharsets.UTF_16LE)) {

                // Encoding
                bufferedWriter.write("\uFEFF");

                // Meta
                writeAndEndWithCRLF(
                        format(COMMENT_PATTERN, resourceFileName), bufferedWriter);
                writeAndEndWithCRLF(
                        format(COMMENT_INFO_PATTERN, "version", dbResourceDto.getVersion()), bufferedWriter);
                writeAndEndWithCRLF(
                        format(COMMENT_INFO_PATTERN, "categories", dbResourceDto.getCategoryCount()), bufferedWriter);

                // Resources
                for(DbResourceDto.Entry entry : dbResourceDto.getEntries()) {
                    writeAndEndWithCRLF(
                            format(RESOURCE_ENTRY_PATTERN, entry.getValue(), entry.getReference()), bufferedWriter);
                }
            }

            writtenPaths.add(resourceFile.getAbsolutePath());
        }

        return writtenPaths;
    }

    private static long writeAndEndWithCRLF(String text, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(text);
        bufferedWriter.write("\r\n");

        return text.length() + 2;
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