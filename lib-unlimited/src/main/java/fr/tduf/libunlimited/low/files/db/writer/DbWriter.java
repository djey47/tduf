package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.parser.DbParser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to generate db files (contents+structure) from database instances.
 */
public class DbWriter {

    private static final String COMMENT_PATTERN = "// %s";
    private static final String COMMENT_INFO_PATTERN = "// %s: %s";
    private static final String ENTRY_PATTERN = "{%s} %s";

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
        writeResources(path);
    }

    private void writeStructureAndContents(String directoryPath) throws FileNotFoundException {

        DbStructureDto dbStructureDto = this.databaseDto.getStructure();
        DbDataDto dbDataDto = this.databaseDto.getData();

        DbDto.Topic currentTopic = dbStructureDto.getTopic();

        String topicLabel = currentTopic.getLabel();
        String contentsFileName = format("%s.db", topicLabel);

        Path path = Paths.get(directoryPath + "/" + contentsFileName);

        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Meta
            bufferedWriter.write(format(COMMENT_PATTERN, contentsFileName));
            bufferedWriter.newLine();
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Version", dbStructureDto.getVersion()));
            bufferedWriter.newLine();
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Categories", dbStructureDto.getCategoryCount()));
            bufferedWriter.newLine();

            // Structure
            bufferedWriter.write(format(COMMENT_INFO_PATTERN, "Fields", dbStructureDto.getFields().size()));
            bufferedWriter.newLine();
            bufferedWriter.write(format(ENTRY_PATTERN, topicLabel, dbStructureDto.getRef()));
            bufferedWriter.newLine();
            for (DbStructureDto.Field field : dbStructureDto.getFields()) {
                bufferedWriter.write(format(ENTRY_PATTERN, field.getName(), field.getFieldType().getCode()));
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
            // TODO handle error
            e.printStackTrace();
        }
    }

    private void writeResources(String directoryPath) {

        List<DbResourceDto> dbResourceDtos = this.databaseDto.getResources();
        String topicLabel = this.databaseDto.getStructure().getTopic().getLabel();

        for(DbResourceDto dbResourceDto : dbResourceDtos) {

            String localeCode = dbResourceDto.getLocale().getCode();
            String resourceFileName = format("%s.%s", topicLabel, localeCode);
            Path path = Paths.get(directoryPath + "/" + resourceFileName);

            try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_16LE)) {
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
                            format(ENTRY_PATTERN, entry.getValue(), entry.getReference()), bufferedWriter);
                }
            } catch (IOException e) {
                // TODO handle error
                e.printStackTrace();
            }
        }
    }

    private static void writeAndEndWithCRLF(String text, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(text);
        bufferedWriter.write("\r\n");
    }
}