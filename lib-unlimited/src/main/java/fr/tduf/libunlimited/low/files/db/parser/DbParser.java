package fr.tduf.libunlimited.low.files.db.parser;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.fromCode;
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
        List<DbResourceDto> resources = parseResources();
        DbDataDto data = parseContents(structure, resources);

        return DbDto.builder()
                .withData(data)
                .addResources(resources)
                .withStructure(structure)
                .build();
    }

    private List<DbResourceDto> parseResources() {

        final Pattern resourceNamePattern = Pattern.compile(RES_NAME_PATTERN);
        final Pattern resourceVersionPattern = Pattern.compile(RES_VERSION_PATTERN);
        final Pattern categoryCountPattern = Pattern.compile(RES_CATEGORY_COUNT_PATTERN);
        final Pattern resourceEntryPattern = Pattern.compile(RES_ENTRY_PATTERN);

        return new ArrayList<>(Lists.transform(this.resources, new Function<List<String>, DbResourceDto>() {
            @Override
            public DbResourceDto apply(List<String> resourceLines) {

                final List<DbResourceDto.Entry> entries = new ArrayList<>();
                String version = null;
                String localeCode = null;
                int categoryCount = 0;

                for (String line : resourceLines) {
                    Matcher matcher = resourceNamePattern.matcher(line);
                    if (matcher.matches()) {
                        localeCode = matcher.group(1);
                        continue;
                    }

                    matcher = resourceVersionPattern.matcher(line);
                    if (matcher.matches()) {
                        version = matcher.group(1);
                        continue;
                    }

                    matcher = categoryCountPattern.matcher(line);
                    if (matcher.matches()) {
                        categoryCount = Integer.valueOf(matcher.group(1));
                        continue;
                    }

                    if (Pattern.matches(COMMENT_PATTERN, line)) {
                        continue;
                    }

                    matcher = resourceEntryPattern.matcher(line);
                    if (matcher.matches()) {
                        entries.add(DbResourceDto.Entry.builder()
                                .forReference(matcher.group(2))
                                .withValue(matcher.group(1))
                                .build());
                    }
                }

                return DbResourceDto.builder()
                        .atVersion(version)
                        .withLocale(fromCode(localeCode))
                        .withCategoryCount(categoryCount)
                        .addEntries(entries)
                        .build();
            }
        }));
    }

    private DbDataDto parseContents(DbStructureDto structure, List<DbResourceDto> resources) {

        List<DbDataDto.Entry> entries = new ArrayList<>();
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

            List<DbDataDto.Item> items = new ArrayList<>();
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

        List<DbStructureDto.Field> fields = new ArrayList<>();
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
}