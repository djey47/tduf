package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to generate db files (contents+structure) from database instances.
 */
public class DbWriter {

    private static final String COMMENT_PATTERN = "// %s";
    private static final String COMMENT_INFO_PATTERN = "// %s: %s";
    private static final String STRUCTURE_ENTRY_PATTERN = "{%s} %s";

    private final DbDto databaseDto;

    private DbWriter(DbDto dbDto) {
        this.databaseDto = dbDto;
    }

    /**
     * Single entry point for this writer.
     * @param dbDto full database information
     * @return writer instance.
     */
    public static DbWriter load(DbDto dbDto) {
        requireNonNull(dbDto, "Full database information is required");
        requireNonNull(dbDto.getStructure(), "Database structure information is required");
        requireNonNull(dbDto.getData(), "Database contents are required");

        return new DbWriter(dbDto);
    }

    /**
     * Write all TDU database files to given path (must exist).
     * @param path location to write db files
     */
    public void writeAll(String path) throws FileNotFoundException {
        writeStructureAndContents(path);
        writeResources();
    }

    private void writeStructureAndContents(String directoryPath) throws FileNotFoundException {

        DbStructureDto dbStructureDto = this.databaseDto.getStructure();
        DbDataDto dbDataDto = this.databaseDto.getData();

        DbDto.Topic currentTopic = dbStructureDto.getTopic();

        String topicLabel = currentTopic.getLabel();
        String contentsFileName = format("%s.db", topicLabel);

        Path path = Paths.get(directoryPath + "/" +contentsFileName);

        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Header
            bufferedWriter.write(format(COMMENT_PATTERN, contentsFileName));
            bufferedWriter.newLine();
            // TODO Real Version
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Version", "1,2"));
            bufferedWriter.newLine();
            // TODO Real Category count
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Categories", 6));
            bufferedWriter.newLine();

            // Structure
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Fields", dbStructureDto.getFields().size()));
            bufferedWriter.newLine();
            bufferedWriter.write(format(STRUCTURE_ENTRY_PATTERN, topicLabel, dbStructureDto.getRef()));
            bufferedWriter.newLine();
            for (DbStructureDto.Field field : dbStructureDto.getFields()) {
                bufferedWriter.write(format(STRUCTURE_ENTRY_PATTERN, field.getName(), field.getFieldType().getCode()));
                bufferedWriter.newLine();
            }

            // Contents
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "items", dbDataDto.getEntries().size()));
            bufferedWriter.newLine();
            for (DbDataDto.Entry entry : dbDataDto.getEntries()) {

                for (DbDataDto.Item item : entry.getItems()) {
                    bufferedWriter.write(item.getRawValue());
                    bufferedWriter.write(DbParser.VALUE_DELIMITER);
                }

                bufferedWriter.newLine();
            }

            // Nul
            bufferedWriter.write("\0");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeResources() {

    }
}
