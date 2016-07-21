package fr.tduf.libunlimited.high.files.db.patcher.helper;

import fr.tduf.libunlimited.high.files.db.common.helper.DatabaseGenHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import org.apache.commons.lang3.Range;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Component to handle placeholder values in patch instructions.
 */
public class PlaceholderResolver {

    private static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{(.+)\\}");
    private static final int FIELD_RANK_ID_CAR = 102;

    private final DbPatchDto patchObject;
    private final PatchProperties patchProperties;
    private final BulkDatabaseMiner databaseMiner;
    private final Set<String> generatedIdentifiers = new HashSet<>();

    private PlaceholderResolver(DbPatchDto patchObject, PatchProperties patchProperties, BulkDatabaseMiner databaseMiner) {
        this.patchObject = patchObject;
        this.patchProperties = patchProperties;
        this.databaseMiner = databaseMiner;
    }

    /**
     * @param patchObject         : patch to be processed
     * @param effectiveProperties : property set for current patch object
     * @param databaseMiner       : miner to perform operations on current database
     * @return resolver instance.
     */
    public static PlaceholderResolver load(DbPatchDto patchObject, PatchProperties effectiveProperties, BulkDatabaseMiner databaseMiner) {
        return new PlaceholderResolver(
                requireNonNull(patchObject, "Patch contents are required."),
                requireNonNull(effectiveProperties, "Patch properties are required."),
                requireNonNull(databaseMiner, "Database miner is required."));
    }

    /**
     * Main component entry point
     */
    public void resolveAllPlaceholders() {
        generatedIdentifiers.clear();

        resolveContentsReferencePlaceholders();

        resolveResourceReferencePlaceholders();

        resolveAllContentsValuesPlaceholders();

        resolveResourceValuePlaceholders();
    }

    private void resolveContentsReferencePlaceholders() {
        patchObject.getChanges().stream()
                .filter(changeObject -> DELETE == changeObject.getType()
                        || UPDATE == changeObject.getType())
                .filter(changeObject -> changeObject.getRef() != null)
                .forEach(changeObject -> {
                    final DbDto.Topic currentTopic = changeObject.getTopic();
                    DbDto topicObject = databaseMiner.getDatabaseTopic(currentTopic)
                            .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No database object found for topic: " + currentTopic));
                    String effectiveReference = resolveReferencePlaceholder(true, changeObject.getRef(), patchProperties, topicObject, generatedIdentifiers);
                    changeObject.setRef(effectiveReference);
                });
    }

    private void resolveResourceReferencePlaceholders() {
        patchObject.getChanges().stream()
                .filter(changeObject -> DELETE_RES == changeObject.getType()
                        || UPDATE_RES == changeObject.getType())
                .filter(changeObject -> changeObject.getRef() != null)
                .forEach(changeObject -> {
                    final DbDto.Topic currentTopic = changeObject.getTopic();
                    DbDto topicObject = databaseMiner.getDatabaseTopic(changeObject.getTopic())
                            .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No database object found for topic: " + currentTopic));
                    String effectiveReference = resolveReferencePlaceholder(false, changeObject.getRef(), patchProperties, topicObject, generatedIdentifiers);
                    changeObject.setRef(effectiveReference);
                });
    }

    private void resolveAllContentsValuesPlaceholders() {
        patchObject.getChanges().stream()
                .filter(changeObject -> UPDATE == changeObject.getType()
                        || DELETE == changeObject.getType())
                .forEach(changeObject -> {
                    resolveContentsValuesPlaceholders(changeObject);
                    resolveContentsPartialValuesPlaceholders(changeObject);
                    resolveContentsFilterValuesPlaceholders(changeObject);
                });
    }

    private void resolveResourceValuePlaceholders() {
        patchObject.getChanges().stream()
                .filter(changeObject -> UPDATE_RES == changeObject.getType())
                .filter(changeObject -> changeObject.getValue() != null)
                .forEach(changeObject -> {
                    String effectiveValue = resolveValuePlaceholder(changeObject.getValue(), patchProperties, databaseMiner);
                    changeObject.setValue(effectiveValue);
                });
    }

