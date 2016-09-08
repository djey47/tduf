package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;

import java.util.*;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Class providing method to repair Database.
 */
public class DatabaseIntegrityFixer extends AbstractDatabaseHolder {

    private static final String RESOURCE_VALUE_DEFAULT = "-FIXED BY TDUF-";

    private Set<IntegrityError> integrityErrors;

    // Following errors are auto-handled: CONTENT_ITEMS_COUNT_MISMATCH, STRUCTURE_FIELDS_COUNT_MISMATCH
    private static final Set<IntegrityError.ErrorTypeEnum> FIXABLE_ERRORS = new HashSet<>(asList(RESOURCE_NOT_FOUND, RESOURCE_REFERENCE_NOT_FOUND, CONTENTS_REFERENCE_NOT_FOUND, CONTENTS_FIELDS_COUNT_MISMATCH, RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES));
    private static final Set<IntegrityError.ErrorTypeEnum> UNFIXABLE_ERRORS = new HashSet<>(asList(INCOMPLETE_DATABASE, CONTENTS_NOT_FOUND, CONTENTS_ENCRYPTION_NOT_SUPPORTED));

    private DatabaseGenHelper genHelper;

    /**
     * Process fixing over all loaded database objects.
     *
     * @param integrityErrors : integrity errors to fix.
     * @return list of remaining integrity errors.
     */
    public Set<IntegrityError> fixAllContentsObjects(Set<IntegrityError> integrityErrors) {

        this.integrityErrors = requireNonNull(integrityErrors, "A list of integrity errors is required.");

        Set<IntegrityError> remainingIntegrityErrors = new LinkedHashSet<>();

        if (this.integrityErrors.isEmpty()) {
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

                        .filter(integrityError -> UNFIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toList()));
    }

    private void handleFixableErrors(Set<IntegrityError> remainingIntegrityErrors) {
        remainingIntegrityErrors.addAll(
                this.integrityErrors.stream()

                        .filter(integrityError -> FIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toMap(integrityError -> integrityError, this::fixIntegrityError))

                        .entrySet().stream()

                        .filter(resultEntry -> !resultEntry.getValue())

                        .map(Map.Entry::getKey)

                        .collect(toList())
        );
    }

