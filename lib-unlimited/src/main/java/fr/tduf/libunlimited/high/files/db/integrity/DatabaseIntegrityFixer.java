package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Class providing method to repair Database.
 */
public class DatabaseIntegrityFixer extends AbstractDatabaseHolder {

    private static final String RESOURCE_VALUE_DEFAULT = "-FIXED BY TDUF-";

    private Set<IntegrityError> integrityErrors;

    // Following errors are auto-handled: CONTENT_ITEMS_COUNT_MISMATCH, STRUCTURE_FIELDS_COUNT_MISMATCH
    private static final Set<IntegrityError.ErrorTypeEnum> FIXABLE_ERRORS = new HashSet<>(asList(RESOURCE_NOT_FOUND, RESOURCE_ITEMS_COUNT_MISMATCH, RESOURCE_REFERENCE_NOT_FOUND, CONTENTS_REFERENCE_NOT_FOUND, CONTENTS_FIELDS_COUNT_MISMATCH, RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES));
    private static final Set<IntegrityError.ErrorTypeEnum> UNFIXABLE_ERRORS = new HashSet<>(asList(INCOMPLETE_DATABASE, CONTENTS_NOT_FOUND, CONTENTS_ENCRYPTION_NOT_SUPPORTED));

    private DatabaseGenHelper genHelper;

    /**
     * Process fixing over all loaded database objects.
     * @param integrityErrors : integrity errors to fix.
     * @return list of remaining integrity errors.
     */
    public Set<IntegrityError> fixAllContentsObjects(Set<IntegrityError> integrityErrors) {

        this.integrityErrors = requireNonNull(integrityErrors, "A list of integrity errors is required.");

        Set<IntegrityError> remainingIntegrityErrors = new LinkedHashSet<>();

        if(this.integrityErrors.isEmpty()) {
            return remainingIntegrityErrors;
        }

        handleUnfixableErrors(remainingIntegrityErrors);

        handleFixableErrors(remainingIntegrityErrors);

        return remainingIntegrityErrors;
    }

    @Override
    protected void postPrepare() {
        genHelper = new DatabaseGenHelper(databaseMiner);
    }

    private void handleUnfixableErrors(Set<IntegrityError> remainingIntegrityErrors) {
        remainingIntegrityErrors.addAll(
                this.integrityErrors.stream()

                        .filter((integrityError) -> UNFIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toList()));
    }

    private void handleFixableErrors(Set<IntegrityError> remainingIntegrityErrors) {
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
            DbDto.Topic sourceTopic = (DbDto.Topic) information.get(SOURCE_TOPIC);
            DbDto.Topic remoteTopic = (DbDto.Topic) information.get(REMOTE_TOPIC);
            String reference = (String) information.get(REFERENCE);
            Long entryIdentifier = (Long) information.get(ENTRY_ID);
            DbResourceDto.Locale locale = (DbResourceDto.Locale) information.get(ErrorInfoEnum.LOCALE);
            Map<DbResourceDto.Locale, Integer> perLocaleCount = (Map<DbResourceDto.Locale, Integer>) information.get(PER_LOCALE_COUNT);
            Map<String, Integer> perValueCount = (Map<String, Integer>) information.get(PER_VALUE_COUNT);

            switch(integrityError.getErrorTypeEnum()) {
                case RESOURCE_REFERENCE_NOT_FOUND:
                    addResourceEntryFromValidLocale(reference, remoteTopic, locale);
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
                case RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES:
                    fixAllResourceEntryValues(reference, perValueCount, sourceTopic);
                    break;
                default:
                    throw new IllegalArgumentException("Kind of integrity error not handled yet: " + integrityError.getErrorTypeEnum());
            }

            BulkDatabaseMiner.clearAllCaches();
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

        List<DbResourceDto> topicResourceObjects = databaseMiner.getAllResourcesFromTopic(topic).get();

        DbResourceDto referenceResourceObject = databaseMiner.getResourceFromTopicAndLocale(topic, referenceLocale).get();

        topicResourceObjects.stream()

                    .filter((resourceObject) -> resourceObject.getEntries().size() < referenceResourceCount)

                    .forEach((corruptedResourceObject) -> addMissingResourceEntries(corruptedResourceObject, referenceResourceObject));
    }

