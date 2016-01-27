package fr.tduf.libunlimited.high.files.db.patcher.helper;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Component to handle placeholder values in patch instructions.
 */
public class PlaceholderResolver {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{(.+)\\}");

    private final DbPatchDto patchObject;
    private final PatchProperties patchProperties;
    private final BulkDatabaseMiner databaseMiner;

    /**
     * @param patchObject           : patch to be processed
     * @param effectiveProperties   : property set for current patch object
     * @param databaseMiner         : miner to perform operations on current database
     * @return resolver instance.
     */
    public static PlaceholderResolver load(DbPatchDto patchObject, PatchProperties effectiveProperties, BulkDatabaseMiner databaseMiner) {
       return new PlaceholderResolver(
               requireNonNull(patchObject, "Patch contents are required."),
               requireNonNull(effectiveProperties, "Patch properties are required."),
               requireNonNull(databaseMiner, "Database miner is required."));
    }

    private PlaceholderResolver(DbPatchDto patchObject, PatchProperties patchProperties, BulkDatabaseMiner databaseMiner) {
        this.patchObject = patchObject;
        this.patchProperties = patchProperties;
        this.databaseMiner = databaseMiner;
    }

    /**
     *
     */
    public void resolveAllPlaceholders() {

        resolveContentsReferencePlaceholders();

        resolveResourceReferencePlaceholders();

        resolveContentsValuesPlaceholders();

        resolveResourceValuePlaceholders();
    }

    private void resolveContentsReferencePlaceholders() {

        patchObject.getChanges().stream()

                .filter((changeObject) -> DELETE == changeObject.getType()
                        || UPDATE == changeObject.getType())

                .filter((changeObject) -> changeObject.getRef() != null)

                .forEach((changeObject) -> {
                    DbDto topicObject = databaseMiner.getDatabaseTopic(changeObject.getTopic()).get();
                    String effectiveReference = resolveContentsReferencePlaceholder(changeObject.getRef(), patchProperties, of(topicObject));
                    changeObject.setRef(effectiveReference);
                });
    }

    private void resolveResourceReferencePlaceholders() {

        patchObject.getChanges().stream()

                .filter((changeObject) -> DELETE_RES == changeObject.getType()
                        || UPDATE_RES == changeObject.getType())

                .filter((changeObject) -> changeObject.getRef() != null)

                .forEach((changeObject) -> {
                    DbDto topicObject = databaseMiner.getDatabaseTopic(changeObject.getTopic()).get();
                    String effectiveReference = resolveResourceReferencePlaceholder(changeObject.getRef(), patchProperties, of(topicObject));
                    changeObject.setRef(effectiveReference);
                });
    }

    private void resolveContentsValuesPlaceholders() {

        patchObject.getChanges().stream()

                .filter((changeObject) -> UPDATE == changeObject.getType())

                .forEach((changeObject) -> {
                    resolveContentsValuesPlaceholders(changeObject);
                    resolveContentsPartialValuesPlaceholders(changeObject);
                });
    }

    private void resolveResourceValuePlaceholders() {

        patchObject.getChanges().stream()

                .filter((changeObject) -> UPDATE_RES == changeObject.getType())

                .filter((changeObject) -> changeObject.getValue() != null)

                .forEach((changeObject) -> {
                    String effectiveValue = resolveResourceValuePlaceholder(changeObject.getValue(), patchProperties);
                    changeObject.setValue(effectiveValue);
                });
    }

    private void resolveContentsValuesPlaceholders(DbPatchDto.DbChangeDto changeObject) {
        if (changeObject.getValues() == null) {
            return;
        }

        List<String> effectiveValues = changeObject.getValues().stream()

                .map((value) -> resolveContentsReferencePlaceholder(value, patchProperties, empty()))

                .collect(toList());

        changeObject.setValues(effectiveValues);
    }

    // TODO
    private void resolveContentsPartialValuesPlaceholders(DbPatchDto.DbChangeDto changeObject) {
        if (changeObject.getPartialValues() == null) {
            return;
        }

    }

    // TODO factorize methods below
    static String resolveContentsReferencePlaceholder(String value, PatchProperties patchProperties, Optional<DbDto> topicObject) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if(matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseGet(() -> {
                        if (topicObject.isPresent()) {
                            // TODO take newly generated values into account for unicity
                            final String generatedValue = DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject.get());
                            patchProperties.register(placeholderName, generatedValue);
                            return generatedValue;
                        }

                        return value;
                    });
        }

        return value;
    }

    static String resolveResourceReferencePlaceholder(String value, PatchProperties patchProperties, Optional<DbDto> topicObject) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if(matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseGet(() -> {
                        if (topicObject.isPresent()) {
                            // TODO take newly generated values into account for unicity
                            final String generatedValue = DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject.get());
                            patchProperties.register(placeholderName, generatedValue);
                            return generatedValue;
                        }

                        return value;
                    });
        }

        return value;
    }

    static String resolveResourceValuePlaceholder(String value, PatchProperties patchProperties) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if(matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)

                    .orElseThrow(() -> new IllegalArgumentException("No property found for value placeholder: " + value));
        }

        return value;
    }
}