    private boolean fixIntegrityError(IntegrityError integrityError) {

        try {
            Map<ErrorInfoEnum, Object> information = integrityError.getInformation();

            DbDto.Topic sourceTopic = (DbDto.Topic) information.get(SOURCE_TOPIC);
            Optional<DbDto.Topic> remoteTopic = Optional.ofNullable((DbDto.Topic) information.get(REMOTE_TOPIC));
            String reference = (String) information.get(REFERENCE);
            int entryIdentifier = (Integer) information.get(ENTRY_ID);
            fr.tduf.libunlimited.common.game.domain.Locale locale = (Locale) information.get(ErrorInfoEnum.LOCALE);
            Optional<Set<Locale>> missingLocales = Optional.ofNullable ((Set<Locale>) information.get(MISSING_LOCALES));
            Map<String, Integer> perValueCount = (Map<String, Integer>) information.get(PER_VALUE_COUNT);

            switch (integrityError.getErrorTypeEnum()) {
                case RESOURCE_REFERENCE_NOT_FOUND:
                    addResourceValueFromValidLocale(reference, remoteTopic.orElse(sourceTopic), missingLocales);
                    break;
                case CONTENTS_REFERENCE_NOT_FOUND:
                    addContentsEntryWithDefaultItems(Optional.of(reference), remoteTopic
                            .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No remote topic for a contents reference"))
                    );
                    break;
                case CONTENTS_FIELDS_COUNT_MISMATCH:
                    addMissingContentsFields(entryIdentifier, sourceTopic);
                    break;
                case RESOURCE_NOT_FOUND:
                    addResourceLocaleFromValidResource(locale, sourceTopic);
                    break;
                case RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES:
                    fixAllResourceEntryValues(reference, perValueCount, sourceTopic);
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

    private void addResourceLocaleFromValidResource(Locale missingLocale, DbDto.Topic topic) throws Exception {
        Set<Locale> validResourceLocales = findValidResourceLocales();
        if (validResourceLocales.isEmpty()) {
            throw new Exception("Unable to build missing locale " + missingLocale + ": no valid resource locale exists.");
        }

        Locale referenceLocale = pickAvailableLocaleOrElseWhatever(UNITED_STATES, validResourceLocales);
        databaseMiner.getResourcesFromTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No resources for topic: " + topic))
                .getEntries()
                .forEach(entry -> {
                    String referenceValue = entry.getValueForLocale(referenceLocale)
                            .orElse(RESOURCE_VALUE_DEFAULT);
                    entry.setValueForLocale(referenceValue, missingLocale);
                });
    }

    private void addMissingContentsFields(int entryInternalIdentifier, DbDto.Topic topic) {
        DbDto topicObject = databaseMiner.getDatabaseTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No contents for topic: " + topic));

        ContentEntryDto invalidEntry = databaseMiner.getContentEntryFromTopicWithInternalIdentifier(entryInternalIdentifier, topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("Invalid entry not found at id: " + entryInternalIdentifier));

        // Structure is the reference
        topicObject.getStructure().getFields().stream()
                .filter(field -> invalidEntry.getItems().stream()
                        .map(ContentItemDto::getFieldRank)
                        .filter(rank -> rank == field.getRank())
                        .count() == 0)
                .forEach(missingField -> addContentItem(missingField, invalidEntry, topicObject));
    }

    private void fixAllResourceEntryValues(String resourceReference, Map<String, Integer> perValueCount, DbDto.Topic topic) {
        String mostFrequentValue = perValueCount.entrySet().stream()
                .max(comparing(Map.Entry::getValue))
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No maximum per-value entry count in topic: " + topic))
                .getKey();

        databaseMiner.getResourcesFromTopic(topic)
                .flatMap(resources -> resources.getEntryByReference(resourceReference))
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No resource from topic: " + topic + " at ref: " + resourceReference))
                .setValue(mostFrequentValue);
    }

    private void addContentItem(DbStructureDto.Field missingField, ContentEntryDto invalidEntry, DbDto topicObject) {
        int newFieldRank = missingField.getRank();
        ContentItemDto newItem = genHelper.buildDefaultContentItem(Optional.empty(), missingField, topicObject);
        invalidEntry.addItemAtRank(newFieldRank, newItem);
    }

    private void addResourceValueFromValidLocale(String reference, DbDto.Topic topic, Optional<Set<Locale>> missingLocales) throws Exception {
        Set<Locale> validResourceLocales = findValidResourceLocales();
        if (validResourceLocales.isEmpty()) {
            throw new Exception("Unable to add value for locales " + missingLocales + ": no valid resource locale exists.");
        }

        Locale referenceLocale = pickAvailableLocaleOrElseWhatever(UNITED_STATES, validResourceLocales);
        DbResourceDto resourceObject = databaseMiner.getResourcesFromTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No resources for topic: " + topic));
        ResourceEntryDto entry = resourceObject
                .getEntryByReference(reference)
                // Use supplier to only invoke addEntry when result is absent
                .orElseGet(() -> resourceObject.addEntryByReference(reference));

        missingLocales.orElse(Locale.valuesAsStream().collect(toSet())).stream()
                .forEach(missingLocale -> {
                    String referenceValue = entry.getValueForLocale(referenceLocale)
                            .orElse(RESOURCE_VALUE_DEFAULT);
                    entry.setValueForLocale(referenceValue, missingLocale);
        });
    }

    private void addContentsEntryWithDefaultItems(Optional<String> reference, DbDto.Topic topic) {
        DbDto topicObject = databaseMiner.getDatabaseTopic(topic)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No contents for topic: " + topic));

        DbDataDto dataDto = topicObject.getData();

        ContentEntryDto newEntry = ContentEntryDto.builder()
                .addItems(genHelper.buildDefaultContentItems(reference, topicObject))
                .build();

        dataDto.addEntry(newEntry);
    }

    private Set<Locale> findValidResourceLocales() {
        return Locale.valuesAsStream()
                .filter(locale -> !this.integrityErrors.stream()
                        .filter(integrityError -> integrityError.getErrorTypeEnum() == IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND
                                || integrityError.getErrorTypeEnum() == IntegrityError.ErrorTypeEnum.RESOURCE_NOT_FOUND)
                        .map(resourceIntegrityError -> (Locale) resourceIntegrityError.getInformation().get(ErrorInfoEnum.LOCALE))
                        .collect(toSet())
                        .contains(locale))
                .collect(toSet());
    }

    private static Locale pickAvailableLocaleOrElseWhatever(Locale locale, Set<Locale> validResourceLocales) {
        if (validResourceLocales.contains(locale)) {
            return locale;
        }

        return validResourceLocales.stream().findFirst()
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No valid resource locale available"));
    }

    Set<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }

    void setGenHelper(DatabaseGenHelper genHelper) {
        this.genHelper = genHelper;
    }
}