    private void addResourceLocaleFromValidResource(DbResourceDto.Locale missingLocale, DbDto.Topic topic) throws Exception {
        Set<DbResourceDto.Locale> validResourceLocales = findValidResourceLocales();

        if (validResourceLocales.isEmpty()) {
            throw new Exception("Unable to build missing locale " + missingLocale + ": no valid resource locale exists.");
        }

        DbResourceDto.Locale referenceLocale = pickAvailableLocaleOrElseWhatever(DbResourceDto.Locale.UNITED_STATES, validResourceLocales);
        DbResourceDto referenceResourceObject = databaseMiner.getResourceFromTopicAndLocale(topic, referenceLocale).get();

        databaseMiner.getDatabaseTopic(topic).get().getResources().add(DbResourceDto.builder()
                .fromExistingResource(referenceResourceObject)
                .withLocale(missingLocale)
                .build());
    }

    private void addMissingContentsFields(long entryInternalIdentifier, DbDto.Topic topic) {
        DbDto topicObject = databaseMiner.getDatabaseTopic(topic).get();

        DbDataDto.Entry invalidEntry = databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryInternalIdentifier, topic).get();

        // Structure is the reference
        topicObject.getStructure().getFields().stream()

                .filter((field) -> invalidEntry.getItems().stream()

                        .map(DbDataDto.Item::getName)

                        .filter((name) -> name.equals(field.getName()))

                        .count() == 0)

                .forEach((missingField) -> addContentItem(missingField, invalidEntry, topicObject));
    }

    private void fixAllResourceEntryValues(String resourceReference, Map<String, Integer> perValueCount, DbDto.Topic topic) {

        String mostFrequentValue = perValueCount.entrySet().stream()

                .max(Comparator.comparing(Map.Entry::getValue))

                .get().getKey();

        databaseMiner.getAllResourcesFromTopic(topic).get().stream()

                .map(DbResourceDto::getLocale)

                .forEach((locale) -> databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(resourceReference, topic, locale)

                        .ifPresent((resourceEntry) -> {
                            if (!mostFrequentValue.equals(resourceEntry.getValue())) {
                                resourceEntry.setValue(mostFrequentValue);
                            }
                        })
                );
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
        DbDataDto.Item newItem = genHelper.buildDefaultContentItem(Optional.empty(), missingField, topicObject, true);
        invalidEntry.addItemAtRank(newFieldRank, newItem);
    }

    private void addResourceEntryFromValidLocale(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {
        Set<DbResourceDto.Locale> validResourceLocales = findValidResourceLocales();

        String resourceValue = RESOURCE_VALUE_DEFAULT;
        if ( !validResourceLocales.isEmpty()) {

            DbResourceDto.Locale referenceLocale = pickAvailableLocaleOrElseWhatever(DbResourceDto.Locale.UNITED_STATES, validResourceLocales);
            Optional<DbResourceDto.Entry> referenceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(reference, topic, referenceLocale);

            if (referenceEntry.isPresent()){
                resourceValue = referenceEntry.get().getValue();
            }
        }

        DbResourceDto.Entry newEntry = DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue(resourceValue)
                .build();

        DbResourceDto resourceDto = databaseMiner.getResourceFromTopicAndLocale(topic, locale).get();
        resourceDto.getEntries().add(newEntry);
    }

    private void addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {

        DbDto topicObject = databaseMiner.getDatabaseTopic(topic).get();

        DbDataDto dataDto = topicObject.getData();

        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(dataDto.getEntries().size())
                .addItems(genHelper.buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.addEntry(newEntry);
    }

    private Set<DbResourceDto.Locale> findValidResourceLocales() {
        return Stream.of(DbResourceDto.Locale.values())

                .filter((locale) -> !this.integrityErrors.stream()

                        .filter((integrityError) -> integrityError.getErrorTypeEnum() == IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND
                                || integrityError.getErrorTypeEnum() == IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND)

                        .map((resourceIntegrityError) -> (DbResourceDto.Locale) resourceIntegrityError.getInformation().get(ErrorInfoEnum.LOCALE))

                        .collect(toSet())

                        .contains(locale))

                .collect(toSet());
    }

    private static DbResourceDto.Locale pickAvailableLocaleOrElseWhatever(DbResourceDto.Locale locale, Set<DbResourceDto.Locale> validResourceLocales) {
        if (validResourceLocales.contains(locale)) {
            return locale;
        }

        return  validResourceLocales.stream().findFirst().get();
    }

    Set<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }

    void setGenHelper(DatabaseGenHelper genHelper) {
        this.genHelper = genHelper;
    }
}
