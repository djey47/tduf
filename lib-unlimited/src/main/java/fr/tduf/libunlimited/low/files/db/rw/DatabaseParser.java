package fr.tduf.libunlimited.low.files.db.rw;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.BITFIELD;
import static java.lang.Integer.valueOf;
import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

/**
 * Helper class to extract database structure and contents from clear db file.
 */
public class DatabaseParser {

    public static final String VALUE_DELIMITER = ";";

    private static final String THIS_CLASS_NAME = DatabaseParser.class.getSimpleName();

    private static final Pattern COMMENT_PATTERN = compile("^// (.*)$");                                //e.g // Blabla
    private static final Pattern ITEM_REF_PATTERN = compile("^\\{(.*)\\} (\\d*)$");                     //e.g {TDU_Achievements} 2442784645
    private static final Pattern ITEM_PATTERN = compile("^\\{(.*)\\} (.)( (\\d+))?$");                  //e.g {Nb_Achievement_Points_} i OR {Car_Brand} r 1209165514
    private static final Pattern ITEM_COUNT_PATTERN = compile("^// items: (\\d+)$");                    //e.g // items: 74
    private static final Pattern CONTENT_PATTERN = compile("^([0-9\\-\\.,]*;)+$");                      //e.g 55736935;5;20;54400734;54359455;54410835;561129540;5337472;211;

    private static final Pattern META_NAME_PATTERN = compile("^// (TDU_.+)\\.(.+)$");                   //e.g // TDU_Achievements.fr
    private static final Pattern META_VERSION_PATTERN = compile("^// (?:v|V)ersion: (.+)$");            //e.g // version: 1,2 OR // Version: 1,2
    private static final Pattern META_CATEGORY_COUNT_PATTERN = compile("^// (?:c|C)ategories: (.+)$");  //e.g // categories: 6 OR // Categories: 6
    private static final Pattern META_FIELD_COUNT_PATTERN = compile("^// Fields: (.+)$");               //e.g // Fields: 9
    private static final Pattern RES_ENTRY_PATTERN = compile("^\\{(.*(\\n?.*)*)\\} (\\d+)$");           //e.g {??} 53410835

    private final List<String> contentLines;
    private final Map<fr.tduf.libunlimited.common.game.domain.Locale, List<String>> resources;

    private final List<IntegrityError> integrityErrors = new ArrayList<>();

    private DatabaseParser(List<String> contentlines, Map<Locale, List<String>> resources) {
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
    public static DatabaseParser load(List<String> contentLines, Map<Locale, List<String>> resources) {
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
        DbDataDto data = parseContents(structure);
        DbResourceDto resource = parseAllResourcesEnhancedFromTopic(structure.getTopic());

        return DbDto.builder()
                .withData(data)
                .withResource(resource)
                .withStructure(structure)
                .build();
    }

    private static void checkPrerequisites(List<String> contentLines, Map<Locale, List<String>> resources) {
        requireNonNull(contentLines, "Contents are required");
        requireNonNull(resources, "Resources are required");
    }

    private DbResourceDto parseAllResourcesEnhancedFromTopic(DbDto.Topic topic) {
        Map<String, DbResourceDto.Entry> readEntries = new LinkedHashMap<>();
        AtomicInteger categoryCount = new AtomicInteger();
        AtomicReference<String> version = new AtomicReference<>();

        Locale.valuesAsStream()
                .filter(resources::containsKey)
                .filter(locale -> !resources.get(locale).isEmpty())
                .forEach(locale -> parseResourcesEnhancedForLocale(locale, readEntries, categoryCount, version));

        if (readEntries.isEmpty()) {
            return null;
        }

        checkItemCountBetweenResourcesEnhanced(topic, readEntries.values());

        return  DbResourceDto.builder()
                .atVersion(version.get())
                .withCategoryCount(categoryCount.get())
                .containingEntries(readEntries.values())
                .build();
    }

    private void parseResourcesEnhancedForLocale(Locale locale, Map<String, DbResourceDto.Entry> readEntriesByRef, AtomicInteger categoryCount, AtomicReference<String> version) {
        requireNonNull(readEntriesByRef, "A map of entries (even empty) is required.");

        for (String line : resources.get(locale)) {
            Matcher matcher = RES_ENTRY_PATTERN.matcher(line);
            if (matcher.matches()) {
                final String ref = matcher.group(3);
                DbResourceDto.Entry entry = readEntriesByRef.get(ref);
                if (entry == null) {
                    entry = DbResourceDto.Entry.builder()
                            .forReference(ref)
                            .build();
                    readEntriesByRef.put(ref, entry);
                }
                entry.setValueForLocale(matcher.group(1), locale);
                continue;
            }

            matcher = META_VERSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                version.set(matcher.group(1));
                continue;
            }

            matcher = META_CATEGORY_COUNT_PATTERN.matcher(line);
            if (matcher.matches()) {
                categoryCount.set(valueOf(matcher.group(1)));
            }
        }
    }

