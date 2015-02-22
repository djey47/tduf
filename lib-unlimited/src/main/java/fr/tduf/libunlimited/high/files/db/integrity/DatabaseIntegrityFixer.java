package fr.tduf.libunlimited.high.files.db.integrity;

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

/**
 * Class providing method to repair Database.
 */
public class DatabaseIntegrityFixer {

    private static final String RESOURCE_REF_DEFAULT = "-FIXED BY TDUF-";
    private final List<DbDto> dbDtos;

    private final List<IntegrityError> integrityErrors;

    // Following errors are auto-handled: CONTENT_ITEMS_COUNT_MISMATCH, STRUCTURE_FIELDS_COUNT_MISMATCH
    // Following errors are not handled yet: CONTENTS_FIELDS_COUNT_MISMATCH, RESOURCE_NOT_FOUND, RESOURCE_ITEMS_COUNT_MISMATCH
    private static final Set<IntegrityError.ErrorTypeEnum> FIXABLE_ERRORS = new HashSet<>(asList(RESOURCE_REFERENCE_NOT_FOUND, CONTENTS_REFERENCE_NOT_FOUND));
    private static final Set<IntegrityError.ErrorTypeEnum> UNFIXABLE_ERRORS = new HashSet<>(asList(CONTENTS_NOT_FOUND, CONTENTS_ENCRYPTION_NOT_SUPPORTED));


    private DatabaseIntegrityFixer(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        this.dbDtos = dbDtos;
        this.integrityErrors = integrityErrors;
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
        requireNonNull(remainingIntegrityErrors, "A list of integrity errors is required.");

        remainingIntegrityErrors.addAll(
                this.integrityErrors.stream()

                        .filter((integrityError) -> UNFIXABLE_ERRORS.contains(integrityError.getErrorTypeEnum()))

                        .collect(toList()));
    }

    private void handleFixableErrors(List<IntegrityError> remainingIntegrityErrors) {
        requireNonNull(remainingIntegrityErrors, "A list of integrity errors is required.");

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
            DbDto.Topic remoteTopic = (DbDto.Topic) information.get(ErrorInfoEnum.REMOTE_TOPIC);
            String reference = (String) information.get(ErrorInfoEnum.REFERENCE);

            switch(integrityError.getErrorTypeEnum()) {
                case RESOURCE_REFERENCE_NOT_FOUND:
                    fixResourceReference(reference, remoteTopic, (DbResourceDto.Locale) information.get(ErrorInfoEnum.LOCALE));
                    break;
                case CONTENTS_REFERENCE_NOT_FOUND:
                    fixContentsReference(reference, remoteTopic);
                    break;
                default:
                    throw new IllegalArgumentException("Integrity error type not handled yet: " + integrityError.getErrorTypeEnum());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void fixResourceReference(String reference, DbDto.Topic topic, DbResourceDto.Locale locale) {

        DbResourceDto resourceDto = this.dbDtos.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findFirst().get().getResources().stream()

                    .filter((resourceObject) -> resourceObject.getLocale() == locale)

                    .findFirst().get();

        DbResourceDto.Entry newEntry = DbResourceDto.Entry.builder()
                .forReference(reference)
                .withValue(RESOURCE_REF_DEFAULT)
                .build();

        resourceDto.getEntries().add(newEntry);
    }

    private void fixContentsReference(String reference, DbDto.Topic topic) {

        DbDataDto dataDto = this.dbDtos.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findFirst().get().getData();

        List<DbDataDto.Item> newItems = buildDefaultItems(reference, topic);

        DbDataDto.Entry newEntry = DbDataDto.Entry.builder()
                .forId(dataDto.getEntries().size())
                .addItems(newItems)
                .build();

        dataDto.getEntries().add(newEntry);
    }

    private List<DbDataDto.Item> buildDefaultItems(String reference, DbDto.Topic topic) {
        return this.dbDtos.stream()

                .filter((databaseObject) -> databaseObject.getStructure().getTopic() == topic)

                .findFirst().get().getStructure().getFields().stream()

                    .map((field) -> buildDefaultItem(field, reference))

                    .collect(toList());
    }

    private DbDataDto.Item buildDefaultItem(DbStructureDto.Field field, String reference) {

        String rawValue;

        switch (field.getFieldType()) {
            case UID:
                rawValue = reference;
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
                rawValue = "999999";
                break;
            case RESOURCE_CURRENT:
            case RESOURCE_CURRENT_AGAIN:
            case RESOURCE_REMOTE:
                // TODO fix missing reference afterwards - should not exist
                rawValue = "999999";
                break;

            default:
                throw new IllegalArgumentException("Unhandled field type: " + field.getFieldType());
        }

        return DbDataDto.Item.builder()
                .forName(field.getName())
                .ofFieldRank(field.getRank())
                .withRawValue(rawValue)
                .build();
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