package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.*;

import java.util.*;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.*;
import static java.util.Collections.synchronizedSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Class providing methods to check Database integrity.
 */
public class DatabaseIntegrityChecker extends AbstractDatabaseHolder {

    private Map<String, DbDto> topicObjectsByReferences;
    private Map<DbDto, Map<Integer, DbStructureDto.Field>> fieldsByRanksByTopicObjects;

    /**
     * Process checking over all loaded database objects.
     * Beware! This piece of code is very CPU-intelnsive on a complete database
     * and takes a couple of minutes on a modern processor!
     *
     * @return set of integrity errors.
     */
    public Set<IntegrityError> checkAllContentsObjects() {
        Set<IntegrityError> integrityErrors = synchronizedSet(new HashSet<>());

        checkRequirements(integrityErrors);
        if (!integrityErrors.isEmpty()) {
            return integrityErrors;
        }

        buildIndexes();

        databaseObjects.stream()

                .parallel()

                .forEach((localTopicObject) -> checkContentsObject(localTopicObject, integrityErrors));

        return integrityErrors;
    }

    @Override
    protected void postPrepare() {
    }

    private void checkRequirements(Set<IntegrityError> integrityErrors) {
        requireNonNull(integrityErrors, "A set of integrity errors (even empty) is required.");

        checkIfAllTopicObjectsPresent(integrityErrors);
    }

    private void checkIfAllTopicObjectsPresent(Set<IntegrityError> integrityErrors) {
        Set<DbDto.Topic> absentTopics = DbDto.Topic.valuesAsStream()

                .filter((topicEnum) -> !databaseMiner.getDatabaseTopic(topicEnum).isPresent())

                .collect(toSet());

        if (!absentTopics.isEmpty()) {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(MISSING_TOPICS, absentTopics);

            integrityErrors.add(IntegrityError.builder()
                    .ofType(INCOMPLETE_DATABASE)
                    .addInformations(informations)
                    .build());
        }
    }

    private void checkContentsObject(DbDto contentsObject, Set<IntegrityError> integrityErrors) {

        Map<Integer, DbStructureDto.Field> fieldsByRankIndex = fieldsByRanksByTopicObjects.get(contentsObject);

        contentsObject.getData().getEntries().stream()

                .forEach((entry) -> entry.getItems().stream()

                        .forEach((item) -> checkContentsItem(item, contentsObject, fieldsByRankIndex, integrityErrors)));
    }

