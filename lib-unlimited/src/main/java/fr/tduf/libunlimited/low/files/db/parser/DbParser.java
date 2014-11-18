package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to extract database structure and contents from clear db file.
 */
public class DbParser {

    private static final String COMMENT_PATTERN = "^\\/\\/ (.*)$";          //e.g // TDU_Achievements.db
    private static final String ITEM_REF_PATTERN = "^\\{(.*)\\} (\\d*)$";   //e.g {TDU_Achievements} 2442784645
    private static final String ITEM_PATTERN = "^\\{(.*)\\} (.)$";          //e.g {Nb_Achievement_Points_} i
    public static final String VALUE_DELIMITER = ";";

    private final List<String> contentLines;
    private final List<List<String>> resources;

    private DbParser(List<String> contentlines, List<List<String>> resources) {
        this.contentLines = contentlines;
        this.resources = resources;
    }

    /**
     * Single entry point for this parser.
     * @param contentLines contentLines from unencrypted database file
     * @param resources list of contentLines from per-language resource files
     * @return a {@link DbParser} instance.
     */
    public static DbParser load(List<String> contentLines, List<List<String>> resources) {
        requireNonNull(contentLines, "Contents are required");
        requireNonNull(resources, "Resources are required");

        //TODO Validate contents and resources

        return new DbParser(contentLines, resources);
    }

    public DbDto parseAll() {
        DbStructureDto structure = parseStructure(contentLines);
        DbResourceDto resources = parseResources(this.resources);
        DbDataDto data = parseContents(contentLines, structure, resources);

        return DbDto.builder()
                .withData(data)
                .withResources(resources)
                .withStructure(structure)
                .build();
    }

    private DbResourceDto parseResources(List<List<String>> resources) {
        return DbResourceDto.builder().build();
    }

    private DbDataDto parseContents(List<String> contentLines, DbStructureDto structure, DbResourceDto resources) {

        List<DbDataDto.Entry> entries = newArrayList();
        long id = 0;

        for (String line : this.contentLines) {
            if (Pattern.matches(COMMENT_PATTERN, line)
                    || Pattern.matches(ITEM_REF_PATTERN, line)
                    || Pattern.matches(ITEM_PATTERN, line)) {
                continue;
            }

            List<DbDataDto.Item> items = newArrayList();
            for(String itemValue : line.split(VALUE_DELIMITER)) {
                // TODO depends on value type
                items.add(DbDataDto.Item.builder()
                        .forName("")
                        .build());
            }

            entries.add(DbDataDto.Entry.builder()
                    .forId(id++)
                    .addItems(items)
                    .build());
        }

        return DbDataDto.builder()
                .addEntries(entries)
                .build();

    }

    private DbStructureDto parseStructure(List<String> contentLines) {

        Pattern itemPattern = Pattern.compile(ITEM_PATTERN);
        Pattern itemRefPattern = Pattern.compile(ITEM_REF_PATTERN);

        List<DbStructureDto.Field> fields = newArrayList();
        long id = 0;
        String reference = null;

        for (String line : this.contentLines) {
            // Skips comments
            if (Pattern.matches(COMMENT_PATTERN, line)) {
                continue;
            }

            // Current reference
            Matcher matcher = itemRefPattern.matcher(line);
            if(matcher.matches()) {
                reference = matcher.group(2);
            }

            // Regular item
            matcher = itemPattern.matcher(line);
            if(matcher.matches()) {
                String name = matcher.group(1);
                String code = matcher.group(2);
                DbStructureDto.FieldType fieldType = DbStructureDto.FieldType.fromCode(code);

                fields.add(DbStructureDto.Field.builder()
                        .withId(id++)
                        .forName(name)
                        .fromType(fieldType)
                        .build());
            }
        }

        return DbStructureDto.builder()
                .forReference(reference)
                .addItems(fields)
                .build();
    }

    public long getContentLineCount() {
        return contentLines.size();
    }

    public long getResourceCount() {
        return resources.size();
    }

    public long getResourceLinesCount() {

        if (resources.isEmpty()) {
            return 0;
        }
        return resources.get(0).size();
    }
}
