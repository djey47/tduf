package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorInfoEnum.*;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Class providing methods to check Database integrity.
 */
public class DatabaseIntegrityChecker {

    private final List<DbDto> dbDtos;
    private final BulkDatabaseMiner bulkDatabaseMiner;

    private Map<String, DbDto> topicObjectsByReferences;
    private Map<DbDto, Map<Integer, DbStructureDto.Field>> fieldsByRanksByTopicObjects;

    private DatabaseIntegrityChecker(List<DbDto> dbDtos) {
        this.dbDtos = requireNonNull(dbDtos, "A list of database objects is required.");
        this.bulkDatabaseMiner = BulkDatabaseMiner.load(dbDtos);

        checkRequirements();
        buildIndexes();
    }

    /**
     * Single entry point for this checker.
     * @param dbDtos    : per topic, database objects
     * @return a {@link DatabaseIntegrityChecker} instance.
     */
    public static DatabaseIntegrityChecker load(List<DbDto> dbDtos) {
        return new DatabaseIntegrityChecker(dbDtos);
    }

    /**
     * Process checking over all loaded database objects.
     * @return list of integrity errors.
     */
    //TODO long method - optimize it
    public List<IntegrityError> checkAllContentsObjects() {

        List<IntegrityError> integrityErrors = new ArrayList<>();

        dbDtos.stream()

                .forEach((localTopicObject) -> checkContentsObject(localTopicObject, integrityErrors));

       return integrityErrors;
    }

    private void checkRequirements() {
        checkIfAllTopicObjectsPresent();
    }

    private void checkIfAllTopicObjectsPresent() {
        List<DbDto.Topic> absentTopics = asList(DbDto.Topic.values()).stream()

                .filter((topicEnum) -> !bulkDatabaseMiner.getDatabaseTopic(topicEnum).isPresent())

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

        DbDto.Topic currentTopic = localTopicObject.getStructure().getTopic();

        switch (field.getFieldType()) {
            case REFERENCE:
                integrityErrors.addAll(checkContentsReference(item.getRawValue(), remoteTopicObject, currentTopic));
                break;
            case RESOURCE_CURRENT:
            case RESOURCE_CURRENT_AGAIN:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), localTopicObject, currentTopic));
                break;
            case RESOURCE_REMOTE:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), remoteTopicObject, currentTopic));
                break;
            default:
                break;
        }
    }

    private List<IntegrityError> checkResourceReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // Through all language resources
        topicObject.getResources().stream()

                .forEach((resourceDto) -> {

                    boolean isResourceReferenceFound = bulkDatabaseMiner.getResourceEntryFromTopicAndLocaleWithReference(reference, topicObject.getStructure().getTopic(), resourceDto.getLocale()).isPresent();
                    if (!isResourceReferenceFound) {
                        Map<IntegrityError.ErrorInfoEnum, Object> informations = new HashMap<>();
                        informations.put(SOURCE_TOPIC, sourceTopic);
                        informations.put(REMOTE_TOPIC, topicObject.getStructure().getTopic());
                        informations.put(LOCALE, resourceDto.getLocale());
                        informations.put(REFERENCE, reference);

                        integrityErrors.add(IntegrityError.builder()
                                        .ofType(RESOURCE_REFERENCE_NOT_FOUND)
                                        .addInformations(informations)
                                        .build()
                        );
                    }

                });

        return integrityErrors;
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
            informations.put(REMOTE_TOPIC, topicObject.getStructure().getTopic());
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
        topicObjectsByReferences = dbDtos.stream()

                .collect( toMap((dto) -> dto.getStructure().getRef(), (dto) -> dto) );

        // Structure
        fieldsByRanksByTopicObjects = dbDtos.stream()

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