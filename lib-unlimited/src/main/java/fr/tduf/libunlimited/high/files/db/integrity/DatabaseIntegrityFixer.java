package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.helper.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.*;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing method to repair Database.
 */
public class DatabaseIntegrityFixer {

    private static final String RESOURCE_VALUE_DEFAULT = "-FIXED BY TDUF-";

    private final List<DbDto> dbDtos;
    private final List<IntegrityError> integrityErrors;
    private final BulkDatabaseMiner bulkDatabaseMiner;

    // Following errors are auto-handled: CONTENT_ITEMS_COUNT_MISMATCH, STRUCTURE_FIELDS_COUNT_MISMATCH
    private static final Set<IntegrityError.ErrorTypeEnum> FIXABLE_ERRORS = new HashSet<>(asList(RESOURCE_NOT_FOUND, RESOURCE_ITEMS_COUNT_MISMATCH, RESOURCE_REFERENCE_NOT_FOUND, CONTENTS_REFERENCE_NOT_FOUND, CONTENTS_FIELDS_COUNT_MISMATCH));
    private static final Set<IntegrityError.ErrorTypeEnum> UNFIXABLE_ERRORS = new HashSet<>(asList(CONTENTS_NOT_FOUND, CONTENTS_ENCRYPTION_NOT_SUPPORTED));

    private DatabaseIntegrityFixer(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        this.dbDtos = dbDtos;
        this.integrityErrors = integrityErrors;
        this.bulkDatabaseMiner = BulkDatabaseMiner.load(dbDtos);
    }

    /**
     * Single entry point for this fixer.
     * @param dbDtos            : per topic, database objects
     * @param integrityErrors   : errors returned by checker module
     * @return a {@link DatabaseIntegrityChecker} instance.
     */
    public static DatabaseIntegrityFixer load(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        checkRequirements(dbDtos, integrityErrors);

        return new DatabaseIntegrityFixer(dbDtos, integrityErrors);
    }

    /**
     * Process fixing over all loaded database objects.
     * @return list of remaining integrity errors.
     */
    public List<IntegrityError> fixAllContentsObjects() {
        List<IntegrityError> remainingIntegrityErrors = new ArrayList<>();

        if(this.integrityErrors.isEmpty()) {
            return remainingIntegrityErrors;
        }

        handleUnfixableErrors(remainingIntegrityErrors);

        handleFixableErrors(remainingIntegrityErrors);

        return remainingIntegrityErrors;
    }

    private void handleUnfixableErrors(List<IntegrityError> remainingIntegrityErrors) {
        remainingIntegrityErrors.addAll(
                this.integrityErrors.stream()

                        .filter((integrityError) -> UNFIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toList()));
    }

    private void handleFixableErrors(List<IntegrityError> remainingIntegrityErrors) {
        remainingIntegrityErrors.addAll(
                this.integrityErrors.stream()

                        .filter((integrityError) -> FIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toMap((integrityError) -> integrityError, this::fixIntegrityError))

                        .entrySet().stream()

                        .filter((resultEntry) -> !resultEntry.getValue())

                        .map(Map.Entry::getKey)

                        .collect(toList())
        );
    }