    private void resolveContentsValuesPlaceholders(DbPatchDto.DbChangeDto changeObject) {
        if (changeObject.getValues() == null) {
            return;
        }

        List<String> effectiveValues = changeObject.getValues().stream()
                .map(value -> resolveValuePlaceholder(value, patchProperties, databaseMiner))
                .collect(toList());

        changeObject.setValues(effectiveValues);
    }

    private void resolveContentsPartialValuesPlaceholders(DbPatchDto.DbChangeDto changeObject) {
        final List<DbFieldValueDto> effectivePartialValues = resolveFieldValuesPlaceholders(changeObject.getPartialValues());

        changeObject.setPartialValues(effectivePartialValues);
    }

    private void resolveContentsFilterValuesPlaceholders(DbPatchDto.DbChangeDto changeObject) {
        final List<DbFieldValueDto> effectiveFilterValues = resolveFieldValuesPlaceholders(changeObject.getFilterCompounds());

        changeObject.setFilterCompounds(effectiveFilterValues);
    }

    private List<DbFieldValueDto> resolveFieldValuesPlaceholders(List<DbFieldValueDto> fieldValues) {
        if (fieldValues == null) {
            return null;
        }

        return fieldValues.stream()
                .map(partialValue -> {
                    String effectiveValue = resolveValuePlaceholder(partialValue.getValue(), patchProperties, databaseMiner);
                    return DbFieldValueDto.fromCouple(partialValue.getRank(), effectiveValue);
                })
                .collect(toList());
    }

    static String resolveReferencePlaceholder(boolean forContents, String value, PatchProperties patchProperties, DbDto topicObject, Set<String> generatedIdentifiers) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if (matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseGet(() -> {
                        String uniqueValue = generateUniqueIdentifier(forContents, topicObject, generatedIdentifiers);
                        patchProperties.register(placeholderName, uniqueValue);
                        return uniqueValue;
                    });
        }

        return value;
    }

    static String resolveValuePlaceholder(String value, PatchProperties patchProperties, BulkDatabaseMiner miner) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);

        if (matcher.matches()) {
            final String placeholderName = matcher.group(1);
            final Optional<String> potentialValue = patchProperties.retrieve(placeholderName);

            if (potentialValue.isPresent()) {
                return potentialValue.get();
            }

            if (PlaceholderConstants.isPlaceholderForCarIdentifier(placeholderName)) {
                return generateValueForCARIDPlaceholder(miner);
            }

            throw new IllegalArgumentException("No property found for value placeholder: " + value);
        }

        return value;
    }

    private static String generateUniqueIdentifier(boolean forContents, DbDto topicObject, Set<String> generatedIdentifiers) {
        String uniqueValue = null;
        while (uniqueValue == null) {
            String generatedValue = forContents ?
                    DatabaseGenHelper.generateUniqueContentsEntryIdentifier(topicObject) :
                    DatabaseGenHelper.generateUniqueResourceEntryIdentifier(topicObject);

            if (!generatedIdentifiers.contains(generatedValue)) {
                uniqueValue = generatedValue;
            }
        }

        generatedIdentifiers.add(uniqueValue);
        return uniqueValue;
    }

    private static String generateValueForCARIDPlaceholder(BulkDatabaseMiner miner) {
        final DbDto topicObject = miner.getDatabaseTopic(CAR_PHYSICS_DATA)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No database object found for topic: CAR_PHYSICS_DATA"));
        final Set<String> allIdCars = topicObject.getData().getEntries().stream()
                .map(entry -> entry.getItemAtRank(FIELD_RANK_ID_CAR)
                        .<IllegalStateException>orElseThrow(() -> new IllegalStateException("No ID_CAR item found for entry id: " + entry.getId()))
                )
                .map(ContentItemDto::getRawValue)
                .collect(toSet());

        return DatabaseGenHelper.generateUniqueIdentifier(allIdCars, Range.between(8000, 9000));
    }
}
