package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.fromCode;
import static java.lang.Integer.valueOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Helper class to extract database structure and contents from clear db file.
 */
public class DatabaseParser {

    public static final String VALUE_DELIMITER = ";";

    private static final String COMMENT_PATTERN = "^// (.*)$";                  //e.g // Blabla
    private static final String ITEM_REF_PATTERN = "^\\{(.*)\\} (\\d*)$";       //e.g {TDU_Achievements} 2442784645
    private static final String ITEM_PATTERN = "^\\{(.*)\\} (.)( (\\d+))?$";    //e.g {Nb_Achievement_Points_} i OR {Car_Brand} r 1209165514
    private static final String ITEM_COUNT_PATTERN = "^// items: (\\d+)$";      //e.g // items: 74
    private static final String CONTENT_PATTERN = "^([0-9\\-\\.,]*;)+$";        //e.g 55736935;5;20;54400734;54359455;54410835;561129540;5337472;211;

    private static final String META_NAME_PATTERN = "^// (TDU_.+)\\.(.+)$";                     //e.g // TDU_Achievements.fr
    private static final String META_VERSION_PATTERN = "^// (?:v|V)ersion: (.+)$";              //e.g // version: 1,2 OR // Version: 1,2
    private static final String META_CATEGORY_COUNT_PATTERN = "^// (?:c|C)ategories: (.+)$";    //e.g // categories: 6 OR // Categories: 6
    private static final String META_FIELD_COUNT_PATTERN = "^// Fields: (.+)$";                 //e.g // Fields: 9
    private static final String RES_ENTRY_PATTERN = "^\\{(.*(\\n?.*)*)\\} (\\d+)$";             //e.g {??} 53410835

    private final List<String> contentLines;
    private final List<List<String>> resources;

    private final List<IntegrityError> integrityErrors = new ArrayList<>();

    private DatabaseParser(List<String> contentlines, List<List<String>> resources) {
        this.contentLines = contentlines;
        this.resources = resources;
    }

    /**
     * Single entry point for this parser.
     * @param contentLines contentLines from unencrypted database file
     * @param resources list of contentLines from per-language resource files
     * @return a {@link DatabaseParser} instance.
     */
    public static DatabaseParser load(List<String> contentLines, List<List<String>> resources) {
        checkPrerequisites(contentLines, resources);

        return new DatabaseParser(contentLines, resources);
    }

    /**
     * Parses all contents.
     */
    public DbDto parseAll() {
        checkPrerequisites(this.contentLines, this.resources);

        integrityErrors.clear();

        DbStructureDto structure = parseStructure();
        List<DbResourceDto> resources = parseResources(structure.getTopic());
        DbDataDto data = parseContents(structure);

        return DbDto.builder()
                .withData(data)
                .addResources(resources)
                .withStructure(structure)
                .build();
    }

    private static void checkPrerequisites(List<String> contentLines, List<List<String>> resources) {
        requireNonNull(contentLines, "Contents are required");
        requireNonNull(resources, "Resources are required");
    }