    private DbDataDto parseContents(DbStructureDto structure) {

        List<DbDataDto.Entry> entries = new ArrayList<>();
        long id = 0;
        long itemCount = 0;

        for (String line : contentLines) {

            Matcher matcher = ITEM_COUNT_PATTERN.matcher(line);
            if (matcher.matches()) {
                itemCount = Long.valueOf(matcher.group(1));
                continue;
            }

            if (COMMENT_PATTERN.matcher(line).matches()
                    || ITEM_REF_PATTERN.matcher(line).matches()
                    || ITEM_PATTERN.matcher(line).matches()
                    || !CONTENT_PATTERN.matcher(line).matches()) {

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
                    .withRawValue(itemValue)
                    .bitFieldForTopic(fieldInformation.getFieldType() == BITFIELD, structure.getTopic())
                    .build());
        }

        checkFieldCountInContents(structure, entryIdentifier, items);

        return items;
    }

    private DbStructureDto parseStructure() {

        List<DbStructureDto.Field> fields = new ArrayList<>();
        String reference = null;
        DbDto.Topic topic = null;
        String topicVersion = null;
        int categoryCount = 0;
        int fieldCount = 0;
        int fieldIndex = 0;

        for (String line : contentLines) {

            Matcher matcher = META_NAME_PATTERN.matcher(line);
            if (matcher.matches()) {
                topic = DbDto.Topic.fromLabel(matcher.group(1));
                continue;
            }

            matcher = META_VERSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                topicVersion = matcher.group(1);
                continue;
            }

            matcher = META_CATEGORY_COUNT_PATTERN.matcher(line);
            if (matcher.matches()) {
                categoryCount = valueOf(matcher.group(1));
                continue;
            }

            matcher = META_FIELD_COUNT_PATTERN.matcher(line);
            if (matcher.matches()) {
                fieldCount = valueOf(matcher.group(1));
                continue;
            }

            // Skips other comments
            if (COMMENT_PATTERN.matcher(line).matches()) {
                continue;
            }

            // Current reference
            matcher = ITEM_REF_PATTERN.matcher(line);
            if (matcher.matches()) {
                reference = matcher.group(2);
            }

            // Regular item
            matcher = ITEM_PATTERN.matcher(line);
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
        checkCount(CONTENT_ITEMS_COUNT_MISMATCH, topic, expectedItemCount, actualEntries.size());
    }

    private void checkFieldCountInStructure(DbDto.Topic topic, int expectedFieldCount, List<DbStructureDto.Field> fields) {
        checkCount(STRUCTURE_FIELDS_COUNT_MISMATCH, topic, expectedFieldCount, fields.size());
    }

    private void checkCount(IntegrityError.ErrorTypeEnum errorType, DbDto.Topic topic, long expectedCount, long actualCount) {
        if (expectedCount != actualCount) {
            Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
            info.put(SOURCE_TOPIC, topic);
            info.put(EXPECTED_COUNT, expectedCount);
            info.put(ACTUAL_COUNT, actualCount);

            addIntegrityError(errorType, info);
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

    private void checkItemCountBetweenResourcesEnhanced(DbDto.Topic topic, Collection<DbResourceDto.Entry> entries) {
        entries.stream()

                .forEach((entry) -> {

                    if (entry.getItemCount() != Locale.values().length) {
                        Map<IntegrityError.ErrorInfoEnum, Object> info = new HashMap<>();
                        info.put(SOURCE_TOPIC, topic);
                        info.put(REFERENCE, entry.getReference());
                        info.put(MISSING_LOCALES, entry.getMissingLocales());

                        addIntegrityError(RESOURCE_REFERENCE_NOT_FOUND, info);
                    }
                });
    }

    private void addIntegrityError(IntegrityError.ErrorTypeEnum errorTypeEnum, Map<IntegrityError.ErrorInfoEnum, Object> info) {
        IntegrityError integrityError = IntegrityError.builder()
                .ofType(errorTypeEnum)
                .addInformations(info)
                .build();
        integrityErrors.add(integrityError);
    }

    public List<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }

    long getContentLineCount() {
        checkPrerequisites(contentLines, resources);

        return contentLines.size();
    }

    long getResourceCount() {
        checkPrerequisites(contentLines, resources);

        return resources.size();
    }
}
