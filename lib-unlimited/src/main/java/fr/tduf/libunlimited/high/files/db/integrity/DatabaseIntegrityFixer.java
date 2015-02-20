package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

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

    private final List<DbDto> dbDtos;
    private List<DbDto> fixedDbDtos = null;

    private final List<IntegrityError> integrityErrors;

    // Following errors are auto-handled: CONTENT_ITEMS_COUNT_MISMATCH, STRUCTURE_FIELDS_COUNT_MISMATCH
    // Following errors are not handled yet: CONTENTS_FIELDS_COUNT_MISMATCH, RESOURCE_NOT_FOUND, RESOURCE_REFERENCE_NOT_FOUND, RESOURCE_ITEMS_COUNT_MISMATCH
    private static final Set<IntegrityError.ErrorTypeEnum> FIXABLE_ERRORS = new HashSet<>(asList(CONTENTS_REFERENCE_NOT_FOUND));
    private static final Set<IntegrityError.ErrorTypeEnum> UNFIXABLE_ERRORS = new HashSet<>(asList(CONTENTS_NOT_FOUND, CONTENTS_ENCRYPTION_NOT_SUPPORTED));


    private DatabaseIntegrityFixer(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        this.dbDtos = dbDtos;
        this.fixedDbDtos = dbDtos;
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
        return false;
    }

    private static void checkRequirements(List<DbDto> dbDtos, List<IntegrityError> integrityErrors) {
        requireNonNull(dbDtos, "Database objects to be fixed are required.");
        requireNonNull(integrityErrors, "List of integrity errors is required.");
    }

    public List<DbDto> getFixedDbDtos() {
        return fixedDbDtos;
    }

    List<DbDto> getDbDtos() {
        return dbDtos;
    }

    List<IntegrityError> getIntegrityErrors() {
        return integrityErrors;
    }
}