    private List<DbResourceDto> parseResources(DbDto.Topic topic) {

        final Pattern resourceNamePattern = Pattern.compile(META_NAME_PATTERN);
        final Pattern resourceVersionPattern = Pattern.compile(META_VERSION_PATTERN);
        final Pattern categoryCountPattern = Pattern.compile(META_CATEGORY_COUNT_PATTERN);
        final Pattern resourceEntryPattern = Pattern.compile(RES_ENTRY_PATTERN);

        List<DbResourceDto> dbResourceDtos = this.resources.stream()

                .filter(resourceLines -> !resourceLines.isEmpty())

                .map(resourceLines -> {
                    final List<DbResourceDto.Entry> entries = new ArrayList<>();
                    String version = null;
                    String localeCode = null;
                    int categoryCount = 0;

                    for (String line : resourceLines) {
                        Matcher matcher = resourceNamePattern.matcher(line);
                        if (matcher.matches()) {
                            localeCode = matcher.group(2);
                            continue;
                        }

                        matcher = resourceVersionPattern.matcher(line);
                        if (matcher.matches()) {
                            version = matcher.group(1);
                            continue;
                        }

                        matcher = categoryCountPattern.matcher(line);
                        if (matcher.matches()) {
                            categoryCount = valueOf(matcher.group(1));
                            continue;
                        }

                        if (Pattern.matches(COMMENT_PATTERN, line)) {
                            continue;
                        }

                        matcher = resourceEntryPattern.matcher(line);
                        if (matcher.matches()) {
                            entries.add(DbResourceDto.Entry.builder()
                                    .forReference(matcher.group(3))
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
                })

                .collect(Collectors.toList());

        checkItemCountBetweenResources(dbResourceDtos, topic);

        return dbResourceDtos;
    }

    private DbDataDto parseContents(DbStructureDto structure) {

        final Pattern itemCountMatcher = Pattern.compile(ITEM_COUNT_PATTERN);

        List<DbDataDto.Entry> entries = new ArrayList<>();
        long id = 0;
        long itemCount = 0;

        for (String line : this.contentLines) {

            Matcher matcher = itemCountMatcher.matcher(line);
            if (matcher.matches()) {
                itemCount = Long.valueOf(matcher.group(1));
                continue;
            }

            if (Pattern.matches(COMMENT_PATTERN, line)
                    || Pattern.matches(ITEM_REF_PATTERN, line)
                    || Pattern.matches(ITEM_PATTERN, line)
                    || !Pattern.matches(CONTENT_PATTERN, line)) {

                // Uncomment below to debug
//                System.out.println("Skipped line: " + line);
                continue;
            }

            entries.add(DbDataDto.Entry.builder()
                    .forId(id++)
                    .addItems(parseContentItems(structure, line))
                    .build());
        }

        checkContentItemsCount(itemCount, entries);

        return DbDataDto.builder()
                .addEntries(entries)
                .build();
    }

    private List<DbDataDto.Item> parseContentItems(DbStructureDto structure, String line) {
        List<DbDataDto.Item> items = new ArrayList<>();
        int fieldIndex = 0;
        for(String itemValue : line.split(VALUE_DELIMITER)) {
            DbStructureDto.Field fieldInformation = structure.getFields().get(fieldIndex++);

            items.add(DbDataDto.Item.builder()
                    .ofFieldRank(fieldInformation.getRank())
                    .forName(fieldInformation.getName())
                    .withRawValue(itemValue)
                    .build());
        }

        checkFieldCountInContents(structure, items);

        return items;
    }

    private DbStructureDto parseStructure() {

        final Pattern topicNamePattern = Pattern.compile(META_NAME_PATTERN);
        final Pattern topicVersionPattern = Pattern.compile(META_VERSION_PATTERN);
        final Pattern categoryCountPattern = Pattern.compile(META_CATEGORY_COUNT_PATTERN);
        final Pattern fieldCountPattern = Pattern.compile(META_FIELD_COUNT_PATTERN);
        final Pattern itemPattern = Pattern.compile(ITEM_PATTERN);
        final Pattern itemRefPattern = Pattern.compile(ITEM_REF_PATTERN);

        List<DbStructureDto.Field> fields = new ArrayList<>();
        String reference = null;
        String topicName = null;
        String topicVersion = null;
        int categoryCount = 0;
        int fieldCount = 0;
        int fieldIndex = 0;

        for (String line : this.contentLines) {

            Matcher matcher = topicNamePattern.matcher(line);
            if (matcher.matches()) {
                topicName = matcher.group(1);
                continue;
            }

            matcher = topicVersionPattern.matcher(line);
            if (matcher.matches()) {
                topicVersion = matcher.group(1);
                continue;
            }

            matcher = categoryCountPattern.matcher(line);
            if (matcher.matches()) {
                categoryCount = valueOf(matcher.group(1));
                continue;
            }

            matcher = fieldCountPattern.matcher(line);
            if (matcher.matches()) {
                fieldCount = valueOf(matcher.group(1));
                continue;
            }

            // Skips other comments
            if (Pattern.matches(COMMENT_PATTERN, line)) {
                continue;
            }

            // Current reference
            matcher = itemRefPattern.matcher(line);
            if(matcher.matches()) {
                reference = matcher.group(2);
            }

            // Regular item
            matcher = itemPattern.matcher(line);
            if(matcher.matches()) {
                fieldIndex++;

                String name = matcher.group(1);
                String typeCode = matcher.group(2);
                String remoteReference = matcher.group(4);

                DbStructureDto.FieldType fieldType = DbStructureDto.FieldType.fromCode(typeCode);

                fields.add(DbStructureDto.Field.builder()
                        .ofRank(fieldIndex)
                        .forName(name)
                        .fromType(fieldType)
                        .toTargetReference(remoteReference)
                        .build());
            }
        }

        checkFieldCountInStructure(fieldCount, fields);

        return DbStructureDto.builder()
                .forTopic(DbDto.Topic.fromLabel(topicName))
                .forReference(reference)
                .atVersion(topicVersion)
                .withCategoryCount(categoryCount)
                .addItems(fields)
                .build();
    }

    private void checkContentItemsCount(long expectedItemCount, List<DbDataDto.Entry> actualEntries) {
        if (expectedItemCount != actualEntries.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(EXPECTED_COUNT, expectedItemCount);
            info.put(ACTUAL_COUNT, actualEntries.size());

            addIntegrityError(CONTENT_ITEMS_COUNT_MISMATCH, info);
        }
    }

    private void checkFieldCountInStructure(int expectedFieldCount, List<DbStructureDto.Field> fields) {
        if (expectedFieldCount != fields.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(EXPECTED_COUNT, expectedFieldCount);
            info.put(ACTUAL_COUNT, fields.size());

            addIntegrityError(STRUCTURE_FIELDS_COUNT_MISMATCH, info);
        }
    }

    private void checkFieldCountInContents(DbStructureDto structureObject, List<DbDataDto.Item> items) {
        int expectedFieldCount = structureObject.getFields().size();

        if (expectedFieldCount != items.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(EXPECTED_COUNT, expectedFieldCount);
            info.put(ACTUAL_COUNT, items.size());
            info.put(SOURCE_TOPIC, structureObject.getTopic());

            addIntegrityError(CONTENTS_FIELDS_COUNT_MISMATCH, info);
        }
    }

    private void checkItemCountBetweenResources(List<DbResourceDto> dbResourceDtos, DbDto.Topic topic) {
        Map<Integer, List<DbResourceDto>> dbResourceDtosByItemCount = dbResourceDtos.stream()

                .collect(groupingBy(dbResourceDto -> dbResourceDto.getEntries().size()));

        if (dbResourceDtosByItemCount.size() > 1) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(SOURCE_TOPIC, topic);
            info.put(PER_LOCALE_COUNT, dbResourceDtos.stream()
                    .collect(toMap(DbResourceDto::getLocale, (dto) -> dto.getEntries().size())));

            addIntegrityError(RESOURCE_ITEMS_COUNT_MISMATCH, info);
        }
    }

    private void addIntegrityError(IntegrityError.ErrorTypeEnum errorTypeEnum, Map<IntegrityError.ErrorInfoEnum, Object> info) {
        IntegrityError integrityError = IntegrityError.builder()
                .ofType(errorTypeEnum)
                .addInformations(info)
                .build();
        integrityErrors.add(integrityError);
    }

    public long getContentLineCount() {
        checkPrerequisites(this.contentLines, this.resources);

        return contentLines.size();
    }

    public long getResourceCount() {
        checkPrerequisites(this.contentLines, this.resources);

        return resources.size();
    }

    public List<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }
}