package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;

/**
 * Helper class to extract database structure and contents from clear db file.
 */
public class DbParser {

    private static final String COMMENT_PATTERN = "^// (.*)$";              //e.g // Blabla
    private static final String ITEM_REF_PATTERN = "^\\{(.*)\\} (\\d*)$";   //e.g {TDU_Achievements} 2442784645
    private static final String ITEM_PATTERN = "^\\{(.*)\\} (.)$";          //e.g {Nb_Achievement_Points_} i
    private static final String CONTENT_PATTERN = "^(\\d+;)+$";             //e.g 55736935;5;20;54400734;54359455;54410835;561129540;5337472;211;
    private static final String VALUE_DELIMITER = ";";

    private static final String RES_NAME_PATTERN = "^// TDU_.+\\.(.+)$";                //e.g // TDU_Achievements.fr
    private static final String RES_VERSION_PATTERN = "^// version: (.+)$";             //e.g // version: 1,2
    private static final String RES_CATEGORY_COUNT_PATTERN = "^// categories: (.+)$";   //e.g // categories: 6
    private static final String RES_ENTRY_PATTERN = "^\\{(.*)\\} (\\d*)$";              //e.g {??} 53410835

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
        DbStructureDto structure = parseStructure();
        DbResourceDto resources = parseResources();
        DbDataDto data = parseContents(structure, resources);

        return DbDto.builder()
                .withData(data)
//                .withResources(resources)
                .withStructure(structure)
                .build();
    }

    private DbResourceDto parseResources() {

        Pattern resourceNamePattern = Pattern.compile(RES_NAME_PATTERN);
        Pattern resourceVersionPattern = Pattern.compile(RES_VERSION_PATTERN);
        Pattern categoryCountPattern = Pattern.compile(RES_CATEGORY_COUNT_PATTERN);
        Pattern resourceEntryPattern = Pattern.compile(RES_ENTRY_PATTERN);

        String localeCode = null;
        String version = null;
        int categoryCount = 0;


        for (List<String> resourceLines : this.resources) {

            for (String line : resourceLines) {

                Matcher matcher = resourceNamePattern.matcher(line);
                if (matcher.matches()) {
                    localeCode = matcher.group(1);
                    System.out.println("Locale found: " + localeCode);
                    continue;
                }

                matcher = resourceVersionPattern.matcher(line);
                if (matcher.matches() && version == null) {
                    version = matcher.group(1);
                    System.out.println("Version found: " + version);
                    continue;
                }

                matcher = categoryCountPattern.matcher(line);
                if (matcher.matches() && categoryCount == 0) {
                    categoryCount = Integer.valueOf(matcher.group(1));
                    System.out.println("Category count found: " + categoryCount);
                    continue;
                }

                if (Pattern.matches(COMMENT_PATTERN, line)) {
                    continue;
                }

                matcher = resourceEntryPattern.matcher(line);
                if (matcher.matches()) {
                    String value = matcher.group(1);
                    String reference = matcher.group(2);

                    String key = reference + "-" + localeCode;

//                    valueByLocalizedReference.put(key, value);
                }
            }
        }

        List<DbResourceDto.Entry> entries = newArrayList();



//                    localizedValues.add(localizedValue);
                    DbResourceDto.Entry entry = DbResourceDto.Entry.builder()
//                            .addLocalizedValues(localizedValues)
//                            .forReference(reference)
                            .build();
                    entries.add(entry);
    //                System.out.println("Entry found: " + reference + " - " + value);
//        }

        return DbResourceDto.builder()
                .atVersion(version)
                .withCategoryCount(categoryCount)
                .addEntries(entries)
                .build();
    }

    private DbDataDto parseContents(DbStructureDto structure, DbResourceDto resources) {

        List<DbDataDto.Entry> entries = newArrayList();
        long id = 0;

        for (String line : this.contentLines) {
            if (Pattern.matches(COMMENT_PATTERN, line)
                    || Pattern.matches(ITEM_REF_PATTERN, line)
                    || Pattern.matches(ITEM_PATTERN, line)) {
                continue;
            }

            if(!Pattern.matches(CONTENT_PATTERN, line)) {
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

    private DbStructureDto parseStructure() {

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