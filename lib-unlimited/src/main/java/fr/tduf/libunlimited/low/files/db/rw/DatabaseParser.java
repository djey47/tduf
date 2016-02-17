package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.BITFIELD;
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
    private final Map<DbResourceEnhancedDto.Locale, List<String>> resources;

    private final List<IntegrityError> integrityErrors = new ArrayList<>();

    private DatabaseParser(List<String> contentlines, Map<DbResourceEnhancedDto.Locale, List<String>> resources) {
        this.contentLines = contentlines;
        this.resources = resources;
    }

    /**
     * Single entry point for this parser.
     *
     * @param contentLines contentLines from unencrypted database file
     * @param resources    list of contentLines from per-language resource files
     * @return a {@link DatabaseParser} instance.
     */
    public static DatabaseParser load(List<String> contentLines, Map<DbResourceEnhancedDto.Locale, List<String>> resources) {
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

        // TODO Remove when using only V2 files
        List<DbResourceDto> resources = parseAllResourcesFromTopic(structure.getTopic());

        DbDataDto data = parseContents(structure);
        DbResourceEnhancedDto resource = parseAllResourcesEnhancedFromTopic(structure.getTopic());

        return DbDto.builder()
                .withData(data)
                .withResource(resource)
                .addResources(resources)
                .withStructure(structure)
                .build();
    }

    private static void checkPrerequisites(List<String> contentLines, Map<DbResourceEnhancedDto.Locale, List<String>> resources) {
        requireNonNull(contentLines, "Contents are required");
        requireNonNull(resources, "Resources are required");
    }

    @Deprecated
    private List<DbResourceDto> parseAllResourcesFromTopic(DbDto.Topic topic) {

        final Pattern resourceVersionPattern = Pattern.compile(META_VERSION_PATTERN);
        final Pattern categoryCountPattern = Pattern.compile(META_CATEGORY_COUNT_PATTERN);
        final Pattern resourceEntryPattern = Pattern.compile(RES_ENTRY_PATTERN);

        List<DbResourceDto> dbResourceDtos = new ArrayList<>();

        Stream.of(DbResourceEnhancedDto.Locale.values())

                .filter(resources::containsKey)

                .filter((locale) -> !resources.get(locale).isEmpty())

                .forEach((locale) -> {
                    DbResourceDto dbResourceDto = parseResourcesForLocale(locale, resourceVersionPattern, categoryCountPattern, resourceEntryPattern);
                    dbResourceDtos.add(dbResourceDto);
                });

        checkItemCountBetweenResources(topic, dbResourceDtos);

        return dbResourceDtos;
    }

    private DbResourceEnhancedDto parseAllResourcesEnhancedFromTopic(DbDto.Topic topic) {
        // TODO extract to constants
        final Pattern resourceVersionPattern = Pattern.compile(META_VERSION_PATTERN);
        final Pattern categoryCountPattern = Pattern.compile(META_CATEGORY_COUNT_PATTERN);
        final Pattern resourceEntryPattern = Pattern.compile(RES_ENTRY_PATTERN);

        Set<DbResourceEnhancedDto.Entry> entries = new LinkedHashSet<>();
        AtomicInteger categoryCount = new AtomicInteger();
        AtomicReference<String> version = new AtomicReference<>();

        Stream.of(DbResourceEnhancedDto.Locale.values())

                .filter(resources::containsKey)

                .filter((locale) -> !resources.get(locale).isEmpty())

                .forEach((locale) -> parseResourcesEnhancedForLocale(locale, entries, categoryCount, version, resourceVersionPattern, categoryCountPattern, resourceEntryPattern));

        if (entries.isEmpty()) {
            return null;
        }

        checkItemCountBetweenResourcesEnhanced(topic, entries);

        return  DbResourceEnhancedDto.builder()
                .atVersion(version.get())
                .withCategoryCount(categoryCount.get())
                .containingEntries(entries)
                .build();
    }

    private void parseResourcesEnhancedForLocale(DbResourceEnhancedDto.Locale locale, Set<DbResourceEnhancedDto.Entry> entries, AtomicInteger categoryCount, AtomicReference<String> version, Pattern resourceVersionPattern, Pattern categoryCountPattern, Pattern resourceEntryPattern) {
        requireNonNull(entries, "A set of entries (even empty) is required.");

        for (String line : resources.get(locale)) {
            Matcher matcher = resourceVersionPattern.matcher(line);
            if (matcher.matches()) {
                version.set(matcher.group(1));
                continue;
            }

            matcher = categoryCountPattern.matcher(line);
            if (matcher.matches()) {
                categoryCount.set(valueOf(matcher.group(1)));
                continue;
            }

            if (Pattern.matches(COMMENT_PATTERN, line)) {
                continue;
            }

            matcher = resourceEntryPattern.matcher(line);
            if (matcher.matches()) {
                final DbResourceEnhancedDto tempResource = DbResourceEnhancedDto.builder()
                        .atVersion("")
                        .withCategoryCount(0)
                        .containingEntries(entries)
                        .build();
                DbResourceEnhancedDto.Entry entry =  tempResource.getEntryByReference(matcher.group(3))
                        .orElse(DbResourceEnhancedDto.Entry.builder()
                                .forReference(matcher.group(3))
                                .build());

                entry.addItem(DbResourceEnhancedDto.Item.builder()
                        .withValue(matcher.group(1))
                        .withLocale(locale)
                        .build());

                entries.add(entry);
            }
        }
    }

    private DbResourceDto parseResourcesForLocale(DbResourceEnhancedDto.Locale locale, Pattern resourceVersionPattern, Pattern categoryCountPattern, Pattern resourceEntryPattern) {
        final List<DbResourceDto.Entry> entries = new ArrayList<>();
        String version = null;
        int categoryCount = 0;

        for (String line : resources.get(locale)) {
            Matcher matcher = resourceVersionPattern.matcher(line);
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
                        .withLocale(locale)
                        .withCategoryCount(categoryCount)
                        .addEntries(entries)
                        .build();
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

                continue;
            }

            entries.add(DbDataDto.Entry.builder()
                    .forId(id)
                    .addItems(parseContentItems(structure, line, id))
                    .build());

            id++;
        }

        checkContentItemsCount(structure.getTopic(), itemCount, entries);

        return DbDataDto.builder()
                .addEntries(entries)
                .build();
    }

    private List<DbDataDto.Item> parseContentItems(DbStructureDto structure, String line, long entryIdentifier) {

        List<DbDataDto.Item> items = new ArrayList<>();
        int fieldIndex = 0;
        for (String itemValue : line.split(VALUE_DELIMITER)) {
            DbStructureDto.Field fieldInformation = structure.getFields().get(fieldIndex++);

            items.add(DbDataDto.Item.builder()
                    .ofFieldRank(fieldInformation.getRank())
                    .forName(fieldInformation.getName())
                    .withRawValue(itemValue)
                    .bitFieldForTopic(fieldInformation.getFieldType() == BITFIELD, structure.getTopic())
                    .build());
        }

        checkFieldCountInContents(structure, entryIdentifier, items);

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
        DbDto.Topic topic = null;
        String topicVersion = null;
        int categoryCount = 0;
        int fieldCount = 0;
        int fieldIndex = 0;

        for (String line : this.contentLines) {

            Matcher matcher = topicNamePattern.matcher(line);
            if (matcher.matches()) {
                topic = DbDto.Topic.fromLabel(matcher.group(1));
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
            if (matcher.matches()) {
                reference = matcher.group(2);
            }

            // Regular item
            matcher = itemPattern.matcher(line);
            if (matcher.matches()) {
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

        checkFieldCountInStructure(topic, fieldCount, fields);

        return DbStructureDto.builder()
                .forTopic(topic)
                .forReference(reference)
                .atVersion(topicVersion)
                .withCategoryCount(categoryCount)
                .addItems(fields)
                .build();
    }

    private void checkContentItemsCount(DbDto.Topic topic, long expectedItemCount, List<DbDataDto.Entry> actualEntries) {
        if (expectedItemCount != actualEntries.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(SOURCE_TOPIC, topic);
            info.put(EXPECTED_COUNT, expectedItemCount);
            info.put(ACTUAL_COUNT, actualEntries.size());

            addIntegrityError(CONTENT_ITEMS_COUNT_MISMATCH, info);
        }
    }

    private void checkFieldCountInStructure(DbDto.Topic topic, int expectedFieldCount, List<DbStructureDto.Field> fields) {
        if (expectedFieldCount != fields.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(SOURCE_TOPIC, topic);
            info.put(EXPECTED_COUNT, expectedFieldCount);
            info.put(ACTUAL_COUNT, fields.size());

            addIntegrityError(STRUCTURE_FIELDS_COUNT_MISMATCH, info);
        }
    }

    private void checkFieldCountInContents(DbStructureDto structureObject, long entryIdentifier, List<DbDataDto.Item> items) {
        int expectedFieldCount = structureObject.getFields().size();

        if (expectedFieldCount != items.size()) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(SOURCE_TOPIC, structureObject.getTopic());
            info.put(EXPECTED_COUNT, expectedFieldCount);
            info.put(ACTUAL_COUNT, items.size());
            info.put(ENTRY_ID, entryIdentifier);

            addIntegrityError(CONTENTS_FIELDS_COUNT_MISMATCH, info);
        }
    }

    @Deprecated
    private void checkItemCountBetweenResources(DbDto.Topic topic, List<DbResourceDto> dbResourceDtos) {
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

    private void checkItemCountBetweenResourcesEnhanced(DbDto.Topic topic, Set<DbResourceEnhancedDto.Entry> entries) {
        //TODO
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