    private void checkContentsItem(DbDataDto.Item item, DbDto localTopicObject, Map<Integer, DbStructureDto.Field> fieldsByRanksIndex, Set<IntegrityError> integrityErrors) {
        DbStructureDto.Field field = fieldsByRanksIndex.get(item.getFieldRank());
        String targetRef = field.getTargetRef();

        DbDto remoteTopicObject = null;
        if (targetRef != null) {
            remoteTopicObject = topicObjectsByReferences.get(targetRef);
        }

        DbDto.Topic currentTopic = localTopicObject.getTopic();

        switch (field.getFieldType()) {
            case REFERENCE:
                integrityErrors.addAll(checkContentsReference(item.getRawValue(), remoteTopicObject, currentTopic));
                break;
            case RESOURCE_CURRENT_GLOBALIZED:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), localTopicObject, currentTopic, true));
                break;
            case RESOURCE_CURRENT_LOCALIZED:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), localTopicObject, currentTopic, false));
                break;
            case RESOURCE_REMOTE:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), remoteTopicObject, currentTopic, false));
                break;
            default:
                break;
        }
    }

    private Set<IntegrityError> checkResourceReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic, boolean globalizedResource) {
        Set<IntegrityError> integrityErrors = new HashSet<>();

        DbDto.Topic currentTopic = topicObject.getTopic();
        final Optional<DbResourceDto.Entry> potentialResourceEntry = databaseMiner.getResourceEntryFromTopicAndReference(currentTopic, reference);
        if (potentialResourceEntry.isPresent()) {
            final DbResourceDto.Entry resourceEntry = potentialResourceEntry.get();

            checkForMissingLocalizedValues(resourceEntry, sourceTopic, currentTopic, integrityErrors);

            if (globalizedResource) {
                checkResourceValuesForReference(resourceEntry, sourceTopic, integrityErrors);
            }

        } else {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(SOURCE_TOPIC, sourceTopic);
            if (sourceTopic != currentTopic) {
                informations.put(REMOTE_TOPIC, currentTopic);
            }
            informations.put(REFERENCE, reference);

            integrityErrors.add(IntegrityError.builder()
                    .ofType(RESOURCE_REFERENCE_NOT_FOUND)
                    .addInformations(informations)
                    .build()
            );
        }

        return integrityErrors;
    }

    private Set<IntegrityError> checkContentsReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        Map<Integer, DbStructureDto.Field> fieldsByRanks = fieldsByRanksByTopicObjects.get(topicObject);

        Set<IntegrityError> integrityErrors = new HashSet<>();

        boolean isReferenceFound = topicObject.getData().getEntries().stream()

                .filter((entry) -> entry.getItems().stream()

                        .filter((item) -> fieldsByRanks
                                .get(item.getFieldRank())
                                .getFieldType() == DbStructureDto.FieldType.UID
                                && item.getRawValue().equals(reference))

                        .findFirst()

                        .isPresent()
                )

                .findFirst()

                .isPresent();

        if (!isReferenceFound) {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(SOURCE_TOPIC, sourceTopic);
            informations.put(REMOTE_TOPIC, topicObject.getTopic());
            informations.put(REFERENCE, reference);

            integrityErrors.add(IntegrityError.builder()
                    .ofType(CONTENTS_REFERENCE_NOT_FOUND)
                    .addInformations(informations)
                    .build()
            );
        }

        return integrityErrors;
    }

    private void buildIndexes() {

        // Topic by reference
        topicObjectsByReferences = databaseObjects.stream()

                .collect(toMap((dto) -> dto.getStructure().getRef(), (dto) -> dto));

        // Structure
        fieldsByRanksByTopicObjects = databaseObjects.stream()

                .collect(toMap((dto) -> dto, this::buildFieldIndex));
    }

    private Map<Integer, DbStructureDto.Field> buildFieldIndex(DbDto dto) {
        return dto.getStructure().getFields().stream()

                .collect(toMap(DbStructureDto.Field::getRank, (field) -> field));
    }

    private static void checkForMissingLocalizedValues(DbResourceDto.Entry resourceEntry, DbDto.Topic sourceTopic, DbDto.Topic remoteTopic, Set<IntegrityError> integrityErrors) {
        final Set<fr.tduf.libunlimited.common.game.domain.Locale> missingLocales = resourceEntry.getMissingLocales();
        if (missingLocales.isEmpty()) {
            return;
        }

        Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
        informations.put(SOURCE_TOPIC, sourceTopic);
        if (sourceTopic != remoteTopic) {
            informations.put(REMOTE_TOPIC, remoteTopic);
        }
        informations.put(REFERENCE, resourceEntry.getReference());
        informations.put(MISSING_LOCALES, missingLocales);

        integrityErrors.add(IntegrityError.builder()
                .ofType(RESOURCE_REFERENCE_NOT_FOUND)
                .addInformations(informations)
                .build()
        );
    }

    private static void updateResourceValueCounter(Map<String, Integer> resourceValueCounter, String resourceValue) {
        int valueCount = 0;
        if (resourceValueCounter.containsKey(resourceValue)) {
            valueCount = resourceValueCounter.get(resourceValue);
        }
        resourceValueCounter.put(resourceValue, ++valueCount);
    }

    private static void checkResourceValuesForReference(DbResourceDto.Entry resourceEntry, DbDto.Topic sourceTopic, Set<IntegrityError> integrityErrors) {
        Map<String, Integer> resourceValueCounter = new HashMap<>();
        resourceEntry.getPresentLocales()
                .forEach((presentLocale) -> {
                    final Optional<String> resourceValue = resourceEntry.getValueForLocale(presentLocale);
                    updateResourceValueCounter(resourceValueCounter, resourceValue.get());
                });

        if (resourceValueCounter.size() > 1) {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(SOURCE_TOPIC, sourceTopic);
            informations.put(REFERENCE, resourceEntry.getReference());
            informations.put(PER_VALUE_COUNT, resourceValueCounter);

            integrityErrors.add(IntegrityError.builder()
                    .ofType(RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES)
                    .addInformations(informations)
                    .build()
            );
        }
    }

    Map<String, DbDto> getTopicObjectsByReferences() {
        return topicObjectsByReferences;
    }

    Map<DbDto, Map<Integer, DbStructureDto.Field>> getFieldsByRanksByTopicObjects() {
        return fieldsByRanksByTopicObjects;
    }
}
