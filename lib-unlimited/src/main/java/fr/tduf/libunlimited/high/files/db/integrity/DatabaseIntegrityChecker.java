package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.*;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
     * @return list of integrity errors.
     */
    public List<IntegrityError> checkAllContentsObjects() {

        checkRequirements();

        buildIndexes();

        List<IntegrityError> integrityErrors = new ArrayList<>();

        databaseObjects.stream()

                .forEach((localTopicObject) -> checkContentsObject(localTopicObject, integrityErrors));

        return integrityErrors;
    }

    @Override
    protected void postPrepare() {
    }

    private void checkRequirements() {
        checkIfAllTopicObjectsPresent();
    }

    private void checkIfAllTopicObjectsPresent() {
        List<DbDto.Topic> absentTopics = Stream.of(DbDto.Topic.values())

                .filter((topicEnum) -> !databaseMiner.getDatabaseTopic(topicEnum).isPresent())

                .collect(toList());

        if (!absentTopics.isEmpty()) {
            throw new IllegalArgumentException("Missing one or more database topics: " + absentTopics);
        }
    }

    private void checkContentsObject(DbDto contentsObject, List<IntegrityError> integrityErrors) {

        Map<Integer, DbStructureDto.Field> fieldsByRankIndex = fieldsByRanksByTopicObjects.get(contentsObject);

        contentsObject.getData().getEntries().stream()

                .forEach((entry) -> entry.getItems().stream()

                        .forEach((item) -> checkContentsItem(item, contentsObject, fieldsByRankIndex, integrityErrors)));
    }

    private void checkContentsItem(DbDataDto.Item item, DbDto localTopicObject, Map<Integer, DbStructureDto.Field> fieldsByRanksIndex, List<IntegrityError> integrityErrors) {
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

    private List<IntegrityError> checkResourceReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic, boolean globalizedResource) {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        Map<String, Integer> resourceValueCounter = new HashMap<>();

        topicObject.getResources().stream()

                .forEach((resourceDto) -> checkLocalizedResourceObject(resourceDto, reference, topicObject, sourceTopic, globalizedResource, integrityErrors, resourceValueCounter));

        if (globalizedResource) {
            checkResourceValuesForReference(reference, sourceTopic, integrityErrors, resourceValueCounter);
        }

        return integrityErrors;
    }

    private void checkLocalizedResourceObject(DbResourceDto resourceDto, String reference, DbDto topicObject, DbDto.Topic sourceTopic, boolean globalizedResource, List<IntegrityError> integrityErrors, Map<String, Integer> resourceValueCounter) {
        Optional<DbResourceDto.Entry> potentialResourceEntry = databaseMiner.getResourceEntryFromTopicAndLocaleWithReference(reference, topicObject.getStructure().getTopic(), resourceDto.getLocale());

        boolean isResourceReferenceFound = potentialResourceEntry.isPresent();
        if (isResourceReferenceFound) {
            if (globalizedResource) {
                updateResourceValueCounter(resourceValueCounter, potentialResourceEntry);
            }
        } else {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(SOURCE_TOPIC, sourceTopic);
            informations.put(REMOTE_TOPIC, topicObject.getTopic());
            informations.put(LOCALE, resourceDto.getLocale());
            informations.put(REFERENCE, reference);

            integrityErrors.add(IntegrityError.builder()
                            .ofType(RESOURCE_REFERENCE_NOT_FOUND)
                            .addInformations(informations)
                            .build()
            );
        }
    }

    private List<IntegrityError> checkContentsReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        Map<Integer, DbStructureDto.Field> fieldsByRanks = fieldsByRanksByTopicObjects.get(topicObject);

        List<IntegrityError> integrityErrors = new ArrayList<>();

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

    private void checkResourceValuesForReference(String reference, DbDto.Topic sourceTopic, List<IntegrityError> integrityErrors, Map<String, Integer> resourceValueCounter) {
        if (resourceValueCounter.size() > 1) {
            Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
            informations.put(SOURCE_TOPIC, sourceTopic);
            informations.put(REFERENCE, reference);
            informations.put(PER_VALUE_COUNT, resourceValueCounter);

            integrityErrors.add(IntegrityError.builder()
                            .ofType(RESOURCE_VALUES_DIFFERENT_BETWEEN_LOCALES)
                            .addInformations(informations)
                            .build()
            );
        }
    }

    private void updateResourceValueCounter(Map<String, Integer> resourceValueCounter, Optional<DbResourceDto.Entry> potentialResourceEntry) {
        String resourceValue = potentialResourceEntry.get().getValue();
        int valueCount = 0;
        if (resourceValueCounter.containsKey(resourceValue)) {
            valueCount = resourceValueCounter.get(resourceValue);
        }
        resourceValueCounter.put(resourceValue, ++valueCount);
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

    Map<String, DbDto> getTopicObjectsByReferences() {
        return topicObjectsByReferences;
    }

    Map<DbDto, Map<Integer, DbStructureDto.Field>> getFieldsByRanksByTopicObjects() {
        return fieldsByRanksByTopicObjects;
    }
}