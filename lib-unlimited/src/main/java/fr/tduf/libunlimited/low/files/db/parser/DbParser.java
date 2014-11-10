package fr.tduf.libunlimited.low.files.db.parser;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Helper class to extract database structure and contents from clear db file.
 */
public class DbParser {

    private static final String COMMENT_PATTERN = "^\\/\\/ (.*)$";          //e.g // TDU_Achievements.db
    private static final String ITEM_REF_PATTERN = "^\\{(.*)\\} (\\d*)$";   //e.g {TDU_Achievements} 2442784645
    private static final String ITEM_PATTERN = "^\\{(.*)\\} (.)$";          //e.g {Nb_Achievement_Points_} i
    public static final String VALUE_DELIMITER = ";";

    private final List<String> lines;

    private DbParser(List<String> lines) {
        this.lines = lines;
    }

    /**
     * Single entry point for this parser.
     * @param lines lines from unencrypted database file
     * @return a {@link DbParser} instance.
     */
    public static DbParser load(List<String> lines) {
        Objects.requireNonNull(lines, "Contents are required");

        //TODO Validate contents

        return new DbParser(lines);
    }

    public DbDto parseContents() {

        List<DbDto.Entry> entries = newArrayList();
        long id = 0;

        for (String line : lines) {
            if (Pattern.matches(COMMENT_PATTERN, line)
                    || Pattern.matches(ITEM_REF_PATTERN, line)
                    || Pattern.matches(ITEM_PATTERN, line)) {
                continue;
            }

            List<DbDto.Item> items = newArrayList();
            for(String itemValue : line.split(VALUE_DELIMITER)) {
                items.add(DbDto.Item.builder()
                        .forName("")
                        .withRawValue(itemValue)
                        .build());
            }

            entries.add(DbDto.Entry.builder()
                    .forId(id++)
                    .addItems(items)
                    .build());
        }

        return DbDto.builder()
                .addEntries(entries)
                .build();

    }

    public DbStructureDto parseStructure() {

        Pattern itemPattern = Pattern.compile(ITEM_PATTERN);
        Pattern itemRefPattern = Pattern.compile(ITEM_REF_PATTERN);

        List<DbStructureDto.Item> items = newArrayList();
        long id = 0;
        String reference = null;

        for (String line : lines) {
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
                DbStructureDto.Type type = DbStructureDto.Type.fromCode(code);

                items.add(DbStructureDto.Item.builder()
                        .withId(id++)
                        .forName(name)
                        .fromType(type)
                        .build());
            }
        }

        return DbStructureDto.builder()
                .forReference(reference)
                .addItems(items)
                .build();
    }

    public long getLineCount() {
        return lines.size();
    }
}