    private boolean fixIntegrityError(IntegrityError integrityError) {

        try {
            Map<ErrorInfoEnum, Object> information = integrityError.getInformation();
            DbDto.Topic sourceTopic = (DbDto.Topic) information.get(ErrorInfoEnum.SOURCE_TOPIC);
            DbDto.Topic remoteTopic = (DbDto.Topic) information.get(ErrorInfoEnum.REMOTE_TOPIC);
            String reference = (String) information.get(ErrorInfoEnum.REFERENCE);
            Long entryIdentifier = (Long) information.get(ErrorInfoEnum.ENTRY_ID);
            DbResourceDto.Locale locale = (DbResourceDto.Locale) information.get(ErrorInfoEnum.LOCALE);
            Map<DbResourceDto.Locale, Integer> perLocaleCount = (Map<DbResourceDto.Locale, Integer>) information.get(ErrorInfoEnum.PER_LOCALE_COUNT);

            switch(integrityError.getErrorTypeEnum()) {
                case RESOURCE_REFERENCE_NOT_FOUND:
                    addResourceEntryWithDefaultValue(reference, remoteTopic, locale);
                    break;
                case CONTENTS_REFERENCE_NOT_FOUND:
                    addContentsEntryWithDefaultItems(Optional.of(reference), remoteTopic);
                    break;
                case CONTENTS_FIELDS_COUNT_MISMATCH:
                    addMissingContentsFields(entryIdentifier, sourceTopic);
                    break;
                case RESOURCE_NOT_FOUND:
                    addResourceLocaleFromValidResource(locale, sourceTopic);
                    break;
                case RESOURCE_ITEMS_COUNT_MISMATCH:
                    addAllMissingResourceEntries(perLocaleCount, sourceTopic);
                    break;
                default:
                    throw new IllegalArgumentException("Kind of integrity error not handled yet: " + integrityError.getErrorTypeEnum());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void addAllMissingResourceEntries(Map<DbResourceDto.Locale, Integer> perLocaleCount, DbDto.Topic topic) {
        // Find locales with the maximum item count
        DbResourceDto.Locale referenceLocale = perLocaleCount.entrySet().stream()

                .max((entry1, entry2) -> Integer.compare(entry1.getValue(), entry2.getValue()))

                .map(Map.Entry::getKey)

                .get();

        int referenceResourceCount = perLocaleCount.get(referenceLocale);

        List<DbResourceDto> topicResourceObjects = bulkDatabaseMiner.getAllResourcesFromTopic(topic);

        DbResourceDto referenceResourceObject = bulkDatabaseMiner.getResourceFromTopicAndLocale(topic, referenceLocale);

        topicResourceObjects.stream()

                    .filter((resourceObject) -> resourceObject.getEntries().size() < referenceResourceCount)

                    .forEach((corruptedResourceObject) -> addMissingResourceEntries(corruptedResourceObject, referenceResourceObject));
    }

    private void addResourceLocaleFromValidResource(DbResourceDto.Locale missingLocale, DbDto.Topic topic) {
        // TODO what if RESOURCE_ITEMS_COUNT_MISMATCH on US locale ?
        // TODO what if US locale not found ?
        DbDto topicObject = bulkDatabaseMiner.getDatabaseTopic(topic);
        DbResourceDto referenceResourceObject = bulkDatabaseMiner.getResourceFromTopicAndLocale(topic, DbResourceDto.Locale.UNITED_STATES);

        topicObject.getResources().add(DbResourceDto.builder()
                .fromExistingResource(referenceResourceObject)
                .withLocale(missingLocale)
                .build());
    }

    private void addMissingContentsFields(long entryInternalIdentifier, DbDto.Topic topic) {
        DbDto topicObject = bulkDatabaseMiner.getDatabaseTopic(topic);

        DbDataDto.Entry invalidEntry = bulkDatabaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryInternalIdentifier, topic);

        // Structure is the reference
        topicObject.getStructure().getFields().stream()

                .filter((field) -> invalidEntry.getItems().stream()

                        .map(DbDataDto.Item::getName)

                        .filter((name) -> name.equals(field.getName()))

                        .count() == 0)

                .forEach((missingField) -> addContentItem(missingField, invalidEntry, topicObject));
    }

    private static void addMissingResourceEntries(DbResourceDto corruptedResourceObject, DbResourceDto referenceResourceObject) {

        Set<String> resourceEntriesInCorruptedResourceObject = corruptedResourceObject.getEntries().stream()

                .map(DbResourceDto.Entry::getReference)

                .collect(toSet());

        Set<DbResourceDto.Entry> newResourceEntries = referenceResourceObject.getEntries().stream()

                .filter((referenceEntry) -> !resourceEntriesInCorruptedResourceObject.contains(referenceEntry.getReference()))

                .map((missingResourceEntry) -> DbResourceDto.Entry.builder()
                        .fromExistingEntry(missingResourceEntry)
                        .build())

                .collect(toSet());

        corruptedResourceObject.getEntries().addAll(newResourceEntries);
    }

    private void addContentItem(DbStructureDto.Field missingField, DbDataDto.Entry invalidEntry, DbDto topicObject) {

        int newFieldRank = missingField.getRank();
        List<DbDataDto.Item> items = invalidEntry.getItems();

        DbDataDto.Item newItem = buildDefaultContentItem(Optional.empty(), missingField, topicObject);
        items.add(newFieldRank - 1, newItem);

        // Rank update
        for (int i = newFieldRank ; i < items.size() ; i++) {
            items.get(i).shiftFieldRankRight();
        }
    }

    private void addResourceEntryWithDefaultValue(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {

        DbResourceDto resourceDto = bulkDatabaseMiner.getResourceFromTopicAndLocale(topic, locale);

        DbResourceDto.Entry newEntry = DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue(RESOURCE_VALUE_DEFAULT)
                .build();

        resourceDto.getEntries().add(newEntry);
    }

    private void addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {

        DbDto topicObject = bulkDatabaseMiner.getDatabaseTopic(topic);

        DbDataDto dataDto = topicObject.getData();

        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(dataDto.getEntries().size())
                .addItems(buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.getEntries().add(newEntry);
    }

    private List<DbDataDto.Item> buildDefaultContentItems(Optional<String> reference, DbDto topicObject) {

        return topicObject.getStructure().getFields().stream()

                    .map((structureField) -> buildDefaultContentItem(reference, structureField, topicObject))

                    .collect(toList());
    }

    private DbDataDto.Item buildDefaultContentItem(Optional<String> entryReference, DbStructureDto.Field field, DbDto topicObject) {

        String rawValue;

        DbStructureDto.FieldType fieldType = field.getFieldType();
        switch (fieldType) {
            case UID:
                if (entryReference.isPresent()) {
                    rawValue = entryReference.get();
                } else {
                    rawValue = DatabaseHelper.generateUniqueContentsEntryIdentifier(topicObject);
                }
                break;
            case BITFIELD:
                rawValue = "00000000";
                break;
            case FLOAT:
                rawValue = "0.0";
                break;
            case INTEGER:
                rawValue = "0";
                break;
            case PERCENT:
                rawValue = "1";
                break;
            case REFERENCE:
                // TODO fix missing reference afterwards - should not exist
                // FIXME should have remote structure and contents ....
                rawValue = DatabaseHelper.generateUniqueContentsEntryIdentifier(topicObject);
                break;
            case RESOURCE_CURRENT:
            case RESOURCE_CURRENT_AGAIN:
                rawValue = DatabaseHelper.generateUniqueResourceEntryIdentifier(topicObject);
                addDefaultResourceReferenceForAllLocales(rawValue, topicObject);
                break;
            case RESOURCE_REMOTE:
                // TODO fix missing reference afterwards - should not exist
                // TODO generate unique entry identifier, should have remote structure and contents ....
                rawValue = "999999";
                break;

            default:
                throw new IllegalArgumentException("Unhandled field type: " + fieldType);
        }

        return DbDataDto.Item.builder()
                .fromStructureField(field)
                .withRawValue(rawValue)
                .build();
    }

    private void addDefaultResourceReferenceForAllLocales(String resourceReference, DbDto topicObject) {
        asList(DbResourceDto.Locale.values()).stream()

                .forEach((locale) -> addResourceEntryWithDefaultValue(resourceReference, topicObject.getStructure().getTopic(), locale));
    }

    private static void checkRequirements(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        requireNonNull(dbDtos, "Database objects to be fixed are required.");
        requireNonNull(integrityErrors, "List of integrity errors is required.");
    }

    public List<DbDto> getFixedDbDtos() {
        return dbDtos;
    }

    List<DbDto> getDbDtos() {
        return dbDtos;
    }

    List<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }
}